package com.goodtrendltd.HolySongs

/**
 * Activity-local one-shot state for effects that leave the lyric screen.
 *
 * This deliberately tracks no navigation generation: a callback cannot be associated with a
 * generation at this boundary, so same-URL stale WebView callbacks remain an explicit limitation.
 */
internal class DisplayLyricLaunchGuards {
    private var shareInFlight = false
    private var videoInFlight = false

    fun tryStartShare(): Boolean {
        if (shareInFlight) return false
        shareInFlight = true
        return true
    }

    fun tryStartVideo(): Boolean {
        if (videoInFlight) return false
        videoInFlight = true
        return true
    }

    fun resetShare() {
        shareInFlight = false
    }

    fun resetVideo() {
        videoInFlight = false
    }

    fun resetOnReturn() {
        resetShare()
        resetVideo()
    }
}
