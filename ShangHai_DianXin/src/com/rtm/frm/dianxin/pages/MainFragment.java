package com.rtm.frm.dianxin.pages;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ZoomControls;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
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
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.rtm.common.model.RMLocation;
import com.rtm.frm.dianxin.R;
import com.rtm.frm.dianxin.RtmActivity;
import com.rtm.frm.dianxin.UserActivity;
import com.rtm.frm.dianxin.base.BaseActivity;
import com.rtm.frm.dianxin.base.BaseFragment;
import com.rtm.frm.dianxin.bean.PrivateBuild;
import com.rtm.frm.dianxin.manager.AppContext;
import com.rtm.frm.dianxin.manager.RtMapLocManager;
import com.rtm.frm.dianxin.utils.BaiDuSdkUtil;
import com.rtm.frm.dianxin.utils.DialogUtil;
import com.rtm.frm.dianxin.utils.MyUtil;
import com.rtm.frm.dianxin.utils.NetWorkUtil;
import com.rtm.frm.dianxin.utils.RMlbsUtils;
import com.rtm.frm.dianxin.utils.SharePrefUtil;
import com.rtm.frm.dianxin.utils.StringUtils;
import com.rtm.frm.dianxin.utils.ToastUtils;
import com.rtm.frm.model.BuildInfo;
import com.rtm.frm.model.POI;
import com.rtm.frm.model.RMRoute;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ly on 15-7-24.
 */
public class MainFragment extends BaseFragment implements
		BaiduMap.OnMarkerClickListener, BaiduMap.OnMapClickListener,
		View.OnClickListener, BDLocationListener,
		RMlbsUtils.OnRmGetFinishListener,
		RtMapLocManager.RtMapLocManagerListener,
		BaiduMap.OnMapStatusChangeListener {
	private MapView mBaiDuMapView;
	private BaiduMap mBaiDuMap;
	private ViewStub vsMarkerDetail;
	private Button btnMyLocation;
	private LinearLayout llSearch;
	private TextView tvChangeCity;
	private ImageView imgPrivateBuild;
	private TextView tvDebug;

	@Override
	protected View initView(LayoutInflater inflater) {
		getActivity().getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		View rootView = inflater.inflate(R.layout.fragment_main, null);
		vsMarkerDetail = (ViewStub) rootView
				.findViewById(R.id.vs_marker_detail);
		btnMyLocation = (Button) rootView.findViewById(R.id.btn_my_location);
		llSearch = (LinearLayout) rootView.findViewById(R.id.ll_search);
		tvChangeCity = (TextView) rootView.findViewById(R.id.tv_change_city);
		tvDebug = (TextView) rootView.findViewById(R.id.tv_debug);
		rootView.findViewById(R.id.button1).setOnClickListener(this);
		tvDebug.setVisibility(View.GONE);
		mBaiDuMapView = (MapView) rootView.findViewById(R.id.mv_baidu);
		imgPrivateBuild = (ImageView) rootView
				.findViewById(R.id.img_private_build);
		mBaiDuMap = mBaiDuMapView.getMap();
		mBaiDuMap.setMyLocationEnabled(true);

		mBaiDuMap.setOnMapStatusChangeListener(this);

		RtMapLocManager.instance().addReceiver(this);

		// if (!WifiUtils.isWifiActive(context)){
		// WifiUtils.toggleWiFi(context,true);
		// }
		return rootView;
	}

	@Override
	protected void setListener() {
		mBaiDuMap.setOnMarkerClickListener(this);
		mBaiDuMap.setOnMapClickListener(this);
		btnMyLocation.setOnClickListener(this);
		llSearch.setOnClickListener(this);
		tvChangeCity.setOnClickListener(this);
		imgPrivateBuild.setOnClickListener(this);
	}

	@Override
	protected void initData(Bundle savedInstanceState) {

		mBaiDuMapView.removeViewAt(1);
		int childCount = mBaiDuMapView.getChildCount();
		for (int i = 0; i < childCount; ++i) {
			if (mBaiDuMapView.getChildAt(i) instanceof ZoomControls) {
				mBaiDuMapView.getChildAt(i).setVisibility(View.GONE);
			}
		}

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				Point p = new Point(MyUtil.dip2px(40), MyUtil.dip2px(90));
				UiSettings settings = mBaiDuMap.getUiSettings();
				settings.setCompassEnabled(true);
				settings.setCompassPosition(p);
			}
		}, 1000);
		((BaseActivity) getActivity()).openBdLoc(getActivity(), this);
		// if (NetWorkUtil.isNetworkConnected(context)) {
		// showLoading();
		// } else {
		// ToastUtils.shortToast(R.string.network_not_connected);
		// }

		// 如果已经登录，显示私有建筑列表入口
		String username = SharePrefUtil.getString(AppContext.instance(),
				"username", "");
		String password = SharePrefUtil.getString(AppContext.instance(),
				"password", "");
		if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) {
			imgPrivateBuild.setVisibility(View.VISIBLE);
			startFlick(imgPrivateBuild);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		mBaiDuMapView.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		mBaiDuMapView.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mBaiDuMapView.onDestroy();
		RtMapLocManager.instance().destroy();
		AppContext.instance().mChooseCity = "";
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		BitmapDescriptor bd = BitmapDescriptorFactory
				.fromResource(R.drawable.icon_gcoding_blue);
		marker.setIcon(bd);
		showMarkerDetail(marker);
		return true;
	}

	@Override
	public void onMapClick(LatLng latLng) {
		BaiDuSdkUtil.hideMarkerDetail(vsMarkerDetail);
	}

	@Override
	public boolean onMapPoiClick(MapPoi mapPoi) {
		return false;
	}

	private Marker mLastClickMarker;

	/**
	 * @return void
	 * @author LiYan
	 * @date 2014-9-7 下午8:48:18
	 * @explain 将上一次点击的marker背景恢复默认红色
	 */
	private void exchangeLastClickMarker(Marker clickMarker) {
		BitmapDescriptor bd = BitmapDescriptorFactory
				.fromResource(R.drawable.icon_gcoding_red);
		if (mLastClickMarker != null
				&& !mLastClickMarker.getTitle().toString()
						.equals(clickMarker.getTitle().toString())) {
			mLastClickMarker.setIcon(bd);
		}
		mLastClickMarker = clickMarker;
		bd.recycle();
	}

	/***
	 * 地图显示制定建筑poi
	 *
	 * @param buildInfo
	 */
	public void findBuildOnMap(BuildInfo buildInfo) {
		List<BuildInfo> buildInfos = new ArrayList<BuildInfo>();
		buildInfos.add(buildInfo);
		BaiDuSdkUtil.changeToBdLatLngBatch(buildInfos);

		BitmapDescriptor markerIcon = BitmapDescriptorFactory
				.fromResource(R.drawable.icon_flag_blue);
		OverlayOptions overlayOptions;
		LatLng buildLatLag = new LatLng(buildInfos.get(0).getLat(), buildInfos
				.get(0).getLong());
		overlayOptions = new MarkerOptions().position(buildLatLag)
				.icon(markerIcon).zIndex(9).draggable(true);
		Marker marker = (Marker) mBaiDuMap.addOverlay(overlayOptions);
		Bundle data = new Bundle();
		data.putSerializable("data", buildInfos.get(0));
		marker.setTitle(buildInfos.get(0).getBuildName());
		marker.setExtraInfo(data);

		LatLng ll = new LatLng(buildInfos.get(0).getLat(), buildInfos.get(0)
				.getLong());
		MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
		mBaiDuMap.animateMapStatus(u);
		showMarkerDetail(marker);
	}

	/***
	 * 显示marker详情
	 *
	 * @param marker
	 */
	private void showMarkerDetail(Marker marker) {
		exchangeLastClickMarker(marker);
		BaiDuSdkUtil.showMarkerDetail(vsMarkerDetail, marker,
				mBaiDuMap.getLocationData(),
				new BaiDuSdkUtil.OnMarkerDetailClickListerner() {
					@Override
					public void onMarkerDetailClickListener(Marker marker) {
						Bundle b = marker.getExtraInfo();
						BuildInfo buildInfo = (BuildInfo) b
								.getSerializable("data");
						goToRtmActivity(buildInfo);
					}
				});
	}

	/***
	 * 根据建筑信息进入，室内地图
	 *
	 * @param buildInfo
	 */
	private void goToRtmActivity(BuildInfo buildInfo) {
		// 由于建筑列表中的buildInfo，没有楼层信息，因此，需要调用建筑详情借口获取一下buildInfo完整对象（里面包括楼层信息）
		if (getBuildDetailFinish) {
			getBuildDetailFinish = false;
			showLoading();
			if(!RMlbsUtils.getInstance().getBuildDetail(buildInfo.getBuildId(),
					this)){
				dismissLoading();
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button1:
			Intent intent = new Intent(getActivity(),UserActivity.class);
			startActivity(intent);
			break;
		case R.id.btn_my_location: {
			if (myLocationData != null) {
				LatLng ll = new LatLng(myLocationData.latitude,
						myLocationData.longitude);
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
				mBaiDuMap.setMapStatus(u);// .animateMapStatus(u);
				isFromClickLoc = true;
				getBuildByCity(locCity);
				setFollowing(true);
			} else {
				ToastUtils.shortToast(R.string.no_locate);
			}
		}
			break;
		case R.id.ll_search: {
			if (!StringUtils.isEmpty(AppContext.instance().mChooseCity)) {
				SearchBuildFragment searchBuildFragment = new SearchBuildFragment();
				searchBuildFragment.setLocationData(myLocationData,
						AppContext.instance().mChooseCity);
				((BaseActivity) getActivity()).pushFragment(
						searchBuildFragment, R.id.main_content);
			}
		}
			break;
		case R.id.tv_change_city: {
			getCityList();
		}
			break;
		case R.id.img_private_build: {
			SearchBuildFragment searchBuildFragment = new SearchBuildFragment();
			searchBuildFragment.setLocationData(myLocationData, null);
			((BaseActivity) getActivity()).pushFragment(searchBuildFragment,
					R.id.main_content);
		}
			break;
		}
	}

	private boolean isFirstLoc = true;
	private MyLocationData myLocationData;
	private String locCity = "";

	@Override
	public void onReceiveLocation(BDLocation location) {
		// map view 销毁后不在处理新接收的位置
		if (location == null || mBaiDuMap == null)
			return;
		RtMapLocManager.instance().setBdLocation(location);

		// 测试上海定位
		// double longitude = 121.47535;
		// double latitude = 31.235811;
		// location.setLongitude(longitude);
		// location.setLatitude(latitude);
		myLocationData = new MyLocationData.Builder()
				// .accuracy(location.getRadius())
				.accuracy(0)
				// 此处设置开发者获取到的方向信息，顺时针0-360
				.direction(100).latitude(location.getLatitude())
				.longitude(location.getLongitude()).build();
		mBaiDuMap.setMyLocationData(myLocationData);
		String city = location.getCity();
		if (!TextUtils.isEmpty(city) && !getBuildFinish) {
			getBuildFinish = true;
			locCity = city = city.replace("市", "");
			getBuildByCity(city.replace("市", ""));
		}
		if (isFirstLoc) {
			isFirstLoc = false;
			LatLng ll = new LatLng(location.getLatitude(),
					location.getLongitude());
			MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
			mBaiDuMap.animateMapStatus(u);
		}
	}

	/***
	 * 根据城市名显示建筑
	 *
	 * @param city
	 */
	public void getBuildByCity(String city) {
		if (!StringUtils.isEmpty(city)
				&& !city.equals(AppContext.instance().mChooseCity)) {
			AppContext.instance().mChooseCity = city;
			tvChangeCity.setText(city);
			showLoading();
			if(!RMlbsUtils.getInstance().getBuildList(city, this)){
				dismissLoading();
			}
		}
	}

	private void getCityList() {
		showLoading();
		if(!RMlbsUtils.getInstance().getCityList(this)){
			dismissLoading();
		}
	}

	@Override
	public void onGetCityListFinish(List<String> result) {
		dismissLoading();
		if (result != null) {
			CityListFragment cityListFragment = new CityListFragment();
			cityListFragment.setCityList(result, tvChangeCity);
			((BaseActivity) getActivity()).pushFragment(cityListFragment,
					R.id.main_content);
		} else {
			ToastUtils.shortToast(R.string.no_result);
		}
	}

	private boolean getBuildFinish = false;
	private boolean isFromClickLoc = false;

	@Override
	public void onGetBuildListFinish(final List<BuildInfo> result) {
		dismissLoading();
		getBuildFinish = true;
		mBaiDuMap.clear();
		if (result == null || result.size() == 0) {
			return;
		}

		new Thread() {
			@Override
			public void run() {

				BaiDuSdkUtil.changeToBdLatLngBatch(result);
				BitmapDescriptor markerIcon = BitmapDescriptorFactory
						.fromResource(R.drawable.icon_gcoding_red);
				int i = 0;
				double latSum = 0;
				double lngSum = 0;
				for (BuildInfo build : result) {
					OverlayOptions overlayOptions;
					LatLng buildLatLag = new LatLng(build.getLat(),
							build.getLong());
					overlayOptions = new MarkerOptions().position(buildLatLag)
							.icon(markerIcon).zIndex(9).draggable(true);
					Marker marker = (Marker) mBaiDuMap
							.addOverlay(overlayOptions);
					Bundle data = new Bundle();
					data.putSerializable("data", build);
					marker.setTitle(build.getBuildName());
					marker.setExtraInfo(data);
					latSum += build.getLat();
					lngSum += build.getLong();
					i++;
				}
				latSum /= i;
				lngSum /= i;
				LatLng ll = new LatLng(latSum, lngSum);
				if (!isFromClickLoc) {
					mBaiDuMap
							.setMapStatus(MapStatusUpdateFactory.zoomTo(18.0f));

					MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
					mBaiDuMap.animateMapStatus(u);
				} else {
					isFromClickLoc = false;
				}
			}
		}.start();
	}

	private boolean getBuildDetailFinish = true;

	@Override
	public void onGetBuildDetailFinish(BuildInfo result) {
		dismissLoading();
		getBuildDetailFinish = true;
		if (result != null) {
			BaiDuSdkUtil.hideMarkerDetail(vsMarkerDetail);
			Bundle b = new Bundle();
			b.putSerializable("data", result);
			b.putBoolean("follow", RtMapLocManager.instance().isFollowing());
			((BaseActivity) getActivity()).pushActivity(RtmActivity.class, b);
		}
	}

	@Override
	public void onGetNavigationFinish(RMRoute result) {

	}

	@Override
	public void onGetPoiSearchFinish(List<POI> result) {

	}

	@Override
	public void onGetPrivateBuildFinish(List<PrivateBuild> result) {

	}

	private Dialog loading;

	private void showLoading() {
		try {
			if (loading == null) {
				loading = DialogUtil.getLoadingDialog(getActivity(), false,
						null);
			}
			if (loading.isShowing()) {
				return;
			}
			loading.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void dismissLoading() {
		if (loading != null && loading.isShowing()) {
			loading.dismiss();
		}
	}

	/**
	 * 开启View闪烁效果
	 */

	private void startFlick(View view) {

		if (null == view) {
			return;
		}
		Animation alphaAnimation = new AlphaAnimation(1, 0);
		alphaAnimation.setDuration(1000);
		alphaAnimation.setInterpolator(new LinearInterpolator());
		alphaAnimation.setRepeatCount(Animation.INFINITE);
		alphaAnimation.setRepeatMode(Animation.REVERSE);
		view.startAnimation(alphaAnimation);
	}

	/**
	 * 取消View闪烁效果
	 */

	private void stopFlick(View view) {
		if (null == view) {
			return;
		}
		view.clearAnimation();
	}

	private void setFollowing(boolean isFollow) {
		MyLocationConfiguration.LocationMode setMode = isFollow ? MyLocationConfiguration.LocationMode.FOLLOWING
				: MyLocationConfiguration.LocationMode.NORMAL;
		if (mBaiDuMap.getLocationConfigeration() != null
				&& setMode == mBaiDuMap.getLocationConfigeration().locationMode) {
			return;
		}

		mBaiDuMap.setMyLocationConfigeration(new MyLocationConfiguration(
				setMode, true, null));
		if (setMode == MyLocationConfiguration.LocationMode.FOLLOWING) {
			btnMyLocation.setBackgroundResource(R.drawable.local_flow);
		} else {
			btnMyLocation.setBackgroundResource(R.drawable.local_my);
		}
		RtMapLocManager.instance().setFollowMode(isFollow);
	}

	private boolean isTipShowed = false;

	@Override
	public void onRtMapLocListenerReceiver(RMLocation rmLocation,
			boolean isFollowing) {
		setFollowing(isFollowing);
		if (rmLocation == null) {
			return;
		}

		// upLoadLocation(rmLocation);
		tvDebug.setText("室内外："+rmLocation.getInOutDoorFlg() + " | err:"
				+ rmLocation.error + " | id:" + rmLocation.getBuildID()
				+ " | 获取时间:" + System.currentTimeMillis());
		// tvChangeCity.setText(rmLocation.getInOutDoorFlg() + "");
		if (rmLocation.getInOutDoorFlg() == RMLocation.LOC_INDOOR
				&& rmLocation.error == 0) {
			if (isFollowing) {
				if (getBuildDetailFinish) {
					getBuildDetailFinish = false;
					RMlbsUtils.getInstance().getBuildDetail(
							rmLocation.getBuildID(), this);
				}
				rootView.findViewById(R.id.tv_tip).setVisibility(View.GONE);
			} else {
				if (!isTipShowed) {
					isTipShowed = true;
					RMlbsUtils.getInstance().getBuildDetail(
							rmLocation.getBuildID(),
							new RMlbsUtils.OnRmGetFinishListener() {
								@Override
								public void onGetCityListFinish(
										List<String> result) {

								}

								@Override
								public void onGetBuildListFinish(
										List<BuildInfo> result) {

								}

								@Override
								public void onGetBuildDetailFinish(
										BuildInfo result) {
									if (MainFragment.this.isDetached()) {
										return;
									}
									String tipStr = "发现您在室内，点击定位按钮进入";
									if (result != null) {
										tipStr = "发现您在" + result.getBuildName()
												+ "，点击定位按钮进入";
									}
									rootView.findViewById(R.id.tv_tip)
											.setVisibility(View.VISIBLE);
									((TextView) rootView
											.findViewById(R.id.tv_tip))
											.setText(tipStr);
								}

								@Override
								public void onGetNavigationFinish(RMRoute result) {

								}

								@Override
								public void onGetPoiSearchFinish(
										List<POI> result) {

								}

								@Override
								public void onGetPrivateBuildFinish(
										List<PrivateBuild> result) {

								}
							});
				}
			}
		}

	}

	private boolean isStartStatus = false;

	@Override
	public void onMapStatusChangeStart(MapStatus mapStatus) {
		if (isStartStatus
				&& mBaiDuMap.getLocationConfigeration().locationMode == MyLocationConfiguration.LocationMode.FOLLOWING) {
			setFollowing(false);
		}
		isStartStatus = true;
	}

	@Override
	public void onMapStatusChange(MapStatus mapStatus) {

	}

	@Override
	public void onMapStatusChangeFinish(MapStatus mapStatus) {
		isStartStatus = false;
	}
}
