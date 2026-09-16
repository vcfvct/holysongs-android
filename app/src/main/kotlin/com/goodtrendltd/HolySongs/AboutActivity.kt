package com.goodtrendltd.HolySongs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goodtrendltd.HolySongs.data.ReaderPreferenceSnapshot
import com.goodtrendltd.HolySongs.data.ReaderPreferences
import com.goodtrendltd.HolySongs.ui.AboutScreen
import com.goodtrendltd.HolySongs.ui.HolySongsTheme

/** Compose About host preserving the legacy text and auto-linked destinations. */
class AboutActivity : ComponentActivity() {
    private val readerPreferences by lazy { ReaderPreferences(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val preferences = readerPreferences.snapshots.collectAsStateWithLifecycle(
                initialValue = ReaderPreferenceSnapshot(
                    fontSize = 20,
                    effectiveFontSize = 20,
                    nightMode = true,
                ),
            ).value
            HolySongsTheme(preferences.nightMode) {
                AboutScreen()
            }
        }
    }
}
