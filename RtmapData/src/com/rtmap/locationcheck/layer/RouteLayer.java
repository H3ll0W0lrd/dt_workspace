package com.rtmap.locationcheck.layer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
import android.graphics.PathEffect;
import android.view.MotionEvent;

import com.rtm.frm.map.BaseMapLayer;
import com.rtm.frm.map.MapView;
import com.rtm.frm.model.Location;
import com.rtm.frm.model.PointInfo;
import com.rtmap.locationcheck.core.model.LCPoint;
import com.rtmap.locationcheck.util.DTLog;
import com.rtmap.locationcheck.util.DTMathUtils;
import com.rtmap.locationcheck.util.DTStringUtils;

public class RouteLayer implements BaseMapLayer {

	private MapView mMapView;
	private Paint mRoutePaint;
	private OnBeaconClickListener mClickListener;

	private Bitmap mPointIcon;// 中间点icon
	private Bitmap mStartIcon;// 开始icon
	private Bitmap mEndIcon;// 结束icon

	private HashMap<String, ArrayList<LCPoint>> mRouteMap;

	@Deprecated
	public RouteLayer(MapView view) {
		mMapView = view;
		mRouteMap = new HashMap<String, ArrayList<LCPoint>>();
		initLayer(view);
	}
	/**
	 * 设置所有路线
	 * @param map
	 */
	public void setRouteMap(HashMap<String, ArrayList<LCPoint>> map) {
		if(map!=null)
			mRouteMap = map;
	}
	
	/**
	 * 得到所有路线
	 * @return
	 */
	public HashMap<String, ArrayList<LCPoint>> getRouteMap() {
		return mRouteMap;
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
	public RouteLayer(MapView view, Bitmap start, Bitmap end, Bitmap mark) {
		mMapView = view;
		mStartIcon = start;
		mEndIcon = end;
		mPointIcon = mark;
		mRouteMap = new HashMap<String, ArrayList<LCPoint>>();
		initLayer(view);
	}

	/**
	 * 添加路线
	 * 
	 * @param key
	 *            每条路线对应一个key,这个key只要保证唯一就行，方便你查找具体路线进行修改（建议使用你生成的文件名做key）
	 * @param points
	 *            本楼层的路线点集合
	 */
	public boolean addRoute(String key, ArrayList<LCPoint> points) {
		if (DTStringUtils.isEmpty(key) || points == null)// key为空返回
			return false;
		mRouteMap.put(key, points);// 添加地图
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
	public ArrayList<LCPoint> getRoute(String key) {
		if (containsKey(key))
			return mRouteMap.get(key);
		else
			return null;
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
	 * 移除某一条路线
	 * @param key
	 */
	public void removeRoute(String key) {
		if(containsKey(key)){
			mRouteMap.remove(key);
		}
	}

	/**
	 * 某一路线上添加新的点
	 * 
	 * @param key
	 *            每条路线对应一个key,这个key只要保证唯一就行，方便你查找具体路线进行修改（建议使用你生成的文件名做key）
	 * @param point
	 *            本楼层默认添加到路线list最后一项
	 */
	public boolean addRoutePoint(String key, LCPoint point) {
		if (DTStringUtils.isEmpty(key) || point == null)// key为空返回
			return false;
		mRouteMap.get(key).add(point);
		return true;
	}

	/**
	 * 设置点的监听器
	 * 
	 * @param mClickListener
	 */
	public void setOnPointClickListener(OnBeaconClickListener mClickListener) {
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
		PathEffect peArray = new PathDashPathEffect(makePathDash(), 18, 0,
				PathDashPathEffect.Style.ROTATE);
		mRoutePaint.setPathEffect(peArray);
	}
	
	private boolean isDraw=true;//是否画
	public void setDraw(boolean isDraw) {
		this.isDraw = isDraw;
	}
	
	public void setRouteColor(int color) {
		mRoutePaint.setColor(color);
	}

	@Override
	public void onDraw(Canvas canvas) {
		if(!isDraw)
			return;
		Iterator<String> keySet = mRouteMap.keySet().iterator();
		while (keySet.hasNext()) {
			ArrayList<LCPoint> points = mRouteMap.get(keySet.next());
			for (int i = 0; i < points.size(); i++) {
				LCPoint p = points.get(i);
				PointInfo temppoi = mMapView.fromLocation(new Location(
						p.getX() / 1000.0f, p.getY() / 1000.0f));

				float pointX = temppoi.getX() - mStartIcon.getWidth() / 2.0f;// 实际点的x
				float pointY = temppoi.getY() - mStartIcon.getHeight() / 2.0f;// 实际点的y
				if (i != 0) {
					LCPoint p1 = points.get(i - 1);
					PointInfo temppoi1 = mMapView.fromLocation(new Location(p1
							.getX() / 1000.0f, p1.getY() / 1000.0f));
					if (temppoi.getX() == temppoi1.getX()
							&& temppoi.getY() == temppoi1.getY())
						continue;
					canvas.drawLine(temppoi1.getX(), temppoi1.getY(),
							temppoi.getX(), temppoi.getY(), mRoutePaint);
					if (i > 0 && i < points.size() - 1)
						canvas.drawBitmap(mPointIcon, pointX, pointY, null);
					else
						canvas.drawBitmap(mEndIcon, pointX, pointY, null);
				} else {
					canvas.drawBitmap(mStartIcon, pointX, pointY, null);
				}
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return false;
	}

	@Override
	public void destroyLayer() {
		mRouteMap.clear();
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
			DTLog.e("e.getX : " + e.getX() + "   e.getY : " + e.getY());
			LCPoint clickPoint = null;
			String key = null;
			float p2p = -1;// 两个点之间的距离
			if (Math.abs(e.getX() - downX) < 20
					&& Math.abs(e.getY() - downY) < 20) {// 如果按下与抬起距离在20像素范围内，可视为点击
				Iterator<String> keySet = mRouteMap.keySet().iterator();
				while (keySet.hasNext()) {
					String str = keySet.next();
					ArrayList<LCPoint> points = mRouteMap.get(str);
					for (int i = 0; i < points.size(); i++) {
						LCPoint p = points.get(i);
						PointInfo temppoi = mMapView.fromLocation(new Location(p
								.getX() / 1000.0f, p.getY() / 1000.0f));
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
					DTLog.e("点击事件");
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
		return (mRouteMap != null && mRouteMap.size() != 0);
	}
	@Override
	public void clearLayer() {
		// TODO Auto-generated method stub
		
	}
}
