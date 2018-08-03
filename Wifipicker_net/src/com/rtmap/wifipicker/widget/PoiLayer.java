package com.rtmap.wifipicker.widget;

import java.util.ArrayList;

import com.rtmap.wifipicker.data.WifiPoint;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

public class PoiLayer implements Marker {
    
    /** 采集点数据类型,POI **/
    public static final int POINT_TYPE_POI = 2;
    private ArrayList<WifiPoint> mPOIPoints;
    private boolean isPoiNameShow;
    private Point mPoint;
    private Paint mPaint;
    private Paint mTextPaint;
    
    public PoiLayer() {
        super();
        isPoiNameShow = true;
        mPOIPoints = new ArrayList<WifiPoint>();
        mPoint = new Point();
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mTextPaint = new Paint();
        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setTextSize(15);
    }
    
    public void setShowPOIName(boolean show) {
        isPoiNameShow = show;
    }

    public ArrayList<WifiPoint> getPOIPoints() {
        return mPOIPoints;
    }
    
    public void clearPOIPoints() {
        mPOIPoints.clear();
    }
    
    public void addPOIPoints(ArrayList<WifiPoint> pois) {
        mPOIPoints.addAll(pois);
    }
    
    public void addPOIPoint(WifiPoint poi) {
        deletPOIPoint(poi);
        mPOIPoints.add(poi);
    }
    
    public WifiPoint getLastPOIPoint() {
        if (mPOIPoints.size() > 0) {
            return mPOIPoints.get(mPOIPoints.size() - 1);
        }
        return null;
    }
    
    public void deletPOIPoint(WifiPoint poi) {
        int size = mPOIPoints.size();
        for(int i = 0; i < size; i++) {
            if(mPOIPoints.get(i).mX == poi.mX && mPOIPoints.get(i).mY == poi.mY) {
                mPOIPoints.remove(i);
                break;
            }
        }
    }
    
    @Override
    public void draw(Canvas c, CoordTransform ct) {
        
        int sizeOfPOIs = mPOIPoints.size();
        for(int i = 0; i < sizeOfPOIs; i++) {
            int wx = mPOIPoints.get(i).mX;
            int wy = mPOIPoints.get(i).mY;
            ct.worldToClient(wx, wy, mPoint);
            if(mPoint.x <= 0 || mPoint.y <= 0) {
                continue;
            }
            mPaint.setColor(Color.GREEN);
            //c.drawCircle(mPoint.x, mPoint.y, 6, mPaint);
            c.drawRect(mPoint.x - 3, mPoint.y - 3, mPoint.x + 3, mPoint.y + 3, mPaint);
            if(isPoiNameShow) {
                String[] tmp = mPOIPoints.get(i).mName.split("\t");
                String poiName = tmp[tmp.length - 1];
                c.drawText(poiName, mPoint.x, mPoint.y, mTextPaint);
            }
        }
        
    }

    @Override
    public void setVisiable(boolean visiable) {
    }
}
