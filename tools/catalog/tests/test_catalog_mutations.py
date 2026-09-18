from __future__ import annotations

from contextlib import closing
from pathlib import Path
import sqlite3
import tempfile
import unittest

from tools.catalog.generate_song_database import generate_database

ROOT = Path(__file__).resolve().parents[3]
FIXTURES = ROOT / "tools/catalog/tests/fixtures/mutations"
SCHEMA = ROOT / "tools/catalog/schema.sql"


def rows(path: Path, count: int) -> list[tuple[str, str, int]]:
    with tempfile.TemporaryDirectory() as directory:
        database = Path(directory) / "songs.db"
        generate_database(path, database, SCHEMA, expected_count=count)
        with closing(sqlite3.connect(database)) as connection:
            return connection.execute(
                "SELECT title, lyric, source_order FROM songs ORDER BY source_order"
            ).fetchall()


class CatalogMutationTest(unittest.TestCase):
    def test_add_edit_rename_and_delete_are_reflected_exactly(self) -> None:
        base = rows(FIXTURES / "base.xml", 2)
        added = rows(FIXTURES / "added.xml", 3)
        edited = rows(FIXTURES / "edited.xml", 2)
        renamed = rows(FIXTURES / "renamed.xml", 2)
        deleted = rows(FIXTURES / "deleted.xml", 1)

        self.assertEqual(["甲", "乙"], [row[0] for row in base])
        self.assertEqual(["甲", "乙", "丙"], [row[0] for row in added])
        self.assertEqual("已编辑\n歌词　保持格式", edited[0][1])
        self.assertNotIn("甲", [row[0] for row in renamed])
        self.assertIn("甲新", [row[0] for row in renamed])
        self.assertEqual([("甲", "第一行\n第二行　保留全宽空格", 0)], deleted)

    def test_only_ascii_space_is_removed(self) -> None:
        base = rows(FIXTURES / "base.xml", 2)
        self.assertEqual("Latinwords会移除ASCII空格", base[1][1])
        self.assertIn("　", base[0][1])
        self.assertIn("\n", base[0][1])


if __name__ == "__main__":
    unittest.main()
