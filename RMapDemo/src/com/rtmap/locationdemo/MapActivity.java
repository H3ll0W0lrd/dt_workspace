package com.rtmap.locationdemo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

import com.rtm.common.model.RMLocation;
import com.rtm.common.utils.Constants;
import com.rtm.frm.map.CompassLayer;
import com.rtm.frm.map.MapView;
import com.rtm.frm.map.MapView.OnMapModeChangedListener;
import com.rtm.frm.map.RMLocationMode;
import com.rtm.frm.map.XunluMap;
import com.rtm.frm.utils.Handlerlist;
import com.rtm.location.LocationApp;
import com.rtm.location.utils.RMLocationListener;
import com.rtmap.mapdemo.R;

/**
 * 定位显示页面 这个页面需要定位功能支持，如果你只需要地图功能，可以移除以下文件和配置信息：1.rtmap_lbs_location_v*.
 * jar和所有libIndoorLoc.so库；2.AndroidManifest.xml中的定位服务配置；
 * 
 * @author dingtao
 *
 */
public class MapActivity extends Activity implements RMLocationListener {

	private MapView mMapView;// 地图view

	private CompassLayer mCompassLayer;// 指南针图层

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
				}else if (progress == Constants.MAP_LOAD_END) {
					Log.e("rtmap", "地图加载完成");
				} else if (progress == Constants.MAP_LICENSE) {
					Log.e("rtmap", "Liscense校验结果：" + (String) msg.obj);
				}
				break;
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_main);

		initLocation();
		initMap();
		

		RadioGroup mGroup = (RadioGroup) findViewById(R.id.radioGroup1);
		mGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == R.id.radio0) {// 自由模式
					mMapView.setLocationMode(RMLocationMode.NORMAL);
				} else {// 跟随模式
					mMapView.setLocationMode(RMLocationMode.FOLLOW);
				}
			}
		});
	}

	/**
	 * 初始化地图
	 */
	private void initMap() {
		Handlerlist.getInstance().register(mHandler);
		XunluMap.getInstance().init(this);// 初始化
		mMapView = (MapView) findViewById(R.id.map_view);
		initLayers();// 初始化图层
		mMapView.initMapConfig("860100010020300001", "F1");// 打开地图（建筑物id，楼层id）
		mMapView.startSensor();// 开启指针方向
	}

	/**
	 * 初始化定位
	 */
	private void initLocation() {
		LocationApp.getInstance().init(getApplicationContext());// 初始化定位
		LocationApp.getInstance().registerLocationListener(this);
	}

	@Override
	public void onReceiveLocation(RMLocation result) {
		if (result.getError() == 0) {
			Log.i("rtmap",
					"result : " + result.getCoordX() + "    "
							+ result.getCoordY() + "   " + result.getFloorID());
			// *********如果固定在某一建筑物的某一楼层定位，则这段代码可以写在onCreate中
			if (!result.getBuildID().equals(mMapView.getBuildId())
					|| !mMapView.getFloor().equals(result.getFloor())) {
				mMapView.initMapConfig(result.getBuildID(), result.getFloor());
			}
			// *********
		}else{
			Log.i("rtmap", result.getErrorInfo());
		}
		mMapView.setMyCurrentLocation(result);
	}

	@Override
	protected void onResume() {
		super.onResume();
		/**
		 * 开启定位会检测AndroidManifest.xml中是否有配置的智慧图的key，如果没有，则定位无法启动
		 */
		boolean result = LocationApp.getInstance().start();// 开始定位
		if (!result)
			Toast.makeText(this, "请输入key", Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onPause() {
		super.onPause();
		LocationApp.getInstance().stop();// 停止定位
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mMapView.removeSensor();//移除罗盘传感器
		mMapView.clearMapLayer();// 移除所有layer
		Handlerlist.getInstance().remove(mHandler);// 移除handler提示
		LocationApp.getInstance().unRegisterLocationListener(this);// 移除定位回调
	}

	/**
	 * 初始化图层
	 */
	private void initLayers() {
		mCompassLayer = new CompassLayer(mMapView);// 指南针图层
		mMapView.addMapLayer(mCompassLayer);
		mMapView.setOnMapModeChangedListener(new OnMapModeChangedListener() {
			
			@Override
			public void onMapModeChanged() {
				if (mMapView.getLocationMode() == RMLocationMode.NORMAL) {
					RadioButton btn = (RadioButton) findViewById(R.id.radio0);
					btn.setChecked(true);
				}
			}
		});
		mMapView.refreshMap();
	}

}