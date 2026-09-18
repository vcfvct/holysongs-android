package com.goodtrendltd.HolySongs

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goodtrendltd.HolySongs.data.BundledSongDatabase
import com.goodtrendltd.HolySongs.data.ReaderPreferenceSnapshot
import com.goodtrendltd.HolySongs.data.ReaderPreferences
import com.goodtrendltd.HolySongs.data.SongCatalogLoader
import com.goodtrendltd.HolySongs.ui.HolySongsTheme
import com.goodtrendltd.HolySongs.ui.SongListScreen
import com.goodtrendltd.HolySongs.ui.SongListViewModel

/** Compose launcher host; legacy reader/settings/about/video Activities remain unchanged here. */
class MainActivity : ComponentActivity() {
    companion object {
        const val LYRIC = "com.goodtrendltd.LYRIC"
        const val SONG_NAME = "com.goodtrendltd.SONG_NAME"

        /** Test-only construction seam; normal UI never changes this factory. */
        internal var catalogLoaderFactory: (Context) -> SongCatalogLoader = { context ->
            val database = BundledSongDatabase(
                openAsset = { context.assets.open("songs.db") },
                cacheDirectory = context.cacheDir,
            )
            SongCatalogLoader(readSongs = database::readSongs)
        }
    }

    private val readerPreferences by lazy { ReaderPreferences(applicationContext) }
    private val listViewModel: SongListViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (!modelClass.isAssignableFrom(SongListViewModel::class.java)) {
                    throw IllegalArgumentException("Unexpected ViewModel ${modelClass.name}")
                }
                return SongListViewModel(catalogLoaderFactory(applicationContext)) as T
            }
        }
    }
    private var externalActionInFlight = false

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
            HolySongsTheme(nightMode = preferences.nightMode) {
                SongListScreen(
                    state = listViewModel.state.collectAsStateWithLifecycle().value,
                    preferences = preferences,
                    onSongSelected = ::navigateToLyric,
                    onSettings = ::openSettings,
                    onAbout = ::openAbout,
                    onShareApp = ::openSharing,
                    onRetry = listViewModel::retry,
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Returning from a retained Activity or chooser must make the transient guard usable
        // again; no navigation/share command is persisted or replayed.
        externalActionInFlight = false
    }

    private fun navigateToLyric(songName: String, lyric: String) {
        if (externalActionInFlight) return
        externalActionInFlight = true
        val intent = Intent(this, DisplayLyricActivity::class.java)
            .putExtra(LYRIC, lyric)
            .putExtra(SONG_NAME, songName)
        try {
            startActivity(intent)
        } catch (failure: ActivityNotFoundException) {
            externalActionInFlight = false
        }
    }

    private fun openSharing() {
        if (externalActionInFlight) return
        externalActionInFlight = true
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "敬拜赞美诗")
            putExtra(
                Intent.EXTRA_TEXT,
                "\n敬拜赞美诗App安装地址: \n\n  安卓：${getString(R.string.app_url_android)} \n\n" +
                    " 苹果：${getString(R.string.app_url_ios)} \n\n",
            )
        }
        try {
            startActivity(Intent.createChooser(intent, "选择用于分享的app"))
        } catch (failure: ActivityNotFoundException) {
            externalActionInFlight = false
        }
    }

    private fun openSettings() {
        if (externalActionInFlight) return
        externalActionInFlight = true
        try {
            startActivity(Intent(this, SettingsActivity::class.java))
        } catch (failure: ActivityNotFoundException) {
            externalActionInFlight = false
        }
    }

    private fun openAbout() {
        if (externalActionInFlight) return
        externalActionInFlight = true
        try {
            startActivity(Intent(this, AboutActivity::class.java))
        } catch (failure: ActivityNotFoundException) {
            externalActionInFlight = false
        }
    }
}
