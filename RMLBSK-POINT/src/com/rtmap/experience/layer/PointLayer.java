package com.rtmap.experience.layer;

import java.util.ArrayList;

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
import com.rtmap.experience.core.model.BeaconInfo;
import com.rtmap.experience.core.model.LCPoint;
import com.rtmap.experience.util.DTImageUtil;
import com.rtmap.experience.util.DTLog;
import com.rtmap.experience.util.DTStringUtils;
import com.rtmap.experience.util.DTUIUtils;
import com.rtmap.experience.util.map.LCPointTransform;
import com.rtmap.experience.util.map.MapWidget;
import com.rtmap.experience.util.map.Marker;

public class PointLayer implements Marker {

	/** 采集点数据类型,mapcorrect **/
	public static final int POINT_TYPE_MAP_CORRECT = 4;

	private ArrayList<BeaconInfo> correctPoints;

	private Point mPoint;
	private Path mPath;
	private Paint mPaint;
	private Paint mTextPaint;
	private Paint mPathPaint;
	private Paint mHistoryPaint;
	private Paint mHistoryStartPaint;
	private int mScale;// 比例尺
	private Bitmap mPointBitmap;

	public PointLayer(int scale) {
		super();

		mScale = scale;
		correctPoints = new ArrayList<BeaconInfo>();

		mPointBitmap = DTImageUtil.drawableToBitmap(DTUIUtils
				.getDrawable(R.drawable.map_point));

		mPaint = new Paint();
		mPaint.setColor(Color.RED);
		mPaint.setStyle(Paint.Style.FILL_AND_STROKE);

		mTextPaint = new Paint();
		mTextPaint.setColor(Color.BLACK);
		mTextPaint.setTextSize(20);

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

	public void addCorrectPoint(BeaconInfo point) {
		if (point != null)
			correctPoints.add(point);
	}

	public void addCorrectPointList(ArrayList<BeaconInfo> point) {
		if (point != null && point.size() != 0)
			correctPoints.addAll(point);
	}

	public ArrayList<BeaconInfo> getCorrectPoints() {
		return correctPoints;
	}
	
	public int getCount(){
		return correctPoints.size();
	}

	public void deleteLastCorrectPoint() {
		if (correctPoints.size() > 0) {
			correctPoints.remove(correctPoints.size() - 1);
		}
	}

	public void clearAllCorrectPoints() {
		correctPoints.clear();
	}

	public void removeLastBeacon() {
		if (correctPoints.size() > 0) {
			correctPoints.remove(correctPoints.size() - 1);
		}
	}

	public int getmScale() {
		return mScale;
	}

	public void setmScale(int mScale) {
		this.mScale = mScale;
	}

	@Override
	public void draw(Canvas c, LCPointTransform ct) {

		// 当前轨迹
		mPath.reset();
		for (int i = 0; i < correctPoints.size(); i++) {
			BeaconInfo info = correctPoints.get(i);
			int wx, wy;
			if (mScale == 0) {
				wx = info.getX();
				wy = info.getY();
			} else {
				wx = info.getX() / mScale;
				wy = info.getY() / mScale;
			}
			ct.worldToClient(wx, wy, mPoint);
			if (mPoint.x > 0 && mPoint.y > 0) {
				mPaint.setColor(Color.RED);
				c.drawBitmap(mPointBitmap, mPoint.x - 24, mPoint.y - 55, mPaint);
				if (!DTStringUtils.isEmpty(info.getMac()))
					c.drawText(info.getMac(), mPoint.x - info.getMac().length()
							* 5, mPoint.y + 24, mTextPaint);
			}
		}
		c.drawPath(mPath, mPathPaint);

	}

	@Override
	public void setVisiable(boolean visiable) {
	}

	/**
	 * 点击方法
	 * 
	 * @param mw
	 * @param x
	 * @param y
	 */
	public void onSingleTap(MapWidget mw, float x, float y,
			OnLayerPointClickListener listener) {
		LCPoint point = new LCPoint();
		mw.getLCPointTransformer().clientToWorld(x, y, point);
		int p2p = -1;// 触点与地图上的点两个点之间的距离
		for (int i = 0; i < correctPoints.size(); i++) {
			BeaconInfo p = correctPoints.get(i);
			DTLog.e("x : " + point.getX() * mScale + "   y : " + point.getY()
					* mScale + " p.x:" + p.getX() + "   p.y:" + p.getY());
			if (Math.abs(p.getX() / mScale - point.getX()) <= 25
					&& Math.abs(p.getY() / mScale - point.getY()) <= 25) {
				p2p = i;
				break;
			}
		}
		if (p2p != -1)
			listener.onClick(correctPoints.get(p2p));
	}

}
