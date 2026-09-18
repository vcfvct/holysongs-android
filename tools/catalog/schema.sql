-- Executable schema for the generated HolySongs catalog.
-- Keep synchronized with specs/003-song-database/contracts/sqlite-schema.sql;
-- generator tests compare the application schema contract.

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
