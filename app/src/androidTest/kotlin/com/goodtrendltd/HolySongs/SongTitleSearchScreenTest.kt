package com.goodtrendltd.HolySongs

import android.app.Activity
import android.app.Instrumentation
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.goodtrendltd.HolySongs.data.SongCatalogLoader
import com.goodtrendltd.HolySongs.data.StoredSong
import kotlinx.coroutines.Dispatchers
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicInteger

@RunWith(AndroidJUnit4::class)
class SongTitleSearchScreenTest {
    @get:Rule
    val composeRule = createEmptyComposeRule()

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val originalFactory = MainActivity.catalogLoaderFactory
    private val loaderCalls = AtomicInteger()
    private var scenario: ActivityScenario<MainActivity>? = null

    private val rows = listOf(
        StoredSong("Alpha 12", "alpha lyric", 0),
        StoredSong("敬拜！赞美", "敬拜歌词", 1),
        StoredSong("Song-BETA", "beta lyric", 2),
        StoredSong("另一首歌", "另一首歌词", 3),
    )

    @Before
    fun launchReadyMain() {
        MainActivity.catalogLoaderFactory = { _ ->
            SongCatalogLoader(
                readSongs = {
                    loaderCalls.incrementAndGet()
                    rows
                },
                dispatcher = Dispatchers.IO,
            )
        }
        scenario = ActivityScenario.launch(MainActivity::class.java)
        waitForList()
    }

    @After
    fun cleanup() {
        scenario?.close()
        scenario = null
        MainActivity.catalogLoaderFactory = originalFactory
    }

    @Test
    fun searchEntryFocusesFieldFiltersLiveHidesSidebarAndOpensExactLyric() {
        openSearch()
        composeRule.onNodeWithTag("search-query").assertIsFocused()
        composeRule.onAllNodesWithTag("letter-sidebar").assertCountEquals(0)

        composeRule.onNodeWithTag("search-query").performTextReplacement("  beta  ")
        composeRule.onNodeWithText("Song-BETA").assertIsDisplayed()
        composeRule.onAllNodesWithText("Alpha 12").assertCountEquals(0)

        val monitor = Instrumentation.ActivityMonitor(DisplayLyricActivity::class.java.name, null, false)
        instrumentation.addMonitor(monitor)
        try {
            composeRule.onNodeWithText("Song-BETA").performClick()
            val reader = monitor.waitForActivityWithTimeout(5_000)
                ?: error("DisplayLyricActivity was not launched")
            try {
                assertEquals("Song-BETA", reader.intent.getStringExtra(MainActivity.SONG_NAME))
                assertEquals("beta lyric", reader.intent.getStringExtra(MainActivity.LYRIC))
            } finally {
                instrumentation.runOnMainSync { reader.finish() }
            }
        } finally {
            instrumentation.removeMonitor(monitor)
        }
    }

    @Test
    fun clearAndBothExitPathsRestoreNormalBrowsingAndNewSessionIsEmpty() {
        openSearch()
        val query = composeRule.onNodeWithTag("search-query")
        query.performTextReplacement("Alpha")
        composeRule.onNode(hasContentDescription(context.getString(R.string.clear_search_query)))
            .performClick()
        query.assertTextContains("")
        query.assertIsFocused()
        composeRule.onNodeWithText("Alpha 12").assertIsDisplayed()
        composeRule.onAllNodesWithTag("letter-sidebar").assertCountEquals(0)

        composeRule.onNode(hasContentDescription(context.getString(R.string.exit_search))).performClick()
        composeRule.onAllNodesWithTag("search-query").assertCountEquals(0)
        composeRule.onNodeWithTag("letter-sidebar").assertIsDisplayed()

        openSearch()
        composeRule.onNodeWithTag("search-query").assertTextContains("")
        composeRule.onNodeWithTag("search-query").performTextReplacement("敬拜")
        scenario!!.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()
        composeRule.onAllNodesWithTag("search-query").assertCountEquals(0)
        composeRule.onNodeWithTag("letter-sidebar").assertIsDisplayed()

        openSearch()
        composeRule.onNodeWithTag("search-query").assertTextContains("")
    }

    @Test
    fun recreationRetainsActiveQueryWithoutReloadingCatalog() {
        openSearch()
        composeRule.onNodeWithTag("search-query").performTextReplacement("敬拜")
        composeRule.onNodeWithText("敬拜！赞美").assertIsDisplayed()

        scenario!!.recreate()
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithTag("search-query").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("search-query").assertTextContains("敬拜").assertIsFocused()
        composeRule.onNodeWithText("敬拜！赞美").assertIsDisplayed()
        assertEquals(1, loaderCalls.get())
    }

    @Test
    fun noResultsIsExplicitAndEditingOrClearingRecovers() {
        openSearch()
        composeRule.onNodeWithTag("search-query").performTextReplacement("不存在")
        composeRule.onNodeWithTag("search-no-results").assertIsDisplayed()
        composeRule.onNode(hasContentDescription(context.getString(R.string.clear_search_query)))
            .assertIsDisplayed()
        composeRule.onNode(hasContentDescription(context.getString(R.string.exit_search)))
            .assertIsDisplayed()

        composeRule.onNodeWithTag("search-query").performTextReplacement("Alpha")
        composeRule.onAllNodesWithTag("search-no-results").assertCountEquals(0)
        composeRule.onNodeWithText("Alpha 12").assertIsDisplayed()
        composeRule.onNode(hasContentDescription(context.getString(R.string.clear_search_query)))
            .performClick()
        composeRule.onNodeWithText("另一首歌").assertIsDisplayed()
    }

    private fun openSearch() {
        composeRule.onNode(hasContentDescription(context.getString(R.string.more_actions))).performClick()
        composeRule.onNodeWithText(context.getString(R.string.search_songs)).performClick()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("search-query").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitForList() {
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithTag("song-list").fetchSemanticsNodes().isNotEmpty()
        }
    }
}
