package com.rtmap.locationdemo.ar;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Dialog;
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
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rtm.common.model.POI;
import com.rtm.common.model.RMLocation;
import com.rtm.common.model.RMPois;
import com.rtm.common.utils.Constants;
import com.rtm.common.utils.OnSearchPoiListener;
import com.rtm.common.utils.RMStringUtils;
import com.rtm.frm.model.NavigatePoint;
import com.rtm.frm.utils.Handlerlist;
import com.rtm.frm.utils.RMSearchPoiUtil;
import com.rtm.frm.utils.RMathUtils;
import com.rtm.location.LocationApp;
import com.rtm.location.utils.RMLocationListener;
import com.rtmap.locationdemo.beta.R;
import com.rtmap.locationdemo.util.DTUIUtils;

public class ArShowActivity extends FragmentActivity implements
		RMLocationListener, OnClickListener {

	public static final String KEY_MAP_DEGREE = "KEY_MAP_DEGREE";
	public static final String KEY_SHOW_DISTANCE = "KEY_SHOW_DISTANCE";

	public static ArShowActivity instance;
	// 摄像头相关---------
	private Camera camera;
	private Camera.Parameters parameters = null;
	// 传感器相关---------
	private SensorManager mSensorManager;// 传感器管理对象
	private Sensor mOrientationSensor;// 传感器对象
	// view相关-------------
	private SurfaceView surfaceView;
	// 箭头指向相关
	private boolean isStopArrowRun = true;
	// private MapView mMapView;
	private ImageView mArrow;
	/**
	 * 传感器角度
	 */
	private float mSensorDegree = 0f;

	private int mScreenWidth;
	private int mScreenHeight;
	private List<ArShowView> mARShowViewsByFloor = new ArrayList<ArShowView>();

	private Float INVI_DEGREE = 60f;
	private float mRaidus = 20f;

	private RelativeLayout mRlArItem;
	private float mMapDegree;// 地图角度
	private ArrayList<NavigatePoint> mPointList;

	private Handler mHandler = new Handler() {// 下载地图过程中下载进度消息
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Constants.RTMAP_MAP:
				int progress = msg.arg1;
				Log.e("rtmap", "SDK进度码" + progress);
				if (progress == Constants.MAP_LOAD_START) {// 开始加载
					Log.e("rtmap", "开始加载");
				} else if (progress == Constants.MAP_FailNetResult) {// 校验结果失败
					Log.e("rtmap", "校验结果：" + (String) msg.obj);
				} else if (progress == Constants.MAP_Down_Success) {
					Log.e("rtmap", "地图下载成功");
					Toast.makeText(getApplicationContext(), "地图下载成功",
							Toast.LENGTH_LONG).show();
				} else if (progress == Constants.MAP_Down_Fail) {
					Log.e("rtmap", "地图下载失败");
					Toast.makeText(getApplicationContext(), "地图下载失败",
							Toast.LENGTH_LONG).show();
				} else if (progress == Constants.MAP_Update_Success) {
					Log.e("rtmap", "地图更新成功");
					Toast.makeText(getApplicationContext(), "地图更新成功",
							Toast.LENGTH_LONG).show();
				} else if (progress == Constants.MAP_Update_Fail) {
					Log.e("rtmap", "地图更新失败");
					Toast.makeText(getApplicationContext(), "地图更新失败",
							Toast.LENGTH_LONG).show();
				} else if (progress == Constants.MAP_Down_Fail) {
					Log.e("rtmap", "地图下载失败");
				} else if (progress == Constants.MAP_LOAD_END) {
					Log.e("rtmap", "地图加载完成");
				} else if (progress == Constants.MAP_LICENSE) {
					Log.e("rtmap", "Liscense校验结果：" + (String) msg.obj);
				}
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ar_show);
		// AR工具类初始化
		Intent i = getIntent();

		mMapDegree = i.getFloatExtra(KEY_MAP_DEGREE, 0f);
		mPointList = (ArrayList<NavigatePoint>) i.getExtras().getSerializable(
				"route");
		if (mPointList != null && mPointList.size() > 0) {
			isNoAdd = true;
		}
		instance = this;
		initMapView();
		initView();
		initDialog();
	}

	private void initMapView() {
		// Handlerlist.getInstance().register(mHandler);
		// XunluMap.getInstance().init(this);
		// mMapView = (MapView) findViewById(R.id.mapview);
		// mMapView.setZOrderOnTop(true);
		// mMapView.setLocationIcon(R.drawable.sign_black,
		// R.drawable.sign_green);
	}

	@Override
	protected void onStart() {
		super.onStart();

		init();
	}

	@Override
	protected void onResume() {

		registerSensorListener();
		startArrowRun();
		LocationApp.getInstance().registerLocationListener(this);
		super.onResume();
	}

	@Override
	protected void onPause() {

		unRegisterSensorListener();
		stopArrowRun();
		LocationApp.getInstance().unRegisterLocationListener(this);
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		Handlerlist.getInstance().remove(mHandler);
		ArManager.instance().destroy();
		destroySurfaceView();
		super.onDestroy();
	}

	public void myClick(View v) {
		int i = v.getId();
		if (i == R.id.close) {
			this.finish();

		}
	}

	private void init() {
		initSurfaceView();
		initSensor();
	}

	private TextView mArDisText;

	private void initView() {
		mRlArItem = (RelativeLayout) findViewById(R.id.rl_ar_item);

		surfaceView = (SurfaceView) findViewById(R.id.surfaceview_ar);
		mArrow = (ImageView) findViewById(R.id.imageview_arrow);
		mArrow.setVisibility(View.GONE);
		mArDisText = (TextView) findViewById(R.id.ar_distance_text);
		mArDisText.setVisibility(View.GONE);
		DisplayMetrics dm = this.getResources().getDisplayMetrics();
		mScreenWidth = dm.widthPixels;
		mScreenHeight = dm.heightPixels - ArUtils.getStatusBarHeight(this);
	}

	/***
	 * 设置显示的view
	 *
	 * @param floor
	 */
	private void setARShowViewsByFloor(String floor) {
		mARShowViewsByFloor = ArManager.instance().getArShowViewsByFloor(floor);
		((TextView) findViewById(R.id.btn_floor)).setText("" + floor);

		mRlArItem.removeAllViews();
		for (ArShowView showView : mARShowViewsByFloor) {
			mRlArItem.addView(showView.getLayoutView());
		}
	}

	private void initSensor() {
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mOrientationSensor = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ORIENTATION);
	}

	private void registerSensorListener() {
		if (mOrientationSensor != null) {
			mSensorManager.registerListener(sensorEventListener,
					mOrientationSensor, SensorManager.SENSOR_DELAY_GAME);
		} else {
		}
	}

	private void unRegisterSensorListener() {
		if (mOrientationSensor != null) {
			mSensorManager.unregisterListener(sensorEventListener);
		}
	}

	// 方向传感器变化监听
	private SensorEventListener sensorEventListener = new SensorEventListener() {
		@Override
		public void onSensorChanged(SensorEvent event) {
			float direction = event.values[0] * -1.0f;// 罗盘角度vlues[0]的值为0-360，正北是0然后顺时针方向
			mSensorDegree = ArUtils.getInstance().normalizeDegree(direction);// 赋值给全局变量，让指南针旋转
			Log.i("rtmap", "罗盘角度：" + mSensorDegree);
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	};

	@SuppressWarnings("deprecation")
	private void initSurfaceView() {
		try {
			surfaceView.getHolder().setType(
					SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
			surfaceView.getHolder().setFixedSize(1280, 780); // 设置Surface分辨率
			surfaceView.getHolder().setKeepScreenOn(true);// 屏幕常亮
			surfaceView.getHolder().addCallback(new SurfaceCallback());// 为SurfaceView的句柄添加一个回调函数
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private final class SurfaceCallback implements Callback {
		// 拍照状态变化时调用该方法
		@SuppressWarnings("deprecation")
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			try {
				parameters = camera.getParameters(); // 获取各项参数
				parameters.setPictureFormat(PixelFormat.JPEG); // 设置图片格式
				parameters.setPreviewSize(width, height); // 设置预览大小
				parameters.setPreviewFrameRate(5); // 设置每秒显示4帧
				parameters.setPictureSize(width, height); // 设置保存的图片尺寸
				parameters.setJpegQuality(80); // 设置照片质量
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// 开始拍照时调用该方法
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			try {
				camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK); // 打开摄像头
				camera.setPreviewDisplay(holder); // 设置用于显示拍照影像的SurfaceHolder对象
				// 获得手机的方向
				int degrees = getDisplayRotation();
				Camera.CameraInfo info = new Camera.CameraInfo();
				Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info);
				camera.setDisplayOrientation((info.orientation - degrees + 360) % 360);
				camera.startPreview(); // 开始预览
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// 停止拍照时调用该方法
		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {

			destroySurfaceView();
		}
	}

	public int getDisplayRotation() {
		int rotation = getWindowManager().getDefaultDisplay().getRotation();
		switch (rotation) {
		case Surface.ROTATION_0:
			return 0;
		case Surface.ROTATION_90:
			return 90;
		case Surface.ROTATION_180:
			return 180;
		case Surface.ROTATION_270:
			return 270;
		}
		return 0;
	}

	private void destroySurfaceView() {
		if (camera != null) {
			camera.stopPreview();
			camera.release(); // 释放照相机
			camera = null;
		}
	}

	private void startArrowRun() {
		isStopArrowRun = false;
		mHandler.post(arrowRun);
	}

	private void stopArrowRun() {
		isStopArrowRun = true;
	}

	/**
	 * 用来实时刷新箭头方向和arViews，时间间隔x毫秒
	 */
	protected Runnable arrowRun = new Runnable() {
		@Override
		public void run() {
			if (!isStopArrowRun) {
				drawView();
				mHandler.postDelayed(this, 50);
			}
		}
	};

	/**
	 * 刷新ar view
	 *
	 * @param direction
	 *            void
	 */
	@SuppressLint("NewApi")
	private void drawView() {
		if (mLocation == null || mLocation.getError() != 0) {
			return;
		}

		for (int i = 0; i < mARShowViewsByFloor.size(); i++) {
			ArShowView view = mARShowViewsByFloor.get(i);
			float sensorDegree = mSensorDegree;
			// Log.i("rtmap", "罗盘角度：" + sensorDegree);

			float poiDegree = ArUtils.getInstance()
					.getDegreeBetweenWithThround(mLocation, view);

			// 由于POI角度x,y差值都取了正值，所以现在需要根据x,y位置来判断向量的指向，确定实际角度
			if (mLocation.x > view.getPoiTargetX()
					&& Math.abs(mLocation.getY()) > Math.abs(view
							.getPoiTargetY())) {
				poiDegree = 90 - poiDegree;
			} else if (mLocation.x < view.getPoiTargetX()
					&& Math.abs(mLocation.getY()) > Math.abs(view
							.getPoiTargetY())) {
				poiDegree = 270 - poiDegree;
			} else if (mLocation.x < view.getPoiTargetX()
					&& Math.abs(mLocation.getY()) < Math.abs(view
							.getPoiTargetY())) {
				poiDegree = 270 - poiDegree;
			} else if (mLocation.x > view.getPoiTargetX()
					&& Math.abs(mLocation.getY()) < Math.abs(view
							.getPoiTargetY())) {
				poiDegree = 90 - poiDegree;
			}

			poiDegree = ArUtils.getInstance().normalizeDegree(poiDegree);

			// 差值 = 罗盘角度+地图角度-poi角度
			float dis = sensorDegree + mMapDegree - poiDegree;

			// Log.i("rtmap",
			// "disDegree角度差值：" + dis + "    poi名字："
			// + view.getTargetName() + "    屏幕宽度：" + mScreenWidth);
			dis = ArUtils.getInstance().normalizeDegree(dis);
			if (dis < 60) {
				view.show();
				Float currentX = (float) (dis * mScreenWidth / 60);

				// showDistance = targerDistance * 1.25f;
				float he = RMathUtils.distance(mLocation.x, mLocation.y,
						view.getPoiTargetX(), view.getPoiTargetY());// 长度
				// Log.i("rtmap", "屏幕高宽度：" + mScreenHeight + "    " +
				// mScreenWidth
				// + "   距离：" + he);
				((TextView) view.getLayoutView().findViewById(R.id.distance))
						.setText((int) he + "米");
				Float currentY = (mScreenHeight - 100)
						- ((mScreenHeight - 200) / mRaidus * he);// 屏幕长度/半径*长度

				// 只有在API LEVEL 11及之后才添加，所有使用之前的低版本可能会有问题
				view.setScreenX(currentX);
				view.setScreenY(currentY);
				if (isNoAdd) {
					mArDisText.setText("距离目的地还有" + (int) he + "米");
					rotateArrow(dis + 330);
				}
			} else {
				// 超出范围隐藏view，若设置动画，则停止动画
				view.hide();
			}
		}

	}

	RotateAnimation animation;
	private float mFormDegree;

	private void rotateArrow(float toDegree) {
		animation = new RotateAnimation(mFormDegree, toDegree,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		animation.setDuration(50);// 设置动画持续时间
		animation.setFillAfter(true);// 动画执行完后是否停留在执行完的状态
		animation.start();
		mArrow.setAnimation(animation);

		mFormDegree = toDegree;
	}

	private long time;
	private RMLocation mLocation;

	@Override
	public void onReceiveLocation(RMLocation location) {
		if (location.getError() != 0) {
			return;
		}
		mLocation = location;

		// if (!location.getBuildID().equals(mMapView.getBuildId())
		// || !location.getFloor().equals(mMapView.getFloor())) {
		// mMapView.initMapConfig(location.getBuildID(), location.getFloor());
		// }
		// mMapView.setMyCurrentLocation(location);
		setARShowViewsByFloor(location.getFloor());
		if (System.currentTimeMillis() - time > 10 * 1000 &&isNoAdd) {
			time = System.currentTimeMillis();
			if (mPointList.size() > 0) {
				NavigatePoint mEndPoi = null;
				for (int i = 0; i < mPointList.size(); i++) {
					NavigatePoint p = mPointList.get(i);
					if (!p.getFloor().equals(location.getFloor()) && i > 0) {
						mEndPoi = mPointList.get(i - 1);
						break;
					} else {
						mEndPoi = mPointList.get(i);
					}
				}
				if (mEndPoi != null) {
					POI poi = new POI(0, mEndPoi.getAroundPoiName(),
							mEndPoi.getBuildId(), mEndPoi.getFloor(),
							mEndPoi.getX(), mEndPoi.getY());
					navigatePoi(poi);
				}
			}
		}

		if (System.currentTimeMillis() - time > 15 * 1000 && !isNoAdd) {
			time = System.currentTimeMillis();
			POI poi = new POI(0, null, location.getBuildID(),
					location.getFloor(), location.getX(), Math.abs(location
							.getY()));
			RMSearchPoiUtil.searchAroundPoiListByMap(poi, 20,
					new OnSearchPoiListener() {

						@Override
						public void onSearchPoi(RMPois r) {
							if (r.getError_code() == 0 && !isNoAdd) {
								List<ArShowView> showViews = new ArrayList<ArShowView>();

								for (POI poi : r.getPoilist()) {
									if (RMStringUtils.isEmpty(poi.getName())
											|| "电话机".equals(poi.getName()))
										continue;
									View view = DTUIUtils
											.inflate(R.layout.ar_item_view);
									view.setTag(poi);
									view.setOnClickListener(ArShowActivity.this);
									MyArItem myArItemView = new MyArItem();
									myArItemView.setLayoutView(view);
									myArItemView.setFloor(poi.getFloor());
									myArItemView.setPoiTargetX(poi.getX());
									myArItemView.setPoiTargetY(poi.getY_abs());
									Log.i("rtmap", "poi:" + poi.getX() + "    "
											+ poi.getY_abs());
									myArItemView.setTargetName(poi.getName());
									((TextView) view
											.findViewById(R.id.tv_item_name))
											.setText(poi.getName());
									showViews.add(myArItemView);
								}
								ArManager.instance().setArShowViews(showViews);
							}
						}
					});
		}

	}

	@Override
	public void onClick(View v) {
		POI poi = (POI) v.getTag();
		mSignText.setText("是否前往" + poi.getName() + "?");
		mDialogOk.setTag(poi);
		mDialog.show();
	}

	private Dialog mDialog;
	private TextView mSignText;
	private TextView mDialogOk, mDialogCancel;
	private boolean isNoAdd;

	private void initDialog() {
		mDialog = new Dialog(this, R.style.dialog);
		mDialog.setContentView(R.layout.msg_dialog);
		mDialog.setCanceledOnTouchOutside(true);
		mSignText = (TextView) mDialog.findViewById(R.id.sign);

		mDialogOk = (TextView) mDialog.findViewById(R.id.ok);
		mDialogOk.setText("立即前往");
		mDialogCancel = (TextView) mDialog.findViewById(R.id.cancel);

		mDialogOk.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mDialog.cancel();
				isNoAdd = true;
				POI poi = (POI) v.getTag();
				navigatePoi(poi);
			}
		});
		mDialogCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mDialog.cancel();
			}
		});
	}

	private void navigatePoi(POI poi) {
		View view = DTUIUtils.inflate(R.layout.ar_item_view);
		view.setTag(poi);
		view.setOnClickListener(ArShowActivity.this);
		MyArItem myArItemView = new MyArItem();
		myArItemView.setLayoutView(view);
		myArItemView.setFloor(poi.getFloor());
		myArItemView.setPoiTargetX(poi.getX());
		myArItemView.setPoiTargetY(poi.getY_abs());
		Log.i("rtmap", "poi:" + poi.getX() + "    " + poi.getY_abs());
		myArItemView.setTargetName(poi.getName());
		((TextView) view.findViewById(R.id.tv_item_name))
				.setText(poi.getName());
		ArManager.instance().setArShowView(myArItemView);
		float he = RMathUtils.distance(mLocation.x, mLocation.y,
				poi.getX(), poi.getY());// 长度
		mRaidus = he+5;
		mArDisText.setText("距离目的地还有" + (int) he + "米");
		mArrow.setVisibility(View.VISIBLE);
		mArDisText.setVisibility(View.VISIBLE);
	}
}
