package com.rtm.frm.map;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Path.Direction;
import android.graphics.Region.Op;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;

import com.rtm.common.model.POI;
import com.rtm.common.model.RMLocation;
import com.rtm.common.style.DrawStyle;
import com.rtm.common.style.TextStyle;
import com.rtm.common.utils.Constants;
import com.rtm.common.utils.RMFileUtil;
import com.rtm.common.utils.RMIOUtils;
import com.rtm.common.utils.RMStringUtils;
//import net.sourceforge.pinyin4j.PinyinHelper;
//import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
//import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
//import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
//import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
//import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import com.rtm.frm.drawmap.AnimateDraggingMapThread;
import com.rtm.frm.map.MultiTouchSupport.MultiTouchZoomListener;
import com.rtm.frm.model.Location;
import com.rtm.frm.model.PointInfo;
import com.rtm.frm.utils.Geometry;
import com.rtm.frm.utils.Handlerlist;
import com.rtm.frm.vmap.Coord;
import com.rtm.frm.vmap.Edge;
import com.rtm.frm.vmap.Shape;

public class MapView extends ViewGroup {

	private Context mContext;
	private SurfaceView mmapView;
	private PointInfo viewpoint;
	private float viewx;
	private float viewy;
	private int viewdx = 0;
	private int viewdy = 0;
	private int viewleft;
	private int viewright;
	private float viewtop;
	private int viewbottom;
	private boolean stopmove;
	private boolean ismove;
	private float toJpgScale;
	private boolean drawLabel = true;

	private boolean drawLogo = true;
	// ---------------传感器--------------
	private SensorManager sm = null;
	private Sensor mOrientationSensor = null;
	private float mTargetDirection;// 目标浮点方向
	private boolean isORientationSupport;
	private int MOVE_INTERVAL = 30;// 页面40毫秒刷新一次
	private int mapBackgroundColor = 0xffffffff;

	private boolean isUpdateMap;// 是否更新下载地图
	private final Handler mSensorHandler = new Handler();

	private static int COLOR_LOCATION_CIRCLE = 0x64D3E6F6;

	private static int COLOR_LOCATION_OUTLINE = 0xE00084C0;
	private static float LOCATION_OUTLINE_WIDTH = 1f;
	private float mLocationAngle = 0;
	private int radius = 10;
	private Paint mPaint1;
	private Paint mPaint2;

	/**
	 * 定位点正常的图片
	 */
	private Bitmap mImageLocationNormal;
	/**
	 * 定位点闪亮的图片
	 */
	private Bitmap mImageLocationLight;

	private Bitmap logoBitmap;

	private long refreshTime;// 刷新时间
	private ArrayList<Integer> priorityShowLevels;// 优先绘制的level级别poi

	/**
	 * 当前位置
	 */
	private Location mCurrentLocation;

	private int locationMode;// 定位模式

	private boolean mStopDrawing = true;// 是否停止指南针旋转的标志位
	private boolean touched;
	private boolean DoubleTapable = true;
	private boolean Roamable = true;
	private boolean Rotateable = true;
	private boolean Zoomable = true;
	private boolean isfling = false;

	private boolean mTapable = true;
	private float mScale;// 地图比例尺
	private AnimateDraggingMapThread animatedDraggingThread;
	public int popuindex = 0;

	private Location mCenterLocation;// 屏幕中心点坐标
	private boolean isTrackMode;
	private boolean menuvisible = true;

	private ValueAnimator mAnimator;// 全局MapView动画

	// 自定义样式poi列表
	private ArrayList<POI> mCustomPoiList;

	// 手势监测
	private GestureDetector gestureDetector;
	// 多点触控
	private MultiTouchSupport multiTouchSupport;

	private OnTapListener mOnTapListener;
	// 点击屏幕时查询当前点选点附近信息
	private OnTapListener mOnClickListener;
	// 监测是否是跟踪模式
	private OnMapModeChangedListener mOnMapModeChangedListener;
	POI selectPoi;

	private boolean mIsLight = true;
	private MapConfig mConfig;

	private boolean isResetMapCenter = true;// 是否每次重置地图中心点
	private boolean isResetMapScale;// 是否每次重置地图比例尺
	
	private boolean drawText = true;

	/**
	 * 在地图重绘时，是否重新绘制POI的名字
	 */
	private boolean mGetlabels = true;
	private int mLogoPosition = 0;
	private ArrayList<BaseMapLayer> mMapLayers;

	private static final int LOAD_MAP = 818;
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Constants.MAP_REFRESH:
				refreshMapview();
				break;
			case LOAD_MAP:
				loadMap(buildId, floor);
				break;
			}
		}
	};

	/**
	 * 动画运行递增值
	 */
	public static final float DELTA_LENGTH = 0.1f;
	/**
	 * 动画执行时间
	 */
	private static final int ANIM_DURATION = 1000;// 每次执行动画时间为900毫秒，

	/**
	 * 定位点动画和居中点动画
	 */
	private AnimatorSet mLocationAnim, mCenterAnim;

	/**
	 * 地图缩放最小值，单位：米/像素；所以想让地图最大屏占比更大，可减小此属性的值，默认每像素0.01米
	 */
	public static float ZOOM_MIN = 0.01f;
	/**
	 * 地图放大最大值，单位：米/像素；所以想让地图最小屏占比更小，可扩大此属性的值，默认每像素0.8米
	 */
	public static float ZOOM_MAX = 0.8f;

	/**
	 * 地图屏占比，表示地图在屏幕中初始占屏幕的比重，以长边为准，默认是全屏
	 */
	public static float MAP_SCREEN_SCALE = 1F;

	/**
	 * 存放地图所有绘图颜色样式
	 */
	@SuppressLint("UseSparseArrays")
	public final static HashMap<Integer, DrawStyle> STYLES = new HashMap<Integer, DrawStyle>();

	/**
	 * 绘制poi图标，使用全名匹配assets文件。例：put("肯德基","kendeji.jpg")
	 */
	private HashMap<String, String> poiIconMap = new HashMap<String, String>();

	/**
	 * 无效区域颜色样式，此属性已经实例化，请直接调用方法设置颜色
	 */
	public final static DrawStyle MAPINVALID = new DrawStyle(0xffe5e5e5,
			0xffc9c5c3, 0.5f);
	/**
	 * 未知区域颜色样式，此属性已经实例化，请直接调用方法设置颜色
	 */
	public final static DrawStyle MAPUNKNOWN = new DrawStyle(0xffe5e5e5,
			0xffc9c5c3, 0.5f);
	/**
	 * 店铺,poi颜色样式，此属性已经实例化，请直接调用方法设置颜色
	 */
	public final static DrawStyle MAPPOI = new DrawStyle(0xffefefef,
			0xffc9c5c3, 0.5f);
	/**
	 * 卫生间颜色样式，此属性已经实例化，请直接调用方法设置颜色
	 */
	public final static DrawStyle MAPWC = new DrawStyle(0xfff7f1eb, 0xffc9c5c3,
			0.5f);
	/**
	 * 通行设施：电梯，楼梯，扶梯颜色样式，此属性已经实例化，请直接调用方法设置颜色
	 */
	public final static DrawStyle MAPSTAIRS = new DrawStyle(0xfff9f2eb,
			0xffc9c5c3, 0.5f);
	/**
	 * 地图地面颜色样式，此属性已经实例化，请直接调用方法设置颜色
	 */
	public final static DrawStyle MAPGROUND = new DrawStyle(0xfff7f1eb,
			0xffc9c5c3, 3);
	/**
	 * 地图文字样式，此属性已经实例化，请直接调用方法设置颜色
	 */
	public final static TextStyle MAPTEXT = new TextStyle();

	static {
		STYLES.put(3, MAPINVALID);// 天井,无效区
		STYLES.put(4, MAPUNKNOWN);// 未知区
		STYLES.put(5, MAPPOI);// 店铺，POI
		STYLES.put(6, MAPWC);// 卫生间
		STYLES.put(7, MAPSTAIRS);// 通行设施：电梯，楼梯，扶梯
		STYLES.put(8, MAPGROUND);// 外边框，地图地面
	}

	/**
	 * 构造方法
	 * 
	 * @param context
	 *            上下文对象
	 * @param attrs
	 *            属性参数
	 * @param defStyleAttr
	 *            样式参数
	 */
	public MapView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	/**
	 * 构造方法
	 * 
	 * @param context
	 *            上下文
	 */
	public MapView(Context context) {
		super(context);
		init(context);
	}

	/**
	 * 构造方法
	 * 
	 * @param context
	 *            上下文
	 * @param attrs
	 *            属性参数
	 */
	public MapView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	/**
	 * 地图刷新器，罗盘指针刷新
	 */
	private Runnable mCompassViewUpdater = new Runnable() {
		@Override
		public void run() {

			if (!mStopDrawing) {
				if (mCurrentLocation != null
						&& mCurrentLocation.getBuildId().equals(
								mConfig.getBuildId())
						&& mCurrentLocation.getFloor().equals(
								mConfig.getFloor())) {

					// calculate the short routine
					float to = (mTargetDirection + 360) % 360;// 罗盘角度旋转方向为正向，而图片旋转角度为正向，所以需要翻转一下

					if (locationMode == RMLocationMode.COMPASS) {
						mapangle = (float) -Math.toRadians(to)
								+ mConfig.getDrawMap().getAngle();// 手机指向的角度需要减去已经地图原本偏转的角度，就是现在地图应该旋转的角度
						mGetlabels = true;
						mLocationAngle = 0;
					} else {// 自由模式下
						mLocationAngle = (float) (Math.toDegrees(mConfig
								.getDrawMap().getAngle()) + to + Math// 指针角度等于地图原本偏角+目前的旋转角度+罗盘角度
								.toDegrees(mapangle));
						if (onCompassUpdateListener != null) {
							boolean isLeft = false;
							float degrees = mConfig.getDrawMap().getAngle() - 180.0f;
							if (mLocationAngle > degrees
									&& mLocationAngle < mConfig.getDrawMap()
											.getAngle()) {
								isLeft = true;
							}
							onCompassUpdateListener.onCompassUpdate(isLeft);
						}
					}
				}
			}
			mSensorHandler.postDelayed(mCompassViewUpdater, MOVE_INTERVAL);// 500毫秒后重新执行自己，比定时器好
		}
	};

	private OnCompassUpdateListener onCompassUpdateListener;

	public void setOnCompassUpdateListener(
			OnCompassUpdateListener onCompassUpdateListener) {
		this.onCompassUpdateListener = onCompassUpdateListener;
	}

	public interface OnCompassUpdateListener {
		void onCompassUpdate(boolean isLeft);
	}

	/**
	 * 移除刷新线程，刷新线程控制地图定位点刷新，地图位置更新等。
	 */
	private void removeRefreshRunnable() {
		mSensorHandler.removeCallbacks(mCompassViewUpdater);
	}

	/**
	 * 重新开启刷新线程，刷新线程控制地图定位点刷新，地图位置更新等。
	 */
	private void restartRefreshRunnable() {
		mSensorHandler.removeCallbacks(mCompassViewUpdater);
		mSensorHandler.postDelayed(mCompassViewUpdater, MOVE_INTERVAL);// 50毫米后重新执行自己，比定时器好
	}

	/**
	 * 初始化方向感应器
	 * 
	 * @param context
	 */
	@SuppressWarnings("deprecation")
	private void initSensor(Context context) {
		mTargetDirection = 0.0f;// 初始化目标方向
		// mInterpolator = new AccelerateInterpolator();
		if (Constants.ROTATE) {
			sm = (SensorManager) context
					.getSystemService(Context.SENSOR_SERVICE);
			// 得到手机上所有的传感器
			List<Sensor> l = sm.getSensorList(Sensor.TYPE_ORIENTATION);
			if (l != null && l.size() != 0) {
				mOrientationSensor = l.get(0);// 获取方向传感器

				if (mOrientationSensor != null) {
					isORientationSupport = true;
				}
			}
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void initMapAnim() {
		mAnimator = ValueAnimator.ofInt(1, 20);
		mAnimator.setDuration(1000);
		mAnimator.setRepeatCount(ValueAnimator.INFINITE);
		mAnimator.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				if (!mStopDrawing)
					refreshMap();
			}
		});
		mAnimator.start();
	}
	
	/**
	 * 是否绘制地图标签：POI名字
	 * @return 默认true 绘制
	 */
	public boolean isDrawText() {
		return drawText;
	}

	/**
	 * 设置是否绘制地图标签
	 * @param drawText 默认true 绘制
	 */
	public void setDrawText(boolean drawText) {
		this.drawText = drawText;
	}

	/**
	 * 调整方向传感器获取的值
	 * 
	 * @param degree
	 * @return
	 */
	private float normalizeDegree(float degree) {
		return (degree + 720) % 360;
	}

	private final SensorEventListener mOrientationSensorEventListener = new SensorEventListener() {
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {

		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			@SuppressWarnings("deprecation")
			float direction = event.values[SensorManager.DATA_X];
			mTargetDirection = normalizeDegree(direction);// 定位图标方向为正90度
		}
	};

	/**
	 * 开启方向传感器,如果需要刷新地图箭头指向，请调用此方法，时时改变定位指针方向
	 */
	public void startSensor() {
		if (Constants.ROTATE) {
			mStopDrawing = false;
			if (isORientationSupport) {
				sm.registerListener(mOrientationSensorEventListener,
						mOrientationSensor, SensorManager.SENSOR_DELAY_GAME);// 注册方向传感器监听器
				restartRefreshRunnable();
			}
		}
	}

	/**
	 * 移除方向传感器监听
	 */
	public void removeSensor() {
		if (Constants.ROTATE && isORientationSupport) {
			mStopDrawing = true;
			sm.unregisterListener(mOrientationSensorEventListener,
					mOrientationSensor);
			removeRefreshRunnable();
		}
	}

	// ---------------传感器end--------------

	/**
	 * 是否停止移动
	 * 
	 * @return
	 */
	public boolean isStopmove() {
		return stopmove;
	}

	/**
	 * @param stopmove
	 */
	public void setStopmove(boolean stopmove) {
		this.stopmove = stopmove;
	}

	/**
	 * 设置view的x坐标
	 * 
	 * @param viewdx
	 */
	public void setViewdx(int viewdx) {
		this.viewdx = viewdx;
	}

	/**
	 * 设置view的y坐标
	 * 
	 * @param viewdy
	 */
	public void setViewdy(int viewdy) {
		this.viewdy = viewdy;
	}

	void addchild(View childView, float f, float g) {
		viewdx = 0;
		viewdy = 0;
		viewx = f;
		viewy = g;
		viewpoint = fromLocation(new Location(f, g));
		viewleft = childView.getLeft();
		viewright = childView.getRight();
		viewtop = childView.getTop();
		viewbottom = childView.getBottom();
		deleteAllView();
		addView(childView);

	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		final View backView = getChildAt(0);
		backView.setVisibility(View.VISIBLE);

		backView.layout(
				0 - (getWidth() * Constants.buffer / 2 - getWidth() / 2),
				0 - (getHeight() * Constants.buffer / 2 - getHeight() / 2),
				getWidth()
						+ (getWidth() * Constants.buffer / 2 - getWidth() / 2),
				getHeight()
						+ (getHeight() * Constants.buffer / 2 - getHeight() / 2));// 1127

		if (getChildCount() > 1) {
			View popView = getChildAt(1);
			popView.layout((int) (viewleft + viewdx), (int) (viewtop + viewdy),
					(int) (viewright + viewdx), (int) (viewbottom + viewdy));
		}

	}

	// ------------------------获取方向相关变量－－－－－－－－－－

	/**
	 * 地图方向弧度
	 */
	public float mapangle = 0f;

	/**
	 * 得到地图方向角度，偏转角
	 * 
	 * @return 得到弧度，单位：弧度
	 */
	public float getMapAngle() {
		return mapangle;
	}

	/**
	 * 设置地图方向角度,偏转角
	 * 
	 * @param mapangle
	 *            弧度，单位：弧度
	 */
	public void setMapAngle(float mapangle) {
		this.mapangle = mapangle;
	}

	/**
	 * 是否可以拖动
	 * 
	 * @return
	 */
	public boolean isTouched() {
		return touched;
	}

	/**
	 * 设置是否拖动
	 * 
	 * @param touched
	 */
	public void setTouched(boolean touched) {
		this.touched = touched;
	}

	/**
	 * 是否跟踪，V2.4版本之后废弃
	 *
	 * @return
	 */
	@Deprecated
	public boolean isTrackMode() {
		return isTrackMode;
	}

	/**
	 * 设置跟踪模式,true：跟踪；跟踪模式：定位点始终保持在地图中心，当手动拖动地图，退出跟踪模式，V2.4版本之后废弃，
	 * 请使用setLocationCode()
	 *
	 * @param isTrackMode
	 *            是否跟踪
	 */
	@Deprecated
	public void setTrackMode(boolean isTrackMode) {
		this.isTrackMode = isTrackMode;
		if (isTrackMode) {
			locationMode = RMLocationMode.FOLLOW;
		} else {
			locationMode = RMLocationMode.NORMAL;
		}
	}

	public boolean isMenuvisible() {
		return menuvisible;
	}

	public void setMenuvisible(boolean menuvisible) {
		this.menuvisible = menuvisible;
	}

	/**
	 * 得到地图配置参数
	 * 
	 * @return MapConfig类型，此类型暂不公开说明
	 */
	public MapConfig getConfig() {
		return mConfig;
	}

	/**
	 * 重置比例尺
	 */
	public void resetscale() {
		if (getHeight() != 0 && getWidth() != 0 && mConfig != null
				&& mConfig.getBuildHeight() != 0
				&& mConfig.getBuildWidth() != 0) {
			mScale = getDefaultscale();
		}
		refreshMap();
	}

	/**
	 * 设置地图中心坐标，默认中心点不执行平移动画，请参见setCenter(Location location)
	 * 
	 * @param location
	 *            位置
	 */
	public void setCenter(Location location) {
		setCenter(location, false);
	}

	/**
	 * 设置地图中心坐标，如果运行动画，你设置的点将会平行移动到中心点
	 * 
	 * @param location
	 *            位置
	 * @param isRunAnimator
	 *            是否运行动画
	 */
	@SuppressLint("NewApi")
	public void setCenter(Location location, boolean isRunAnimator) {
		if (location == null) {
			return;
		}
		if (location.equals(mCurrentLocation)) {
			location = new Location(location.getX(), location.getY(),
					location.getFloor(), location.getBuildId());
		}

		float newX = location.getX();
		float newY = location.getY();
		// 防止超界
		float[] range = getRangeOfZoom(mScale);
		if (newX < range[0]) {
			newX = range[0];
		}
		if (newY < range[1]) {
			newY = range[1];
		}
		if (newX > range[2]) {
			newX = range[2];
		}
		if (newY > range[3]) {
			newY = range[3];
		}
		if (mCenterLocation != null && isRunAnimator) {
			if ((Math.abs(mCenterLocation.getX() - location.getX()) > DELTA_LENGTH || Math
					.abs(mCenterLocation.getY() - location.getY()) > DELTA_LENGTH)
					&& Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				final ValueAnimator ybum = ObjectAnimator.ofFloat(
						mCenterLocation, "y", mCenterLocation.getY(),
						location.getY() - DELTA_LENGTH);
				final ValueAnimator xbum = ObjectAnimator.ofFloat(
						mCenterLocation, "x", mCenterLocation.getX(),
						location.getX() - DELTA_LENGTH);
				mCenterAnim = new AnimatorSet();
				mCenterAnim.setDuration(ANIM_DURATION);
				mCenterAnim.setInterpolator(new AccelerateInterpolator());
				mCenterAnim.play(ybum).with(xbum);
				ybum.addUpdateListener(new AnimatorUpdateListener() {
					@Override
					public void onAnimationUpdate(ValueAnimator animation) {
						refreshMap();
					}
				});
				mCenterAnim.addListener(new AnimatorListener() {

					@Override
					public void onAnimationStart(Animator animation) {
					}

					@Override
					public void onAnimationRepeat(Animator animation) {
					}

					@Override
					public void onAnimationEnd(Animator animation) {
						ybum.end();
						xbum.end();
						refreshMap();
					}

					@Override
					public void onAnimationCancel(Animator animation) {
						ybum.cancel();
						xbum.cancel();
						refreshMap();
					}
				});
				mCenterAnim.start();
			} else {
				mCenterLocation = location;
			}
		} else {
			mCenterLocation = location;
			mGetlabels = true;
			refreshMap();
		}
	}

	/**
	 * 设置中心点x,y
	 * 
	 * @param x
	 *            横向坐标，实际位置，单位：米
	 * @param y
	 *            纵向坐标，实际位置，单位：米
	 * @param isRunAnimator
	 *            是否运行动画,true：从设置点到中心点执行平移动画，否则不执行
	 */
	public void setCenter(float x, float y, boolean isRunAnimator) {
		setCenter(new Location(x, y), isRunAnimator);
	}

	/**
	 * 设置中心点x,y,默认不执行中心点平移动画，请参见setCenter(float x, float y, boolean
	 * isRunAnimator)
	 * 
	 * @param x
	 *            横向坐标，实际位置，单位：米
	 * @param y
	 *            纵向坐标，实际位置，单位：米
	 */
	public void setCenter(float x, float y) {
		setCenter(new Location(x, y), false);
	}

	/**
	 * 获取中心坐标
	 * 
	 * @return 中心点实际坐标信息
	 */
	public Location getCenter() {
		return mCenterLocation;
	}

	/**
	 * 将实际坐标转化为屏幕坐标
	 * 
	 * @param location
	 *            地图实际坐标信息，location只需包含x,y两个参数
	 * @return 屏幕坐标信息，详细请查看PointInfo
	 */
	public PointInfo fromLocation(Location location) {
		if (location == null)
			return null;
		return fromLocation(location.getX(), location.getY());
	}

	/**
	 * 将实际坐标转化为屏幕坐标
	 * 
	 * @param location
	 *            地图实际坐标信息，location只需包含x,y两个参数
	 * @return 屏幕坐标信息，详细请查看PointInfo
	 */
	public PointInfo fromLocation(float x, float y) {
		float dis_x, dis_y;
		if (mapangle != 0) {
			dis_x = ((x - mCenterLocation.getX()) / mScale);
			dis_y = ((y - mCenterLocation.getY()) / mScale);
			float dx = (float) (dis_x * Math.cos(mapangle) - dis_y
					* Math.sin(mapangle));
			float dy = (float) (dis_x * Math.sin(mapangle) + dis_y
					* Math.cos(mapangle));
			return rotation(new PointInfo((float) (dx + getWidth() / 2),
					(float) (dy + getHeight() / 2)), 0);
		} else {

			dis_x = ((x - mCenterLocation.getX()) / mScale);

			dis_y = ((y - mCenterLocation.getY()) / mScale);

			return new PointInfo((dis_x + (float) getWidth() / 2),
					(dis_y + (float) getHeight() / 2));// 1127
		}
	}

	/**
	 * 将屏幕坐标转化为实际坐标
	 * 
	 * @param point
	 *            PointInfo类型，屏幕点信息只需包括x,y两个参数信息即可
	 * @return 实际地图坐标，详细请查看Location
	 */
	public Location fromPixels(PointInfo point) {
		if (mCenterLocation == null) {
			return null;
		}

		float x = (point.getX() - getWidth() / 2f) * mScale;
		float y = (point.getY() - getHeight() / 2f) * mScale;
		float dx = (float) (x * Math.cos(-mapangle) - y * Math.sin(-mapangle));
		float dy = (float) (x * Math.sin(-mapangle) + y * Math.cos(-mapangle));
		return new Location((float) (mCenterLocation.getX() + dx),
				(float) (mCenterLocation.getY() + dy));
	}

	public PointInfo rotation(PointInfo point, float angle) {
		if (angle == 0) {
			return point;
		}
		angle = angle / 180 * 3.1416f;
		double x1 = point.getX() * Math.cos(angle) - point.getY()
				* Math.sin(angle);
		double y1 = point.getY() * Math.cos(angle) + point.getX()
				* Math.sin(angle);
		return new PointInfo((float) x1, (float) y1);
	}

	/**
	 * 将屏幕坐标转化为实际坐标
	 * 
	 * @param x
	 *            屏幕横向坐标，单位：像素
	 * @param y
	 *            屏幕纵向坐标，单位：项目
	 * @return 实际位置坐标，详细请查Location类
	 */
	public Location fromPixels(float x, float y) {
		if (mCenterLocation == null) {
			return null;
		}
		return fromPixels(new PointInfo(x, y));
	}

	/**
	 * 初始化
	 */
	private void init(Context context) {
		mContext = context;
		mCustomPoiList = new ArrayList<POI>();
		priorityShowLevels = new ArrayList<Integer>();
		locationMode = RMLocationMode.NORMAL;// 默认是自由模式
		isUpdateMap = true;
		mmapView = new SurfaceView(context);
		addView(mmapView);
		mConfig = new MapConfig(this);
		initSensor(context);
		initMapAnim();
		multiTouchSupport = new MultiTouchSupport(getContext(),
				new MapTileViewMultiTouchZoomListener());
		if (Handlerlist.getInstance().getlistsize() == 0) {
			Handlerlist.getInstance().register(mHandler);
		}
		mmapView.getHolder().addCallback(new SurfaceHolder.Callback() {

			@Override
			public void surfaceChanged(SurfaceHolder holder, int format,
					int width, int height) {
				refreshMap();
			}

			@Override
			public void surfaceCreated(SurfaceHolder holder) {

				if (Handlerlist.getInstance().getlistsize() == 0) {
					Handlerlist.getInstance().register(mHandler);
				}
				if ((mScale == 0 || mScale == Float.POSITIVE_INFINITY)
						&& getHeight() != 0 && getWidth() != 0
						&& mConfig != null) {
					mScale = getDefaultscale();
				}
				Constants.VIEWHIGHT = getHeight();
				Constants.VIEWWIDTH = getWidth();
			}

			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				Handlerlist.getInstance().remove(mHandler);
			}
		});

		setLongClickable(true);
		setFocusable(true);
		animatedDraggingThread = new AnimateDraggingMapThread(this);
		gestureDetector = new GestureDetector(getContext(),
				new MapTileViewOnGestureListener());
		gestureDetector.setIsLongpressEnabled(true);
		gestureDetector.setOnDoubleTapListener(new OnDoubleTapListener() {

			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				// Log.log("MapView", String.format("single tap confirmed %d",
				// e.getAction()));
				if (mOnClickListener != null) {
					if (mOnClickListener.onTap(e)) {
						return true;
					}
				}
				return false;
			}

			@Override
			public boolean onDoubleTap(MotionEvent e) {
				if (DoubleTapable && mScale < ZOOM_MAX) {
					setScale(mScale / 2);
					return true;
				}
				return false;
			}

			@Override
			public boolean onDoubleTapEvent(MotionEvent e) {
				return false;
			}
		});
		try {
			logoBitmap = BitmapFactory.decodeStream(context.getAssets().open(
					"logo.png"));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		initLayer();
		initLocationLayer();
	}

	/**
	 * 是否允许MapView在界面最上层显示，这种情况一般出现于多个SurfaceView在一个界面中重叠显示的情况下，
	 * 需要将我们地图设置为true显示在最上层
	 * ，参见官方SurfaceView的setZOrderOnTop(boolean)方法介绍；我们地图默认为false：普通显示
	 * 
	 * @param isTop
	 *            true 则显示在多个surface的最上层；false 普通显示
	 */
	public void setZOrderOnTop(boolean isTop) {
		mmapView.setZOrderOnTop(isTop);
	}

	public SurfaceView getSurfaceView() {
		return mmapView;
	}

	/**
	 * 重置地图中心坐标为建筑物中心
	 */
	public void reset() {
		mCenterLocation = new Location(mConfig.getBuildWidth() / 2,
				mConfig.getBuildHeight() / 2, mConfig.getFloor(),
				mConfig.getBuildId());
	}

	/**
	 * 添加图层
	 * 
	 * @param layer
	 *            自定义图层
	 */
	public void addMapLayer(BaseMapLayer layer) {
		mMapLayers.add(layer);
	}
	
	/**
	 * 地图绘制时，标签是否同步绘制，地图会在各种手势操作和动画执行中自动设置true和false
	 * 
	 * @param isDraw
	 *            地图绘制时，标签是否同步绘制，地图会在各种手势操作和动画执行中自动设置true和false
	 */
	public void setGetLabels(boolean isDraw) {
		mGetlabels = isDraw;
	}

	/**
	 * 地图绘制时，得到标签是否同步绘制，地图会在各种手势操作和动画执行中自动设置true和false
	 * 
	 * @param isDraw
	 *            地图绘制时，得到标签是否同步绘制，地图会在各种手势操作和动画执行中自动设置true和false
	 */
	public boolean getGetLabeals() {
		return mGetlabels;
	}

	/**
	 * 移除某个图层
	 * 
	 * @param layer
	 *            指定你的图层对象
	 */
	public void removeMapLayer(BaseMapLayer layer) {
		if (mMapLayers.contains(layer))
			mMapLayers.remove(layer);
	}
	
	/**
	 * 是否包含某个图层
	 * @param layer
	 * @return
	 */
	public boolean contains(BaseMapLayer layer) {
		return mMapLayers.contains(layer);
	}

	/**
	 * 清除图层，清空所有数据
	 */
	public void clearMapLayer() {
		clearLayers();
		mCustomPoiList.clear();
		mSensorHandler.removeCallbacks(mCompassViewUpdater);
		if (mImageLocationLight != null && !mImageLocationLight.isRecycled()) {
			mImageLocationLight.recycle();
		}
		if (mImageLocationNormal != null && !mImageLocationNormal.isRecycled()) {
			mImageLocationNormal.recycle();
		}
		System.gc();
	}

	/**
	 * 设置比例尺，数值在ZOOM_MAX和ZOOM_MIN之间
	 * 
	 * @param scale
	 *            比例尺，单位：米/像素
	 */
	public void setScale(float scale) {
		if (scale > ZOOM_MAX) {
			scale = ZOOM_MAX;
		} else if (mScale < ZOOM_MIN) {
			scale = ZOOM_MIN;
		}
		mScale = scale;
		mGetlabels = true;
		refreshMap();
	}

	/**
	 * 初始化比例尺
	 */
	@Deprecated
	public void initScale() {
		if (mConfig.getBuildHeight() == 0 || mConfig.getBuildWidth() == 0) {
			return;
		}
		if (mConfig.isNewMap()) {
			mScale = getDefaultscale();
			reset();
		}
	}

	/**
	 * 屏幕宽度的一半
	 * 
	 * @return
	 */
	protected int getCenterPointX() {
		return getWidth() / 2;
	}

	/**
	 * 屏幕高度的一半
	 * 
	 * @return
	 */
	protected int getCenterPointY() {
		return getHeight() / 2;
	}

	/**
	 * 刷新地图
	 */
	public void refreshMap() {

		adjust();

		drawview();
		// refreshMapview();
		Message message = new Message();
		message.what = Constants.MAP_REFRESH;
		mHandler.sendMessage(message);
	}

	public void partialRefreshMapView() {
		invalidate();
	}

	/**
	 * 刷新地图界面
	 */
	@SuppressLint("WrongCall")
	public void refreshMapview() {
		// 绘制主图层
		Canvas c = mmapView.getHolder().lockCanvas();// 锁定画布
		synchronized (mmapView.getHolder()) {// 锁定holder
			drawMap(c);// 绘制每一帧画面
		}
		if (RMStringUtils.isEmpty(mConfig.getBuildId())
				|| RMStringUtils.isEmpty(mConfig.getFloor())) {
			return;
		}
		if (mCenterLocation == null) {
			reset();
		}

		// 越界判断
		getCenterPointX();
		getCenterPointY();
	}

	/**
	 * 拖拽动画，移动地图到其他位置
	 * 
	 * @param fromX
	 * @param fromY
	 * @param toX
	 * @param toY
	 */
	public void dragToAnimate(float fromX, float fromY, float toX, float toY) {
		float dx = (fromX - toX);
		float dy = (fromY - toY);

		moveTo(dx, dy);
	}

	/**
	 * 拖拽地图时移动地图，dx和dy分别是移动的x、y距离
	 * 
	 * @param dx
	 * @param dy
	 */
	protected void moveTo(float dx, float dy) {
		if (mCenterLocation == null) {
			return;
		}
		float newX;
		float newY;
		if (mapangle == 0) {
			newX = dx * mScale + mCenterLocation.getX();
			newY = dy * mScale + mCenterLocation.getY();
		} else {
			newX = (float) (dx * mScale * Math.cos(-mapangle) - dy * mScale
					* Math.sin(-mapangle))
					+ mCenterLocation.getX();
			newY = (float) (dy * mScale * Math.cos(-mapangle) + dx * mScale
					* Math.sin(-mapangle))
					+ mCenterLocation.getY();
		}

		mCenterLocation.setX(newX);
		mCenterLocation.setY(newY);
		refreshMap();

	}

	/**
	 * 计算地图边界
	 */
	private void adjust() {
		if (mCenterLocation == null) {
			return;
		}
		if (mScale == 0) {
			mScale = getDefaultscale();
		}
		if (mScale < ZOOM_MIN) {
			mScale = ZOOM_MIN;
		}
		if (mScale > ZOOM_MAX) {
			mScale = ZOOM_MAX;
		}
		float[] range = getRangeOfZoom(mScale);
		// float[] range = new float[]{0,
		// 0,mConfig.getBuildWidth(),mConfig.getBuildHeight()};
		float newX = mCenterLocation.getX();
		float newY = mCenterLocation.getY();
		if (newX < range[0]) {
			newX = range[0];
		}
		if (newY < range[1]) {
			newY = range[1];
		}
		if (newX > range[2]) {
			newX = range[2];
		}
		if (newY > range[3]) {
			newY = range[3];
		}
		mCenterLocation.setX(newX);
		mCenterLocation.setY(newY);
	}

	// 计算地图边界
	private float[] getRangeOfZoom(float scale) {
		return new float[] { 0, 0, mConfig.getBuildWidth(),
				mConfig.getBuildHeight() };
	}

	/**
	 * 设置定位点图片，定位点的闪烁是两张图片循环播放的效果，根据自己业务，两张图片合理设置
	 * 
	 * @param normal
	 *            相比较而言，这个属性是普通色的定位图片
	 * @param light
	 *            相比较而言，这个属性是亮色的定位图片
	 */
	public void setLocationIcon(Bitmap normal, Bitmap light) {
		if (light != null && !light.isRecycled()) {
			mImageLocationLight = light;
		}
		if (normal != null && !normal.isRecycled()) {
			mImageLocationNormal = normal;
		}
	}

	public Bitmap getLocationIconNormal() {
		return mImageLocationNormal;
	}

	public Bitmap getLocationIconLight() {
		return mImageLocationLight;
	}

	/**
	 * 设置定位点图片，定位点的闪烁是两张图片循环播放的效果，根据自己业务，两张图片合理设置
	 * 
	 * @param normalDrawableId
	 *            drawable*文件夹中的资源图片id
	 * @param lightDrawableId
	 *            drawable*文件夹中的资源图片id
	 */
	@SuppressWarnings("deprecation")
	public void setLocationIcon(int normalDrawableId, int lightDrawableId) {
		Drawable normalDraw = mContext.getResources().getDrawable(
				normalDrawableId);
		Drawable lightDraw = mContext.getResources().getDrawable(
				lightDrawableId);
		mImageLocationNormal = RMIOUtils.drawableToBitmap(normalDraw);
		mImageLocationLight = RMIOUtils.drawableToBitmap(lightDraw);
	}

	/**
	 * 清除定位点图片，还原为系统默认定位点图片
	 */
	public void clearLocationIconStyle() {
		try {
			if (Constants.ROTATE) {
				mImageLocationNormal = BitmapFactory.decodeStream(mContext
						.getAssets().open("icon_loc_normal.png"));
				mImageLocationLight = BitmapFactory.decodeStream(mContext
						.getAssets().open("icon_loc_light.png"));
			} else {
				mImageLocationNormal = BitmapFactory.decodeStream(mContext
						.getAssets().open("icon_locr_normal.png"));
				mImageLocationLight = BitmapFactory.decodeStream(mContext
						.getAssets().open("icon_locr_light.png"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setOnTapListener(OnTapListener listener) {
		mOnTapListener = listener;
	}

	public void setOnClickListener(OnTapListener listener) {
		mOnClickListener = listener;
	}

	public void setOnZoomChangedListener(OnZoomChangedListener listener) {
	}

	public void setOnMapModeChangedListener(OnMapModeChangedListener listener) {
		mOnMapModeChangedListener = listener;
	}

	public void removeOnMapModeChangedListener() {
		mOnMapModeChangedListener = null;
	}

	@SuppressLint({ "NewApi", "ClickableViewAccessibility" })
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if (mConfig == null || mConfig.getDrawMap() == null
				|| mConfig.getDrawMap().getLayer() == null
				|| mConfig.getDrawMap().getLayer().shapes == null) {
			return false;
		}
		touched = true;
		// 如果地图在拖拽动画，则停止动画
		if (locationMode != RMLocationMode.NORMAL) {
			locationMode = RMLocationMode.NORMAL;
			if (mCenterAnim != null) {
				mCenterAnim.cancel();
			}
		}
		// 移动地图时，地图退出跟踪模式
		if (mOnMapModeChangedListener != null) {
			mOnMapModeChangedListener.onMapModeChanged();
		}

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			animatedDraggingThread.stopAnimating();
			setfling(true);
			mGetlabels = false;
			stopmove = true;
			if (!ismove) {
				ismove = true;
			}
		}
		// 点击到图层的处理
		if (mOnTapListener != null) {
			if (mOnTapListener.onTap(event)) {
				for (int i = mMapLayers.size() - 1; i >= 0; i--) {
					if (mMapLayers.get(i) instanceof TapPOILayer) {
						((TapPOILayer) mMapLayers.get(i)).onAttach();
					}
				}
				return true;
			}
		}

		if (event.getAction() == MotionEvent.ACTION_UP
				|| event.getAction() == MotionEvent.ACTION_CANCEL) {
			mGetlabels = true;
			setfling(false);
			stopmove = false;
			refreshMap();

		}
		// 多点触控的处理
		if (!multiTouchSupport.onTouchEvent(event)) {
			gestureDetector.onTouchEvent(event);
		}

		return true;
	}

	/**
	 * 拖拽地图动画,地图手势操作控制器
	 * 
	 * @author dingtao
	 *
	 */
	private class MapTileViewOnGestureListener implements OnGestureListener {
		@Override
		public boolean onDown(MotionEvent e) {
			return false;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {

			return false;
		}

		@Override
		public void onLongPress(MotionEvent e) {
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {

			if (Roamable) {
				moveTo(distanceX, distanceY);
			}
			return true;
		}

		@Override
		public void onShowPress(MotionEvent e) {
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			if (e.getAction() == MotionEvent.ACTION_UP) {

				// mMainLayer.mGetlabels=true;
			}

			setfling(false);
			stopmove = false;
			setfling(false);
			refreshMap();
			return false;
		}
	}

	/**
	 * 双指缩放
	 * 
	 * @author dingtao
	 *
	 */
	private class MapTileViewMultiTouchZoomListener implements
			MultiTouchZoomListener {
		private float initialMultiTouchZoom;
		private float oldangle;
		private float oldscale;

		@Override
		public void onZoomEnded(float distance, float relativeToStart) {
			if (mScale != oldscale || mapangle != oldangle) {
				mGetlabels = true;
				setfling(false);
				stopmove = false;
				refreshMap();
			}

		}

		@Override
		public void onGestureInit(float x1, float y1, float x2, float y2) {
		}

		@Override
		public void onZoomStarted(float distance, PointF centerPoint) {
			// mMainLayer.mGetlabels=true;
			initialMultiTouchZoom = mScale;
			oldangle = mapangle;
			oldscale = mScale;
			mGetlabels = false;
		}

		@Override
		public void onZooming(float distance, float relativeToStart,
				double radians) {
			if (!Rotateable) {
				radians = 0;
			}
			if (!Zoomable) {
				relativeToStart = 1;
			}
			stopmove = true;
			mGetlabels = false;
			float calcZoom = initialMultiTouchZoom / relativeToStart;
			if (calcZoom < ZOOM_MIN) {
				calcZoom = ZOOM_MIN;
			}
			if (calcZoom > ZOOM_MAX) {
				calcZoom = ZOOM_MAX;
			}
			if (mScale != calcZoom) {
				mScale = calcZoom;
				refreshMap();
			}
			if (radians != 0) {
				mapangle = oldangle + (float) radians;
				refreshMap();
			}
		}
	}

	public interface OnTapListener {
		public boolean onTap(MotionEvent e);
	}

	public interface OnZoomChangedListener {
		public void onZoomChanged(float zoom);
	}

	public interface OnMapModeChangedListener {
		public void onMapModeChanged();
	}

	/**
	 * 得到地图比例尺
	 * 
	 * @return 地图比例尺，单位m/px：米每像素
	 */
	public float getScale() {
		return mScale;
	}

	/**
	 * 设置当前建筑物ID，V2.0之前的方法，已经没有实际作用，请尽快移除，我们将在后续几个版本中移除
	 * 
	 * @param id
	 *            建筑物ID
	 */
	@Deprecated
	public void setCurrentBuildId(String id) {
		// mConfig.setCurrentBuildId(id);
	}

	/**
	 * 设置当前位置以及是否跟随，V2.3版本之后，此方法不建议使用（我们可能在后续几个版本删除此方法），isTrackMode不再会有任何效果，
	 * 请使用setMyCurrentLocation (RMLocation location
	 * )，我们在测试中发现这种情况下跟随模式是有bug的：正常业务是：如果使用setTrackMode(true)则进入跟随模式，
	 * 一旦拖动地图，则从跟随模式自动退出变成自由模式
	 * ；但V2.3版本之前此方法每次调用如果都设置true，用户在拖动情况下，地图的跟随和自由会不停的切换，这种效果是用户不能忍受的。
	 * 
	 * @param location
	 *            位置，RMLocation类型
	 * @param isTrackMode
	 *            是否跟随，此参数已经失效，请尽快使用setMyCurrentLocation(RMLocation location)替代
	 */
	@Deprecated
	public void setMyCurrentLocation(RMLocation location, boolean isTrackMode) {
		if (location.getError() == 0) {
			Location loc = new Location(location.getX(), location.getY(),
					location.getFloor(), location.getBuildID());
			setMyCurrentLocation(loc);
		}
	}

	/**
	 * 设置当前位置以及是否跟随，V2.3版本之后，此方法不建议使用，isTrackMode不再会有任何效果，
	 * 请使用setMyCurrentLocation (RMLocation location
	 * )，我们在测试中发现这种情况下跟随模式是有bug的：正常业务是：如果使用setTrackMode(true)则进入跟随模式，
	 * 一旦拖动地图，则从跟随模式自动退出变成自由模式
	 * ；但V2.3版本之前此方法每次调用如果都设置true，用户在拖动情况下，地图的跟随和自由会不停的切换，这种效果是用户不能忍受的。
	 * 
	 * @param location
	 *            位置，Location类型
	 * @param isTrackMode
	 *            是否跟随，此参数已经失效，请尽快使用setMyCurrentLocation(RMLocation location)替代
	 */
	@Deprecated
	public void setMyCurrentLocation(Location location, boolean isTrackMode) {
		setMyCurrentLocation(location);
	}

	/**
	 * 设置当前位置
	 * 
	 * @param location
	 *            位置，RMLocation类型
	 */
	public void setMyCurrentLocation(RMLocation location) {
		if (location.getError() == 0) {
			setRadius(location.getAccuracy());
			Location loc = new Location(location.getX(), location.getY(),
					location.getFloor(), location.getBuildID());
			setMyCurrentLocation(loc);
		}
	}

	/**
	 * 得到我的当前位置
	 * 
	 * @return
	 */
	public Location getMyCurrentLocation() {
		return mCurrentLocation;
	}

	/**
	 * 设置当前位置，location类型是封装了RMLocatino数据，具有一定的局限性，比方说：精度，方向等数值，
	 * 建议使用setMyCurrentLocation( RMLocation location)
	 * 
	 * @param location
	 *            位置，Location类型
	 */
	@SuppressLint("NewApi")
	@Deprecated
	public void setMyCurrentLocation(Location location) {
		if (mMapLayers.size() != 0) {
			int size = mMapLayers.size();
			for (int i = 0; i < size; i++) {
				BaseMapLayer layer = mMapLayers.get(i);
				if (layer instanceof RouteLayer) {// 如果正在导航，则采用定位位置强制并线
					if (((RouteLayer) layer).isNavigating()) {
						((RouteLayer) layer).updateLocation(location);
					}
				}
			}
		}
		if (location == null) {
			if (mCurrentLocation != null) {
				mCurrentLocation = null;
				refreshMap();
			}
			return;
		} else {
			if (RMStringUtils.isEmpty(location.getBuildId())
					|| RMStringUtils.isEmpty(location.getFloor())
					|| !location.getBuildId().equals(mConfig.getBuildId())
					|| !location.getFloor().equals(mConfig.getFloor())) {// 如果新传入的位置字段为空或者位置与地图不相符则直接return掉
				return;
			}
		}

		if (mCurrentLocation != null
				&& mCurrentLocation.getBuildId().equals(location.getBuildId())
				&& mCurrentLocation.getFloor().equals(location.getFloor())) {
			if (locationMode == RMLocationMode.NORMAL) {// 如果是自由模式，那么定位点平滑移动
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
						&& (Math.abs(mCurrentLocation.getX() - location.getX()) > DELTA_LENGTH || Math
								.abs(mCurrentLocation.getY() - location.getY()) > DELTA_LENGTH)) {
					ValueAnimator ybum = ObjectAnimator.ofFloat(
							mCurrentLocation, "y", mCurrentLocation.getY(),
							location.getY() - DELTA_LENGTH);
					ValueAnimator xbum = ObjectAnimator.ofFloat(
							mCurrentLocation, "x", mCurrentLocation.getX(),
							location.getX() - DELTA_LENGTH);
					mLocationAnim = new AnimatorSet();
					mLocationAnim.setDuration(ANIM_DURATION);
					mLocationAnim.play(ybum).with(xbum);
					ybum.addUpdateListener(new AnimatorUpdateListener() {
						@Override
						public void onAnimationUpdate(ValueAnimator animation) {
							refreshMap();
						}
					});
					mLocationAnim.addListener(new AnimatorListener() {

						@Override
						public void onAnimationStart(Animator animation) {

						}

						@Override
						public void onAnimationRepeat(Animator animation) {

						}

						@Override
						public void onAnimationEnd(Animator animation) {
							// mCurrentLocation = location;
							// refreshMap();
						}

						@Override
						public void onAnimationCancel(Animator animation) {
							// mCurrentLocation = location;
							// refreshMap();
						}
					});
					mLocationAnim.start();
				} else {
					mCurrentLocation = location;
				}
			} else {// 如果是跟随模式，定位点是中心，移动地图
				mCurrentLocation = location;
			}
		} else {
			mCurrentLocation = location;
		}
		if (locationMode != RMLocationMode.NORMAL) {// 如果不是自由模式，肯定需要执行定位点居中动画
			if (locationMode == RMLocationMode.COMPASS) {
				setCenter(mCurrentLocation, false);// 每次设置位置，需要执行动画
			} else
				setCenter(mCurrentLocation, true);// 每次设置位置，需要执行动画
		}
		refreshMap();
	}

	public Location locationByRoad(Location location) {
		Edge[] mEdges = mConfig.getDrawMap().getLayer().edges;
		Coord[] mCoords = mConfig.getDrawMap().getLayer().coords;
		if (mEdges != null) {
			double distance = Float.MAX_VALUE;
			int NearIndex = -1;
			for (int i = 0; i < mEdges.length; i++) {
				double temp = Geometry
						.pointToLine(
								mCoords[mEdges[i].mPointIds[0]].mX / 1000f,
								mCoords[mEdges[i].mPointIds[0]].mY / 1000f,
								mCoords[mEdges[i].mPointIds[mEdges[i].mPointIds.length - 1]].mX / 1000f,
								mCoords[mEdges[i].mPointIds[mEdges[i].mPointIds.length - 1]].mY / 1000f,
								location.getX(), location.getY());
				if (temp < distance) {
					distance = temp;
					NearIndex = i;
				}
			}
			if (NearIndex != -1) {
				location = Geometry
						.projectpoint(
								mCoords[mEdges[NearIndex].mPointIds[0]].mX / 1000f,
								mCoords[mEdges[NearIndex].mPointIds[0]].mY / 1000f,
								mCoords[mEdges[NearIndex].mPointIds[mEdges[NearIndex].mPointIds.length - 1]].mX / 1000f,
								mCoords[mEdges[NearIndex].mPointIds[mEdges[NearIndex].mPointIds.length - 1]].mY / 1000f,
								location.getX(), location.getY());
			}
		}
		return location;
	}

	/**
	 * 初始化地图配置以及更新地图，
	 * 
	 * @param buildId
	 *            建筑物ID,不能为空
	 * @param floor
	 *            楼层，不能为空，例：F1
	 */
	public void initMapConfig(String buildId, String floor) {
		if (getHeight() != 0 && getWidth() != 0) {
			loadMap(buildId, floor);
		} else {
			this.buildId = buildId;
			this.floor = floor;
			Message msg = mHandler.obtainMessage(LOAD_MAP);
			mHandler.sendMessageDelayed(msg, 800);
		}
	}

	/**
	 * 初始化地图配置，加载地图
	 * 
	 * @param buildId
	 *            建筑物ID,不能为空
	 * @param floor
	 *            楼层，不能为空，例：20010
	 */
	public void initMapConfig(final String buildId, final int floor) {
		initMapConfig(buildId, RMStringUtils.floorTransform(floor));

	}

	private String buildId, floor;// 仅仅是给加载地图的预留

	/**
	 * 加载地图
	 * 
	 * @param buildId
	 *            建筑物ID
	 * @param floor
	 *            楼层
	 */
	private void loadMap(String buildId, String floor) {
		if (RMStringUtils.isEmpty(buildId) || RMStringUtils.isEmpty(floor))
			return;
		// boolean resetscale;
		// if (mConfig.getBuildId() == null) {
		// resetscale = true;
		// } else {
		// resetscale = !buildId.equalsIgnoreCase(mConfig.getBuildId());
		// }
		this.selectPoi = null;
		mConfig.initMapConfig(buildId, floor);
		// if (resetscale) {
		// resetscale();
		// reset();
		// mapangle = 0;
		// }
	}

	/**
	 * 得到默认比例尺
	 * 
	 * @return 比例尺
	 */
	public float getDefaultscale() {
		return Math.max(((float) mConfig.getBuildHeight())
				/ (float) getHeight(), (float) mConfig.getBuildWidth()
				/ (float) getWidth())
				/ MAP_SCREEN_SCALE;
	}

	private int sortNo;

	/**
	 * 排序规则，1.style;2.level;3.5级绘图模式
	 * 
	 * @param sortNo
	 */
	public void setMapSortRule(int sortNo) {
		this.sortNo = sortNo;
	}

	/**
	 * 排序规则，1.style;2.level;3.5级绘图模式
	 */
	public int getSortNo() {
		return sortNo;
	}

	private ArrayList<Integer> levelist = new ArrayList<Integer>();

	/**
	 * 添加优先绘制的POI层级
	 * 
	 * @param level
	 */
	public void addDrawPoiHighLevel(int level) {
		levelist.add(level);
	}

	public ArrayList<Integer> getHighLevel() {
		return levelist;
	}

	/**
	 * 得到建筑物ID
	 * 
	 * @return 建筑物ID
	 */
	public String getBuildId() {
		return mConfig.getBuildId();
	}

	/**
	 * 得到楼层
	 * 
	 * @return
	 */
	public String getFloor() {
		return mConfig.getFloor();
	}

	/**
	 * 是否可以点击
	 * 
	 * @return
	 */
	public boolean isTapable() {
		return mTapable;
	}

	/**
	 * 设置点击
	 * 
	 * @param mTapable
	 */
	public void setTapable(boolean mTapable) {
		this.mTapable = mTapable;
	}

	/**
	 * 是否可以双击
	 * 
	 * @return
	 */
	public boolean isDoubleTapable() {
		return DoubleTapable;
	}

	/**
	 * 设置双击
	 * 
	 * @param doubleTapable
	 */
	public void setDoubleTapable(boolean doubleTapable) {
		DoubleTapable = doubleTapable;
	}

	public boolean isRoamable() {
		return Roamable;
	}

	public void setRoamable(boolean roamable) {
		Roamable = roamable;
	}

	/**
	 * 是否旋转
	 * 
	 * @return
	 */
	public boolean isRotateable() {
		return Rotateable;
	}

	/**
	 * 设置旋转
	 * 
	 * @param rotateable
	 */
	public void setRotateable(boolean rotateable) {
		Rotateable = rotateable;
	}

	/**
	 * 是否缩放
	 * 
	 * @return
	 */
	public boolean isZoomable() {
		return Zoomable;
	}

	/**
	 * 设置缩放
	 * 
	 * @param zoomable
	 */
	public void setZoomable(boolean zoomable) {
		Zoomable = zoomable;
	}

	/**
	 * 删除所有view项
	 */
	public void deleteAllView() {
		int size = this.getChildCount();
		for (int i = 1; i < size; i++) {
			this.removeViewAt(1);
		}
	}

	/**
	 * 是否填充
	 * 
	 * @return
	 */
	public boolean Isfling() {
		return isfling;
	}

	/**
	 * 设置填充
	 * 
	 * @param isfling
	 */
	public void setfling(boolean isfling) {
		this.isfling = isfling;
	}

	private float touchdx;
	private float touchdy;

	/**
	 * 绘制view
	 */
	public void drawview() {

		if (getChildCount() > 1) {
			PointInfo mPoint = fromLocation(new Location(viewx, viewy));
			if (viewpoint != null) {
				float oldx = viewdx;
				float oldy = viewdy;
				viewdx = (int) -(viewpoint.getX() - mPoint.getX());
				viewdy = (int) -(viewpoint.getY() - mPoint.getY());
				touchdx = Math.abs(viewdx - oldx);
				touchdy = Math.abs(viewdy - oldy);
				View popView = getChildAt(1);
				if (touchdx == 0 && touchdy == 0) {
					return;
				}
				popView.requestLayout();
				popView.invalidate();
			}
		}
	}

	@Override
	public void setVisibility(int visibility) {
		super.setVisibility(visibility);
		for (int i = 0; i < getChildCount() - 1; i++) {
			getChildAt(i).setVisibility(visibility);
		}
		// mmapView.setVisibility(visibility);
	}

	public POI getNestestPoi(float x, float y) {
		double disMin = Float.MAX_VALUE;
		int index = 0;
		Shape[] shapes = getConfig().getDrawMap().getLayer().shapes;
		if (getConfig() == null || getConfig().getDrawMap() == null
				|| shapes == null) {
			return null;
		}
		for (int i = 0; i < shapes.length; i++) {
			if (RMStringUtils.isEmpty(shapes[i].mName)) {
				continue;
			}
			if (shapes[i].mCenter == null) {
				continue;
			}
			double dis = Math.pow(shapes[i].mCenter.mX / 1000f - x, 2)
					+ Math.pow(shapes[i].mCenter.mY / 1000f + y, 2);
			if (dis < disMin) {
				disMin = dis;
				index = i;
			}
		}

		POI mPoi = new POI(shapes[index].mId, shapes[index].mName, getConfig()
				.getBuildId(), getConfig().getFloor(),
				shapes[index].mCenter.mX / 1000f, shapes[index].mCenter.mY
						/ -1000f);
		return mPoi;

	}

	/**
	 * 将地图绘制成jpg
	 * 
	 * @param name
	 */
	public void drawMapToJpg(String name) {
		Bitmap mBitmap = null;

		Shape[] shapes = mConfig.getDrawMap().getLayer().shapes;

		if (shapes == null || shapes.length == 0) {
			return;
		}

		PointInfo theMaxPoint = skewCoord(new PointInfo(
				mConfig.getBuildWidth(), mConfig.getBuildHeight()));
		toJpgScale = Math.max(
				theMaxPoint.getY()
						/ Math.max(Constants.VIEWHIGHT, Constants.VIEWWIDTH),
				theMaxPoint.getX()
						/ Math.min(Constants.VIEWHIGHT, Constants.VIEWWIDTH)) * 1000;
		try {
			mBitmap = Bitmap.createBitmap((int) (theMaxPoint.getX()
					/ toJpgScale * 1000), (int) (theMaxPoint.getY()
					/ toJpgScale * 1000), Bitmap.Config.ARGB_8888);
		} catch (Exception e) {
			return;
		}
		Canvas canvas = new Canvas(mBitmap);
		Rect dirty = new Rect(0, 0, (int) mConfig.getBuildWidth() * 1000,
				(int) mConfig.getBuildHeight() * 1000);
		mConfig.getDrawMap().drawShape(canvas, dirty, true);
		canvas.save(Canvas.ALL_SAVE_FLAG);
		canvas.restore();
		if (mBitmap != null) {
			File f = new File(RMFileUtil.getMapDataDir() + name + ".jpg");
			try {
				f.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			FileOutputStream fOut = null;
			try {
				fOut = new FileOutputStream(f);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
			try {
				fOut.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				fOut.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public PointInfo skewCoord(PointInfo mPoint) {
		float y = (float) ((mPoint.getY()) * Math.cos(Math
				.toRadians(Constants.MAP_SKEW_ANGLE)));
		float x = (float) (mPoint.getX() + y
				* Math.tan(Math.toRadians(Constants.MAP_SKEW_ANGLE)));
		// return mPoint;
		return new PointInfo(x, y);
	}

	public void changeTapPOIViewSize(int width, int height) {
		if (getChildCount() < 2) {
			return;
		}
		viewleft = viewleft + (viewright - viewleft - width) / 2;
		viewright = viewright - (viewright - viewleft - width) / 2;
		viewtop = viewtop + (viewbottom - viewtop - height);
		getChildAt(1).requestLayout();
	}

	public void setMapSkewAngle(float angle) {
		Constants.MAP_SKEW_ANGLE = angle;
	}

	public boolean isInMapBound(float x, float y) {
		Shape[] shapes = mConfig.getDrawMap().getLayer().shapes;
		if (mConfig == null || mConfig.getDrawMap() == null || shapes == null) {
			return false;
		}
		for (int i = 0; i < shapes.length; i++) {
			Shape mShape = shapes[i];
			if (mShape.mStyle == 8) {
				if (mConfig.getDrawMap().getLayer()
						.inPolygon(mShape, x * 1000, y * 1000)) {
					return true;
				} else {
					return false;
				}
			}
		}
		return false;
	}

	/**
	 * 根据任意x,y得到所属POI
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public POI selectPoi(float x, float y) {
		Shape[] shapes = mConfig.getDrawMap().getLayer().shapes;
		if (mConfig == null || mConfig.getDrawMap() == null || shapes == null) {
			return null;
		}
		for (int i = 0; i < shapes.length; i++) {
			Shape mShape = shapes[i];

			if (mConfig.getDrawMap().getLayer()
					.inPolygon(mShape, x * 1000, y * 1000)) {
				POI poi = new POI(mShape.mId, mShape.mName,
						mConfig.getBuildId(), mConfig.getFloor(),
						mShape.mCenter.mX / 1000f, mShape.mCenter.mY / 1000f);
				poi.setFloor(mConfig.getFloor());
				poi.setBuildId(mConfig.getBuildId());
				poi.setType(mShape.mType);
				return poi;
			}
		}
		return null;
	}

	/**
	 * 设置背景色
	 */
	public void setMapBackgroundColor(int mapBackgroundColor) {
		this.mapBackgroundColor = mapBackgroundColor;
	}

	public boolean isDrawLabel() {
		return drawLabel;
	}

	public void setDrawLabel(boolean drawLabel) {
		this.drawLabel = drawLabel;
	}

	private HashMap<String, Integer> customColors = new HashMap<String, Integer>();

	public HashMap<String, Integer> getCustomColors() {
		return customColors;
	}

	public void setCustomColors(String name, int color) {
		customColors.put(name, color);
	}

	/**
	 * 初始化图层
	 */
	public void initLayer() {
		// mBitmapPaint = new Paint(Paint.DITHER_FLAG);
		mMapLayers = new ArrayList<BaseMapLayer>();
		mLogoPosition = 0;

		if (getHeight() != 0 && getWidth() != 0 && mConfig != null
				&& mConfig.getBuildHeight() != 0
				&& mConfig.getBuildWidth() != 0 && mScale == 0) {
			mScale = getDefaultscale();
		}
		setOnTapListener(new OnTapListener() {
			@SuppressLint("WrongCall")
			@Override
			public boolean onTap(MotionEvent e) {
				if (mMapLayers == null) {
					return false;
				}

				boolean result = false;
				int size = mMapLayers.size();
				for (int i = size - 1; i > -1; i--) {
					if (mMapLayers.get(i).onTap(e)) {
						result = true;
						setfling(false);
						mGetlabels = true;
						break;
					}
				}
				if (result) {
					onDraw(null);
				}
				return result;
			}
		});
	}

	public HashMap<String, String> getPoiIconMap() {
		return poiIconMap;
	}

	/**
	 * 添加poi的图标，例如：实现所有建筑物打开时，其中肯德基都要变成图标，则使用这个方法
	 * 
	 * @param poiName
	 *            地图上显示的poi名字
	 * @param assetsIconName
	 *            assets文件夹下图标的名字
	 */
	public void putPoiIcon(String poiName, String assetsIconName) {
		poiIconMap.put(poiName, assetsIconName);
	}

	/**
	 * 移除poi的图标
	 * 
	 * @param poiName
	 *            地图上显示的poi名字
	 */
	public void removePoiIcon(String poiName) {
		if (poiIconMap.containsKey(poiName)) {
			poiIconMap.remove(poiName);
		}
	}
	
	Path path = new Path();
	
	public void drawCustom(Canvas c) {

	}

	/**
	 * 画地图
	 * 
	 * @param c
	 */
	public void drawMap(Canvas c) {
		if (c != null) {
			c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
			drawCustom(c);
			c.drawColor(mapBackgroundColor);
			if (mConfig == null || mConfig.getDrawMap() == null
					|| mConfig.getDrawMap().getLayer() == null
					|| mConfig.getDrawMap().getLayer().shapes == null) {
				mmapView.getHolder().unlockCanvasAndPost(c);
				return;
			}
			float x = getCenter().getX();
			float y = getCenter().getY();

			// Log.i("rtmap", "比例尺："+mScale);

			mConfig.getDrawMap().init(x, y, mScale, getWidth(), getHeight(),
					selectPoi);
			mConfig.getDrawMap().drawShape(c);
			mConfig.getDrawMap().drawLabels(c, getContext(), mGetlabels);

			drawLayers(c);// 绘制图层
			drawlogo(c);// 绘制图标
			drawLocation(c);// 绘制定位点
			mmapView.getHolder().unlockCanvasAndPost(c);
		}
	}

	/**
	 * 添加自定义样式的POI，其中绘图使用POI参数drawStyle和textStyle为自定义属性，支持图形与文字样式自定义
	 * 
	 * @param poi
	 *            参见{@link POI}
	 */
	public void addCustomPoi(POI poi) {
		if (poi != null)
			mCustomPoiList.add(poi);
	}

	/**
	 * 添加一组自定义样式的POI,其中绘图使用POI参数drawStyle和textStyle为自定义属性，支持图形与文字样式自定义
	 * 
	 * @param poiList
	 *            poi列表，参见{@link POI}
	 */
	public void addCustomPoiList(List<POI> poiList) {
		if (poiList != null && poiList.size() > 0) {
			mCustomPoiList.addAll(poiList);
		}
	}

	/**
	 * 得到自定义样式的POI列表
	 * 
	 * @return poi列表，参见{@link POI}
	 */
	public ArrayList<POI> getCustomPoiList() {
		return mCustomPoiList;
	}

	/**
	 * 移除自定义POI
	 * 
	 * @param poi
	 *            参见{@link POI}
	 */
	public void clearCustomPoi(POI poi) {
		if (poi != null && mCustomPoiList.contains(poi))
			mCustomPoiList.remove(poi);
	}

	/**
	 * 清空POI列表
	 */
	public void clearCustomPoiList() {
		mCustomPoiList.clear();
	}

	/**
	 * 设置是否绘制RTMAP智慧图logo
	 * 
	 * @param drawLogo
	 *            默认为true绘制
	 */
	public void setDrawLogo(boolean drawLogo) {
		this.drawLogo = drawLogo;
	}

	/**
	 * 是否绘制RTMAP智慧图logo
	 * 
	 * @return true 绘制logo，false 不绘制
	 */
	public boolean isDrawLogo() {
		return drawLogo;
	}

	private void drawlogo(Canvas canvas) {
		if (logoBitmap != null && !logoBitmap.isRecycled() && drawLogo) {
			int logoWidth = getWidth() / 7;
			int logopad = getWidth() / 30;
			Rect iconrect = new Rect();
			switch (mLogoPosition) {
			case Constants.TOP_RIGHT:
				iconrect.left = getWidth() - logoWidth - logopad;
				iconrect.top = 0;
				iconrect.right = getWidth();
				iconrect.bottom = logopad;
				break;

			case Constants.TOP_LEFT:
				iconrect.left = 0;
				iconrect.top = 0;
				iconrect.right = logoWidth + logopad;
				iconrect.bottom = logopad;
				break;

			case Constants.BOTTOM_RIGHT:
				iconrect.left = getWidth() - logoWidth - logopad;
				iconrect.top = (int) (getHeight() - logopad - 5);
				iconrect.right = getWidth();
				iconrect.bottom = (int) (getHeight() - 5);
				break;

			case Constants.BOTTOM_LEFT:
				iconrect.left = 0;
				iconrect.top = getHeight() - logopad;
				iconrect.right = logoWidth + logopad;
				iconrect.bottom = getHeight();
				break;

			default:
				break;
			}

			Paint mPaint = new Paint();
			mPaint.setAlpha(180);
			canvas.drawBitmap(logoBitmap, null, iconrect, mPaint);
		}
	}

	/**
	 * 添加图层
	 * 
	 * @param layer
	 */
	public void addLayer(BaseMapLayer layer) {

		mMapLayers.add(layer);
	}

	/**
	 * 绘制图层
	 * 
	 * @param canvas
	 */
	@SuppressLint("WrongCall")
	private void drawLayers(Canvas canvas) {

		int size = mMapLayers.size();
		for (int i = 0; i < size; i++) {
			mMapLayers.get(i).onDraw(canvas);
		}
	}

	/**
	 * 移除所有图层并且删除图层中的数据
	 */
	public void clearLayers() {
		if (mConfig.getDrawMap() != null) {
			mConfig.getDrawMap().setSelectPoi(null);
		}
		selectPoi = null;
		int size = mMapLayers.size();
		for (int i = 0; i < size; i++) {
			mMapLayers.get(i).destroyLayer();
		}
		if (mMapLayers != null) {
			mMapLayers.clear();
		}
		refreshMap();
	}

	/**
	 * 得到POI点击图层
	 * 
	 * @return
	 */
	public TapPOILayer getTapPOILayer() {
		if (mMapLayers == null || mMapLayers.size() == 0) {
			return null;
		}

		int size = mMapLayers.size();
		for (int i = 0; i < size; i++) {
			BaseMapLayer layer = mMapLayers.get(i);
			if (layer instanceof TapPOILayer) {
				return (TapPOILayer) layer;
			}
		}
		return null;
	}

	/**
	 * 初始化定位点图层
	 */
	private void initLocationLayer() {
		clearLocationIconStyle();// 清除定位样式
		mPaint1 = new Paint();
		mPaint1.setColor(COLOR_LOCATION_CIRCLE);
		mPaint1.setDither(true);
		mPaint1.setStrokeCap(Paint.Cap.ROUND);
		mPaint1.setStrokeJoin(Paint.Join.ROUND);
		mPaint1.setStyle(Paint.Style.FILL);

		mPaint2 = new Paint();
		mPaint2.setColor(COLOR_LOCATION_OUTLINE);
		mPaint2.setStyle(Paint.Style.STROKE);
		mPaint2.setAntiAlias(true);
		mPaint2.setStrokeWidth(LOCATION_OUTLINE_WIDTH);
	}

	/**
	 * 绘制定位点位置
	 * 
	 * @param canvas
	 */
	private void drawLocation(Canvas canvas) {
		PaintFlagsDrawFilter pfd = new PaintFlagsDrawFilter(0,
				Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
		canvas.setDrawFilter(pfd);
		if (getConfig().getDrawMap() == null || mCurrentLocation == null
				|| !mCurrentLocation.getBuildId().equals(mConfig.getBuildId())
				|| !mCurrentLocation.getFloor().equals(mConfig.getFloor())) {
			return;
		}
		PointInfo point;
		// if (locationMode != RMLocationMode.NORMAL) {// 如果不是自由模式
		// point = fromLocation(getCenter());
		// } else {
		point = fromLocation(mCurrentLocation);
		// }
		canvas.drawCircle(point.getX(), point.getY(),
				(float) (radius / mScale), mPaint1);

		canvas.drawCircle(point.getX(), point.getY(),
				(float) (radius / mScale), mPaint2);

		canvas.save();
		if (locationMode != RMLocationMode.COMPASS) {
			canvas.rotate(mLocationAngle, point.getX(), point.getY());
		}
		Matrix matrix = new Matrix();
		matrix.postTranslate(point.getX() - mImageLocationLight.getWidth() / 2,
				point.getY() - mImageLocationLight.getHeight() / 2);

		Paint mBmpPaint = new Paint();
		mBmpPaint.setAntiAlias(true);
		if (mConfig.getFloor().equals(mCurrentLocation.getFloor())) {
			mBmpPaint.setAlpha(255);
		} else {
			mBmpPaint.setAlpha(100);
		}
		if (System.currentTimeMillis() - refreshTime >= 1000) {
			refreshTime = System.currentTimeMillis();
			mIsLight = !mIsLight;
		}
		if (!mIsLight) {
			canvas.drawBitmap(mImageLocationLight, matrix, mBmpPaint);

		} else {
			canvas.drawBitmap(mImageLocationNormal, matrix, mBmpPaint);
		}
		canvas.restore();
	}

	/**
	 * 得到定位图标的角度
	 * 
	 * @return 定位图标角度，单位：度
	 */
	public float getAngle() {
		return mLocationAngle;// 自由模式下，指针角度等于地图原本偏角+目前的旋转角度+罗盘角度
	}

	/**
	 * 设置定位图标角度,当对定位点的方向有特殊要求时，调用此方法请勿使用startSensor()开启方向传感器，或者使用removeSensor()
	 * 移除方向传感器
	 * 
	 * @param locationAngle
	 *            定位图标角度，单位：度
	 */
	public void setAngle(float locationAngle) {
		mLocationAngle = locationAngle;
		if (!Isfling()) {
			refreshMap();
		}
	}

	/**
	 * 得到定位半径，单位：米
	 * 
	 * @return 定位半径，单位：米
	 */
	public int getRadius() {
		return radius;
	}

	/**
	 * 设置定位精度，以此作为定位点半径，单位：米
	 * 
	 * @param radius
	 *            定位精度，单位：米
	 */
	public void setRadius(int radius) {
		this.radius = radius;
	}

	/**
	 * 设置定位圆形颜色，标记定位精度范围
	 * 
	 * @param colorLocationCircle
	 *            颜色值
	 */
	public void setColorLocationCircle(int colorLocationCircle) {
		COLOR_LOCATION_CIRCLE = colorLocationCircle;
	}

	/**
	 * 设置定位圆形外边框颜色
	 * 
	 * @param colorLocationOutline
	 *            颜色值
	 */
	public void setColorLocationOutline(int colorLocationOutline) {
		COLOR_LOCATION_OUTLINE = colorLocationOutline;
	}

	/**
	 * 设置定位圆形外边框宽度
	 * 
	 * @param LocationOutlineWidth
	 */
	public void setLocationOutlineWidth(float LocationOutlineWidth) {
		LOCATION_OUTLINE_WIDTH = LocationOutlineWidth;
	}

	/**
	 * 是否允许下载地图，一般用于本地有地图文件，需要禁止地图下载 v2.1
	 * 
	 * @return 布尔值
	 */
	public boolean isUpdateMap() {
		return isUpdateMap;
	}

	/**
	 * 是否允许下载地图，一般用于本地有地图文件，需要禁止地图下载 v2.1
	 * 
	 * @param isDownLoadMap
	 *            布尔值
	 */
	public void setUpdateMap(boolean isUpdateMap) {
		this.isUpdateMap = isUpdateMap;
	}

	/**
	 * 设置定位模式，定位模式现提供三种：自由NORMAL、罗盘COMPASS、跟随FOLLOW；模式参照RMLocationMode v2.4
	 * 
	 * @param locationMode
	 *            定位模式，请使用RMLocationMode参数
	 */
	public void setLocationMode(int locationMode) {
		this.locationMode = locationMode;
	}

	/**
	 * 得到当前定位模式 v2.4
	 * 
	 * @return 定位模式，参考RMLocationMode中三种模式
	 */
	public int getLocationMode() {
		return locationMode;
	}

	/**
	 * 是否在加载地图的时候重置地图中心，例：地图平移之后，中心发生偏移，在切换楼层时，大部分用户关心中心点是否重置，所以从V3.1之后，默认为true，
	 * 将地图中心点移动回屏幕中心
	 * 
	 * @param isResetMapCenter
	 *            true则地图加载的时候重置中心，false则不重置
	 */
	public void setResetMapCenter(boolean isResetMapCenter) {
		this.isResetMapCenter = isResetMapCenter;
	}

	/**
	 * 是否在加载地图的时候重置比例尺，例：地图放大缩小后，大部分用户期望切换地图，比例尺不发生变化，所以V3.1之后，默认false 沿用上一次比例尺；
	 * 
	 * @param isResetMapScale
	 *            true则重置地图比例尺，false则不重置
	 */
	public void setResetMapScale(boolean isResetMapScale) {
		this.isResetMapScale = isResetMapScale;
	}

	public boolean isResetMapCenter() {
		return isResetMapCenter;
	}

	public boolean isResetMapScale() {
		return isResetMapScale;
	}

	/**
	 * 得到优先绘制的poi层级
	 * 
	 * @return 用户设置的优先的级别列表
	 */
	public ArrayList<Integer> getPriorityShowLevels() {
		return priorityShowLevels;
	}

	/**
	 * 设置优先绘制的poi层级列表
	 * 
	 * @param priorityShowLevels
	 *            优先绘制的poi层级列表
	 */
	public void setPriorityShowLevels(ArrayList<Integer> priorityShowLevels) {
		if (priorityShowLevels != null && priorityShowLevels.size() > 0)
			this.priorityShowLevels = priorityShowLevels;
		mConfig.getDrawMap().sortLevelArray();
	}
	
	public POI getAroundPOI(String keywords,float x,float y) {
		return mConfig.getDrawMap().getAroundPOI(keywords,x, y);
	}
}
