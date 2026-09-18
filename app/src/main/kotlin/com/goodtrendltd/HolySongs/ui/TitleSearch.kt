package com.goodtrendltd.HolySongs.ui

/** Returns title references in their established catalog order. */
fun filterTitlesByQuery(orderedTitles: List<String>, rawQuery: String): List<String> {
    val query = rawQuery.trim()
    if (query.isEmpty()) return orderedTitles
    return orderedTitles.filter { title -> title.contains(query, ignoreCase = true) }
}
