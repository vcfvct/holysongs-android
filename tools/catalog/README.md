# Song catalog tooling

`songs.xml` is the only human-edited catalog. SQLite is generated output; never edit a `.db` file by hand.

Canonical format and validation rules are defined in:

- [`specs/003-song-database/contracts/canonical-xml.md`](../../specs/003-song-database/contracts/canonical-xml.md)
- [`specs/003-song-database/contracts/generation.md`](../../specs/003-song-database/contracts/generation.md)

## Normal commands

From the repository root:

```bash
python3 -m unittest discover -s tools/catalog/tests -p 'test_*.py'
./gradlew :app:generateSongDatabase :app:verifySongCatalog
```

The Gradle task writes `app/build/generated/songCatalog/assets/songs.db`. It is an ignored build output and is packaged automatically as `assets/songs.db`.

Manual generation and verification are available for diagnostics:

```bash
python3 tools/catalog/generate_song_database.py generate \
  --source app/src/main/assets/songs.xml \
  --schema tools/catalog/schema.sql \
  --output /tmp/songs.db \
  --expected-count 414

python3 tools/catalog/generate_song_database.py verify \
  --source app/src/main/assets/songs.xml \
  --schema tools/catalog/schema.sql \
  --database /tmp/songs.db \
  --baseline app/src/test/resources/catalog-baseline.xml \
  --expected-count 414
```

## Add, edit, rename, or delete

1. Edit only `app/src/main/assets/songs.xml`.
2. Preserve the text-only `<song><name>…</name><lyric>…</lyric></song>` structure.
3. Run Python tests and `:app:verifySongCatalog`.
4. Review the XML diff; do not add or commit `app/build/generated/`.
5. Build the APK and verify its packaged `assets/songs.db` as described in the feature quickstart.

Effective title identity is the title after removing U+0020 ASCII spaces. Therefore:

- an added title must be unique after that transform;
- a title edit/rename intentionally deletes the old identity and creates the new identity;
- deleting the XML block deletes the generated row;
- full-width U+3000 spaces, line feeds, blank lines, punctuation, and all other code points are retained;
- duplicate titles are errors; there is no last-entry-wins behavior in the canonical source.

The generator writes to a temporary sibling file and replaces the requested output only after validation. On malformed XML, duplicate/empty fields, count mismatch, SQL failure, stale metadata, or baseline mismatch, fix the reported XML/schema issue and rerun. A failed run must not be worked around by editing SQLite.

## Future search boundary

Schema v1 stores `title` and `lyric` as separate queryable `TEXT` fields and uses effective title as the primary identity. It deliberately includes no FTS table, tokenized text, ranking data, or search UI.

A later feature must define and test Chinese matching semantics before choosing ordinary substring queries, `unicode61`, trigram FTS, or another tokenizer. Any derived search index must be rebuildable from `songs`; it must not rewrite canonical lyrics or replace title identity.
