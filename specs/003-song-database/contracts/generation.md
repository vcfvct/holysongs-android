# Catalog Generation Contract

## Command surface

The implementation must provide one repository-local Python 3 entry point:

```text
python3 tools/catalog/generate_song_database.py generate \
  --source <songs.xml> \
  --output <songs.db> \
  [--expected-count <n>]

python3 tools/catalog/generate_song_database.py verify \
  --source <songs.xml> \
  --database <songs.db> \
  [--baseline <legacy-422.xml>] \
  [--expected-count <n>]
```

Exact option spelling may be adjusted during implementation only if the same capabilities and documented Gradle entry point remain. Commands return zero only when every requested validation succeeds and print actionable, non-secret diagnostics on failure.

## Gradle integration

The app module owns a `generateSongDatabase` task with:

- inputs: canonical XML, generator source, schema/transform versions, and any checked-in generator support file;
- output directory: `app/build/generated/songCatalog/assets/`;
- output file: `songs.db`;
- dependency from Android asset packaging/pre-build so stale or missing output cannot be packaged;
- no output under tracked source directories;
- no machine-specific absolute path written into the database.

A `verifySongCatalog`/generator-test task must be reachable from the normal verification lifecycle and must validate logical database content. `clean` removes the generated asset.

## Generation transaction

1. Read exact canonical source bytes and compute lowercase SHA-256.
2. Parse and validate the canonical XML contract.
3. Apply the effective-value transform.
4. Refuse duplicate effective titles.
5. Create a new temporary SQLite file; never update the accepted output in place.
6. Apply `contracts/sqlite-schema.sql` and set `PRAGMA user_version = 1`.
7. Insert metadata and song rows in one transaction using bound parameters.
8. Commit and close all statements/connections.
9. Verify integrity, schema/user version, singleton metadata, count, uniqueness, contiguous source order, and exact ordered values.
10. Replace the task output only after all checks pass. On failure, delete the temporary file and leave no newly accepted partial output.

The generator must not add timestamps, random IDs, host paths, locale-dependent sorting, or implicit platform text normalization.

## Verification semantics

Logical equality consists of:

- exact table/index/schema contract;
- exact metadata values;
- exact `(source_order, title, lyric)` sequence;
- exact code points returned for title and lyric;
- successful `PRAGMA integrity_check`;
- absence of required WAL/journal sidecars.

Byte-identical SQLite files are not required across different Python/SQLite versions. A binary hash alone is not sufficient proof of logical equality.

When a historical baseline is supplied, verification derives the old effective map by U+0020 removal plus last-entry-wins behavior, then requires exact equality with all 414 generated title/lyric pairs. The historical baseline is read-only and is not rewritten to 414 entries.

## Failure contract

Errors identify the source or database path and, where applicable, song position/title and violated rule. Failure categories include:

- malformed/unsupported XML;
- missing, repeated, nested, or empty fields;
- duplicate effective title;
- wrong source count;
- SQLite constraint/collision failure;
- schema, metadata, transform, or user-version mismatch;
- source digest mismatch;
- row/count/order/content mismatch;
- integrity failure;
- I/O failure.

No failure may result in a packaged partial database or a successful Gradle task.
