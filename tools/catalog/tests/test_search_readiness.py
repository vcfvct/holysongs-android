from __future__ import annotations

from contextlib import closing
from pathlib import Path
import sqlite3
import tempfile
import unittest

from tools.catalog.generate_song_database import CatalogError, generate_database, verify_database

ROOT = Path(__file__).resolve().parents[3]
FIXTURES = ROOT / "tools/catalog/tests/fixtures/mutations"
SCHEMA = ROOT / "tools/catalog/schema.sql"


class SearchReadinessTest(unittest.TestCase):
    def test_title_is_identity_and_title_and_lyric_are_independently_queryable(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            database = Path(directory) / "songs.db"
            generate_database(FIXTURES / "base.xml", database, SCHEMA, expected_count=2)
            with closing(sqlite3.connect(database)) as connection:
                columns = connection.execute("PRAGMA table_info(songs)").fetchall()
                self.assertEqual(["title", "lyric", "source_order"], [column[1] for column in columns])
                self.assertEqual(("TEXT", 1, 1), (columns[0][2], columns[0][3], columns[0][5]))
                self.assertEqual(("TEXT", 1), (columns[1][2], columns[1][3]))
                self.assertEqual([("甲",), ("乙",)], connection.execute(
                    "SELECT title FROM songs ORDER BY source_order"
                ).fetchall())
                self.assertEqual(
                    ["第一行\n第二行　保留全宽空格", "Latinwords会移除ASCII空格"],
                    [row[0] for row in connection.execute(
                        "SELECT lyric FROM songs ORDER BY source_order"
                    ).fetchall()],
                )
                objects = connection.execute(
                    "SELECT name FROM sqlite_master WHERE name NOT LIKE 'sqlite_%' ORDER BY name"
                ).fetchall()
                self.assertEqual([("catalog_metadata",), ("songs",)], objects)

    def test_rename_replaces_title_identity(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            base = Path(directory) / "base.db"
            renamed = Path(directory) / "renamed.db"
            generate_database(FIXTURES / "base.xml", base, SCHEMA, expected_count=2)
            generate_database(FIXTURES / "renamed.xml", renamed, SCHEMA, expected_count=2)
            with closing(sqlite3.connect(base)) as left, closing(sqlite3.connect(renamed)) as right:
                left_titles = {row[0] for row in left.execute("SELECT title FROM songs")}
                right_titles = {row[0] for row in right.execute("SELECT title FROM songs")}
            self.assertIn("甲", left_titles)
            self.assertNotIn("甲", right_titles)
            self.assertIn("甲新", right_titles)

    def test_verifier_rejects_unapproved_search_or_application_objects(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            database = Path(directory) / "songs.db"
            generate_database(FIXTURES / "base.xml", database, SCHEMA, expected_count=2)
            with closing(sqlite3.connect(database)) as connection:
                connection.execute("CREATE TABLE search_shadow(term TEXT)")
                connection.commit()
            with self.assertRaises(CatalogError):
                verify_database(FIXTURES / "base.xml", database, SCHEMA, expected_count=2)


if __name__ == "__main__":
    unittest.main()
