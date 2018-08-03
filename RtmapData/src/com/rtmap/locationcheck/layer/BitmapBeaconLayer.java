package com.rtmap.locationcheck.layer;

import java.util.ArrayList;
import java.util.HashMap;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;

import com.rtmap.locationcheck.core.model.BeaconInfo;
import com.rtmap.locationcheck.util.DTStringUtils;
import com.rtmap.locationcheck.util.map.Coord;
import com.rtmap.locationcheck.util.map.CoordTransform;
import com.rtmap.locationcheck.util.map.MapWidget;
import com.rtmap.locationcheck.util.map.Marker;

public class BitmapBeaconLayer implements Marker {

	/** 采集点数据类型,mapcorrect **/
	public static final int POINT_TYPE_MAP_CORRECT = 4;

	private ArrayList<BeaconInfo> correctPoints;

	private Point mPoint;
	private Path mPath;
	private Paint mPaint;
	private Paint mTextPaint;
	private Paint mRedPaint;
	private Bitmap mClickPointIcon;// 中间点icon
	private HashMap<Integer, Bitmap> mIconMap;// 中间点icon

	public BitmapBeaconLayer(HashMap<Integer, Bitmap> statusBitmap) {
		super();

		correctPoints = new ArrayList<BeaconInfo>();

		mPaint = new Paint();
		mPaint.setColor(Color.RED);
		mPaint.setStyle(Paint.Style.FILL_AND_STROKE);

		mTextPaint = new Paint();
		mTextPaint.setTextAlign(Align.CENTER);
		mTextPaint.setColor(Color.BLACK);
		mTextPaint.setAntiAlias(true);
		mTextPaint.setTextSize(30); // (Config.getDensity()+1)/2

		mRedPaint = new Paint();
		mRedPaint.setColor(Color.RED);
		mRedPaint.setStyle(Style.STROKE);
		mRedPaint.setStrokeWidth(2);
		mRedPaint.setStrokeCap(Cap.ROUND);
		mRedPaint.setStrokeJoin(Join.ROUND);

		mPath = new Path();
		mPoint = new Point();
		mIconMap = statusBitmap;
	}

	private float scale;

	public float getScale() {
		return scale;
	}

	public void setScale(float scale) {
		this.scale = scale;
	}

	public void addCorrectPoint(BeaconInfo Correct) {
		correctPoints.add(Correct);
	}

	public ArrayList<BeaconInfo> getCorrectPoints() {
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

	/**
	 * 设置点击点得颜色
	 * 
	 * @param clickBmp
	 */
	public void setClickPoint(Bitmap clickBmp) {
		mClickPointIcon = clickBmp;
	}

	@Override
	public void draw(Canvas c, CoordTransform ct) {

		// 当前轨迹
		mPath.reset();
		for (int i = 0; i < correctPoints.size(); i++) {
			float wx = correctPoints.get(i).getX() / scale;
			float wy = correctPoints.get(i).getY() / scale;
			BeaconInfo p = correctPoints.get(i);
			ct.worldToClient(wx, wy, mPoint);
			float pointX = mPoint.x - mIconMap.get(0).getWidth() / 2;
			float pointY = mPoint.y - mIconMap.get(0).getWidth() / 2;
			if (mPoint.x > 0 && mPoint.y > 0) {
				// 工作状态：0正常，-1低电量，-2故障，-3缺失，-4未知
				// 编辑状态：0正常，1删除，2新建，3修改
				// 颜色：正常，修改，删除，未知
				if (p.getWork_status() == -4) {
					if (p.getEdit_status() == 1) {
						c.drawBitmap(mIconMap.get(2), pointX, pointY, mPaint);
					} else
						c.drawBitmap(mIconMap.get(3), pointX, pointY, mPaint);
				} else if (p.getWork_status() == 0) {
					if (p.getEdit_status() == 0 || p.getEdit_status() == 2) {// 正常
						c.drawBitmap(mIconMap.get(0), pointX, pointY, mPaint);
					} else if (p.getEdit_status() == 1) {
						c.drawBitmap(mIconMap.get(2), pointX, pointY, mPaint);
					} else {
						c.drawBitmap(mIconMap.get(1), pointX, pointY, mPaint);
					}
				} else {
					if (p.getEdit_status() == 1) {
						c.drawBitmap(mIconMap.get(2), pointX, pointY, mPaint);
					} else {
						c.drawBitmap(mIconMap.get(1), pointX, pointY, mPaint);
					}
				}
			}
			if (!DTStringUtils.isEmpty(p.getName()) && isVisibility) {
				c.drawText(p.getName(), pointX, pointY
						+ mIconMap.get(0).getWidth() + 20, mTextPaint);
			}

			if (mClickPointIcon != null && correctPoints.get(i).isClick()) {
				float clickX = mPoint.x - mClickPointIcon.getWidth() / 2.0f;// 实际点的x
				float clickY = mPoint.y - mClickPointIcon.getHeight();// 实际点的y
				c.drawBitmap(mClickPointIcon, clickX, clickY, null);// 画出点
			}
		}
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
		Coord point = new Coord();
		mw.getLCPointTransformer().clientToWorld(x, y, point);
		int p2p = -1;// 触点与地图上的点两个点之间的距离
		for (int i = 0; i < correctPoints.size(); i++) {
			BeaconInfo p = correctPoints.get(i);
			p.setClick(false);
			if (Math.abs(p.getX() / scale - point.mX) <= 25
					&& Math.abs(p.getY() / scale - point.mY) <= 25) {
				p2p = i;
			}
		}
		if (p2p != -1)
			listener.onClick(correctPoints.get(p2p));
	}

	private boolean isVisibility;

	/**
	 * 是否显示name
	 * 
	 * @param visibility
	 */
	public void setNameVisibility(boolean visibility) {
		isVisibility = visibility;
	}

	@Override
	public void setVisiable(boolean visiable) {

	}
	
	
	/**
	 * 移除点
	 * 
	 * @param index
	 */
	public void clearPoint(int index) {
		if (index > correctPoints.size() - 1)
			return;
		correctPoints.remove(index);
	}

	/**
	 * 移除点
	 * 
	 * @param point
	 */
	public void clearPoint(BeaconInfo point) {
		if (correctPoints.contains(point))
			correctPoints.remove(point);
	}

	public int getPointCount() {
		return correctPoints.size();
	}

	public BeaconInfo getPoint(int i) {
		return correctPoints.get(i);
	}

	public ArrayList<BeaconInfo> getPointList() {
		return correctPoints;
	}

}
