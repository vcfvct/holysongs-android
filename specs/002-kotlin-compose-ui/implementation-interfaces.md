# US1 test/implementation seams

Master-selected interfaces for the T011–T023 handoff. These specify small concrete boundaries
already authorized by plan/data-model, not implemented behavior or a framework/store migration.
Use execution-decisions.md's API37 runtime scope; minSdk23 remains unchanged.

## Catalog / state (main Kotlin package com.goodtrendltd.HolySongs)

Under `.data`:

- `SongCatalogLoader(openAsset: () -> java.io.InputStream, dispatcher: CoroutineDispatcher = Dispatchers.IO)`;
  `suspend fun load(): SongCatalog`. Full/closed UTF8 read, literal U+0020 preprocessing, actual
  XMLParser, source-order HashMap overwrite, existing comparator; no swallowed cancellation.
- `SongCatalog` read-only properties `lyricsByTitle: Map<String,String>`,
  `orderedTitles: List<String>`, `initials: List<String?>`, `sectionIndex: LegacySectionIndex`.
  Constructor accepts these properties; production loader must defensively freeze data. Guarded
  initials are uppercase A–Z when resolvable, null otherwise; this does not change title ordering.
- `LegacySectionIndex(orderedTitles: List<String>)`; `val sections: List<String>` exactly A–Z;
  `fun getPositionForSection(section: Int): Int?`. Extract the original algorithm/cache semantics,
  preserve all valid captured results and return null for invalid/empty/out-of-range cases.
  Do not infer a first-initial map from this API.

Under `.ui`:

- `SongCatalogUiState`: `Loading`, `Ready(val catalog: SongCatalog)`, `Error(val reason: String)`.
- `SongListViewModel(loader: SongCatalogLoader)` with `val state: StateFlow<SongCatalogUiState>`
  and `fun retry()`. One load per ViewModel owner; retry only from Error; ViewModelStore.clear
  cancels the owner's work. Loader injection/dispatcher control supplies deterministic tests.

## Preferences (.data)

- `ReaderPreferences(context: Context, dispatcher: CoroutineDispatcher = Dispatchers.IO)`.
- `val snapshots: Flow<ReaderPreferenceSnapshot>`; cold observation registers before fresh read,
  pairs listener/unregister, uses application context and off-main initial disk access.
- `ReaderPreferenceSnapshot(fontSize: Int, effectiveFontSize: Int, nightMode: Boolean)`.
  fontSize retains typed integers even outside UI choices; missing/wrong type uses20 in memory.
  effectiveFontSize is1–200 inclusive unchanged, otherwise20. Missing/wrong-type night usestrue.
  Tests inspect actual SharedPreferences separately to prove keys/types/values remain unchanged.
- `setFontSize(Int)` accepts only16/18/20/22/24/26/28/30; `resetFontSize()` writes20 only;
  `setNightMode(Boolean)`. Only explicit calls write; observation/composition never repairs storage.

## Real Main Activity interaction tests

Use the production Activity's content, not a second test setContent. Existing strings label
standard menu/actions: settings_text, about, sharing_app_text. Stable test-only tags may locate
custom geometry without being offered as accessibility labels: song-list, letter-sidebar,
letter-A through letter-Z, letter-indicator, catalog-loading, catalog-error, catalog-empty.
Use actual title text for lazy row assertions and scroll/wait for Ready rather than assuming all
414 rows exist simultaneously in semantics.

For deterministic Main recreation-while-Loading and failure/retry tests, allow one narrow
`internal` companion property on the future MainActivity:
`catalogLoaderFactory: (Context) -> SongCatalogLoader`, defaulting to the real asset loader.
Tests temporarily supply a controlled opener/dispatcher before Activity launch and restore the
factory in finally. No intent flag, production preference, exported debug component or persistent
command controls it. It is not a navigation command, process cache or DI framework. Normal UI
never changes it; the Activity-scoped ViewModel still owns actual loading across recreation.

The test-authoring batch may reference these not-yet-created APIs; missing-type compile errors
are evidence of pending implementation, never passing runtime assertions. Do not use @Ignore or
fake production stubs to manufacture green tests. Record any additional necessary seam/proposal
for parent review rather than adding speculative APIs or silently changing these contracts.
