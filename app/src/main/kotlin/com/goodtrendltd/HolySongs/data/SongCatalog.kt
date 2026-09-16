package com.goodtrendltd.HolySongs.data

import java.util.Collections

/** Immutable, source-order-independent view of the effective song catalog. */
class SongCatalog(
    lyricsByTitle: Map<String, String>,
    orderedTitles: List<String>,
    initials: List<String?>,
    sectionIndex: LegacySectionIndex,
) {
    val lyricsByTitle: Map<String, String> =
        Collections.unmodifiableMap(HashMap(lyricsByTitle))
    val orderedTitles: List<String> =
        Collections.unmodifiableList(ArrayList(orderedTitles))
    val initials: List<String?> =
        Collections.unmodifiableList(ArrayList(initials))
    val sectionIndex: LegacySectionIndex = sectionIndex
}
