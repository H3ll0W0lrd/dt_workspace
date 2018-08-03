package com.rtm.frm.map;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;

import com.rtm.common.utils.Constants;
import com.rtm.frm.model.PointInfo;

/**
 * 指南针图层
 * 
 * @author dingtao
 *
 */
public class CompassLayer implements BaseMapLayer {
	private MapView mMapView;
	private Paint mPaint;
	private Bitmap bmp;
	private int x;
	private int y;
	private int DrawX = 5;
	private int DrawY = 5;
	private static final int TAP_BOUND = 30;
	private int mPosition = Constants.TOP_RIGHT;

	/**
	 * 设置指南针x轴位置
	 * 
	 * @param drawX
	 */
	public void setDrawX(int drawX) {
		DrawX = drawX;
	}

	/**
	 * 设置指南针y轴位置
	 * 
	 * @param drawY
	 */
	public void setDrawY(int drawY) {
		DrawY = drawY;
	}

	public CompassLayer(MapView view) {
		initLayer(view);
	}

	/**
	 * 构造函数中自定义指南针图标
	 * 
	 * @param view
	 * @param compass
	 */
	public CompassLayer(MapView view, Bitmap compass) {
		initLayer(view);
		bmp = compass;
	}

	@Override
	public void initLayer(MapView view) {
		mMapView = view;

		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setFilterBitmap(true);
		mPaint.setDither(true);
		try {
			bmp = BitmapFactory.decodeStream(mMapView.getContext().getAssets()
					.open("compass.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressLint("DrawAllocation")
	@Override
	public void onDraw(Canvas canvas) {
		if (!Constants.ROTATE || mMapView.getConfig().getDrawMap() == null) {
			return;

		}
		/*
		 * if (mMapView.mapangle==0) { return; }
		 */
		Matrix matrix = new Matrix();
		matrix.postRotate(
				(float) Math.toDegrees(mMapView.mapangle
						+ mMapView.getConfig().getDrawMap().getAngle()),
				bmp.getWidth() / 2, bmp.getHeight() / 2);
		switch (mPosition) {
		case Constants.TOP_LEFT:
			x = DrawX;
			y = DrawY;
			break;
		case Constants.TOP_RIGHT:
			x = mMapView.getWidth() - bmp.getWidth() - DrawX;
			y = DrawY;
			break;
		case Constants.BOTTOM_LEFT:
			x = DrawX;
			y = mMapView.getHeight() - bmp.getHeight() - DrawY;
			break;
		case Constants.BOTTOM_RIGHT:
			x = mMapView.getWidth() - bmp.getWidth() - DrawX;
			y = mMapView.getHeight() - bmp.getHeight() - DrawY;
			break;
		case Constants.COMAPASS_CUSTOM:
			x = DrawX;
			y = DrawY;
			break;
		}
		matrix.postTranslate(x, y);
		canvas.drawBitmap(bmp, matrix, mPaint);
	}

	@Override
	public void destroyLayer() {
		if (bmp != null && !bmp.isRecycled()) {
			bmp.recycle();
		}
	}

	@Override
	public boolean onTap(MotionEvent e) {

		if (!Constants.ROTATE || e.getAction() != MotionEvent.ACTION_UP) {
			return false;
		}

		PointInfo pointOfTouch = new PointInfo(e.getX(), e.getY());
		PointInfo pointOfLocation = new PointInfo(x + bmp.getWidth() / 2, y
				+ bmp.getHeight() / 2);
		if (Math.abs(pointOfLocation.getX() - pointOfTouch.getX()) <= TAP_BOUND
				&& Math.abs(pointOfLocation.getY() - pointOfTouch.getY()) <= TAP_BOUND) {
			mMapView.setLocationMode(RMLocationMode.NORMAL);
			// mMapView.setmInRotateMode(false);
			mMapView.mapangle = 0;

			mMapView.setGetLabels(true);
			mMapView.refreshMap();
			return true;
		}
		return false;
	}

	@Override
	public boolean hasData() {
		return false;
	}

	/**
	 * 得到指南针位置
	 * 
	 * @return
	 */
	public int getPosition() {
		return mPosition;
	}

	/**
	 * 设置指南针位置
	 * 
	 * @param mPosition
	 */
	public void setPosition(int mPosition) {
		this.mPosition = mPosition;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return false;
	}

	/**
	 * 参见父类说明
	 */
	@Deprecated
	@Override
	public void clearLayer() {
		destroyLayer();
	}
}
