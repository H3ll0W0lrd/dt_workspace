package com.rtmap.experience.layer;

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
import android.graphics.Path;
import android.graphics.Point;

import com.rtmap.experience.R;
import com.rtmap.experience.core.model.LCPoint;
import com.rtmap.experience.util.DTImageUtil;
import com.rtmap.experience.util.DTUIUtils;
import com.rtmap.experience.util.map.LCPointTransform;
import com.rtmap.experience.util.map.Marker;

public class MapCorrectionLayer implements Marker {

	/** 采集点数据类型,mapcorrect **/
	public static final int POINT_TYPE_MAP_CORRECT = 4;

	private ArrayList<LCPoint> correctPoints;
	private HashMap<String, ArrayList<LCPoint>> historyCorrectPoints;
	private HashMap<String, LCPoint> historyStartCorrectPoints;

	private Point mPoint;
	private Path mPath;
	private Paint mPaint;
	private Paint mTextPaint;
	private Paint mPathPaint;
	private Paint mHistoryPaint;
	private Paint mHistoryStartPaint;

	private Bitmap mPointBitmap;

	public MapCorrectionLayer() {
		super();

		correctPoints = new ArrayList<LCPoint>();
		historyCorrectPoints = new HashMap<String, ArrayList<LCPoint>>();
		historyStartCorrectPoints = new HashMap<String, LCPoint>();

		mPointBitmap = DTImageUtil.drawableToBitmap(DTUIUtils
				.getDrawable(R.drawable.map_point));

		mPaint = new Paint();
		mPaint.setColor(Color.RED);
		mPaint.setStyle(Paint.Style.FILL_AND_STROKE);

		mTextPaint = new Paint();
		mTextPaint.setColor(Color.BLACK);
		mTextPaint.setTextSize(15);

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

	public void addCorrectPoint(LCPoint correct) {
		correctPoints.add(correct);
	}

	public void addHistoryCorrectPoints(String path, ArrayList<LCPoint> points) {
		historyCorrectPoints.put(path, points);
		historyStartCorrectPoints.put(path, points.get(0));
	}

	public void clearHistoryCorrectPoints() {
		historyCorrectPoints.clear();
		historyStartCorrectPoints.clear();
	}

	public ArrayList<LCPoint> getCorrectPoints() {
		return correctPoints;
	}

	public void deleteLastCorrectPoint() {
		if (correctPoints.size() > 0) {
			correctPoints.remove(correctPoints.size() - 1);
		}
	}

	public void clearAllCorrectPoints() {
		correctPoints.clear();
	}

	@Override
	public void draw(Canvas c, LCPointTransform ct) {

		// 当前轨迹
		mPath.reset();
		for (int i = 0; i < correctPoints.size(); i++) {
			int wx = correctPoints.get(i).getX();
			int wy = correctPoints.get(i).getY();
			ct.worldToClient(wx, wy, mPoint);
			if (mPoint.x > 0 && mPoint.y > 0) {
				mPaint.setColor(Color.RED);
				c.drawBitmap(mPointBitmap, mPoint.x-24, mPoint.y-55, mPaint);
//				c.drawRect(mPoint.x - 4, mPoint.y - 4, mPoint.x + 4,
//						mPoint.y + 4, );
			}
			if (i == 0) {
				mPath.moveTo(mPoint.x, mPoint.y);
			} else {
				mPath.lineTo(mPoint.x, mPoint.y);
			}
		}
		c.drawPath(mPath, mPathPaint);

		// 历史轨迹
		Iterator<Map.Entry<String, ArrayList<LCPoint>>> iterRD = historyCorrectPoints
				.entrySet().iterator();
		while (iterRD.hasNext()) {
			Map.Entry<String, ArrayList<LCPoint>> entry = (Map.Entry<String, ArrayList<LCPoint>>) iterRD
					.next();
			ArrayList<LCPoint> points = (ArrayList<LCPoint>) entry.getValue();
			mPath.reset();
			int sizeOfPoints = points.size();
			int sumX = 0;
			int sumY = 0;
			for (int j = 0; j < sizeOfPoints; j++) {
				int wx = points.get(j).getX();
				int wy = points.get(j).getY();
				ct.worldToClient(wx, wy, mPoint);
				if (mPoint.x > 0 && mPoint.y > 0) {
					if (j == 0) {
//						c.drawRect(mPoint.x - 4, mPoint.y - 4, mPoint.x + 4,
//								mPoint.y + 4, mHistoryStartPaint);
						c.drawBitmap(mPointBitmap, mPoint.x-15, mPoint.y-40, mHistoryPaint);
					} else {
//						c.drawRect(mPoint.x - 4, mPoint.y - 4, mPoint.x + 4,
//								mPoint.y + 4, mHistoryPaint);
						c.drawBitmap(mPointBitmap, mPoint.x-15, mPoint.y-40, mHistoryPaint);
					}
					sumX += mPoint.x;
					sumY += mPoint.y;
				}
				if (j == 0) {
					mPath.moveTo(mPoint.x, mPoint.y);
				} else {
					mPath.lineTo(mPoint.x, mPoint.y);
				}
			}
			c.drawPath(mPath, mHistoryPaint);
			if (sizeOfPoints != 0) {
				int tx = sumX / sizeOfPoints;
				int ty = sumY / sizeOfPoints;
				c.drawText(points.get(0).getName(), tx, ty, mTextPaint);
			}
		}

	}

	@Override
	public void setVisiable(boolean visiable) {
	}

}
