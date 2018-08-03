package com.rtmap.locationdemo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.rtm.common.model.POI;
import com.rtm.common.model.RMLocation;
import com.rtm.common.utils.Constants;
import com.rtm.common.utils.RMFileUtil;
import com.rtm.common.utils.RMIOUtils;
import com.rtm.common.utils.RMLog;
import com.rtm.frm.map.MapView;
import com.rtm.frm.map.MapView.OnCompassUpdateListener;
import com.rtm.frm.map.MapView.OnMapModeChangedListener;
import com.rtm.frm.map.POILayer;
import com.rtm.frm.map.RMLocationMode;
import com.rtm.frm.map.TapPOILayer;
import com.rtm.frm.map.TapPOILayer.OnMapLongClickListener;
import com.rtm.frm.map.TapPOILayer.OnPOITappedListener;
import com.rtm.frm.map.XunluMap;
import com.rtm.frm.model.Location;
import com.rtm.frm.model.PointInfo;
import com.rtm.frm.model.RMPoiDetail;
import com.rtm.frm.utils.Handlerlist;
import com.rtm.frm.utils.RMPoiDetailUtil;
import com.rtm.location.LocationApp;
import com.rtm.location.utils.RMLocationListener;
import com.rtmap.locationdemo.beta.R;

public class LocMapActivity extends Activity implements RMLocationListener,
		OnClickListener, OnMapModeChangedListener, OnCheckedChangeListener,
		OnCompassUpdateListener {


	MapView mMapView;
	private POILayer mPoiLayer;
	RadioButton mNormalBtn, mFollowBtn, mCompassBtn;
	private Gson gson = new Gson();

	private Handler mHandler = new Handler() {// 下载地图过程中下载进度消息
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Constants.RTMAP_MAP:
				int progress = msg.arg1;
				if (progress == Constants.MAP_LOAD_START) {// 开始加载
					Log.e("rtmap", "开始加载");
				} else if (progress == Constants.MAP_FailNetResult) {// 校验结果失败
					Log.e("rtmap", "地图校验结果：" + (String) msg.obj);
				} else if (progress == Constants.MAP_FailCheckNet) {// 联网检测失败
					Log.e("rtmap", "校验联网失败");
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
				} else if (progress == Constants.MAP_LOAD_END) {
					Log.e("rtmap", "地图加载完成");
				} else if (progress == Constants.MAP_LICENSE) {
					Log.e("rtmap", "license校验结果：" + (String) msg.obj);
				}
				break;
			}
		}
	};

	private TextView mText;
	String logpath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loc_map);
		Handlerlist.getInstance().register(mHandler);
		mText = (TextView) findViewById(R.id.text);
		initLocation();
		mNormalBtn = (RadioButton) findViewById(R.id.normal);
		mFollowBtn = (RadioButton) findViewById(R.id.follow);
		mCompassBtn = (RadioButton) findViewById(R.id.compass);

		mNormalBtn.setOnCheckedChangeListener(this);
		mFollowBtn.setOnCheckedChangeListener(this);
		mCompassBtn.setOnCheckedChangeListener(this);

		logpath = RMFileUtil.getLogDir() + "location-log-"
				+ System.currentTimeMillis() + ".txt";
		findViewById(R.id.poibtn).setOnClickListener(this);
		initMap();
	}

	private void initMap() {
		RMLog.LOG_LEVEL = RMLog.LOG_LEVEL_ERROR;
		XunluMap.getInstance().init(getApplicationContext());// 初始化
		XunluMap.getInstance().setRootFolder("TestDingtao/public");
		mMapView = (MapView) findViewById(R.id.map_view);
		// mMapView.initMapConfig(buildId, floor);//初始化设置地图防止地图变黑色
		mMapView.setOnMapModeChangedListener(this);
		mMapView.startSensor();// 开启指针方向
		mMapView.setOnCompassUpdateListener(this);
		TapPOILayer tappoiLayer = new TapPOILayer(mMapView);
		mMapView.addLayer(tappoiLayer);
		tappoiLayer.setOnMapLongClickListener(new OnMapLongClickListener() {

			@Override
			public void onMapLongClick(PointInfo point, Location location) {
				Toast.makeText(getApplicationContext(), "长按", 2000).show();
			}
		});
		Drawable blue = getResources().getDrawable(R.drawable.sign_purple);
		Bitmap bitmap = RMIOUtils.drawableToBitmap(blue);
		mPoiLayer = new POILayer(mMapView, bitmap);
		tappoiLayer.setOnPOITappedListener(new OnPOITappedListener() {

			@Override
			public Bitmap onPOITapped(POI poi) {
				Log.i("dt", gson.toJson(poi));
//				mMapView.setCenter(poi.getX(), poi.getY(), true);
				return RMIOUtils.drawableToBitmap(getResources().getDrawable(
						R.drawable.sign_black));
			}
		});
		mMapView.addLayer(mPoiLayer);
		mMapView.setLocationIcon(R.drawable.sign_green, R.drawable.sign_purple);
	}

	/**
	 * 初始化定位
	 */
	private void initLocation() {
		LocationApp.getInstance().init(getApplicationContext());// 初始化定位
		LocationApp.getInstance().registerLocationListener(this);
		LocationApp.getInstance().setTestStatus(false);
		LocationApp.getInstance().setRootFolder("TestDingtao/public");
	}

	@Override
	protected void onResume() {
		super.onResume();
		LocationApp.getInstance().start();// 开始定位
	}

	@Override
	protected void onPause() {
		super.onPause();
		LocationApp.getInstance().stop();
	}

	@Override
	protected void onDestroy() {
		mMapView.removeSensor();
		super.onDestroy();
		mMapView.clearMapLayer();
		LocationApp.getInstance().unRegisterLocationListener(this);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (event.getAction() != KeyEvent.ACTION_DOWN) {
			return false;
		}
		switch (keyCode) {
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			mMapView.clearLocationIconStyle();
			return true;
		case KeyEvent.KEYCODE_VOLUME_UP:
			mMapView.setLocationIcon(R.drawable.sign_black,
					R.drawable.sign_green);
			return true;
		}
		return super.onKeyDown(keyCode, event);

	}

	int i = 0;
	private RMLocation mLocation;

	@Override
	public void onReceiveLocation(RMLocation result) {
		if (result.getError() == 0) {
			mLocation = result;

			// Log.i("rtmap",
			// "result : " + result.getCoordX() + "    "
			// + result.getCoordY() + "   " + result.getFloorID());
			// *********如果固定在某一建筑物的某一楼层定位，则这段代码可以写在onCreate中
//			if (!result.getBuildID().equals(mMapView.getBuildId())
//					|| !mMapView.getFloor().equals(result.getFloor())) {
				mMapView.initMapConfig(result.getBuildID(), result.getFloor());
//			}
			mText.setText(result.getError() + "  " + result.getBuildID()
					+ "   " + result.getFloor() + "   精度：" + result.accuracy
					+ "米\nx: " + String.format("%.3f", result.getX())
					+ "   y: " + result.getY() + "\nUid：" + result.getUserID()
					+ "\n推算类型：" + result.getCalculateType() + "\n偏转角："
					+ LocationApp.getInstance().getMapAngle());
			// *********
			mMapView.setMyCurrentLocation(result);
		}
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.poibtn:
			if (mLocation != null && mLocation.getError() == 0) {
				RMPoiDetailUtil.getPoiInfo(mLocation, null,
						new RMPoiDetailUtil.OnGetPoiDetailListener() {

							@Override
							public void onFinished(RMPoiDetail result) {
								if (result.getError_code() == 0) {
									Toast.makeText(
											getApplicationContext(),
											result.getPoi().getName()
													+ "   id:"
													+ result.getPoi()
															.getPoiNo(),
											Toast.LENGTH_LONG).show();
									mPoiLayer.destroyLayer();
									mPoiLayer.addPoi(result.getPoi());
								}
							}
						});
			}
			break;
		}
	}

	@Override
	public void onMapModeChanged() {
		if (mMapView.getLocationMode() == RMLocationMode.NORMAL) {
			mNormalBtn.setChecked(true);
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isChecked) {
			switch (buttonView.getId()) {
			case R.id.normal:
				mMapView.setLocationMode(RMLocationMode.NORMAL);
				break;
			case R.id.follow:
				mMapView.setLocationMode(RMLocationMode.FOLLOW);
				mMapView.setCenter(new Location(mLocation.getX(), mLocation
						.getY()));
				break;
			case R.id.compass:
				mMapView.setLocationMode(RMLocationMode.COMPASS);
				break;
			}
		}
	}

	@Override
	public void onCompassUpdate(boolean isLeft) {
		// if(isLeft)
		// mMapView.setLocationIcon(R.drawable.sign_black,
		// R.drawable.sign_green);
		// else
		// mMapView.clearLocationIconStyle();
	}
}
