# User Story 3 — Search-Readiness Evidence

**Date**: 2026-09-17
**Status**: PASS for scoped schema boundary; search remains intentionally unimplemented

## Automated checks

```text
python3 -W error::ResourceWarning -m unittest discover -s tools/catalog/tests -p 'test_*.py'
```

Result: **PASS**, 12 tests including `test_search_readiness.py`.

```text
./gradlew :app:verifySongCatalog --no-build-cache --rerun-tasks
```

Result: **PASS**. The verifier rejects extra application tables/indexes, including attempted search shadow objects.

## Packaged schema inspection

After `./gradlew :app:assembleDebug --no-build-cache --rerun-tasks`, the extracted APK database contained only:

```text
(table, catalog_metadata)
(table, songs)
```

`songs` columns:

```text
title        TEXT     NOT NULL  PRIMARY KEY
lyric        TEXT     NOT NULL
source_order INTEGER  NOT NULL  UNIQUE
```

Independent `SELECT title ...` and `SELECT lyric ...` queries succeeded; the lyric query returned 414 rows. Unchanged titles retain direct title identity, and fixture tests prove rename-as-delete/add behavior.

A source scan found no song-search UI or FTS references under `app/src/main/kotlin` or `app/src/main/res`. Chinese tokenization, substring semantics, ranking, highlighting, FTS selection, and UI remain deferred.
