package com.goodtrendltd.HolySongs

import android.accessibilityservice.AccessibilityService
import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.graphics.PointF
import android.view.MotionEvent
import android.os.SystemClock
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.ui.semantics.ScrollAxisRange
import androidx.compose.ui.semantics.SemanticsConfiguration
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.goodtrendltd.HolySongs.data.SongCatalogLoader
import com.goodtrendltd.HolySongs.ui.ABOUT_CONTENT
import com.goodtrendltd.HolySongs.helpers.HanziHelper
import kotlinx.coroutines.Dispatchers
import org.json.JSONObject
import org.junit.After
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayInputStream
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

/**
 * Black-box checks for the real MainActivity content.  This rule deliberately does not call
 * setContent: MainActivity owns the Compose content under test.  The API37 fixture is the
 * changed-toolchain legacy capture, not a host-generated ordering golden.
 *
 * These tests are authored before the MainActivity/SongCatalog replacement.  Until that
 * replacement exists, missing production types and the catalogLoaderFactory seam are expected
 * compilation blockers; they are not runtime results.
 */
@RunWith(AndroidJUnit4::class)
class SongListScreenTest {
    @get:Rule
    val composeRule = createEmptyComposeRule()

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val targetContext = instrumentation.targetContext
    private var scenario: ActivityScenario<MainActivity>? = null
    private val activeMonitors = mutableListOf<Instrumentation.ActivityMonitor>()
    private val launchedDestinations = mutableListOf<Activity>()

    @Before
    fun verifyIsolatedTarget() {
        assertEquals("com.goodtrendltd.HolySongs", targetContext.packageName)
    }

    @After
    fun closeActivityAndRestoreFactory() {
        finishTrackedDestinations()
        closeCurrentScenario()
        activeMonitors.toList().forEach { instrumentation.removeMonitor(it) }
        activeMonitors.clear()
    }

    @Test
    fun api37FixtureOrderWinnersAndEveryLegacyLetterDestinationAreReachable() {
        launchMain()
        waitForReady()
        val rows = baselineRows()
        assertEquals(414, rows.size)

        // Lazy content is checked one row at a time through the list container. A detached
        // offscreen row cannot receive performScrollTo() because it is absent from semantics.
        rows.forEach { row ->
            composeRule.onNodeWithTag("song-list").performScrollToIndex(row.position)
            composeRule.onNodeWithText(row.title, useUnmergedTree = true).assertIsDisplayed()
            val visible = visibleSongTitles(rows.map { it.title })
            val start = rows.indexOfFirst { it.title == visible.first() }
            assertEquals("Visible rows retain fixture order", rows.drop(start).take(visible.size).map { it.title }, visible)
        }
        // Verify the ends separately after the full enumeration; both cannot be displayed at
        // once in a lazy viewport.
        composeRule.onNodeWithTag("song-list").performScrollToIndex(rows.first().position)
        assertEquals(rows.first().title, firstVisibleSongTitle(rows.map { it.title }))
        composeRule.onNodeWithTag("song-list").performScrollToIndex(rows.last().position)
        // Lazy lists clamp at their content end: the final row need not be the FIRST row.
        composeRule.onNodeWithText(rows.last().title, useUnmergedTree = true).assertIsDisplayed()

        val letters = baselineLetters()
        assertEquals(('A'..'Z').map(Char::toString), letters.map { it.letter })
        letters.forEach { expected ->
            exposeLetter(expected.letter)
            val letter = letterNode(expected.letter)
            letter.assert(
                hasText(expected.letter, substring = false) or
                    hasContentDescription(expected.letter)
            ).assert(hasClickAction())
                .performClick()
            rows[expected.position].let { destination ->
                // Absent I/O/U/V requests retain the legacy destination; the indicator reflects
                // that destination's guarded HanziHelper initial, not the fabricated request.
                waitForIndicator(actualInitial(destination.title))
                waitForLetterDestination(destination.title, rows.map { it.title })
            }
        }

        // All eight duplicate winners are opened through the real Main -> retained reader
        // boundary, rather than by reconstructing the reader in a test host.
        duplicateWinners().forEach { expected ->
            val readerMonitor = classMonitor(DisplayLyricActivity::class.java)
            addMonitor(readerMonitor)
            scrollSongTo(expected.title)
            composeRule.onNodeWithText(expected.title, useUnmergedTree = true).performClick()
            val reader = readerMonitor.waitForActivityWithTimeout(5_000)
                ?: error("${DisplayLyricActivity::class.java.name} was not launched")
            launchedDestinations += reader
            removeMonitor(readerMonitor)
            assertEquals(expected.title, reader.intent.getStringExtra(MainActivity.SONG_NAME))
            assertEquals(expected.lyric, reader.intent.getStringExtra(MainActivity.LYRIC))
            sendSystemBack()
            waitForMainResumed()
        }
    }

    @Test
    fun catalogLoadsOnceAndRestoresLazyPositionAcrossRecreation() {
        val openerCalls = AtomicInteger()
        val originalFactory = MainActivity.catalogLoaderFactory
        try {
            MainActivity.catalogLoaderFactory = { _ ->
                SongCatalogLoader(
                    openAsset = {
                        openerCalls.incrementAndGet()
                        ByteArrayInputStream(minimalCatalogXml(64).toByteArray(Charsets.UTF_8))
                    },
                    dispatcher = Dispatchers.IO
                )
            }
            launchMain()
            waitForReady("测试歌0")
            assertEquals(1, openerCalls.get())
            scrollSongTo("测试歌32")
            val beforeRecreate = songListScrollValue()
            composeRule.onNodeWithText("测试歌32", useUnmergedTree = true).assertIsDisplayed()

            scenario!!.recreate()
            waitForReadyWithoutScrolling()
            waitForVisibleTitle("测试歌32")
            assertEquals(
                "Activity recreation must retain the ViewModel-owned catalog load",
                1,
                openerCalls.get()
            )
            assertEquals(
                "recreation must restore a nonzero semantic list position",
                beforeRecreate,
                songListScrollValue(),
                0.5f
            )
            scenario!!.moveToState(androidx.lifecycle.Lifecycle.State.CREATED)
            scenario!!.moveToState(androidx.lifecycle.Lifecycle.State.RESUMED)
            waitForReadyWithoutScrolling()
            waitForVisibleTitle("测试歌32")
            assertEquals("Resume must not reload the catalog", 1, openerCalls.get())
            assertTrue("restored position must remain nonzero", songListScrollValue() > 0f)
        } finally {
            closeCurrentScenario()
            MainActivity.catalogLoaderFactory = originalFactory
        }
    }

    @Test
    fun loadingSurvivesRecreationAndErrorRetriesOnlyAfterExplicitRetry() {
        val release = CountDownLatch(1)
        val loadingFactory = MainActivity.catalogLoaderFactory
        try {
            MainActivity.catalogLoaderFactory = { _ ->
                SongCatalogLoader(
                    openAsset = {
                        check(release.await(10, TimeUnit.SECONDS)) { "loading gate timed out" }
                        ByteArrayInputStream(minimalCatalogXml(1).toByteArray(Charsets.UTF_8))
                    },
                    dispatcher = Dispatchers.IO
                )
            }
            launchMain()
            composeRule.waitUntil(5_000) {
                composeRule.onAllNodesWithTag("catalog-loading").fetchSemanticsNodes().isNotEmpty()
            }
            scenario!!.recreate()
            release.countDown()
            waitForReady("测试歌0")
            composeRule.onNodeWithText("测试歌0", useUnmergedTree = true).assertIsDisplayed()
        } finally {
            release.countDown()
            closeCurrentScenario()
            MainActivity.catalogLoaderFactory = loadingFactory
        }

        val attempts = AtomicInteger()
        val errorFactory = MainActivity.catalogLoaderFactory
        try {
            MainActivity.catalogLoaderFactory = { _ ->
                SongCatalogLoader(
                    openAsset = {
                        if (attempts.getAndIncrement() == 0) {
                            throw IOException("controlled catalog failure")
                        }
                        ByteArrayInputStream(minimalCatalogXml(1).toByteArray(Charsets.UTF_8))
                    },
                    dispatcher = Dispatchers.IO
                )
            }
            launchMain()
            waitForTag("catalog-error")
            assertEquals(
                "Showing Error must not start an implicit second load",
                1,
                attempts.get()
            )
            composeRule.onNode(
                (hasText("重试", substring = false) or hasContentDescription("重试")),
                useUnmergedTree = false
            ).assert(hasClickAction()).performClick()
            waitForReady("测试歌0")
            assertEquals(
                "Only the explicit retry may start the second load",
                2,
                attempts.get()
            )
        } finally {
            closeCurrentScenario()
            MainActivity.catalogLoaderFactory = errorFactory
        }
    }

    @Test
    fun emptyCatalogIsSafeAndHasNoLetterNavigation() {
        val originalFactory = MainActivity.catalogLoaderFactory
        try {
            MainActivity.catalogLoaderFactory = { _ ->
                SongCatalogLoader(
                    openAsset = { ByteArrayInputStream("<songs></songs>".toByteArray()) },
                    dispatcher = Dispatchers.IO
                )
            }
            launchMain()
            waitForTag("catalog-empty")
            composeRule.onAllNodesWithTag("letter-sidebar").assertCountEquals(0)
            composeRule.onAllNodesWithTag("letter-A").assertCountEquals(0)
        } finally {
            closeCurrentScenario()
            MainActivity.catalogLoaderFactory = originalFactory
        }
    }

    @Test
    fun sidebarLabelsAndCancellationUseStandardSemantics() {
        launchMain()
        waitForReady()
        val a = letterNode("A")
        a.assert(
            hasText("A", substring = false) or hasContentDescription("A")
        ).assert(hasClickAction())
        a.performClick()
        val fixtureRows = baselineRows()
        waitForIndicator("A")
        waitForLetterDestination(
            fixtureRows[baselineLetters().first().position].title,
            fixtureRows.map { it.title }
        )
        waitForIndicatorDismissed()
        // Use the same A coordinates for the pending gesture. Cancellation must preserve the
        // already committed destination; persistent selected semantics are not required.
        a.performTouchInput {
            down(center)
            cancel()
        }
        waitForLetterDestination(
            fixtureRows[baselineLetters().first().position].title,
            fixtureRows.map { it.title }
        )
    }

    @Test
    fun fitModeMeasuredTapAndDragDoNotScrollTheRail() {
        val requestedMode = InstrumentationRegistry.getArguments().getString("sidebarMode")
        assumeTrue(
            "Pass -e sidebarMode=fit on the disposable fit-mode API37 window",
            requestedMode == "fit"
        )
        launchMain()
        waitForReady()
        val rail = composeRule.onNodeWithTag("letter-sidebar")
        val range = verticalRange(rail)
        assertFitGeometry(rail)
        assertTrue("fit-mode rail must not expose scrolling", range == null || range.maxValue() == 0f)

        // Fit mode must select from both tap and drag, while its rail has no scroll axis.
        val fixtureRows = baselineRows()
        val centers = measuredLetterCenters()
        val heldDown = pointerDown(centers.getValue("A"))
        assertEquals("fit down must select A before release", "A", waitForAnyIndicator())
        pointerMove(centers.getValue("A"), centers.getValue("D"), heldDown)
        waitForIndicator("D") // Must reach D while held, before UP, not merely show any old letter.
        pointerUp(centers.getValue("A"), centers.getValue("D"), heldDown)
        waitForLetterDestination(
            fixtureRows[baselineLetters().first { it.letter == "D" }.position].title,
            fixtureRows.map { it.title }
        )
        waitForIndicatorDismissed()
        letterNode("A").performClick()
        waitForIndicator("A")
        waitForLetterDestination(
            fixtureRows[baselineLetters().first().position].title,
            fixtureRows.map { it.title }
        )
        assertTrue("fit-mode rail must not scroll", verticalRange(rail)?.maxValue() ?: 0f == 0f)
        scrollSongTo(fixtureRows[100].title)
        waitForIndicatorDismissed()
        letterNode("A").performClick()
        waitForLetterDestination(fixtureRows.first().title, fixtureRows.map { it.title })
    }

    @Test
    fun compactModeTapSlopDragAndRailOnlyScrollingKeepSongListStable() {
        val requestedMode = InstrumentationRegistry.getArguments().getString("sidebarMode")
        assumeTrue(
            "Pass -e sidebarMode=compact on the disposable compact API37 window",
            requestedMode == "compact"
        )
        launchMain()
        waitForReady()
        val rail = composeRule.onNodeWithTag("letter-sidebar")
        val range = checkNotNull(verticalRange(rail))
        assertTrue("compact mode must expose a finite measured rail range", range.maxValue().isFinite())
        assertTrue("compact mode must expose a positive measured rail range", range.maxValue() > 0f)
        assertMeasuredRowsNonZero()
        val fixtureRows = baselineRows()
        val listBefore = songListScrollValue()
        val slop = ViewConfiguration.get(targetContext).scaledTouchSlop.toFloat()
        assertTrue("platform touch slop must be measurable", slop > 0f)
        exposeLetter("A")
        val a = measuredLetterCenters().getValue("A")

        settleInitialIndicator()
        // Down and an under-slop move do not change list state or navigate until release.
        val underDown = pointerDown(a)
        assertEquals(listBefore, songListScrollValue())
        assertNoIndicator()
        pointerMove(a, PointF(a.x, a.y + (slop - 1f).coerceAtLeast(1f)), underDown)
        assertEquals(listBefore, songListScrollValue())
        assertNoIndicator()
        pointerUp(a, PointF(a.x, a.y + (slop - 1f).coerceAtLeast(1f)), underDown)
        waitForIndicator("A")
        waitForLetterDestination(
            fixtureRows[baselineLetters().first().position].title,
            fixtureRows.map { it.title }
        )
        waitForIndicatorDismissed()

        // A past-slop drag changes only the rail. Its release must not select a letter or move
        // the song list, including the release event.
        val listBeforeDrag = songListScrollValue()
        exposeLetter("A")
        val visibleCenters = measuredLetterCenters().values.toList()
        val dragA = visibleCenters[visibleCenters.size / 2]
        val railBeforeDrag = checkNotNull(verticalRange(rail)).value()
        val dragEnd = PointF(dragA.x, dragA.y - slop - 40f)
        val dragDown = pointerDown(dragA)
        assertEquals(listBeforeDrag, songListScrollValue())
        pointerMove(dragA, dragEnd, dragDown)
        assertEquals(listBeforeDrag, songListScrollValue())
        assertNoIndicator()
        pointerUp(dragA, dragEnd, dragDown)
        composeRule.waitUntil(3_000) { checkNotNull(verticalRange(rail)).value() > railBeforeDrag }
        assertEquals(listBeforeDrag, songListScrollValue())
        assertNoIndicator()

        // Cancellation is also inert. Then expose A/Z through rail scrolling only.
        exposeLetter("A")
        val cancelDown = pointerDown(measuredLetterCenters().getValue("A"))
        assertEquals(listBeforeDrag, songListScrollValue())
        pointerCancel(measuredLetterCenters().getValue("A"), cancelDown)
        assertEquals(listBeforeDrag, songListScrollValue())
        assertNoIndicator()
        rail.performScrollToNode(hasText("Z"))
        letterNode("Z").performClick()
        waitForIndicator("Z")
        waitForLetterDestination(
            fixtureRows[baselineLetters().last().position].title,
            fixtureRows.map { it.title }
        )
    }

    @Test
    fun sidebarResizeAndModeTransitionCancelPendingInputWhenOptedIn() {
        assumeTrue(
            "Pass -e allowSidebarResize=true on the disposable API37 target",
            InstrumentationRegistry.getArguments().getString("allowSidebarResize") == "true"
        )
        assertEquals("resize transition must begin from compact mode", "compact",
            InstrumentationRegistry.getArguments().getString("sidebarMode"))
        val originalSize = shell("wm size")
        val originalDensity = shell("wm density")
        val originalFontScale = shell("settings get system font_scale").trim()
        try {
            launchMain()
            waitForReady()
            val rail = composeRule.onNodeWithTag("letter-sidebar")
            val beforeRail = checkNotNull(verticalRange(rail))
            assertTrue("resize must begin with compact rail range", beforeRail.maxValue() > 0f)
            val fixtureRows = baselineRows()
            val beforeTitle = checkNotNull(firstVisibleSongTitle(fixtureRows.map { it.title }))
            exposeLetter("A")
            val pendingPoint = measuredLetterCenters().getValue("A")
            val pendingDown = pointerDown(pendingPoint)
            assertEquals(beforeTitle, firstVisibleSongTitle(fixtureRows.map { it.title }))
            assertNoIndicator()
            shell("wm size 1344x4200")
            shell("wm density 420")
            shell("settings put system font_scale 1.0")
            composeRule.waitForIdle()
            scenario!!.recreate()
            waitForReadyWithoutScrolling()
            // The pointer began before the mode/size transition. Release as cancellation on the
            // resized host and require no extra destination or list movement.
            pointerCancel(pendingPoint, pendingDown)
            assertFitGeometry(composeRule.onNodeWithTag("letter-sidebar"))
            assertNoIndicator()
            assertEquals(beforeTitle, firstVisibleSongTitle(fixtureRows.map { it.title }))
        } finally {
            closeCurrentScenario()
            restoreShellOverride("wm size", originalSize, "size")
            restoreShellOverride("wm density", originalDensity, "density")
            if (originalFontScale == "null" || originalFontScale.isEmpty()) {
                shell("settings delete system font_scale")
            } else {
                shell("settings put system font_scale $originalFontScale")
            }
        }
    }

    @Test
    fun sameModeResizeCancelsPendingCompactPointerWhenOptedIn() {
        assumeTrue(
            "Pass -e allowSidebarResize=true on the disposable API37 target",
            InstrumentationRegistry.getArguments().getString("allowSidebarResize") == "true"
        )
        assertEquals("same-mode resize must begin from compact mode", "compact",
            InstrumentationRegistry.getArguments().getString("sidebarMode"))
        val originalSize = shell("wm size")
        try {
            launchMain()
            waitForReady()
            val rail = composeRule.onNodeWithTag("letter-sidebar")
            assertTrue("same-mode resize must begin compact", checkNotNull(verticalRange(rail)).maxValue() > 0f)
            val fixtureRows = baselineRows()
            val beforeTitle = checkNotNull(firstVisibleSongTitle(fixtureRows.map { it.title }))
            exposeLetter("A")
            val pendingPoint = measuredLetterCenters().getValue("A")
            val pendingDown = pointerDown(pendingPoint)
            assertNoIndicator()

            // Keep the rail compact while changing its actual height. Release the original
            // pointer after the window change; a stale pointer coroutine would incorrectly tap A.
            shell("wm size 1080x1000")
            val changedSize = shell("wm size")
            assertTrue("same-mode resize must change the actual window size",
                Regex("Override size: 1080x1000").containsMatchIn(changedSize))
            composeRule.waitForIdle()
            pointerUp(pendingPoint, pendingPoint, pendingDown)
            assertNoIndicator()
            assertEquals(beforeTitle, firstVisibleSongTitle(fixtureRows.map { it.title }))
        } finally {
            closeCurrentScenario()
            restoreShellOverride("wm size", originalSize, "size")
        }
    }

    @Test
    fun indicatorDismissesAfterTwoSecondsAndDisappearsWhenMainStops() {
        launchMain()
        waitForReady()
        letterNode("A").performClick()
        waitForIndicator("A")
        // Compose effects use the rule's controlled clock; wall-clock polling alone does not
        // advance delay(2_000) under instrumentation.
        composeRule.mainClock.advanceTimeBy(2_100)
        composeRule.waitForIdle()
        assertNoIndicator()
        letterNode("B").performClick()
        waitForIndicator("B")
        scenario!!.moveToState(androidx.lifecycle.Lifecycle.State.CREATED)
        // A stopped Activity has no attached Compose hierarchy on this runner. Resume it only
        // to inspect that ON_STOP dismissed the indicator and did not replay it.
        scenario!!.moveToState(androidx.lifecycle.Lifecycle.State.RESUMED)
        composeRule.waitForIdle()
        assertNoIndicator()
    }

    @Test
    fun retainedDestinationsAndAppShareUseRealActivitiesWithoutReplayedActions() {
        launchMain()
        waitForReady()
        val retainedTitle = baselineRows()[32].title
        scrollSongTo(retainedTitle)
        val beforeSettings = songListScrollValue()
        try {
            val settingsMonitor = classMonitor(SettingsActivity::class.java)
            addMonitor(settingsMonitor)
            clickLabeledAction(getString(R.string.settings_text))
            val settings = checkNotNull(settingsMonitor.waitForActivityWithTimeout(5_000))
            launchedDestinations += settings
            sendSystemBack()
            removeMonitor(settingsMonitor)
            waitForMainResumed()
            assertEquals(
                "Settings Back must retain list position",
                beforeSettings,
                songListScrollValue(),
                0.5f
            )
            waitForVisibleTitle(retainedTitle)

            val aboutMonitor = classMonitor(AboutActivity::class.java)
            addMonitor(aboutMonitor)
            clickLabeledAction(getString(R.string.about))
            val about = checkNotNull(aboutMonitor.waitForActivityWithTimeout(5_000))
            launchedDestinations += about
            assertAboutLinks(about)
            sendSystemBack()
            removeMonitor(aboutMonitor)
            waitForMainResumed()
            assertEquals(
                "About Back must retain list position",
                beforeSettings,
                songListScrollValue(),
                0.5f
            )
            waitForVisibleTitle(retainedTitle)

            val shareMonitor = BlockingLaunchMonitor { intent ->
                intent.action == Intent.ACTION_CHOOSER || intent.action == Intent.ACTION_SEND
            }
            addMonitor(shareMonitor)
            clickLabeledAction(getString(R.string.sharing_app_text))
            composeRule.waitUntil(5_000) { shareMonitor.startedIntent != null }
            assertEquals("exactly one outbound share launch before recreation", 1, shareMonitor.launchCount.get())
            val chooser = checkNotNull(shareMonitor.startedIntent)
            assertEquals(Intent.ACTION_CHOOSER, chooser.action)
            val send = chooser.getParcelableExtraCompat(Intent.EXTRA_INTENT)
            val sendIntent = checkNotNull(send)
            assertEquals(Intent.ACTION_SEND, sendIntent.action)
            assertEquals("text/plain", sendIntent.type)
            val expectedApp = baselineJson().getJSONObject("observations")
                .getJSONObject("N1-N2").getJSONObject("N2-APP")
            assertEquals(expectedApp.getString("subject"), sendIntent.getStringExtra(Intent.EXTRA_SUBJECT))
            assertEquals(expectedApp.getString("text"), sendIntent.getStringExtra(Intent.EXTRA_TEXT))
            scenario!!.recreate()
            waitForReadyWithoutScrolling()
            waitForVisibleTitle(retainedTitle)
            assertEquals(
                "recreation/resume must not replay app sharing",
                1,
                shareMonitor.launchCount.get()
            )
            removeMonitor(shareMonitor)
        } finally {
            // This smoke test does not mutate preferences; there is intentionally no restore write.
            activeMonitors.toList().forEach { removeMonitor(it) }
        }
    }

    /** The settings toggle is deliberately opt-in because it mutates the disposable app store. */
    @Test
    fun legacySettingsThemeChangeReturnsToTheSameMainInstanceWhenOptedIn() {
        assumeTrue(
            "Pass -e allowPreferenceMutation=true on the disposable API37 target",
            InstrumentationRegistry.getArguments().getString("allowPreferenceMutation") == "true"
        )
        val originalFactory = MainActivity.catalogLoaderFactory
        val openerCalls = AtomicInteger()
        val originalPrefs = snapshotPreferences()
        try {
            seedKnownPreferenceTypes()
            MainActivity.catalogLoaderFactory = { _ ->
                SongCatalogLoader(
                    openAsset = {
                        openerCalls.incrementAndGet()
                        ByteArrayInputStream(minimalCatalogXml(64).toByteArray(Charsets.UTF_8))
                    },
                    dispatcher = Dispatchers.IO
                )
            }
            launchMain()
            waitForReady("测试歌0")
            scrollSongTo("测试歌32")
            val beforePosition = songListScrollValue()
            var mainBefore: MainActivity? = null
            scenario!!.onActivity { mainBefore = it }
            val nightBefore = targetContext.getSharedPreferences(
                getString(R.string.app_pref), Context.MODE_PRIVATE
            ).getBoolean(getString(R.string.night_mode_pref_key), true)
            val monitor = classMonitor(SettingsActivity::class.java)
            addMonitor(monitor)
            clickLabeledAction(getString(R.string.settings_text))
            val settings = checkNotNull(monitor.waitForActivityWithTimeout(5_000))
            launchedDestinations += settings
            composeRule.waitUntil(5_000) {
                var ready = false
                instrumentation.runOnMainSync {
                    ready = settings.findViewById<View>(R.id.nightModeSwitch) != null
                }
                ready
            }
            var switch: View? = null
            onMain { switch = settings.findViewById(R.id.nightModeSwitch) }
            checkNotNull(switch)
            onMain { switch!!.performClick() }
            sendSystemBack()
            removeMonitor(monitor)
            waitForMainResumed()
            var mainAfter: MainActivity? = null
            scenario!!.onActivity { mainAfter = it }
            assertTrue("ordinary Back must return to the existing Main", mainBefore === mainAfter)
            assertEquals("legacy theme toggle must update raw preference", !nightBefore,
                targetContext.getSharedPreferences(getString(R.string.app_pref), Context.MODE_PRIVATE)
                    .getBoolean(getString(R.string.night_mode_pref_key), true))
            assertEquals("theme return must retain list position", beforePosition, songListScrollValue(), 0.5f)
            waitForVisibleTitle("测试歌32")
            assertEquals("theme return must not reload catalog", 1, openerCalls.get())
        } finally {
            finishTrackedDestinations()
            closeCurrentScenario()
            MainActivity.catalogLoaderFactory = originalFactory
            restorePreferences(originalPrefs)
        }
    }

    private fun launchMain() {
        closeCurrentScenario()
        scenario = ActivityScenario.launch(MainActivity::class.java)
        composeRule.waitForIdle()
    }

    private fun waitForReady(expectedTitle: String? = null) {
        composeRule.waitUntil(30_000) {
            val loading = composeRule.onAllNodesWithTag("catalog-loading").fetchSemanticsNodes().isNotEmpty()
            val error = composeRule.onAllNodesWithTag("catalog-error").fetchSemanticsNodes().isNotEmpty()
            val list = composeRule.onAllNodesWithTag("song-list").fetchSemanticsNodes()
            if (loading || error || list.isEmpty()) return@waitUntil false
            val title = expectedTitle ?: baselineRows().first().title
            runCatching {
                // Readiness never scrolls. A first-launch expected row must already be visible.
                composeRule.onNodeWithText(title, useUnmergedTree = true).assertIsDisplayed()
                true
            }.getOrDefault(false)
        }
    }

    private fun waitForReadyWithoutScrolling() {
        composeRule.waitUntil(30_000) {
            val loading = composeRule.onAllNodesWithTag("catalog-loading").fetchSemanticsNodes().isNotEmpty()
            val error = composeRule.onAllNodesWithTag("catalog-error").fetchSemanticsNodes().isNotEmpty()
            val list = composeRule.onAllNodesWithTag("song-list").fetchSemanticsNodes()
            !loading && !error && list.isNotEmpty() && songListScrollRange() != null
        }
    }

    private fun scrollSongTo(title: String) {
        composeRule.onNodeWithTag("song-list").performScrollToNode(hasText(title))
        composeRule.onNodeWithText(title, useUnmergedTree = true).assertIsDisplayed()
    }

    private fun waitForVisibleTitle(title: String) {
        composeRule.waitUntil(5_000) {
            runCatching {
                composeRule.onNodeWithText(title, useUnmergedTree = true).assertIsDisplayed()
                true
            }.getOrDefault(false)
        }
    }

    private fun waitForLetterDestination(title: String, titles: List<String>) {
        // This helper never scrolls the song list. A letter test only passes when the actual
        // sidebar action places the captured destination at the lazy list's first position.
        composeRule.waitUntil(5_000) {
            firstVisibleSongTitle(titles) == title
        }
    }

    private fun exposeLetter(letter: String) {
        if (!runCatching { letterNode(letter).assertIsDisplayed(); true }.getOrDefault(false)) {
            // Only the rail is scrolled to expose a compact-mode letter.
            composeRule.onNodeWithTag("letter-sidebar").performScrollToNode(hasText(letter))
        }
    }

    private fun firstVisibleSongTitle(titles: List<String>): String? = visibleSongTitles(titles).firstOrNull()

    private fun visibleSongTitles(titles: List<String>): List<String> {
        val titleSet = titles.toSet()
        val root = composeRule.onNodeWithTag("song-list").fetchSemanticsNode()
        val viewport = root.boundsInRoot
        val rows = mutableListOf<Pair<Float, String>>()
        fun visit(node: androidx.compose.ui.semantics.SemanticsNode) {
            val bounds = node.boundsInRoot
            val text = node.config.read(SemanticsProperties.Text)?.joinToString("") { it.text }
            if (bounds.width > 0 && bounds.height > 0 && bounds.overlaps(viewport) && text in titleSet) {
                rows += bounds.top to checkNotNull(text)
            }
            node.children.forEach { visit(it) }
        }
        visit(root)
        return rows.sortedBy { it.first }.map { it.second }.distinct()
    }

    private fun songListScrollRange(): ScrollAxisRange? = runCatching {
        composeRule.onNodeWithTag("song-list").fetchSemanticsNode()
            .config.read(SemanticsProperties.VerticalScrollAxisRange)
    }.getOrNull()

    private fun songListScrollValue(): Float =
        songListScrollRange()?.value?.invoke() ?: error("song-list has no vertical scroll semantics")

    private fun waitForTag(tag: String) {
        composeRule.waitUntil(30_000) {
            composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitForIndicator(letter: String) {
        composeRule.waitUntil(3_000) {
            runCatching {
                composeRule.onNodeWithTag("letter-indicator").assertTextContains(letter)
                true
            }.getOrDefault(false)
        }
    }

    private fun waitForIndicatorDismissed() {
        composeRule.mainClock.advanceTimeBy(2_100)
        composeRule.waitForIdle()
        assertNoIndicator()
    }

    private fun settleInitialIndicator() {
        if (composeRule.onAllNodesWithTag("letter-indicator").fetchSemanticsNodes().isNotEmpty()) {
            composeRule.mainClock.advanceTimeBy(2_100)
            composeRule.waitForIdle()
        }
        assertNoIndicator()
    }

    private fun assertNoIndicator() {
        composeRule.onAllNodesWithTag("letter-indicator").assertCountEquals(0)
    }

    private fun waitForAnyIndicator(): String {
        var letter = ""
        composeRule.waitUntil(3_000) {
            val nodes = composeRule.onAllNodesWithTag("letter-indicator").fetchSemanticsNodes()
            if (nodes.isEmpty()) return@waitUntil false
            val text = nodes.first().config.read(SemanticsProperties.Text)
                ?.joinToString("") { it.text }.orEmpty()
            letter = text.firstOrNull { it in 'A'..'Z' }?.toString().orEmpty()
            letter.isNotEmpty()
        }
        return letter
    }

    private fun verticalRange(node: SemanticsNodeInteraction): ScrollAxisRange? = runCatching {
        node.fetchSemanticsNode().config.read(SemanticsProperties.VerticalScrollAxisRange)
    }.getOrNull()

    private fun assertFitGeometry(rail: SemanticsNodeInteraction) {
        val railBounds = rail.getUnclippedBoundsInRoot()
        val bounds = ('A'..'Z').map { letter ->
            composeRule.onNodeWithTag("letter-$letter", useUnmergedTree = true)
                .getUnclippedBoundsInRoot()
        }
        assertEquals("fit mode must expose all 26 measured letter rows", 26, bounds.size)
        bounds.forEach { row ->
            assertTrue(
                "letter row must have nonzero measured bounds",
                row.right - row.left > 0.dp && row.bottom - row.top > 0.dp
            )
            assertTrue(
                "letter row must fit inside measured rail",
                row.top >= railBounds.top && row.bottom <= railBounds.bottom
            )
        }
    }

    private fun assertMeasuredRowsNonZero() {
        val bounds = ('A'..'Z').map { letter ->
            composeRule.onNodeWithTag("letter-$letter", useUnmergedTree = true)
                .getUnclippedBoundsInRoot()
        }
        assertEquals("compact mode must measure all 26 letter rows", 26, bounds.size)
        bounds.forEach { row ->
            assertTrue("letter row width must be measured", row.right - row.left > 0.dp)
            assertTrue("letter row height must be measured", row.bottom - row.top > 0.dp)
        }
    }

    private fun measuredLetterCenters(): Map<String, PointF> {
        var location = IntArray(2)
        scenario!!.onActivity { activity ->
            location = IntArray(2)
            activity.window.decorView.getLocationOnScreen(location)
        }
        val density = composeRule.density.density
        return ('A'..'Z').mapNotNull { letter ->
            val node = composeRule.onNodeWithTag("letter-$letter", useUnmergedTree = true)
            if (!runCatching { node.assertIsDisplayed(); true }.getOrDefault(false)) return@mapNotNull null
            val bounds = node.getUnclippedBoundsInRoot()
            letter.toString() to PointF(
                location[0] + ((bounds.left.value + bounds.right.value) / 2f * density),
                location[1] + ((bounds.top.value + bounds.bottom.value) / 2f * density)
            )
        }.toMap()
    }

    private fun injectDrag(from: PointF, to: PointF) {
        val downTime = SystemClock.uptimeMillis()
        sendMotion(MotionEvent.ACTION_DOWN, from, downTime)
        repeat(4) { step ->
            val fraction = (step + 1) / 4f
            sendMotion(
                MotionEvent.ACTION_MOVE,
                PointF(
                    from.x + (to.x - from.x) * fraction,
                    from.y + (to.y - from.y) * fraction
                ),
                downTime
            )
        }
        sendMotion(MotionEvent.ACTION_UP, to, downTime)
    }

    private fun pointerDown(point: PointF): Long {
        val downTime = SystemClock.uptimeMillis()
        sendMotion(MotionEvent.ACTION_DOWN, point, downTime)
        return downTime
    }

    private fun pointerMove(start: PointF, point: PointF, downTime: Long) {
        sendMotion(MotionEvent.ACTION_MOVE, point, downTime)
    }

    private fun pointerUp(start: PointF, point: PointF, downTime: Long) {
        sendMotion(MotionEvent.ACTION_UP, point, downTime)
    }

    private fun pointerCancel(point: PointF, downTime: Long) {
        sendMotion(MotionEvent.ACTION_CANCEL, point, downTime)
    }

    private fun sendMotion(action: Int, point: PointF, downTime: Long) {
        val event = MotionEvent.obtain(
            downTime,
            SystemClock.uptimeMillis(),
            action,
            point.x,
            point.y,
            0
        )
        try {
            instrumentation.sendPointerSync(event)
        } finally {
            event.recycle()
        }
    }

    private fun <T> SemanticsConfiguration.read(key: SemanticsPropertyKey<T>): T? =
        if (contains(key)) get(key) else null

    private fun letterNode(letter: String): SemanticsNodeInteraction =
        composeRule.onNodeWithTag("letter-$letter", useUnmergedTree = true)

    private fun clickLabeledAction(label: String) {
        val target = hasText(label, substring = false) or hasContentDescription(label)
        if (runCatching {
                composeRule.onNode(target, useUnmergedTree = false)
                    .assertIsDisplayed()
                    .assert(hasClickAction())
                true
            }.getOrDefault(false)
        ) {
            composeRule.onNode(target, useUnmergedTree = false).performClick()
            return
        }
        composeRule.onNode(
            hasContentDescription(getString(R.string.more_actions)),
            useUnmergedTree = true,
        ).assert(hasClickAction()).performClick()
        composeRule.onNode(target, useUnmergedTree = false)
            .assertIsDisplayed()
            .assert(hasClickAction())
            .performClick()
    }

    private fun waitForMainResumed() {
        // Compose synchronisation is the stable signal here; ActivityScenario.onActivity may
        // observe the retained Main as destroyed while a returned reader transition is settling.
        composeRule.waitUntil(15_000) {
            runCatching {
                composeRule.onAllNodesWithTag("song-list").fetchSemanticsNodes().isNotEmpty()
            }.getOrDefault(false)
        }
        composeRule.waitForIdle()
    }

    private fun sendSystemBack() {
        check(instrumentation.uiAutomation.performGlobalAction(
            AccessibilityService.GLOBAL_ACTION_BACK
        )) { "system Back was not dispatched" }
        waitForMainResumed()
    }

    private fun onMain(block: () -> Unit) {
        instrumentation.runOnMainSync(block)
        instrumentation.waitForIdleSync()
    }

    private fun classMonitor(type: Class<out Activity>) =
        Instrumentation.ActivityMonitor(type.name, null, false)

    private fun addMonitor(monitor: Instrumentation.ActivityMonitor) {
        instrumentation.addMonitor(monitor)
        activeMonitors += monitor
    }

    private fun removeMonitor(monitor: Instrumentation.ActivityMonitor) {
        instrumentation.removeMonitor(monitor)
        activeMonitors.remove(monitor)
    }

    private fun closeCurrentScenario() {
        scenario?.close()
        scenario = null
    }

    private fun finishTrackedDestinations() {
        val destinations = launchedDestinations.toList().asReversed()
        destinations.forEach { activity ->
            onMain {
                if (!activity.isFinishing && !activity.isDestroyed) activity.finish()
            }
        }
        launchedDestinations.clear()
    }

    private fun shell(command: String): String {
        val descriptor = instrumentation.uiAutomation.executeShellCommand(command)
        return android.os.ParcelFileDescriptor.AutoCloseInputStream(descriptor).use {
            it.bufferedReader(Charsets.UTF_8).readText()
        }
    }

    private fun restoreShellOverride(command: String, original: String, kind: String) {
        val pattern = if (kind == "size") "([0-9]+x[0-9]+)" else "([0-9]+)"
        val override = Regex("Override $kind: $pattern").find(original)?.groupValues?.get(1)
        shell(if (override == null) "$command reset" else "$command $override")
    }

    private data class ExpectedRow(val position: Int, val title: String, val lyric: String)
    private data class ExpectedLetter(val letter: String, val position: Int)

    private fun baselineJson(): JSONObject {
        val input = instrumentation.context.assets.open("legacy-api37.json")
        return input.use { JSONObject(it.bufferedReader(Charsets.UTF_8).readText()) }
    }

    private fun actualInitial(title: String): String =
        HanziHelper.words2Pinyin(title)
            .firstOrNull()
            ?.uppercaseChar()
            ?.takeIf { it in 'A'..'Z' }
            ?.toString()
            ?: error("No guarded A-Z initial for destination $title")

    private fun baselineRows(): List<ExpectedRow> {
        val array = baselineJson().getJSONObject("observations")
            .getJSONObject("U1").getJSONArray("orderedTitlesAndWinningLyrics")
        return List(array.length()) { index ->
            val row = array.getJSONObject(index)
            ExpectedRow(
                position = row.getInt("position"),
                title = row.getString("title"),
                lyric = row.getString("winningLyric")
            )
        }
    }

    private fun baselineLetters(): List<ExpectedLetter> {
        val array = baselineJson().getJSONObject("observations")
            .getJSONObject("U1-LETTER").getJSONArray("rows")
        return List(array.length()) { index ->
            val row = array.getJSONObject(index)
            ExpectedLetter(row.getString("letter"), row.getJSONObject("destination").getInt("position"))
        }
    }

    private fun duplicateWinners(): List<ExpectedRow> {
        val byTitle = baselineRows().associateBy { it.title }
        return listOf(
            "以色列的圣者", "像天空的鸽子", "全地宣告", "天堂在我心",
            "愿您崇高", "耶稣基督是主", "耶稣耶稣", "轻轻听"
        ).map { title -> byTitle[title] ?: error("missing fixture winner $title") }
    }

    private fun minimalCatalogXml(rowCount: Int): String = buildString {
        append("<songs>")
        repeat(rowCount) { index ->
            append("<song><name>测试歌")
            append(index)
            append("</name><lyric>测试歌词")
            append(index)
            append("</lyric></song>")
        }
        append("</songs>")
    }

    private fun getString(id: Int) = targetContext.getString(id)

    private fun snapshotPreferences(): Map<String, Any?> {
        val preferences = targetContext.getSharedPreferences(
            targetContext.getString(R.string.app_pref), Context.MODE_PRIVATE
        )
        // SharedPreferences.getInt/getBoolean throw for deliberately seeded wrong types. A full
        // raw map preserves missing keys and every legacy primitive without coercion.
        return LinkedHashMap(preferences.all)
    }

    private fun seedKnownPreferenceTypes() {
        val preferences = targetContext.getSharedPreferences(
            targetContext.getString(R.string.app_pref), Context.MODE_PRIVATE
        )
        check(preferences.edit()
            .putInt(getString(R.string.font_size_pref_key), 20)
            .putBoolean(getString(R.string.night_mode_pref_key), true)
            .commit()) { "known preference fixture seed failed" }
    }

    private fun restorePreferences(snapshot: Map<String, Any?>) {
        val preferences = targetContext.getSharedPreferences(
            targetContext.getString(R.string.app_pref), Context.MODE_PRIVATE
        )
        val editor = preferences.edit().clear()
        snapshot.forEach { (key, value) ->
            when (value) {
                null -> Unit
                is Boolean -> editor.putBoolean(key, value)
                is Int -> editor.putInt(key, value)
                is Long -> editor.putLong(key, value)
                is Float -> editor.putFloat(key, value)
                is String -> editor.putString(key, value)
                is Set<*> -> editor.putStringSet(key, value.filterIsInstance<String>().toSet())
                else -> error("Unsupported SharedPreferences fixture type for $key: ${value::class.java}")
            }
        }
        check(editor.commit()) { "preference fixture restoration failed" }
        check(preferences.all == snapshot) { "preference fixture raw map was not restored" }
    }

    private fun assertAboutLinks(activity: Activity) {
        fun findAboutTextView(view: View): TextView? {
            if (view is TextView && view.text.toString() == ABOUT_CONTENT) return view
            if (view is ViewGroup) {
                for (index in 0 until view.childCount) {
                    findAboutTextView(view.getChildAt(index))?.let { return it }
                }
            }
            return null
        }
        composeRule.waitUntil(5_000) {
            var found = false
            instrumentation.runOnMainSync {
                found = findAboutTextView(activity.window.decorView) != null
            }
            found
        }
        lateinit var aboutTextView: TextView
        onMain {
            aboutTextView = checkNotNull(findAboutTextView(activity.window.decorView)) {
                "About content TextView not found"
            }
        }
        val spanned = aboutTextView.text as? android.text.Spanned
            ?: error("About content must retain link spans")
        val links = spanned.getSpans(0, spanned.length, android.text.style.URLSpan::class.java)
            .map { it.url }
        val expected = baselineJson().getJSONObject("observations")
            .getJSONObject("N1-N2").getJSONObject("N1-ABOUT").getJSONObject("about")
        val expectedLinks = List(expected.getJSONArray("linkDestinations").length()) { index ->
            expected.getJSONArray("linkDestinations").getString(index)
        }
        assertEquals(expectedLinks.toSet(), links.toSet())
        // The captured text intentionally includes the website's trailing slash; URLSpan's
        // normalized destination may omit it, so validate both representations separately.
        assertEquals(ABOUT_CONTENT, aboutTextView.text.toString())
        assertEquals(expected.getString("text"), aboutTextView.text.toString())
    }

    private class BlockingLaunchMonitor(
        private val shouldCapture: (Intent) -> Boolean
    ) : Instrumentation.ActivityMonitor() {
        @Volatile
        var startedIntent: Intent? = null
            private set
        val launchCount = AtomicInteger()

        override fun onStartActivity(intent: Intent): Instrumentation.ActivityResult? {
            if (!shouldCapture(intent)) return null
            launchCount.incrementAndGet()
            startedIntent = Intent(intent)
            return Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null)
        }
    }

    @Suppress("DEPRECATION")
    private fun Intent.getParcelableExtraCompat(key: String): Intent? =
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            getParcelableExtra(key, Intent::class.java)
        } else {
            getParcelableExtra(key) as? Intent
        }
}
