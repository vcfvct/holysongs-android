from __future__ import annotations

from contextlib import closing
from pathlib import Path
import sqlite3
import tempfile
import unittest

from tools.catalog.generate_song_database import CatalogError, generate_database, verify_database

ROOT = Path(__file__).resolve().parents[3]
MUTATIONS = ROOT / "tools/catalog/tests/fixtures/mutations"
FAILURES = ROOT / "tools/catalog/tests/fixtures/failures"
SCHEMA = ROOT / "tools/catalog/schema.sql"


class CatalogFailureTest(unittest.TestCase):
    def test_invalid_xml_shapes_and_duplicate_effective_titles_are_rejected(self) -> None:
        for name in ("duplicate.xml", "nested.xml", "empty.xml", "wrong-root.xml", "repeated.xml", "malformed.xml"):
            with self.subTest(name=name), tempfile.TemporaryDirectory() as directory:
                with self.assertRaises(CatalogError):
                    generate_database(FAILURES / name, Path(directory) / "songs.db", SCHEMA, expected_count=1)

    def test_wrong_count_is_rejected(self) -> None:
        with tempfile.TemporaryDirectory() as directory, self.assertRaises(CatalogError):
            generate_database(MUTATIONS / "base.xml", Path(directory) / "songs.db", SCHEMA, expected_count=3)

    def test_failed_generation_leaves_existing_output_unchanged(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            output = Path(directory) / "songs.db"
            generate_database(MUTATIONS / "base.xml", output, SCHEMA, expected_count=2)
            before = output.read_bytes()
            with self.assertRaises(CatalogError):
                generate_database(FAILURES / "malformed.xml", output, SCHEMA, expected_count=1)
            self.assertEqual(before, output.read_bytes())

            invalid_schema = Path(directory) / "invalid-schema.sql"
            invalid_schema.write_text("CREATE TABL broken", encoding="utf-8")
            with self.assertRaises(CatalogError):
                generate_database(MUTATIONS / "base.xml", output, invalid_schema, expected_count=2)
            self.assertEqual(before, output.read_bytes())

            weakened_schema = Path(directory) / "weakened-schema.sql"
            weakened_schema.write_text(
                "PRAGMA user_version=1;"
                "CREATE TABLE catalog_metadata("
                "singleton_id INTEGER, schema_version INTEGER, transform_version INTEGER,"
                "source_sha256 TEXT, song_count INTEGER);"
                "CREATE TABLE songs("
                "title TEXT PRIMARY KEY NOT NULL, lyric TEXT NOT NULL,"
                "source_order INTEGER NOT NULL UNIQUE);",
                encoding="utf-8",
            )
            with self.assertRaises(CatalogError):
                generate_database(MUTATIONS / "base.xml", output, weakened_schema, expected_count=2)
            self.assertEqual(before, output.read_bytes())
            with self.assertRaises(CatalogError):
                verify_database(
                    MUTATIONS / "base.xml",
                    output,
                    weakened_schema,
                    expected_count=2,
                )

            wrong_version_schema = Path(directory) / "wrong-version-schema.sql"
            wrong_version_schema.write_text(
                SCHEMA.read_text(encoding="utf-8").replace(
                    "PRAGMA user_version = 1;",
                    "PRAGMA user_version = 99;",
                ),
                encoding="utf-8",
            )
            with self.assertRaises(CatalogError):
                verify_database(
                    MUTATIONS / "base.xml",
                    output,
                    wrong_version_schema,
                    expected_count=2,
                )
            self.assertEqual([], list(Path(directory).glob(".songs.db.*.tmp")))

    def test_stale_source_and_schema_metadata_are_rejected(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            database = Path(directory) / "songs.db"
            generate_database(MUTATIONS / "base.xml", database, SCHEMA, expected_count=2)
            with self.assertRaises(CatalogError):
                verify_database(MUTATIONS / "edited.xml", database, SCHEMA, expected_count=2)

            with closing(sqlite3.connect(database)) as connection:
                connection.execute("UPDATE catalog_metadata SET source_sha256 = ?", ("0" * 64,))
                connection.commit()
            with self.assertRaises(CatalogError):
                verify_database(MUTATIONS / "base.xml", database, SCHEMA, expected_count=2)

    def test_unwritable_output_location_fails_without_partial_database(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            parent_is_file = Path(directory) / "not-a-directory"
            parent_is_file.write_text("occupied", encoding="utf-8")
            output = parent_is_file / "songs.db"
            with self.assertRaises((CatalogError, OSError)):
                generate_database(MUTATIONS / "base.xml", output, SCHEMA, expected_count=2)
            self.assertFalse(output.exists())


if __name__ == "__main__":
    unittest.main()
