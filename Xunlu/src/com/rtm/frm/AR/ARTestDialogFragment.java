package com.rtm.frm.AR;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rtm.common.model.POI;
import com.rtm.frm.R;
import com.rtm.frm.dialogfragment.BaseDialogFragment;
import com.rtm.frm.map.XunluMap;
import com.rtm.frm.model.MyLocation;
import com.rtm.frm.model.RMRoute;
import com.rtm.frm.net.PostData;
import com.rtm.frm.newframe.NewFrameActivity;
import com.rtm.frm.tab0.TestRtmapFragment;
import com.rtm.frm.utils.ConstantsUtil;
import com.rtm.frm.utils.RMNavigationUtil;
import com.rtm.frm.utils.ToastUtil;
import com.rtm.frm.utils.XunluUtil;

@SuppressLint({ "HandlerLeak", "NewApi" })
public class ARTestDialogFragment extends BaseDialogFragment implements
		View.OnClickListener {
	private SensorManager mSensorManager;// 传感器管理对象
	private Sensor mOrientationSensor;// 传感器对象
	private Camera camera;
	private Camera.Parameters parameters = null;

	public boolean mStopDrawing = false;// 用来停止更新进程
	public boolean mStopUpdatePois = false;// 停止更新pois
	public boolean mStopUpdateSelect = true;// 用来停止更新进程

	TextView tvAngle;
	Button btClose;
	View contentView;
	SurfaceView surfaceView;
	RelativeLayout relItems;
	TextView distance;
	drawView drawView;

	public ARTestDialogFragment() {
		setStyle(DialogFragment.STYLE_NORMAL,
				R.style.dialogfragment_transparent_bg);
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		contentView = inflater.inflate(R.layout.fragment_ar, container, false);

		initView(contentView);// 初始化将要使用到的view

		return contentView;
	}

	@Override
	public void onViewCreated(View v, Bundle b) {
		super.onViewCreated(v, b);

		initData();// 初始化数据

		initSensor();// 初始化方向传感器

		initSurfaceView();// 初始化surfaceview

		addViewToRel();// 添加view到REL布局
	}

	private void initData() {
		ARTestManager.statusBarHeight = XunluUtil.getStatusBarHeight(mContext);
		ARTestManager.screenWidth = getResources().getDisplayMetrics().widthPixels;
		ARTestManager.screenHeight = getResources().getDisplayMetrics().heightPixels;

		TestRtmapFragment frag = NewFrameActivity.getInstance().getTab0();
		ARTestManager.mapDegree = -(float) Math.toDegrees(frag.getMapView()
				.getConfig().getDrawMap().getAngle());

		// 开启定时刷新
		mStopDrawing = false;
		mHandler.postDelayed(runCompass, 40);
	}

	private void initView(View v) {
		tvAngle = (TextView) v.findViewById(R.id.ar_tv_angle);
		btClose = (Button) v.findViewById(R.id.ar_bt_close);
		btClose.setOnClickListener(this);
		relItems = (RelativeLayout) v.findViewById(R.id.ar_rel_items);
		surfaceView = (SurfaceView) v.findViewById(R.id.view_surface);
	}

	// //将所有的点都添加到rel中
	private void addViewToRel() {
		// 距离大于default的不添加 TODO
		// for (arPoiItem aritem : ARTestManager.getInstance().arItemsList) {
		for (int i = 0; i < ARTestManager.getInstance().arItemsList.size(); i++) {
			float dis = ARTestManager.getDistance(
					ARTestManager.getInstance().arItemsList.get(i).getX(),
					ARTestManager.getInstance().arItemsList.get(i).getX());

			View arview = LayoutInflater.from(mContext).inflate(
					R.layout.ar_item, null);

			// 设置poi名称
			TextView name = (TextView) arview.findViewById(R.id.name);
			name.setText(ARTestManager.getInstance().arItemsList.get(i)
					.getName());

			// 设置poi距离
			distance = (TextView) arview.findViewById(R.id.distance);
			distance.setText((int) dis + "米");

			// 设置distance
			ARTestManager.getInstance().arItemsList.get(i).setDistance(dis);

			ARTestManager.getInstance().arItemsList.get(i).setView(arview);

			arview.setTag(i);

			arview.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// 给每个item添加点击事件
					itemOnclick(v);
				}

			});

			relItems.addView(arview);// 将view添加到rel
		}
	}

	private void itemOnclick(View v) {
		// 设置选中的点
		ARTestManager.selected = (Integer) v.getTag();

		// 发送一个网络请求，请求从当前位置到选中点直接的线路
		// 设置起点
		POI start = new POI(0, "起点", MyLocation.getInstance().getBuildId(),
				MyLocation.getInstance().getFloor(), MyLocation.getInstance()
						.getX(), MyLocation.getInstance().getY());
		// 设置终点
		float endx = ARTestManager.getInstance().arItemsList.get(
				ARTestManager.selected).getX();
		float endy = ARTestManager.getInstance().arItemsList.get(
				ARTestManager.selected).getY();
		String floor = ARTestManager.getInstance().arItemsList.get(
				ARTestManager.selected).getCurrFloor();
		POI end = new POI(0, "终点", MyLocation.getInstance().getBuildId(),
				floor, endx, endy);

		// 设置起点终点的图标
		TestRtmapFragment fragment = NewFrameActivity.getInstance().getTab0();
		if (fragment != null) {
			if (fragment.getMapView() != null) {
				// 修改成新的地图之后需要手动刷新地图才能显示出来
				fragment.getMapView().refreshMap();
			}
		}
		// 发送请求
		RMNavigationUtil.requestNavigation(XunluMap.getInstance().getApiKey(),
				MyLocation.getInstance().getBuildId(), start, end, null,
				new RMNavigationUtil.OnNavigationListener() {

					@Override
					public void onFinished(RMRoute route) {
						if (route.getError_code() == 0) {
							dealResult(route);
						} else {
							ToastUtil.shortToast(route.getError_msg());
						}
					}
				});

	}

	Handler mHandler = new Handler();

	private void dealResult(RMRoute result) {
		// 将请求的点设置到地图图层上，并画出线路
		NewFrameActivity.getInstance().getTab0().mRouteLayer
				.setNavigatePoints(result.getPointlist());

		ARTestManager.getInstance().navigatePoints = result.getPointlist();

		// // 直接获取数据，进入AR导航模式
		// int sizeOfKeyPoints = ((TestRtmapFragment) MyFragmentManager
		// .getFragmentByFlag(MyFragmentManager.PROCESS_RT_MAP,
		// MyFragmentManager.FRAGMENT_RT_MAP)).mRouteLayer
		// .getKeyNavigatePoints().size();
		// ARTestManager.getInstance().arNaviList.clear();
		// for (int i = 1; i < sizeOfKeyPoints; i++) {
		// NavigatePoint pointPre = (NavigatePoint) ((TestRtmapFragment)
		// MyFragmentManager
		// .getFragmentByFlag(MyFragmentManager.PROCESS_RT_MAP,
		// MyFragmentManager.FRAGMENT_RT_MAP)).mRouteLayer
		// .getKeyNavigatePoints().get(i - 1);
		// NavigatePoint point = (NavigatePoint)((TestRtmapFragment)
		// MyFragmentManager
		// .getFragmentByFlag(MyFragmentManager.PROCESS_RT_MAP,
		// MyFragmentManager.FRAGMENT_RT_MAP)).mRouteLayer
		// .getKeyNavigatePoints().get(i);
		//
		// arPoiItem item = new arPoiItem();
		//
		// item.setTargetFloor(pointPre.getFloor());
		// item.setRouteInfo(pointPre.getMessageTurn());
		//
		// item.setX(point.getX());
		// item.setY(point.getY());
		// item.setName(point.getMessageTurn());
		//
		// ARTestManager.getInstance().arNaviList.add(item);
		// }

		// 停止更新poi的坐标
		mStopUpdatePois = true;
		// 开启更新选中点的标志位
		mStopUpdateSelect = false;
	}

	Runnable runCompass = new Runnable() {
		@Override
		public void run() {
			if (!mStopDrawing) {
				if (!mStopUpdatePois) {
					ARTestManager.getInstance().updatePois();
				}

				if (!mStopUpdateSelect) {
					relItems.removeAllViewsInLayout();
					ARTestManager.getInstance().updateSelect();

					// 画一条路线到目标点
					drawView = new drawView(mContext);
					relItems.addView(drawView);
					drawView.invalidate();

					// 添加目标点
					View v = LayoutInflater.from(mContext).inflate(
							R.layout.ar_navi_dis, null);
					TextView tv = (TextView) v.findViewById(R.id.distance);
					v.setX(ARTestManager.navix);
					v.setY(ARTestManager.naviy);
					relItems.addView(v);
				}

				mHandler.postDelayed(runCompass, 60);
			}
		}
	};

	@SuppressWarnings("deprecation")
	private void initSensor() {
		mSensorManager = (SensorManager) mContext
				.getSystemService(Context.SENSOR_SERVICE);
		mOrientationSensor = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ORIENTATION);

		registerSensorListener();
	}

	private void registerSensorListener() {
		if (mOrientationSensor != null) {
			mSensorManager.registerListener(sensorEventListener,
					mOrientationSensor, SensorManager.SENSOR_DELAY_GAME);
		} else {
			ToastUtil.shortToast("获取传感器失败");
		}
	}

	// 方向传感器变化监听
	private SensorEventListener sensorEventListener = new SensorEventListener() {
		@Override
		public void onSensorChanged(SensorEvent event) {
			ARTestManager.sensorX = event.values[0];
			ARTestManager.sensorY = event.values[1];
			tvAngle.setText("sensorY:" + ARTestManager.sensorY + "\n");
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	};

	private void initSurfaceView() {
		surfaceView.getHolder()
				.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		surfaceView.getHolder().setFixedSize(1280, 780); // 设置Surface分辨率
		surfaceView.getHolder().setKeepScreenOn(true);// 屏幕常亮
		surfaceView.getHolder().addCallback(new SurfaceCallback());// 为SurfaceView的句柄添加一个回调函数
	}

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
				// 获得手机的方向
				int rotation = getActivity().getWindowManager()
						.getDefaultDisplay().getRotation();
				camera.setDisplayOrientation(ARTestManager.getInstance()
						.getPreviewDegree(rotation));
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
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ar_bt_close:
			dismiss();
			break;

		default:
			break;
		}
	}

	@Override
	public void onDestroy() {
		mStopDrawing = true;

		mSensorManager.unregisterListener(sensorEventListener);

		if (camera != null) {
			camera.stopPreview();
			camera.release(); // 释放照相机
			camera = null;
		}

		super.onDestroy();
	}
}
