package com.goodtrendltd.HolySongs

import android.os.SystemClock
import androidx.compose.ui.test.assertCountEquals
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SongTitleSearchPerformanceTest {
    @get:Rule
    val composeRule = createEmptyComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val originalFactory = MainActivity.catalogLoaderFactory
    private var scenario: ActivityScenario<MainActivity>? = null

    @Before
    fun launchFullCatalog() {
        MainActivity.catalogLoaderFactory = { _ ->
            SongCatalogLoader(
                readSongs = {
                    List(414) { index ->
                        StoredSong(
                            title = "测试歌曲${index.toString().padStart(3, '0')}",
                            lyric = "测试歌词$index",
                            sourceOrder = index,
                        )
                    }
                },
                dispatcher = Dispatchers.IO,
            )
        }
        scenario = ActivityScenario.launch(MainActivity::class.java)
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithTag("song-list").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNode(hasContentDescription(context.getString(R.string.more_actions))).performClick()
        composeRule.onNodeWithText(context.getString(R.string.search_songs)).performClick()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("search-query").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @After
    fun cleanup() {
        scenario?.close()
        scenario = null
        MainActivity.catalogLoaderFactory = originalFactory
    }

    @Test
    fun representativeEditsSettleWithinTwoHundredMilliseconds() {
        val cases = listOf(
            "歌曲041" to "测试歌曲041",
            "  歌曲123  " to "测试歌曲123",
            "不存在" to null,
        )
        cases.forEach { (query, expectedTitle) ->
            val started = SystemClock.elapsedRealtimeNanos()
            composeRule.onNodeWithTag("search-query").performTextReplacement(query)
            composeRule.waitUntil(200) {
                if (expectedTitle == null) {
                    composeRule.onAllNodesWithTag("search-no-results").fetchSemanticsNodes().isNotEmpty()
                } else {
                    runCatching {
                        composeRule.onNodeWithText(expectedTitle).fetchSemanticsNode()
                        true
                    }.getOrDefault(false)
                }
            }
            val elapsedMs = (SystemClock.elapsedRealtimeNanos() - started) / 1_000_000L
            assertTrue("Search did not settle within 200 ms for '$query': $elapsedMs ms", elapsedMs < 200L)
        }
    }

    @Test
    fun rapidEditsExposeOnlyTheFinalQueryResult() {
        val query = composeRule.onNodeWithTag("search-query")
        listOf("歌", "歌曲", "歌曲4", "歌曲41", "歌曲413").forEach(query::performTextReplacement)
        composeRule.waitUntil(200) {
            runCatching {
                composeRule.onNodeWithText("测试歌曲413").fetchSemanticsNode()
                true
            }.getOrDefault(false)
        }
        composeRule.onNodeWithText("测试歌曲413").fetchSemanticsNode()
        composeRule.onAllNodesWithText("测试歌曲041").assertCountEquals(0)
    }
}
