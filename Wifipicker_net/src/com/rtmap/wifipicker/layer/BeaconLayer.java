package com.rtmap.wifipicker.layer;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.view.MotionEvent;

import com.rtm.frm.map.BaseMapLayer;
import com.rtm.frm.map.MapView;
import com.rtm.frm.model.Location;
import com.rtm.frm.model.PointInfo;
import com.rtmap.wifipicker.core.model.RMPoi;

public class BeaconLayer implements BaseMapLayer {

	private MapView mMapView;
	private Paint mBeaconPaint;
	private Paint mTextPaint;
	private OnPointClickListener mClickListener;

	private Bitmap mIconMap;// 中间点icon

	private ArrayList<RMPoi> mPointList;

	@Deprecated
	public BeaconLayer(MapView view) {
		mMapView = view;
		mPointList = new ArrayList<RMPoi>();
		initLayer(view);
	}

	/**
	 * 构造方法
	 * 
	 * @param view
	 *            MapView
	 * @param start
	 *            开始图标
	 * @param end
	 *            结束图标
	 * @param mark
	 *            中间点标记图标
	 */
	public BeaconLayer(MapView view, Bitmap statusBitmap) {
		mMapView = view;
		mIconMap = statusBitmap;
		mPointList = new ArrayList<RMPoi>();
		initLayer(view);
	}

	/**
	 * 添加POI点集合
	 * 
	 * @param points
	 *            本楼层的POI点集合
	 */
	public boolean addPointList(List<RMPoi> points) {
		if (points == null || points.size() == 0)// key为空返回
			return false;
		mPointList.addAll(points);// 添加地图
		return true;
	}

	/**
	 * 添加POI点
	 * 
	 * @param point
	 *            本楼层的POI点
	 */
	public boolean addPoint(RMPoi point) {
		if (point == null)// key为空返回
			return false;
		mPointList.add(point);// 添加地图
		return true;
	}

	/**
	 * 得到Point
	 * 
	 * @param i
	 * @return
	 */
	public RMPoi getPoint(int i) {
		if (i > mPointList.size() - 1)
			return null;
		return mPointList.get(i);
	}

	public ArrayList<RMPoi> getPointList() {
		return mPointList;
	}

	/**
	 * 得到点的数量
	 * 
	 * @return
	 */
	public int getPointCount() {
		return mPointList.size();
	}

	/**
	 * 移除点
	 * 
	 * @param index
	 */
	public void clearPoint(int index) {
		if (index > mPointList.size() - 1)
			return;
		mPointList.remove(index);
	}

	/**
	 * 移除点
	 * 
	 * @param point
	 */
	public void clearPoint(RMPoi point) {
		if (mPointList.contains(point))
			mPointList.remove(point);
	}

	/**
	 * 清除所有点
	 */
	public void clearAllPoints() {
		mPointList.clear();
	}

	public OnPointClickListener getOnPointClickListener() {
		return mClickListener;
	}

	/**
	 * 设置点的监听器
	 * 
	 * @param mClickListener
	 */
	public void setOnPointClickListener(OnPointClickListener mClickListener) {
		this.mClickListener = mClickListener;
	}

	@Override
	public void initLayer(MapView view) {

		mTextPaint = new Paint();
		mTextPaint.setTextAlign(Align.CENTER);
		mTextPaint.setColor(Color.BLACK);
		mTextPaint.setAntiAlias(true);
		mTextPaint.setTextSize(30); // (Config.getDensity()+1)/2

		mBeaconPaint = new Paint();
		isVisibility = true;
	}

	private boolean isVisibility;

	/**
	 * 是否显示name
	 * 
	 * @param visibility
	 */
	public void setVisibility(boolean visibility) {
		isVisibility = visibility;
	}

	@Override
	public void onDraw(Canvas canvas) {
		for (int i = 0; i < mPointList.size(); i++) {
			RMPoi p = mPointList.get(i);
			PointInfo temppoi = mMapView.fromLocation(new Location(
					p.getX() / 1000.0f, p.getY() / 1000.0f));
			float pointX = temppoi.getX() - mIconMap.getWidth() / 2.0f;// 实际点的x
			float pointY = temppoi.getY() - mIconMap.getHeight() / 2.0f;// 实际点的y
			if (pointX < 0 || pointY < 0) {
				continue;
			}
			if (isVisibility)
				canvas.drawBitmap(mIconMap, pointX, pointY, mBeaconPaint);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return false;
	}

	@Override
	public void destroyLayer() {

	}

	@Override
	public boolean onTap(MotionEvent e) {
		return false;
	}

	/**
	 * 清空所有路线list
	 */
	@Override
	public void clearLayer() {
		mPointList.clear();
		// mMapView.getTapPOILayer().setDisableTap(false);
		mMapView.popuindex = 0;
	}

	/**
	 * 是否有数据
	 */
	@Override
	public boolean hasData() {
		return (mPointList != null && mPointList.size() != 0);
	}

	public static interface OnPopupPositionChangedListener {
		public void onPopupPositionChanged(int position);
	}

	public static interface OnFloorChangedListener {
		public void onFloorChanged(String floor);
	}

	public static interface OnIsEndListener {
		public void onisend();
	}

}
