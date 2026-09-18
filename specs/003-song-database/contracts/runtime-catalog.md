# Runtime Catalog Contract

## Construction boundary

`MainActivity.catalogLoaderFactory` remains the test-only construction seam. Production construction supplies a SQLite row source backed by packaged `assets/songs.db`; it must not open or parse `songs.xml` during normal runtime loading.

`SongListViewModel` retains its current contract:

```text
create owner -> Loading -> Ready(catalog)
                       \-> Error(non-blank safe reason)
Error + explicit retry -> Loading -> Ready/Error
```

One ViewModel owner triggers one load unless an error is explicitly retried. Cancellation is lifecycle control and is not presented as a parse/database error.

## Reader lifecycle

For each load, the bundled reader must:

1. execute on the loader's I/O dispatcher;
2. create a unique app-private cache file;
3. copy `assets/songs.db` completely with streams closed on all paths;
4. open the copied file with `SQLiteDatabase.OPEN_READONLY`;
5. require `PRAGMA user_version = 1`;
6. require exactly one valid metadata row with schema/transform version 1 and count 414;
7. query explicit columns `title`, `lyric`, `source_order` from `songs ORDER BY source_order`;
8. reject missing columns, null/empty values, duplicate titles/orders, non-contiguous order, wrong count, or metadata disagreement;
9. materialize storage-neutral rows and then the existing immutable `SongCatalog`;
10. close cursor/database and delete the temporary copy in `finally`, including I/O, SQLite, validation, and cancellation failures.

No runtime code writes canonical or generated song data. No writable database handle, DAO, cursor, or temporary path escapes the reader.

## Catalog behavior

The resulting catalog must preserve:

- exactly 414 titles and lyrics;
- exhaustive equality to the old effective last-entry-wins map;
- existing `ChineseCharComp` ordering;
- existing guarded pinyin initials;
- existing `LegacySectionIndex` behavior;
- immutable exposed collections;
- existing list selection, lyric intent extras, lyric rendering, share payload, and video-search title behavior.

Database `source_order` is for validation/diagnostics only. It must not replace Chinese UI sorting.

## Error behavior

Any asset, copy, open, schema, metadata, query, validation, or assembly failure produces no partial `SongCatalog`. A live ViewModel owner receives the existing error state with a non-empty safe message and may explicitly retry. Cancellation is rethrown. The app must not fall back silently to XML, an arbitrary song, an empty partial list, or network data.

## Upgrade behavior

There is no persistent catalog database. The temporary copy exists for one load and is deleted. Therefore:

- an APK update automatically supplies its packaged generated database;
- no installed database migration or destructive fallback is needed;
- SharedPreferences remain untouched;
- interrupted prior copies cannot become the next load's source;
- cache eviction is harmless.

Schema changes require a new schema/user version and updated generation/runtime contracts in a future feature or approved plan amendment.

## Search boundary

Schema v1 permits exact title lookup and separate title/lyric querying, but this feature performs only the full catalog read. It exposes no search UI or query syntax. A later search feature must research Chinese matching/tokenization and may add a derived index without changing canonical lyric text or title identity.
