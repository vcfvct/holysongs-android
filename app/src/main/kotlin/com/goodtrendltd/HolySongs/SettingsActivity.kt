package com.goodtrendltd.HolySongs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goodtrendltd.HolySongs.data.ReaderPreferenceSnapshot
import com.goodtrendltd.HolySongs.data.ReaderPreferences
import com.goodtrendltd.HolySongs.ui.HolySongsTheme
import com.goodtrendltd.HolySongs.ui.SettingsScreen

/** Compose settings host retaining appPrefFile, fontSize and nightMode semantics. */
class SettingsActivity : ComponentActivity() {
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
                SettingsScreen(
                    preferences = preferences,
                    onFontSizeSelected = readerPreferences::setFontSize,
                    onResetFontSize = readerPreferences::resetFontSize,
                    onNightModeChanged = readerPreferences::setNightMode,
                )
            }
        }
    }
}
