package com.rtmap.indoor_switch.pages;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rtm.common.model.BuildInfo;
import com.rtm.common.model.POI;
import com.rtm.common.model.RMLocation;
import com.rtm.common.utils.Constants;
import com.rtm.frm.map.CompassLayer;
import com.rtm.frm.map.MapView;
import com.rtm.frm.map.POILayer;
import com.rtm.frm.map.RouteLayer;
import com.rtm.frm.map.TapPOILayer;
import com.rtm.frm.model.NavigatePoint;
import com.rtm.frm.model.RMRoute;
import com.rtm.frm.utils.Handlerlist;
import com.rtmap.indoor_switch.LCScannerActivity;
import com.airport.test.R;
import com.rtmap.indoor_switch.ar.ArGuideActivity;
import com.rtmap.indoor_switch.ar.ArManager;
import com.rtmap.indoor_switch.ar.ArShowActivity;
import com.rtmap.indoor_switch.ar.ArShowView;
import com.rtmap.indoor_switch.ar.MyArItem;
import com.rtmap.indoor_switch.base.BaseActivity;
import com.rtmap.indoor_switch.base.BaseFragment;
import com.rtmap.indoor_switch.bean.PrivateBuild;
import com.rtmap.indoor_switch.manager.RtMapLocManager;
import com.rtmap.indoor_switch.utils.DialogUtil;
import com.rtmap.indoor_switch.utils.RMlbsUtils;
import com.rtmap.indoor_switch.utils.SensorManagerUtil;
import com.rtmap.indoor_switch.utils.ToastUtils;

/**
 * Created by ly on 15-7-24.
 */
public class RtmFragment extends BaseFragment implements View.OnClickListener,
		RMlbsUtils.OnRmGetFinishListener,
		RtMapLocManager.RtMapLocManagerListener {

	private BuildInfo buildInfo;
	private MapView mvRtm;
	private TextView tvBuildName;
	private TextView tvFloor;
	private Button btnMyLocation;
	private Dialog loading;
	private LinearLayout llSearch;
	private LinearLayout llCleanNavLine;
	private ImageView imgBack;
	private ImageView imgScan;

	private RelativeLayout dialogContentRel;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Constants.RTMAP_MAP:
				int progress = msg.arg1;
				if (progress == Constants.MAP_LOAD_START) {// 开始加载
					showLoading();
				} else if (progress == Constants.MAP_FailNetResult) {// 校验结果失败
					dismissLoading();
				} else if (progress == Constants.MAP_FailCheckNet) {// 联网检测失败
					dismissLoading();
				} else if (progress == Constants.MAP_Down_Success) {
				} else if (progress == Constants.MAP_Down_Fail) {
					dismissLoading();
				} else if (progress == Constants.MAP_LOAD_END) {
					dismissLoading();
				}
				break;
			}
		}
	};
	SensorManagerUtil sensorManagerUtil;

	public RtmFragment() {

	}

	@Override
	protected View initView(LayoutInflater inflater) {
		getActivity().getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		View view = inflater.inflate(R.layout.fragment_rtm, null);
		Handlerlist.getInstance().register(handler);
		mvRtm = (MapView) view.findViewById(R.id.mv_rtm);
        mvRtm.startSensor();// 开启指针方向
		
		tvBuildName = (TextView) view.findViewById(R.id.tv_build_name);
		tvFloor = (TextView) view.findViewById(R.id.tv_floor);
		btnMyLocation = (Button) view.findViewById(R.id.btn_my_location);
		llSearch = (LinearLayout) view.findViewById(R.id.ll_search);
		llCleanNavLine = (LinearLayout) view
				.findViewById(R.id.ll_clean_nav_line);
		imgBack = (ImageView) view.findViewById(R.id.img_back);
		imgScan = (ImageView) view.findViewById(R.id.img_scan);
		dialogContentRel = (RelativeLayout) view
				.findViewById(R.id.rl_dialog_content);

		// 通过自定义manager进行管理
		RtMapLocManager.instance().addReceiver(this);

		sensorManagerUtil = new SensorManagerUtil(getActivity(),
				new SensorManagerUtil.SensorEventListener() {
					@Override
					public void onSensorChanged(boolean isFlat) {// 是否平放
						if (isFlat) {// 水平
							closeAr();
						} else {// 垂直
							openAr();
						}
					}
				});
		sensorManagerUtil.registerSensor();

		show(dialogContentRel, TRANSFORM);
		return view;
	}

	@Override
	protected void setListener() {
		mvRtm.setOnMapModeChangedListener(new MapView.OnMapModeChangedListener() {
			@Override
			public void onMapModeChanged() {
				setFollowModel(false);
			}
		});
		tvFloor.setOnClickListener(this);
		btnMyLocation.setOnClickListener(this);
		llSearch.setOnClickListener(this);
		llCleanNavLine.setOnClickListener(this);
		imgBack.setOnClickListener(this);
		imgScan.setOnClickListener(this);

	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		Handlerlist.getInstance().remove(handler);
	}

	@Override
	protected void initData(Bundle savedInstanceState) {
		Bundle bundle = getArguments();
		buildInfo = (BuildInfo) bundle.getSerializable("data");
		if (buildInfo == null) {
			return;
		}
		tvBuildName.setText(buildInfo.getBuildName());
		initLayers();
		switchFloor(buildInfo.getBuildId(), buildInfo.getFloorlist().get(0)
				.getFloor());
	}

	private POILayer mPoiLayer;
	private RouteLayer mRouteLayer;
	private TapPOILayer mTapPOILayer;

	private void initLayers() {

		mTapPOILayer = new TapPOILayer(mvRtm);
		mTapPOILayer
				.setOnPOITappedListener(new TapPOILayer.OnPOITappedListener() {
					@Override
					public Bitmap onPOITapped(POI poi) {
						showPoiDetail(poi);
						Bitmap bitmap = BitmapFactory.decodeResource(
								getResources(), R.drawable.da_marker_red);
						return bitmap;
					}
				});
		mvRtm.addMapLayer(mTapPOILayer);

		Bitmap startBit = BitmapFactory.decodeResource(getResources(),
				R.drawable.navi_start);
		Bitmap endBit = BitmapFactory.decodeResource(getResources(),
				R.drawable.navi_end);
		Bitmap marker = BitmapFactory.decodeResource(getResources(),
				R.drawable.da_marker_red);
		mRouteLayer = new RouteLayer(mvRtm, startBit, endBit, marker);
		mvRtm.addMapLayer(mRouteLayer);

		mPoiLayer = new POILayer(mvRtm);
		mPoiLayer.setOnPOIDrawListener(new POILayer.OnPOIDrawListener() {
			@Override
			public Bitmap onPOIDraw(POI poi) {
				return BitmapFactory.decodeResource(getResources(),
						R.drawable.icon_poi);
			}
		});
		mvRtm.addMapLayer(mPoiLayer);

		CompassLayer compassLayer = new CompassLayer(mvRtm);
		compassLayer.setPosition(Constants.TOP_LEFT);
		mvRtm.addMapLayer(compassLayer);
		mvRtm.refreshMap();
	}

	@Override
	public void onResume() {
		super.onResume();
		RtMapLocManager.instance().startLoc();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onDestroy() {
		mvRtm.clearLayers();
		mvRtm.removeAllViews();
		mvRtm = null;
		sensorManagerUtil.unRegisterSensor();
		RtMapLocManager.instance().removeReceiver(this);
		stopTimer();
		unregisterScanReciever();
		super.onDestroy();
	}

	private boolean mIsFirstLocate = true;

	/**
	 * @param isFollow
	 * @explain 设置地图是否为跟随模式
	 */
	public void setFollowModel(boolean isFollow) {
		if (isFollow) {
			btnMyLocation.setBackgroundResource(R.drawable.local_flow);// 跟随
		} else {
			btnMyLocation.setBackgroundResource(R.drawable.local_my);//
		}

		RtMapLocManager.instance().setFollowMode(isFollow);
	}

	/***
	 * 显示poi详情
	 *
	 * @param poi
	 */
	private void showPoiDetail(POI poi) {
		mPoiLayer.clearLayer();
		PoiDetailFragment poiDetailFragment = new PoiDetailFragment();
		poiDetailFragment.setPoi(poi);
		poiDetailFragment
				.setPoiDetailCallBack(new PoiDetailFragment.PoiDetailCallBack() {
					@Override
					public void onSetStart(POI poi) {
						mStartPoi = poi;
						showNavLine(mStartPoi, mEndPoi);
					}

					@Override
					public void onSetEnd(POI poi) {
						mEndPoi = poi;
						showNavLine(mStartPoi, mEndPoi);
					}
				});
		((BaseActivity) getActivity()).pushFragment(poiDetailFragment,
				R.id.main_content);
	}

	private POI mStartPoi = null;
	private POI mEndPoi = null;

	/***
	 * 显示路线规划
	 *
	 * @param start
	 * @param end
	 */
	private void showNavLine(POI start, POI end) {
		if (start != null && end != null) {
			showLoading();
			RMlbsUtils.getInstance().getNavigation(mvRtm.getBuildId(),
					mStartPoi, mEndPoi, null, this);
		}
	}

	/***
	 * 在地图上显示poi
	 *
	 * @param poi
	 */
	public void showPoiNoMap(POI poi) {
		mPoiLayer.clearLayer();
		mPoiLayer.addPoi(poi);
		mvRtm.setCenter(poi.getX(), poi.getY());
		mvRtm.refreshMap();
	}

	/***
	 * 切换建筑物楼层
	 *
	 * @param buildId
	 * @param floor
	 */
	public void switchFloor(String buildId, String floor) {
		if (!floor.equals(mvRtm.getFloor())) {
			mvRtm.initMapConfig(buildId, floor);
			tvFloor.setText(floor);
		}
	}

	/***
	 * 是否显示清楚路线按钮
	 *
	 * @param isShow
	 */
	private void showCleanNavLine(boolean isShow) {
		if (isShow) {
			llCleanNavLine.setVisibility(View.VISIBLE);
		} else {
			if (mRouteLayer.hasData()) {
				mRouteLayer.clearLayer();
				mvRtm.refreshMap();
			}
			RotateAnimation ro = new RotateAnimation(0f, 30f,
					Animation.RELATIVE_TO_SELF, 0.5f,
					Animation.RELATIVE_TO_SELF, 0.5f);
			ro.setDuration(500);
			ro.setInterpolator(new CycleInterpolator(2f));
			ro.setRepeatCount(0);
			ro.setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {

				}

				@Override
				public void onAnimationEnd(Animation animation) {
					llCleanNavLine.setVisibility(View.GONE);
				}

				@Override
				public void onAnimationRepeat(Animation animation) {

				}
			});
			llCleanNavLine.startAnimation(ro);
			mStartPoi = null;
			mEndPoi = null;
		}
	}

	private void showLoading() {
		try {

			if (loading == null) {
				loading = DialogUtil.getLoadingDialog(null, false, null);
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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_floor: {
			FloorListFragment floorListFragment = new FloorListFragment();
			floorListFragment.setBuildId(mvRtm.getBuildId());
			((BaseActivity) getActivity()).pushFragment(floorListFragment,
					R.id.main_content);
		}
			break;
		case R.id.btn_my_location: {
			setFollowModel(true);
		}
			break;
		case R.id.ll_search: {
			RtmSearchFragment rtmSearchFragment = new RtmSearchFragment();
			rtmSearchFragment.setBuildId(mvRtm.getBuildId());
			((BaseActivity) getActivity()).pushFragment(rtmSearchFragment,
					R.id.main_content);
		}
			break;
		case R.id.ll_clean_nav_line: {
			showCleanNavLine(false);
		}
			break;
		case R.id.img_back: {
			// 用户手动退出取消跟随
			RtMapLocManager.instance().setFollowMode(false);
			backFragment();
		}
			break;
		case R.id.img_scan: {
			openScan();
		}
			break;
		}
	}

	private void openScan() {
		isScanOpened = true;
		// 启动扫码，注册扫描结果接收器
		registerScanReceiver();
		Intent intent = new Intent(context, LCScannerActivity.class);
		startActivity(intent);
	}

	private BroadcastReceiver scanReciver;
	private boolean isScanOpened = false;

	private void registerScanReceiver() {
		if (scanReciver == null) {
			scanReciver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					isScanOpened = false;
					show(dialogContentRel, BOARDING_INFORMATION);

					// 假数据楼梯口x = 271.339
					// y = 291.333
					POI startPOI = new POI();
					startPOI.setBuildId("860100010030100002");
					startPOI.setFloor("F2");
					startPOI.setX(271.339f);
					startPOI.setY(291.333f);
					startPOI.setName("当前位置");
					mStartPoi = startPOI;

					// 安检中心
					POI endPoi = new POI();
					endPoi.setBuildId("860100010030100002");
					endPoi.setName("安检中心");
					endPoi.setFloor("F2");
					endPoi.setX(377.344f);
					endPoi.setY(198.983f);
					endPoi.setPoiNo(388);
					mEndPoi = endPoi;

					showNavLine(startPOI, endPoi);
				}
			};
		}

		IntentFilter intentFilter = new IntentFilter("");
		context.registerReceiver(scanReciver, intentFilter);

	}

	private void unregisterScanReciever() {
		if (scanReciver != null) {
			context.unregisterReceiver(scanReciver);
		}
	}

	@Override
	public void onGetCityListFinish(List<String> result) {

	}

	@Override
	public void onGetBuildListFinish(List<BuildInfo> result) {

	}

	@Override
	public void onGetBuildDetailFinish(BuildInfo result) {
		if (!this.isDetached()) {
			getBuildDetailFinish = true;
			if (result != null) {
				buildInfo = result;
				tvBuildName.setText(buildInfo.getBuildName());
				if (mRmLocation.error == 0)
					switchFloor(buildInfo.getBuildId(), mRmLocation.getFloor());
			}
		}
	}

	@Override
	public void onGetNavigationFinish(RMRoute result) {
		dismissLoading();
		if (result != null) {
			mPoiLayer.destroyLayer();
			mTapPOILayer.destroyLayer();
			mRouteLayer.setNavigatePoints(result.getPointlist());
			mvRtm.refreshMap();
			showCleanNavLine(true);
		} else {
			ToastUtils.shortToast(R.string.no_result);
		}
	}

	@Override
	public void onGetPoiSearchFinish(List<POI> result) {

	}

	@Override
	public void onGetPrivateBuildFinish(List<PrivateBuild> result) {

	}

	private RMLocation mRmLocation;
	private int i = 0;

	@Override
	public void onRtMapLocListenerReceiver(RMLocation rmLocation,
			boolean isFollowing) {
		mRmLocation = rmLocation;

		// try {
		// tvBuildName.setText("error: " + rmLocation.error);
		// } catch (Exception e) {
		// ToastUtils.shortToast("RMLocation null");
		// }
		// if (rmLocation == null || rmLocation.error != 0) {
		if (rmLocation == null) {
			return;
		}
		// tvBuildName.setText(rmLocation.getInOutDoorFlg() + "  " +
		// rmLocation.getFloor());
		if (rmLocation.getInOutDoorFlg() == RMLocation.LOC_OUTDOOR
				&& isFollowing) {
			finishActivity();
			return;
		}

		if (mRouteLayer.hasData()) {
			ArrayList<NavigatePoint> navigatePoints = mRouteLayer
					.getmNavigatePoints();
			NavigatePoint navpoint;
			if (i < navigatePoints.size()) {
				navpoint = navigatePoints.get(i++);
			} else {
				navpoint = navigatePoints.get(navigatePoints.size() - 1);
			}
			rmLocation.setX(navpoint.getX());
			rmLocation.setY(navpoint.getY());
			rmLocation.setFloor("F2");
			rmLocation.buildID = "860100010030100002";
			rmLocation.floorID = 20020;
		} else {
			// poi.setX(377.344f);
			// poi.setY(198.983f);
			rmLocation.setX(377.344f);
			rmLocation.setY(198.983f);
			rmLocation.setFloor("F2");
			rmLocation.buildID = "860100010030100002";
			rmLocation.floorID = 20020;

		}
		mRmLocation = rmLocation;

		String locateFloor = RMlbsUtils.getInstance().getFloorById(
				rmLocation.getFloorID());
		String locateBuildId = rmLocation.getBuildID();

		// if (!locateBuildId.equals(mvRtm.getBuildId())) {//
		// 如果当前地图显示的buildID与定位buildId不同，则不显示定位点
		// // 提示切换建筑&& !mIsChangeBuild
		// return;
		// }
		// 判断是否显示当前位置点
		// float x = rmLocation.getCoordX() / 1000;
		// float y = rmLocation.getCoordY() / 1000;
		float x = rmLocation.getX();
		float y = rmLocation.getY();

		if (!locateFloor.equals(mvRtm.getFloor())
				|| !locateBuildId.equals(mvRtm.getBuildId())) {

			if (RtMapLocManager.instance().isFollowing()) {
				if (!locateBuildId.equals(mvRtm.getBuildId())) {
					// 获取当前定位建筑信息
					getLocBuildDetail(locateBuildId);
					return;
				} else {
					switchFloor(locateBuildId, locateFloor);
				}
			}
		}

		if (mIsFirstLocate) {
			mIsFirstLocate = false;
			setFollowModel(isFollowing);
		}
		mvRtm.setMyCurrentLocation(rmLocation);

		ArManager.ArLocation location = new ArManager.ArLocation();
		location.setFloor("F2");
		location.setBuildId(locateBuildId);
		location.setTargetX(rmLocation.getCoordX());
		location.setTargetY(rmLocation.getCoordY());

		ArManager.instance().notifyLocationChanged(location);
	}

	private boolean getBuildDetailFinish = true;

	private void getLocBuildDetail(String buildId) {
		showLoading();
		if (getBuildDetailFinish) {
			getBuildDetailFinish = false;
			RMlbsUtils.getInstance().getBuildDetail(buildId, this);
		}
	}

	private boolean isArOpened = false;

	private void openAr() {

		if (isArOpened || isScanOpened) {
			return;
		}
		isArOpened = true;
		List<ArShowView> showViews = new ArrayList<ArShowView>();
		// x:377.344,y:198.983,poiNo:388,id:860100010030100002,F2,安全检查
		// POI poi = new POI();
		// poi.setBuildId("860100010030100002");
		// poi.setName("安检中心");
		// poi.setFloor("F2");
		// poi.setX(377.344f);
		// poi.setY(198.983f);
		// poi.setPoiNo(388);

		POI poi = new POI();
		poi.setBuildId("860100010030100002");
		poi.setName("书刊店");
		poi.setFloor("F2");
		poi.setX(336.62f);
		poi.setY(255.329f);
		poi.setPoiNo(219);
		// x = 336.62
		// y = 255.329

		View view = View.inflate(context, R.layout.ar_item_view, null);
		MyArItem myArItemView = new MyArItem();
		myArItemView.setLayoutView(view);
		myArItemView.setFloor(poi.getFloor());
		myArItemView.setPoiTargetX(poi.getX());
		myArItemView.setPoiTargetY(poi.getY());
		myArItemView.setTargetName(poi.getName());
		((TextView) view.findViewById(R.id.tv_item_name))
				.setText(poi.getName());
		showViews.add(myArItemView);

		ArManager.instance().setArShowViews(showViews);

		Intent intent = new Intent(context, ArGuideActivity.class);
		intent.putExtra(ArShowActivity.KEY_MAP_DEGREE, mvRtm.getAngle());
		intent.putExtra("build_name", tvBuildName.getText());
		startActivity(intent);
	}

	public static final String ACTION_AR_SHOW_CLOSE = "ACTION_AR_SHOW_CLOSE";

	private void closeAr() {
		if (!isArOpened) {
			return;
		}
		isArOpened = false;

		// 安检中心
		POI endPoi = new POI();
		endPoi.setBuildId("860100010030100002");
		endPoi.setName("安检中心");
		endPoi.setFloor("F2");
		endPoi.setX(377.344f);
		endPoi.setY(198.983f);
		endPoi.setPoiNo(388);

		showPoiNoMap(endPoi);

		show(dialogContentRel, CLASSIFY);
		Intent intent = new Intent(ACTION_AR_SHOW_CLOSE);
		context.sendBroadcast(intent);
	}

	/***
	 * /提示框
	 */
	TextView mTransform;
	TextView mClassify;
	TextView mInteresting;
	TextView mTime;
	Object senMessage;
	ImageView mDiaglog;

	public static final int TRANSFORM = 0;// 室内外切换
	public static final int BOARDING_INFORMATION = 1;// 登记卡信息
	public static final int INTEREST = 2;// 兴趣
	public static final int CLASSIFY = 3;// 排队
	public static final int LAST_TIME = 4;// 登机时间

	private Handler hhhh = new Handler();
	private Runnable timerRun = new Runnable() {
		@Override
		public void run() {
			mTransform.setVisibility(View.GONE);
			mClassify.setVisibility(View.GONE);
			mInteresting.setVisibility(View.GONE);
		}
	};

	private void startTimer(int delayTime) {
		hhhh.postDelayed(timerRun, delayTime);
	}

	private void stopTimer() {
		hhhh.removeCallbacks(timerRun);
	}

	private void show(RelativeLayout relativeLayout, int i) {

		relativeLayout.addView(diaglog_1());
		relativeLayout.addView(diaglog_2());
		relativeLayout.addView(diaglog_3());
		relativeLayout.addView(diaglog_4());
		relativeLayout.addView(diaglog_5());
		switch (i) {
		case TRANSFORM:// 室内外切换
			mTransform.setVisibility(View.VISIBLE);
			Message message_1 = new Message();
			message_1.obj = senMessage;
			// hhhh.sendMessage(message_1);
			startTimer(5000);
			break;
		case BOARDING_INFORMATION:// 登记卡信息
			mDiaglog.setVisibility(View.VISIBLE);
			// dialogContentRel.setAlpha(0.85f);
			break;
		case INTEREST:// 兴趣
			mInteresting.setVisibility(View.VISIBLE);
			Message message_3 = new Message();
			message_3.obj = senMessage;
			// hhhh.sendMessage(message_3);
			startTimer(5000);
			break;
		case CLASSIFY:// 排队
			mClassify.setVisibility(View.VISIBLE);
			Message message_4 = new Message();
			message_4.obj = senMessage;
			// hhhh.sendMessage(message_4);
			startTimer(8000);
			break;
		case LAST_TIME:// 登机时间
			mTime.setVisibility(View.VISIBLE);
			break;
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private View diaglog_5() {
		mTime = new TextView(context);
		mTime.setText("您离登机时间还有28分");
		mTime.setTextColor(Color.parseColor("#FFF9FB"));
		mTime.setBackgroundColor(Color.parseColor("#797981"));
		mTime.setGravity(Gravity.CENTER);
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT, 80);
		mTime.setAlpha((float) 0.85);
		mTime.setLayoutParams(layoutParams);
		mTime.setVisibility(View.GONE);
		return mTime;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private View diaglog_3() {
		mInteresting = new TextView(context);
		mInteresting.setText("您想看的《芈月传》本店已上架");
		mInteresting.setTextColor(Color.parseColor("#FFF9FB"));
		mInteresting.setBackgroundColor(Color.parseColor("#ED3970"));
		mInteresting.setGravity(Gravity.CENTER);
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT, 150);
		mInteresting.setAlpha((float) 0.9);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		mInteresting.setLayoutParams(layoutParams);
		mInteresting.setVisibility(View.GONE);

		return mInteresting;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	private View diaglog_4() {
		mClassify = new TextView(context);
		mClassify.setText("当前安检排队人数较多，请尽快前往");
		mClassify.setTextColor(Color.parseColor("#FFF9FB"));
		mClassify.setBackgroundColor(Color.parseColor("#ED3970"));
		mClassify.setGravity(Gravity.CENTER);
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT, 150);
		mClassify.setAlpha((float) 0.9);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		mClassify.setLayoutParams(layoutParams);
		mClassify.setVisibility(View.GONE);

		return mClassify;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private View diaglog_1() {
		mTransform = new TextView(context);
		mTransform.setText("欢迎来到首都机场");
		mTransform.setTextColor(Color.parseColor("#FFF9FB"));
		mTransform.setBackgroundColor(Color.parseColor("#ED3970"));
		mTransform.setGravity(Gravity.CENTER);
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT, 150);
		mTransform.setAlpha((float) 0.9);
		mTransform.setLayoutParams(layoutParams);
		mTransform.setVisibility(View.GONE);

		return mTransform;
	}

	private ImageView diaglog_2() {
		mDiaglog = new ImageView(context);
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);
		mDiaglog.setImageResource(R.drawable.passmessage_content);
		mDiaglog.setBackgroundResource(R.drawable.passenger_bgt);
		mDiaglog.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

		mDiaglog.setLayoutParams(layoutParams);
		mDiaglog.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// onBackPressed();
				mDiaglog.setVisibility(View.GONE);
				// dialogContentRel.setAlpha(0.0f);
			}
		});
		mDiaglog.setVisibility(View.GONE);

		return mDiaglog;
	}
}
