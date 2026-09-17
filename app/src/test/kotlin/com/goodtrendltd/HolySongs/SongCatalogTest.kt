package com.goodtrendltd.HolySongs

import com.goodtrendltd.HolySongs.helpers.XMLParser
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.w3c.dom.Element
import org.w3c.dom.NodeList

class SongCatalogTest {
    private val parser = XMLParser()

    @Test
    fun preservesEveryRawSourceFieldAtItsOriginalPosition() {
        val actual = actualSongs()
        val expected = baselineSongs()
        assertEquals("Frozen input contains all 422 source entries", 422, expected.length)
        assertEquals("All 422 entries stay in original order", 422, actual.length)

        for (position in 0 until expected.length) {
            val source = actual.item(position) as Element
            val baseline = expected.item(position) as Element
            assertEquals(
                "Raw parsed name at zero-based XML position $position",
                baseline.getElementsByTagName("name").item(0).textContent,
                parser.getValue(source, "name"),
            )
            assertEquals(
                "Raw lyric including line breaks and original text at position $position",
                baseline.getElementsByTagName("lyric").item(0).textContent,
                parser.getValue(source, "lyric"),
            )
        }
        assertEquals("云上太阳", parser.getValue(actual.item(0) as Element, "name"))
        assertEquals("我一生", parser.getValue(actual.item(421) as Element, "name"))
    }

    @Test
    fun preservesBothPositionsAndBothLyricsOfEveryDuplicatePair() {
        val actual = actualSongs()
        val baseline = baselineSongs()
        val positionsByTitle = linkedMapOf<String, MutableList<Int>>()
        for (position in 0 until actual.length) {
            val title = parser.getValue(actual.item(position) as Element, "name")
            positionsByTitle.getOrPut(title) { mutableListOf() }.add(position)
        }
        assertEquals("414 distinct raw titles", 414, positionsByTitle.size)

        val duplicates = linkedMapOf<String, MutableList<Int>>()
        for ((title, positions) in positionsByTitle) {
            if (positions.size > 1) {
                duplicates[title] = positions
            }
        }

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
        assertEquals(expectedPairs, duplicates)

        for ((pairName, positions) in expectedPairs) {
            for (position in positions) {
                val expected = baseline.item(position) as Element
                assertEquals(
                    "$pairName raw lyric at $position",
                    expected.getElementsByTagName("lyric").item(0).textContent,
                    parser.getValue(actual.item(position) as Element, "lyric"),
                )
            }
        }
    }

    @Test
    fun preservesExactRepresentativeRawWhitespaceAndChineseLyrics() {
        val actual = actualSongs()
        assertEquals(
            "\n" +
                "            无论是住在美丽的高山，或是躺卧在阴暗的幽谷，\n" +
                "            当你抬起头，你将会发现，主已为你我而预备。\n" +
                "            云上太阳它总不改变，虽然小雨洒在脸上，\n" +
                "            云上太阳它总不改变，阿哈……它不改变。\n" +
                "        ",
            parser.getValue(actual.item(0) as Element, "lyric"),
        )
        assertEquals(
            "\n" +
                "            轻轻细心听　轻轻细心听您声\n" +
                "            轻轻细心听　轻轻细心听您声\n" +
                "            细细听　轻轻细细听\n" +
                "            倾听您说话儿共对应\n" +
                "            细细说　轻轻细细说\n" +
                "            因知道我牧人在细听\n" +
                "            上帝您是我独一生命光\n" +
                "            羊属您必清楚听您声\n" +
                "            牧养引导我　轻声教导我\n" +
                "            一生听您话儿共对应\n" +
                "        ",
            parser.getValue(actual.item(320) as Element, "lyric"),
        )
    }

    @Test
    fun characterizesFirstTextNodeExtractionWithoutTrimmingOrJoining() {
        val document = parser.getDomElement("<song><name> A　主 </name>" +
            "<lyric> first\n<verse>nested</verse>last </lyric></song>")
        assertNotNull(document)
        val song = document!!.documentElement
        assertEquals(" A　主 ", parser.getValue(song, "name"))
        assertEquals(" first\n", parser.getValue(song, "lyric"))
        assertEquals("", parser.getValue(song, "missing"))
    }

    private fun repositoryRoot(): Path {
        val root = System.getProperty("repositoryRoot")
            ?: throw IllegalStateException("Missing required system property 'repositoryRoot'.")
        return Paths.get(root).toAbsolutePath().normalize()
    }

    private fun actualSongs(): NodeList {
        val xml = String(Files.readAllBytes(repositoryRoot().resolve("app/src/main/assets/songs.xml")), StandardCharsets.UTF_8)
        val document = parser.getDomElement(xml)
        assertNotNull("The real XMLParser must parse the bundled source", document)
        return document!!.getElementsByTagName("song")
    }

    private fun baselineSongs(): NodeList {
        val resource: InputStream = javaClass.getResourceAsStream("/catalog-baseline.xml")
            ?: throw AssertionError("Missing frozen catalog-baseline.xml test resource")
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(resource)
            .getElementsByTagName("song")
    }
}
