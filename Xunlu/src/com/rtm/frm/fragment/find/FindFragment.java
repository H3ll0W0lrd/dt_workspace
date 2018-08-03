/**
 *@author hukunge
 *@date 2014.09.03 14:19
 */
package com.rtm.frm.fragment.find;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rtm.common.model.POI;
import com.rtm.frm.R;
import com.rtm.frm.fragment.BaseFragment;
import com.rtm.frm.fragment.BaseFragment.OnFinishListener;
import com.rtm.frm.fragment.controller.FindManager;
import com.rtm.frm.fragment.controller.MyFragmentManager;
import com.rtm.frm.intf.LocationImpl;
import com.rtm.frm.intf.LocationObserverInf;
import com.rtm.frm.model.MyLocation;
import com.rtm.frm.net.PostData;
import com.rtm.frm.newframe.NewFrameActivity;
import com.rtm.frm.utils.ConstantsUtil;
import com.rtm.frm.utils.PreferencesUtil;
import com.rtm.frm.utils.ToastUtil;
import com.rtm.frm.utils.XunluUtil;

@SuppressLint("NewApi")
public class FindFragment extends BaseFragment implements OnFinishListener,
		LocationObserverInf, View.OnClickListener {
	View contentView;
	SectorView sectorView;
	ImageView img;
	ImageView imgHead;
	RelativeLayout relBaozang;
	FrameLayout headFrame;
	Button btBZList;
	Button btBack;
	TextView tvTitle;
	// 方向传感器和磁传感器做的指南针
	private SensorManager mSensorManager;// 传感器管理对象
	private Sensor mOrientationSensor;// 传感器对象
	public boolean isStopSearch = false;
	public boolean isStopPOIShowing = true;// 是否停止将poi显示在屏幕上
	private List<View> poiViews = new ArrayList<View>();// 必须放在list里
	Runnable run;

	public FindFragment() {
		// this.setStyle(DialogFragment.STYLE_NORMAL,
		// R.style.dialogfragment_transparent_bg);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewgroup,
			Bundle b) {
		super.onCreateView(inflater, viewgroup, b);
		contentView = inflater
				.inflate(R.layout.fragment_find, viewgroup, false);

		FindManager.isFindShowing = true;

		// 添加位置监听
		LocationImpl.getInstance().addObserver(this);

		initView(contentView);

		postFresh();// 定时刷新雷达动画和poi位置

		initSensor();// 初始化传感器

		return contentView;
	}

	@Override
	public void onViewCreated(View v, Bundle b) {
		super.onViewCreated(v, b);

		initData();
	}

	@Override
	public void onDestroy() {
		// FindManager.getInstance().isDone = false;
		FindManager.isFindShowing = false;
		stopSensor();
		stopFresh();
		// 移除位置监听
		LocationImpl.getInstance().removeObserver(this);
		super.onDestroy();
	}

	private void initSensor() {
		mSensorManager = (SensorManager) mContext
				.getSystemService(Context.SENSOR_SERVICE);
		mOrientationSensor = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		startSensor();
	}

	private void startSensor() {
		mSensorManager.registerListener(mOrientationSensorEventListener,
				mOrientationSensor, SensorManager.SENSOR_DELAY_GAME);
	}

	private void stopSensor() {
		mSensorManager.unregisterListener(mOrientationSensorEventListener);
	}

	// 方向传感器变化监听
	private SensorEventListener mOrientationSensorEventListener = new SensorEventListener() {
		@Override
		public void onSensorChanged(SensorEvent event) {
			// 记录当前旋转后的角度
			FindManager.compassAngle = event.values[0];
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	};


	private void initData() {
		// map偏移角度
		try {

			FindManager.mapDegree = -(float) Math.toDegrees(NewFrameActivity
					.getInstance().getTab0().mMapView.getConfig().getDrawMap()
					.getAngle());
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 屏幕宽高
		FindManager.width = mContext.getResources().getDisplayMetrics().widthPixels;
		FindManager.height_T = mContext.getResources().getDisplayMetrics().heightPixels;
		FindManager.statusBarHeight = XunluUtil.getStatusBarHeight(mContext);
		FindManager.height = FindManager.height_T - FindManager.statusBarHeight;

		// 取得图片的长宽
		Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(),
				R.drawable.baoxiang_far);
		FindManager.imgHeight = bitmap.getHeight();
		FindManager.imgWidth = bitmap.getWidth();
		bitmap.recycle();
	}

	@SuppressLint("NewApi")
	private void initView(View v) {
		btBZList = (Button) v.findViewById(R.id.bt_bz_list);
		btBZList.setOnClickListener(this);

		img = (ImageView) v.findViewById(R.id.leida_rel);
		relBaozang = (RelativeLayout) v.findViewById(R.id.baozang_rel);
		headFrame = (FrameLayout) v.findViewById(R.id.baozang_frame_head);
		imgHead = (ImageView) v.findViewById(R.id.baozang_head);

		btBack = (Button) v.findViewById(R.id.bt_back);
		btBack.setOnClickListener(this);
		tvTitle = (TextView) v.findViewById(R.id.tv_title);
	}

	@Override
	public void onResume() {
		FindManager.getInstance().currentAngle = 0;
		super.onResume();
	}

	private void postFresh() {
		run = new Runnable() {// 定时刷新
			@Override
			public void run() {
				if (!isStopSearch) {
					// 刷新雷达扫描图片在屏幕上位置
					FindManager.getInstance().currentAngle -= 2;
					showLeida();// 旋转的图片

					// 刷新宝藏在屏幕上的位置
					if (!isStopPOIShowing) {
						FindManager.getInstance().updateXY();// 计算宝物在屏幕上的位置
						FindManager.getInstance().getNearestDistance();// 计算宝物和我的位置的距离
						updateBaozang();// 将宝物添加到屏幕上

						showSucc();
					}

					mHandler.postDelayed(run, 40);
				}
			}
		};

		mHandler.post(run);
	}

	boolean isSuccProcess = false;

	private void showSucc() {
		if (FindManager.getInstance().nearestDis < 5f) {
			if (isSuccProcess) {
				return;
			}
		}

	}

	private void stopFresh() {
		isStopSearch = true;
	}

	private void startFresh() {
		isStopSearch = false;
		mHandler.post(run);
	}

	private void showLeida() {
		// 从上次的角度旋转
		RotateAnimation ra = new RotateAnimation(
				FindManager.getInstance().currentAngle,
				FindManager.getInstance().currentAngle - 2,
				FindManager.width / 2, FindManager.height / 2);
		ra.setDuration(300);
		// 设置动画结束后的保留状态
		ra.setFillAfter(true);
		img.startAnimation(ra);
	}

	boolean isStarted = false;
	@SuppressLint("HandlerLeak")
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case ConstantsUtil.HANDLER_POST_BAOZANG:

				if (!isStarted
						&& msg.arg1 == ConstantsUtil.STATE_NET_ERR_UNUSED) {
					isStarted = true;

					Log.d("kunge.hu", "开启请求，每秒一次");
					return;
				}

				String result = (String) msg.obj;
				FindManager.getInstance().parseResult(mHandler, result);
				break;
			case ConstantsUtil.HANDLER_POST_BAOZANG_POSITION:
				// 判断宝物是否被挖完
				FindManager.getInstance().getNearestPoi();
				clearBaozangInMap();

				// 如果没有挖完，就继续重新添加宝物
				isStopPOIShowing = true;// 停止显示poi

				float dis = FindManager.getInstance().getNearestDistance();
				if (dis > 10f) {
					FindManager.radius = FindManager.height / 3;
				} else if (dis > 0f && dis <= 10f) {
					FindManager.radius = FindManager.height / 6;
				}

				addBaozangToMap();
				isStopPOIShowing = false;// 开始显示poi
				break;
			case ConstantsUtil.HANDLER_POST_BAOZANG_NO:// 如果没有宝藏就重复请求
				ToastUtil.longToast("在您附近未发现宝藏");
				break;
			case ConstantsUtil.HANDLER_POST_TO_SUCC:
				String data = (String) msg.obj;
				parseBaoZangSuccResult(data);
				break;
			default:
				break;
			}
		}
	};

	public void parseBaoZangSuccResult(String data) {
		String error = "-1";
		String id = "暂无数据";
		try {
			JSONObject mDataObject = new JSONObject(data);
			error = mDataObject.getString("error");
			String msg = mDataObject.getString("msg");
			JSONArray jsonArray = mDataObject.getJSONArray("data");

			JSONObject js = jsonArray.getJSONObject(0);
			id = js.getString("id");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		if ("0".equals(error)) {// 寻宝成功
			stopSensor();
			stopFresh();

			SuccessFragment succ = new SuccessFragment();
			succ.setKey(id);
			succ.setOnFinishListener(this);
			MyFragmentManager.getInstance().addFragment(
					NewFrameActivity.ID_ALL, succ,
					MyFragmentManager.PRCOCESS_DIALOGFRAGMENT_SUCC,
					MyFragmentManager.DIALOGFRAGMENT_SUCC);

			// 将寻宝成功的点保存到本地
		} else {
			if ("1".equals(error)) {
				ToastUtil.shortToast("缺少参数");
			} else if ("2".equals(error)) {
				ToastUtil.shortToast("异常错误");
			} else if ("3".equals(error)) {
				ToastUtil.shortToast("宝物过期");
				// 宝物过期也存在本地，下次不在显示
			} else if ("4".equals(error)) {
				ToastUtil.shortToast("宝物已经被其他人找到");
				// 宝物被其他人找到了也保存在本地，下次不在显示

				stopSensor();
				stopFresh();
				FindFailFragment fail = new FindFailFragment();
				fail.setOnFinishListener(this);
				MyFragmentManager.getInstance().addFragment(
						NewFrameActivity.ID_ALL, fail,
						MyFragmentManager.PRCOCESS_DIALOGFRAGMENT_FIND_FAIL,
						MyFragmentManager.DIALOGFRAGMENT_FIND_LIST_FAIL);
			} else {
				ToastUtil.shortToast("未知错误");
			}
			Message msg = new Message();
			msg.what = ConstantsUtil.HANDLER_POST_BAOZANG_POSITION;
			mHandler.sendMessage(msg);
		}
	}

	boolean stopBaoZangRun = true;

	private void updateBaozang() {
		View v = poiViews.get(0);
		v.setX(FindManager.map_poi_x);
		v.setY(FindManager.map_poi_y);

		ImageView imgbtn = (ImageView) v.findViewById(R.id.imgView);

		imgbtn.setImageResource(R.drawable.baoxiang_far);
	}

	private void addBaozangToMap() {
		// // 若最近的poi在十米之内，放第一个圈，否则放第二个圈
		View v = View.inflate(mContext, R.layout.baozang, null);

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);

		ImageView imgbtn = (ImageView) v.findViewById(R.id.imgView);

		imgbtn.setImageResource(R.drawable.baoxiang_far);

		poiViews.add(v);// 将view放到list里
		relBaozang.addView(v, params);// 将view添加到布局中
	}

	private void clearBaozangInMap() {
		poiViews.clear();
		relBaozang.removeAllViews();
	}

	@Override
	public void onFinish(String flag, Bundle data) {
		boolean isCloseAll = data.getBoolean("isCloseAll", false);
		boolean isCancel = data.getBoolean("isCancel", false);
		boolean isKeep = data.getBoolean("isKeepSucc", false);
		if (isCloseAll) {
			MyFragmentManager.getInstance().backFragment();
			return;
		} else if (isCancel) {
			// do something
		} else if (isKeep) {

			Message msg = new Message();
			msg.what = ConstantsUtil.HANDLER_POST_BAOZANG_POSITION;
			mHandler.sendMessage(msg);
		}
		isSuccProcess = false;
		startSensor();
		startFresh();
	}

	@Override
	public void onUpdateLocation(MyLocation myLocation) {
		FindManager.center_x = myLocation.getX();
		FindManager.center_y = myLocation.getY();
		FindManager.mCurrentFloor = myLocation.getFloor();

		// 实时计算当前位置和宝物的距离
		getBaozangDis();
	}

	private void getBaozangDis() {
		if (FindManager.getInstance().nearestPOI == null) {
			return;
		}
		float dis = FindManager.getInstance().nearestDis;
		if (dis < 10) {
			FindManager.radius = FindManager.height / 6;
		} else if (dis >= 10) {
			FindManager.radius = FindManager.height / 3;
		}

		if (isStopPOIShowing) {
			tvTitle.setText("");
		} else {
			tvTitle.setText("您距离宝箱 " + (int) dis + " m");
		}
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.bt_back:
			MyFragmentManager.getInstance().backFragment();
			break;
		case R.id.bt_bz_list:
			stopSensor();
			stopFresh();
			BZListFragment bzList = new BZListFragment();
			bzList.setOnFinishListener(this);
			MyFragmentManager.getInstance().addFragment(
					NewFrameActivity.ID_ALL, bzList,
					MyFragmentManager.PRCOCESS_DIALOGFRAGMENT_FIND_LIST,
					MyFragmentManager.DIALOGFRAGMENT_FIND_LIST);
			break;
		default:
			break;
		}
	}
}