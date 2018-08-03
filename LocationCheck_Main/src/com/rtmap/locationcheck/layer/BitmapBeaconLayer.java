package com.rtmap.locationcheck.layer;

import java.util.ArrayList;

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
import android.graphics.drawable.Drawable;

import com.rtmap.locationcheck.R;
import com.rtmap.locationcheck.core.model.BeaconInfo;
import com.rtmap.locationcheck.util.DTIOUtils;
import com.rtmap.locationcheck.util.DTLog;
import com.rtmap.locationcheck.util.DTStringUtils;
import com.rtmap.locationcheck.util.DTUIUtils;
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
	private Bitmap mRedBitmap;

	public BitmapBeaconLayer() {
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
		Drawable red = DTUIUtils.getResources()
				.getDrawable(R.drawable.sign_red);
		mRedBitmap = DTIOUtils.drawableToBitmap(red);

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

	@Override
	public void draw(Canvas c, CoordTransform ct) {

		// 当前轨迹
		mPath.reset();
		for (int i = 0; i < correctPoints.size(); i++) {
			float wx = correctPoints.get(i).getX()/scale;
			float wy = correctPoints.get(i).getY()/scale;
			ct.worldToClient(wx, wy, mPoint);
			if (mPoint.x > 0 && mPoint.y > 0) {
				c.drawBitmap(mRedBitmap, mPoint.x-mRedBitmap.getWidth()/2, mPoint.y-mRedBitmap.getWidth()/2, mPaint);
			}
			if (!DTStringUtils.isEmpty(correctPoints.get(i).getMac())
					&& isVisibility) {
				c.drawText(correctPoints.get(i).getMajor()+"   "+correctPoints.get(i).getMinor(), mPoint.x, mPoint.y
						+ mRedBitmap.getWidth() + 20, mTextPaint);
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
			if (Math.abs(p.getX()/scale - point.mX) <= 25
					&& Math.abs(p.getY()/scale - point.mY) <= 25) {
				p2p = i;
				break;
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
