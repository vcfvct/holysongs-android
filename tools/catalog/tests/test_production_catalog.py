from __future__ import annotations

from contextlib import closing
import hashlib
from pathlib import Path
import sqlite3
import tempfile
import unittest

from tools.catalog.tests.baseline_oracle import DUPLICATE_POSITIONS, effective_catalog

try:
    from tools.catalog.generate_song_database import generate_database, load_canonical_songs, verify_database
except ModuleNotFoundError:  # Expected red state before generator implementation.
    generate_database = load_canonical_songs = verify_database = None


REPOSITORY_ROOT = Path(__file__).resolve().parents[3]
SOURCE = REPOSITORY_ROOT / "app/src/main/assets/songs.xml"
BASELINE = REPOSITORY_ROOT / "app/src/test/resources/catalog-baseline.xml"
SCHEMA = REPOSITORY_ROOT / "tools/catalog/schema.sql"
SCHEMA_CONTRACT = REPOSITORY_ROOT / "specs/003-song-database/contracts/sqlite-schema.sql"


def normalized_sql(path: Path) -> str:
    statements = []
    for line in path.read_text(encoding="utf-8").splitlines():
        content = line.split("--", 1)[0].strip()
        if content:
            statements.append(content)
    return " ".join(statements)


class ProductionCatalogTest(unittest.TestCase):
    def setUp(self) -> None:
        if generate_database is None:
            self.fail("Production generator is not implemented")

    def test_executable_schema_matches_the_documented_contract(self) -> None:
        self.assertEqual(normalized_sql(SCHEMA_CONTRACT), normalized_sql(SCHEMA))

    def test_canonical_source_and_database_match_all_effective_baseline_rows(self) -> None:
        songs = load_canonical_songs(SOURCE, expected_count=414)
        self.assertEqual(414, len(songs))
        self.assertEqual(414, len({song.title for song in songs}))
        self.assertEqual(list(range(414)), [song.source_order for song in songs])

        expected = effective_catalog(BASELINE)
        self.assertEqual(expected, {song.title: song.lyric for song in songs})
        self.assertTrue(set(DUPLICATE_POSITIONS).issubset(expected))

        with tempfile.TemporaryDirectory() as directory:
            database = Path(directory) / "songs.db"
            generate_database(SOURCE, database, SCHEMA, expected_count=414)
            verify_database(
                SOURCE,
                database,
                SCHEMA,
                expected_count=414,
                baseline=BASELINE,
            )
            with closing(sqlite3.connect(database)) as connection:
                self.assertEqual(1, connection.execute("PRAGMA user_version").fetchone()[0])
                self.assertEqual("ok", connection.execute("PRAGMA integrity_check").fetchone()[0])
                metadata = connection.execute(
                    "SELECT schema_version, transform_version, source_sha256, song_count "
                    "FROM catalog_metadata WHERE singleton_id = 1"
                ).fetchone()
                self.assertEqual(
                    (1, 1, hashlib.sha256(SOURCE.read_bytes()).hexdigest(), 414),
                    metadata,
                )
                rows = connection.execute(
                    "SELECT source_order, title, lyric FROM songs ORDER BY source_order"
                ).fetchall()
                self.assertEqual(list(range(414)), [row[0] for row in rows])
                self.assertEqual(expected, {title: lyric for _, title, lyric in rows})

    def test_repeated_generation_is_logically_identical(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            first = Path(directory) / "first.db"
            second = Path(directory) / "second.db"
            generate_database(SOURCE, first, SCHEMA, expected_count=414)
            generate_database(SOURCE, second, SCHEMA, expected_count=414)
            with closing(sqlite3.connect(first)) as left, closing(sqlite3.connect(second)) as right:
                query = "SELECT source_order, title, lyric FROM songs ORDER BY source_order"
                self.assertEqual(left.execute(query).fetchall(), right.execute(query).fetchall())
                metadata = "SELECT * FROM catalog_metadata ORDER BY singleton_id"
                self.assertEqual(left.execute(metadata).fetchall(), right.execute(metadata).fetchall())


if __name__ == "__main__":
    unittest.main()
