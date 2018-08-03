package com.rtmap.wifipicker.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.rtmap.wifipicker.data.WifiPoint;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;

public class RoadNetLayer implements Marker {

    /** 采集点数据类型,ROADNET **/
    public static final int POINT_TYPE_ROADNET = 3;
    
    private ArrayList<WifiPoint> mRoadNetPoints;
    private HashMap<String, ArrayList<WifiPoint>> mHistoryRoadNetPoints;
    private HashMap<String, WifiPoint> mHistoryStartRoadNetPoints;
    
    private Point mPoint;
    private Path mPath;
    private Paint mPaint;
    private Paint mPathPaint;
    private Paint mHistoryPaint;
    private Paint mHistoryStartPaint;
    
    public RoadNetLayer() {
        super();
        
        mRoadNetPoints = new ArrayList<WifiPoint>();
        mHistoryRoadNetPoints = new HashMap<String, ArrayList<WifiPoint>>();
        mHistoryStartRoadNetPoints = new HashMap<String, WifiPoint>();
        
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        
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
        
    }
    
    
    public void addRoadNetPoint(WifiPoint roadnet) {
        mRoadNetPoints.add(roadnet);
    }
    
    public void addHistoryRoadNetPoints(String path, ArrayList<WifiPoint> points) {
        mHistoryRoadNetPoints.put(path, points);
        mHistoryStartRoadNetPoints.put(path, points.get(0));
    }
    
    public void clearHistoryRoadNetPoints() {
        mHistoryRoadNetPoints.clear();
        mHistoryStartRoadNetPoints.clear();
    }
    
    public ArrayList<WifiPoint> getRoadNetPoints() {
        return mRoadNetPoints;
    }
    
    public void deleteLastRoadNetPoint() {
        if (mRoadNetPoints.size() > 0) {
            mRoadNetPoints.remove(mRoadNetPoints.size() - 1);
        }
    }
    
    public void clearAllRoadNetPoints() {
        mRoadNetPoints.clear();
    }

    @Override
    public void draw(Canvas c, CoordTransform ct) {
        
        // 当前轨迹
        mPath.reset();
        for (int i = 0; i < mRoadNetPoints.size(); i++) {
            int wx = mRoadNetPoints.get(i).mX;
            int wy = mRoadNetPoints.get(i).mY;
            ct.worldToClient(wx, wy, mPoint);
            if (mPoint.x > 0 && mPoint.y > 0) {
                mPaint.setColor(Color.RED);
                c.drawRect(mPoint.x - 4, mPoint.y - 4, mPoint.x + 4, mPoint.y + 4, mPaint);
            }
            if(i == 0) {
                mPath.moveTo(mPoint.x, mPoint.y);
            } else {
                mPath.lineTo(mPoint.x, mPoint.y);
            }
        }
        c.drawPath(mPath, mPathPaint);
        
        // 历史轨迹
        Iterator<Map.Entry<String,ArrayList<WifiPoint>>> iterRD = mHistoryRoadNetPoints.entrySet().iterator();
        while (iterRD.hasNext()) {
            Map.Entry<String, ArrayList<WifiPoint>> entry = (Map.Entry<String, ArrayList<WifiPoint>>) iterRD.next();
            ArrayList<WifiPoint> points = (ArrayList<WifiPoint>) entry.getValue();
            mPath.reset();
            int sizeOfPoints = points.size();
            for (int j = 0; j < sizeOfPoints; j++) {
                int wx = points.get(j).mX;
                int wy = points.get(j).mY;
                ct.worldToClient(wx, wy, mPoint);

                if (mPoint.x > 0 && mPoint.y > 0) {
                    mPaint.setColor(Color.RED);
                    if(j == 0) {
                        c.drawRect(mPoint.x - 4, mPoint.y - 4, mPoint.x + 4,
                                mPoint.y + 4, mHistoryStartPaint);
                    } else {
                        c.drawRect(mPoint.x - 4, mPoint.y - 4, mPoint.x + 4,
                                mPoint.y + 4, mHistoryPaint);
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
