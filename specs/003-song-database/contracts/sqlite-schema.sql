-- Contract schema for feature 003. The generator may add only SQLite-internal
-- objects required by the engine; application tables/columns must match this file.

PRAGMA user_version = 1;

CREATE TABLE catalog_metadata (
    singleton_id INTEGER NOT NULL PRIMARY KEY CHECK (singleton_id = 1),
    schema_version INTEGER NOT NULL CHECK (schema_version = 1),
    transform_version INTEGER NOT NULL CHECK (transform_version = 1),
    source_sha256 TEXT NOT NULL CHECK (
        length(source_sha256) = 64
        AND source_sha256 = lower(source_sha256)
    ),
    song_count INTEGER NOT NULL CHECK (song_count >= 0)
);

CREATE TABLE songs (
    title TEXT NOT NULL PRIMARY KEY CHECK (length(title) > 0),
    lyric TEXT NOT NULL CHECK (length(lyric) > 0),
    source_order INTEGER NOT NULL UNIQUE CHECK (source_order >= 0)
);

-- Production invariant, validated by generator/runtime rather than a static
-- CHECK expression: metadata.song_count = COUNT(songs) = 414 and source_order
-- is contiguous from 0 through 413.
--
-- UI order, pinyin initials, and section positions are intentionally not
-- persisted. They remain derived by the established Kotlin helpers.
-- Search/FTS tables and indexes are intentionally absent from schema v1.
