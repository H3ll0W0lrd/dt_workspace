package com.rtm.frm.map;

import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
import android.graphics.PathEffect;
import android.graphics.Typeface;
import android.graphics.drawable.NinePatchDrawable;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import com.rtm.common.model.POI;
import com.rtm.common.model.RMLocation;
import com.rtm.common.utils.Constants;
import com.rtm.frm.model.Location;
import com.rtm.frm.model.NavigatePoint;
import com.rtm.frm.model.PointInfo;
import com.rtm.frm.model.RMRoute;
import com.rtm.frm.utils.Handlerlist;
import com.rtm.frm.utils.RMNavigationUtil;
import com.rtm.frm.utils.RMathUtils;
import com.rtm.frm.utils.RMNavigationUtil.OnNavigationListener;

/**
 * 导航路线图层
 * 
 * @author dingtao
 *
 */
public class RouteLayer implements BaseMapLayer {

	/**
	 * 导航标记
	 */
	public static final int NAVIGATE = 200;
	/**
	 * 开始规划路线
	 */
	public static final int PLAN_ROUTE_START = 1;
	/**
	 * 规划路线请求服务器错误
	 */
	public static final int PLAN_ROUTE_ERROR = 2;

	/**
	 * 开始导航
	 */
	public static final int NAVIGATE_START = 3;

	/**
	 * 导航关键节点
	 */
	public static final int NAVIGATE_CURRENT_POINT = 8;

	/**
	 * 结束导航
	 */
	public static final int NAVIGATE_STOP = 4;

	/**
	 * 到达终点
	 */
	public static final int ARRIVED = 5;

	/**
	 * 已经偏离路线，重新规划
	 */
	public static final int REPLAN_ROUTE_START = 6;

	/**
	 * 导航无法开启
	 */
	public static final int NAVIGATE_FAIL = 7;

	private Path mFloorPath, mOtherPath, mKeyFloorPath;
	private ArrayList<NavigatePoint> mRoutePointList;

	private ArrayList<NavigatePoint> mNavigateRoutePointList;

	private MapView mMapView;
	private Paint mRoutePaint;
	private Paint mRoutePaint2;

	private static final int DISTANCE_LIMIT = 4;// 距离限制默认4米
	private static final int COUNT_LIMIT = 6;// 偏移次数默认6次
	private static final int DISTANCE_END = 10;// 结束导航的距离
	private int mLimitCount = 0;// 偏移次数
	/**
	 * 关键路线高亮
	 */
	private Paint mKeyRoutePaint;
	/**
	 * 高亮路线开始的点下标和结束的点下标
	 */
	private int mKeyStart, mKeyEnd;
	private Paint mTextPaint;
	private boolean mShowOtherFloor;

	/**
	 * 是否显示其他楼层的路线，默认为：true显示
	 * 
	 * @param show
	 *            是否显示
	 */
	public void setShowOtherFloor(boolean show) {
		mShowOtherFloor = show;
	}

	private Bitmap mPin;

	private Bitmap mStartIcon;
	private Bitmap mEndIcon;
	private float mDistance;

	private boolean isNavigating;// 是否正在导航

	public RouteLayer(MapView view) {
		mMapView = view;
		initLayer(view);
	}

	public RouteLayer(MapView view, Bitmap start, Bitmap end, Bitmap mark) {
		mMapView = view;
		mStartIcon = start;
		mEndIcon = end;
		mPin = mark;
		initLayer(view);
	}

	/**
	 * 设置导航路线
	 * 
	 * @param points
	 */
	public void setNavigatePoints(ArrayList<NavigatePoint> points) {
		mKeyEnd = 0;
		mKeyStart = 0;
		mRoutePointList.clear();
		mRoutePointList.addAll(points);
		mNavigateRoutePointList.clear();
		mNavigateRoutePointList.addAll(points);
	}

	public void setDistance(float distance) {
		mDistance = distance;
	}

	public float getDistance() {
		return mDistance;
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
		mRoutePointList = new ArrayList<NavigatePoint>();
		mNavigateRoutePointList = new ArrayList<NavigatePoint>();
		mRoutePaint = new Paint();
		mRoutePaint.setStyle(Style.STROKE);
		mRoutePaint.setStrokeWidth(10);
		mRoutePaint.setAntiAlias(true);
		mRoutePaint.setStrokeCap(Cap.ROUND);
		mRoutePaint.setStrokeJoin(Join.ROUND);
		mRoutePaint.setColor(0x960079C6);
		PathEffect peArray = new PathDashPathEffect(makePathDash(), // 形状
				18, // 间距
				0,// 首绘制偏移量
				PathDashPathEffect.Style.ROTATE);
		mRoutePaint.setPathEffect(peArray);

		mRoutePaint2 = new Paint();
		mRoutePaint2.setStyle(Style.STROKE);
		mRoutePaint2.setStrokeWidth(10);
		mRoutePaint2.setAntiAlias(true);
		mRoutePaint2.setStrokeCap(Cap.ROUND);
		mRoutePaint2.setStrokeJoin(Join.ROUND);
		mRoutePaint2.setColor(Color.rgb(0, 0, 255));
		mRoutePaint2.setPathEffect(peArray);
		mRoutePaint2.setAlpha(50);

		mKeyRoutePaint = new Paint();
		mKeyRoutePaint.setStyle(Style.STROKE);
		mKeyRoutePaint.setStrokeWidth(10);
		mKeyRoutePaint.setAntiAlias(true);
		mKeyRoutePaint.setStrokeCap(Cap.ROUND);
		mKeyRoutePaint.setStrokeJoin(Join.ROUND);
		mKeyRoutePaint.setColor(0xfffd0101);

		mFloorPath = new Path();
		mOtherPath = new Path();
		mKeyFloorPath = new Path();

		mTextPaint = new Paint();
		mTextPaint.setTextAlign(Align.CENTER);
		mTextPaint.setColor(Color.BLACK);
		mTextPaint.setAntiAlias(true);
		mTextPaint.setTextSize(24/** (Config.getDensity()+1)/2 */
		);
		mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);

	}

	public ArrayList<NavigatePoint> getNavigatePoints() {
		return mRoutePointList;
	}

	public ArrayList<NavigatePoint> getNavigateRoutePoints() {
		return mNavigateRoutePointList;
	}

	/**
	 * 是否正在导航
	 * 
	 * @return
	 */
	public boolean isNavigating() {
		return isNavigating;
	}

	/**
	 * 得到当前关键节点
	 */
	public NavigatePoint getCurrentKeyPoint() {
		for (int i = 0; i < mNavigateRoutePointList.size(); i++) {
			NavigatePoint p = mNavigateRoutePointList.get(i);
			if (p.isImportant()) {// 如果是关键节点
				return p;
			}
		}
		return null;
	}

	/**
	 * 规划导航后自动开始导航
	 */
	public void planNavigate(RMLocation location, POI end) {
		if (location == null || location.getError() != 0 || end == null) {
			isNavigating = true;
			POI start = new POI(1, "我的位置", location.getBuildID(),
					location.getFloor(), location.x, location.y);
			Handlerlist.getInstance().notifications(NAVIGATE, PLAN_ROUTE_START,
					null);
			restartNavigate(start, end);
		} else {
			Handlerlist.getInstance().notifications(NAVIGATE, NAVIGATE_FAIL,
					null);
		}
	}

	public void startNavigate() {
		if (mNavigateRoutePointList.size() != 0) {
			Handlerlist.getInstance().notifications(NAVIGATE, NAVIGATE_START,
					null);
			isNavigating = true;
			mMapView.setLocationMode(RMLocationMode.COMPASS);
		} else {
			Handlerlist.getInstance().notifications(NAVIGATE, NAVIGATE_FAIL,
					null);
		}
	}

	/**
	 * 停止导航，当手动停止导航后，可以调用startNavigate()开启导航
	 */
	public void stopNavigate() {
		Handlerlist.getInstance().notifications(NAVIGATE, NAVIGATE_STOP, null);
		mMapView.setLocationMode(RMLocationMode.NORMAL);
		isNavigating = false;
	}

	private Location mDashedPoint;// 虚线点

	/**
	 * 更新位置
	 * 
	 * @param location
	 */
	public void updateLocation(Location location) {
		if (!isNavigating)
			return;
		double distance = -1, distanceVertrial = -1;// 点到最近的线的距离,和点到垂足的距离
		NavigatePoint point;// 距离最近的点
		int index = 0, indexVertrial = 0;
		mDashedPoint = null;
		double[] v = null;
		if (mNavigateRoutePointList.size() > 1 && !isLock) {
			for (int i = 1; i < mNavigateRoutePointList.size(); i++) {
				NavigatePoint p = mNavigateRoutePointList.get(i - 1);
				NavigatePoint p1 = mNavigateRoutePointList.get(i);
				if (p1.getFloor().equals(p.getFloor())
						&& p1.getFloor().equals(location.getFloor())
						&& p1.getBuildId().equals(location.getBuildId())
						&& p1.getBuildId().equals(p.getBuildId())) {//定位点和路线在同层
					// Log.i("rtmap", "标记："+3);
					double[] vp = getVerticalPoint(location.getX(),
							Math.abs(location.getY()), p.getX(),
							Math.abs(p.getY()), p1.getX(), Math.abs(p1.getY()));
					boolean isNear = getNearlyPoint(vp[0], vp[1], p.getX(),
							Math.abs(p.getY()), p1.getX(), Math.abs(p1.getY()));
					if (isNear) {// 如果点的垂足在线段里面
						double d1 = RMathUtils.distance(location.getX(),
								Math.abs(location.getY()), vp[0], vp[1]);// 求出点到线的距离
						// Log.i("rtmap", "标记：" + 4 + "     i：" + i + "   "
						// + location.getX() + "   " + location.getY()
						// + "    " + p.getX() + "  " + p.getY() + "   "
						// + p1.getX() + "   " + p1.getY() + "    " + d1);
						if (distanceVertrial == -1) {
							distanceVertrial = d1;
							indexVertrial = i;
							v = vp;
						} else {
							if (distanceVertrial > d1) {
								distanceVertrial = d1;
								indexVertrial = i;
								v = vp;
							}
						}
					} else {// 如果点的垂足不在线段里面，那么需要得到定位点最近的是线段上那个点
						double d1 = RMathUtils.distance(location.getX(),
								Math.abs(location.getY()), p.getX(),
								Math.abs(p.getY()));
						if (distance == -1) {// 如果刚开始
							distance = d1;
							index = i - 1;
						} else {
							if (distance > d1) {// 如果距离比此时的线段长
								distance = d1;
								index = i - 1;
							}
						}
					}
				} else {
					break;
				}
			}
			// Log.i("rtmap", "标记：" + 6 + "     index：" + index +
			// "     indexVer:"
			// + indexVertrial + "   distance:" + distance + "     ver:"
			// + distanceVertrial + "    size():"
			// + mNavigateRoutePointList.size());
			if (distanceVertrial > distance || distanceVertrial == -1) {// 如果点到垂足的距离大于点到线的距离
				for (int j = 0; j < index; j++) {
					NavigatePoint p = mNavigateRoutePointList.get(0);
					// Log.i("rtmap", "已移除：" + p.getX() + "    " + p.getY()
					// + "     " + mNavigateRoutePointList.size());
					mNavigateRoutePointList.remove(0);
				}
				if (distance < DISTANCE_LIMIT) {// 如果距离小于阈值
					mLimitCount = 0;
					location.setX(mNavigateRoutePointList.get(0).getX());
					location.setY(mNavigateRoutePointList.get(0).getY());
				} else {// 如果大于阈值，则直接拉虚线
					mDashedPoint = location;
					mLimitCount++;
				}
			} else {// 选择垂足点
				// Log.i("rtmap", "vp[0]: " + v[0] + "    vp[1]:" + v[1]);
				point = new NavigatePoint();
				point.setX((float) v[0]);
				point.setY((float) v[1]);
				point.setFloor(location.getFloor());
				point.setBuildId(location.getBuildId());
				for (int j = 0; j < indexVertrial; j++) {
					NavigatePoint p = mNavigateRoutePointList.get(0);
					// Log.i("rtmap", "已移除：" + p.getX() + "    " + p.getY()
					// + "     " + mNavigateRoutePointList.size());
					mNavigateRoutePointList.remove(0);
				}
				mNavigateRoutePointList.add(0, point);// 将垂足添加到导航路线上
				if (distanceVertrial < DISTANCE_LIMIT) {// 垂距小于阈值，直接拉掉
					mLimitCount = 0;
					location.setX((float) v[0]);
					location.setY((float) v[1]);
				} else {// 否则产生虚点
					mDashedPoint = location;
					mLimitCount++;
				}
			}
			if (mLimitCount > COUNT_LIMIT && !isLock) {// 重新规划路线
				Handlerlist.getInstance().notifications(NAVIGATE,
						REPLAN_ROUTE_START, null);
				mDashedPoint = location;
				POI start = new POI(1, "我的位置", location.getBuildId(),
						location.getFloor(), location.getX(), location.getY());
				NavigatePoint endPoint = mNavigateRoutePointList
						.get(mNavigateRoutePointList.size() - 1);
				POI end = new POI(2, endPoint.getAroundPoiName(),
						endPoint.getBuildId(), endPoint.getFloor(),
						endPoint.getX(), endPoint.getY());
				restartNavigate(start, end);
			}
		}
		if (mNavigateRoutePointList.size() < 3 && !isLock) {
			// 进行距离判断然后到达终点了
			NavigatePoint p = mNavigateRoutePointList
					.get(mNavigateRoutePointList.size() - 1);
			double a = RMathUtils.distance(location.getX(),
					Math.abs(location.getY()), p.getX(), p.getY());
			if (a < DISTANCE_END) {
				Handlerlist.getInstance().notifications(NAVIGATE, ARRIVED,
						getCurrentKeyPoint());
				mNavigateRoutePointList.clear();// 清空导航路线
				stopNavigate();
			} else {
				mDashedPoint = location;
			}
		}
		// 用于告知用户下一个导航节点信息是什么
		Handlerlist.getInstance().notifications(NAVIGATE,
				NAVIGATE_CURRENT_POINT, getCurrentKeyPoint());
	}

	@Override
	public void onDraw(Canvas canvas) {
		ArrayList<NavigatePoint> list;
		if (isNavigating) {// 如果正在导航中
			list = mNavigateRoutePointList;
		} else {
			list = mRoutePointList;
		}
		if (list.size() == 0) {
			return;
		}
		mOtherPath.reset();
		mFloorPath.reset();
		mKeyFloorPath.reset();
		boolean isHadFloor = false;
		for (int i = 0; i < list.size(); i++) {
			NavigatePoint temppoi = list.get(i), temppoi1 = null;
			PointInfo screen = mMapView.fromLocation(temppoi.getX(),
					temppoi.getY());

			if (temppoi.getBuildId().equals(mMapView.getBuildId())
					&& temppoi.getFloor().equals(mMapView.getFloor())) {// 楼层建筑物都一样，那么同层
				isHadFloor = true;
				if (i != 0) {
					temppoi1 = list.get(i - 1);
				}
				if (!isNavigating) {
					NavigatePoint start = list.get(mKeyStart);
					NavigatePoint end = list.get(mKeyEnd);
					if (start.getFloor().equals(end.getFloor())) {// 如果高亮在同一层
						if (start.getFloor().equals(temppoi.getFloor())
								&& i >= mKeyStart && i <= mKeyEnd) {
							if (i == mKeyStart) {// 第一个点
								mKeyFloorPath.moveTo(screen.getX(),
										screen.getY());
							} else {
								mKeyFloorPath.lineTo(screen.getX(),
										screen.getY());
							}
						}
					} else {// 起点和终点不在同一层
						if (start.getFloor().equals(temppoi.getFloor())
								&& i >= mKeyStart) {// 高亮在起点的一层；最外围判断说明同层才能进入，所以，不用判断结束点
							if (i == mKeyStart) {// 第一个点
								mKeyFloorPath.moveTo(screen.getX(),
										screen.getY());
							} else {
								mKeyFloorPath.lineTo(screen.getX(),
										screen.getY());
							}
						} else if (end.getFloor().equals(temppoi.getFloor())
								&& i <= mKeyEnd) {
							if (mKeyFloorPath.isEmpty()) {
								mKeyFloorPath.moveTo(screen.getX(),
										screen.getY());
							} else {
								mKeyFloorPath.lineTo(screen.getX(),
										screen.getY());
							}
						}
					}
				}

				if (mFloorPath.isEmpty()
						|| (temppoi1 != null && !temppoi1.getFloor().equals(
								temppoi.getFloor()))) {
					mFloorPath.moveTo(screen.getX(), screen.getY());
				} else {
					mFloorPath.lineTo(screen.getX(), screen.getY());
				}
			} else if (temppoi.getBuildId().equals(mMapView.getBuildId())
					&& !temppoi.getFloor().equals(mMapView.getFloor())) {
				if (mOtherPath.isEmpty()
						|| (temppoi1 != null && !temppoi1.getFloor().equals(
								temppoi.getFloor()))) {
					mOtherPath.moveTo(screen.getX(), screen.getY());
				} else {
					mOtherPath.lineTo(screen.getX(), screen.getY());
				}
			}
		}

		if (isNavigating) {
			if (mDashedPoint != null) {
				NavigatePoint temppoi = list.get(0);
				PointInfo s1 = mMapView.fromLocation(temppoi.getX(),
						temppoi.getY());
				PointInfo s2 = mMapView.fromLocation(mDashedPoint.getX(),
						mDashedPoint.getY());
				canvas.drawLine(s2.getX(), s2.getY(), s1.getX(), s1.getY(),
						mRoutePaint2);
			}
			canvas.drawPath(mFloorPath, mRoutePaint);
		} else {
			if (mShowOtherFloor && isHadFloor)// 是否显示其他楼层
				canvas.drawPath(mOtherPath, mRoutePaint2);
			canvas.drawPath(mFloorPath, mRoutePaint);
			if (!mKeyFloorPath.isEmpty()) {
				canvas.drawPath(mKeyFloorPath, mKeyRoutePaint);
			}
		}

		for (int i = 0; i < list.size(); i++) {
			NavigatePoint temppoi = list.get(i), temppoi1 = null;
			PointInfo tempp = mMapView.fromLocation(temppoi.getX(),
					temppoi.getY());

			if (temppoi.getBuildId().equals(mMapView.getBuildId())
					&& temppoi.getFloor().equals(mMapView.getFloor())) {// 楼层建筑物都一样，那么同层
				if (i == 0) {
					if (mStartIcon != null && !isNavigating)// 导航中不画起点
						canvas.drawBitmap(mStartIcon,
								tempp.getX() - mStartIcon.getWidth() / 2,
								tempp.getY() - mStartIcon.getHeight(), null);
				} else if (i == list.size() - 1) {
					if (mEndIcon != null)
						canvas.drawBitmap(mEndIcon,
								tempp.getX() - mEndIcon.getWidth() / 2,
								tempp.getY() - mEndIcon.getHeight(), null);
				} else {
					if (mPin != null && !isNavigating)// 导航中不画中间点
						canvas.drawBitmap(mPin, tempp.getX() - mPin.getWidth()
								/ 2, tempp.getY() - mPin.getHeight(), null);
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
		if (mRoutePointList != null) {
			mRoutePointList.clear();
		}
		mMapView.popuindex = 0;
	}

	@Override
	public boolean onTap(MotionEvent e) {
		return false;
	}

	@Override
	public boolean hasData() {
		return (mRoutePointList != null && mRoutePointList.size() != 0);
	}

	/**
	 * 自定义高亮（选中）路线段颜色
	 * 
	 * @param mPaint
	 *            Paint类对象
	 */
	public void setKeyRoutePaint(Paint mPaint) {
		mKeyRoutePaint = mPaint;
	}

	/**
	 * 设置高亮(选中)的路线开始和结束的导航点的下标（即ArrayList集合中第几个到第几个）
	 * 
	 * @param startIndex
	 *            高亮起点下标
	 * @param endIndex
	 *            高亮结束点下标
	 */
	public void setKeyRouteIndex(int startIndex, int endIndex) {
		if (endIndex > startIndex && startIndex >= 0
				&& startIndex < mRoutePointList.size() && endIndex >= 0
				&& endIndex < mRoutePointList.size()) {// 结束点必须在起点之后，且两个点范围必须在集合中
			mKeyStart = startIndex;
			mKeyEnd = endIndex;
		}
	}

	/**
	 * 自定义本层路线画笔样式
	 * 
	 * @param mPaint
	 *            Paint类对象
	 */
	public void setRoutePaint(Paint mPaint) {
		mRoutePaint = mPaint;
	}

	/**
	 * 自定义其他层路线画笔样式
	 * 
	 * @param mPaint
	 *            Paint类对象
	 */
	public void setOtherFloorRoutePaint(Paint mPaint) {
		mRoutePaint2 = mPaint;
	}

	/**
	 * 参见父类说明
	 */
	@Deprecated
	@Override
	public void clearLayer() {
		destroyLayer();
	}

	private boolean isLock;

	/**
	 * 重新规划导航
	 * 
	 * @param start
	 * @param end
	 */
	private void restartNavigate(POI start, POI end) {
		if (!isLock) {
			isLock = true;
			RMNavigationUtil.requestNavigation(XunluMap.getInstance()
					.getApiKey(), start.getBuildId(), start, end, null, false,
					new OnNavigationListener() {

						@Override
						public void onFinished(RMRoute route) {
							if (route.getError_code() == 0) {
								isLock = false;
								mMapView.setLocationMode(RMLocationMode.COMPASS);
								Handlerlist.getInstance().notifications(
										NAVIGATE, NAVIGATE_START, null);
								mLimitCount = 0;
								setNavigatePoints(route.getPointlist());
							} else {
								Handlerlist.getInstance().notifications(
										NAVIGATE, PLAN_ROUTE_ERROR, null);
							}
						}
					});
		}
	}

	/**
	 * 计算点是否在线段中
	 * 
	 * @param x0
	 *            点的x坐标
	 * @param y0
	 *            点的y坐标
	 * @param x1
	 *            线段的端点x1
	 * @param y1
	 *            线段的端点y1
	 * @param x2
	 *            线段的端点x2
	 * @param y2
	 *            线段的端点y2
	 */
	public boolean getNearlyPoint(double x0, double y0, double x1, double y1,
			double x2, double y2) {
		double a = x0 - x1;
		double a1 = x0 - x2;
		double b = y0 - y1;
		double b1 = y0 - y2;
		if (x1 == x2) {// 平行于y轴
			if (b * b1 < 0) {
				return true;
			}
		}

		if (y1 == y2) {
			if (a * a1 < 0) {
				return true;
			}
		}

		if (x1 != x2 && y1 != y2) {
			if (a * a1 < 0 && b * b1 < 0) {
				return true;
			}
		}
		return false;
	}

	// 如果该线段平行于X轴（Y轴），则过点point作该线段所在直线的垂线，垂足很容
	// 易求得，然后计算出垂足，如果垂足在线段上则返回垂足，否则返回离垂足近的端
	// 点；
	//
	// 如果该线段不平行于X轴也不平行于Y轴，则斜率存在且不为0。设线段的两端点为
	// pt1和pt2，斜率为：
	// k = ( pt2.y - pt1. y ) / (pt2.x - pt1.x );
	// 该直线方程为：
	// y = k* ( x - pt1.x) + pt1.y
	// 其垂线的斜率为 - 1 / k，
	// 垂线方程为：
	// y = (-1/k) * (x - point.x) + point.y
	// 联立两直线方程解得：
	// x = ( k^2 * pt1.x + k * (point.y - pt1.y ) + point.x ) / ( k^2 + 1)
	// y = k * ( x - pt1.x) + pt1.y;
	/**
	 * 从点到线做垂足，计算垂足的坐标
	 * 
	 * @param x0
	 *            点的x坐标
	 * @param y0
	 *            点的y坐标
	 * @param x1
	 *            线段的端点x1
	 * @param y1
	 *            线段的端点y1
	 * @param x2
	 *            线段的端点x2
	 * @param y2
	 *            线段的端点y2
	 * @return 垂足的坐标
	 */
	public double[] getVerticalPoint(double x0, double y0, double x1,
			double y1, double x2, double y2) {
		double[] points = new double[2];
		if (y1 == y2) {// 平行于x轴
			points[0] = x0;
			points[1] = y1;
		}
		if (x1 == x2) {// 平行于y轴
			points[0] = x1;
			points[1] = y0;
		}

		if (x1 != x2 && y1 != y2) {
			double k = (y1 - y2) / (x1 - x2);// 斜率
			points[0] = (k * k * x1 + k * (y0 - y1) + x0) / (k * k + 1);
			points[1] = k * (points[0] - x1) + y1;
		}
		return points;
	}

	/**
	 * 点到直线的最短距离的判断 点（x0,y0） 到由两点组成的线段（x1,y1） ,( x2,y2 )
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param x0
	 * @param y0
	 * @return
	 */
	private double pointToLine(int x1, int y1, int x2, int y2, int x0, int y0) {
		double space = 0;
		double a, b, c;
		a = lineSpace(x1, y1, x2, y2);// 线段的长度
		b = lineSpace(x1, y1, x0, y0);// (x1,y1)到点的距离
		c = lineSpace(x2, y2, x0, y0);// (x2,y2)到点的距离
		if (c <= 0.000001 || b <= 0.000001) {
			space = 0;
			return space;
		}
		if (a <= 0.000001) {
			space = b;
			return space;
		}
		if (c * c >= a * a + b * b) {
			space = b;
			return space;
		}
		if (b * b >= a * a + c * c) {
			space = c;
			return space;
		}
		double p = (a + b + c) / 2;// 半周长
		double s = Math.sqrt(p * (p - a) * (p - b) * (p - c));// 海伦公式求面积
		space = 2 * s / a;// 返回点到线的距离（利用三角形面积公式求高）
		return space;
	}

	/**
	 * 计算两点之间的距离
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	private double lineSpace(int x1, int y1, int x2, int y2) {
		double lineLength = 0;
		lineLength = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
		return lineLength;
	}
}
