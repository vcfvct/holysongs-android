package com.goodtrendltd.HolySongs;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.util.Linkify;
import android.widget.TextView;

/**
 * Created with IntelliJ IDEA.
 * User: LeOn
 * Date: 13-10-6
 * Time: 下午10:33
 */
public class AboutActivity extends Activity {
    private SharedPreferences sharedPreferences;

    private String ABOUT_CONTENT = "\n我们是位于马里兰州Germantown的德国镇基督教会，欢迎大家光临。http://www.cccgermantown.org/ " +
            "\n\n 本app为方便团契或其他聚会时大家敬拜之用，至少可以省去打印的麻烦:-) " +
            "\n\n任何意见，请反馈至: vcfvct@gmail.com \n感谢Katie的鼓励和添加歌曲^_^。";
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences(getString(R.string.app_pref), MODE_PRIVATE);
        if (sharedPreferences.getBoolean(getString(R.string.night_mode_pref_key), true)) {
            setTheme(android.R.style.Theme_Holo);
        } else {
            setTheme(android.R.style.Theme_Holo_Light);
        }
        TextView textView = new TextView(this);
        textView.setLineSpacing(10, 1);
        textView.setAutoLinkMask(Linkify.ALL);
        textView.setText(ABOUT_CONTENT);
        setContentView(textView);
    }
}