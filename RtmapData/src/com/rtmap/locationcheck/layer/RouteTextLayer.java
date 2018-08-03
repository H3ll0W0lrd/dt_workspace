package com.rtmap.locationcheck.layer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Typeface;
import android.view.MotionEvent;

import com.rtm.frm.map.BaseMapLayer;
import com.rtm.frm.map.MapView;
import com.rtm.frm.model.Location;
import com.rtm.frm.model.PointInfo;
import com.rtmap.locationcheck.core.model.RMPoi;
import com.rtmap.locationcheck.util.DTLog;
import com.rtmap.locationcheck.util.DTMathUtils;
import com.rtmap.locationcheck.util.DTStringUtils;

public class RouteTextLayer implements BaseMapLayer {

	public final static int DOOR = 1;// 门
	public final static int ROUTE = 2;// 路线

	private Path mPath;

	private MapView mMapView;
	private Paint mRoutePaint;
	private Paint mTextPaint;
	private OnPointClickListener mClickListener;

	private Bitmap mRouteIcon, mRouteHisIcon, mMarkHisIcon;// 中间点icon
	private Bitmap mMarkIcon;// 标记icon

	private boolean isDraw = true;

	private HashMap<String, ArrayList<RMPoi>> mRouteMap;

	private int mPopupPosition;

	@Deprecated
	public RouteTextLayer(MapView view) {
		mMapView = view;
		mRouteMap = new HashMap<String, ArrayList<RMPoi>>();
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
	public RouteTextLayer(MapView view, Bitmap point, Bitmap markPoint,
			Bitmap historyPoint, Bitmap markHisPoint) {
		mMapView = view;
		mRouteIcon = point;
		mRouteHisIcon = historyPoint;
		mMarkHisIcon = markHisPoint;
		mMarkIcon = markPoint;
		mRouteMap = new HashMap<String, ArrayList<RMPoi>>();
		initLayer(view);
	}

	/**
	 * 添加路线或者门
	 * 
	 * @param key
	 *            每条路线对应一个key,这个key只要保证唯一就行，方便你查找具体路线进行修改（建议使用你生成的文件名做key）
	 * @param points
	 *            本楼层的路线点集合
	 */
	public boolean addRoute(String key, ArrayList<RMPoi> points) {
		if (DTStringUtils.isEmpty(key) || points == null)// key为空返回
			return false;
		mRouteMap.put(key, points);// 添加地图
		return true;
	}

	/**
	 * 移除路线
	 */
	public boolean removeRoute(String key) {
		if (DTStringUtils.isEmpty(key) || !mRouteMap.containsKey(key))// key为空返回
			return false;
		mRouteMap.remove(key);
		return true;
	}

	/**
	 * 清除所有路线
	 */
	public void clearAllRoute() {
		mRouteMap.clear();
	}

	/**
	 * 得到路线集合
	 * 
	 * @param key
	 * @return
	 */
	public ArrayList<RMPoi> getRoute(String key) {
		if (containsKey(key))
			return mRouteMap.get(key);
		else
			return null;
	}

	/**
	 * 是否绘图
	 * 
	 * @param isDraw
	 */
	public void setDraw(boolean isDraw) {
		this.isDraw = isDraw;
	}

	/**
	 * 是否包含这个key，以此说明是否已经添加这条路线
	 * 
	 * @param key
	 * @return
	 */
	public boolean containsKey(String key) {
		return mRouteMap.containsKey(key);
	}

	/**
	 * 某一路线上添加新的点
	 * 
	 * @param key
	 *            每条路线对应一个key,这个key只要保证唯一就行，方便你查找具体路线进行修改（建议使用你生成的文件名做key）
	 * @param point
	 *            本楼层默认添加到路线list最后一项
	 */
	public boolean addRoutePoint(String key, RMPoi point) {
		if (DTStringUtils.isEmpty(key) || point == null)// key为空返回
			return false;
		mRouteMap.get(key).add(point);
		mMapView.refreshMap();
		return true;
	}

	public int getPopupPosition() {
		return mPopupPosition;
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

	private Path makePathDash() {
		Path p = new Path();
		p.moveTo(4, 0);
		p.lineTo(0, -4);
		p.lineTo(8, -4);
		p.lineTo(12, 0);
		p.lineTo(8, 4);
		p.lineTo(0, 4);
		return p;
	}

	@Override
	public void initLayer(MapView view) {
		mRoutePaint = new Paint();
		mRoutePaint.setStyle(Style.STROKE);
		mRoutePaint.setStrokeWidth(3);
		mRoutePaint.setAntiAlias(true);
		mRoutePaint.setStrokeCap(Cap.ROUND);
		mRoutePaint.setStrokeJoin(Join.ROUND);
		mRoutePaint.setColor(Color.BLUE);

		mPath = new Path();

		mTextPaint = new Paint();
		mTextPaint.setTextAlign(Align.CENTER);
		mTextPaint.setColor(Color.BLACK);
		mTextPaint.setAntiAlias(true);
		mTextPaint.setTextSize(24/** (Config.getDensity()+1)/2 */
		);
		mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);

		mPopupPosition = 0;

		// mNodeIcon = Utils.decodeBitmap(view.getContext(),
		// ResourceUtil.getDrawableId(view.getContext(), "icon_node"));

	}

	@Override
	public void onDraw(Canvas canvas) {
		if (!isDraw)
			return;

		mPath.reset();// 重绘路线图
		Iterator<String> keySet = mRouteMap.keySet().iterator();
		while (keySet.hasNext()) {
			String key = keySet.next();
			ArrayList<RMPoi> points = mRouteMap.get(key);
			if (points.size() == 0) {
				continue;
			}
			if (key.contains("door")) {// 门
				for (int i = 0; i < points.size(); i++) {
					RMPoi p = points.get(i);
					PointInfo temppoi = mMapView.fromLocation(new Location(p
							.getX(), p.getY()));
					float pointX = temppoi.getX() - mRouteIcon.getWidth()
							/ 2.0f;// 实际点的x
					float pointY = temppoi.getY() - mRouteIcon.getHeight()
							/ 2.0f;// 实际点的y
					if (key.contains("door_upload")) {
						canvas.drawBitmap(mMarkHisIcon, pointX, pointY, null);// 画门
					} else
						canvas.drawBitmap(mMarkIcon, pointX, pointY, null);// 画门
					canvas.drawText(p.getName(), pointX,
							pointY + mRouteIcon.getWidth() + 20, mTextPaint);// 画文本
				}
			} else if (key.contains("poi")) {// poi
				for (int i = 0; i < points.size(); i++) {
					RMPoi p = points.get(i);
					PointInfo temppoi = mMapView.fromLocation(new Location(p
							.getX(), p.getY()));
					float pointX = temppoi.getX() - mRouteIcon.getWidth()
							/ 2.0f;// 实际点的x
					float pointY = temppoi.getY() - mRouteIcon.getHeight()
							/ 2.0f;// 实际点的y
					if (key.contains("poi_upload")) {
						canvas.drawBitmap(mMarkHisIcon, pointX, pointY, null);// 画门
					} else
						canvas.drawBitmap(mMarkIcon, pointX, pointY, null);// 画门
					canvas.drawText(p.getName(), pointX,
							pointY + mRouteIcon.getWidth() + 20, mTextPaint);// 画文本
				}
			} else {// 路线轮廓
				RMPoi centerPoint = calculateCenter(points);
				drawText(canvas, centerPoint);
				for (int i = 0; i < points.size(); i++) {
					RMPoi p = points.get(i);
					PointInfo temppoi = mMapView.fromLocation(new Location(p
							.getX(), p.getY()));
					float pointX = temppoi.getX() - mRouteIcon.getWidth()
							/ 2.0f;// 实际点的x
					float pointY = temppoi.getY() - mRouteIcon.getHeight()
							/ 2.0f;// 实际点的y
					Bitmap bitmap;
					if (key.contains("_upload")) {
						bitmap = mRouteHisIcon;
					} else {
						bitmap = mRouteIcon;
					}
					if (i != 0) {
						RMPoi p1 = points.get(i - 1);
						PointInfo temppoi1 = mMapView
								.fromLocation(new Location(p1.getX(), p1.getY()));// 转化成地图的XY
						if (temppoi.getX() == temppoi1.getX()
								&& temppoi.getY() == temppoi1.getY())
							continue;
						canvas.drawLine(temppoi1.getX(), temppoi1.getY(),
								temppoi.getX(), temppoi.getY(), mRoutePaint);
						if (i > 0 && i < points.size() - 1)
							canvas.drawBitmap(bitmap, pointX, pointY, null);
						else
							canvas.drawBitmap(bitmap, pointX, pointY, null);
					} else {
						canvas.drawBitmap(bitmap, pointX, pointY, null);
					}
				}
			}
		}
	}

	/**
	 * 画文本
	 * 
	 * @param canvas
	 * @param p
	 */
	private void drawText(Canvas canvas, RMPoi p) {
		if (p == null)
			return;
		PointInfo temppoi = mMapView.fromLocation(new Location(p.getX(), p
				.getY()));
		canvas.drawText(p.getName(), temppoi.getX(), temppoi.getY(), mTextPaint);// 画文本
	}

	/**
	 * 计算中心点
	 * 
	 * @param pointList
	 * @return
	 */
	private synchronized RMPoi calculateCenter(ArrayList<RMPoi> pointList) {
		if (pointList.size() == 1)
			return pointList.get(0);
		float maxY = pointList.get(0).getY();
		float minY = pointList.get(0).getY();
		for (int i = 0; i < pointList.size(); i++) {
			if (maxY < pointList.get(i).getY())
				maxY = pointList.get(i).getY();
			if (minY > pointList.get(i).getY())
				minY = pointList.get(i).getY();
		}
		float y = (maxY + minY) / 2;
		int nCount = pointList.size();
		ArrayList<Float> XList = new ArrayList<Float>();
		for (int j = 0; j < nCount; j++) {
			RMPoi p1 = pointList.get(j);
			RMPoi p2 = pointList.get((j + 1) % nCount);
			// 求解 y=p.y 与 p1p2 的交点
			if (p1.getY() == p2.getY()) // p1p2 与 y=p0.y平行
				continue;
			if (y < Math.min(p1.getY(), p2.getY())) // 交点在p1p2延长线上
				continue;
			if (y >= Math.max(p1.getY(), p2.getY())) // 交点在p1p2延长线上
				continue;
			// 求交点的 X 坐标
			float cx = (y - p1.getY()) * (p2.getX() - p1.getX())
					/ (p2.getY() - p1.getY()) + p1.getX();
			XList.add(cx);
		}
		if (!XList.isEmpty()) {
			double distance = 0;
			int index = 0;
			for (int i = 0; i < XList.size(); i += 2) {
				double temp = Math.abs(XList.get(i) - XList.get(i + 1));
				if (temp > distance) {
					distance = temp;
					index = i;
				}
			}
			float x = (XList.get(index) + XList.get(index + 1)) / 2;
			RMPoi poi = new RMPoi();
			poi.setX(x);
			poi.setY(y);
			poi.setName(pointList.get(0).getName());
			return poi;
		}

		return null;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return false;
	}

	@Override
	public void destroyLayer() {
		clearLayer();
	}

	private float downX, downY;

	@Override
	public boolean onTap(MotionEvent e) {
		if (e.getAction() == MotionEvent.ACTION_DOWN) {
			downX = e.getX();
			downY = e.getY();
		} else if (e.getAction() == MotionEvent.ACTION_UP
				|| e.getAction() == MotionEvent.ACTION_CANCEL) {// 当手指抬起时的
			DTLog.e("e.getX : " + e.getX() + "   e.getY : " + e.getY());
			RMPoi clickPoint = null;
			String key = null;
			float p2p = -1;// 两个点之间的距离
			if (Math.abs(e.getX() - downX) < 20
					&& Math.abs(e.getY() - downY) < 20) {// 如果按下与抬起距离在20像素范围内，可视为点击
				Iterator<String> keySet = mRouteMap.keySet().iterator();
				while (keySet.hasNext()) {
					String str = keySet.next();
					ArrayList<RMPoi> points = mRouteMap.get(str);
					for (int i = 0; i < points.size(); i++) {
						RMPoi p = points.get(i);
						PointInfo temppoi = mMapView.fromLocation(new Location(
								p.getX(), p.getY()));
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
							key = str;
						}
					}
				}
				if (p2p > -1 && mClickListener != null) {// 说明点击在点的范围内
					DTLog.e("点击事件clickPointX : " + clickPoint.getX()
							+ "     Y : " + clickPoint.getY());
					mClickListener.onClick(clickPoint, key);
				}
			}
		}
		return false;
	}

	/**
	 * 清空所有路线list
	 */
	@Override
	public void clearLayer() {
		mRouteMap.clear();
		// mMapView.getTapPOILayer().setDisableTap(false);
		mMapView.popuindex = 0;
	}

	public HashMap<String, ArrayList<RMPoi>> getRouteMap() {
		return mRouteMap;
	}

	public void setRouteMap(HashMap<String, ArrayList<RMPoi>> map) {
		mRouteMap = map;
	}

	/**
	 * 是否有数据
	 */
	@Override
	public boolean hasData() {
		return (mRouteMap != null && mRouteMap.size() != 0);
	}

}
