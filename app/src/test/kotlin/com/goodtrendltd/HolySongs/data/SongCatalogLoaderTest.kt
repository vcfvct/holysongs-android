package com.goodtrendltd.HolySongs.data

import com.goodtrendltd.HolySongs.helpers.ChineseCharComp
import com.goodtrendltd.HolySongs.helpers.HanziHelper
import java.io.ByteArrayInputStream
import java.io.FilterInputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.CancellationException
import java.util.concurrent.atomic.AtomicInteger
import javax.xml.parsers.DocumentBuilderFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.w3c.dom.Element

/**
 * Contract tests for the extracted loader. Expected catalog values come from the independent
 * JAXP-parsed catalog-baseline.xml, never from SongCatalogLoader or the shipped asset parser.
 */
class SongCatalogLoaderTest {
    private val duplicatePositions = linkedMapOf(
        "轻轻听" to listOf(42, 320),
        "以色列的圣者" to listOf(53, 373),
        "耶稣基督是主" to listOf(87, 123),
        "耶稣耶稣" to listOf(102, 110),
        "天堂在我心" to listOf(109, 351),
        "全地宣告" to listOf(143, 144),
        "像天空的鸽子" to listOf(213, 214),
        "愿您崇高" to listOf(341, 342),
    )

    private data class RawSong(val name: String, val lyric: String)

    private fun repositoryRoot() = Paths.get(
        System.getProperty("repositoryRoot")
            ?: error("Missing repositoryRoot test system property")
    ).toAbsolutePath().normalize()

    private fun baselineSongs(): List<RawSong> {
        // This resource is the independently frozen T004 input, parsed with JAXP directly.
        val input = javaClass.getResourceAsStream("/catalog-baseline.xml")
        assertNotNull("Missing frozen catalog-baseline.xml", input)
        input!!.use { stream ->
            val document = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().parse(stream)
            val songs = document.getElementsByTagName("song")
            return (0 until songs.length).map { position ->
                val song = songs.item(position) as Element
                val name = song.getElementsByTagName("name").item(0)
                val lyric = song.getElementsByTagName("lyric").item(0)
                assertNotNull("Missing baseline name at position $position", name)
                assertNotNull("Missing baseline lyric at position $position", lyric)
                RawSong(name.textContent, lyric.textContent)
            }
        }
    }

    private fun effectiveExpected(songs: List<RawSong>): HashMap<String, String> {
        // Production extraction uses a HashMap; derive the expected key iteration from the same
        // unordered map before applying the existing comparator, not source insertion order.
        val result = HashMap<String, String>()
        songs.forEach { song ->
            // This is the specified preprocessing, applied to independent raw fields.
            result[song.name.replace(" ", "")] = song.lyric.replace(" ", "")
        }
        return result
    }

    private fun loaderFor(
        xml: String,
        dispatcher: CoroutineDispatcher = StandardTestDispatcher(),
        opened: AtomicInteger? = null,
    ): SongCatalogLoader = SongCatalogLoader(
        openAsset = {
            opened?.incrementAndGet()
            ByteArrayInputStream(xml.toByteArray(StandardCharsets.UTF_8))
        },
        dispatcher = dispatcher,
    )

    @Test
    fun loadsAllEffectiveKeysAndLastSourceEntryWins() = runTest {
        val raw = baselineSongs()
        assertEquals("The frozen source has every 422 raw entries", 422, raw.size)
        val expected = effectiveExpected(raw)
        assertEquals("Space removal leaves 414 effective keys", 414, expected.size)

        val loaderOpened = AtomicInteger()
        // available() is deliberately zero and every bulk read is capped at two bytes. This
        // catches implementations that size one read from available() or assume a full read.
        val stream = ShortReadingInputStream(
            Files.newInputStream(repositoryRoot().resolve("app/src/main/assets/songs.xml")),
            maxChunk = 2,
        )
        val loader = SongCatalogLoader(
            openAsset = {
                loaderOpened.incrementAndGet()
                stream
            },
            dispatcher = StandardTestDispatcher(testScheduler),
        )
        val catalog = loader.load()

        assertEquals(1, loaderOpened.get())
        assertTrue("The loader must close its asset stream", stream.closed)
        assertEquals(expected, catalog.lyricsByTitle)
        assertEquals(414, catalog.orderedTitles.size)
        assertEquals(414, catalog.orderedTitles.toSet().size)
        assertEquals(expected.keys.toSet(), catalog.orderedTitles.toSet())
        assertEquals(
            expected.keys.sortedWith(Comparator { left, right -> ChineseCharComp().compare(left, right) }),
            catalog.orderedTitles,
        )

        duplicatePositions.forEach { (title, positions) ->
            assertEquals(2, positions.size)
            val first = raw[positions[0]]
            val last = raw[positions[1]]
            assertEquals(title, first.name.replace(" ", ""))
            assertEquals(title, last.name.replace(" ", ""))
            assertEquals(
                "The final source entry wins for $title",
                last.lyric.replace(" ", ""),
                catalog.lyricsByTitle[title],
            )
        }
        assertEquals("Duplicate raw titles must not become duplicate lazy keys", 414, catalog.orderedTitles.toSet().size)

        assertCatalogCollectionsAreImmutable(catalog)
        catalog.orderedTitles.forEachIndexed { index, title ->
            val pinyin = HanziHelper.words2Pinyin(title)
            val expectedInitial = pinyin.firstOrNull()?.uppercaseChar()?.takeIf { it in 'A'..'Z' }
                ?.toString()
            assertEquals("Guarded HanziHelper initial for $title", expectedInitial, catalog.initials[index])
        }
        assertTrue("At least one title has a guarded pinyin initial", catalog.initials.any { it != null })
    }

    private fun assertCatalogCollectionsAreImmutable(catalog: SongCatalog) {
        val originalLyric = catalog.lyricsByTitle["云上太阳"]
        try {
            @Suppress("UNCHECKED_CAST")
            (catalog.lyricsByTitle as MutableMap<String, String>)["云上太阳"] = "mutated"
            fail("lyricsByTitle must reject mutation")
        } catch (_: UnsupportedOperationException) {
            // Expected for a defensively frozen map.
        } catch (_: ClassCastException) {
            // Also acceptable for an exposed read-only implementation.
        }
        assertEquals(originalLyric, catalog.lyricsByTitle["云上太阳"])

        val originalTitles = catalog.orderedTitles.toList()
        try {
            @Suppress("UNCHECKED_CAST")
            (catalog.orderedTitles as MutableList<String>).clear()
            fail("orderedTitles must reject mutation")
        } catch (_: UnsupportedOperationException) {
            // Expected for a defensively frozen list.
        } catch (_: ClassCastException) {
            // Also acceptable for an exposed read-only implementation.
        }
        assertEquals(originalTitles, catalog.orderedTitles)

        val originalInitials = catalog.initials.toList()
        try {
            @Suppress("UNCHECKED_CAST")
            (catalog.initials as MutableList<String?>).clear()
            fail("initials must reject mutation")
        } catch (_: UnsupportedOperationException) {
            // Expected for a defensively frozen list.
        } catch (_: ClassCastException) {
            // Also acceptable for an exposed read-only implementation.
        }
        assertEquals(originalInitials, catalog.initials)
    }

    @Test
    fun removesOnlyAsciiSpacesAndPreservesUtf8AndNonAsciiWhitespace() = runTest {
        val xml = """
            <songs><song><name>爱 我</name><lyric>中 文　换行
                下一行</lyric></song></songs>
        """.trimIndent()
        val catalog = loaderFor(xml, StandardTestDispatcher(testScheduler)).load()

        assertEquals(listOf("爱我"), catalog.orderedTitles)
        assertEquals("中文　换行\n下一行", catalog.lyricsByTitle["爱我"])
    }

    @Test
    fun acceptsEmptyCatalogAndNonChineseTitlesWithNullWhenInitialCannotBeDerived() = runTest {
        val empty = loaderFor("<songs />", StandardTestDispatcher(testScheduler)).load()
        assertTrue(empty.lyricsByTitle.isEmpty())
        assertTrue(empty.orderedTitles.isEmpty())
        assertTrue(empty.initials.isEmpty())

        val nonChinese = loaderFor(
            "<songs>" +
                "<song><name>123</name><lyric>numbers</lyric></song>" +
                "<song><name>ABC</name><lyric>letters</lyric></song>" +
                "</songs>",
            StandardTestDispatcher(testScheduler),
        ).load()
        assertEquals(mapOf("123" to "numbers", "ABC" to "letters"), nonChinese.lyricsByTitle)
        assertEquals(2, nonChinese.initials.size)
        assertNull(nonChinese.initials[nonChinese.orderedTitles.indexOf("123")])
        assertEquals("A", nonChinese.initials[nonChinese.orderedTitles.indexOf("ABC")])
    }

    @Test
    fun rejectsMalformedAndMissingRequiredFieldsInsteadOfReturningPartialCatalog() = runTest {
        listOf(
            "<songs>",
            "<songs><song><name>only name</name></song></songs>",
            "<songs><song><lyric>only lyric</lyric></song></songs>",
            "<songs><song><name></name><lyric>lyric</lyric></song></songs>",
            "<songs><song><name>title</name><lyric></lyric></song></songs>",
        ).forEach { xml ->
            try {
                loaderFor(xml, StandardTestDispatcher(testScheduler)).load()
                fail("Expected invalid catalog input to fail: $xml")
            } catch (expected: CancellationException) {
                throw expected
            } catch (_: Exception) {
                // The public contract does not prescribe a concrete parse exception type.
            }
        }
    }

    @Test
    fun preservesFirstTextNodeRatherThanJoiningNestedXmlText() = runTest {
        val xml = "<songs><song><name>标题</name><lyric>第一行\n" +
            "<verse>nested text</verse>尾部</lyric></song></songs>"
        val catalog = loaderFor(xml, StandardTestDispatcher(testScheduler)).load()

        // XMLParser's established extraction returns the first direct TEXT_NODE only.
        assertEquals("第一行\n", catalog.lyricsByTitle["标题"])
    }

    @Test
    fun closesStreamOnMalformedPartialInputAndPropagatesCancellation() = runTest {
        val malformedAfterValidSong = "<songs>" +
            "<song><name>好</name><lyric>歌词</lyric></song>" +
            "<song><name>坏</name></song></songs>"
        val malformedStream = ShortReadingInputStream(
            ByteArrayInputStream(malformedAfterValidSong.toByteArray(StandardCharsets.UTF_8)),
            maxChunk = 1,
        )
        assertLoadFails(
            SongCatalogLoader(
                openAsset = { malformedStream },
                dispatcher = StandardTestDispatcher(testScheduler),
            )
        )
        assertTrue("Malformed partial input must still close the stream", malformedStream.closed)

        val ioFailure = FailingInputStream(
            "<songs><song><name>好</name><lyric>歌词</lyric></song></songs>"
                .toByteArray(StandardCharsets.UTF_8),
            failAfter = 7,
        )
        assertLoadFails(
            SongCatalogLoader(
                openAsset = { ioFailure },
                dispatcher = StandardTestDispatcher(testScheduler),
            )
        )
        assertTrue("I/O failure must still close the stream", ioFailure.closed)

        val openerFailure = SongCatalogLoader(
            openAsset = { throw java.io.IOException("open failed") },
            dispatcher = StandardTestDispatcher(testScheduler),
        )
        assertLoadFails(openerFailure)

        val cancelled = CancellationInputStream()
        assertLoadFails(
            SongCatalogLoader(
                openAsset = { cancelled },
                dispatcher = StandardTestDispatcher(testScheduler),
            ),
            expectCancellation = true,
        )
        assertTrue("Cancellation must still close the opened stream", cancelled.closed)
    }

    private suspend fun assertLoadFails(loader: SongCatalogLoader, expectCancellation: Boolean = false) {
        try {
            loader.load()
            fail("Expected invalid or failed catalog input to fail")
        } catch (expected: CancellationException) {
            if (!expectCancellation) throw expected
        } catch (_: Exception) {
            if (expectCancellation) fail("Expected cancellation to propagate")
        }
    }

    private class ShortReadingInputStream(
        delegate: InputStream,
        private val maxChunk: Int,
    ) : FilterInputStream(delegate) {
        init {
            require(maxChunk in 1..3)
        }

        var closed = false
            private set

        override fun available(): Int = 0

        override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
            if (length == 0) return 0
            return super.read(buffer, offset, minOf(length, maxChunk))
        }

        override fun close() {
            closed = true
            super.close()
        }
    }

    private class FailingInputStream(
        bytes: ByteArray,
        private val failAfter: Int,
    ) : InputStream() {
        private val delegate = ByteArrayInputStream(bytes)
        private var emitted = 0

        var closed = false
            private set

        override fun available(): Int = 0

        override fun read(): Int {
            if (emitted >= failAfter) throw java.io.IOException("controlled read failure")
            val value = delegate.read()
            if (value >= 0) emitted++
            return value
        }

        override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
            if (length == 0) return 0
            if (emitted >= failAfter) throw java.io.IOException("controlled read failure")
            val allowed = minOf(length, 2, failAfter - emitted)
            val count = delegate.read(buffer, offset, allowed)
            if (count > 0) emitted += count
            return count
        }

        override fun close() {
            closed = true
            delegate.close()
        }
    }

    private class CancellationInputStream : InputStream() {
        var closed = false
            private set

        override fun read(): Int = throw CancellationException("controlled cancellation")

        override fun close() {
            closed = true
        }
    }
}
