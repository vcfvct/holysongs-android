package com.goodtrendltd.HolySongs

import android.content.Intent
import android.net.Uri
import android.webkit.GeolocationPermissions
import android.webkit.TestSslErrorHandler
import android.webkit.WebResourceRequest
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Deterministic safety checks for the retained Java video boundary. */
@RunWith(AndroidJUnit4::class)
class VideoSearchTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val testContext = instrumentation.context

    @Test
    fun invalidExtrasFinishWithoutCreatingAWebView() {
        val cases = listOf<Intent.() -> Unit>(
            { putExtra(DisplayLyricActivity.SEARCH_TARGET, context.getString(R.string.youtube)) },
            { putExtra(MainActivity.SONG_NAME, "一首歌") },
            {
                putExtra(DisplayLyricActivity.SEARCH_TARGET, 7)
                putExtra(MainActivity.SONG_NAME, "一首歌")
            },
            {
                putExtra(DisplayLyricActivity.SEARCH_TARGET, "unsupported")
                putExtra(MainActivity.SONG_NAME, "一首歌")
            },
            {
                putExtra(DisplayLyricActivity.SEARCH_TARGET, "youku")
                putExtra(MainActivity.SONG_NAME, "一首歌")
            },
            {
                putExtra(DisplayLyricActivity.SEARCH_TARGET, "tudou")
                putExtra(MainActivity.SONG_NAME, "一首歌")
            },
            {
                putExtra(DisplayLyricActivity.SEARCH_TARGET, context.getString(R.string.youtube))
                putExtra(MainActivity.SONG_NAME, 7)
            },
            {
                putExtra(DisplayLyricActivity.SEARCH_TARGET, context.getString(R.string.youtube))
                putExtra(MainActivity.SONG_NAME, "")
            }
        )

        cases.forEach { configure ->
            val intent = Intent(context, VideoSearch::class.java).also(configure)
            val scenario = ActivityScenario.launch<VideoSearch>(intent)
            try {
                assertEquals(Lifecycle.State.DESTROYED, scenario.state)
            } finally {
                scenario.close()
            }
        }
    }

    @Test
    fun providerIdentityAndFixtureInputsRemainExplicit() {
        val title = "一首中文歌"
        val providers = listOf(
            context.getString(R.string.youtube) to context.getString(R.string.youtube_url),
            context.getString(R.string.bilibili) to context.getString(R.string.bilibili_url)
        )
        providers.forEach { (target, prefix) ->
            val intent = Intent(context, VideoSearch::class.java)
                .putExtra(DisplayLyricActivity.SEARCH_TARGET, target)
                .putExtra(MainActivity.SONG_NAME, title)
            assertEquals(target, intent.getStringExtra(DisplayLyricActivity.SEARCH_TARGET))
            assertEquals(title, intent.getStringExtra(MainActivity.SONG_NAME))
            assertEquals(prefix, when (target) {
                context.getString(R.string.youtube) -> context.getString(R.string.youtube_url)
                else -> context.getString(R.string.bilibili_url)
            })
            assertEquals(prefix + Uri.encode(title), VideoSearch.searchUrlFor(context, target, title))
        }
        assertTrue(VideoSearch.isSupportedTarget(context, context.getString(R.string.youtube)))
        assertTrue(VideoSearch.isSupportedTarget(context, context.getString(R.string.bilibili)))
        assertFalse(VideoSearch.isSupportedTarget(context, "youku"))
        assertFalse(VideoSearch.isSupportedTarget(context, "tudou"))
        assertEquals("youtube", context.getString(R.string.youtube))
        assertEquals("bilibili", context.getString(R.string.bilibili))
        assertEquals("http://m.youtube.com/results?q=", context.getString(R.string.youtube_url))
        assertEquals("https://search.bilibili.com/all?keyword=", context.getString(R.string.bilibili_url))
        assertNull(VideoSearch.searchUrlFor(context, "youku", title))
        assertNull(VideoSearch.searchUrlFor(context, "tudou", title))

        listOf("loading.html", "ok.html", "error-main-frame.html", "fullscreen.html",
            "geolocation.html", "ssl-error.html").forEach { name ->
            testContext.assets.open("video-fixtures/$name").use { input ->
                assertTrue("fixture must be nonempty: $name", input.read() >= 0)
            }
        }
    }

    @Test
    fun queryValueIsUtf8EncodedWithoutChangingProviderPrefixes() {
        val title = "中文 & a/b?=+%"
        val expectedQuery = Uri.encode(title)
        val providers = listOf(
            context.getString(R.string.youtube) to context.getString(R.string.youtube_url),
            context.getString(R.string.bilibili) to context.getString(R.string.bilibili_url),
        )
        providers.forEach { (target, prefix) ->
            assertEquals(prefix + expectedQuery, VideoSearch.searchUrlFor(context, target, title))
        }
        assertNull(VideoSearch.searchUrlFor(context, "youku", title))
        assertNull(VideoSearch.searchUrlFor(context, "tudou", title))
        assertEquals("中文 & a/b?=+%", title)
    }

    @Test
    fun webViewKeepsOnlyHttpAndHttpsAndConsumesOtherSchemes() {
        assertTrue(HTML5WebView.isHttpOrHttpsUrl("HTTP://example.test/path"))
        assertTrue(HTML5WebView.isHttpOrHttpsUrl("https://example.test/path"))
        assertFalse(HTML5WebView.isHttpOrHttpsUrl("intent://example.test"))
        assertFalse(HTML5WebView.isHttpOrHttpsUrl("mailto:test@example.test"))

        lateinit var webView: HTML5WebView
        lateinit var client: android.webkit.WebViewClient
        instrumentation.runOnMainSync {
            webView = HTML5WebView(context.applicationContext)
            client = webView.getWebViewClientForTest()
            assertFalse(client.shouldOverrideUrlLoading(webView, "https://example.test"))
            assertTrue(client.shouldOverrideUrlLoading(webView, "intent://example.test"))
            val request = object : WebResourceRequest {
                override fun getUrl() = Uri.parse("mailto:test@example.test")
                override fun isForMainFrame() = true
                override fun isRedirect() = false
                override fun hasGesture() = true
                override fun getMethod() = "GET"
                override fun getRequestHeaders() = emptyMap<String, String>()
            }
            assertTrue(client.shouldOverrideUrlLoading(webView, request))
            webView.release()
        }
        assertTrue(webView.isReleased)
    }

    @Test
    fun webChromeProgressCompletionSettlesActiveLoad() {
        lateinit var webView: HTML5WebView
        instrumentation.runOnMainSync {
            webView = HTML5WebView(context.applicationContext)
            val client = webView.getWebViewClientForTest()
            val chrome = webView.getWebChromeClientForTest()
            client.onPageStarted(webView, "https://fixture.invalid/loading", null)
            assertTrue(webView.isLoading)
            chrome.onProgressChanged(webView, 99)
            assertTrue(webView.isLoading)
            chrome.onProgressChanged(webView, 100)
            assertFalse(webView.isLoading)
            webView.release()
        }
        assertTrue(webView.isReleased)
    }

    @Test
    fun mainFrameFailureSettlesLoadingAndGeolocationIsDenied() {
        lateinit var webView: HTML5WebView
        lateinit var client: android.webkit.WebViewClient
        var denied = false
        instrumentation.runOnMainSync {
            webView = HTML5WebView(context.applicationContext)
            client = webView.getWebViewClientForTest()
            val chrome = webView.getWebChromeClientForTest()
            chrome.onGeolocationPermissionsShowPrompt("https://fixture.invalid", object : GeolocationPermissions.Callback {
                override fun invoke(origin: String?, allow: Boolean, retain: Boolean) {
                    denied = !allow && !retain
                }
            })
            client.onPageStarted(webView, "file:///android_asset/video-fixtures/loading.html", null)
            assertTrue(webView.isLoading)
            client.onReceivedError(webView, -2, "fixture failure", "file:///android_asset/video-fixtures/loading.html")
            assertFalse(webView.isLoading)
            // A late callback cannot recreate a settled progress dialog/loading state.
            client.onPageFinished(webView, "file:///android_asset/video-fixtures/loading.html")
            webView.release()
        }
        assertTrue(denied)
        assertTrue(webView.isReleased)
    }

    @Test
    fun sslCallbacksCancelAlwaysButOnlyMatchingActiveErrorsSettle() {
        lateinit var webView: HTML5WebView
        lateinit var staleHandler: TestSslErrorHandler
        lateinit var matchingHandler: TestSslErrorHandler
        instrumentation.runOnMainSync {
            webView = HTML5WebView(context.applicationContext)
            val client = webView.getWebViewClientForTest()
            val activeUrl = "https://fixture.invalid/ssl"
            client.onPageStarted(webView, activeUrl, null)

            staleHandler = TestSslErrorHandler()
            client.onReceivedSslError(webView, staleHandler, null)
            assertTrue(webView.isLoading)

            val otherHandler = TestSslErrorHandler()
            webView.handleSslErrorForTest(otherHandler, "https://other.invalid/ssl")
            assertTrue(webView.isLoading)

            matchingHandler = TestSslErrorHandler()
            webView.handleSslErrorForTest(matchingHandler, activeUrl)
            assertFalse(webView.isLoading)
            webView.release()
        }
        assertTrue(staleHandler.cancelled)
        assertTrue(matchingHandler.cancelled)
    }

    @Test
    fun cancellingProgressDialogStopsAndSettlesTheActiveLoad() {
        val intent = Intent(context, VideoSearch::class.java)
            .putExtra(DisplayLyricActivity.SEARCH_TARGET, context.getString(R.string.youtube))
            .putExtra(MainActivity.SONG_NAME, "一首歌")
        ActivityScenario.launch<VideoSearch>(intent).use { scenario ->
            scenario.onActivity { activity ->
                val webView = checkNotNull(activity.webViewForTest())
                val client = webView.getWebViewClientForTest()
                client.onPageStarted(webView, "https://fixture.invalid/loading", null)
                assertTrue(webView.isLoading)
                // ProgressDialog.cancel() dispatches its OnCancelListener asynchronously on
                // some API levels. Cancel in this callback, then observe the settled state from
                // a later callback after the main queue has drained.
                webView.cancelProgressDialogForTest()
            }
            instrumentation.waitForIdleSync()
            scenario.onActivity { activity ->
                assertFalse(checkNotNull(activity.webViewForTest()).isLoading)
            }
        }
    }

    @Test
    fun emptyRestoredHistoryRequiresFreshSearchUrl() {
        assertFalse(VideoSearch.hasUsableRestoredState(null))
        assertFalse(VideoSearch.hasUsableRestoredEntryCount(0))
        assertTrue(VideoSearch.hasUsableRestoredEntryCount(1))
    }
}
