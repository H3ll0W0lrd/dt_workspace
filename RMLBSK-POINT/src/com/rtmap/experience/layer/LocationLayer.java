package com.rtmap.experience.layer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.Drawable;

import com.rtmap.experience.R;
import com.rtmap.experience.core.model.Floor;
import com.rtmap.experience.core.model.LCPoint;
import com.rtmap.experience.util.DTImageUtil;
import com.rtmap.experience.util.DTLog;
import com.rtmap.experience.util.DTUIUtils;
import com.rtmap.experience.util.map.LCPointTransform;
import com.rtmap.experience.util.map.MapWidget;
import com.rtmap.experience.util.map.Marker;

public class LocationLayer implements Marker {

	private Point mPoint;
	private Path mPath;
	private Paint mPaint;
	private Paint mTextPaint;
	private Floor mFloor;// 比例尺
	private LCPoint mLoactionPoint;
	private Bitmap mLightBitmap, mNormalBitmap;
	private MapWidget mMapView;

	public LocationLayer(Floor floor, MapWidget mapView) {
		super();
		mMapView = mapView;
		mFloor = floor;
		mLoactionPoint = new LCPoint();

		mPaint = new Paint();
		mPaint.setColor(Color.RED);
		mPaint.setStyle(Paint.Style.FILL_AND_STROKE);

		mTextPaint = new Paint();
		mTextPaint.setColor(Color.BLACK);
		mTextPaint.setTextSize(15);

		mPath = new Path();
		mPoint = new Point();

		Drawable drawable = DTUIUtils.getResources().getDrawable(
				R.drawable.icon_locr_light);
		mLightBitmap = DTImageUtil.drawableToBitmap(drawable);
		drawable = DTUIUtils.getResources().getDrawable(
				R.drawable.icon_locr_normal);
		mNormalBitmap = DTImageUtil.drawableToBitmap(drawable);
	}

	private int mX, mY;

	/**
	 * 设置定位数据
	 * 
	 * @param x
	 * @param y
	 */
	public void setLocation(int x, int y) {
		mX = x;
		mY = y;
	}

	private boolean isLight;
	private long mTime;

	@Override
	public void draw(Canvas c, LCPointTransform ct) {
		if (mTime == 0) {
			mTime = System.currentTimeMillis();
		} else {
			long delay = System.currentTimeMillis() - mTime;
			if (delay > 500) {
				isLight = !isLight;
				mTime = System.currentTimeMillis();
			}
		}
		if (mX == 0 && mY == 0)
			return;
		int wx, wy;
		if (mFloor.getScale() == 0) {
			wx = mX;
			wy = mY;
		} else {
			wx = mX / mFloor.getScale();
			wy = mY / mFloor.getScale();
		}
		ct.worldToClient(wx, wy, mPoint);
		if (isLight) {
			c.drawBitmap(mLightBitmap, mPoint.x - mLightBitmap.getWidth() / 2,
					mPoint.y - mLightBitmap.getWidth() / 2, null);
		} else {
			c.drawBitmap(mNormalBitmap, mPoint.x - mLightBitmap.getWidth() / 2,
					mPoint.y - mLightBitmap.getWidth() / 2, null);
		}
	}

	@Override
	public void setVisiable(boolean visiable) {
	}

}
