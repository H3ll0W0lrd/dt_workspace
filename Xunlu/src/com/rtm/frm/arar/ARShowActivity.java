package com.rtm.frm.arar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.rtm.common.model.POI;
import com.rtm.frm.R;
import com.rtm.frm.AR.ARTestManager;
import com.rtm.frm.fragment.controller.MyFragmentManager;
import com.rtm.frm.intf.LocationImpl;
import com.rtm.frm.intf.LocationObserverInf;
import com.rtm.frm.map.XunluMap;
import com.rtm.frm.model.Location;
import com.rtm.frm.model.MyLocation;
import com.rtm.frm.model.NavigatePoint;
import com.rtm.frm.model.POITargetInfo;
import com.rtm.frm.model.RMRoute;
import com.rtm.frm.net.NetworkCore;
import com.rtm.frm.net.PostData;
import com.rtm.frm.newframe.NewFrameActivity;
import com.rtm.frm.newui.NavPagerFragment;
import com.rtm.frm.tab0.TestRtmapFragment;
import com.rtm.frm.utils.ConstantsUtil;
import com.rtm.frm.utils.PreferencesUtil;
import com.rtm.frm.utils.RMNavigationUtil;
import com.rtm.frm.utils.RMNavigationUtil.OnNavigationListener;
import com.rtm.frm.utils.StaticData;
import com.rtm.frm.utils.ToastUtil;
import com.rtm.frm.utils.XunluUtil;
import com.rtm.frm.view.MySeekBar;
import com.rtm.frm.view.MySeekBar.OnSeekBarChangeListener;

/**
 * 
 * 暂时默认只显示进入时的楼层的数据，到其他楼层后的数据以后解决。
 * 
 */
@SuppressLint({ "HandlerLeak", "InflateParams" })
public class ARShowActivity extends Activity implements OnClickListener,
		LocationObserverInf {
	public static final int SHOW_ROUTE = 1;
	public static final int NOT_SHOW_ROUTE = 2;
	public static final int SHOW_END_ICON = 3;

	POI start;// start
	POI mypoi;// end
	// 判断是否进入了AR导航模式
	public static boolean isInARMode = false;
	// view的Tag，方便查找view
	ArrayList<String> viewTag = new ArrayList<String>();
	private Camera camera;
	private Camera.Parameters parameters = null;
	Bundle bundle = null; // 声明一个Bundle对象，用来存储数据
	// private RelativeLayout FrameLayout1;
	private RelativeLayout rel;
	private Float showDistance;
	// 状态栏的高度
	private int statusBarHeight;
	public static String mLastFloor;
	private ProgressDialog mDialogLoad;
	// 方向传感器和磁传感器做的指南针
	private SensorManager mSensorManager;// 传感器管理对象
	private Sensor mOrientationSensor;// 传感器对象
	private float mTargetDirection;// 目标浮点方向
	private RMRoute arNavigateModel;
	Handler mHandler = new Handler();
	private boolean mStopDrawing;// 是否停止指南针旋转的标志位
	private TextView textView1;
	private ListView listView;
	private MySeekBar distance_seekbar;
	private Button arguide_close;
	private TextView arshow_buildinfo, arshow_distance_show;
	SimpleAdapter listItemAdapter;
	private boolean isShowArguide = false;
	// 获取导航路线失败
	public static final int FETCHROUTE_FAIL = 10001;
	// 没有网络
	public static final int NET_ERROR = 10002;

	private List<ARShowView> arShowViews = new ArrayList<ARShowView>();
	private List<View> arShowLayout = new ArrayList<View>();
	private Dialog mARPoiInfoShow;
	private ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_arshow);
		// AR工具类初始化
		ARUtils.getInstance().initARUtils(ARShowActivity.this);
		// 位置坐标回调
		LocationImpl.getInstance().addObserver(this);

		mDialogLoad = new ProgressDialog(ARShowActivity.this);

		initData();
		initServices();// 初始化传感器和位置服务
		initSurfaceView();// 初始化surfaceview

		initResources();// 初始化view
		showPOIInAngle();// 显示信息点

	}

	private void initData() {
		showDistance = ARUtils.getInstance().defaultDisplayMaxDisance / 2;
		statusBarHeight = XunluUtil.getStatusBarHeight(ARShowActivity.this);
	}

	private void showPOIInAngle() {
		Location location = new Location(MyLocation.getInstance().getX(),
				MyLocation.getInstance().getY());
		location.setBuildId(MyLocation.getInstance().getBuildId());
		location.setFloor(MyLocation.getInstance().getFloor());

		// 每次刷新都要获取一下当前的位置信息，然后重新计算角度和距离
		// com.rtm.frm.map.data.Location location = MapActivity.getInstance()
		// .getCurrentMapLocation();
		for (POITargetInfo targetInfos : ARTestManager.targetInfos) {
			float degreeBetween = ARUtils.getInstance()
					.getDegreeBetweenWithThround(location, targetInfos);

			float targetDegree = (float) (90 + ARUtils.getInstance().mapDegree - degreeBetween);
			float targerDistance = ARUtils.getInstance()
					.getTargetDistanceInARShow(location, targetInfos);

			// 一个楼层有很多点，只显示默认距离范围内的
			if (targerDistance <= ARUtils.getInstance().defaultDisplayMaxDisance
					&& targerDistance >= ARUtils.getInstance().defaultDisplayMinDisance) {
				View arShowlayout = LayoutInflater.from(ARShowActivity.this)
						.inflate(R.layout.arshow_viewlayout, null);
				String poiName = targetInfos.getPoiTargetName();

				ARShowView arShowView = new ARShowView();
				arShowView.setTargetDegree(targetDegree);
				arShowView.setTargerDistance(targerDistance);
				arShowView.setTargetName(poiName);
				arShowView.setPoiTargetX(targetInfos.getPoiTargetX());
				arShowView.setPoiTargetY(targetInfos.getPoiTargetY());
				arShowView.setTargetImageResource(targetInfos
						.getPoiTargetImageRes());

				arShowViews.add(arShowView);// 给view设置参数等
				arShowLayout.add(arShowlayout);// 将overlay放到list中
				rel.addView(arShowlayout);
			}
		}
	}

	public void clearAROverlay() {
		// 删除从图层获得的所有的点的list
		ARTestManager.targetInfos.clear();
		// 删除放poi点信息的list
		arShowViews.clear();
		// 删除放view的list
		arShowLayout.clear();
		rel.removeAllViewsInLayout();
	}

	private void initSurfaceView() {
		SurfaceView surfaceView = (SurfaceView) this
				.findViewById(R.id.surfaceView);
		ARUtils.getInstance().initSurfaceView(surfaceView);
		surfaceView.getHolder().addCallback(new SurfaceCallback());// 为SurfaceView的句柄添加一个回调函数
	}

	// 初始化view
	private void initResources() {
		mTargetDirection = 0.0f;// 初始化目标方向
		mStopDrawing = true;

		// 在屏幕上显示角度的
		textView1 = (TextView) findViewById(R.id.textView1);
		arguide_close = (Button) findViewById(R.id.arguide_close);
		arshow_buildinfo = (TextView) findViewById(R.id.arshow_buildinfo);
		arguide_close.setOnClickListener(this);
		listView = (ListView) findViewById(R.id.listView1);
		listItemAdapter = new SimpleAdapter(this, listItem,// 数据源
				R.layout.arshow_listlayout,// ListItem的XML实现
				// 动态数组与ImageItem对应的子项
				new String[] { "arshowlayout_headimage", "arshowlayout_name",
						"arshowlayout_distance" },
				// ImageItem的XML文件里面的一个ImageView,两个TextView ID
				new int[] { R.id.arshowlayout_headimage,
						R.id.arshowlayout_name, R.id.arshowlayout_distance });
		listView.setAdapter(listItemAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				shoPoiInfoInAR((listItem.get(arg2)));

			}
		});
		rel = (RelativeLayout) findViewById(R.id.rel);
		arshow_buildinfo.setText(MyLocation.getInstance().getBuildName() + " "
				+ MyLocation.getInstance().getFloor());
		distance_seekbar = (MySeekBar) findViewById(R.id.distance_seekbar);
		distance_seekbar.setProgress(50);
		distance_seekbar
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
					@Override
					public void onProgressChanged(MySeekBar VerticalSeekBar,
							int progress, boolean fromUser) {
						showDistance = VerticalSeekBar.getProgress()
								* ARUtils.getInstance().defaultDisplayMaxDisance
								/ 100;
						if (showDistance == 0) {
							showDistance = 1.0f;
						}
						arshow_distance_show.setText(showDistance.intValue()
								+ "米");
					}

					@Override
					public void onStartTrackingTouch(MySeekBar VerticalSeekBar) {
					}

					@Override
					public void onStopTrackingTouch(MySeekBar VerticalSeekBar) {
						showDistance = VerticalSeekBar.getProgress()
								* ARUtils.getInstance().defaultDisplayMaxDisance
								/ 100;
						if (showDistance == 0) {
							showDistance = 1.0f;
						}
						arshow_distance_show.setText(showDistance.intValue()
								+ "米");
					}
				});

		arshow_distance_show = (TextView) findViewById(R.id.arshow_distance_show);
		arshow_distance_show.setText(showDistance.intValue() + "米");
	}

	// 初始化传感器和位置服务
	@SuppressWarnings("deprecation")
	private void initServices() {
		// sensor manager
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mOrientationSensor = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ORIENTATION);
	}

	// 更新顶部方向显示的方法
	private void updateDirection() {
		Float direction = ARUtils.getInstance().normalizeDegree(
				mTargetDirection * -1.0f);
		// 显示角度值在屏幕上
		textView1.setText("" + direction);
		drawView(direction);
	}

	/**
	 * 
	 * 方法描述 : 创建者：brillantzhao 版本： v1.0 创建时间： 2014-4-4 上午9:40:45
	 * 
	 * @param direction
	 *            void
	 */
	@SuppressLint("NewApi")
	private void drawView(Float direction) {
		if (MyLocation.getMyLocation() == null)
			return;
		Location location = new Location(MyLocation.getInstance().getX(),
				MyLocation.getInstance().getY());
		location.setBuildId(MyLocation.getInstance().getBuildId());
		location.setFloor(MyLocation.getInstance().getFloor());

		// com.rtm.frm.map.data.Location location = MapActivity.getInstance()
		// .getCurrentMapLocation();
		// if (location == null || location.getBuildId() == null
		// || location.getFloor() == null) {
		// return;
		// }
		listItem.clear();

		for (final ARShowView arShowView : arShowViews) {
			Float drawDirection = direction;
			View arshowLayout = arShowLayout.get(arShowViews
					.indexOf(arShowView));
			arshowLayout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					shoPoiInfoInAR(arShowView);
				}
			});
			Float degreeBetween = ARUtils.getInstance()
					.getDegreeBetweenWithThround(location, arShowView);
			Float targetDegree = ARUtils.getInstance().getTargetDegreeInARShow(
					location, arShowView, degreeBetween);

			// 因为旋转了固定的角度，需要在这里减去
			Float targerDistance = ARUtils.getInstance()
					.getTargetDistanceInARShow(location, arShowView);

			targetDegree = (targetDegree + 360) % 360;

			Float modifyDegree = drawDirection - targetDegree;
			if (modifyDegree >= 360 - ARUtils.getInstance().eyeDegree / 2) {
				modifyDegree = modifyDegree - 360;
			} else if (modifyDegree <= (ARUtils.getInstance().eyeDegree + ARUtils
					.getInstance().eyeDegreeOutScreen) / 2 - 360) {
				modifyDegree = 360 + modifyDegree;
			}
			// 有选择的重画
			if (targerDistance <= (showDistance + 10)
					&& Math.abs(modifyDegree) <= (ARUtils.getInstance().eyeDegree + ARUtils
							.getInstance().eyeDegreeOutScreen) / 2) {
				arshowLayout.setVisibility(View.VISIBLE);

				Float currentX = (-modifyDegree + ARUtils.getInstance().eyeDegree / 2)
						* (PreferencesUtil.getInt("screenWidth", 720) / ARUtils
								.getInstance().eyeDegree);
				Float currentY = (PreferencesUtil.getInt("screenHeight", 1280)
						- statusBarHeight - targerDistance
						* ((PreferencesUtil.getInt("screenHeight", 1280) - statusBarHeight) / showDistance));
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
				try {
					// 只有在API LEVEL 11及之后才添加，所有使用之前的低版本可能会有问题
					arshowLayout.setX(arShowView.getCurrentx());
					arshowLayout.setY(arShowView.getCurrenty());
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				arshowLayout.setVisibility(View.GONE);
			}
		}
	}

	// 方向传感器变化监听
	private SensorEventListener mOrientationSensorEventListener = new SensorEventListener() {
		@Override
		public void onSensorChanged(SensorEvent event) {
			float direction = event.values[0] * -1.0f;
			mTargetDirection = ARUtils.getInstance().normalizeDegree(direction);// 赋值给全局变量，让指南针旋转
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	};

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
						.getPreviewDegree(ARShowActivity.this));
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
		mHandler.postDelayed(mCompassViewUpdater, 20);// 20毫秒执行一次更新指南针图片旋转
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
		isInARMode = false;
		finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (isShowArguide && arNavigateModel != null) {
			TestRtmapFragment t = (TestRtmapFragment) NewFrameActivity
					.getInstance().getTab0();
			NavPagerFragment navPagerFragment = new NavPagerFragment(t.mMapView,
					t.mRouteLayer, t.mMapShowBuildId, t.mMapShowBuildName,
					arNavigateModel, false);
			MyFragmentManager.getInstance().replaceFragment(
					NewFrameActivity.ID_ALL, navPagerFragment,
					MyFragmentManager.PROCESS_NAV_FLOOR_CHANGE,
					MyFragmentManager.FRAGMENT_NAV_FLOOR_CHANGE);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		LocationImpl.getInstance().removeObserver(this);
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

	/**
	 * 
	 * 方法描述 : 创建者：brillantzhao 版本： v1.0 创建时间： 2014-3-29 上午9:29:40 void
	 */
	private void shoPoiInfoInAR(final ARShowView arShowView) {
		if (mARPoiInfoShow == null) {
			mARPoiInfoShow = new Dialog(ARShowActivity.this,
					R.style.style_poi_dialog);
			mARPoiInfoShow.setContentView(R.layout.dialog_poi_detail_arshow);
			LayoutParams wmParams = mARPoiInfoShow.getWindow().getAttributes();
			wmParams.gravity = Gravity.CENTER | Gravity.CENTER_VERTICAL;
			wmParams.alpha = 1f;
			wmParams.width = LayoutParams.WRAP_CONTENT;
			wmParams.height = LayoutParams.WRAP_CONTENT;
			mARPoiInfoShow.getWindow().setAttributes(wmParams);
		}
		TextView arguide_targetname = (TextView) mARPoiInfoShow
				.findViewById(R.id.arguide_targetname);
		arguide_targetname.setText(arShowView.getTargetName());

		TextView arguide_loadinfo = (TextView) mARPoiInfoShow
				.findViewById(R.id.arguide_loadinfo);

		if (arShowView.getTargerDistance() < ARUtils.getInstance().defaultIsNearDisance) {
			arguide_loadinfo.setText(MyLocation.getInstance().getFloor() + "  "
					+ getString(R.string.arshow_isnear));
		} else {
			arguide_loadinfo.setText(MyLocation.getInstance().getFloor() + "  "
					+ (int) arShowView.getTargerDistance() + "米");
		}

		ImageView arguide_endimage = (ImageView) mARPoiInfoShow
				.findViewById(R.id.arguide_endimage);
		arguide_endimage.setImageResource(arShowView.getTargetImageResource());

		Button button_tuan = (Button) mARPoiInfoShow
				.findViewById(R.id.button_tuan);
		button_tuan.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mARPoiInfoShow.dismiss();
			}
		});
		Button button_navigate = (Button) mARPoiInfoShow
				.findViewById(R.id.button_navigate);
		button_navigate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mARPoiInfoShow.dismiss();
				POI POI = new POI(0, arShowView.getTargetName(), MyLocation
						.getInstance().getBuildId(), MyLocation.getInstance()
						.getFloor(), arShowView.getPoiTargetX() / 1000,
						arShowView.getPoiTargetY() / 1000);
				fetchRoute(POI);
			}
		});
		mARPoiInfoShow.show();
	}

	private void shoPoiInfoInAR(final HashMap<String, Object> hashMap) {
		if (mARPoiInfoShow == null) {
			mARPoiInfoShow = new Dialog(ARShowActivity.this,
					R.style.style_poi_dialog);
			mARPoiInfoShow.setContentView(R.layout.dialog_poi_detail_arshow);
			LayoutParams wmParams = mARPoiInfoShow.getWindow().getAttributes();
			wmParams.gravity = Gravity.CENTER | Gravity.CENTER_VERTICAL;
			wmParams.alpha = 1f;
			wmParams.width = LayoutParams.WRAP_CONTENT;
			wmParams.height = LayoutParams.WRAP_CONTENT;
			mARPoiInfoShow.getWindow().setAttributes(wmParams);
		}
		TextView arguide_targetname = (TextView) mARPoiInfoShow
				.findViewById(R.id.arguide_targetname);
		arguide_targetname.setText(hashMap.get("arshowlayout_name").toString());

		TextView arguide_loadinfo = (TextView) mARPoiInfoShow
				.findViewById(R.id.arguide_loadinfo);

		arguide_loadinfo.setText(MyLocation.getInstance().getFloor() + "  "
				+ hashMap.get("arshowlayout_distance"));

		ImageView arguide_endimage = (ImageView) mARPoiInfoShow
				.findViewById(R.id.arguide_endimage);
		arguide_endimage.setImageResource((Integer) hashMap
				.get("arshowlayout_headimage"));

		Button button_tuan = (Button) mARPoiInfoShow
				.findViewById(R.id.button_tuan);
		button_tuan.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mARPoiInfoShow.dismiss();
			}
		});
		Button button_navigate = (Button) mARPoiInfoShow
				.findViewById(R.id.button_navigate);
		button_navigate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mARPoiInfoShow.dismiss();
				POI POI = new POI(0, hashMap.get("arshowlayout_name")
						.toString(), MyLocation.getInstance().getBuildId(),
						MyLocation.getInstance().getFloor(), (Float) hashMap
								.get("x") / 1000,
						(Float) hashMap.get("y") / 1000);
				fetchRoute(POI);
			}
		});
		mARPoiInfoShow.show();

	}

	/**
	 * 
	 * 方法描述 : 创建者：BrillantZhao_rtmap 版本： v1.0 创建时间： 2014-5-22 下午3:07:34
	 * 
	 * @param poi
	 *            void
	 */
	public void fetchRoute(final POI poi) {
		// 记录起点位置，在导航页使用
		if (MyLocation.getMyLocation() == null) {
			// 获取不到当前位置，获取不在当前建筑物内弹出提示
			ToastUtil.shortToast(R.string.message_cannot_locate);
			return;
		}
		start = new POI(0, "起点", MyLocation.getInstance().getBuildId(),
				MyLocation.getInstance().getFloor(), MyLocation.getInstance()
						.getX(), MyLocation.getInstance().getY());

		mDialogLoad.setMessage(("正在加载"));
		if (mDialogLoad.isShowing()) {
			return;
		}
		mDialogLoad.show();

		mypoi = poi;
		RMNavigationUtil.requestNavigation(XunluMap.getInstance().getApiKey(),
				MyLocation.getInstance().getBuildId(), start, poi, null,
				new OnNavigationListener() {

					@Override
					public void onFinished(RMRoute route) {
						if (mDialogLoad != null && mDialogLoad.isShowing()) {
							mDialogLoad.dismiss();
						}
						if (route.getError_code() == 0) {
							TestRtmapFragment testrtFragment = NewFrameActivity
									.getInstance().getTab0();

							try {
								arNavigateModel = route;
								TestRtmapFragment rtmFragment = NewFrameActivity
										.getInstance().getTab0();
								rtmFragment.mRouteLayer
										.setNavigatePoints(arNavigateModel
												.getPointlist());
								StaticData.navigateBuild = MyLocation
										.getMyLocation().getBuildId();
								StaticData.navigateEndPOI = mypoi;
								//
								// // 用来在确定导航后重新回到地图时使用
								StaticData.navigate_poi = mypoi;

								// 设置起点终点的图标
								if (testrtFragment != null) {
									// 地图上放入起点图标
									if (testrtFragment.getMapView() != null) {
										// 修改成新的地图之后需要手动刷新地图才能显示出来
										testrtFragment.getMapView()
												.refreshMap();
									}
								}

								// 直接获取数据，进入AR导航模式
								int sizeOfKeyPoints = testrtFragment.mRouteLayer
										.getNavigatePoints().size();
								ARTestManager.targetInfos.clear();
								for (int i = 1; i < sizeOfKeyPoints; i++) {
									NavigatePoint keyPoint = (NavigatePoint) testrtFragment.mRouteLayer
											.getNavigatePoints().get(i);
									NavigatePoint keyPointPre = (NavigatePoint) testrtFragment.mRouteLayer
											.getNavigatePoints().get(i - 1);

									POITargetInfo targetInfo = new POITargetInfo();
									targetInfo.setPoiTargetX(keyPoint.getX());
									targetInfo.setPoiTargetY(keyPoint.getY());
									targetInfo.setPoiTargetName(keyPoint
											.getAroundPoiName());
									targetInfo.setPoiTargetFloor(keyPointPre
											.getFloor());
									targetInfo
											.setPoiTargetRouteInfo(keyPointPre
													.getAroundPoiName());
									targetInfo.setPoiTargetImageRes(XunluUtil
											.getResourceID(ARShowActivity.this,
													keyPoint.getAroundPoiName(),
													null, 0));
									ARTestManager.targetInfos.add(targetInfo);
								}

								// 最后一个点就是目标点
								POITargetInfo targetInfo = new POITargetInfo();
								targetInfo
										.setPoiTargetX(StaticData.navigate_poi
												.getX());
								targetInfo
										.setPoiTargetY(StaticData.navigate_poi
												.getY());
								targetInfo
										.setPoiTargetName(StaticData.navigate_poi
												.getName());
								targetInfo
										.setPoiTargetFloor(StaticData.navigate_poi
												.getFloor());
								targetInfo.setPoiTargetRouteInfo("到达终点");
								targetInfo.setPoiTargetImageRes(XunluUtil
										.getResourceID(ARShowActivity.this,
												StaticData.navigate_poi
														.getName(), null, 0));
								ARTestManager.targetInfos.add(targetInfo);

								Intent intent = new Intent(ARShowActivity.this,
										ARGuideActivity.class);
								startActivity(intent);
								isShowArguide = true;
							} catch (Exception e) {
								e.printStackTrace();
								if (NetworkCore
										.isNetConnected(ARShowActivity.this)) {
								} else {
									ToastUtil
											.shortToast(R.string.message_net_error);
								}
							}
						} else {
							ToastUtil.shortToast(route.getError_msg());
						}
					}
				});
	}

	// 这个是更新指南针旋转的线程，handler的灵活使用，每20毫秒检测方向变化值，对应更新指南针旋转
	protected Runnable mCompassViewUpdater = new Runnable() {
		@Override
		public void run() {
			if (!mStopDrawing) {
				updateDirection();// 更新方向值
				mHandler.postDelayed(mCompassViewUpdater, 40);
			}
		}
	};

	@Override
	public void onUpdateLocation(MyLocation myLocation) {

		if (!mLastFloor.equals(myLocation.getFloor())) {
			mStopDrawing = true;
			// 停止指南针旋转回调
			clearAROverlay();
			initData();
			// 设置targetInfos里的数据
			TestRtmapFragment rtmFragment = NewFrameActivity.getInstance()
					.getTab0();
			rtmFragment.setARPOI();
			// 将poi添加并显示出来
			showPOIInAngle();
			// 开启指南针旋转回调
			mStopDrawing = false;
			// 递归，每隔20毫秒反复调用更新
			mHandler.postDelayed(mCompassViewUpdater, 20);
			// 设置最后一次所在的楼层
			mLastFloor = myLocation.getFloor();
		}
	}
}
