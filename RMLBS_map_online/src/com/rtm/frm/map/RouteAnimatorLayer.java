package com.rtm.frm.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
import android.graphics.PathEffect;
import android.graphics.Typeface;
import android.util.Log;
import android.view.MotionEvent;

import com.rtm.common.utils.Constants;
import com.rtm.common.utils.RMStringUtils;
import com.rtm.frm.model.Location;
import com.rtm.frm.model.NavigatePoint;
import com.rtm.frm.model.PointInfo;
import com.rtm.frm.utils.RMathUtils;

@SuppressLint("NewApi")
public class RouteAnimatorLayer implements BaseMapLayer,
		ValueAnimator.AnimatorUpdateListener {

	private Path mPath;

	private MapView mMapView;
	private Paint mRoutePaint;
	private Paint mPopupPaint;
	private Paint mTextPaint;
	private OnPointClickListener mClickListener;
	private OnRouteAnimatorEndListener onRouteAnimatorEndListener;

	private Bitmap mPointIcon;// 中间点icon
	private Bitmap mStartIcon;// 开始icon
	private Bitmap mEndIcon;// 结束icon
	private int time = 60;// 每米运动时间为60ms

	private HashMap<String, ArrayList<NavigatePoint>> mRouteMap;

	@Deprecated
	public RouteAnimatorLayer(MapView view) {
		mMapView = view;
		mRouteMap = new HashMap<String, ArrayList<NavigatePoint>>();
		initLayer(view);
	}

	/**
	 * 设置动画运动速度，我们定义此设置为：每运动一米需要的时间。默认是60ms，我们为了保证动画运行流畅给用户良好的体验做了大量测试，
	 * 以西单大悦城的地图为例
	 * ，测试过程中：我们发现当时间小于60ms，在地图正常的比例尺下，运动点由于速度太快，出现闪动的情况；最后我们选择了默认值为60ms
	 * ，建议使用大于等于60的值进行设置，当小于60ms,我们不会对默认速度进行改变
	 * 
	 * @param time
	 *            时间，单位：毫秒ms；默认值：60ms
	 */
	public void setAnimatorSpeed(int time) {
		if (time >= 60) {
			this.time = time;
		}
	}

	AnimatorSet animSet = null;

	@SuppressLint("NewApi")
	private void createAnimation(String key) {
		if (animSet != null) {
			animSet.cancel();
		}
		animSet = new AnimatorSet();
		final ArrayList<NavigatePoint> pointlist = mRouteMap.get(key);

		// ybum.setInterpolator(new AccelerateInterpolator(2f));

		for (int i = 0; i < pointlist.size() - 1; i++) {
			NavigatePoint p0 = pointlist.get(i);

			final NavigatePoint p1 = pointlist.get(i + 1);
			if(!p1.getFloor().equals(p0.getFloor()))
				break;
			ValueAnimator ybum = ObjectAnimator.ofFloat(mImagePoint, "y",
					p0.getY(), p1.getY() - MapView.DELTA_LENGTH);
			double length = Math.sqrt(Math.pow(p1.getY() - p0.getY(), 2)
					+ Math.pow(p1.getX() - p0.getX(), 2));

			ybum.setDuration((long) (time * length));
			ValueAnimator xbum = ObjectAnimator.ofFloat(mImagePoint, "x",
					p0.getX(), p1.getX() - MapView.DELTA_LENGTH);
			xbum.setDuration((long) (time * length));
			ybum.addUpdateListener(this);
			if (i == 0)
				animSet.play(xbum).with(ybum);
			else {
				AnimatorSet a = new AnimatorSet();
				a.play(animSet).before(ybum).before(xbum);
				animSet = a;
			}
		}
		animSet.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator animation) {

			}

			@Override
			public void onAnimationRepeat(Animator animation) {

			}

			@Override
			public void onAnimationEnd(Animator animation) {
				mImagePoint.setX(pointlist.get(0).getX());
				mImagePoint.setY(pointlist.get(0).getY());
				mImagePoint.setFloor(pointlist.get(0).getFloor());
				mImagePoint.setBuildId(pointlist.get(0).getBuildId());
				if (onRouteAnimatorEndListener != null) {
					onRouteAnimatorEndListener.onRouteAnimatorEnd();
				}
				mMapView.refreshMap();
			}

			@Override
			public void onAnimationCancel(Animator animation) {

			}
		});
	}

	@SuppressLint("NewApi")
	public void startAnimation(String key) {
		if (mRouteMap.containsKey(key) && mNavigationIcon != null) {
			createAnimation(key);
			animSet.start();
		}
	}
	
	
	/**
	 * 设置中间点图片Bitmap
	 * @param pointIcon 中间点图片Bitmap
	 */
	public void setPointIcon(Bitmap pointIcon) {
		mPointIcon = pointIcon;
	}

	/**
	 * 设置动画执行完的监听器
	 * 
	 * @param onRouteAnimatorEndListener
	 */
	public void setOnRouteAnimatorEndListener(
			OnRouteAnimatorEndListener onRouteAnimatorEndListener) {
		this.onRouteAnimatorEndListener = onRouteAnimatorEndListener;
	}

	/**
	 * 设置所有路线
	 * 
	 * @param map
	 */
	public void setRouteMap(HashMap<String, ArrayList<NavigatePoint>> map) {
		if (map != null)
			mRouteMap = map;
	}

	/**
	 * 得到所有路线
	 * 
	 * @return
	 */
	public HashMap<String, ArrayList<NavigatePoint>> getRouteMap() {
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
	public RouteAnimatorLayer(MapView view, Bitmap start, Bitmap end,
			Bitmap mark) {
		mMapView = view;
		mStartIcon = start;
		mEndIcon = end;
		mPointIcon = mark;
		mRouteMap = new HashMap<String, ArrayList<NavigatePoint>>();
		initLayer(view);
	}

	private NavigatePoint mImagePoint;

	/**
	 * 添加路线
	 * 
	 * @param key
	 *            每条路线对应一个key,这个key只要保证唯一就行，方便你查找具体路线进行修改（建议使用你生成的文件名做key）
	 * @param points
	 *            本楼层的路线点集合
	 */
	public boolean addRoute(String key, ArrayList<NavigatePoint> points) {
		if (RMStringUtils.isEmpty(key) || points == null)// key为空返回
			return false;
		mImagePoint = new NavigatePoint();
		mImagePoint.setX(points.get(0).getX());
		mImagePoint.setY(points.get(0).getY());
		mImagePoint.setFloor(points.get(0).getFloor());
		mImagePoint.setBuildId(points.get(0).getBuildId());
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
	public ArrayList<NavigatePoint> getRoute(String key) {
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
	 * 
	 * @param key
	 */
	public void removeRoute(String key) {
		if (containsKey(key)) {
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
	public boolean addRoutePoint(String key, NavigatePoint point) {
		if (RMStringUtils.isEmpty(key) || point == null)// key为空返回
			return false;
		mRouteMap.get(key).add(point);
		return true;
	}

	private Bitmap mNavigationIcon;

	/**
	 * 设置导航图标
	 * 
	 * @param bitmap
	 */
	public void setNavigationIcon(Bitmap bitmap) {
		this.mNavigationIcon = bitmap;
	}

	/**
	 * 移除导航图标
	 */
	public void removeNavigationIcon() {
		this.mNavigationIcon = null;
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
		mRoutePaint.setStrokeWidth(10);
		mRoutePaint.setAntiAlias(true);
		mRoutePaint.setStrokeCap(Cap.ROUND);
		mRoutePaint.setStrokeJoin(Join.ROUND);
		mRoutePaint.setColor(0x960079C6);
		PathEffect peArray = new PathDashPathEffect

		(

		makePathDash(), // 形状

				18, // 间距

				0,// 首绘制偏移量

				PathDashPathEffect.Style.ROTATE

		);
		mRoutePaint.setPathEffect(peArray);

		mPath = new Path();

		mTextPaint = new Paint();
		mTextPaint.setTextAlign(Align.CENTER);
		mTextPaint.setColor(Color.BLACK);
		mTextPaint.setAntiAlias(true);
		mTextPaint.setTextSize(MapView.MAPTEXT.getTextsize());
		mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);

		mPopupPaint = new Paint();
		mPopupPaint.setTextAlign(Align.CENTER);
		mPopupPaint.setColor(Color.BLACK);
		mPopupPaint.setAntiAlias(true);
		mPopupPaint.setTextSize(MapView.MAPTEXT.getTextsize());
	}

	private boolean isDraw = true;// 是否画

	/**
	 * 是否绘制动画路线
	 * @param isDraw 布尔值，是否绘制
	 */
	public void setDraw(boolean isDraw) {
		this.isDraw = isDraw;
	}

	private boolean isSameLocation(NavigatePoint point) {
		return point != null && mMapView.getFloor().equals(point.getFloor())
				&& mMapView.getBuildId().equals(point.getBuildId());
	}

	@SuppressLint("DrawAllocation")
	@Override
	public void onDraw(Canvas canvas) {
		if (!isDraw)
			return;
		mPath.reset();// 重绘路线图
		Iterator<String> keySet = mRouteMap.keySet().iterator();
		while (keySet.hasNext()) {
			ArrayList<NavigatePoint> points = mRouteMap.get(keySet.next());
			for (int i = 0; i < points.size(); i++) {
				NavigatePoint p = points.get(i);
				PointInfo temppoi = mMapView.fromLocation(new Location(
						p.getX(), p.getY()));
				if (!isSameLocation(p))
					continue;

				float pointX;// 实际点的x
				float pointY;// 实际点的y
				if (i != 0) {
					NavigatePoint p1 = points.get(i - 1);
					PointInfo temppoi1 = mMapView.fromLocation(new Location(p1
							.getX(), p1.getY()));
					if (temppoi.getX() == temppoi1.getX()
							&& temppoi.getY() == temppoi1.getY()
							&& p.getFloor().equals(p1.getFloor()))
						continue;
					if (mPointIcon == null) {
						canvas.drawLine(temppoi1.getX(), temppoi1.getY(),
								temppoi.getX(), temppoi.getY(), mRoutePaint);
					}
					if (i > 0 && i < points.size() - 1) {
						if (mPointIcon != null) {

							pointX = temppoi.getX() - mPointIcon.getWidth()
									/ 2.0f;// 实际点的x
							pointY = temppoi.getY() - mPointIcon.getHeight()
									/ 2.0f;// 实际点的y

							Matrix matrix = new Matrix();
							matrix.postRotate(
									(float) (getLineDegree(p, p1) + Math
											.toDegrees(mMapView.mapangle)),
									mPointIcon.getWidth() / 2, mPointIcon
											.getHeight() / 2);
							matrix.postTranslate(pointX, pointY);
							canvas.drawBitmap(mPointIcon, matrix, null);
						}
					} else {
						if (mEndIcon != null) {
							pointX = temppoi.getX() - mEndIcon.getWidth()
									/ 2.0f;// 实际点的x
							pointY = temppoi.getY() - mEndIcon.getHeight()
									/ 2.0f;// 实际点的y
							canvas.drawBitmap(mEndIcon, pointX, pointY, null);
						}
					}
				} else {
					if (mStartIcon != null) {
						pointX = temppoi.getX() - mStartIcon.getWidth() / 2.0f;// 实际点的x
						pointY = temppoi.getY() - mStartIcon.getHeight() / 2.0f;// 实际点的y
						canvas.drawBitmap(mStartIcon, pointX, pointY, null);
					}
				}
			}
			if (isSameLocation(mImagePoint) && mNavigationIcon != null) {// 导航图标
				PointInfo temppoi1 = mMapView.fromLocation(new Location(mImagePoint
						.getX(), mImagePoint.getY()));
				canvas.drawBitmap(mNavigationIcon, temppoi1.getX()
						- mNavigationIcon.getWidth() / 2.0f, temppoi1.getY()
						- mNavigationIcon.getHeight() / 2.0f, null);
			}
		}
	}

	private double getLineDegree(NavigatePoint p, NavigatePoint p1) {
		float x = p.getX() - p1.getX();
		float y = p.getY() - p1.getY();
		double lineDegree;
		if (y > 0) {
			lineDegree = Math.toDegrees(Math.atan(y / x));
			if (x < 0) {
				lineDegree += 180;
			}
		} else if (y == 0) {
			if (x >= 0) {
				lineDegree = 0;
			} else {
				lineDegree = 180;
			}
		} else {
			lineDegree = Math.toDegrees(Math.atan(y / x));
			if (x >= 0) {
				lineDegree += 360;
			} else {
				lineDegree += 180;
			}
		}
		return lineDegree;
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
		if (mClickListener == null)
			return false;
		if (e.getAction() == MotionEvent.ACTION_DOWN) {
			downX = e.getX();
			downY = e.getY();
		} else if (e.getAction() == MotionEvent.ACTION_UP
				|| e.getAction() == MotionEvent.ACTION_CANCEL) {// 当手指抬起时的
			Log.e("rtmap", "e.getX : " + e.getX() + "   e.getY : " + e.getY());
			NavigatePoint clickPoint = null;
			String key = null;
			float p2p = -1;// 两个点之间的距离
			if (Math.abs(e.getX() - downX) < 20
					&& Math.abs(e.getY() - downY) < 20) {// 如果按下与抬起距离在20像素范围内，可视为点击
				Iterator<String> keySet = mRouteMap.keySet().iterator();
				while (keySet.hasNext()) {
					String str = keySet.next();
					ArrayList<NavigatePoint> points = mRouteMap.get(str);
					for (int i = 0; i < points.size(); i++) {
						NavigatePoint p = points.get(i);
						PointInfo temppoi = mMapView.fromLocation(new Location(
								p.getX(), p.getY()));
						if (temppoi.getX() < 0 || temppoi.getY() < 0)// 屏幕外的不用计算
							continue;
						float reduceX = Math.abs(temppoi.getX() - e.getX());
						float reduceY = Math.abs(temppoi.getY() - e.getY());
						if (reduceX > 20 || reduceY > 20)// 超出手指同一水平线范围
							continue;
						float dis = RMathUtils.distance(e.getX(), e.getY(),
								temppoi.getX(), temppoi.getY());// 计算两点之间的距离
						if (p2p < 0 || p2p > dis) {// 距离比他大
							clickPoint = p;// 保存距离范围内的点
							p2p = dis;
							key = str;
						}
					}
				}
				if (p2p > -1 && mClickListener != null) {// 说明点击在点的范围内
					mClickListener.onClick(clickPoint, key);
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
	public void onAnimationUpdate(ValueAnimator animation) {
		mMapView.refreshMap();
	}

	/**
	 * 参见父类说明
	 */
	@Deprecated
	@Override
	public void clearLayer() {
		destroyLayer();
	}

	/**
	 * 当动画执行结束回调接口
	 * 
	 * @author dingtao
	 *
	 */
	public interface OnRouteAnimatorEndListener {
		void onRouteAnimatorEnd();
	}
}
