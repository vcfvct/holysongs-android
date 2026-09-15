package com.goodtrendltd.HolySongs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ShareActionProvider;
import android.widget.TextView;

/**
 * Created with IntelliJ IDEA.
 * User: LeOn
 * Date: 13-10-5
 * Time: 下午10:13
 */
public class DisplayLyricActivity extends Activity {
    public static String SEARCH_TARGET = "com.goodtrendltd.searchTarget";

    private ShareActionProvider mShareActionProvider;
    private String songName;
    private String lyric;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.app_pref), MODE_PRIVATE);
        if (sharedPreferences.getBoolean(getString(R.string.night_mode_pref_key), true)) {
            setTheme(R.style.MyHoloTheme);
        } else {
            setTheme(R.style.MyHoloLightTheme);
        }
        setContentView(R.layout.lyric_view);

        Intent intent = getIntent();
        lyric = intent.getStringExtra(MainActivity.LYRIC);
        songName = intent.getStringExtra(MainActivity.SONG_NAME);

        // Create the text view
        TextView lyricTextView = (TextView) findViewById(R.id.lyricContent);
        lyricTextView.setMovementMethod(new ScrollingMovementMethod());
        lyricTextView.setTextSize(sharedPreferences.getInt(getString(R.string.font_size_pref_key), 20));
        lyricTextView.setText(lyric);
        lyricTextView.setLineSpacing(20, 1.0f);

        TextView titleTextView = (TextView) findViewById(R.id.title);
        titleTextView.setText(songName);
        titleTextView.setTextSize(25);
        titleTextView.setTextColor(Color.parseColor("#00BFFF"));

    }

    //begin of menu related
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.lyricview_actions, menu);
        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);
        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) item.getActionProvider();

        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, songName);
        i.putExtra(Intent.EXTRA_TEXT, lyric);

        mShareActionProvider.setShareIntent(i);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.youtube) {
            openSearch(getString(R.string.youtube));
            return true;
        }
        if (itemId == R.id.youku) {
            openSearch(getString(R.string.youku));
            return true;
        }
        if (itemId == R.id.tudou) {
            openSearch(getString(R.string.tudou));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openSearch(final String target) {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo.State wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
        if (wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING) {
            navigateToSearch(target);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setIcon(android.R.drawable.ic_dialog_alert);
            builder.setTitle(R.string.alert_title);
            builder.setMessage(R.string.alert_message);
            builder.setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // 如果没有网络连接，则进入网络设置界面
                    navigateToSearch(target);
                }
            });
            builder.setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.create();
            builder.show();
        }
    }

    private void navigateToSearch(String target){
        Intent intent = new Intent(this, VideoSearch.class);
        intent.putExtra(SEARCH_TARGET, target);
        intent.putExtra(MainActivity.SONG_NAME, songName);
        startActivity(intent);
    }

    // Call to update the share intent
    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }
}