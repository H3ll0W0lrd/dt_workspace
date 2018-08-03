package com.rtmap.locationcheck.layer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Point;

import com.rtmap.locationcheck.core.model.RMPoi;
import com.rtmap.locationcheck.util.map.CoordTransform;
import com.rtmap.locationcheck.util.map.Marker;

public class MapCorrectionLayer implements Marker {

	/** 采集点数据类型,mapcorrect **/
	public static final int POINT_TYPE_MAP_CORRECT = 4;

	private HashMap<String, ArrayList<RMPoi>> mRouteMap;
	private boolean isDraw = true;
	private Point mPoint;
	private Paint mPaint;
	private Paint mTextPaint;
	private Paint mRedPaint;
	private Paint mHistoryPaint;
	private Paint mGreenPaint;
	private float mScale;
	private Bitmap mRouteIcon, mRouteHisIcon, mMarkHisIcon;// 中间点icon
	private Bitmap mMarkIcon;// 标记icon

	public MapCorrectionLayer(float scale, Bitmap point, Bitmap markPoint,
			Bitmap historyPoint, Bitmap markHisPoint) {
		super();

		mScale = scale;

		mRouteIcon = point;
		mRouteHisIcon = historyPoint;
		mMarkHisIcon = markHisPoint;
		mMarkIcon = markPoint;
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

		mPoint = new Point();

	}

	public void addRoute(String path, ArrayList<RMPoi> points) {
		mRouteMap.put(path, points);
	}

	public void addRouteMap(HashMap<String, ArrayList<RMPoi>> map) {
		mRouteMap.putAll(map);
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

	public void setDraw(boolean isDraw) {
		this.isDraw = isDraw;
	}

	@Override
	public void draw(Canvas c, CoordTransform ct) {
		if (!isDraw)
			return;
		// 历史轨迹
		Iterator<Map.Entry<String, ArrayList<RMPoi>>> iterRD = mRouteMap
				.entrySet().iterator();
		while (iterRD.hasNext()) {
			Map.Entry<String, ArrayList<RMPoi>> entry = (Map.Entry<String, ArrayList<RMPoi>>) iterRD
					.next();
			ArrayList<RMPoi> points = (ArrayList<RMPoi>) entry.getValue();
			int sizeOfPoints = points.size();
			int sumX = 0;
			int sumY = 0;
			for (int j = 0; j < sizeOfPoints; j++) {
				ct.worldToClient(points.get(j).getX() / mScale, points.get(j)
						.getY() / mScale, mPoint);
				String key = entry.getKey();
				float pointX = mPoint.x - mRouteIcon.getWidth() / 2.0f;// 实际点的x
				float pointY = mPoint.y - mRouteIcon.getHeight() / 2.0f;// 实际点的y
				if (key.contains(".door")) {
					if (key.contains("door_upload")) {
						c.drawBitmap(mMarkHisIcon, pointX, pointY, null);// 画门
					} else
						c.drawBitmap(mMarkIcon, pointX, pointY, null);// 画门
					c.drawText(points.get(0).getName(), mPoint.x, mPoint.y,
							mTextPaint);
				} else if (key.contains(".poi")) {
					if (key.contains("poi_upload")) {
						c.drawBitmap(mMarkHisIcon, pointX, pointY, null);// 画门
					} else
						c.drawBitmap(mMarkIcon, pointX, pointY, null);// 画门
					c.drawText(points.get(j).getName(), mPoint.x, mPoint.y,
							mTextPaint);
				} else {
					Bitmap bitmap;
					if (key.contains("_upload")) {
						bitmap = mRouteHisIcon;
					} else {
						bitmap = mRouteIcon;
					}
					if (j != 0) {
						RMPoi p1 = points.get(j - 1);
						Point point1 = new Point();
						ct.worldToClient(p1.getX() / mScale,
								p1.getY() / mScale, point1);
						if (point1.x == mPoint.x && point1.y == mPoint.y)
							continue;
						c.drawLine(point1.x, point1.y, mPoint.x, mPoint.y,
								mHistoryPaint);
						if (j > 0 && j < points.size() - 1)
							c.drawBitmap(bitmap, pointX, pointY, null);
						else
							c.drawBitmap(bitmap, pointX, pointY, null);
					} else {
						c.drawBitmap(bitmap, pointX, pointY, null);
					}
					sumX += mPoint.x;
					sumY += mPoint.y;
				}
			}
			if (!entry.getKey().contains(".door")) {
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

	public void setRouteMap(HashMap<String, ArrayList<RMPoi>> map) {
		mRouteMap = map;
	}

	public void removeRoute(String route) {
		if (mRouteMap.containsKey(route)) {
			mRouteMap.remove(route);
		}
	}

}
