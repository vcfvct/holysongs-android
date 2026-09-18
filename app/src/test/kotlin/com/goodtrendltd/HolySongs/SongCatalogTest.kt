package com.goodtrendltd.HolySongs

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element

class SongCatalogTest {
    private data class RawSong(val title: String, val lyric: String)

    @Test
    fun canonicalSourceContainsOnlyThe414EffectiveLegacySongs() {
        val canonical = songs(repositoryRoot().resolve("app/src/main/assets/songs.xml"))
        val historical = baselineSongs()
        val expected = effectiveMap(historical)

        assertEquals("Canonical source must be deduplicated", 414, canonical.size)
        assertEquals("Canonical effective titles must be unique", 414, canonical.map { effective(it.title) }.toSet().size)
        assertEquals(expected, canonical.associate { effective(it.title) to effective(it.lyric) })
        assertEquals("云上太阳", effective(canonical.first().title))
        assertEquals("我一生", effective(canonical.last().title))
    }

    @Test
    fun historicalBaselineRetainsBothMembersAndCanonicalKeepsEachWinner() {
        val historical = baselineSongs()
        assertEquals(422, historical.size)
        val expectedPairs = linkedMapOf(
            "轻轻听" to listOf(42, 320),
            "以色列的圣者" to listOf(53, 373),
            "耶稣基督是主" to listOf(87, 123),
            "耶稣耶稣" to listOf(102, 110),
            "天堂在我心" to listOf(109, 351),
            "全地宣告" to listOf(143, 144),
            "像天空的鸽子" to listOf(213, 214),
            "愿您崇高" to listOf(341, 342),
        )
        val positions = historical.indices.groupBy { effective(historical[it].title) }
            .filterValues { it.size > 1 }
        assertEquals(expectedPairs, positions)

        val canonical = songs(repositoryRoot().resolve("app/src/main/assets/songs.xml"))
            .associate { effective(it.title) to effective(it.lyric) }
        expectedPairs.forEach { (title, pair) ->
            assertFalse("Historical duplicate lyrics must differ for $title", effective(historical[pair[0]].lyric) == effective(historical[pair[1]].lyric))
            assertEquals("Canonical source must retain the historical winner for $title", effective(historical[pair[1]].lyric), canonical[title])
        }
    }

    @Test
    fun canonicalSourcePreservesRepresentativeWhitespaceAndUnicodeEffectively() {
        val canonical = songs(repositoryRoot().resolve("app/src/main/assets/songs.xml"))
            .associate { effective(it.title) to effective(it.lyric) }
        val expected = effectiveMap(baselineSongs())

        listOf("云上太阳", "轻轻听", "爱中相遇", "我一生").forEach { title ->
            assertEquals(expected[title], canonical[title])
        }
        assertTrue("Representative lyric keeps line feeds", canonical.getValue("云上太阳").contains('\n'))
        assertTrue("Catalog retains at least one ideographic space", canonical.values.any { it.contains('　') })
        assertTrue("Catalog retains blank lines", canonical.values.any { it.contains("\n\n") })
    }

    private fun effective(value: String): String = value.replace(" ", "")

    private fun effectiveMap(songs: List<RawSong>): Map<String, String> = buildMap {
        songs.forEach { put(effective(it.title), effective(it.lyric)) }
    }

    private fun baselineSongs(): List<RawSong> {
        val stream = javaClass.getResourceAsStream("/catalog-baseline.xml")
            ?: error("Missing frozen catalog-baseline.xml")
        return stream.use { input ->
            val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input)
            elements(document.getElementsByTagName("song").length) { index ->
                document.getElementsByTagName("song").item(index) as Element
            }
        }
    }

    private fun songs(path: Path): List<RawSong> {
        assertTrue("Missing catalog: $path", Files.exists(path))
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(path.toFile())
        val nodes = document.getElementsByTagName("song")
        return elements(nodes.length) { index -> nodes.item(index) as Element }
    }

    private fun elements(count: Int, elementAt: (Int) -> Element): List<RawSong> =
        (0 until count).map { index ->
            val song = elementAt(index)
            RawSong(
                title = song.getElementsByTagName("name").item(0).textContent,
                lyric = song.getElementsByTagName("lyric").item(0).textContent,
            )
        }

    private fun repositoryRoot(): Path {
        val root = System.getProperty("repositoryRoot")
            ?: throw IllegalStateException("Missing required system property 'repositoryRoot'.")
        return Paths.get(root).toAbsolutePath().normalize()
    }
}
