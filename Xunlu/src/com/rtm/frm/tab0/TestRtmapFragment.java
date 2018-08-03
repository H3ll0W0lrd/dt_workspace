package com.rtm.frm.tab0;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.rtm.common.model.POI;
import com.rtm.common.model.RMLocation;
import com.rtm.common.utils.Constants;
import com.rtm.common.utils.RMLog;
import com.rtm.frm.R;
import com.rtm.frm.XunluApplication;
import com.rtm.frm.AR.ARTestDialogFragment;
import com.rtm.frm.AR.ARTestManager;
import com.rtm.frm.AR.arPoiItem;
import com.rtm.frm.arar.ARGuideActivity;
import com.rtm.frm.arar.ARShowActivity;
import com.rtm.frm.database.DBOperation;
import com.rtm.frm.dialogfragment.LoadingFragment;
import com.rtm.frm.drawmap.DrawMap;
import com.rtm.frm.fragment.BaseFragment;
import com.rtm.frm.fragment.controller.FindManager;
import com.rtm.frm.fragment.controller.MyFragmentManager;
import com.rtm.frm.fragment.find.FindFragment;
import com.rtm.frm.fragment.map.BaiduMapFragment;
import com.rtm.frm.fragment.map.FloorChangeFragment;
import com.rtm.frm.fragment.map.PoiDetailFragment;
import com.rtm.frm.fragment.map.RtmapSearchFragment;
import com.rtm.frm.map.CompassLayer;
import com.rtm.frm.map.MapView;
import com.rtm.frm.map.POILayer;
import com.rtm.frm.map.RouteLayer;
import com.rtm.frm.map.TapPOILayer;
import com.rtm.frm.map.TapPOILayer.OnPOITappedListener;
import com.rtm.frm.map.XunluMap;
import com.rtm.frm.model.Build;
import com.rtm.frm.model.FavorablePoiDbModel;
import com.rtm.frm.model.MyLocation;
import com.rtm.frm.model.NavigatePoint;
import com.rtm.frm.model.POITargetInfo;
import com.rtm.frm.model.PushPoi;
import com.rtm.frm.model.RMRoute;
import com.rtm.frm.newframe.NewFrameActivity;
import com.rtm.frm.newui.NavPagerFragment;
import com.rtm.frm.stack.BackStackManager.OnBackStackChangedListener;
import com.rtm.frm.utils.ConstantsUtil;
import com.rtm.frm.utils.FavorablePoiParseUtil;
import com.rtm.frm.utils.Handlerlist;
import com.rtm.frm.utils.PreferencesUtil;
import com.rtm.frm.utils.ToastUtil;
import com.rtm.frm.utils.XunluUtil;
import com.rtm.frm.view.NavTitle;
import com.rtm.frm.vmap.Shape;
import com.rtm.location.LocationApp;
import com.rtm.location.utils.RMLocationListener;
import com.umeng.analytics.MobclickAgent;

@SuppressLint("InflateParams")
public class TestRtmapFragment extends BaseFragment implements OnClickListener,
		OnBackStackChangedListener, RMLocationListener {

	public static String PUSH_TRMAP_ACTION = "push_rtmap_action";

	public static String KEY_PUSH_MSG = "key_push_msg";

	// loading
	private LoadingFragment mLoadingFragment;

	// 地图
	public MapView mMapView;
	public TapPOILayer mTapPOILayer;// 点击图层
	public POILayer mPoiLayer;
	private CompassLayer mCompassLayer;// 指南针
	public RouteLayer mRouteLayer;// 导航

	// 地图控制
	private ImageButton mButtonLocate;// 当前位置定位按钮
	private Button mChangeBuildButton;
	private Button mButtonArguide;// ar按钮
	private ImageButton mButtonZoomUp, mButtonZoomDown;
	private TextView mStatus;

	// 定位相关
	public RMLocation mCurrentLocation;
	public boolean mLocationIsRun = false;
	public String mCurrentFloor;
	public String mCurrentBuildId;
	public String mCurrentBuildName;
	public String mMapShowBuildId = "";
	public String mMapShowBuildName;
	public String mMapShowFloor;
	private String mMapShowCityName;
	public boolean followModel = false;// 是否为跟随模式

	// 定时器相关，连续5秒没有定位信息，提示定位失败。注：每定位成功timerCount都会重置为0
	// listener

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
					Toast.makeText(getActivity(), "地图文件检验结果失败",
							Toast.LENGTH_LONG).show();
				} else if (progress == Constants.MAP_Down_Success) {
					Log.e("rtmap", "地图下载成功");
					Toast.makeText(getActivity(), "地图下载成功", Toast.LENGTH_LONG)
							.show();
				} else if (progress == Constants.MAP_Down_Fail) {
					Log.e("rtmap", "地图下载失败");
					Toast.makeText(getActivity(), "地图下载失败", Toast.LENGTH_LONG)
							.show();
				} else if (progress == Constants.MAP_Update_Success) {
					Log.e("rtmap", "地图更新成功");
					Toast.makeText(getActivity(), "地图更新成功", Toast.LENGTH_LONG)
							.show();
				} else if (progress == Constants.MAP_Update_Fail) {
					Log.e("rtmap", "地图更新失败");
					Toast.makeText(getActivity(), "地图更新失败", Toast.LENGTH_LONG)
							.show();
				} else if (progress == Constants.MAP_Down_Fail) {
					Log.e("rtmap", "地图下载失败");
					Toast.makeText(getActivity(), "地图下载失败", Toast.LENGTH_LONG)
							.show();
				} else if (progress == Constants.MAP_LOAD_END) {
					Log.e("rtmap", "地图加载完成");
				} else if (progress == Constants.MAP_LICENSE) {
					Log.e("rtmap", "Liscense校验结果：" + (String) msg.obj);
				}
				break;
			case RouteLayer.NAVIGATE:
				navPagerFragment.navi(msg);
				break;
			}
		}
	};

	// push接收
	private RtmapPushReceiver mRtmapPushReceiver;

	private View mPushNotificationLay;

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		Handlerlist.getInstance().register(mHandler);
		View contentView = inflater.inflate(R.layout.fragment_test_map, null);
		return contentView;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		initViews(view);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		Handlerlist.getInstance().remove(mHandler);
	}

	private void initViews(View view) {
		mPushNotificationLay = view.findViewById(R.id.rtmap_push_msg_lay);// 推动通知条
		mPushNotificationLay.setOnClickListener(this);
		/* 地图初始化 */
		XunluMap.getInstance().init(XunluApplication.mApp);
		// 地图相关初始化
		mMapView = (MapView) view.findViewById(R.id.test_map_view);
		mMapView.setResetMapScale(true);
		mMapView.setDrawLogo(false);
		mMapView.setLocationIcon(R.drawable.default_location,
				R.drawable.default_location);
		/* layer */
		mTapPOILayer = new TapPOILayer(mMapView);
		mMapView.addMapLayer(mTapPOILayer);
		mPoiLayer = new POILayer(mMapView, BitmapFactory.decodeResource(
				getResources(), R.drawable.map_poi));
		mMapView.addMapLayer(mPoiLayer);
		mCompassLayer = new CompassLayer(mMapView,
				BitmapFactory.decodeResource(getResources(),
						R.drawable.compass_icon));
		mMapView.addMapLayer(mCompassLayer);
		mRouteLayer = new RouteLayer(mMapView);
		RouteLayer.DISTANCE_END = 15;
		mMapView.addMapLayer(mRouteLayer);

		mTapPOILayer.setOnPOITappedListener(new OnPOITappedListener() {

			@Override
			public Bitmap onPOITapped(POI poi) {
				MobclickAgent.onEvent(mContext, ConstantsUtil.EVENT_CLICK_POI);
				mPoiLayer.destroyLayer();
				mPoiLayer.addPoi(poi);
				/* 地图poi监听 */
				PoiDetailFragment detailFragment = new PoiDetailFragment(poi);
				mMapView.setCenter(poi.getX(), poi.getY());
				MyFragmentManager.getInstance().replaceFragment(
						NewFrameActivity.ID_ALL, detailFragment,
						MyFragmentManager.PROCESS_DIALOGFRAGEMENT_POI_DETAIL,
						MyFragmentManager.DIALOGFRAGMENT_POI_DETAIL);
				return null;
			}
		});
		mStatus = (TextView) view.findViewById(R.id.status);
		mStatus.setVisibility(View.GONE);
		// 初始化view
		mButtonLocate = (ImageButton) view.findViewById(R.id.button_locate);
		mButtonLocate.setOnClickListener(this);
		mChangeBuildButton = (Button) view.findViewById(R.id.btn_change_build);
		mChangeBuildButton.setOnClickListener(this);
		mButtonArguide = (Button) view.findViewById(R.id.btn_ar);
		mButtonArguide.setOnClickListener(this);

		NavTitle title = (NavTitle) view.findViewById(R.id.nav_title);
		title.setLeftOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {// 搜索楼层
				if (BaiduMapFragment.isShowing) {
					return;
				}
				MobclickAgent.onEvent(mContext, "event_click_indoor_search");

				RtmapSearchFragment rtmapSearchFragment;
				if (mMapShowBuildId.equals(mCurrentBuildId)) {
					rtmapSearchFragment = new RtmapSearchFragment(
							mMapShowBuildId);
				} else {
					rtmapSearchFragment = new RtmapSearchFragment(
							mMapShowBuildId);
				}

				MyFragmentManager.getInstance().replaceFragment(
						NewFrameActivity.ID_ALL, rtmapSearchFragment,
						MyFragmentManager.PROCESS_RTMAP_SEARCH,
						MyFragmentManager.FRAGMENT_RTMAP_SEARCH);
			}
		});
		title.setTitleOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (BaiduMapFragment.isShowing) {
					return;
				}
				MobclickAgent.onEvent(mContext, "event_click_indoor_title");

				FloorChangeFragment floorChangeFragment;
				if (mMapShowBuildId.equals(mCurrentBuildId)) {
					floorChangeFragment = new FloorChangeFragment(
							mMapShowBuildId);
				} else {
					floorChangeFragment = new FloorChangeFragment(
							mMapShowBuildId);
				}
				MyFragmentManager.getInstance().replaceFragment(
						NewFrameActivity.ID_ALL, floorChangeFragment,
						MyFragmentManager.PROCESS_RTMAP_FLOOR_CHANGE,
						MyFragmentManager.FRAGMENT_RTMAP_FLOOR_CHANGE);
			}
		});
		title.setRightOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (BaiduMapFragment.isShowing) {
					return;
				}
				MobclickAgent.onEvent(mContext, "event_click_indoor_find");

				FindManager.isFindShowing = true;

				MyFragmentManager.getInstance().replaceFragment(
						NewFrameActivity.ID_ALL, new FindFragment(),
						MyFragmentManager.PRCOCESS_DIALOGFRAGMENT_FIND,
						MyFragmentManager.DIALOGFRAGMENT_FIND);
			}
		});

		mButtonZoomUp = (ImageButton) view.findViewById(R.id.button_zoom_up);
		mButtonZoomDown = (ImageButton) view
				.findViewById(R.id.button_zoom_down);
		mButtonZoomUp.setOnClickListener(this);
		mButtonZoomDown.setOnClickListener(this);

		// mMapView.mDialogLoad.dismiss();
		RMLog.LOG_LEVEL = RMLog.LOG_LEVEL_INFO;
		LocationApp.getInstance().init(getActivity());
		LocationApp.getInstance().registerLocationListener(this);
		LocationApp.getInstance().start();
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

	}

	@Override
	public void onResume() {
		super.onResume();
		isStopRun = false;
		isOnSaveInstance = false;
		if (mRouteLayer.hasData()) {
			TestRtmapFragmentManager.getInstance().dismissLoading();
		}
		mMapView.startSensor();

		// followModel = true;

		// 显示上一次退出时的地图
		mCurrentBuildId = PreferencesUtil.getString(
				ConstantsUtil.PREFS_LAST_BUILD_ID, "861700010020300002");// aolai:860100010080300003,zhihut:860100010040500009
		mCurrentFloor = PreferencesUtil.getString(
				ConstantsUtil.PREFS_LAST_BUILD_FLOOR, "F1");// 默认楼层
		mCurrentBuildName = PreferencesUtil.getString(
				ConstantsUtil.PREFS_LAST_BUILD_NAME, "茂业天地");// 默认建筑名
		mMapShowCityName = PreferencesUtil.getString(
				ConstantsUtil.PREFS_LAST_CITY_NAME, "太原");
		// isMapShowPrivate = PreferencesUtil.getBoolean(
		// ConstantsUtil.PREFS_LAST_BUILD_IS_PRIVATE, false);

		if (XunluUtil.isEmpty(mCurrentBuildId)) {
			// showBaiduMap(true);
			return;
		}

		// 默认显示当前
		switchBuild(mCurrentBuildName, mCurrentBuildId, mCurrentFloor);
		if (mRtmapPushReceiver == null) {
			mRtmapPushReceiver = new RtmapPushReceiver(this);
			IntentFilter filter = new IntentFilter(PUSH_TRMAP_ACTION);
			XunluApplication.mApp.registerReceiver(mRtmapPushReceiver, filter);
		}
	}

	public void initFirst() {
		// 上海南京路寻宝，逻辑修改
		mCurrentBuildId = PreferencesUtil.getString(
				ConstantsUtil.PREFS_LAST_BUILD_ID, "");// aolai,zhihut:860100010040500009
		mCurrentFloor = PreferencesUtil.getString(
				ConstantsUtil.PREFS_LAST_BUILD_FLOOR, "");// 默认楼层
		mCurrentBuildName = PreferencesUtil.getString(
				ConstantsUtil.PREFS_LAST_BUILD_NAME, "");// 默认建筑名
		mMapShowCityName = PreferencesUtil.getString(
				ConstantsUtil.PREFS_LAST_CITY_NAME, "上海");
		//
		// if (XunluUtil.isEmpty(mCurrentBuildId)) {
		// showBaiduMap(true);
		// }
	}

	@Override
	public void onPause() {
		super.onPause();

		mMapView.removeSensor();
		// 将当前buildID，floor存入本地
		PreferencesUtil.putString(ConstantsUtil.PREFS_LAST_BUILD_ID,
				mMapShowBuildId);
		PreferencesUtil.putString(ConstantsUtil.PREFS_LAST_BUILD_FLOOR,
				mMapShowFloor);
		PreferencesUtil.putString(ConstantsUtil.PREFS_LAST_BUILD_NAME,
				mMapShowBuildName);
		PreferencesUtil.putString(ConstantsUtil.PREFS_LAST_CITY_NAME,
				mMapShowCityName);
		if (mRtmapPushReceiver != null) {
			XunluApplication.mApp.unregisterReceiver(mRtmapPushReceiver);
			mRtmapPushReceiver = null;
		}
	}

	public boolean isShowing() {
		if (isHidden()) {
			return false;
		}
		return true;
	}

	@Override
	public void onDestroy() {
		isStopRun = true;

		TestRtmapFragmentManager.getInstance().DestroyManager();

		// 停止定位服务
		mLocationIsRun = false;
		LocationApp.getInstance().unRegisterLocationListener(this);
		LocationApp.getInstance().stop();
		super.onDestroy();
	}

	private boolean mIsFirstLocate = true;
	public int mLastFlag = -1;
	public MyLocation myCurrLocation;

	boolean isStopRun = false;
	// added by linweiling
	static double last_indoor_x = 0;
	static double last_indoor_y = 0;
	static double last_outdoor_x = 0;
	static double last_outdoor_y = 0;

	@Override
	public void onBackStackCleared() {

	}

	@Override
	public void onClick(View v) {
		if (BaiduMapFragment.isShowing) {
			return;
		}
		switch (v.getId()) {
		case R.id.button_locate:// 定位跟随模式
			MobclickAgent.onEvent(mContext, "event_click_indoor_locate");
			if (mCurrentLocation != null
					&& mCurrentLocation.getBuildID().equals(
							mMapView.getBuildId()))
				mMapView.setCenter(mCurrentLocation.getX(),
						mCurrentLocation.getY());
			break;
		case R.id.button_nav_close: // 关闭导航
			closeNav();
			break;
		case R.id.btn_change_build:// 显示室外地图
			// 如果楼层选择正在显示，则关闭楼层选择fragment
			FloorChangeFragment fcf = (FloorChangeFragment) MyFragmentManager
					.getFragmentByFlag(
							MyFragmentManager.PROCESS_RTMAP_FLOOR_CHANGE,
							MyFragmentManager.FRAGMENT_RTMAP_FLOOR_CHANGE);
			if (fcf != null) {
				if (fcf.isAdded()) {
					List<String> flags = new ArrayList<String>();
					flags.add(MyFragmentManager.PROCESS_RTMAP_FLOOR_CHANGE
							+ "-"
							+ MyFragmentManager.FRAGMENT_RTMAP_FLOOR_CHANGE);
					MyFragmentManager.getInstance().backFragmentByFlags(flags);
				}
			}

			if (mRouteLayer.hasData()) {
				List<String> flags = new ArrayList<String>();
				flags.add(MyFragmentManager.PROCESS_NAV_FLOOR_CHANGE + "-"
						+ MyFragmentManager.FRAGMENT_NAV_FLOOR_CHANGE);
				MyFragmentManager.getInstance().backFragmentByFlags(flags);
			}
			TestRtmapFragmentManager.getInstance().clearFragmentMangerPoint();
			showBaiduMap(false);
			break;
		case R.id.btn_ar:
			MobclickAgent.onEvent(mContext, "event_click_indoor_ar");

			// 必须得有当前的位置信息才能进入AR导航
			if (mCurrentLocation == null) {
				ToastUtil.shortToast("定位失败，请稍后重试");
				return;
			}
			// 进入AR模式标志位
			ARShowActivity.isInARMode = true;

			// 跳转时候，所在的楼层
			ARShowActivity.mLastFloor = mCurrentFloor;

			ARTestManager.targetInfos = new ArrayList<POITargetInfo>();
			ARTestManager.targetInfos.clear();

			goIntoARScreen();// 旧逻辑

			// showAR();//新逻辑

			break;
		case R.id.rtmap_push_msg_lay:
			mPushNotificationLay.setVisibility(View.GONE);
			PushPoi pushPoi = (PushPoi) mPushNotificationLay.getTag();
			showPushPoiPoint(pushPoi);
			break;
		case R.id.button_zoom_up:
			MobclickAgent.onEvent(mContext, "event_click_indoor_zoomin");
			// 放大按钮
			mMapView.setScale(mMapView.getScale() / 2);
			break;
		case R.id.button_zoom_down:
			MobclickAgent.onEvent(mContext, "event_click_indoor_zoomout");
			// 缩小按钮
			mMapView.setScale(mMapView.getScale() * 2);
			break;
		}
	}

	private void goIntoARScreen() {
		boolean isOnOtherFloor = true;
		for (int i = 0; i < mRouteLayer.getNavigatePoints().size(); i++) {
			if (mRouteLayer.getNavigatePoints().get(i).getFloor()
					.equals(MyLocation.getMyLocation().getFloor())) {
				isOnOtherFloor = false;
			}
		}
		if (!isOnOtherFloor) {
			// 处于导航状态，展示导航信息
			int sizeOfKeyPoints = mRouteLayer.getNavigatePoints().size();
			for (int i = 1; i < sizeOfKeyPoints; i++) {
				NavigatePoint keyPoint = (NavigatePoint) mRouteLayer
						.getNavigatePoints().get(i);
				NavigatePoint keyPointPre = (NavigatePoint) mRouteLayer
						.getNavigatePoints().get(i - 1);

				POITargetInfo targetInfo = new POITargetInfo();
				targetInfo.setPoiTargetX(keyPoint.getX());
				targetInfo.setPoiTargetY(keyPoint.getY());
				targetInfo.setPoiTargetName(keyPoint.getAroundPoiName());
				targetInfo.setPoiTargetFloor(keyPointPre.getFloor());
				targetInfo
						.setPoiTargetRouteInfo(keyPointPre.getAroundPoiName());
				// targetInfo.setPoiTargetImageRes(XunluUtils.getResourceID(
				// mContext, keyPoint.gettrueName(), null, 0));
				ARTestManager.targetInfos.add(targetInfo);
			}

			NavigatePoint poi = mRouteLayer.getNavigatePoints().get(
					mRouteLayer.getNavigatePoints().size() - 1);

			// 最后一个点就是目标点
			POITargetInfo targetInfo = new POITargetInfo();
			targetInfo.setPoiTargetX(poi.getX());
			targetInfo.setPoiTargetY(poi.getY());
			targetInfo.setPoiTargetName(poi.getAroundPoiName());
			targetInfo.setPoiTargetFloor(poi.getFloor());
			targetInfo.setPoiTargetRouteInfo("到达终点");
			// targetInfo.setPoiTargetImageRes(XunluUtils.getResourceID(
			// mContext, StaticData.navigate_poi.getName(), null,
			// 0));
			ARTestManager.targetInfos.add(targetInfo);

			Intent intent = new Intent(mContext, ARGuideActivity.class);
			startActivity(intent);
		} else {
			setOldARPOI();
			// AR导航按钮
			Intent intent = new Intent(mContext, ARShowActivity.class);
			startActivity(intent);
		}
	}

	/**
	 * @author LiYan
	 * @date 2014-9-13 下午5:17:05
	 * @explain 显示室外地图
	 * @return void
	 * @param isDefault
	 */
	private void showBaiduMap(boolean isDefault) {
		/*** by kunge.hu **/
		BaiduMapFragment baiduMapFragment;
		if (isDefault) {
			baiduMapFragment = new BaiduMapFragment();
		} else {
			baiduMapFragment = new BaiduMapFragment(mMapShowBuildId,
					mMapShowFloor);
		}

		MyFragmentManager.getInstance().addFragment(NewFrameActivity.ID_ALL,
				baiduMapFragment, MyFragmentManager.PROCESS_BAIDU_MAP,
				MyFragmentManager.FRAGMENT_BAIDU_MAP);
		hideRtmapView();

	}

	public void showAR() {
		if (mCurrentLocation == null) {
			ToastUtil.shortToast("请定位成功后再试");
			return;
		}
		ARTestManager.isInARMode = true;

		setARPOI();

		MyFragmentManager.showFragmentdialog(new ARTestDialogFragment(),
				MyFragmentManager.PRCOCESS_DIALOGFRAGMENT_AR,
				MyFragmentManager.DIALOGFRAGMENT_AR);
		// ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	}

	// //优化之后的逻辑
	public void setARPOI() {
		// 非导航状态，展示所有的POI信息
		DrawMap drawMap = mMapView.getConfig().getDrawMap();
		Shape shape[] = drawMap.getLayer().getShapes();
		// 获取优惠扫描的数据
		for (int i = 0; i < shape.length; i++) {
			if (!shape[i].mName.equals("") && shape[i].mName != null) {
				arPoiItem item = new arPoiItem();
				item.setX((float) shape[i].mCenter.mX / 1000);
				item.setY((float) shape[i].mCenter.mY / 1000);

				// 第一个角落点的坐标
				item.setName(shape[i].mName);
				item.setCurrFloor(mCurrentFloor);
				ARTestManager.getInstance().arItemsList.add(item);

				// 算出来每个点和当前位置的距离
				// float dis = ARTestManager.getDistance(
				// (float) shape[i].mX / 1000, (float) shape[i].mY / 1000);
				// float degree = ARTestManager.getMyDegree(
				// (float) shape[i].mX / 1000, (float) shape[i].mY / 1000);
				// android.util.Log.d("kunge.hu", shape[i].mName + dis);
				// android.util.Log.d("kunge.hu", shape[i].mName + degree);
			}
		}
	}

	// 优化之前的逻辑
	public void setOldARPOI() {
		// 非导航状态，展示所有的POI信息
		DrawMap drawMap = mMapView.getConfig().getDrawMap();
		Shape shape[] = drawMap.getLayer().getShapes();
		// 获取优惠扫描的数据
		for (int i = 0; i < shape.length; i++) {
			if (!shape[i].mName.equals("") && shape[i].mName != null) {
				// 算出来每个点和当前位置的距离,距离大于默认距离则不天剑
				// float dis = ARTestManager.getDistance(
				// (float) shape[i].mX / 1000, (float) shape[i].mY / 1000);
				// float degree = ARTestManager.getMyDegree(
				// (float) shape[i].mX / 1000, (float) shape[i].mY / 1000);

				POITargetInfo targetInfo = new POITargetInfo();
				targetInfo.setPoiTargetX((float) shape[i].mCenter.mX);
				targetInfo.setPoiTargetY((float) shape[i].mCenter.mY);
				// 第一个角落点的坐标
				String poiName = shape[i].mName;

				targetInfo.setPoiTargetName(poiName);
				targetInfo.setPoiTargetFloor(MyLocation.getInstance()
						.getFloor());
				ARTestManager.targetInfos.add(targetInfo);
			}
		}
	}

	/**
	 * @author LiYan
	 * @date 2014-9-3 下午4:04:50
	 * @explain
	 * @return void
	 * @param buildName
	 * @param buildId
	 * @param floor
	 * @param isInitScale
	 *            是否初始化比例尺
	 */
	public void switchBuild(String buildName, String buildId, String floor) {
		Intent intent = new Intent(NavTitle.NAV_BROAD_CAST_FILTER);
		intent.putExtra(NavTitle.NAV_KEY_BUILD_NAME, buildName + " " + floor);
		mContext.sendBroadcast(intent);

		mMapShowBuildId = buildId;
		mMapShowBuildName = buildName;
		mMapShowFloor = floor;
		// if (mMapShowBuildId.equals(MyLocation.getInstance().getBuildId())) {
		// isMapShowPrivate = MyLocation.getInstance().isPrivate();
		// }
		// 清空地图所有图层数据
		mMapView.initMapConfig(mMapShowBuildId, mMapShowFloor);

		if (!mMapShowBuildId.equals(mCurrentBuildId)) {
			isShowArButton(false);
		}

		isShowBuildTitle(true, mMapShowBuildName, mMapShowFloor);

		// 判断导航滑动页面是否现实
		NavPagerFragment navPagerFragment = (NavPagerFragment) MyFragmentManager
				.getFragmentByFlag(MyFragmentManager.PROCESS_NAV_FLOOR_CHANGE,
						MyFragmentManager.FRAGMENT_NAV_FLOOR_CHANGE);
		if (navPagerFragment != null && !navPagerFragment.isRemoving()) {
			navPagerFragment.changeFloorByMap(mMapShowFloor);
		}
	}

	NavPagerFragment navPagerFragment;

	// 原来逻辑修改
	public void showNavLine(RMRoute model, POI start, POI end) {
		if (model != null && model.getPointlist().size() > 0) {

			ArrayList<NavigatePoint> navPoints = model.getPointlist();

			mRouteLayer.setDistance(model.getDistance());
			// mRouteLayer.setShowRoyteType(Constants.NOT_SHOW_ROUTE);
			mRouteLayer.setNavigatePoints(navPoints);
			mMapView.setLocationIcon(R.drawable.location_icon_navi,
					R.drawable.location_icon_navi);
			mRouteLayer.startNavigate();
			boolean isShowNavImg = false;

			// 意思是获取回来的导航列表中，只要有一个poi跟起点不是在一个楼层中，说明是跨层导航，这个时候就需要显示导航缩略图
			for (int i = 0; i < navPoints.size(); ++i) {
				NavigatePoint navigatePoint = navPoints.get(i);
				if (!navigatePoint.getFloor().equals(
						navPoints.get(0).getFloor())) {

					isShowNavImg = true;
					break;
				}
			}
			navPagerFragment = new NavPagerFragment(mMapView, mRouteLayer,
					mMapShowBuildId, mMapShowBuildName, model, isShowNavImg);
			// MyFragmentManager.getInstance().addFragment(R.id.tab_frame_lay,
			// navPagerFragment, MyFragmentManager.PROCESS_NAV_FLOOR_CHANGE,
			// MyFragmentManager.FRAGMENT_NAV_FLOOR_CHANGE);

			MyFragmentManager.getInstance().replaceFragment(
					NewFrameActivity.ID_ALL, navPagerFragment,
					MyFragmentManager.PROCESS_NAV_FLOOR_CHANGE,
					MyFragmentManager.FRAGMENT_NAV_FLOOR_CHANGE);
			// if(isShowNavImg) {
			// NavImgFragment imgFragment = new
			// NavImgFragment(model,mRouteLayer, mStartAndEndLayer,true);
			// MyFragmentManager.getInstance().addFragment(R.id.test_main_container,
			// imgFragment, MyFragmentManager.PRCOCESS_DIALOGFRAGMENT_NAV_IMG,
			// MyFragmentManager.DIALOGFRAGMENT_NAV_IMG);
			// }
		} else {
			// 提示没有路线
			ToastUtil.shortToast(R.string.toast_nav_route_err);
		}
		start = null;
		end = null;
	}

	/**
	 * 是否显示ar按钮
	 */
	private void isShowArButton(boolean isShow) {
		if (isShow) {
			mButtonArguide.setVisibility(View.VISIBLE);
		} else {
			mButtonArguide.setVisibility(View.GONE);
		}
	}

	/**
	 * @explain 显示建筑物title
	 * @param isShow
	 * @param buildName
	 * @param floor
	 */
	public void isShowBuildTitle(boolean isShow, String buildName, String floor) {
		// if (isShow) {
		// mBuildTitleLay.setVisibility(View.VISIBLE);
		// } else {
		// mBuildTitleLay.setVisibility(View.GONE);
		// }
		// if (buildName != null) {
		// TextView buildNameTextView = (TextView)
		// findViewById(R.id.map_show_build_name);
		// buildNameTextView.setText(buildName);
		// }
		// if (floor != null) {
		// TextView floorTextView = (TextView)
		// findViewById(R.id.map_show_floor);
		// floorTextView.setText(floor);
		// }
	}

	/**
	 * 是否显示定位按钮
	 */
	private void isShowLocateButton(boolean isShow) {
		if (isShow) {
			mButtonLocate.setVisibility(View.VISIBLE);
		} else {
			mButtonLocate.setVisibility(View.GONE);
		}
	}

	/**
	 * @explain 关闭导航
	 */
	public boolean closeNav() {
		MobclickAgent.onEvent(mContext, "event_click_indoor_route_cancel");

		if (mRouteLayer.hasData()) {
			mMapView.getTapPOILayer().setDisableTap(false);
			mMapView.refreshMap();
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 显示搜索后的poi点
	 * 
	 * @param poi
	 * @param buildName
	 *            如果buildName为空，则默认将poi点显示在当前地图建筑物，否则显示其他建筑物
	 */
	public void showSearchPoiPoint(POI poi, String buildName) {
		if (XunluUtil.isEmpty(buildName)) {
			// 普通的搜索
			switchBuild(mMapShowBuildName, poi.getBuildId(), poi.getFloor());
		} else {
			switchBuild(buildName, poi.getBuildId(), poi.getFloor());
		}
		// 默认让搜索出来的第一个poi变亮
		mPoiLayer.destroyLayer();
		mPoiLayer.addPoi(poi);
		PoiDetailFragment detailFragment = new PoiDetailFragment(poi);
		mMapView.setCenter(poi.getX(), poi.getY());
		MyFragmentManager.getInstance().replaceFragment(
				NewFrameActivity.ID_ALL, detailFragment,
				MyFragmentManager.PROCESS_DIALOGFRAGEMENT_POI_DETAIL,
				MyFragmentManager.DIALOGFRAGMENT_POI_DETAIL);
		mMapView.refreshMap();
	}

	/**
	 * @author LiYan
	 * @date 2014-9-10 上午10:36:55
	 * @explain 显示推送过来的poi信息
	 * @return void
	 * @param pushPoi
	 */
	public void showPushPoiPoint(PushPoi pushPoi) {
		// TODO
		ArrayList<POI> pois = new ArrayList<POI>();
		POI poi = new POI(Integer.valueOf(pushPoi.getPoiId()),
				pushPoi.getPoiName(), pushPoi.getBuildId(), pushPoi.getFloor(),
				Float.valueOf(pushPoi.getPoiX()), Float.valueOf(pushPoi
						.getPoiY()));

		pois.add(poi);
		// 普通的搜索
		switchBuild(pushPoi.getBuildName(), poi.getBuildId(), poi.getFloor());
		// 默认让搜索出来的第一个poi变亮
		mPoiLayer.addPoiList(pois);// pois，poi列表，0，第几个poi设置为地图中心店
		mMapView.setCenter(poi.getX(), poi.getY());
	}

	/**
	 * @author LiYan
	 * @date 2014-9-3 上午11:09:38
	 * @explain 获取地图mapview
	 * @return MapView
	 */
	public MapView getMapView() {
		return mMapView;
	}

	/**
	 * @author LiYan
	 * @date 2014-9-4 下午8:53:14
	 * @explain 获取当前地图显示的建筑物名称
	 * @return String
	 */
	public String getMapShowBuildName() {
		return mMapShowBuildName;
	}

	/**
	 * 隐藏RtmapView
	 */
	public void hideRtmapView() {
		if (mMapView != null)
			mMapView.setVisibility(View.GONE);
	}

	/**
	 * 显示RtmapView
	 */
	public void showRtmapView() {
		mMapView.setVisibility(View.VISIBLE);
	}

	/**
	 * @author LiYan
	 * @date 2014-9-9 下午9:48:47
	 * @explain 更新推送信息条
	 * @return void
	 * @param msg
	 */
	public void showPushNotification(String msg) {
		PushPoi pushPoi = JSON.parseObject(msg.toString(), PushPoi.class);
		TextView pushMsg = (TextView) mPushNotificationLay
				.findViewById(R.id.rtmap_push_msg);
		pushMsg.setText(pushPoi.getCoupon());
		mPushNotificationLay.setTag(pushPoi);
		mPushNotificationLay.setVisibility(View.VISIBLE);
	}

	public void showLoading() {
		if (mLoadingFragment == null) {
			mLoadingFragment = new LoadingFragment(R.string.loading_init);
		}
		MyFragmentManager.showFragmentdialog(mLoadingFragment,
				MyFragmentManager.PROCESS_LOADING,
				MyFragmentManager.FRAGMENT_LOADING);
	}

	public void dismissLoading() {
		if (mLoadingFragment != null) {
			mLoadingFragment.dismiss();
		}
	}

	private class RtmapPushReceiver extends BroadcastReceiver {

		private TestRtmapFragment mRtmapFragment;

		public RtmapPushReceiver(TestRtmapFragment rtmapFragment) {
			mRtmapFragment = rtmapFragment;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			String msg = intent.getStringExtra(KEY_PUSH_MSG);
			mRtmapFragment.showPushNotification(msg);
		}
	}

	public TapPOILayer getTapPOILayer() {
		return mTapPOILayer;
	}

	public void cancelMapRouteFloowMode() {
	}

	/**
	 * @explain 闪烁显示优惠信息
	 * @param isAnimationDisplay
	 */
	private void fetchCouponsInCache(String data, String buildId,
			boolean isAnimationDisplay) {
		// 同一天不需要获取
		try {
			FavorablePoiParseUtil parseUtil = new FavorablePoiParseUtil(data);
			if (isStopRun) {
				return;
			}
			List<FavorablePoiDbModel> dbPois = parseUtil.mPois;
			ArrayList<POI> pois = new ArrayList<POI>();
			for (FavorablePoiDbModel dbPoi : dbPois) {
				POI poi = new POI(Integer.valueOf(dbPoi.number), dbPoi.poiName,
						buildId, dbPoi.floor, Float.valueOf(dbPoi.poiX),
						Float.valueOf(dbPoi.poiY));
				poi.setDesc(dbPoi.discription);
				poi.setPoiImage(dbPoi.adUrl);
				poi.setType(1);

				pois.add(poi);
			}
			// 把搜索到的优惠信息放进地图
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public boolean isOnSaveInstance = false;

	@Override
	public void onSaveInstanceState(Bundle outState) {
		isOnSaveInstance = true;
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onReceiveLocation(RMLocation location) {
		Log.i("rtmap", location.getErrorInfo());
		mMapView.setMyCurrentLocation(location);
		mStatus.setText(location.getErrorInfo());
		if (location.getError() == 0) {
			isShowArButton(true);
			if (mCurrentLocation == null
					|| (!location.getBuildID().equals(
							mCurrentLocation.getBuildID()) || !location
							.getFloor().equals(mCurrentLocation.getFloor()))) {
				mMapView.initMapConfig(location.getBuildID(),
						location.getFloor());
				if (mCurrentLocation == null
						|| !location.getBuildID().equals(
								mCurrentLocation.getBuildID())) {
					Build build = DBOperation.getInstance().queryBuildById(
							location.getBuildID());
					MyLocation.getInstance().setBuild(build);
					if (build != null) {
						MyLocation.getInstance().setBuildName(build.getName());
						switchBuild(build.getName(), location.getBuildID(),
								location.getFloor());
					}
				}
			}
			mCurrentLocation = location;
			MyLocation.getInstance().setBuildId(location.getBuildID());
			MyLocation.getInstance().setFloor(location.getFloor());
			MyLocation.getInstance().setX(location.getX());
			MyLocation.getInstance().setY(location.getY());
			MyLocation.getInstance()
					.setInOutDoorFlg(location.getInOutDoorFlg());
			// MyLocation.getInstance().setInOutDoorFlg(1);
			MyLocation.getInstance().setGpsLat(location.latitude);
			MyLocation.getInstance().setGpsLng(location.longitude);
			// LocationImpl.getInstance()
			// .notfilyObserver(MyLocation.getInstance());
		} else {
		}
	}
}
