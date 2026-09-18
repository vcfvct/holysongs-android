package com.goodtrendltd.HolySongs.data

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import java.io.File
import java.io.InputStream
import java.util.concurrent.CancellationException

/** Reads the generated catalog through a short-lived, private, read-only SQLite copy. */
class BundledSongDatabase internal constructor(
    private val openAsset: () -> InputStream,
    private val cacheDirectory: File,
    private val expectedSongCount: Int = EXPECTED_SONG_COUNT,
) {
    fun readSongs(): List<StoredSong> {
        require(expectedSongCount > 0) { "Expected song count must be positive" }
        cacheDirectory.mkdirs()
        val temporary = File.createTempFile(TEMP_FILE_PREFIX, ".db", cacheDirectory)
        try {
            openAsset().use { input ->
                temporary.outputStream().use { output -> input.copyTo(output) }
            }
            return readDatabase(temporary)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } finally {
            if (temporary.exists() && !temporary.delete()) {
                temporary.deleteOnExit()
            }
        }
    }

    private fun readDatabase(file: File): List<StoredSong> {
        val flags = SQLiteDatabase.OPEN_READONLY or SQLiteDatabase.NO_LOCALIZED_COLLATORS
        val database = SQLiteDatabase.openDatabase(file.absolutePath, null, flags)
        try {
            require(singleLong(database, "PRAGMA user_version") == SCHEMA_VERSION.toLong()) {
                "Unsupported song database schema version"
            }
            require(singleString(database, "PRAGMA integrity_check(1)") == "ok") {
                "Song database integrity check failed"
            }
            validateApplicationObjects(database)
            validateMetadataTable(database)
            validateSongsTable(database)

            val metadataRows = database.rawQuery(
                "SELECT singleton_id, schema_version, transform_version, source_sha256, song_count " +
                    "FROM catalog_metadata ORDER BY singleton_id",
                null,
            ).use { cursor ->
                buildList {
                    while (cursor.moveToNext()) {
                        add(
                            Metadata(
                                singletonId = cursor.getInt(0),
                                schemaVersion = cursor.getInt(1),
                                transformVersion = cursor.getInt(2),
                                sourceSha256 = cursor.getString(3),
                                songCount = cursor.getInt(4),
                            )
                        )
                    }
                }
            }
            require(metadataRows.size == 1) { "Song database must contain exactly one metadata row" }
            val metadata = metadataRows.single()
            require(metadata.singletonId == 1) { "Song database metadata singleton id must be 1" }
            require(metadata.schemaVersion == SCHEMA_VERSION) { "Unsupported metadata schema version" }
            require(metadata.transformVersion == TRANSFORM_VERSION) { "Unsupported catalog transform version" }
            require(SHA_256.matches(metadata.sourceSha256)) { "Invalid catalog source digest" }
            require(metadata.songCount == expectedSongCount) {
                "Expected $expectedSongCount songs, metadata declared ${metadata.songCount}"
            }

            val rows = database.rawQuery(
                "SELECT title, lyric, source_order FROM songs ORDER BY source_order",
                null,
            ).use { cursor -> readRows(cursor) }
            require(rows.size == expectedSongCount) {
                "Expected $expectedSongCount songs, database contained ${rows.size}"
            }
            rows.forEachIndexed { expectedOrder, song ->
                require(song.sourceOrder == expectedOrder) {
                    "Song source order must be contiguous from zero"
                }
            }
            require(rows.map(StoredSong::title).toSet().size == rows.size) {
                "Song titles must be unique"
            }
            return rows
        } finally {
            database.close()
        }
    }

    private fun validateApplicationObjects(database: SQLiteDatabase) {
        val objects = database.rawQuery(
            "SELECT type, name, sql FROM sqlite_master " +
                "WHERE name NOT LIKE 'sqlite_%' ORDER BY type, name",
            null,
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(Triple(cursor.getString(0), cursor.getString(1), normalizeSql(cursor.getString(2))))
                }
            }
        }
        val expected = listOf(
            Triple("table", "catalog_metadata", normalizeSql(EXPECTED_METADATA_SQL)),
            Triple("table", "songs", normalizeSql(EXPECTED_SONGS_SQL)),
        )
        require(objects == expected) { "Unsupported song database application schema" }
    }

    private fun validateMetadataTable(database: SQLiteDatabase) {
        val columns = tableColumns(database, "catalog_metadata")
        require(columns.map(Column::name) == listOf(
            "singleton_id", "schema_version", "transform_version", "source_sha256", "song_count"
        )) { "Unsupported catalog metadata columns" }
        require(columns.map { it.type.uppercase() } == listOf(
            "INTEGER", "INTEGER", "INTEGER", "TEXT", "INTEGER"
        )) { "Unsupported catalog metadata column types" }
        require(columns.all(Column::notNull)) { "Catalog metadata columns must be non-null" }
        require(columns.map(Column::primaryKey) == listOf(true, false, false, false, false)) {
            "Unsupported catalog metadata primary key"
        }
    }

    private fun validateSongsTable(database: SQLiteDatabase) {
        val columns = tableColumns(database, "songs")
        require(columns.map(Column::name) == listOf("title", "lyric", "source_order")) {
            "Unsupported songs table columns"
        }
        require(columns[0].type.equals("TEXT", ignoreCase = true) && columns[0].notNull && columns[0].primaryKey) {
            "Song title must be the non-null TEXT primary key"
        }
        require(columns[1].type.equals("TEXT", ignoreCase = true) && columns[1].notNull) {
            "Song lyric must be non-null TEXT"
        }
        require(columns[2].type.equals("INTEGER", ignoreCase = true) && columns[2].notNull) {
            "Song source order must be non-null INTEGER"
        }
    }

    private fun tableColumns(database: SQLiteDatabase, table: String): List<Column> =
        database.rawQuery("PRAGMA table_info($table)", null).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        Column(
                            name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                            type = cursor.getString(cursor.getColumnIndexOrThrow("type")),
                            notNull = cursor.getInt(cursor.getColumnIndexOrThrow("notnull")) == 1,
                            primaryKey = cursor.getInt(cursor.getColumnIndexOrThrow("pk")) == 1,
                        )
                    )
                }
            }
        }

    private fun readRows(cursor: Cursor): List<StoredSong> {
        val titleIndex = cursor.getColumnIndexOrThrow("title")
        val lyricIndex = cursor.getColumnIndexOrThrow("lyric")
        val orderIndex = cursor.getColumnIndexOrThrow("source_order")
        return buildList {
            while (cursor.moveToNext()) {
                require(!cursor.isNull(titleIndex) && !cursor.isNull(lyricIndex) && !cursor.isNull(orderIndex)) {
                    "Song database contains a null required field"
                }
                val title = cursor.getString(titleIndex)
                val lyric = cursor.getString(lyricIndex)
                require(title.isNotEmpty()) { "Song database contains an empty title" }
                require(lyric.isNotEmpty()) { "Song database contains an empty lyric" }
                add(StoredSong(title, lyric, cursor.getInt(orderIndex)))
            }
        }
    }

    private fun normalizeSql(value: String): String =
        value.lowercase().filterNot(Char::isWhitespace).removeSuffix(";")

    private fun singleLong(database: SQLiteDatabase, query: String): Long =
        database.rawQuery(query, null).use { cursor ->
            require(cursor.moveToFirst()) { "Query returned no value: $query" }
            cursor.getLong(0)
        }

    private fun singleString(database: SQLiteDatabase, query: String): String =
        database.rawQuery(query, null).use { cursor ->
            require(cursor.moveToFirst()) { "Query returned no value: $query" }
            cursor.getString(0)
        }

    private data class Metadata(
        val singletonId: Int,
        val schemaVersion: Int,
        val transformVersion: Int,
        val sourceSha256: String,
        val songCount: Int,
    )

    private data class Column(
        val name: String,
        val type: String,
        val notNull: Boolean,
        val primaryKey: Boolean,
    )

    companion object {
        internal const val TEMP_FILE_PREFIX = "holysongs-catalog-"
        private const val EXPECTED_SONG_COUNT = 414
        private const val SCHEMA_VERSION = 1
        private const val TRANSFORM_VERSION = 1
        private val SHA_256 = Regex("[0-9a-f]{64}")
        private val EXPECTED_METADATA_SQL = """
            CREATE TABLE catalog_metadata (
                singleton_id INTEGER NOT NULL PRIMARY KEY CHECK (singleton_id = 1),
                schema_version INTEGER NOT NULL CHECK (schema_version = 1),
                transform_version INTEGER NOT NULL CHECK (transform_version = 1),
                source_sha256 TEXT NOT NULL CHECK (
                    length(source_sha256) = 64
                    AND source_sha256 = lower(source_sha256)
                ),
                song_count INTEGER NOT NULL CHECK (song_count >= 0)
            )
        """.trimIndent()
        private val EXPECTED_SONGS_SQL = """
            CREATE TABLE songs (
                title TEXT NOT NULL PRIMARY KEY CHECK (length(title) > 0),
                lyric TEXT NOT NULL CHECK (length(lyric) > 0),
                source_order INTEGER NOT NULL UNIQUE CHECK (source_order >= 0)
            )
        """.trimIndent()
    }
}
