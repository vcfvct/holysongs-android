package com.goodtrendltd.HolySongs.data

import com.goodtrendltd.HolySongs.helpers.ChineseCharComp
import com.goodtrendltd.HolySongs.helpers.HanziHelper
import com.goodtrendltd.HolySongs.helpers.XMLParser
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.w3c.dom.Element

/** Loads the legacy songs asset without changing its parser or ordering rules. */
class SongCatalogLoader(
    private val openAsset: () -> java.io.InputStream,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    suspend fun load(): SongCatalog = withContext(dispatcher) {
        val xml = openAsset().use { input ->
            input.readBytes().toString(StandardCharsets.UTF_8)
        }.replace(" ", "")

        val parser = XMLParser()
        val document = parser.getDomElement(xml)
            ?: throw IllegalArgumentException("Unable to parse song catalog XML")
        val songs = document.getElementsByTagName("song")
        val lyricsByTitle = HashMap<String, String>(songs.length)
        for (position in 0 until songs.length) {
            val song = songs.item(position) as? Element
                ?: throw IllegalArgumentException("Song entry $position is not an element")
            val name = parser.getValue(song, "name")
            val lyric = parser.getValue(song, "lyric")
            if (name.isEmpty() || lyric.isEmpty()) {
                throw IllegalArgumentException("Song entry $position has an empty required field")
            }
            lyricsByTitle[name] = lyric
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
