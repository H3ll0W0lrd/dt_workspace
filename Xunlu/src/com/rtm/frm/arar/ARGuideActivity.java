package com.rtm.frm.arar;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager.OnBackStackChangedListener;
import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rtm.frm.R;
import com.rtm.frm.AR.ARTestManager;
import com.rtm.frm.model.Location;
import com.rtm.frm.model.MyLocation;
import com.rtm.frm.model.POITargetInfo;
import com.rtm.frm.newframe.NewFrameActivity;
import com.rtm.frm.tab0.TestRtmapFragment;
import com.rtm.frm.utils.PreferencesUtil;
import com.rtm.frm.utils.XunluUtil;

/**
 * 
 * 
 * @author wwj
 * @date 2013/4/29
 */
@SuppressLint("NewApi")
public class ARGuideActivity extends Activity implements OnClickListener,
		OnBackStackChangedListener {
	private Camera camera;
	private Camera.Parameters parameters = null;
	Bundle bundle = null; // 声明一个Bundle对象，用来存储数据
	private RelativeLayout FrameLayout1;

	// 状态栏的高度
	private int statusBarHeight;
	private Float defaultDisplayMaxDisanceInGuide = 100f;

	// ===============指南针
	private SensorManager mSensorManager;
	private Sensor mOrientationSensor;
	private float mTargetDirection;
	protected final Handler mHandler = new Handler();
	private boolean mStopDrawing;
	// ==================

	// 当前位置
	private List<ARShowView> arShowViews = new ArrayList<ARShowView>();
	private List<View> arShowLayout = new ArrayList<View>();

	private Button arguide_close;
	private ImageView arguide_arrow, arguide_endimage;
	private TextView arguide_targetname, arguide_loadinfo,
			arguide_distanceshow, textView1;

	private Float fromDegree = (float) 0.0;
	private Float mapDegree = (float) 0;

	private View arShowlayout;
	private ARShowView arShowView;

	private Animation translateAnimation;

	private float Lastdirect=Float.MIN_VALUE;
	// 这个是更新指南针旋转的线程，handler的灵活使用，每20毫秒检测方向变化值，对应更新指南针旋转
	protected Runnable mCompassViewUpdater = new Runnable() {
		@Override
		public void run() {
			if (!mStopDrawing) {
				updateDirection();
				mHandler.postDelayed(mCompassViewUpdater, 300);
			}
		}
	};

	public Location getCurrentMapLocation(){
		Location location = new Location(MyLocation
				.getInstance().getX(), MyLocation.getInstance().getY());
		location.setBuildId(MyLocation.getInstance().getBuildId());
		location.setFloor(MyLocation.getInstance().getFloor());
		
		return location;
	}
	
	@SuppressLint("InflateParams")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_arguide);
		// AR工具类初始化
		ARUtils.getInstance().initARUtils(ARGuideActivity.this);

		initResources();// 初始化view
		initServices();// 初始化传感器和位置服务

		statusBarHeight = XunluUtil.getStatusBarHeight(ARGuideActivity.this);

		SurfaceView surfaceView = (SurfaceView) this
				.findViewById(R.id.surfaceView);
		ARUtils.getInstance().initSurfaceView(surfaceView);
		surfaceView.getHolder().addCallback(new SurfaceCallback());// 为SurfaceView的句柄添加一个回调函数

		FrameLayout1 = (RelativeLayout) findViewById(R.id.FrameLayout1);
		// 每次刷新都要获取一下当前的位置信息，然后重新计算角度和距离
		Location location = getCurrentMapLocation();

		for (POITargetInfo targetInfos : ARTestManager.targetInfos) {
			Float degreeBetween = ARUtils.getInstance().getDegreeBetween(
					location, targetInfos);

			Float targetDegree = (float) (90 + mapDegree - degreeBetween);
			Float targerDistance = ARUtils.getInstance()
					.getTargentDistanceInARGuide(location, targetInfos);

			defaultDisplayMaxDisanceInGuide = (float) (targerDistance * 1.25);

			arShowlayout = LayoutInflater.from(ARGuideActivity.this).inflate(
					R.layout.arshow_viewlayout, null);

			arShowView = new ARShowView();
			arShowView.setTargetDegree(targetDegree);
			arShowView.setTargerDistance(targerDistance);
			arShowView.setTargetName(targetInfos.getPoiTargetName());
			arShowView.setPoiTargetX(targetInfos.getPoiTargetX());
			arShowView.setPoiTargetY(targetInfos.getPoiTargetY());
			arShowView
					.setPoiTargetDefaultDistance((float) (targerDistance * 1.25));
			arShowView.setPoiTargetFloor(targetInfos.getPoiTargetFloor());
			arShowView.setPoiTargetRouteInfo(targetInfos
					.getPoiTargetRouteInfo());
			arShowView.setTargetImageResource(targetInfos
					.getPoiTargetImageRes());
			arguide_endimage.setImageResource(targetInfos
					.getPoiTargetImageRes());

			ImageView arshowlayout_headimage = (ImageView) arShowlayout
					.findViewById(R.id.arshowlayout_headimage);
			arshowlayout_headimage.setBackgroundResource(targetInfos
					.getPoiTargetImageRes());
			arShowViews.add(arShowView);
			arShowLayout.add(arShowlayout);
			FrameLayout1.addView(arShowlayout);
		}

		TestRtmapFragment rtmFragment = NewFrameActivity.getInstance().getTab0();
		mapDegree = -(float) Math
				.toDegrees(rtmFragment.mMapView.getConfig()
						.getDrawMap().getAngle());
	}

	// 初始化view
	private void initResources() {
		mTargetDirection = 0.0f;
		mStopDrawing = true;

		// 在屏幕上显示角度的
		textView1 = (TextView) findViewById(R.id.textView1);
		// textView1.setVisibility(View.GONE);

		arguide_close = (Button) findViewById(R.id.arguide_close);
		arguide_close.setOnClickListener(this);
		arguide_arrow = (ImageView) findViewById(R.id.arguide_arrow);
		arguide_endimage = (ImageView) findViewById(R.id.arguide_endimage);

		arguide_targetname = (TextView) findViewById(R.id.arguide_targetname);
		arguide_loadinfo = (TextView) findViewById(R.id.arguide_loadinfo);
		arguide_distanceshow = (TextView) findViewById(R.id.arguide_distanceshow);
	}

	// 初始化传感器和位置服务
	@SuppressWarnings("deprecation")
	private void initServices() {
		// sensor manager
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mOrientationSensor = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		// location manager
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
		criteria.setPowerRequirement(Criteria.POWER_LOW);
	}

	// 更新顶部方向显示的方法
	private void updateDirection() {
		Float direction = ARUtils.getInstance().normalizeDegree(
				mTargetDirection * -1.0f);
		// 显示角度值在屏幕上
		textView1.setText("" + direction);
		if(Lastdirect==Float.MIN_VALUE){
			Lastdirect=direction;
		}else{
			if (Math.abs(Lastdirect-direction)<2) {
				return;
			}
		}
		Lastdirect=direction;
		
		drawView(direction);
	}

	/**
	 * 
	 * 方法描述 : 创建者：brillantzhao 版本： v1.0 创建时间： 2014-3-19 上午10:10:46
	 * 
	 * @param direction
	 *            void
	 */
	@SuppressLint("NewApi")
	private void drawView(Float direction) {
		// 每次刷新都要获取一下当前的位置信息，然后重新计算角度和距离
		Location location = getCurrentMapLocation();
		if (location == null || location.getBuildId() == null
				|| location.getFloor() == null) {
			return;
		}
		// 这里的循环方法租了修改，以改进性能，可能会出现错误
		for (ARShowView arShowView : arShowViews) {
			Float drawDirection = direction;
			View arshowLayout = arShowLayout.get(arShowViews
					.indexOf(arShowView));
			if (arShowView.getPoiTargetFloor().equals(location.getFloor())) {
				arshowLayout.setVisibility(View.VISIBLE);

				// 自己和poi点之间的夹角
				Float degreeBetween = ARUtils.getInstance().getDegreeBetween(
						location, arShowView);
				Float targetDegree = ARUtils.getInstance()
						.getTargetDegreeInARGuide(location, arShowView,
								degreeBetween);
				Float targerDistance = ARUtils.getInstance()
						.getTargentDistanceInARGuide(location, arShowView);
				targetDegree = (targetDegree + 360) % 360;

				Float modifyDegree = drawDirection - targetDegree;
				if (modifyDegree >= 360 - ARUtils.getInstance().eyeDegree / 2) {
					modifyDegree = modifyDegree - 360;
				} else if (modifyDegree <= (ARUtils.getInstance().eyeDegree + ARUtils
						.getInstance().eyeDegreeOutScreen) / 2 - 360) {
					modifyDegree = 360 + modifyDegree;
				}

				arShowView.setTargerDistance(targerDistance);
				Float currentX = (-modifyDegree + ARUtils.getInstance().eyeDegree / 2)
						* (PreferencesUtil.getInt("screenWidth", 720) / ARUtils
								.getInstance().eyeDegree);
				Float currentY = (PreferencesUtil.getInt("screenHeight", 1280)
						- statusBarHeight - targerDistance
						* ((PreferencesUtil.getInt("screenHeight", 1280) - statusBarHeight) / defaultDisplayMaxDisanceInGuide));
				// =====================
				// 初始化
				translateAnimation = new TranslateAnimation(
						arShowView.getCurrentx(), currentX,
						arShowView.getCurrenty(), currentY);
				// 设置动画时间
				translateAnimation.setDuration(100);
				translateAnimation.setFillAfter(true);
				arshowLayout.startAnimation(translateAnimation);
				// ==========================
				arShowView.setCurrentx(currentX);
				arShowView.setCurrenty(currentY);

				TextView arshowlayout_name = (TextView) arshowLayout
						.findViewById(R.id.arshowlayout_name);
				TextView arshowlayout_distance = (TextView) arshowLayout
						.findViewById(R.id.arshowlayout_distance);

				arshowlayout_name.setText(arShowView.getTargetName());

				if (arShowView.getTargerDistance() < ARUtils.getInstance().defaultIsNearDisance) {
					arshowlayout_distance
							.setText(getString(R.string.arshow_isnear));
				} else {
					arshowlayout_distance.setText((int) arShowView
							.getTargerDistance() + "米");
				}
				updateRouteLayerTip(arShowViews.get(arShowViews.size() - 1),
						arShowView, modifyDegree);
			} else {
				// 不在本楼层的不展示
				arshowLayout.setVisibility(View.GONE);
			}
		}
	}

	/**
	 * 
	 * 方法描述 : 创建者：brillantzhao 版本： v1.0 创建时间： 2014-3-14 上午10:29:31
	 * 
	 * @param drawview
	 * @param targetDegree
	 *            void
	 */
	private void updateRouteLayerTip(ARShowView endPOI, ARShowView arShowView,
			Float modifyDegree) {
		arguide_targetname.setText(endPOI.getPoiTargetFloor() + " "
				+ endPOI.getTargetName());

		arguide_loadinfo.setText(arShowView.getPoiTargetRouteInfo());

		if (arShowView.getTargerDistance() < ARUtils.getInstance().defaultIsNearDisance) {
			arguide_distanceshow.setText(getString(R.string.arshow_isnear));
		} else {
			arguide_distanceshow.setText("距离"
					+ (int) arShowView.getTargerDistance() + "米");
		}
		final RotateAnimation animation = new RotateAnimation(-fromDegree,
				-modifyDegree, Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		animation.setDuration(300);// 设置动画持续时间
		animation.setFillAfter(true);// 动画执行完后是否停留在执行完的状态
		arguide_arrow.setAnimation(animation);
		/** 开始动画 */
		animation.startNow();
		fromDegree = modifyDegree;
	}

	private SensorEventListener mOrientationSensorEventListener = new SensorEventListener() {
		@Override
		public void onSensorChanged(SensorEvent event) {
			float direction = event.values[0] * -1.0f;
			mTargetDirection = ARUtils.getInstance().normalizeDegree(direction);
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	};

	/**
	 * 
	 * com.mycamera.SurfaceCallback
	 * 
	 * @author BrillantZhao <br/>
	 *         create at 2014-3-5 上午11:18:57
	 */
	private final class SurfaceCallback implements Callback {
		// 拍照状态变化时调用该方法
		@SuppressWarnings("deprecation")
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			parameters = camera.getParameters(); // 获取各项参数
			parameters.setPictureFormat(PixelFormat.JPEG); // 设置图片格式
			parameters.setPreviewSize(width, height); // 设置预览大小
			parameters.setPreviewFrameRate(5); // 设置每秒显示4帧
			parameters.setPictureSize(width, height); // 设置保存的图片尺寸
			parameters.setJpegQuality(80); // 设置照片质量
		}

		// 开始拍照时调用该方法
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			try {
				camera = Camera.open(); // 打开摄像头
				camera.setPreviewDisplay(holder); // 设置用于显示拍照影像的SurfaceHolder对象
				camera.setDisplayOrientation(ARUtils.getInstance()
						.getPreviewDegree(ARGuideActivity.this));
				camera.startPreview(); // 开始预览
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// 停止拍照时调用该方法
		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			if (camera != null) {
				camera.stopPreview();
				camera.release(); // 释放照相机
				camera = null;
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mOrientationSensor != null) {
			mSensorManager.registerListener(mOrientationSensorEventListener,
					mOrientationSensor, SensorManager.SENSOR_DELAY_GAME);
		}
		mStopDrawing = false;
		mHandler.postDelayed(mCompassViewUpdater, 300);
		ARShowActivity.isInARMode = true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		mStopDrawing = true;
		if (mOrientationSensor != null) {
			mSensorManager.unregisterListener(mOrientationSensorEventListener);
		}
		if (camera != null) {
			camera.stopPreview();
			camera.release(); // 释放照相机
			camera = null;
		}
		ARShowActivity.isInARMode = false;
		finish();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.arguide_close: {
			finish();
			break;
		}
		default:
			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onBackStackChanged() {
		// Fragment 栈后退监听

	}
}
