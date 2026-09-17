package com.goodtrendltd.HolySongs

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.net.http.SslError
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.webkit.GeolocationPermissions
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView

class HTML5WebView : WebView {
    private var mContext: Context? = null
    private var mWebChromeClient: MyWebChromeClient? = null
    private var mWebViewClient: MyWebViewClient? = null
    private var mCustomView: View? = null
    private var mCustomViewContainer: FrameLayout? = null
    private var mCustomViewCallback: WebChromeClient.CustomViewCallback? = null

    private var mContentView: FrameLayout? = null
    private var mBrowserFrameLayout: FrameLayout? = null
    private var mLayout: FrameLayout? = null

    private var pd: ProgressDialog? = null
    private var loadingUrl: String? = null
    private var loading = false
    private var hostActive = true
    private var released = false
    private var destroyed = false
    private var handlingProgressCancel = false
    private var ignoreNextPageStart = false

    private fun init(context: Context) {
        mContext = context
        mLayout = FrameLayout(context)

        mBrowserFrameLayout = FrameLayout(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
        }
        mContentView = FrameLayout(context).apply {
            id = R.id.main_content
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
        }
        mCustomViewContainer = FrameLayout(context).apply {
            id = R.id.fullscreen_custom_content
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            visibility = View.GONE
        }

        val outer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
        }
        val errorConsole = LinearLayout(context).apply {
            id = R.id.error_console
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
        }
        outer.addView(errorConsole)
        outer.addView(mContentView, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        ))

        mBrowserFrameLayout!!.addView(mCustomViewContainer, COVER_SCREEN_PARAMS)
        mBrowserFrameLayout!!.addView(outer, COVER_SCREEN_PARAMS)
        mLayout!!.addView(mBrowserFrameLayout, COVER_SCREEN_PARAMS)

        mWebChromeClient = MyWebChromeClient()
        webChromeClient = mWebChromeClient!!

        mWebViewClient = MyWebViewClient()
        webViewClient = mWebViewClient!!

        val settings: WebSettings = settings
        settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NARROW_COLUMNS
        settings.saveFormData = true
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true

        mContentView!!.addView(this)
    }

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        init(context)
    }

    fun getLayout(): FrameLayout = mLayout ?: FrameLayout(context)

    fun inCustomView(): Boolean = mCustomView != null

    val isLoading: Boolean
        get() = loading

    internal val isReleased: Boolean
        get() = released

    internal fun getWebViewClientForTest(): WebViewClient = mWebViewClient!!

    internal fun getWebChromeClientForTest(): WebChromeClient = mWebChromeClient!!

    fun setHostActive(active: Boolean) {
        if (released) return
        hostActive = active
        if (!active) {
            dismissProgress()
        }
    }

    fun cancelLoading() {
        if (released) return
        ignoreNextPageStart = true
        stopLoading()
        settleLoading()
    }

    internal fun cancelProgressDialogForTest() {
        if (pd != null) {
            pd!!.cancel()
        } else {
            cancelLoading()
        }
    }

    private fun cancelLoadingFromProgressDialog() {
        if (released || handlingProgressCancel) return
        handlingProgressCancel = true
        try {
            cancelLoading()
        } finally {
            handlingProgressCancel = false
        }
    }

    fun pauseForHost() {
        if (!released) {
            onPause()
        }
    }

    fun resumeForHost() {
        if (!released) {
            onResume()
        }
    }

    fun hideCustomView() {
        if (!released) {
            mWebChromeClient?.onHideCustomView()
        }
    }

    internal fun release() {
        if (released) return
        released = true
        hostActive = false
        settleLoading()
        hideCustomViewForRelease()
        if (mContentView != null) {
            mContentView!!.removeView(this)
        }
        if (parent is ViewGroup) {
            (parent as ViewGroup).removeView(this)
        }
        if (!destroyed) {
            destroyed = true
            super.destroy()
        }
        mContext = null
        mContentView = null
        mBrowserFrameLayout = null
        mCustomViewContainer = null
        mLayout = null
    }

    private fun hideCustomViewForRelease() {
        val view = mCustomView ?: return
        view.visibility = View.GONE
        if (mCustomViewContainer != null) {
            mCustomViewContainer!!.removeView(view)
            mCustomViewContainer!!.visibility = View.GONE
        }
        val callback = mCustomViewCallback
        mCustomView = null
        mCustomViewCallback = null
        callback?.onCustomViewHidden()
    }

    private fun settleLoading() {
        loading = false
        loadingUrl = null
        dismissProgress()
    }

    private fun dismissProgress() {
        val dialog = pd
        pd = null
        if (dialog != null && dialog.isShowing) {
            try {
                dialog.dismiss()
            } catch (_: RuntimeException) {
                // The host window may already have gone away.
            }
        }
    }

    private fun isLiveForUi(): Boolean {
        val context = mContext as? Activity ?: return false
        return !released && hostActive && !context.isFinishing
    }

    internal fun handleSslErrorForTest(handler: SslErrorHandler?, errorUrl: String?) {
        mWebViewClient?.handleSslError(handler, errorUrl)
    }

    private inner class MyWebChromeClient : WebChromeClient() {
        private var mDefaultVideoPoster: Bitmap? = null
        private var mVideoProgressView: View? = null

        override fun onShowCustomView(view: View, callback: WebChromeClient.CustomViewCallback) {
            if (released || !hostActive || view == null) {
                callback?.onCustomViewHidden()
                return
            }
            this@HTML5WebView.visibility = View.GONE
            if (mCustomView != null) {
                callback?.onCustomViewHidden()
                return
            }
            mCustomViewContainer!!.addView(view)
            mCustomView = view
            mCustomViewCallback = callback
            mCustomViewContainer!!.visibility = View.VISIBLE
        }

        override fun onHideCustomView() {
            val view = mCustomView ?: return
            view.visibility = View.GONE
            if (mCustomViewContainer != null) {
                mCustomViewContainer!!.removeView(view)
                mCustomViewContainer!!.visibility = View.GONE
            }
            val callback = mCustomViewCallback
            mCustomView = null
            mCustomViewCallback = null
            callback?.onCustomViewHidden()
            if (!released) {
                this@HTML5WebView.visibility = View.VISIBLE
            }
        }

        override fun getDefaultVideoPoster(): Bitmap? {
            if (mDefaultVideoPoster == null && !released) {
                mDefaultVideoPoster = BitmapFactory.decodeResource(resources, R.drawable.default_video_poster)
            }
            return mDefaultVideoPoster
        }

        override fun getVideoLoadingProgressView(): View? {
            if (mVideoProgressView == null && !released) {
                val context = mContext ?: return null
                mVideoProgressView = LinearLayout(context).apply {
                    id = R.id.progress_indicator
                    orientation = LinearLayout.VERTICAL
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    )
                    gravity = android.view.Gravity.CENTER
                    addView(ProgressBar(context, null, android.R.attr.progressBarStyleLarge).apply {
                        isIndeterminate = true
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                        ).apply {
                            gravity = android.view.Gravity.CENTER
                        }
                    })
                    addView(TextView(context).apply {
                        text = context.getString(R.string.loading_video)
                        textSize = 14f
                        setTextColor(android.graphics.Color.DKGRAY)
                        setPadding(0, 5, 0, 0)
                        gravity = android.view.Gravity.CENTER
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                        ).apply {
                            gravity = android.view.Gravity.CENTER
                        }
                    })
                }
            }
            return mVideoProgressView
        }

        override fun onReceivedTitle(view: WebView, title: String) {
            if (isLiveForUi()) {
                (mContext as Activity).title = title
            }
        }

        override fun onProgressChanged(view: WebView, newProgress: Int) {
            if (!released && loading && newProgress >= 100) {
                settleLoading()
            }
            if (isLiveForUi()) {
                (mContext as Activity).window.setFeatureInt(Window.FEATURE_PROGRESS, newProgress * 100)
            }
        }

        override fun onGeolocationPermissionsShowPrompt(origin: String, callback: GeolocationPermissions.Callback) {
            callback.invoke(origin, false, false)
        }
    }

    private inner class MyWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            if (!isHttpOrHttpsUrl(url)) {
                return true
            }
            if (!released) {
                Log.i(LOGTAG, "shouldOverrideUrlLoading: $url")
            }
            return false
        }

        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            val url = request.url?.toString()
            if (!isHttpOrHttpsUrl(url)) {
                return true
            }
            if (!released) {
                Log.i(LOGTAG, "shouldOverrideUrlLoading: $url")
            }
            return false
        }

        override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
            if (released) {
                return
            }
            if (ignoreNextPageStart) {
                ignoreNextPageStart = false
                return
            }
            loading = true
            loadingUrl = url
            if (isLiveForUi()) {
                dismissProgress()
                try {
                    val context = mContext ?: return
                    pd = ProgressDialog.show(context, "", context.getString(R.string.loading_video), true)
                    pd?.setCancelable(true)
                    pd?.setOnCancelListener { cancelLoadingFromProgressDialog() }
                } catch (_: RuntimeException) {
                    pd = null
                }
            }
        }

        override fun onPageFinished(view: WebView, url: String?) {
            if (!released && loading && sameLoadingUrl(url)) {
                settleLoading()
            }
        }

        override fun onReceivedError(view: WebView, errorCode: Int, description: String?, failingUrl: String?) {
            if (!released && loading && sameLoadingUrl(failingUrl)) {
                settleLoading()
            }
        }

        override fun onReceivedError(view: WebView, request: WebResourceRequest?, error: WebResourceError) {
            if (!released && request != null && request.isForMainFrame && loading && sameLoadingUrl(request.url.toString())) {
                settleLoading()
            }
        }

        override fun onReceivedHttpError(
            view: WebView,
            request: WebResourceRequest?,
            errorResponse: WebResourceResponse,
        ) {
            if (!released && request != null && request.isForMainFrame && loading && sameLoadingUrl(request.url.toString())) {
                settleLoading()
            }
        }

        override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError?) {
            handleSslError(handler, error?.url)
        }

        internal fun handleSslError(handler: SslErrorHandler?, errorUrl: String?) {
            handler?.cancel()
            if (!released && loading && errorUrl != null && sameLoadingUrl(errorUrl)) {
                settleLoading()
            }
        }

        private fun sameLoadingUrl(url: String?): Boolean {
            return loadingUrl != null && loadingUrl == url
        }
    }

    companion object {
        const val LOGTAG: String = "HTML5WebView"

        @JvmField
        val COVER_SCREEN_PARAMS: FrameLayout.LayoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        )

        @JvmStatic
        fun isHttpOrHttpsUrl(url: String?): Boolean {
            if (url == null) {
                return false
            }
            return try {
                val scheme = Uri.parse(url).scheme
                "http".equals(scheme, ignoreCase = true) || "https".equals(scheme, ignoreCase = true)
            } catch (_: RuntimeException) {
                false
            }
        }
    }
}
