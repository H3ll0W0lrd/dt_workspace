package com.rtmap.experience.util.map;

import android.graphics.Canvas;

public interface Marker {
    public void draw(Canvas c, LCPointTransform ct);

    public void setVisiable(boolean visiable);
}