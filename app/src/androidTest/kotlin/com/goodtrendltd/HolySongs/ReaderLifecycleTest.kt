package com.goodtrendltd.HolySongs

import android.content.Intent
import android.os.Build
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReaderLifecycleTest {
    @Test
    fun lyricRecreationKeepsTheValidatedSelectionWithoutReplayingNavigation() {
        assumeTrue("Rotation/recreation slice requires the API37 target", Build.VERSION.SDK_INT >= 37)
        val context = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext
        val intent = Intent(context, DisplayLyricActivity::class.java)
            .putExtra(MainActivity.SONG_NAME, "生命周期测试")
            .putExtra(MainActivity.LYRIC, "第一行\n第二行")
        ActivityScenario.launch<DisplayLyricActivity>(intent).use { scenario ->
            scenario.onActivity { assertFalse(it.isFinishing) }
            scenario.recreate()
            scenario.onActivity {
                assertFalse(it.isFinishing)
                assertFalse(it.intent.action == Intent.ACTION_SEND)
            }
        }
    }
}
