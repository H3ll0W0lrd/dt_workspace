package com.rtmap.wifipicker.widget;

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
import android.graphics.Point;

import com.rtmap.wifipicker.core.model.RMPoi;

public class MapCorrectionLayer implements Marker {

	/** 采集点数据类型,mapcorrect **/
	public static final int POINT_TYPE_MAP_CORRECT = 4;

	private HashMap<String, ArrayList<RMPoi>> mRouteMap;

	private Point mPoint;
	private Path mPath;
	private Paint mPaint;
	private Paint mTextPaint;
	private Paint mRedPaint;
	private Paint mHistoryPaint;
	private Paint mGreenPaint;
	private float mScale;

	public MapCorrectionLayer(float scale) {
		super();

		mScale = scale;
		mRouteMap = new HashMap<String, ArrayList<RMPoi>>();

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

		mGreenPaint = new Paint();
		mGreenPaint.setColor(Color.GREEN);
		mGreenPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		mGreenPaint.setStrokeWidth(2);
		mGreenPaint.setStrokeCap(Cap.ROUND);
		mGreenPaint.setStrokeJoin(Join.ROUND);

		mPath = new Path();
		mPoint = new Point();

	}

	public void addRoute(String path, ArrayList<RMPoi> points) {
		mRouteMap.put(path, points);
	}

	public ArrayList<RMPoi> getRoute(String key) {
		if (mRouteMap.containsKey(key)) {
			return mRouteMap.get(key);
		}
		return null;
	}

	public void clearHistoryCorrectPoints() {
		mRouteMap.clear();
	}

	public HashMap<String, ArrayList<RMPoi>> getRouteMap() {
		return mRouteMap;
	}

	@Override
	public void draw(Canvas c, CoordTransform ct) {

		// 当前轨迹
		mPath.reset();
		// 历史轨迹
		Iterator<Map.Entry<String, ArrayList<RMPoi>>> iterRD = mRouteMap
				.entrySet().iterator();
		while (iterRD.hasNext()) {
			Map.Entry<String, ArrayList<RMPoi>> entry = (Map.Entry<String, ArrayList<RMPoi>>) iterRD
					.next();
			ArrayList<RMPoi> points = (ArrayList<RMPoi>) entry.getValue();
			mPath.reset();
			int sizeOfPoints = points.size();
			int sumX = 0;
			int sumY = 0;
			for (int j = 0; j < sizeOfPoints; j++) {
				ct.worldToClient(points.get(j).getX() / mScale, points.get(j)
						.getY() / mScale, mPoint);
				if (!"door".equals(entry.getKey())) {
					if (j == 0) {
						c.drawRect(mPoint.x - 4, mPoint.y - 4, mPoint.x + 4,
								mPoint.y + 4, mGreenPaint);
					} else {
						c.drawRect(mPoint.x - 4, mPoint.y - 4, mPoint.x + 4,
								mPoint.y + 4, mHistoryPaint);
					}
					sumX += mPoint.x;
					sumY += mPoint.y;

					if (j == 0) {
						mPath.moveTo(mPoint.x, mPoint.y);
					} else {
						mPath.lineTo(mPoint.x, mPoint.y);
					}
				} else {
					c.drawRect(mPoint.x - 4, mPoint.y - 4, mPoint.x + 4,
							mPoint.y + 4, mGreenPaint);
					c.drawText(points.get(0).getName(), mPoint.x, mPoint.y,
							mTextPaint);
				}
			}
			if (!"door".equals(entry.getKey())) {
				c.drawPath(mPath, mHistoryPaint);
				if (sizeOfPoints != 0) {
					int tx = sumX / sizeOfPoints;
					int ty = sumY / sizeOfPoints;
					c.drawText(points.get(0).getName(), tx, ty, mTextPaint);
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
