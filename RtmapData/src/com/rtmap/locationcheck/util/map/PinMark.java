package com.rtmap.locationcheck.util.map;

import java.io.InputStream;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.view.View;

public class PinMark implements Marker {
    private int x;
    private int y;
    private boolean mVisiable = false;
    private Bitmap mBitmap;
    private Paint mPaint;

    public PinMark() {
    }

    public PinMark(View v, int resId) {
        Resources res = v.getResources();
        InputStream isBmp = res.openRawResource(resId);
        mBitmap = BitmapFactory.decodeStream(isBmp);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(10);
    }

    public void setLocation(int x, int y) {
        this.x = x - mBitmap.getWidth() / 2;
        this.y = y + mBitmap.getHeight() / 2;
    }

    public void offset(int ox, int oy) {
        x += ox;
        y += oy;
    }

    public int getX() {
        return x + mBitmap.getWidth() / 2;
    }

    public int getY() {
        return y - mBitmap.getHeight() / 2;
    }

    @Override
    public void draw(Canvas c, CoordTransform ct) {
        if (!mVisiable)
            return;
        if (mBitmap == null)
            return;
        if (x == 0 && y == 0)
            return;
        int dx = x;
        int dy = y - mBitmap.getHeight();
        c.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        c.drawBitmap(mBitmap, dx, dy, mPaint);
    }

    @Override
    public void setVisiable(boolean visiable) {
        mVisiable = visiable;
    }
}
