package com.rtmap.locationcheck.util.map;

import android.graphics.Canvas;

public interface Marker {
    public void draw(Canvas c, CoordTransform ct);

    public void setVisiable(boolean visiable);
}