package com.rtmap.wifipicker.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.rtmap.wifipicker.data.Coord;
import com.rtmap.wifipicker.data.WifiPoint;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;

public class WifiOverLayer implements Marker {

    /** 采集点数据类型,WIFI **/
    public static final int POINT_TYPE_WIFI = 1;

    private ArrayList<WifiPoint> mWifiPoints;
    private HashMap<String, ArrayList<WifiPoint>> mHistoryWifiPoints;
    //private HashMap<String, WifiPoint> mHistoryStartWifiPoints;

    private Point mPoint;
    private Path mPath;
    
    private Coord mLocateCoord;
    private Point mLocatePoint;
    
    private Paint mPaint;
    private Paint mLocatePaint;
    private Paint mPathPaint;
    private Paint mHistoryPaint;
    private Paint mHistoryStartPaint;

    public WifiOverLayer() {

        mWifiPoints = new ArrayList<WifiPoint>();
        mHistoryWifiPoints = new HashMap<String, ArrayList<WifiPoint>>();
        //mHistoryStartWifiPoints = new HashMap<String, WifiPoint>();

        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mLocatePaint = new Paint();
        mLocatePaint.setColor(Color.BLUE);
        mLocatePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        
        mPathPaint = new Paint();
        mPathPaint.setColor(Color.RED);
        mPathPaint.setStyle(Style.STROKE);
        mPathPaint.setStrokeWidth(2);
        mPathPaint.setStrokeCap(Cap.ROUND);
        mPathPaint.setStrokeJoin(Join.ROUND);

        mHistoryPaint = new Paint();
        mHistoryPaint.setColor(Color.BLACK);
        mHistoryPaint.setAlpha(64);
        mHistoryPaint.setStyle(Paint.Style.STROKE);
        mHistoryPaint.setStrokeWidth(2);
        mHistoryPaint.setStrokeCap(Cap.ROUND);
        mHistoryPaint.setStrokeJoin(Join.ROUND);

        mHistoryStartPaint = new Paint();
        mHistoryStartPaint.setColor(Color.GREEN);
        mHistoryStartPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mHistoryStartPaint.setStrokeWidth(2);
        mHistoryStartPaint.setStrokeCap(Cap.ROUND);
        mHistoryStartPaint.setStrokeJoin(Join.ROUND);

        mPath = new Path();
        mPoint = new Point();
        mLocatePoint = new Point();
        mLocateCoord = new Coord();
    }

    public void setLocateCoord(int x, int y) {
        mLocateCoord.mX = x;
        mLocateCoord.mY = y;
    }

    public void clearWifiPoints() {
        mWifiPoints.clear();
    }

    /**
     * 添加wifi点
     * @param point
     */
    public void addWifiPoint(WifiPoint point) {
        mWifiPoints.add(point);
    }
    
    public void addWifiPoints(ArrayList<WifiPoint> points) {
        mWifiPoints.addAll(points);
    }
    
    public ArrayList<WifiPoint> getWifiPoints() {
        return mWifiPoints;
    }
    
    public void removeWifiPoint(int x, int y) {
        int index = -1, size = mWifiPoints.size();
        for (int i = 0; i < size; i++) {
            if ((x == mWifiPoints.get(i).mX) && (y == mWifiPoints.get(i).mY)) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            mWifiPoints.remove(index);
        }
    }
    
    /** 删除前一次采集点 **/
    public WifiPoint removeBeforePoint() {
        int index = mWifiPoints.size();
        WifiPoint retPoint = new WifiPoint(0, 0);
        if (index > 0) {
            retPoint.mX = mWifiPoints.get(index - 1).mX;
            retPoint.mY = mWifiPoints.get(index - 1).mY;
            mWifiPoints.remove(index - 1);
        }

        return retPoint;
    }

    public ArrayList<WifiPoint> getHistoryWifiPoints(String startPoint) {
        ArrayList<WifiPoint> wps = new ArrayList<WifiPoint>(mHistoryWifiPoints.get(startPoint));
        return wps;
    }
    
    public void addHistoryWifiPoints(String path, ArrayList<WifiPoint> points) {
        mHistoryWifiPoints.put(path, points);
        //mHistoryStartWifiPoints.put(path, points.get(0));
    }

    public void removeHistory(String startPointStr) {
        mHistoryWifiPoints.remove(startPointStr);
    }
    
    public void clearHistoryWifiPoints() {
        mHistoryWifiPoints.clear();
        //mHistoryStartWifiPoints.clear();
    }

    public WifiPoint getLastPointOfOneHistory(String startPointStr) {
        WifiPoint deletPoint = null;
        ArrayList<WifiPoint> wps = mHistoryWifiPoints.get(startPointStr);
        if (wps != null && wps.size() > 0) {
            deletPoint = new WifiPoint(wps.get(wps.size() - 1));
        }
        return deletPoint;
    }
    
    public WifiPoint removeLastPointOfOneHistory(String startPointStr) {
        WifiPoint deletPoint = null;
        ArrayList<WifiPoint> wps = mHistoryWifiPoints.get(startPointStr);
        if (wps != null && wps.size() > 0) {
            deletPoint = new WifiPoint(wps.get(wps.size() - 1));
            wps.remove(wps.size() - 1);
            mHistoryWifiPoints.put(startPointStr, wps);
        }
        return deletPoint;
    }
   

    public void draw(Canvas c, CoordTransform ct) {

        int size = mWifiPoints.size();
        mPath.reset();
        for (int i = 0; i < size; i++) {
            int wx = mWifiPoints.get(i).mX;
            int wy = mWifiPoints.get(i).mY;
            ct.worldToClient(wx, wy, mPoint);

            if (mPoint.x > 0 && mPoint.y > 0) {
                mPaint.setColor(Color.RED);
                c.drawRect(mPoint.x - 4, mPoint.y - 4, mPoint.x + 4, mPoint.y + 4, mPaint);
            }

            if (i == 0) {
                mPath.moveTo(mPoint.x, mPoint.y);
            } else {
                mPath.lineTo(mPoint.x, mPoint.y);
            }
        }
        c.drawPath(mPath, mPathPaint);
        
        // 画定位点
        ct.worldToClient(mLocateCoord.mX, mLocateCoord.mY, mLocatePoint);
        c.drawCircle(mLocatePoint.x, mLocatePoint.y, 8, mLocatePaint); 

        Iterator<Map.Entry<String, ArrayList<WifiPoint>>> iter = mHistoryWifiPoints.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, ArrayList<WifiPoint>> entry = (Map.Entry<String, ArrayList<WifiPoint>>) iter.next();
            ArrayList<WifiPoint> points = (ArrayList<WifiPoint>) entry.getValue();
            mPath.reset();
            int sizeOfPoints = points.size();
            for (int j = 0; j < sizeOfPoints; j++) {
                int wx = points.get(j).mX;
                int wy = points.get(j).mY;
                ct.worldToClient(wx, wy, mPoint);

                if (mPoint.x > 0 && mPoint.y > 0) {
                    mPaint.setColor(Color.RED);
                    if (j == 0) {
                        c.drawRect(mPoint.x - 4, mPoint.y - 4, mPoint.x + 4, mPoint.y + 4, mHistoryStartPaint);
                    } else {
                        c.drawRect(mPoint.x - 4, mPoint.y - 4, mPoint.x + 4, mPoint.y + 4, mHistoryPaint);
                    }

                }
                if (j == 0) {
                    mPath.moveTo(mPoint.x, mPoint.y);
                } else {
                    mPath.lineTo(mPoint.x, mPoint.y);
                }
            }
            c.drawPath(mPath, mHistoryPaint);
        }
    }

    @Override
    public void setVisiable(boolean visiable) {

    }
}
