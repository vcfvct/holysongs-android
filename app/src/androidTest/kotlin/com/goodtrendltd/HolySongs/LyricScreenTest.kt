package com.goodtrendltd.HolySongs

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LyricScreenTest {
    @Test
    fun reconstructionUsesTheLegacyStringKeysAndSearchTarget() {
        assertEquals("com.goodtrendltd.SONG_NAME", MainActivity.SONG_NAME)
        assertEquals("com.goodtrendltd.LYRIC", MainActivity.LYRIC)
        assertEquals("com.goodtrendltd.searchTarget", DisplayLyricActivity.SEARCH_TARGET)
    }

    @Test
    fun missingOrWrongTypeExtrasFinishWithoutFallbackContent() {
        val invalidInt = Intent(
            androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext,
            DisplayLyricActivity::class.java,
        ).putExtra(MainActivity.SONG_NAME, 7)
        ActivityScenario.launch<DisplayLyricActivity>(invalidInt).use { scenario ->
            assertEquals(androidx.lifecycle.Lifecycle.State.DESTROYED, scenario.state)
        }
    }

    @Test
    fun validChineseAndLineBreakExtrasRemainTheReaderInput() {
        val title = "重复歌名"
        val lyric = "第一行\n\n第二行"
        val intent = Intent(
            androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext,
            DisplayLyricActivity::class.java,
        ).putExtra(MainActivity.SONG_NAME, title)
            .putExtra(MainActivity.LYRIC, lyric)
        ActivityScenario.launch<DisplayLyricActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                assertFalse(activity.isFinishing)
                assertEquals(title, activity.intent.getStringExtra(MainActivity.SONG_NAME))
                assertEquals(lyric, activity.intent.getStringExtra(MainActivity.LYRIC))
            }
        }
    }
}
