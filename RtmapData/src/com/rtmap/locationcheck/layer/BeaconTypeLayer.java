package com.rtmap.locationcheck.layer;

import java.util.ArrayList;
import java.util.HashMap;
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
import com.rtmap.locationcheck.core.model.BeaconInfo;
import com.rtmap.locationcheck.util.DTMathUtils;
import com.rtmap.locationcheck.util.DTStringUtils;

public class BeaconTypeLayer implements BaseMapLayer {

	private MapView mMapView;
	private Paint mBeaconPaint;
	private Paint mTextPaint;
	private OnBeaconClickListener mClickListener;

	private HashMap<Integer, Bitmap> mIconMap;// 中间点icon
	private Bitmap mClickPointIcon;// 中间点icon

	private ArrayList<BeaconInfo> mPointList;

	public BeaconTypeLayer(MapView view) {
		mMapView = view;
		mPointList = new ArrayList<BeaconInfo>();
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
	public BeaconTypeLayer(MapView view, HashMap<Integer, Bitmap> statusBitmap) {
		mMapView = view;
		mIconMap = statusBitmap;
		mPointList = new ArrayList<BeaconInfo>();
		initLayer(view);
	}

	/**
	 * 设置点击点得颜色
	 * 
	 * @param clickBmp
	 */
	public void setClickPoint(Bitmap clickBmp) {
		mClickPointIcon = clickBmp;
	}

	/**
	 * 添加POI点集合
	 * 
	 * @param points
	 *            本楼层的POI点集合
	 */
	public boolean addPointList(List<BeaconInfo> points) {
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
	public boolean addPoint(BeaconInfo point) {
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
	public BeaconInfo getPoint(int i) {
		if (i > mPointList.size() - 1)
			return null;
		return mPointList.get(i);
	}

	public ArrayList<BeaconInfo> getPointList() {
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
	public void clearPoint(BeaconInfo point) {
		if (mPointList.contains(point))
			mPointList.remove(point);
	}

	/**
	 * 清除所有点
	 */
	public void clearAllPoints() {
		mPointList.clear();
	}

	public OnBeaconClickListener getOnPointClickListener() {
		return mClickListener;
	}

	/**
	 * 设置点的监听器
	 * 
	 * @param mClickListener
	 */
	public void setOnPointClickListener(OnBeaconClickListener mClickListener) {
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
	public void onDraw(Canvas canvas) {
		for (int i = 0; i < mPointList.size(); i++) {
			BeaconInfo p = mPointList.get(i);
			PointInfo temppoi = mMapView.fromLocation(new Location(
					p.getX() / 1000.0f, p.getY() / 1000.0f));
			float pointX = temppoi.getX() - mIconMap.get(0).getWidth() / 2.0f;// 实际点的x
			float pointY = temppoi.getY() - mIconMap.get(0).getHeight() / 2.0f;// 实际点的y
			if (pointX < 0 || pointY < 0) {
				continue;
			}
			// 工作状态：0正常，-1低电量，-2故障，-3缺失，-4未知
			// 编辑状态：0正常，1删除，2新建，3修改
			// 颜色：正常，修改，删除，未知
			if (p.getMac().startsWith("C91A")) {
				canvas.drawBitmap(mIconMap.get(0), pointX, pointY, mBeaconPaint);
			} else {
				canvas.drawBitmap(mIconMap.get(1), pointX, pointY, mBeaconPaint);
			}
			if (!DTStringUtils.isEmpty(p.getName()) && isVisibility) {
				canvas.drawText(p.getName(), pointX, pointY
						+ mIconMap.get(0).getWidth() + 20, mTextPaint);
			}

			if (mClickPointIcon != null && p.isClick()) {
				float clickX = temppoi.getX() - mClickPointIcon.getWidth()
						/ 2.0f;// 实际点的x
				float clickY = temppoi.getY() - mClickPointIcon.getHeight();// 实际点的y
				canvas.drawBitmap(mClickPointIcon, clickX, clickY, mBeaconPaint);// 画出点
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return false;
	}

	@Override
	public void destroyLayer() {
		mPointList.clear();
		mMapView.popuindex = 0;
	}

	private float downX, downY;

	@Override
	public boolean onTap(MotionEvent e) {
		if (e.getAction() == MotionEvent.ACTION_DOWN) {
			downX = e.getX();
			downY = e.getY();
		} else if (e.getAction() == MotionEvent.ACTION_UP
				|| e.getAction() == MotionEvent.ACTION_CANCEL) {// 当手指抬起时的
			BeaconInfo clickPoint = null;
			String key = null;
			float p2p = -1;// 触点与地图上的点两个点之间的距离
			if (Math.abs(e.getX() - downX) < 20
					&& Math.abs(e.getY() - downY) < 20) {// 如果按下与抬起距离在20像素范围内，可视为点击
				for (int i = 0; i < mPointList.size(); i++) {
					BeaconInfo p = mPointList.get(i);
					p.setClick(false);
					PointInfo temppoi = mMapView.fromLocation(new Location(
							p.getX() / 1000.0f, p.getY() / 1000.0f));
					// DTLog.e("e.getX : " + e.getX() +"   "+temppoi.getX()+
					// "   e.getY : " + e.getY()+"   "+temppoi.getY());
					if (temppoi.getX() < 0 || temppoi.getY() < 0)// 屏幕外的不用计算
						continue;
					float reduceX = Math.abs(temppoi.getX() - e.getX());
					float reduceY = Math.abs(temppoi.getY() - e.getY());
					if (reduceX > 20 || reduceY > 20)// 超出手指同一水平线范围
						continue;
					float dis = DTMathUtils.distance(e.getX(), e.getY(),
							temppoi.getX(), temppoi.getY());// 计算两点之间的距离
					if (p2p < 0 || p2p > dis) {// 距离比他大
						clickPoint = p;// 保存距离范围内的点
						p2p = dis;
					}
				}
				if (p2p > -1 && mClickListener != null) {// 说明点击在点的范围内
					mClickListener.onBeaconClick(clickPoint, key);
				}
			}
		}
		return false;
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
	@Override
	public void clearLayer() {
		// TODO Auto-generated method stub
		
	}
}
