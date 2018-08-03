package com.rtmap.locationcheck.layer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
import android.graphics.PathEffect;
import android.graphics.Point;

import com.rtmap.locationcheck.core.model.LCPoint;
import com.rtmap.locationcheck.util.map.CoordTransform;
import com.rtmap.locationcheck.util.map.Marker;

public class BitmapRouteLayer implements Marker {

	/** 采集点数据类型,mapcorrect **/
	public static final int POINT_TYPE_MAP_CORRECT = 4;

	private HashMap<String, ArrayList<LCPoint>> mRouteMap;

	private Point mPoint;
	private Paint mPaint;
	private Paint mTextPaint;
	private Paint mRedPaint;
	private Paint mHistoryPaint;
	private Paint mGreenPaint;
	private float mScale;

	public BitmapRouteLayer(float scale) {
		super();

		mScale = scale;
		mRouteMap = new HashMap<String, ArrayList<LCPoint>>();

		mPaint = new Paint();
		mPaint.setColor(Color.RED);
		mPaint.setStyle(Paint.Style.FILL_AND_STROKE);

		mTextPaint = new Paint();
		mTextPaint.setColor(Color.BLACK);
		mTextPaint.setTextSize(15);

		mRedPaint = new Paint();
		mRedPaint.setColor(Color.RED);
		mRedPaint.setStyle(Style.STROKE);
		mRedPaint.setStrokeWidth(2);
		mRedPaint.setStrokeCap(Cap.ROUND);
		mRedPaint.setStrokeJoin(Join.ROUND);

		mHistoryPaint = new Paint();
		mHistoryPaint.setColor(Color.BLUE);
		mHistoryPaint.setStyle(Paint.Style.STROKE);
		mHistoryPaint.setStrokeWidth(2);
		mHistoryPaint.setStrokeCap(Cap.ROUND);
		mHistoryPaint.setStrokeJoin(Join.ROUND);
		PathEffect peArray = new PathDashPathEffect(makePathDash(), 18, 0,
				PathDashPathEffect.Style.ROTATE);
		mHistoryPaint.setPathEffect(peArray);

		mGreenPaint = new Paint();
		mGreenPaint.setColor(Color.GREEN);
		mGreenPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		mGreenPaint.setStrokeWidth(2);
		mGreenPaint.setStrokeCap(Cap.ROUND);
		mGreenPaint.setStrokeJoin(Join.ROUND);

		mPoint = new Point();

	}

	public void addRoute(String path, ArrayList<LCPoint> points) {
		mRouteMap.put(path, points);
	}

	public void addRouteMap(HashMap<String, ArrayList<LCPoint>> map) {
		mRouteMap.putAll(map);
	}

	private Path makePathDash() {
		Path p = new Path();
		p.moveTo(4, 0);
		p.lineTo(0, -4);
		p.lineTo(8, -4);
		p.lineTo(12, 0);
		p.lineTo(8, 4);
		p.lineTo(0, 4);
		return p;
	}

	public ArrayList<LCPoint> getRoute(String key) {
		if (mRouteMap.containsKey(key)) {
			return mRouteMap.get(key);
		}
		return null;
	}

	public void clearHistoryCorrectPoints() {
		mRouteMap.clear();
	}

	public HashMap<String, ArrayList<LCPoint>> getRouteMap() {
		return mRouteMap;
	}

	@Override
	public void draw(Canvas c, CoordTransform ct) {

		// 历史轨迹
		Iterator<Map.Entry<String, ArrayList<LCPoint>>> iterRD = mRouteMap
				.entrySet().iterator();
		while (iterRD.hasNext()) {
			Map.Entry<String, ArrayList<LCPoint>> entry = (Map.Entry<String, ArrayList<LCPoint>>) iterRD
					.next();
			ArrayList<LCPoint> points = (ArrayList<LCPoint>) entry.getValue();
			int sizeOfPoints = points.size();
			for (int j = 0; j < sizeOfPoints; j++) {
				ct.worldToClient(points.get(j).getX() / mScale, points.get(j)
						.getY() / mScale, mPoint);
				if (!"door".equals(entry.getKey())) {
					if (j != 0) {
						Point point = new Point();
						ct.worldToClient(points.get(j - 1).getX() / mScale,
								points.get(j - 1).getY() / mScale, point);
						c.drawLine(point.x, point.y, mPoint.x, mPoint.y,
								mHistoryPaint);
					}
					c.drawCircle(mPoint.x - 2, mPoint.y - 2, 8, mGreenPaint);
				} else {
					c.drawCircle(mPoint.x - 2, mPoint.y - 2, 8, mGreenPaint);
					c.drawText(points.get(0).getName(), mPoint.x, mPoint.y,
							mTextPaint);
				}
			}
		}
	}

	@Override
	public void setVisiable(boolean visiable) {
	}

	public void removeRoute(String route) {
		if (mRouteMap.containsKey(route)) {
			mRouteMap.remove(route);
		}
	}

}
