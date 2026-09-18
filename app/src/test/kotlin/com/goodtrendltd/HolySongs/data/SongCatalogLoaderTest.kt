package com.goodtrendltd.HolySongs.data

import com.goodtrendltd.HolySongs.helpers.ChineseCharComp
import com.goodtrendltd.HolySongs.helpers.HanziHelper
import java.nio.file.Paths
import java.util.concurrent.CancellationException
import java.util.concurrent.atomic.AtomicInteger
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.w3c.dom.Element

class SongCatalogLoaderTest {
    private fun repositoryRoot() = Paths.get(
        System.getProperty("repositoryRoot") ?: error("Missing repositoryRoot test system property")
    ).toAbsolutePath().normalize()

    private fun effectiveBaseline(): Map<String, String> {
        val input = javaClass.getResourceAsStream("/catalog-baseline.xml")
            ?: error("Missing catalog-baseline.xml")
        input.use { stream ->
            val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream)
            val nodes = document.getElementsByTagName("song")
            val result = linkedMapOf<String, String>()
            for (position in 0 until nodes.length) {
                val song = nodes.item(position) as Element
                val title = song.getElementsByTagName("name").item(0).textContent.replace(" ", "")
                val lyric = song.getElementsByTagName("lyric").item(0).textContent.replace(" ", "")
                result[title] = lyric
            }
            return result
        }
    }

    private fun rows(values: Map<String, String>): List<StoredSong> =
        values.entries.mapIndexed { index, entry ->
            StoredSong(entry.key, entry.value, index)
        }

    @Test
    fun loadsAllEffectiveRowsAndPreservesCatalogDerivations() = runTest {
        val expected = effectiveBaseline()
        assertEquals(414, expected.size)
        val calls = AtomicInteger()
        val dispatcher = RecordingDispatcher(StandardTestDispatcher(testScheduler))
        val loader = SongCatalogLoader(
            readSongs = {
                calls.incrementAndGet()
                rows(expected)
            },
            dispatcher = dispatcher,
        )

        val catalog = loader.load()

        assertEquals(1, calls.get())
        assertTrue(dispatcher.dispatchCount > 0)
        assertEquals(expected, catalog.lyricsByTitle)
        assertEquals(expected.keys.sortedWith(ChineseCharComp()), catalog.orderedTitles)
        assertEquals(414, catalog.initials.size)
        catalog.orderedTitles.forEachIndexed { index, title ->
            val expectedInitial = HanziHelper.words2Pinyin(title)
                .firstOrNull()?.uppercaseChar()?.takeIf { it in 'A'..'Z' }?.toString()
            assertEquals(expectedInitial, catalog.initials[index])
        }
        assertCatalogCollectionsAreImmutable(catalog)
    }

    @Test
    fun rejectsEmptyFieldsDuplicateTitlesAndInvalidSourceOrderWithoutPartialCatalog() = runTest {
        val invalidCases = listOf(
            listOf(StoredSong("", "lyric", 0)),
            listOf(StoredSong("title", "", 0)),
            listOf(StoredSong("same", "one", 0), StoredSong("same", "two", 1)),
            listOf(StoredSong("one", "one", 0), StoredSong("two", "two", 0)),
            listOf(StoredSong("one", "one", -1)),
            listOf(StoredSong("one", "one", 1)),
            listOf(StoredSong("one", "one", 0), StoredSong("two", "two", 2)),
        )
        invalidCases.forEach { storedSongs ->
            assertLoadFails(
                SongCatalogLoader(
                    readSongs = { storedSongs },
                    dispatcher = StandardTestDispatcher(testScheduler),
                )
            )
        }
    }

    @Test
    fun acceptsEmptyRowsAsAnEmptyImmutableCatalog() = runTest {
        val catalog = SongCatalogLoader(
            readSongs = { emptyList() },
            dispatcher = StandardTestDispatcher(testScheduler),
        ).load()
        assertTrue(catalog.lyricsByTitle.isEmpty())
        assertTrue(catalog.orderedTitles.isEmpty())
        assertTrue(catalog.initials.isEmpty())
    }

    @Test
    fun propagatesCancellation() = runTest {
        val loader = SongCatalogLoader(
            readSongs = { throw CancellationException("cancelled") },
            dispatcher = StandardTestDispatcher(testScheduler),
        )
        try {
            loader.load()
            fail("Expected cancellation")
        } catch (_: CancellationException) {
            // Expected lifecycle control.
        }
    }

    private suspend fun assertLoadFails(loader: SongCatalogLoader) {
        try {
            loader.load()
            fail("Expected invalid stored rows to fail")
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: IllegalArgumentException) {
            // Expected.
        }
    }

    private fun assertCatalogCollectionsAreImmutable(catalog: SongCatalog) {
        val originalLyric = catalog.lyricsByTitle["云上太阳"]
        try {
            @Suppress("UNCHECKED_CAST")
            (catalog.lyricsByTitle as MutableMap<String, String>)["云上太阳"] = "mutated"
            fail("lyricsByTitle must reject mutation")
        } catch (_: UnsupportedOperationException) {
            // Expected.
        } catch (_: ClassCastException) {
            // Also acceptable.
        }
        assertEquals(originalLyric, catalog.lyricsByTitle["云上太阳"])

        val originalTitles = catalog.orderedTitles.toList()
        try {
            @Suppress("UNCHECKED_CAST")
            (catalog.orderedTitles as MutableList<String>).clear()
            fail("orderedTitles must reject mutation")
        } catch (_: UnsupportedOperationException) {
            // Expected.
        } catch (_: ClassCastException) {
            // Also acceptable.
        }
        assertEquals(originalTitles, catalog.orderedTitles)
    }

    private class RecordingDispatcher(private val delegate: CoroutineDispatcher) : CoroutineDispatcher() {
        var dispatchCount = 0
            private set

        override fun dispatch(context: CoroutineContext, block: Runnable) {
            dispatchCount++
            delegate.dispatch(context, block)
        }
    }
}
