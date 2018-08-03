package com.rtmap.wifipicker.widget;

import android.graphics.Canvas;

public interface Marker {
    public void draw(Canvas c, CoordTransform ct);

    public void setVisiable(boolean visiable);
}