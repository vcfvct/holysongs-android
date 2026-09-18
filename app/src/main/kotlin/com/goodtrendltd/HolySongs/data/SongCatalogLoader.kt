package com.goodtrendltd.HolySongs.data

import com.goodtrendltd.HolySongs.helpers.ChineseCharComp
import com.goodtrendltd.HolySongs.helpers.HanziHelper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Loads validated stored rows and derives the established immutable catalog view. */
class SongCatalogLoader(
    private val readSongs: () -> List<StoredSong>,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    suspend fun load(): SongCatalog = withContext(dispatcher) {
        val songs = readSongs()
        val orderedBySource = songs.sortedBy(StoredSong::sourceOrder)
        orderedBySource.forEachIndexed { expectedOrder, song ->
            require(song.sourceOrder == expectedOrder) {
                "Song source order must be contiguous from zero; expected $expectedOrder, found ${song.sourceOrder}"
            }
            require(song.title.isNotEmpty()) { "Song entry $expectedOrder has an empty title" }
            require(song.lyric.isNotEmpty()) { "Song entry $expectedOrder has an empty lyric" }
        }
        require(orderedBySource.map(StoredSong::sourceOrder).toSet().size == orderedBySource.size) {
            "Song source orders must be unique"
        }

        val lyricsByTitle = HashMap<String, String>(orderedBySource.size)
        orderedBySource.forEach { song ->
            require(!lyricsByTitle.containsKey(song.title)) {
                "Duplicate stored song title ${song.title}"
            }
            lyricsByTitle[song.title] = song.lyric
        }

        val orderedTitles = lyricsByTitle.keys.toList().sortedWith(ChineseCharComp())
        val initials = orderedTitles.map { title ->
            HanziHelper.words2Pinyin(title)
                .firstOrNull()
                ?.uppercaseChar()
                ?.takeIf { it in 'A'..'Z' }
                ?.toString()
        }
        SongCatalog(
            lyricsByTitle = lyricsByTitle,
            orderedTitles = orderedTitles,
            initials = initials,
            sectionIndex = LegacySectionIndex(orderedTitles),
        )
    }
}
