package com.goodtrendltd.HolySongs.data

/** Storage-neutral representation of one generated catalog row. */
data class StoredSong(
    val title: String,
    val lyric: String,
    val sourceOrder: Int,
)
