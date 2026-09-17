package com.goodtrendltd.HolySongs

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goodtrendltd.HolySongs.data.ReaderPreferenceSnapshot
import com.goodtrendltd.HolySongs.data.ReaderPreferences
import com.goodtrendltd.HolySongs.ui.HolySongsTheme
import com.goodtrendltd.HolySongs.ui.LyricScreen

/** Compose lyric host retaining the original String extras and provider entry contract. */
class DisplayLyricActivity : ComponentActivity() {
    companion object {
        @JvmField
        val SEARCH_TARGET: String = "com.goodtrendltd.searchTarget"

        @JvmStatic
        fun validStringExtra(extras: Bundle?, key: String?): String? {
            if (extras == null || key == null || !extras.containsKey(key)) return null
            val value = extras.get(key)
            return value as? String
        }
    }

    private val readerPreferences by lazy { ReaderPreferences(applicationContext) }
    private var songName: String = ""
    private var lyric: String = ""
    // Activity-owned one-shot guards prevent recomposition/rapid taps from duplicating effects.
    private val launchGuards = DisplayLyricLaunchGuards()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val extras = intent?.extras
        lyric = validStringExtra(extras, MainActivity.LYRIC) ?: return finishInvalid()
        songName = validStringExtra(extras, MainActivity.SONG_NAME) ?: return finishInvalid()
        if (songName.isEmpty()) return finishInvalid()

        setContent {
            val preferences = readerPreferences.snapshots.collectAsStateWithLifecycle(
                initialValue = ReaderPreferenceSnapshot(
                    fontSize = 20,
                    effectiveFontSize = 20,
                    nightMode = true,
                ),
            ).value
            HolySongsTheme(preferences.nightMode) {
                LyricScreen(
                    title = songName,
                    lyric = lyric,
                    preferences = preferences,
                    onShare = ::shareLyric,
                    onVideo = ::openSearch,
                )
            }
        }
    }

    private fun finishInvalid() {
        finish()
    }

    private fun shareLyric() {
        if (!launchGuards.tryStartShare()) return
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, songName)
            putExtra(Intent.EXTRA_TEXT, lyric)
        }
        try {
            startActivity(Intent.createChooser(send, "选择用于分享的app"))
        } catch (_: ActivityNotFoundException) {
            launchGuards.resetShare()
            // A device with no recipient remains on the offline lyric screen.
        } catch (_: RuntimeException) {
            launchGuards.resetShare()
            // A broken chooser/window also leaves the offline lyric screen usable.
        }
    }

    private fun openSearch(target: String) {
        if (!isSupportedTarget(target) || songName.isEmpty() || !launchGuards.tryStartVideo()) return
        val manager = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val network = manager?.activeNetwork
        if (network == null) {
            launchGuards.resetVideo()
            return
        }
        val capabilities = manager.getNetworkCapabilities(network)
        if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) {
            navigateToSearch(target)
        } else {
            showMobileDataConfirmation(target)
        }
    }

    private fun showMobileDataConfirmation(target: String) {
        AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(R.string.alert_title)
            .setMessage(R.string.alert_message)
            .setPositiveButton(R.string.ok_button) { _, _ -> navigateToSearch(target) }
            .setNegativeButton(R.string.cancel_button) { dialog, _ -> dialog.cancel() }
            .setOnCancelListener { launchGuards.resetVideo() }
            .show()
    }

    private fun navigateToSearch(target: String) {
        if (!isSupportedTarget(target) || songName.isEmpty()) {
            launchGuards.resetVideo()
            return
        }
        val videoIntent = Intent(this, VideoSearch::class.java)
            .putExtra(SEARCH_TARGET, target)
            .putExtra(MainActivity.SONG_NAME, songName)
        try {
            startActivity(videoIntent)
        } catch (_: ActivityNotFoundException) {
            launchGuards.resetVideo()
            // Keep the lyric visible if the retained provider boundary is unavailable.
        } catch (_: RuntimeException) {
            launchGuards.resetVideo()
            // Keep the lyric visible if the retained provider boundary cannot be launched.
        }
    }

    override fun onResume() {
        super.onResume()
        // Returning from a chooser/video (or a failed transient window) permits a later action.
        launchGuards.resetOnReturn()
    }

    private fun isSupportedTarget(target: String): Boolean =
        target == getString(R.string.youtube) ||
            target == getString(R.string.douyin)
}
