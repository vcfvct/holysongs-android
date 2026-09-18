package com.goodtrendltd.HolySongs.data

import android.database.sqlite.SQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.ByteArrayInputStream
import java.io.File
import java.util.concurrent.CancellationException
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BundledSongDatabaseTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext

    @Test
    fun packagedDatabaseMatchesEveryLegacyEffectiveTitleAndLyric() {
        val before = temporaryCopies()
        val rows = BundledSongDatabase(
            openAsset = { context.assets.open("songs.db") },
            cacheDirectory = context.cacheDir,
        ).readSongs()

        assertEquals(414, rows.size)
        assertEquals((0 until 414).toList(), rows.map { it.sourceOrder })
        assertEquals(414, rows.map { it.title }.toSet().size)
        assertEquals(414, rows.map { it.sourceOrder }.toSet().size)

        val expectedJson = instrumentation.context.assets.open("legacy-api37.json")
            .bufferedReader(Charsets.UTF_8).use { JSONObject(it.readText()) }
            .getJSONObject("observations").getJSONObject("U1")
            .getJSONArray("orderedTitlesAndWinningLyrics")
        val expected = buildMap {
            for (index in 0 until expectedJson.length()) {
                val row = expectedJson.getJSONObject(index)
                put(row.getString("title"), row.getString("winningLyric"))
            }
        }
        assertEquals(expected, rows.associate { it.title to it.lyric })
        assertEquals(before, temporaryCopies())
    }

    @Test
    fun rejectsCorruptOrMismatchedDatabaseAndDeletesTemporaryCopy() {
        val before = temporaryCopies()
        assertFails {
            BundledSongDatabase(
                openAsset = { ByteArrayInputStream("not sqlite".toByteArray()) },
                cacheDirectory = context.cacheDir,
            ).readSongs()
        }
        assertEquals(before, temporaryCopies())

        val wrong = File.createTempFile("wrong-catalog-", ".db", context.cacheDir)
        SQLiteDatabase.openOrCreateDatabase(wrong, null).use { database ->
            database.execSQL("CREATE TABLE wrong(value TEXT)")
            database.version = 99
        }
        try {
            assertFails {
                BundledSongDatabase(
                    openAsset = { wrong.inputStream() },
                    cacheDirectory = context.cacheDir,
                ).readSongs()
            }
            assertEquals(before, temporaryCopies())
        } finally {
            assertTrue(wrong.delete() || !wrong.exists())
        }
    }

    @Test
    fun rejectsAnExtraMetadataRowEvenWhenTheSchemaAndPrimaryRowAreValid() {
        val mutated = File.createTempFile("extra-metadata-", ".db", context.cacheDir)
        context.assets.open("songs.db").use { input ->
            mutated.outputStream().use { output -> input.copyTo(output) }
        }
        try {
            SQLiteDatabase.openDatabase(mutated.absolutePath, null, SQLiteDatabase.OPEN_READWRITE).use { database ->
                database.execSQL("PRAGMA ignore_check_constraints = ON")
                database.execSQL(
                    "INSERT INTO catalog_metadata " +
                        "(singleton_id, schema_version, transform_version, source_sha256, song_count) " +
                        "SELECT 2, schema_version, transform_version, source_sha256, song_count " +
                        "FROM catalog_metadata WHERE singleton_id = 1"
                )
            }
            assertFails {
                BundledSongDatabase(
                    openAsset = { mutated.inputStream() },
                    cacheDirectory = context.cacheDir,
                ).readSongs()
            }
        } finally {
            assertTrue(mutated.delete() || !mutated.exists())
        }
    }

    @Test
    fun cancellationDuringCopyIsRethrownAndTemporaryCopyIsDeleted() {
        val before = temporaryCopies()
        try {
            BundledSongDatabase(
                openAsset = { throw CancellationException("cancelled") },
                cacheDirectory = context.cacheDir,
            ).readSongs()
            fail("Expected cancellation")
        } catch (_: CancellationException) {
            // Expected.
        }
        assertEquals(before, temporaryCopies())
    }

    private fun temporaryCopies(): Set<File> =
        context.cacheDir.listFiles().orEmpty()
            .filter { it.name.startsWith(BundledSongDatabase.TEMP_FILE_PREFIX) }
            .toSet()

    private fun assertFails(block: () -> Unit) {
        try {
            block()
            fail("Expected database read to fail")
        } catch (failure: CancellationException) {
            throw failure
        } catch (_: Exception) {
            // Expected.
        }
    }
}
