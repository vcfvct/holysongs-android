package com.goodtrendltd.HolySongs

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.webkit.WebBackForwardList
import android.window.OnBackInvokedCallback
import android.window.OnBackInvokedDispatcher

class VideoSearch : Activity() {
    private var target: String? = null
    private var songName: String? = null
    private var webView: HTML5WebView? = null
    private var released = false
    private var modernBackCallback: OnBackInvokedCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = intent
        val extras = intent?.extras
        target = validStringExtra(extras, DisplayLyricActivity.SEARCH_TARGET)
        songName = validStringExtra(extras, MainActivity.SONG_NAME)
        if (!isValidTarget(target) || songName == null || songName!!.isEmpty()) {
            finish()
            return
        }

        webView = HTML5WebView(this)
        setContentView(webView!!.getLayout())

        if (savedInstanceState != null && !webView!!.isReleased) {
            val restored = webView!!.restoreState(savedInstanceState)
            if (!hasUsableRestoredState(restored)) {
                webView!!.loadUrl(getSearchUrl())
            }
        } else if (!released) {
            webView!!.loadUrl(getSearchUrl())
        }
        registerModernBackCallback()
    }

    private fun registerModernBackCallback() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return
        }
        val callback = OnBackInvokedCallback { handleBack() }
        modernBackCallback = callback
        onBackInvokedDispatcher.registerOnBackInvokedCallback(
            OnBackInvokedDispatcher.PRIORITY_DEFAULT,
            callback,
        )
    }

    private fun handleBack(): Boolean {
        if (webView != null && !released) {
            if (webView!!.isLoading) {
                webView!!.cancelLoading()
                return true
            }
            if (webView!!.inCustomView()) {
                webView!!.hideCustomView()
                return true
            }
            if (webView!!.canGoBack()) {
                webView!!.goBack()
                return true
            }
        }
        finish()
        return true
    }

    private fun isValidTarget(value: String?): Boolean = isSupportedTarget(this, value)

    private fun getSearchUrl(): String = searchUrlFor(this, target, songName)
        ?: error("Missing usable search URL for target=$target and title=$songName")

    @SuppressLint("GestureBackNavigation")
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return handleBack()
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onPause() {
        if (webView != null && !released) {
            webView!!.setHostActive(false)
            webView!!.pauseForHost()
        }
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        if (webView != null && !released) {
            webView!!.setHostActive(true)
            webView!!.resumeForHost()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (webView != null && !released) {
            webView!!.saveState(outState)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onStop() {
        if (webView != null && !released) {
            webView!!.setHostActive(false)
        }
        super.onStop()
    }

    override fun onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val callback = modernBackCallback
            if (callback != null) {
                onBackInvokedDispatcher.unregisterOnBackInvokedCallback(callback)
                modernBackCallback = null
            }
        }
        if (!released) {
            released = true
            if (webView != null) {
                webView!!.release()
                webView = null
            }
        }
        super.onDestroy()
    }

    internal fun validatedTargetForTest(): String? = target

    internal fun searchUrlForTest(): String? = getSearchUrl()

    internal fun hasLiveWebViewForTest(): Boolean = webView != null && !webView!!.isReleased

    internal fun webViewForTest(): HTML5WebView? = webView

    companion object {
        private fun validStringExtra(extras: Bundle?, key: String?): String? {
            if (extras == null || key == null || !extras.containsKey(key)) {
                return null
            }
            val value = extras[key]
            return value as? String
        }

        @JvmStatic
        fun isSupportedTarget(context: Context, value: String?): Boolean {
            return context.getString(R.string.youtube) == value ||
                context.getString(R.string.bilibili) == value
        }

        @JvmStatic
        fun hasUsableRestoredState(restored: WebBackForwardList?): Boolean {
            return restored != null && hasUsableRestoredEntryCount(restored.size)
        }

        @JvmStatic
        fun hasUsableRestoredEntryCount(entryCount: Int): Boolean {
            return entryCount > 0
        }

        @JvmStatic
        fun searchUrlFor(context: Context, target: String?, title: String?): String? {
            if (title == null || title.isEmpty() || !isSupportedTarget(context, target)) {
                return null
            }
            val encodedTitle = Uri.encode(title)
            return when (target) {
                context.getString(R.string.youtube) -> context.getString(R.string.youtube_url) + encodedTitle
                context.getString(R.string.bilibili) -> context.getString(R.string.bilibili_url) + encodedTitle
                else -> null
            }
        }
    }
}
