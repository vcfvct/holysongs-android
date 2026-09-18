# User Story 2 — Catalog Maintenance Evidence

**Date**: 2026-09-17
**Status**: PASS for host fixture workflow

## Commands and results

```text
python3 -W error::ResourceWarning -m unittest \
  tools.catalog.tests.test_catalog_mutations \
  tools.catalog.tests.test_catalog_failures
```

Result: **PASS**, 7 tests.

Covered outcomes:

- add a unique multiline song;
- edit lyric text while preserving newlines and U+3000;
- rename title as deliberate delete/add identity transition;
- delete a song;
- remove U+0020 only;
- reject malformed/wrong-root/repeated/nested/empty fields;
- reject duplicate effective titles and wrong expected count;
- reject stale source and mismatched metadata;
- preserve an already accepted output after failed generation;
- remove temporary partial output after SQL failure;
- reject an unwritable output location.

```text
./gradlew :app:verifySongCatalog --no-build-cache --rerun-tasks
```

Result: **PASS**. Gradle ran generation, all 9 Python tests then present, and logical verification against the historical baseline.

Canonical XML SHA-256 before and after the isolated fixture drill remained:

```text
d6f0e9bed1a2031335613e746e3983842b3462a4d91c4a3d7c04537ba609e85e
```

Generated build output was `app/build/generated/songCatalog/assets/songs.db`, 196608 bytes. It remains ignored under `app/build/`; no generated database is tracked.
