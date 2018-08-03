package com.rtmap.indoor_switch.pages;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
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
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.rtm.common.model.BuildInfo;
import com.rtm.common.model.POI;
import com.rtm.common.model.RMLocation;
import com.rtm.frm.model.RMRoute;
import com.airport.test.R;
import com.rtmap.indoor_switch.RtmActivity;
import com.rtmap.indoor_switch.base.BaseActivity;
import com.rtmap.indoor_switch.base.BaseFragment;
import com.rtmap.indoor_switch.bean.PrivateBuild;
import com.rtmap.indoor_switch.layer.DrivingRouteOverlay;
import com.rtmap.indoor_switch.manager.AppContext;
import com.rtmap.indoor_switch.manager.RtMapLocManager;
import com.rtmap.indoor_switch.utils.BaiDuSdkUtil;
import com.rtmap.indoor_switch.utils.DTLog;
import com.rtmap.indoor_switch.utils.DialogUtil;
import com.rtmap.indoor_switch.utils.NetWorkUtil;
import com.rtmap.indoor_switch.utils.RMlbsUtils;
import com.rtmap.indoor_switch.utils.SharePrefUtil;
import com.rtmap.indoor_switch.utils.StringUtils;
import com.rtmap.indoor_switch.utils.ToastUtils;

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
		mBaiDuMapView = (MapView) rootView.findViewById(R.id.mv_baidu);
		imgPrivateBuild = (ImageView) rootView
				.findViewById(R.id.img_private_build);
		mBaiDuMap = mBaiDuMapView.getMap();
		mBaiDuMap.setMyLocationEnabled(true);

		mBaiDuMap.setOnMapStatusChangeListener(this);

		RtMapLocManager.instance().addReceiver(this);

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

//		mBaiDuMapView.removeViewAt(1);
//		int childCount = mBaiDuMapView.getChildCount();
//		for (int i = 0; i < childCount; ++i) {
//			if (mBaiDuMapView.getChildAt(i) instanceof ZoomControls) {
//				mBaiDuMapView.getChildAt(i).setVisibility(View.GONE);
//			}
//		}

//		new Handler().postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				UiSettings settings = mBaiDuMap.getUiSettings();
//				settings.setCompassEnabled(true);
//			}
//		}, 1000);
		((BaseActivity) getActivity()).openBdLoc(getActivity(), this);
		if (NetWorkUtil.isNetworkConnected(context)) {
			showLoading();
		} else {
			ToastUtils.shortToast(R.string.network_not_connected);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		RtMapLocManager.instance().startLoc();
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
		List<BuildInfo> buildInfos = new ArrayList<>();
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
		showLoading();
		if (getBuildDetailFinish) {
			getBuildDetailFinish = false;
			RMlbsUtils.getInstance().getBuildDetail(buildInfo.getBuildId(),
					this);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_my_location: {
			if (myLocationData != null) {
				LatLng ll = new LatLng(myLocationData.latitude,
						myLocationData.longitude);
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
				mBaiDuMap.animateMapStatus(u);
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
	private BDLocation mBDLocation;

	@Override
	public void onReceiveLocation(BDLocation location) {
		// map view 销毁后不在处理新接收的位置
		if (location == null || mBaiDuMap == null)
			return;
		myLocationData = new MyLocationData.Builder()
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
		}
		String city = location.getCity();
		if (!TextUtils.isEmpty(city) && !getBuildFinish) {
			getBuildFinish = true;
			locCity = city = city.replace("市", "");
			mBDLocation = location;
			getBuildByCity(city.replace("市", ""));
		}
	}

	/***
	 * 根据城市名显示建筑
	 *
	 * @param city
	 */
	public void getBuildByCity(String city) {
		if (!StringUtils.isEmpty(city)) {
			AppContext.instance().mChooseCity = city;
			tvChangeCity.setText(city);
			showLoading();
			RMlbsUtils.getInstance().getBuildList(city, this);
		}
	}

	private void getCityList() {
		showLoading();
		RMlbsUtils.getInstance().getCityList(this);
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
	private BuildInfo mBuild;

	@Override
	public void onGetBuildListFinish(List<BuildInfo> result) {
		dismissLoading();
		getBuildFinish = true;
		mBaiDuMap.clear();
		if (result == null || result.size() == 0) {
			return;
		}
		BaiDuSdkUtil.changeToBdLatLngBatch(result);
		BitmapDescriptor markerIcon = BitmapDescriptorFactory
				.fromResource(R.drawable.icon_gcoding_red);
		for (BuildInfo build : result) {
			mBuild = build;
			OverlayOptions overlayOptions;
			LatLng buildLatLag = new LatLng(build.getLat(), build.getLong());
			overlayOptions = new MarkerOptions().position(buildLatLag)
					.icon(markerIcon).zIndex(9).draggable(true);
			Marker marker = (Marker) mBaiDuMap.addOverlay(overlayOptions);
			Bundle data = new Bundle();
			data.putSerializable("data", build);
			marker.setTitle(build.getBuildName());
			marker.setExtraInfo(data);
			if (build.getBuildId().equals(RMlbsUtils.AIR_BUILD_ID)) {
				final RoutePlanSearch mSearch = RoutePlanSearch.newInstance();
				// 第二步，创建驾车线路规划检索监听者；
				OnGetRoutePlanResultListener listener = new OnGetRoutePlanResultListener() {
					public void onGetWalkingRouteResult(
							WalkingRouteResult result) {
						// 获取步行线路规划结果
					}

					public void onGetTransitRouteResult(
							TransitRouteResult result) {
						// 获取公交换乘路径规划结果
					}

					public void onGetDrivingRouteResult(
							DrivingRouteResult result) {
						// 获取驾车线路规划结果
						// 第六步，释放检索实例；
						if (result == null
								|| result.error != SearchResult.ERRORNO.NO_ERROR) {
							ToastUtils.shortToast("抱歉，未找到结果");
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
				PlanNode stNode = PlanNode.withLocation(new LatLng(mBDLocation.getLatitude(),mBDLocation.getLongitude()));
				PlanNode enNode = PlanNode.withLocation(new LatLng(mBuild.getLat(), mBuild.getLong()));
				// 第五步，发起驾车线路规划检索；
				mSearch.drivingSearch((new DrivingRoutePlanOption()).from(
						stNode).to(enNode));
			}
		}
	}

	private boolean getBuildDetailFinish = true;

	@Override
	public void onGetBuildDetailFinish(BuildInfo result) {
		getBuildDetailFinish = true;
		if (result != null) {
			BaiDuSdkUtil.hideMarkerDetail(vsMarkerDetail);
			dismissLoading();
			Bundle b = new Bundle();
			b.putSerializable("data", result);
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
		if (loading == null) {
			loading = DialogUtil.getLoadingDialog(getActivity(), false, null);
		}
		if (loading.isShowing()) {
			return;
		}
		loading.show();
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
		// tvChangeCity.setText(rmLocation.getInOutDoorFlg()+"");
		if (rmLocation.error == 0) {
//			if(RMlbsUtils.AIR_BUILD_ID.equals(rmLocation.getBuildID())){//先隐藏掉，机场测试时候用
				goToRtmActivity(mBuild);
				finishActivity();
//			}
//			if (isFollowing) {
//				if (getBuildDetailFinish) {
//					getBuildDetailFinish = false;
//					RMlbsUtils.getInstance().getBuildDetail(
//							rmLocation.getBuildID(), this);
//				}
//				rootView.findViewById(R.id.tv_tip).setVisibility(View.GONE);
//			} else {
//				if (!isTipShowed) {
//					isTipShowed = true;
//					RMlbsUtils.getInstance().getBuildDetail(
//							rmLocation.getBuildID(),
//							new RMlbsUtils.OnRmGetFinishListener() {
//								@Override
//								public void onGetCityListFinish(
//										List<String> result) {
//
//								}
//
//								@Override
//								public void onGetBuildListFinish(
//										List<BuildInfo> result) {
//
//								}
//
//								@Override
//								public void onGetBuildDetailFinish(
//										BuildInfo result) {
//									if (MainFragment.this.isDetached()) {
//										return;
//									}
//									String tipStr = "发现您在室内，点击定位按钮进入";
//									if (result != null) {
//										tipStr = "发现您在" + result.getBuildName()
//												+ "，点击定位按钮进入";
//									}
//									rootView.findViewById(R.id.tv_tip)
//											.setVisibility(View.VISIBLE);
//									((TextView) rootView
//											.findViewById(R.id.tv_tip))
//											.setText(tipStr);
//								}
//
//								@Override
//								public void onGetNavigationFinish(RMRoute result) {
//
//								}
//
//								@Override
//								public void onGetPoiSearchFinish(
//										List<POI> result) {
//
//								}
//
//								@Override
//								public void onGetPrivateBuildFinish(
//										List<PrivateBuild> result) {
//
//								}
//							});
//				}
//			}
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
