package com.rtm.frm.map;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.rtm.common.model.POI;
import com.rtm.common.utils.Constants;
import com.rtm.common.utils.RMStringUtils;
import com.rtm.frm.model.Location;
import com.rtm.frm.model.PointInfo;
import com.rtm.frm.vmap.Shape;

public class TapPOILayer implements BaseMapLayer {
	private POI mPOI;
	// private AbstractPOI mCoupon;//linshi

	private MapView mMapView;
	private Paint mPaint;
	private Paint mDividerPaint;
	private boolean mIsDisableTap;

	private OnTapPOIDrawListener mOnTapPOIDrawListener;
	private OnPOITappedListener mOnPOITappedListener;
	private OnPOISelectedListener mOnPOISelectedListener;
	private OnMapTapedListener mOnMapTapedListener;
	private int moveNo;
	private Bitmap mPin;
	private int mTapState;
	private static final int TOUCH_AROUND = 30;// 触摸范围

	private OnMapLongClickListener onMapLongClickListener;

	@SuppressWarnings("unused")
	private int id;

	private boolean istap = false;
	private boolean isselect = false;
	private float motionx;
	private float motiony;
	private boolean drawpin = true;
	private int down_time = 0;
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (actionCode == MotionEvent.ACTION_DOWN
					&& onMapLongClickListener != null && down_time == msg.arg1) {
				PointInfo point = (PointInfo) msg.obj;
				Location location = mMapView.fromPixels(point);
				onMapLongClickListener.onMapLongClick(point, location);
				isLongPress = true;
			}
		}
	};

	public void setDrawpin(boolean drawpin) {
		this.drawpin = drawpin;
	}

	public void setMapMode(boolean mmapMode) {
	}

	public TapPOILayer(MapView view) {
		initLayer(view);
	}

	public void setPOI(POI poi) {
		if (poi != null
				&& !poi.getFloor().equals(mMapView.getConfig().getFloor())) {
			return;
		}
		if (poi == null) {
			mPOI = null;
			return;
		}
		if (mOnPOITappedListener != null) {
			Bitmap mBitmap = mOnPOITappedListener.onPOITapped(poi);
			mPin = mBitmap;

		}
		View mView = null;
		if (mOnTapPOIDrawListener != null) {
			// mMapView.deleteAllView();
			mView = mOnTapPOIDrawListener.onTapPOIDraw(poi);
			if (mView != null)
				mMapView.addchild(mView, poi.getX(), poi.getY_abs());
		}
		mPOI = poi;
		mMapView.refreshMap();
	}

	public void setCoupon(POI poi) {// linshi
		if (!poi.getFloor().equals(mMapView.getConfig().getFloor())) {
			return;
		}
		if (mMapView.getScale() > 800) {
			mMapView.setScale(800);
		}
		mPOI = null;
		// mCoupon=poi;
		mMapView.getConfig().getDrawMap().setSelectPoi(poi);
		mMapView.selectPoi = poi;
	}

	public POI getPOI() {
		return mPOI;
	}

	/**
	 * 设置POI点击监听器，点击POI绘制Bitmap图标，若不绘制，可返回null
	 * @param listener OnPOITappedListener监听POI点击
	 */
	public void setOnPOITappedListener(OnPOITappedListener listener) {
		mOnPOITappedListener = listener;
	}

	public void setOnPOISelectedListener(OnPOISelectedListener listener) {
		mOnPOISelectedListener = listener;
	}

	public void setOnPOIMenuSelectedListener(OnPOIMenuSelectedListener listener) {
	}

	public void setOnPOINavigateSelectedListener(
			OnPOINavigateSelectedListener listener) {
	}

	/**
	 * 点击地图上POI需要绘制View,若只绘制图标请参见setOnPOITappedListener()方法
	 * @param listener OnTapPOIDrawListener监听POI点击，绘制View
	 */
	public void setOnTapPOIDrawListener(OnTapPOIDrawListener listener) {
		mOnTapPOIDrawListener = listener;
	}

	public void removeOnPOISelectedListener() {
		mOnPOISelectedListener = null;
	}

	public void setDisableTap(boolean isDisable) {
		mIsDisableTap = isDisable;
	}

	public boolean isDisableTap() {
		return mIsDisableTap;
	}

	@Override
	public void initLayer(MapView view) {
		mMapView = view;
		setmTapState(Constants.TAP_STATE_NORMAL);
		mPaint = new Paint();
		mPaint.setColor(Color.rgb(0x8C, 0x61, 0x3F));
		mPaint.setAntiAlias(true);
		mPaint.setTextAlign(Align.CENTER);

		mDividerPaint = new Paint();
		mDividerPaint.setColor(0xffB4B5B5);
		mDividerPaint.setAntiAlias(true);

		mIsDisableTap = false;
	}

	@Override
	public void onDraw(Canvas canvas) {

		if (mPOI == null/* &&mCoupon==null */) {
			mMapView.deleteAllView();
			return;
		}

		if (mPOI != null
				&& !mMapView.getConfig().getFloor().equals(mPOI.getFloor())) {
			mMapView.deleteAllView();
			return;
		}
		/*
		 * if (mCoupon !=
		 * null&&!mMapView.mConfig.getFloor().equals(mCoupon.getFloor())) {
		 * return; }
		 */
		if (mPOI.getName() == null) {

		}
		if (mPOI != null && mPOI.getName() != null) {
			drawPOI(canvas);
			return;
		}
		/*
		 * else if(mCoupon != null) { drawCoupon(canvas); return; }
		 */

	}

	public void drawPOI(Canvas canvas) {

		mPaint.setTextSize(MapView.MAPTEXT.getTextsize());
		mPaint.setColor(Color.BLACK);

		if (mPin != null && isDrawpin()
				&& mPOI.getStyle() != Constants.POI_STYLE_PARK) {
			Rect pinRect = new Rect();
			calculatePinRect(mPOI, pinRect);
			canvas.drawBitmap(mPin, null, pinRect, null);
		}
	}

	private void calculatePinRect(POI poi, Rect pinRect) {
		Location location = new Location(poi.getX(), poi.getY_abs());
		PointInfo p = mMapView.fromLocation(location);
		// 更换图标需要手动更改
		int width = (int) mPin.getWidth();
		int height = (int) mPin.getHeight();
		pinRect.left = (int) (p.getX() - width / 2);
		pinRect.top = (int) (p.getY() - height);
		pinRect.right = (int) (p.getX() + width / 2);
		pinRect.bottom = (int) p.getY();
	}

	private long pressStartTime;
	private boolean isLongPress;
	private int actionCode;

	public void onAttach() {
		down_time++;
	}
	@Override
	public boolean onTap(MotionEvent e) {

		if (mMapView.isTapable() == false
				|| mMapView.getConfig().getDrawMap() == null
				|| mMapView.getConfig().getDrawMap().getLayer().shapes == null
				|| mTapState == Constants.TAP_STATE_POPUP_KEEP) {
			return false;
		}
		PointInfo point = new PointInfo(e.getX(), e.getY());
		Rect rect = new Rect();

		actionCode = e.getAction() & MotionEvent.ACTION_MASK;
		if (actionCode == MotionEvent.ACTION_DOWN) {
			istap = true;
			motionx = e.getX();
			motiony = e.getY();
			moveNo = 0;
			pressStartTime = System.currentTimeMillis();
			isLongPress = false;
			down_time++;
			Message msg = handler.obtainMessage();
			msg.obj = point;
			msg.arg1 = down_time;
			handler.sendMessageDelayed(msg, 800);

		}
		if (actionCode == MotionEvent.ACTION_POINTER_DOWN) {
			istap = false;

		}
		if (actionCode == MotionEvent.ACTION_MOVE) {
			moveNo++;
			long time = System.currentTimeMillis();
			if (istap) {
				if (time - pressStartTime > 800) {// 长按
					if (onMapLongClickListener != null
							&& Math.abs(motionx - e.getX()) < TOUCH_AROUND
							&& Math.abs(motiony - e.getY()) < TOUCH_AROUND
							&& !isLongPress) {
						Location location = mMapView.fromPixels(point);
						onMapLongClickListener.onMapLongClick(point, location);
						isLongPress = true;
						return true;
					}
				} else {
					if (Math.abs(motionx - e.getX()) > TOUCH_AROUND
							|| Math.abs(motiony - e.getY()) > TOUCH_AROUND) {
						isLongPress = true;
					}
				}
			}
		}

		if (actionCode == MotionEvent.ACTION_UP) {
			if (isLongPress) {
				return false;
			}
			long time = System.currentTimeMillis();
			if (mOnMapTapedListener != null && time - pressStartTime <= 800) {
				mOnMapTapedListener.OnMapTaped(mMapView.fromPixels(e.getX(),
						e.getY()));
			}
			// mMapView.deleteAllView();

			if (mPOI != null
					&& mPOI.getName() != null
					&& mPOI.getFloor().equalsIgnoreCase(
							mMapView.getConfig().getFloor())) {
				if (mOnPOISelectedListener != null
						&& rect.contains((int) point.getX(), (int) point.getY())
						&& moveNo < 10 && istap
						&& Math.abs(motionx - e.getX()) < TOUCH_AROUND
						&& Math.abs(motiony - e.getY()) < TOUCH_AROUND) {
					mOnPOISelectedListener.onPOISelected(mPOI);
					isselect = true;
					return true;
				}
			}

			if (isselect) {
				isselect = false;
				return false;
			}
			if (istap) {
				if (Math.abs(motionx - e.getX()) > TOUCH_AROUND
						|| Math.abs(motiony - e.getY()) > TOUCH_AROUND
						|| moveNo > 20) {
					return false;
				}

				if (mIsDisableTap) {
					return false;
				}
				Shape[] shapes = mMapView.getConfig().getDrawMap()
						.getLayer().shapes;
				for (int i = 0; i < shapes.length; i++) {

					Shape mShape = shapes[i];
					if (RMStringUtils.isEmpty(mShape.mName)) {
						continue;
					}

					Location mLocation = mMapView.fromPixels(point);
					if (mMapView
							.getConfig()
							.getDrawMap()
							.getLayer()
							.inPolygon(mShape, mLocation.getX() * 1000,
									mLocation.getY() * 1000)) {
						POI poi = new POI(mShape.mId, mShape.mName, mMapView
								.getConfig().getBuildId(), mMapView.getConfig()
								.getFloor(), mShape.mCenter.mX / 1000f,
								mShape.mCenter.mY / 1000f);
						poi.setType(mShape.mType);
						mMapView.setfling(false);
						this.setPOI(poi);

						setDrawpin(true);
						return true;
					}
				}
				if (mTapState == Constants.TAP_STATE_NORMAL) {
					mMapView.getConfig().getDrawMap().setSelectPoi(null);
					mMapView.selectPoi = null;
					destroyLayer();
					mMapView.refreshMap();
					return false;
				} else if (mTapState == Constants.TAP_STATE_PIN_KEEP) {
					mMapView.deleteAllView();
					return false;
				}

			}
		}
		return false;
	}

	/**
	 * 设置地图长按事件的监听器
	 * 
	 * @param onMapLongClickListener
	 */
	public void setOnMapLongClickListener(
			OnMapLongClickListener onMapLongClickListener) {
		this.onMapLongClickListener = onMapLongClickListener;
	}

	/**
	 * 地图长按监听器
	 * 
	 * @author dingtao
	 *
	 */
	public interface OnMapLongClickListener {
		/**
		 * 长按触发后的回调方法
		 * 
		 * @param eventX
		 * @param eventY
		 * @param coordX
		 * @param coordY
		 */
		public void onMapLongClick(PointInfo point, Location location);
	}

	@Override
	public void destroyLayer() {
		mPOI = null;
		mIsDisableTap = false;
		mMapView.popuindex = 0;
	}

	@Override
	public boolean hasData() {
		return (mPOI != null);
	}

	public boolean isDrawpin() {
		return drawpin;
	}

	public int getmTapState() {
		return mTapState;
	}

	public void setmTapState(int mTapState) {
		this.mTapState = mTapState;
	}

	public void setOnMapTapedListener(OnMapTapedListener mOnMapTapedListener) {
		this.mOnMapTapedListener = mOnMapTapedListener;
	}

	public interface OnPOITappedListener {
		public Bitmap onPOITapped(POI poi);
	}

	public interface OnPOISelectedListener {
		public void onPOISelected(POI poi);
	}

	public interface OnMapTapedListener {
		public void OnMapTaped(Location location);
	}

	public interface OnPOIMenuSelectedListener {
		public void onPOIMenuSelected(POI poi);
	}

	public interface OnPOINavigateSelectedListener {
		public void onPOINavigateSelected(POI poi);
	}

	public interface OnTapPOIDrawListener {
		public View onTapPOIDraw(POI poi);
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
