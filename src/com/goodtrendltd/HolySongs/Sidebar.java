package com.goodtrendltd.HolySongs;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.SectionIndexer;

/**
 * Created with IntelliJ IDEA.
 * User: LeOn
 * Date: 13-10-5
 * Time: 下午11:12
 */
public class Sidebar extends View {
    public static String[] ALPHABET_ARRAY = new String[] { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S",
            "T", "U", "V", "W", "X", "Y", "Z" };

    private SectionIndexer sectionIndexter;
    private ListView list;
    private int itemHeight = 22;

    public Sidebar(Context context) {
        super(context);
        init();
    }

    public Sidebar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Sidebar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setBackgroundColor(0x44FFFFFF);
        //get height by different screen resolution
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;
        itemHeight = height/36;
    }

    public void bindListView(ListView list) {
        this.list = list;
        sectionIndexter = (SectionIndexer) list.getAdapter();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        int i = (int) event.getY();
        int idx = i / itemHeight;
        if (idx >= ALPHABET_ARRAY.length) {
            idx = ALPHABET_ARRAY.length - 1;
        } else if (idx < 0) {
            idx = 0;
        }
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                setBackgroundColor(0x8844FFFF);
                if (sectionIndexter == null) {
                    sectionIndexter = (SectionIndexer) list.getAdapter();
                }
                int position = sectionIndexter.getPositionForSection(idx);
                if (position == -1) {
                    return true;
                }
                list.setSelection(position);
                break;
            case MotionEvent.ACTION_UP:
                setBackgroundColor(0x44FFFFFF);
                break;
        }
        return true;
    }

    protected void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(0xFF111111);
        paint.setTextSize(25);
        paint.setTextAlign(Paint.Align.CENTER);
        float widthCenter = getMeasuredWidth() / 2;
        for (int i = 0, length = ALPHABET_ARRAY.length; i < length; i++) {
            canvas.drawText(ALPHABET_ARRAY[i], widthCenter, itemHeight + (i * itemHeight), paint);
        }
        super.onDraw(canvas);
    }
}
