package com.goodtrendltd.HolySongs;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created with IntelliJ IDEA.
 * User: LeOn
 * Date: 13-11-3
 * Time: 上午8:40
 */
public class VideoSearch extends Activity {
    private String target;
    private String songName;
    private WebView webView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.video_webview);

        Intent intent = getIntent();
        target = intent.getStringExtra(DisplayLyricActivity.SEARCH_TARGET);
        songName = intent.getStringExtra(MainActivity.SONG_NAME);

        webView = (WebView) findViewById(R.id.searchWebView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient(){
             @Override
             public boolean shouldOverrideUrlLoading(WebView webview, String url)
             {
                 webview.setWebChromeClient(new WebChromeClient());
                 return false;
             }
        });
        webView.loadUrl(getSearchUrl());
    }

    private String getSearchUrl() {
        String url = "";
        if (getString(R.string.youtube).equals(target)) {
            url = getString(R.string.youtube_url) + songName;
        } else if (getString(R.string.youku).equals(target)) {
            url = getString(R.string.youku_url) + songName;
        } else if (getString(R.string.tudou).equals(target)) {
            url = getString(R.string.tudou_url) + songName;
        }
        return url;
    }

    //override this to make sure video stops playing after use hit back button.
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack())
        {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);

    }

    @Override
    public void onPause() {
        webView.setVisibility(View.GONE);
        webView.destroy();
        super.onPause();
    }

}