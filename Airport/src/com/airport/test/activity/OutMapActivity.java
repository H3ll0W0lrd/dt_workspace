package com.airport.test.activity;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.airport.test.R;
import com.airport.test.core.AirHttpUrl;
import com.airport.test.layer.DrivingRouteOverlay;
import com.airport.test.model.AirData;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.mapapi.utils.DistanceUtil;
import com.dingtao.libs.DTActivity;
import com.dingtao.libs.DTAsyncTask;
import com.dingtao.libs.DTCallBack;
import com.dingtao.libs.exception.DTException;
import com.dingtao.libs.http.DTHttpUtil;
import com.dingtao.libs.util.DTLog;
import com.dingtao.libs.util.DTUIUtil;
import com.google.gson.Gson;
import com.rtm.common.model.BuildInfo;
import com.rtm.common.model.RMLocation;
import com.rtm.location.LocationApp;
import com.rtm.location.utils.RMLocationListener;

public class OutMapActivity extends DTActivity implements
		BaiduMap.OnMarkerClickListener, BaiduMap.OnMapClickListener,
		BDLocationListener, RMLocationListener, OnClickListener {

	private MapView mBaiDuMapView;
	private BaiduMap mBaiDuMap;

	private TranslateAnimation anim;
	private View stepInto;
	private TextView mWeather, mTemp, mTitle;
	private ImageView mSearch;

	private LocationClient baiDuLocation;
	private BuildInfo mBuild;

	public static void interActivity(Context context) {
		Intent intent = new Intent(context, OutMapActivity.class);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.out_map);

		Gson gson = new Gson();
		mBuild = gson.fromJson(AirData.AIR_DATA, BuildInfo.class);


		mBaiDuMapView = (MapView) findViewById(R.id.mv_baidu);
		mBaiDuMap = mBaiDuMapView.getMap();
		mBaiDuMap.setMyLocationEnabled(true);

		mSearch = (ImageView) findViewById(R.id.sear);
		mSearch.setOnClickListener(this);

		mTemp = (TextView) findViewById(R.id.temp);
		mWeather = (TextView) findViewById(R.id.weather);
		mTitle = (TextView) findViewById(R.id.title);

		mBaiDuMap.setOnMarkerClickListener(this);
		mBaiDuMap.setOnMapClickListener(this);
		openBdLoc();
		new DTAsyncTask(new DTCallBack() {

			@Override
			public Object onCallBackStart(Object... obj) {
				try {
					return DTHttpUtil.getinfo(DTHttpUtil.GET,
							AirHttpUrl.WEATHER);

				} catch (DTException e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			public void onCallBackFinish(Object obj) {
				if (obj != null) {
					String weather = (String) obj;
					try {
						JSONObject o = new JSONObject(weather);
						if (o.getInt("error_code") == 0) {
							JSONObject json = o.getJSONObject("result")
									.getJSONObject("today");
							mTemp.setText(json.getString("temperature"));
							mWeather.setText(json.getString("weather"));
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// {
					// "resultcode":"200",
					// "reason":"successed!",
					// "result":{
					// "today":{
					// "temperature":"16℃~29℃",
					// "weather":"晴转霾",
					// "weather_id":{
					// "fa":"00",
					// "fb":"53"
					// }
					// },
					// "error_code":0
					// }
				}
			}
		}).run();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mBaiDuMapView.onResume();
		baiDuLocation.start();
		LocationApp.getInstance().registerLocationListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		baiDuLocation.stop();
		mBaiDuMapView.onPause();
		LocationApp.getInstance().unRegisterLocationListener(this);
	}

	public void openBdLoc() {
		if (baiDuLocation == null) {
			baiDuLocation = new LocationClient(this);
			baiDuLocation.registerLocationListener(this);
			LocationClientOption option = new LocationClientOption();
			option.setOpenGps(true);// 打开gps
			option.setAddrType("all");
			option.setIsNeedAddress(true);
			option.setCoorType("bd09ll"); // 设置坐标类型
			option.setScanSpan(1000);
			baiDuLocation.setLocOption(option);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mBaiDuMapView.onDestroy();
	}

	/***
	 * 显示marker详情
	 *
	 * @param viewStub
	 * @param marker
	 * @param myLocationData
	 */
	public void showMarkerDetail(final Marker marker,
			MyLocationData myLocationData) {
		View view = findViewById(R.id.build_view);
		view.setVisibility(View.VISIBLE);
		stepInto = findViewById(R.id.tv_step_into);

		anim = new TranslateAnimation(stepInto.getWidth(),
				stepInto.getWidth() + 10, stepInto.getHeight(),
				stepInto.getHeight());

		// 利用 CycleInterpolator 参数 为float 的数 表示 抖动的次数，而抖动的快慢是由 duration 和
		// CycleInterpolator 的参数的大小 联合确定的
		anim.setInterpolator(new CycleInterpolator(3f));
		anim.setDuration(800);
		TextView buildName = (TextView) view.findViewById(R.id.tv_build_name);
		TextView buildDis = (TextView) view.findViewById(R.id.tv_build_dis);
		buildName.setText(mBuild.getBuildName());
		if (myLocationData == null) {
			buildDis.setText("");
		} else {
			double dis = DistanceUtil.getDistance(marker.getPosition(),
					new LatLng(myLocationData.latitude,
							myLocationData.longitude));// 单位为米
			dis = dis / 1000;
			if (dis < 1) {
				buildDis.setText("<1km");
			} else {
				int i = (int) dis * 100;
				dis = i / 100d;
				buildDis.setText(dis + "km");
			}
		}
		view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Bundle b = marker.getExtraInfo();
				APMapActivity.interActivity(OutMapActivity.this);
				finish();
				// goToRtmActivity(buildInfo);进入室内地图
			}
		});

		stepInto.startAnimation(anim);

	}

	private boolean isFirstLoc = true;

	@Override
	public void onReceiveLocation(BDLocation location) {
		// map view 销毁后不在处理新接收的位置
		if (location == null || mBaiDuMap == null)
			return;
		MyLocationData myLocationData = new MyLocationData.Builder()
				// .accuracy(location.getRadius())
				.accuracy(0)
				// 此处设置开发者获取到的方向信息，顺时针0-360
				.direction(100).latitude(location.getLatitude())
				.longitude(location.getLongitude()).build();
		mBaiDuMap.setMyLocationData(myLocationData);
		if (isFirstLoc) {
			isFirstLoc = false;
			LatLng ll = new LatLng(location.getLatitude(),
					location.getLongitude());
			MapStatus mMapStatus = new MapStatus.Builder().target(ll).build();
			MapStatusUpdate u = MapStatusUpdateFactory.newMapStatus(mMapStatus);
			mBaiDuMap.setMapStatus(u);
			startNavigate();// 开启路线规划
		}
		String city = location.getCity();
		if (!TextUtils.isEmpty(city)) {
			mTitle.setText(city + "-" + location.getDistrict());
		}
	}

	@Override
	public String getPageName() {
		return null;
	}

	@Override
	public void onMapClick(LatLng arg0) {

	}

	@Override
	public boolean onMapPoiClick(MapPoi arg0) {
		return false;
	}

	@Override
	public boolean onMarkerClick(Marker arg0) {
		showMarkerDetail(arg0, mBaiDuMap.getLocationData());
		return false;
	}

	@Override
	public void onReceiveLocation(RMLocation result) {
		if (result.error == 0) {
			if (result.getBuildID().equals(mBuild.getBuildId())) {
				APMapActivity.interActivity(this);
				finish();
			}
		}
	}

	// private BuildInfo mBuild;

	private void startNavigate() {
		BitmapDescriptor markerIcon = BitmapDescriptorFactory
				.fromResource(R.drawable.icon_gcoding_red);
		DTLog.i(new Gson().toJson(mBuild));
		OverlayOptions overlayOptions;
		LatLng mBuildLatLag = new LatLng(mBuild.getLat(), mBuild.getLong());
		overlayOptions = new MarkerOptions().position(mBuildLatLag)
				.icon(markerIcon).zIndex(9).draggable(true);
		Marker marker = (Marker) mBaiDuMap.addOverlay(overlayOptions);
		Bundle data = new Bundle();
		data.putSerializable("data", mBuild);
		marker.setTitle(mBuild.getBuildName());
		marker.setExtraInfo(data);
		final RoutePlanSearch mSearch = RoutePlanSearch.newInstance();
		// 第二步，创建驾车线路规划检索监听者；
		OnGetRoutePlanResultListener listener = new OnGetRoutePlanResultListener() {
			public void onGetWalkingRouteResult(WalkingRouteResult result) {
				// 获取步行线路规划结果
			}

			public void onGetTransitRouteResult(TransitRouteResult result) {
				// 获取公交换乘路径规划结果
			}

			public void onGetDrivingRouteResult(DrivingRouteResult result) {
				// 获取驾车线路规划结果
				// 第六步，释放检索实例；
				if (result == null
						|| result.error != SearchResult.ERRORNO.NO_ERROR) {
					DTUIUtil.showToastSafe("抱歉，未找到结果");
				}
				if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
					// 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
					// result.getSuggestAddrInfo()
					return;
				}
				if (result.error == SearchResult.ERRORNO.NO_ERROR) {
					DrivingRouteOverlay overlay = new DrivingRouteOverlay(
							mBaiDuMap);
					mBaiDuMap.setOnMarkerClickListener(overlay);
					overlay.setData(result.getRouteLines().get(0));
					overlay.addToMap();
					overlay.zoomToSpan();
				}
				mSearch.destroy();
			}

			@Override
			public void onGetBikingRouteResult(BikingRouteResult arg0) {

			}
		};
		// 第三步，设置驾车线路规划检索监听者；
		mSearch.setOnGetRoutePlanResultListener(listener);
		// 第四步，准备检索起、终点信息；
		// PlanNode stNode = PlanNode.withLocation(new
		// LatLng(location.getLatitude(), location.getLongitude()));
		MyLocationData d = mBaiDuMap.getLocationData();
		PlanNode stNode = PlanNode.withLocation(new LatLng(d.latitude,
				d.longitude));
		PlanNode enNode = PlanNode.withLocation(new LatLng(mBuild.getLat(),
				mBuild.getLong()));
		// 第五步，发起驾车线路规划检索；
		mSearch.drivingSearch((new DrivingRoutePlanOption()).from(stNode).to(
				enNode));
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.sear:
			LatLng lat = null;
			if (mBaiDuMap.getLocationData() != null) {
				lat = new LatLng(mBaiDuMap.getLocationData().latitude,
						mBaiDuMap.getLocationData().longitude);
			}
			SearchActivity.interActivity(this, lat);
			break;

		default:
			break;
		}
	}

}
