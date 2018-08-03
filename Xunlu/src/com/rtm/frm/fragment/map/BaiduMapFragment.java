package com.rtm.frm.fragment.map;

import java.util.List;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ZoomControls;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
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
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.DrivingRouteOverlay;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.rtm.frm.R;
import com.rtm.frm.XunluApplication;
import com.rtm.frm.database.DBOperation;
import com.rtm.frm.fragment.BaseFragment;
import com.rtm.frm.fragment.controller.BaiduMapManager;
import com.rtm.frm.fragment.controller.MyFragmentManager;
import com.rtm.frm.fragment.map.BaiduMapOrientationListener.OnOrientationListener;
import com.rtm.frm.model.Build;
import com.rtm.frm.newframe.NewFrameActivity;
import com.rtm.frm.newui.TestSearchDialogFragment;
import com.rtm.frm.tab0.TestRtmapFragment;
import com.rtm.frm.utils.PreferencesUtil;
import com.rtm.frm.utils.ToastUtil;
import com.rtm.frm.utils.XunluUtil;
import com.rtm.frm.view.NavTitle;
import com.umeng.analytics.MobclickAgent;

/**
 * @author liyan 20140819
 */
public class BaiduMapFragment extends BaseFragment implements
		OnMarkerClickListener, OnMapClickListener,
		OnGetRoutePlanResultListener, OnTouchListener,
		OnMapStatusChangeListener {
	public static boolean isShowing = false;
	private final String KEY_MARKER_INFO = "info";

	// private static final LatLng GEO_SHANGHAI = new LatLng(31.227, 121.481);

	private final double SHOW_BUILD_RADIUS = 5;// KM

	// private static BaiduMapFragment mBaiduMapFragment;

	private NavTitle mNavTitle;

	private MapView mBdMapView;
	private BaiduMap mBaiduMap;
	// private InfoWindow mInfoWindow;
	private Button mModeButton;
	private View poiDetailLay;
	private Marker mLastClickMarker;

	private boolean isSearchPoiToChangeMapCenter = false;// 是否通过查找poi而改变中心点

	// 定位相关
	private LocationClient mLocClient;
	private MyLocationListenner mLocationListenner = new MyLocationListenner();
	public LocationMode mCurrentMode;
	private BitmapDescriptor mCurrentMarker;
	boolean isFirstLoc = true;// 是否首次定位
	// 方向传感器的监听器
	private BaiduMapOrientationListener mBaiduMapOrientationListener;
	// 方向传感器X方向的值
	private float mXDirection = 0;
	// 搜索相关
	private RoutePlanSearch mSearch = null; // 搜索模块，也可去掉地图模块独立使用 adb monkey -p
											// com.rtm.frm -v
											// 10000>D:/monkeyloginfo_1000000.txt
	private DrivingRouteOverlay mDrivingRouteOverlay = null;
	private TestRtmapFragment mRtmFragment;

	public Build mInitFromBuild;
	private String mInitFromFloor;
	private String mLocationCityName = "";
	private static String BDDefaultLocateCity = "北京";

	private List<Build> mCurrentCityBuilds;

	// public static BaiduMapFragment getInstance() {
	// if (mBaiduMapFragment == null) {
	// mBaiduMapFragment = new BaiduMapFragment();
	// }
	// return mBaiduMapFragment;
	// }

	private final double USER_STEP_MOVE = 0.01;// KM

	private final double BUILD_RADIUS = 0.05;// KM

	public boolean isInitFinish = false;

	public BaiduMapFragment() {
	}

	public BaiduMapFragment(String buildId, String floor) {
		mInitFromBuild = DBOperation.getInstance().queryBuildById(buildId);
		mInitFromFloor = floor;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View contentView = inflater.inflate(R.layout.fragment_baidu_map,
				container, false);

		isShowing = true;
		initViews(contentView);
		init();

		return contentView;
	}

	private void init() {
		mLocationCityName = PreferencesUtil.getString("BDLocateCity",
				BDDefaultLocateCity);

		/****** add by kunge.hu *********/
		if (mInitFromBuild != null) {
			if (mInitFromBuild.getCityName().equals(
					getResources().getString(R.string.db_collect_build)
							.toString())) {
				if (XunluUtil.isEmpty(mLocationCityName)) {
					showBuildOnMapByCity(mInitFromBuild.getCityName());
				} else {
					showBuildOnMapByCity(mLocationCityName);
				}
			}
			// 地图中心店移动至该位置
			LatLng latLng = new LatLng(Float.valueOf(mInitFromBuild.lat),// 原始Google坐标
					Float.valueOf(mInitFromBuild.lng));
			MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(latLng);
			mBaiduMap.animateMapStatus(u);
			// 显示麻点
			mLocationCityName = mInitFromBuild.cityName;
			showBuildOnMapByCity(mLocationCityName.replace("市", ""));
			mNavTitle.setTitleText(mLocationCityName);

			showBuildBySearch(mInitFromBuild);
		}
		/****** add by kunge.hu *********/
	}

	private void initViews(View view) {
		mNavTitle = (NavTitle) view.findViewById(R.id.nav_title);
		mNavTitle.unRegisterReceiver();
		mNavTitle.setLeftOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MobclickAgent.onEvent(getActivity(),
						"event_click_outdoor_search");
				if (mDrivingRouteOverlay != null) {
					mDrivingRouteOverlay.removeFromMap();
					mDrivingRouteOverlay = null;
					hideWindowInfoDetail();
				}
				// 显示室外地图搜索页
				TestSearchDialogFragment searchDialogFragment = new TestSearchDialogFragment();
				MyFragmentManager.getInstance().addFragment(
						NewFrameActivity.ID_ALL, searchDialogFragment,
						MyFragmentManager.PRCOCESS_BAIDU_SEARCH,
						MyFragmentManager.FRAGMENT_BAIDU_SEARCH);
			}
		});
		mBdMapView = (MapView) view.findViewById(R.id.bmapView);
		mBaiduMap = mBdMapView.getMap();
		mModeButton = (Button) view.findViewById(R.id.button_mode);
		mCurrentMode = LocationMode.NORMAL;
		poiDetailLay = view.findViewById(R.id.baidu_map_poi_detail_lay);
		poiDetailLay.setOnTouchListener(this);
		mModeButton.setBackgroundResource(R.drawable.local_my);
		mModeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MobclickAgent.onEvent(getActivity(),
						"event_click_outdoor_locate");
				mNavTitle.setTitleText(mLocationCityName);
				setFollowModel(true);
				for (int i = 0; mCurrentCityBuilds != null
						&& i < mCurrentCityBuilds.size(); ++i) {
					Build build = mCurrentCityBuilds.get(i);
					if (currentBdLocation != null) {
						double dis = XunluUtil.distanceByLatLng(
								currentBdLocation.getLatitude(),
								currentBdLocation.getLongitude(),
								Double.valueOf(build.getLat()),
								Double.valueOf(build.getLng()));
						if (dis <= BUILD_RADIUS) {
							TestRtmapFragment fragment = NewFrameActivity
									.getInstance().getTab0();
							if (!fragment.mMapShowBuildId.equals(build.getId())) {
								// 开启定位
								((NewFrameActivity) getActivity())
										.rtmapLocateStart();
							}
						}
					}
				}

				// 设置地图缩放等级
				MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(14.0f);
				mBaiduMap.setMapStatus(msu);
			}
		});

		// 开启定位图层
		mBaiduMap.setMyLocationEnabled(true);
		mBaiduMap.getUiSettings().setCompassEnabled(true);
		// 定位初始化
		mLocClient = new LocationClient(getActivity());
		mLocClient.registerLocationListener(mLocationListenner);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		// option.setAddrType("all");
		option.setIsNeedAddress(true);// 返回的定位结果包含地址信息
		option.setNeedDeviceDirect(true);// 返回的定位结果包含手机机头的方向
		option.setCoorType("bd09ll"); // 设置坐标类型
		option.setScanSpan(1000);
		mLocClient.setLocOption(option);
		mLocClient.start();
		// 方向监听初始化
		initOritationListener();
		// 初始化搜索模块，注册事件监听
		mSearch = RoutePlanSearch.newInstance();
		mSearch.setOnGetRoutePlanResultListener(this);

		// 监听地图状态变化
		mBaiduMap.setOnMapStatusChangeListener(this);
		// 监听地图点击
		mBaiduMap.setOnMapClickListener(this);
		// 隐藏缩放控件
		int childCount = mBdMapView.getChildCount();
		View zoom = null;
		for (int i = 0; i < childCount; i++) {
			View child = mBdMapView.getChildAt(i);
			if (child instanceof ZoomControls) {
				zoom = child;
				break;
			}
		}
		zoom.setVisibility(View.GONE);
		if (NewFrameActivity.getInstance().mCurrentBdLocation != null) {
			double lat = NewFrameActivity.getInstance().mCurrentBdLocation
					.getLatitude();
			double lng = NewFrameActivity.getInstance().mCurrentBdLocation
					.getLongitude();
			LatLng defaultPointLatLng = new LatLng(lat, lng);
			MapStatusUpdate u = MapStatusUpdateFactory
					.newLatLng(defaultPointLatLng);
			mBaiduMap.animateMapStatus(u);
		}

		// 初始化缩放比例
		MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(12.0f);
		mBaiduMap.setMapStatus(msu);

		// mBdMapView.setOnTouchListener(new OnTouchListener() {
		//
		// @Override
		// public boolean onTouch(View arg0, MotionEvent arg1) {
		// if (mCurrentMode == LocationMode.FOLLOWING ) {
		// // double dis = XunluUtil.distanceByLatLng(lastMapCenterLat,
		// lastMapCenterLng, mapStatus.target.latitude,
		// mapStatus.target.longitude);
		// // Log.e("dis", "dis"+dis+"");
		// // if(dis > 1 && !isGoLocation) {
		// mModeButton.setBackgroundResource(R.drawable.local_my);
		// mCurrentMode = LocationMode.NORMAL;
		// mBaiduMap.setMyLocationConfigeration(new
		// MyLocationConfiguration(mCurrentMode, true, mCurrentMarker));
		// // lastMapCenterLat = mapStatus.target.latitude;
		// // lastMapCenterLng = mapStatus.target.longitude;
		// // }
		//
		// }
		// return false;
		// }
		// });
	}

	/**
	 * 将建筑物列表显示到百度地图上
	 * 
	 * @param builds
	 */
	public void showBuildOnMapByCity(String cityName) {
		mCurrentCityBuilds = BaiduMapManager.getInstance().getBuildsByCityName(
				cityName);
		mBaiduMap.setOnMarkerClickListener(this);
		BitmapDescriptor bd = BitmapDescriptorFactory
				.fromResource(R.drawable.icon_gcoding);

		for (Build build : mCurrentCityBuilds) {

			LatLng latLng = new LatLng(Float.valueOf(build.lat),
					Float.valueOf(build.lng));

			// 2创建overlayOption
			OverlayOptions overlayOption = new MarkerOptions().position(latLng)
					.icon(bd).zIndex(9).draggable(true);
			// 3将overlay添加到mBaiduMap
			Marker marker = (Marker) (mBaiduMap.addOverlay(overlayOption));
			marker.setTitle(build.getName());
			Bundle data = new Bundle();
			data.putSerializable(KEY_MARKER_INFO, build);
			marker.setExtraInfo(data);
		}
		bd.recycle();
		isInitFinish = true;
	}

	/**
	 * @author LiYan
	 * @date 2014-9-10 下午8:33:26
	 * @explain 根据指定位置及半径筛选建筑
	 * @return void
	 * @param latLng
	 * @param radius
	 */
	public void showBuildOnMapByLatLng(LatLng latLng, double radius) {
		if (mDrivingRouteOverlay == null) {// 如果路线规划
			mBaiduMap.clear();
		} else {
			return;
		}
		List<Build> builds = BaiduMapManager.getInstance().getBuildsByLngLat(
				latLng, radius);
		mBaiduMap.setOnMarkerClickListener(this);
		BitmapDescriptor bd = BitmapDescriptorFactory
				.fromResource(R.drawable.icon_gcoding);

		for (Build build : builds) {
			OverlayOptions overlayOption;
			LatLng buildLatLng = new LatLng(Float.valueOf(build.lat),
					Float.valueOf(build.lng));
			if (mLastClickMarker == null
					|| !build.getName().equals(
							mLastClickMarker.getTitle().toString())) {// 判断该建筑是否被点击过
				// 创建overlayOption poi点设置为正常效果
				overlayOption = new MarkerOptions().position(buildLatLng)
						.icon(bd).zIndex(9).draggable(true);
				// 3将overlay添加到mBaiduMap
				Marker marker = (Marker) (mBaiduMap.addOverlay(overlayOption));
				marker.setTitle(build.getName());
				Bundle data = new Bundle();
				data.putSerializable(KEY_MARKER_INFO, build);
				marker.setExtraInfo(data);
			}
		}
		addLastmarker();
		bd.recycle();

	}

	/**
	 * @author LiYan
	 * @date 2014-9-13 下午3:05:43
	 * @explain 添加上次点击的Marker
	 * @return void
	 */
	private void addLastmarker() {
		if (mLastClickMarker != null) {
			BitmapDescriptor bdPress = BitmapDescriptorFactory
					.fromResource(R.drawable.icon_gcoding_press);
			OverlayOptions overlayOption;
			// 创建overlayOption poi点设置为点击效果
			overlayOption = new MarkerOptions()
					.position(mLastClickMarker.getPosition()).icon(bdPress)
					.zIndex(9).draggable(true);
			// 3将overlay添加到mBaiduMap
			Marker marker = (Marker) (mBaiduMap.addOverlay(overlayOption));
			marker.setTitle(mLastClickMarker.getTitle().toString());
			Bundle data = new Bundle();
			data.putSerializable(KEY_MARKER_INFO, mLastClickMarker
					.getExtraInfo().getSerializable(KEY_MARKER_INFO));
			marker.setExtraInfo(data);
			// 因为lastMarker所在的overlay已经被清掉，无法显示。需要将新添加poi的Marker赋值给lastMarker，
			mLastClickMarker = marker;
			bdPress.recycle();
		}
	}

	/**
	 * @author LiYan
	 * @date 2014-9-7 下午7:18:50
	 * @explain 显示指定的建筑点
	 * @return void
	 * @param build
	 */
	public void showBuildBySearch(Build build) {
		setFollowModel(false);
		mNavTitle.setTitleText(build.getCityName());

		if (mDrivingRouteOverlay != null) {
			mDrivingRouteOverlay.removeFromMap();
			mDrivingRouteOverlay = null;
			hideWindowInfoDetail();
		}

		isSearchPoiToChangeMapCenter = true;
		mBaiduMap.setOnMarkerClickListener(this);
		BitmapDescriptor bd = BitmapDescriptorFactory
				.fromResource(R.drawable.icon_gcoding_press);
		LatLng latLng = new LatLng(Float.valueOf(build.lat),// 原始Google坐标
				Float.valueOf(build.lng));
		// 2创建overlayOption
		OverlayOptions overlayOption = new MarkerOptions().position(latLng)
				.icon(bd).zIndex(10).draggable(true);
		// 3将overlay添加到mBaiduMap
		final Marker marker = (Marker) (mBaiduMap.addOverlay(overlayOption));
		marker.setTitle(build.getName());
		Bundle data = new Bundle();
		data.putSerializable(KEY_MARKER_INFO, build);
		marker.setExtraInfo(data);
		bd.recycle();

		exchangeLastClickMarker(marker);// 恢复先前点击的marker图标

		// 地图中心店移动至该位置
		MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(latLng);
		mBaiduMap.animateMapStatus(u);

		try {
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					// 地图缩放级别
					try {
						MapStatusUpdate u1 = MapStatusUpdateFactory
								.zoomTo(12.0f);
						mBaiduMap.animateMapStatus(u1);
						showWindowInfoDetail(marker);
					} catch (Exception e) {
						e.printStackTrace();
					}
					// new Handler().postDelayed(new Runnable() {
					//
					// @Override
					// public void run() {
					// TODO Auto-generated method stub
					// 打开气泡
					// Button button = new Button(XunluApplication.mApp);
					// button.setBackgroundResource(R.drawable.popup);
					// button.setText(marker.getTitle());
					// button.setTextColor(Color.BLACK);
					//
					// Point p =
					// mBaiduMap.getProjection().toScreenLocation(marker.getPosition());
					// p.y -= 47;
					// LatLng llInfo =
					// mBaiduMap.getProjection().fromScreenLocation(p);
					// mInfoWindow = new InfoWindow(button, llInfo, null);
					// mBaiduMap.showInfoWindow(mInfoWindow);
					// }
					// }, 300);
				}
			}, 300);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * @author LiYan
	 * @date 2014-9-7 下午8:48:18
	 * @explain 将上一次点击的marker背景恢复默认红色
	 * @return void
	 */
	private void exchangeLastClickMarker(Marker clickMarker) {
		BitmapDescriptor bd = BitmapDescriptorFactory
				.fromResource(R.drawable.icon_gcoding);
		if (mLastClickMarker != null
				&& !mLastClickMarker.getTitle().toString()
						.equals(clickMarker.getTitle().toString())) {
			mLastClickMarker.setIcon(bd);
		}
		mLastClickMarker = clickMarker;
		bd.recycle();
	}

	@Override
	public void onResume() {
		mBdMapView.onResume();
		// 开启方向传感器
		mBaiduMapOrientationListener.start();
		if (!mLocClient.isStarted()) {
			mLocClient.start();
		}
		super.onResume();
	}

	@Override
	public void onPause() {
		// 关闭方向传感器
		mBaiduMapOrientationListener.stop();
		mBdMapView.onPause();
		if (mLocClient.isStarted()) {
			mLocClient.stop();
		}
		super.onPause();
	}

	@Override
	public void onDestroy() {
		isShowing = false;
		super.onDestroy();
	}

	@Override
	public void onDestroyView() {
		if (mRtmFragment == null) {
			mRtmFragment = NewFrameActivity.getInstance().getTab0();
		}
		mRtmFragment.showRtmapView();
		// 退出时销毁定位
		if (mLocClient.isStarted()) {
			mLocClient.stop();
		}
		// 关闭定位图层
		mBaiduMap.setMyLocationEnabled(false);
		mSearch.destroy();
		mBdMapView.onDestroy();
		super.onDestroyView();
	}

	@SuppressLint("InflateParams")
	@Override
	public boolean onMarkerClick(final Marker marker) {

		if (mDrivingRouteOverlay != null) {
			return false;
		}
		// 设置新点击mark图标
		BitmapDescriptor bd = BitmapDescriptorFactory
				.fromResource(R.drawable.icon_gcoding_press);
		marker.setIcon(bd);
		bd.recycle();
		exchangeLastClickMarker(marker);

		// 显示气泡代码
		// Button button = new Button(XunluApplication.mApp);
		// button.setBackgroundResource(R.drawable.popup);
		// button.setText(marker.getTitle());
		// button.setTextColor(Color.BLACK);
		//
		//
		// final LatLng ll = marker.getPosition();
		// Point p = mBaiduMap.getProjection().toScreenLocation(ll);
		// p.y -= 47;
		// LatLng llInfo = mBaiduMap.getProjection().fromScreenLocation(p);
		// mInfoWindow = new InfoWindow(button, llInfo, null);
		// mBaiduMap.showInfoWindow(mInfoWindow);
		showWindowInfoDetail(marker);
		return true;
	}

	/**
	 * @explain 打开指定build对象建筑物
	 * @param build
	 */
	public void openRtmapBuild(Build build) {
		if (build == null) {
			Log.e("BaiduFragment", "openRtmapBuild build is null");
			return;
		}
		((NewFrameActivity) getActivity()).rtmapLocateStart();
		String defalutFloor = XunluUtil.getDefaultFloor(build.floors);
		// String defalutFloor = build.floors.split("_")[0];
		TestRtmapFragment rtmFragment = NewFrameActivity.getInstance()
				.getTab0();
		rtmFragment.closeNav();
		if (!build.id.equals(rtmFragment.mMapShowBuildId)) {
		}
		rtmFragment.switchBuild(build.name, build.id, defalutFloor);
		// FragmentTabItemManager0.getInstance().backTabFragment();
		rtmFragment.mLastFlag = 1;
		rtmFragment.showRtmapView();
		MyFragmentManager.getInstance().backFragment();
		// MyFragmentManager.getInstance().backFragmentUpFlag(MyFragmentManager.PROCESS_RT_MAP+"-"+MyFragmentManager.FRAGMENT_RT_MAP);
		// TODO 未来优化，百度地图fragment不销毁
		// rtmFragment.showRtmapView();
		// MyFragmentManager.getInstance().getTransaction().hide(this).commit();
	}

	private BDLocation currentBdLocation;

	/**
	 * 定位SDK监听函数
	 */
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(final BDLocation location) {
			currentBdLocation = location;
			// map view 销毁后不在处理新接收的位置
			if (location == null || mBdMapView == null)
				return;
			MyLocationData locData = new MyLocationData.Builder()
					.accuracy(location.getRadius())
					// 此处设置开发者获取到的方向信息，顺时针0-360
					.direction(mXDirection).latitude(location.getLatitude())
					.longitude(location.getLongitude()).build();
			mBaiduMap.setMyLocationData(locData);

			if (!XunluUtil
					.isEmpty(NewFrameActivity.getInstance().mCurrentGpsCity)
					&& isFirstLoc && mInitFromBuild == null) {
				isFirstLoc = false;
				mNavTitle
						.setTitleText(NewFrameActivity.getInstance().mCurrentGpsCity);
				showBuildOnMapByCity(NewFrameActivity.getInstance().mCurrentGpsCity);
			}
			// // if (XunluUtil.isEmpty(mLocationCityName) && location.getCity()
			// !=
			// // null) {
			// if (XunluUtil.isEmpty(mLocationCityName)) {
			// // mLocationCityName = location.getCity().replaceAll("市", "");
			// // mLocationCityName = "上海";
			// mLocationCityName =
			// PreferencesUtil.getString("BDLocateCity",BDDefaultLocateCity);
			//
			// // 2G网络问题,根据坐标点算出来最近的点城市名，然后显示出来
			// // mLocationCityName =
			// // getBaiduCityNameByXY(45.720394,126.62736);
			//
			// showBuildOnMapByCity(mLocationCityName);
			// }
			// if (isFirstLoc) {
			// isFirstLoc = false;
			// if (mInitFromBuild == null) {
			// final LatLng ll = new LatLng(location.getLatitude(),
			// location.getLongitude());
			// MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
			// mBaiduMap.animateMapStatus(u);
			// mBaiduMap.setMyLocationConfigeration(new
			// MyLocationConfiguration(mCurrentMode, true, mCurrentMarker));
			//
			// if (location.getCity() != null) {
			// showBuildOnMapByCity(location.getCity().replace("市", ""));
			// mNavTitle.setTitleText(mLocationCityName);
			//
			// // showBuildOnMapByLatLng(ll, SHOW_BUILD_RADIUS);
			// }
			// } else {
			// if
			// (mInitFromBuild.getCityName().equals(getResources().getString(R.string.db_collect_build).toString()))
			// {
			// if (XunluUtil.isEmpty(mLocationCityName)) {
			// showBuildOnMapByCity(mInitFromBuild.getCityName());
			// } else {
			// showBuildOnMapByCity(mLocationCityName);
			// }
			// }
			// showBuildBySearch(mInitFromBuild);
			// }
			// }

		}

		public void onReceivePoi(BDLocation poiLocation) {
		}
	}

	// private String getBaiduCityNameByXY(double lat_b, double lng_b) {
	// List<Build> bList = DBOperation.getInstance().queryBuildAll();
	//
	// for (int i = 0; i < bList.size(); i++) {
	// float lat_a = Float.valueOf(bList.get(i).getLat());
	// float lng_a = Float.valueOf(bList.get(i).getLng());
	//
	// int d = (int)XunluUtil.distanceByLatLng(lat_a, lng_a, lat_b,lng_b);
	// bList.get(i).setDis(d);
	// }
	//
	// for (int i = 0; i < 5; i++) {
	// Log.d("kunge.hu", "排序之前" + bList.get(i).getName() +
	// bList.get(i).getDis());
	// }
	//
	// // 按距离从大到小排序
	// Collections.sort(bList, new Comparator<Build>() {
	// @Override
	// public int compare(Build p1, Build p2) {
	// return Integer.valueOf(p1.getDis()).compareTo(p2.getDis());
	// }
	// });
	//
	// for (int i = 0; i < 5; i++) {
	// Log.d("kunge.hu", "排序之后" + bList.get(i).getName() +
	// bList.get(i).getDis());
	// }
	//
	// Log.d("kunge.hu", "CityName:" + bList.get(0).getCityName());
	//
	// return bList.get(0).getCityName();
	// }

	/**
	 * 初始化方向传感器
	 */
	private void initOritationListener() {
		mBaiduMapOrientationListener = new BaiduMapOrientationListener(
				XunluApplication.mApp);
		mBaiduMapOrientationListener
				.setOnOrientationListener(new OnOrientationListener() {
					@Override
					public void onOrientationChanged(float x) {
						mXDirection = ((int) (x * 100)) / 100.0f;
					}
				});
	}

	@Override
	public void onMapClick(LatLng arg0) {
		mBaiduMap.hideInfoWindow();
		if (mDrivingRouteOverlay == null) {
			hideWindowInfoDetail();
		}
	}

	@Override
	public boolean onMapPoiClick(MapPoi arg0) {
		return false;
	}

	/**
	 * @explain 显示poi详情
	 * 
	 * @param marker
	 */
	public void showWindowInfoDetail(Marker marker) {
		MobclickAgent.onEvent(getActivity(), "event_click_outdoor_item");
		poiDetailLay.setVisibility(View.VISIBLE);
		final Build clickBuild = (Build) marker.getExtraInfo().get(
				KEY_MARKER_INFO);
		mNavTitle.setTitleText(clickBuild.getCityName());
		TextView buildName = (TextView) poiDetailLay
				.findViewById(R.id.baidu_map_build_name);
		TextView intoRoom = (TextView) poiDetailLay
				.findViewById(R.id.baidu_map_into_room);
		TextView goHere = (TextView) poiDetailLay
				.findViewById(R.id.baidu_map_go_here);
		goHere.setText(R.string.route_line_plan);
		buildName.setText(clickBuild.getName());
		intoRoom.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MobclickAgent.onEvent(getActivity(),
						"event_click_outdoor_item_indoor");
				openRtmapBuild(clickBuild);
			}
		});
		goHere.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MobclickAgent.onEvent(getActivity(),
						"event_click_outdoor_item_planroute");
				// 路线规划
				if (mDrivingRouteOverlay == null) {
					if (currentBdLocation != null) {
						LatLng latLng = new LatLng(Float
								.valueOf(clickBuild.lat), Float
								.valueOf(clickBuild.lng));
						PlanNode stNode = PlanNode.withLocation(new LatLng(
								currentBdLocation.getLatitude(),
								currentBdLocation.getLongitude()));
						PlanNode enNode = PlanNode.withLocation(latLng);
						mSearch.drivingSearch((new DrivingRoutePlanOption())
								.from(stNode).to(enNode));
						((TextView) v).setText(R.string.route_line_plan_close);
					}
				} else {
					mDrivingRouteOverlay.removeFromMap();
					mDrivingRouteOverlay = null;
					((TextView) v).setText(R.string.route_line_plan);
					hideWindowInfoDetail();
				}
			}
		});
		buildName.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				LatLng latLng = new LatLng(Float.valueOf(clickBuild.lat), Float
						.valueOf(clickBuild.lng));

				// 地图中心店移动至该位置
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(latLng);
				mBaiduMap.animateMapStatus(u);
			}
		});
		poiDetailLay.setTag(marker);
	}

	/**
	 * @explain 隐藏poi详情lay
	 */
	public void hideWindowInfoDetail() {
		poiDetailLay.setVisibility(View.GONE);
		if (mDrivingRouteOverlay != null) {
			mDrivingRouteOverlay.removeFromMap();
			mDrivingRouteOverlay = null;
		}
		// Marker marker = (Marker) poiDetailLay.getTag();
		// marker.set
	}

	@Override
	public void onGetDrivingRouteResult(DrivingRouteResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			ToastUtil.showToast("抱歉，未找到结果", true);
		}
		if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
			// 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
			// result.getSuggestAddrInfo()
			return;
		}
		if (result.error == SearchResult.ERRORNO.NO_ERROR) {
			// 清除所有点
			// mBaiduMap.clear();

			// //重新添加上次点击的poi
			// BitmapDescriptor bdPress = BitmapDescriptorFactory
			// .fromResource(R.drawable.icon_gcoding_press);
			// // 创建overlayOption poi点设置为点击效果
			// OverlayOptions overlayOption = new
			// MarkerOptions().position(mLastClickMarker.getPosition())
			// .icon(bdPress).zIndex(9).draggable(true);
			// // 3将overlay添加到mBaiduMap
			// Marker marker = (Marker) (mBaiduMap.addOverlay(overlayOption));
			// marker.setTitle(mLastClickMarker.getTitle().toString());
			// Bundle data = new Bundle();
			// data.putSerializable(KEY_MARKER_INFO,mLastClickMarker.getExtraInfo().getSerializable(KEY_MARKER_INFO));
			// marker.setExtraInfo(data);
			// 因为lastMarker所在的overlay已经被清掉，无法显示。需要将新添加poi的Marker赋值给lastMarker，
			// mLastClickMarker = marker;

			// 显示路线
			mDrivingRouteOverlay = new MyDrivingRouteOverlay(mBaiduMap);
			// mBaiduMap.setOnMarkerClickListener(mDrivingRouteOverlay);
			mDrivingRouteOverlay.setData(result.getRouteLines().get(0));
			mDrivingRouteOverlay.addToMap();
			mDrivingRouteOverlay.zoomToSpan();
		}
	}

	@Override
	public void onGetTransitRouteResult(TransitRouteResult arg0) {
		// 公交路线返回

	}

	@Override
	public void onGetWalkingRouteResult(WalkingRouteResult arg0) {
		// 不行路线返回

	}

	// 定制RouteOverly
	private class MyDrivingRouteOverlay extends DrivingRouteOverlay {

		public MyDrivingRouteOverlay(BaiduMap baiduMap) {
			super(baiduMap);
		}

		@Override
		public BitmapDescriptor getStartMarker() {
			return BitmapDescriptorFactory.fromResource(R.drawable.icon_null);
		}

		@Override
		public BitmapDescriptor getTerminalMarker() {
			return BitmapDescriptorFactory.fromResource(R.drawable.icon_null);
		}
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (v.getId()) {
		case R.id.baidu_map_poi_detail_lay:
			return true;
		}
		return false;
	}

	@Override
	public void onMapStatusChange(MapStatus mapStatus) {
		Log.e("mapstate", "change:" + mapStatus.target.latitude);

	}

	private double lastMapCenterLat = -1;
	private double lastMapCenterLng = -1;
	private boolean isStartStatus = false;

	@Override
	public void onMapStatusChangeFinish(MapStatus mapStatus) {
		Log.e("mapstate", "finish:" + mapStatus.target.latitude);

		if (mInitFromBuild != null) {
			// showBuildOnMapByCity(cityName);
		}
		isStartStatus = false;
	}

	public boolean isGoLocation = false;
	int i = 0;

	@Override
	public void onMapStatusChangeStart(MapStatus arg0) {
		Log.e("mapstate", "start:" + arg0.target.latitude);
		if (isStartStatus && mCurrentMode == LocationMode.FOLLOWING) {
			setFollowModel(false);
		}
		isStartStatus = true;
	}

	/**
	 * @explain 是否设置为跟随模式
	 * @param isFollow
	 */
	public void setFollowModel(boolean isFollow) {
		if (isFollow) {
			isGoLocation = true;
			mModeButton.setBackgroundResource(R.drawable.local_flow);
			mCurrentMode = LocationMode.FOLLOWING;
			mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
					mCurrentMode, true, mCurrentMarker));
			hideWindowInfoDetail();
		} else {
			mModeButton.setBackgroundResource(R.drawable.local_my);
			mCurrentMode = LocationMode.NORMAL;
			mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
					mCurrentMode, true, mCurrentMarker));
		}
	}
}