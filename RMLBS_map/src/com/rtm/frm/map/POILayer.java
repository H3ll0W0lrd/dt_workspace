package com.rtm.frm.map;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.view.MotionEvent;

import com.rtm.common.model.POI;
import com.rtm.frm.model.Location;
import com.rtm.frm.model.PointInfo;

/**
 * 显示POI的图层
 * 
 * @author dingtao
 *
 */
public class POILayer implements BaseMapLayer {

	// private int mCenter;
	private ArrayList<POI> mPOIs;
	private MapView mMapView;
	private Paint mPaint;
	private OnPOIDrawListener mOnPOIDrawListener;
	private Bitmap mPin;

	/**
	 * 构造方法
	 * 
	 * @param view
	 *            地图MaView
	 */
	public POILayer(MapView view) {
		initLayer(view);
	}

	/**
	 * 设置POI图标
	 * 
	 * @param bitmap
	 * @since
	 */
	public void setPoiIcon(Bitmap bitmap) {
		mPin = bitmap;
	}

	/**
	 * 设置POI图标监听器，添加POI后，会使用这个回调方法设置图标，这种糟糕的设置方式不建议使用，我们可能在后几个版本去掉此方法，
	 * 请使用新方法setPoiIcon(Bitmap bitmap)
	 * 
	 * @param listener
	 *            回调方法，不建议使用
	 */
	@Deprecated
	public void setOnPOIDrawListener(OnPOIDrawListener listener) {
		mOnPOIDrawListener = listener;
	}

	/**
	 * 构造方法
	 * 
	 * @param view
	 *            MapView类型
	 * @param icon
	 *            POI图标
	 */
	public POILayer(MapView view, Bitmap icon) {
		initLayer(view);
		mPin = icon;
	}

	/**
	 * 添加POi
	 * 
	 * @param poi
	 *            POI类型
	 */
	public void addPoi(POI poi) {
		if (poi != null) {
			mPOIs.add(poi);
		}
	}

	/**
	 * 添加要标记的POI点
	 * 
	 * @param pois
	 *            POI列表
	 */
	public void addPoiList(ArrayList<POI> pois) {
		if (pois == null || pois.size() == 0)
			return;
		mPOIs.addAll(pois);
	}

	/**
	 * 得到POI列表
	 * 
	 * @return 所有你已经添加的POI列表
	 */
	public ArrayList<POI> getPoiList() {
		return mPOIs;
	}

	/**
	 * 初始化数据
	 * 
	 * @param view
	 *            MapView类型
	 */
	@Override
	public void initLayer(MapView view) {
		mMapView = view;
		mPOIs = new ArrayList<POI>();
		mPaint = new Paint();
		mPaint.setColor(Color.rgb(0x8C, 0x61, 0x3F));
		mPaint.setAntiAlias(true);
		mPaint.setTextAlign(Align.CENTER);

	}

	@Override
	public void onDraw(Canvas canvas) {
		if (mPOIs == null || mPOIs.size() == 0) {
			return;
		}
		draw2D(canvas);
		return;
	}

	/**
	 * 根据POI的id移除POI，id可通过POI.getPoiNo()得到
	 * 
	 * @param id
	 *            POI的id
	 */
	public void removePoi(int id) {
		if (mPOIs == null || mPOIs.size() == 0) {
			return;
		}
		for (int i = 0; i < mPOIs.size(); i++) {
			POI poi = mPOIs.get(i);
			if (poi.getPoiNo() == id) {
				mPOIs.remove(i);
				return;
			}
		}
	}

	/**
	 * 移除POI
	 * 
	 * @param poi
	 *            POI类型
	 */
	public void removePoi(POI poi) {
		if (poi != null && mPOIs.contains(poi)) {
			mPOIs.remove(poi);
		}
	}

	private void draw2D(Canvas canvas) {
		int size = mPOIs.size();
		for (int i = 0; i < size; i++) {
			POI poi = mPOIs.get(i);
			if (mOnPOIDrawListener != null) {
				mPin = mOnPOIDrawListener.onPOIDraw(poi);
			}

			if (mPin != null) {
				if (mMapView.getBuildId().equals(poi.getBuildId())
						&& mMapView.getFloor().equals(poi.getFloor())) {
					Location location = new Location(poi.getX(), poi.getY_abs());
					PointInfo p = mMapView.fromLocation(location);
					canvas.drawBitmap(mPin, p.getX() - mPin.getWidth() / 2,
							p.getY() - mPin.getHeight(), null);
				}
			}
		}
	}

	@Override
	public void destroyLayer() {
		if (mPOIs != null) {
			mPOIs.clear();
		}
	}

	@Override
	public boolean onTap(MotionEvent e) {
		// PointInfo point = new PointInfo(e.getX(), e.getY());
		// if (mPOIs == null || mPOIs.size() == 0) {
		// return false;
		// }
		//
		// if (e.getAction() != MotionEvent.ACTION_UP) {
		// return false;
		// }
		//
		// int size = mPOIs.size();
		// if (mPin != null) {
		// for (int i = 0; i < size; i++) {
		// POI poi = mPOIs.get(i);
		// if (poi == null || RMStringUtils.isEmpty(poi.getFloor())
		// || !poi.getFloor().equals(mMapView.mConfig.getFloor())) {
		// continue;
		// }
		//
		// Rect pinRect = new Rect();
		// calculatePinRect(poi, pinRect);
		//
		// if (pinRect.contains((int) point.getX(), (int) point.getY())) {
		// mCenter = i;
		// if (mMapView.getTapPOILayer() != null) {
		// mMapView.getTapPOILayer().setPOI(mPOIs.get(mCenter));
		// mMapView.getTapPOILayer().setDrawpin(true);
		// }
		// return true;
		// }
		// }
		// }
		return false;
	}

	@Override
	public boolean hasData() {
		return (mPOIs != null && mPOIs.size() != 0);
	}

	/**
	 * 此方法用于设置POI图标，不建议使用，我们将在后续版本中去掉，请使用setPoiIcon(Bitmap bitmap)
	 * 
	 * @author dingtao
	 *
	 */
	@Deprecated
	public static interface OnPOIDrawListener {
		public Bitmap onPOIDraw(POI poi);
	}

	/**
	 * 参见父类中此方法解释
	 */
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
