package com.goodtrendltd.HolySongs;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.GeolocationPermissions;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

/** WebView host used by the retained Java video-search screen. */
public class HTML5WebView extends WebView {

    private Context mContext;
    private MyWebChromeClient mWebChromeClient;
    private MyWebViewClient mWebViewClient;
    private View mCustomView;
    private FrameLayout mCustomViewContainer;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;

    private FrameLayout mContentView;
    private FrameLayout mBrowserFrameLayout;
    private FrameLayout mLayout;

    private ProgressDialog pd;
    private String loadingUrl;
    private boolean loading;
    private boolean hostActive = true;
    private boolean released;
    private boolean destroyed;
    private boolean handlingProgressCancel;

    static final String LOGTAG = "HTML5WebView";

    private void init(Context context) {
        mContext = context;
        mLayout = new FrameLayout(context);

        mBrowserFrameLayout = (FrameLayout) LayoutInflater.from(context).inflate(R.layout.video_html5_screen, null);
        mContentView = (FrameLayout) mBrowserFrameLayout.findViewById(R.id.main_content);
        mCustomViewContainer = (FrameLayout) mBrowserFrameLayout.findViewById(R.id.fullscreen_custom_content);

        mLayout.addView(mBrowserFrameLayout, COVER_SCREEN_PARAMS);

        mWebChromeClient = new MyWebChromeClient();
        setWebChromeClient(mWebChromeClient);

        mWebViewClient = new MyWebViewClient();
        setWebViewClient(mWebViewClient);

        WebSettings s = getSettings();
        s.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        // Password saving is not used by video search and is deprecated on current WebView.
        s.setSaveFormData(true);
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);

        mContentView.addView(this);
    }

    public HTML5WebView(Context context) {
        super(context);
        init(context);
    }

    public HTML5WebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public HTML5WebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public FrameLayout getLayout() {
        return mLayout;
    }

    public boolean inCustomView() {
        return mCustomView != null;
    }

    public boolean isLoading() {
        return loading;
    }

    boolean isReleased() {
        return released;
    }

    WebViewClient getWebViewClientForTest() {
        return mWebViewClient;
    }

    WebChromeClient getWebChromeClientForTest() {
        return mWebChromeClient;
    }

    void setHostActive(boolean active) {
        if (released) {
            return;
        }
        hostActive = active;
        if (!active) {
            dismissProgress();
        }
    }

    void cancelLoading() {
        if (released) {
            return;
        }
        stopLoading();
        settleLoading();
    }

    // Test-only seam exercises the same ProgressDialog cancellation callback used by users.
    void cancelProgressDialogForTest() {
        if (pd != null) {
            pd.cancel();
        } else {
            cancelLoading();
        }
    }

    private void cancelLoadingFromProgressDialog() {
        if (released || handlingProgressCancel) {
            return;
        }
        handlingProgressCancel = true;
        try {
            cancelLoading();
        } finally {
            handlingProgressCancel = false;
        }
    }

    void pauseForHost() {
        if (!released) {
            onPause();
        }
    }

    void resumeForHost() {
        if (!released) {
            onResume();
        }
    }

    public void hideCustomView() {
        if (!released) {
            mWebChromeClient.onHideCustomView();
        }
    }

    /** Idempotent final release; temporary Activity pauses must not call this. */
    void release() {
        if (released) {
            return;
        }
        released = true;
        hostActive = false;
        settleLoading();
        hideCustomViewForRelease();
        if (mContentView != null) {
            mContentView.removeView(this);
        }
        if (getParent() instanceof ViewGroup) {
            ((ViewGroup) getParent()).removeView(this);
        }
        if (!destroyed) {
            destroyed = true;
            super.destroy();
        }
        mContext = null;
        mContentView = null;
        mBrowserFrameLayout = null;
        mCustomViewContainer = null;
        mLayout = null;
    }

    private void hideCustomViewForRelease() {
        if (mCustomView == null) {
            return;
        }
        mCustomView.setVisibility(View.GONE);
        if (mCustomViewContainer != null) {
            mCustomViewContainer.removeView(mCustomView);
            mCustomViewContainer.setVisibility(View.GONE);
        }
        WebChromeClient.CustomViewCallback callback = mCustomViewCallback;
        mCustomView = null;
        mCustomViewCallback = null;
        if (callback != null) {
            callback.onCustomViewHidden();
        }
    }

    private void settleLoading() {
        loading = false;
        loadingUrl = null;
        dismissProgress();
    }

    private void dismissProgress() {
        ProgressDialog dialog = pd;
        pd = null;
        if (dialog != null && dialog.isShowing()) {
            try {
                dialog.dismiss();
            } catch (RuntimeException ignored) {
                // The host window may already have gone away.
            }
        }
    }

    private boolean isLiveForUi() {
        return !released && hostActive && mContext instanceof Activity && !((Activity) mContext).isFinishing();
    }

    private class MyWebChromeClient extends WebChromeClient {
        private Bitmap mDefaultVideoPoster;
        private View mVideoProgressView;

        @Override
        public void onShowCustomView(View view, WebChromeClient.CustomViewCallback callback) {
            if (released || !hostActive || view == null) {
                if (callback != null) {
                    callback.onCustomViewHidden();
                }
                return;
            }
            HTML5WebView.this.setVisibility(View.GONE);
            if (mCustomView != null) {
                if (callback != null) {
                    callback.onCustomViewHidden();
                }
                return;
            }
            mCustomViewContainer.addView(view);
            mCustomView = view;
            mCustomViewCallback = callback;
            mCustomViewContainer.setVisibility(View.VISIBLE);
        }

        @Override
        public void onHideCustomView() {
            if (mCustomView == null) {
                return;
            }
            mCustomView.setVisibility(View.GONE);
            if (mCustomViewContainer != null) {
                mCustomViewContainer.removeView(mCustomView);
                mCustomViewContainer.setVisibility(View.GONE);
            }
            WebChromeClient.CustomViewCallback callback = mCustomViewCallback;
            mCustomView = null;
            mCustomViewCallback = null;
            if (callback != null) {
                callback.onCustomViewHidden();
            }
            if (!released) {
                HTML5WebView.this.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public Bitmap getDefaultVideoPoster() {
            if (mDefaultVideoPoster == null && !released) {
                mDefaultVideoPoster = BitmapFactory.decodeResource(getResources(), R.drawable.default_video_poster);
            }
            return mDefaultVideoPoster;
        }

        @Override
        public View getVideoLoadingProgressView() {
            if (mVideoProgressView == null && !released) {
                mVideoProgressView = LayoutInflater.from(mContext).inflate(R.layout.video_loading_progress, null);
            }
            return mVideoProgressView;
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            if (isLiveForUi()) {
                ((Activity) mContext).setTitle(title);
            }
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (isLiveForUi()) {
                ((Activity) mContext).getWindow().setFeatureInt(Window.FEATURE_PROGRESS, newProgress * 100);
            }
        }

        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            if (callback != null) {
                callback.invoke(origin, false, false);
            }
        }
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (!isHttpOrHttpsUrl(url)) {
                // Consume custom/file/content/about schemes: never hand them to an external
                // activity or an implicit browser. HTTP(S) remains in this WebView.
                return true;
            }
            if (!released) {
                Log.i(LOGTAG, "shouldOverrideUrlLoading: " + url);
            }
            return false;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            String url = request == null || request.getUrl() == null
                    ? null : request.getUrl().toString();
            if (!isHttpOrHttpsUrl(url)) {
                return true;
            }
            if (!released) {
                Log.i(LOGTAG, "shouldOverrideUrlLoading: " + url);
            }
            return false;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            if (released) {
                return;
            }
            loading = true;
            loadingUrl = url;
            if (isLiveForUi()) {
                dismissProgress();
                try {
                    pd = ProgressDialog.show(mContext, "", mContext.getString(R.string.loading_video), true);
                    pd.setCancelable(true);
                    pd.setOnCancelListener(dialog -> cancelLoadingFromProgressDialog());
                } catch (RuntimeException ignored) {
                    pd = null;
                }
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (released || !loading || !sameLoadingUrl(url)) {
                return;
            }
            settleLoading();
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            if (!released && loading && sameLoadingUrl(failingUrl)) {
                settleLoading();
            }
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            if (!released && request != null && request.isForMainFrame() && loading
                    && sameLoadingUrl(request.getUrl().toString())) {
                settleLoading();
            }
        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, android.webkit.WebResourceResponse errorResponse) {
            if (!released && request != null && request.isForMainFrame() && loading
                    && sameLoadingUrl(request.getUrl().toString())) {
                settleLoading();
            }
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handleSslError(handler, error == null ? null : error.getUrl());
        }

        private void handleSslError(SslErrorHandler handler, String errorUrl) {
            if (handler != null) {
                handler.cancel();
            }
            // Always reject the certificate. Only an error for the active navigation may
            // settle loading; stale or malformed callbacks must not settle a newer load.
            if (!released && loading && errorUrl != null && sameLoadingUrl(errorUrl)) {
                settleLoading();
            }
        }

        private boolean sameLoadingUrl(String url) {
            return loadingUrl != null && loadingUrl.equals(url);
        }
    }

    /** Returns whether a main-frame URL is safe to keep inside this WebView. */
    static boolean isHttpOrHttpsUrl(String url) {
        if (url == null) {
            return false;
        }
        try {
            String scheme = Uri.parse(url).getScheme();
            return "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    /*
     * WebView does not attach a navigation generation to page-finished/error callbacks. A
     * same-URL callback from an older navigation is therefore indistinguishable from the live
     * navigation; URL matching is the strongest reliable guard available on API23+.
     */

    // Package-private callback seam for deterministic tests; production callbacks use the same path.
    void handleSslErrorForTest(SslErrorHandler handler, String errorUrl) {
        if (mWebViewClient != null) {
            mWebViewClient.handleSslError(handler, errorUrl);
        }
    }

    static final FrameLayout.LayoutParams COVER_SCREEN_PARAMS =
            new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
}
