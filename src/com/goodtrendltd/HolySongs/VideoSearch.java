package com.goodtrendltd.HolySongs;

import android.app.Activity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebBackForwardList;

import android.window.OnBackInvokedCallback;
import android.window.OnBackInvokedDispatcher;

/** Retained Java video-search screen. */
public class VideoSearch extends Activity {
    private String target;
    private String songName;
    private HTML5WebView webView;
    private boolean released;
    private OnBackInvokedCallback modernBackCallback;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Bundle extras = intent == null ? null : intent.getExtras();
        target = validStringExtra(extras, DisplayLyricActivity.SEARCH_TARGET);
        songName = validStringExtra(extras, MainActivity.SONG_NAME);
        if (!isValidTarget(target) || songName == null || songName.length() == 0) {
            finish();
            return;
        }

        webView = new HTML5WebView(this);
        setContentView(webView.getLayout());

        if (savedInstanceState != null && !webView.isReleased()) {
            WebBackForwardList restored = webView.restoreState(savedInstanceState);
            if (!hasUsableRestoredState(restored)) {
                webView.loadUrl(getSearchUrl());
            }
        } else if (!released) {
            webView.loadUrl(getSearchUrl());
        }
        registerModernBackCallback();
    }

    private void registerModernBackCallback() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }
        modernBackCallback = () -> handleBack();
        getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT, modernBackCallback);
    }

    private boolean handleBack() {
        if (webView != null && !released) {
            if (webView.isLoading()) {
                webView.cancelLoading();
                return true;
            }
            if (webView.inCustomView()) {
                webView.hideCustomView();
                return true;
            }
            if (webView.canGoBack()) {
                webView.goBack();
                return true;
            }
        }
        finish();
        return true;
    }

    private static String validStringExtra(Bundle extras, String key) {
        if (extras == null || key == null || !extras.containsKey(key)) {
            return null;
        }
        Object value = extras.get(key);
        return value instanceof String ? (String) value : null;
    }

    private boolean isValidTarget(String value) {
        return isSupportedTarget(this, value);
    }

    private String getSearchUrl() {
        return searchUrlFor(this, target, songName);
    }

    static boolean isSupportedTarget(android.content.Context context, String value) {
        return context.getString(R.string.youtube).equals(value)
                || context.getString(R.string.youku).equals(value)
                || context.getString(R.string.tudou).equals(value);
    }

    static boolean hasUsableRestoredState(WebBackForwardList restored) {
        return restored != null && hasUsableRestoredEntryCount(restored.getSize());
    }

    static boolean hasUsableRestoredEntryCount(int entryCount) {
        return entryCount > 0;
    }

    static String searchUrlFor(android.content.Context context, String target, String title) {
        if (title == null || title.length() == 0 || !isSupportedTarget(context, target)) {
            return null;
        }
        String encodedTitle = Uri.encode(title);
        if (context.getString(R.string.youtube).equals(target)) {
            return context.getString(R.string.youtube_url) + encodedTitle;
        }
        if (context.getString(R.string.youku).equals(target)) {
            return context.getString(R.string.youku_url) + encodedTitle;
        }
        return context.getString(R.string.tudou_url) + encodedTitle;
    }

    // Package-private seams keep deterministic instrumentation assertions out of the public API.
    String validatedTargetForTest() {
        return target;
    }

    String searchUrlForTest() {
        return getSearchUrl();
    }

    boolean hasLiveWebViewForTest() {
        return webView != null && !webView.isReleased();
    }

    HTML5WebView webViewForTest() {
        return webView;
    }

    /** API23-and-older fallback; API33+ uses OnBackInvokedDispatcher above. */
    @SuppressLint("GestureBackNavigation")
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return handleBack();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onPause() {
        if (webView != null && !released) {
            webView.setHostActive(false);
            webView.pauseForHost();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (webView != null && !released) {
            webView.setHostActive(true);
            webView.resumeForHost();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (webView != null && !released) {
            webView.saveState(outState);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStop() {
        if (webView != null && !released) {
            webView.setHostActive(false);
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && modernBackCallback != null) {
            getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback(modernBackCallback);
            modernBackCallback = null;
        }
        if (!released) {
            released = true;
            if (webView != null) {
                webView.release();
                webView = null;
            }
        }
        super.onDestroy();
    }
}
