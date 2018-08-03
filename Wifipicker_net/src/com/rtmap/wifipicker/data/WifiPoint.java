package com.rtmap.wifipicker.data;

public class WifiPoint {
    public int mX;

    public int mY;

    public int mType;

    public String mName;

    public WifiPoint() {
    }

    public WifiPoint(int x, int y) {
        mX = x;
        mY = y;
    }

    public WifiPoint(int x, int y, int type) {
        mX = x;
        mY = y;
        mType = type;
    }

    public WifiPoint(int x, int y, int type, String name) {
        mX = x;
        mY = y;
        mType = type;
        mName = name;
    }

    public WifiPoint(WifiPoint wp) {
        mX = wp.mX;
        mY = wp.mY;
        mType = wp.mType;
        mName = wp.mName;
    }

    // @Override
    public boolean equals(Object object) {
        if (object instanceof WifiPoint) {
            WifiPoint point = (WifiPoint) object;
            if (point.mX == mX && point.mY == mY) {
                return true;
            }
        }
        return false;
    }
}
