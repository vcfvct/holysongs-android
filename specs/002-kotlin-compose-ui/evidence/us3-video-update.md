# US3 source update (2026-09-16)

The retained Java `VideoSearch` boundary now registers an API33+ `OnBackInvokedDispatcher`
callback and retains the API23-and-older `onKeyDown` fallback. Back ordering remains loading
cancel, fullscreen exit, WebView history, then finish to the lyric. Callback registration is
removed during final teardown. The Java source compiles against the new Kotlin
`DisplayLyricActivity` and its unchanged Java-callable `SEARCH_TARGET` field.

> The historical five-test result below predates the current eight-test `VideoSearchTest` source
> and does not substantiate it. No current instrumentation run is claimed in this update.

The focused `VideoSearchTest` run on the available CPH2583 Android 16/API36 target was a
historical/pre-change five-test result. This is not API37 evidence and does not cover provider
reachability, current UTF-8 query assertions, API23, fullscreen/history runtime, or the required
provider audit. Current source-level UTF-8 status is honest but limited: `searchUrlFor` uses
`Uri.encode(title)` and preserves the three provider prefixes; current runtime verification is
unexecuted. The new `DisplayLyricLaunchGuardsTest` covers one-shot share/video state, cancellation
or failure reset, and return reset at the JVM state-machine boundary; chooser/provider runtime
behavior remains unexecuted. No provider resource or external browser behavior was changed.
