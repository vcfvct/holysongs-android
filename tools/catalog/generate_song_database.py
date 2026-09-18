#!/usr/bin/env python3
"""Validate the canonical XML catalog and generate/verify its SQLite form."""

from __future__ import annotations

import argparse
from contextlib import closing
from dataclasses import dataclass
import hashlib
import os
from pathlib import Path
import sqlite3
import sys
import tempfile
import xml.etree.ElementTree as ET

SCHEMA_VERSION = 1
TRANSFORM_VERSION = 1
PRODUCTION_COUNT = 414
DEFAULT_SCHEMA = Path(__file__).with_name("schema.sql")


class CatalogError(ValueError):
    """A safe, actionable catalog validation failure."""


@dataclass(frozen=True)
class CanonicalSong:
    title: str
    lyric: str
    source_order: int


def _effective(value: str) -> str:
    return value.replace(" ", "")


def _parse_xml(path: Path) -> ET.Element:
    try:
        payload = path.read_bytes()
    except OSError as failure:
        raise CatalogError(f"Unable to read XML source {path}: {failure}") from failure
    upper = payload.upper()
    if b"<!DOCTYPE" in upper or b"<!ENTITY" in upper:
        raise CatalogError(f"Unsupported DTD/entity declaration in {path}")
    try:
        # Decode explicitly so invalid UTF-8 cannot fall back to an XML declaration encoding.
        text = payload.decode("utf-8")
        return ET.fromstring(text)
    except (UnicodeDecodeError, ET.ParseError) as failure:
        raise CatalogError(f"Malformed UTF-8 XML in {path}: {failure}") from failure


def load_canonical_songs(path: Path, expected_count: int = PRODUCTION_COUNT) -> list[CanonicalSong]:
    root = _parse_xml(path)
    if root.tag != "songs" or root.attrib:
        raise CatalogError(f"Expected an attribute-free <songs> root in {path}")
    if root.text and root.text.strip():
        raise CatalogError(f"Unexpected text directly under <songs> in {path}")

    songs: list[CanonicalSong] = []
    seen: dict[str, int] = {}
    for position, song in enumerate(list(root)):
        if song.tag != "song" or song.attrib:
            raise CatalogError(f"Entry {position} in {path} must be an attribute-free <song>")
        fields = list(song)
        if [field.tag for field in fields] != ["name", "lyric"]:
            raise CatalogError(
                f"Song entry {position} in {path} must contain exactly <name> then <lyric>"
            )
        if any(field.attrib or list(field) for field in fields):
            raise CatalogError(f"Song entry {position} in {path} must contain text-only fields")
        if song.text and song.text.strip():
            raise CatalogError(f"Unexpected text before <name> at song entry {position} in {path}")
        if fields[0].tail and fields[0].tail.strip():
            raise CatalogError(f"Unexpected text between fields at song entry {position} in {path}")
        if fields[1].tail and fields[1].tail.strip():
            raise CatalogError(f"Unexpected text after <lyric> at song entry {position} in {path}")

        title = _effective(fields[0].text or "")
        lyric = _effective(fields[1].text or "")
        if not title:
            raise CatalogError(f"Song entry {position} in {path} has an empty effective title")
        if not lyric:
            raise CatalogError(f"Song entry {position} ({title}) in {path} has an empty effective lyric")
        if title in seen:
            raise CatalogError(
                f"Duplicate effective title {title!r} at positions {seen[title]} and {position} in {path}"
            )
        seen[title] = position
        songs.append(CanonicalSong(title=title, lyric=lyric, source_order=position))

    if len(songs) != expected_count:
        raise CatalogError(f"Expected {expected_count} songs in {path}, found {len(songs)}")
    return songs


def _legacy_effective_catalog(path: Path) -> dict[str, str]:
    root = _parse_xml(path)
    if root.tag != "songs":
        raise CatalogError(f"Expected <songs> root in baseline {path}")
    result: dict[str, str] = {}
    for position, song in enumerate(root.findall("song")):
        name = song.find("name")
        lyric = song.find("lyric")
        if name is None or lyric is None:
            raise CatalogError(f"Missing baseline field at position {position} in {path}")
        result[_effective(name.text or "")] = _effective(lyric.text or "")
    return result


def _schema_sql(schema_path: Path) -> str:
    try:
        return schema_path.read_text(encoding="utf-8")
    except OSError as failure:
        raise CatalogError(f"Unable to read schema {schema_path}: {failure}") from failure


def _connect_read_only(path: Path) -> sqlite3.Connection:
    try:
        return sqlite3.connect(f"file:{path.resolve()}?mode=ro", uri=True)
    except sqlite3.Error as failure:
        raise CatalogError(f"Unable to open SQLite database {path}: {failure}") from failure


def _expected_rows(songs: list[CanonicalSong]) -> list[tuple[int, str, str]]:
    return [(song.source_order, song.title, song.lyric) for song in songs]


def _normalized_create_sql(value: str | None) -> str:
    return "".join((value or "").lower().split())


def _application_schema_signature(connection: sqlite3.Connection) -> list[tuple[str, str, str]]:
    return [
        (object_type, name, _normalized_create_sql(sql))
        for object_type, name, sql in connection.execute(
            "SELECT type, name, sql FROM sqlite_master "
            "WHERE name NOT LIKE 'sqlite_%' ORDER BY type, name"
        )
    ]


def _schema_signature_from_file(
    schema: Path,
) -> tuple[int, list[tuple[str, str, str]]]:
    try:
        with closing(sqlite3.connect(":memory:")) as connection:
            connection.executescript(_schema_sql(schema))
            user_version = connection.execute("PRAGMA user_version").fetchone()[0]
            return user_version, _application_schema_signature(connection)
    except sqlite3.Error as failure:
        raise CatalogError(f"Invalid SQLite schema {schema}: {failure}") from failure


def _expected_schema_signature() -> tuple[int, list[tuple[str, str, str]]]:
    return _schema_signature_from_file(DEFAULT_SCHEMA)


def verify_database(
    source: Path,
    database: Path,
    schema: Path = DEFAULT_SCHEMA,
    expected_count: int = PRODUCTION_COUNT,
    baseline: Path | None = None,
) -> None:
    songs = load_canonical_songs(source, expected_count=expected_count)
    if not database.is_file():
        raise CatalogError(f"Missing generated database {database}")

    expected_schema_version, expected_schema = _expected_schema_signature()
    supplied_schema_version, supplied_schema = _schema_signature_from_file(schema)
    if (
        expected_schema_version != SCHEMA_VERSION
        or supplied_schema_version != expected_schema_version
        or supplied_schema != expected_schema
    ):
        raise CatalogError(f"Schema {schema} does not match the canonical schema {DEFAULT_SCHEMA}")

    expected_digest = hashlib.sha256(source.read_bytes()).hexdigest()
    with closing(_connect_read_only(database)) as connection:
        try:
            user_version = connection.execute("PRAGMA user_version").fetchone()[0]
            if user_version != SCHEMA_VERSION:
                raise CatalogError(
                    f"Database {database} has user_version {user_version}, expected {SCHEMA_VERSION}"
                )
            integrity = connection.execute("PRAGMA integrity_check").fetchone()[0]
            if integrity != "ok":
                raise CatalogError(f"Database {database} failed integrity_check: {integrity}")

            metadata_rows = connection.execute(
                "SELECT singleton_id, schema_version, transform_version, source_sha256, song_count "
                "FROM catalog_metadata ORDER BY singleton_id"
            ).fetchall()
            expected_metadata = [(1, SCHEMA_VERSION, TRANSFORM_VERSION, expected_digest, expected_count)]
            if metadata_rows != expected_metadata:
                raise CatalogError(
                    f"Database {database} metadata mismatch: {metadata_rows!r} != {expected_metadata!r}"
                )

            rows = connection.execute(
                "SELECT source_order, title, lyric FROM songs ORDER BY source_order"
            ).fetchall()
            expected_rows = _expected_rows(songs)
            if rows != expected_rows:
                raise CatalogError(f"Database {database} rows do not match canonical source {source}")

            actual_schema = _application_schema_signature(connection)
            if actual_schema != expected_schema:
                raise CatalogError(f"Database {database} does not match the complete schema contract")

            table_info = {
                table: connection.execute(f"PRAGMA table_info({table})").fetchall()
                for table in ("catalog_metadata", "songs")
            }
            metadata_columns = table_info["catalog_metadata"]
            if [row[1] for row in metadata_columns] != [
                "singleton_id", "schema_version", "transform_version", "source_sha256", "song_count"
            ]:
                raise CatalogError(f"Database {database} has an unsupported metadata schema")
            expected_metadata_types = ["INTEGER", "INTEGER", "INTEGER", "TEXT", "INTEGER"]
            if [row[2].upper() for row in metadata_columns] != expected_metadata_types:
                raise CatalogError(f"Database {database} has unsupported metadata column types")
            if any(row[3] != 1 for row in metadata_columns):
                raise CatalogError(f"Database {database} metadata columns must be non-null")
            if [row[5] for row in metadata_columns] != [1, 0, 0, 0, 0]:
                raise CatalogError(f"Database {database} has an unsupported metadata primary key")
            if [row[1] for row in table_info["songs"]] != ["title", "lyric", "source_order"]:
                raise CatalogError(f"Database {database} has an unsupported songs schema")
            title = table_info["songs"][0]
            if title[2].upper() != "TEXT" or title[3] != 1 or title[5] != 1:
                raise CatalogError(f"Database {database} title must be the non-null TEXT primary key")
            lyric = table_info["songs"][1]
            if lyric[2].upper() != "TEXT" or lyric[3] != 1 or lyric[5] != 0:
                raise CatalogError(f"Database {database} lyric must be non-null TEXT")
            source_order = table_info["songs"][2]
            if source_order[2].upper() != "INTEGER" or source_order[3] != 1 or source_order[5] != 0:
                raise CatalogError(f"Database {database} source_order must be non-null INTEGER")
            unique_index_columns = {
                tuple(column[2] for column in connection.execute(f"PRAGMA index_info({index[1]})"))
                for index in connection.execute("PRAGMA index_list(songs)")
                if index[2] == 1
            }
            if ("source_order",) not in unique_index_columns:
                raise CatalogError(f"Database {database} source_order must be unique")
        except sqlite3.Error as failure:
            raise CatalogError(f"Unable to verify SQLite database {database}: {failure}") from failure

    if baseline is not None:
        expected_effective = _legacy_effective_catalog(baseline)
        actual_effective = {song.title: song.lyric for song in songs}
        if len(expected_effective) != 414 or actual_effective != expected_effective:
            raise CatalogError(
                f"Canonical source {source} does not match the historical effective baseline {baseline}"
            )


def generate_database(
    source: Path,
    output: Path,
    schema: Path = DEFAULT_SCHEMA,
    expected_count: int = PRODUCTION_COUNT,
) -> None:
    songs = load_canonical_songs(source, expected_count=expected_count)
    source_digest = hashlib.sha256(source.read_bytes()).hexdigest()
    temporary: Path | None = None
    try:
        output.parent.mkdir(parents=True, exist_ok=True)
        descriptor, temporary_name = tempfile.mkstemp(
            prefix=f".{output.name}.", suffix=".tmp", dir=output.parent
        )
        os.close(descriptor)
        temporary = Path(temporary_name)
        connection = sqlite3.connect(temporary)
        try:
            connection.execute("PRAGMA journal_mode = DELETE")
            connection.execute("PRAGMA synchronous = FULL")
            connection.executescript(_schema_sql(schema))
            with connection:
                connection.execute(
                    "INSERT INTO catalog_metadata "
                    "(singleton_id, schema_version, transform_version, source_sha256, song_count) "
                    "VALUES (?, ?, ?, ?, ?)",
                    (1, SCHEMA_VERSION, TRANSFORM_VERSION, source_digest, len(songs)),
                )
                connection.executemany(
                    "INSERT INTO songs (title, lyric, source_order) VALUES (?, ?, ?)",
                    [(song.title, song.lyric, song.source_order) for song in songs],
                )
        finally:
            connection.close()
        verify_database(source, temporary, schema, expected_count=expected_count)
        os.replace(temporary, output)
        temporary = None
    except (CatalogError, OSError, sqlite3.Error) as failure:
        if temporary is not None:
            temporary.unlink(missing_ok=True)
        if isinstance(failure, CatalogError):
            raise
        raise CatalogError(f"Unable to generate {output} from {source}: {failure}") from failure


def _parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description=__doc__)
    subparsers = parser.add_subparsers(dest="command", required=True)

    generate = subparsers.add_parser("generate", help="Generate a validated SQLite catalog")
    generate.add_argument("--source", required=True, type=Path)
    generate.add_argument("--output", required=True, type=Path)
    generate.add_argument("--schema", type=Path, default=DEFAULT_SCHEMA)
    generate.add_argument("--expected-count", type=int, default=PRODUCTION_COUNT)

    verify = subparsers.add_parser("verify", help="Verify logical SQLite/source parity")
    verify.add_argument("--source", required=True, type=Path)
    verify.add_argument("--database", required=True, type=Path)
    verify.add_argument("--schema", type=Path, default=DEFAULT_SCHEMA)
    verify.add_argument("--baseline", type=Path)
    verify.add_argument("--expected-count", type=int, default=PRODUCTION_COUNT)
    return parser


def main(argv: list[str] | None = None) -> int:
    arguments = _parser().parse_args(argv)
    try:
        if arguments.command == "generate":
            generate_database(arguments.source, arguments.output, arguments.schema, arguments.expected_count)
        else:
            verify_database(
                arguments.source,
                arguments.database,
                arguments.schema,
                arguments.expected_count,
                arguments.baseline,
            )
        return 0
    except CatalogError as failure:
        print(f"catalog error: {failure}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
