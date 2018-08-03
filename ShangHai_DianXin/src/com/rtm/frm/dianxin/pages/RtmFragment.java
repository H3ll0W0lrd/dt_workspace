package com.rtm.frm.dianxin.pages;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.rtm.common.model.RMLocation;
import com.rtm.common.utils.Constants;
import com.rtm.common.utils.RMAsyncTask;
import com.rtm.frm.dianxin.R;
import com.rtm.frm.dianxin.base.BaseActivity;
import com.rtm.frm.dianxin.base.BaseFragment;
import com.rtm.frm.dianxin.bean.PrivateBuild;
import com.rtm.frm.dianxin.manager.RtMapLocManager;
import com.rtm.frm.dianxin.utils.DialogUtil;
import com.rtm.frm.dianxin.utils.RMlbsUtils;
import com.rtm.frm.dianxin.utils.ToastUtils;
import com.rtm.frm.map.CompassLayer;
import com.rtm.frm.map.MapView;
import com.rtm.frm.map.POILayer;
import com.rtm.frm.map.RouteLayer;
import com.rtm.frm.map.TapPOILayer;
import com.rtm.frm.model.BuildInfo;
import com.rtm.frm.model.CityInfo;
import com.rtm.frm.model.Location;
import com.rtm.frm.model.POI;
import com.rtm.frm.model.RMRoute;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

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
	private CityInfo mCityInfo;
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

	private Map<String, Object> errorBuildMap = new HashMap<String, Object>();

	public RtmFragment() {

	}

	@Override
	protected View initView(LayoutInflater inflater) {
		// initMapColor();
		getActivity().getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		View view = inflater.inflate(R.layout.fragment_rtm, null);
		// Constants.MAP_BACKGROUND_COLOR = 0xf9f5f2;
		mvRtm = (MapView) view.findViewById(R.id.mv_rtm);
		tvBuildName = (TextView) view.findViewById(R.id.tv_build_name);
		tvFloor = (TextView) view.findViewById(R.id.tv_floor);
		btnMyLocation = (Button) view.findViewById(R.id.btn_my_location);
		llSearch = (LinearLayout) view.findViewById(R.id.ll_search);
		llCleanNavLine = (LinearLayout) view
				.findViewById(R.id.ll_clean_nav_line);
		RMAsyncTask.EXECUTOR.execute(new Runnable() {

			@Override
			public void run() {
				BufferedReader br;
				try {
					br = new BufferedReader(new InputStreamReader(getActivity()
							.getAssets().open("a.txt"), "utf-8"));
					String line;
					StringBuilder builder = new StringBuilder();
					while ((line = br.readLine()) != null) {
						// 将文本打印到控制台
						builder.append(line);
					}
					br.close();
					String s = builder.toString();
					Gson gson = new Gson();
					mCityInfo = gson.fromJson(s, CityInfo.class);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		// 通过自定义manager进行管理
		RtMapLocManager.instance().addReceiver(this);
		return view;
	}

	/***
     */
	private void initMapColor() {
		// 初始化地图配色
		int MAPINVALID_COLOR_FILL = 0xe1e4e5;// 无效区域 0x1e1d1c 0xff1566f4
		int MAPINVALID_COLOR_BORDER = 0xd4d1d0;

		int MAPUNKNOWN_COLOR_FILL = 0xffe1e4e5;// 未知区域 0x3e3e3d 0xff1566f4
		int MAPUNKNOWN_COLOR_BORDER = 0xd4d1d0;

		int MAPPOI_COLOR_FILL = 0xf5f6f7;// 店铺 0x3e3e3b 0xff1566f4
		int MAPPOI_COLOR_BORDER = 0xd4d1d0;

		int MAPWC_COLOR_FILL = 0xfff5f5;// 卫生间 0xff383233 0xff1566f4
		int MAPWC_COLOR_BORDER = 0xd4d1d0;

		int MAPSTAIRS_COLOR_FILL = 0xfff5f5;// 电梯 0xff383233 0xff1566f4
		int MAPSTAIRS_COLOR_BORDER = 0xd4d1d0;

		int MAPGROUND_COLOR_FILL = 0xf9f5f2;// 外边框 0xff1566f4
		int MAPGROUND_COLOR_BORDER = 0xd4d1d0;
		int MAPGROUND_COLOR_WIDTH = 5;

		int MAPTEXT_COLOR_FILL = 0xff979797;// 地图文字 0xff979797
		int MAPTEXT_SIZE = 10;

		/**
		 * 无效区域颜色样式，此属性已经实例化，请直接调用方法设置颜色
		 */
		MapView.MAPINVALID.setColorfill(MAPINVALID_COLOR_FILL);
		MapView.MAPINVALID.setColorborder(MAPINVALID_COLOR_BORDER);
		/**
		 * 未知区域颜色样式，此属性已经实例化，请直接调用方法设置颜色
		 */
		MapView.MAPUNKNOWN.setColorfill(MAPUNKNOWN_COLOR_FILL);
		MapView.MAPUNKNOWN.setColorborder(MAPUNKNOWN_COLOR_BORDER);
		/**
		 * 店铺颜色样式，此属性已经实例化，请直接调用方法设置颜色
		 */
		MapView.MAPPOI.setColorfill(MAPPOI_COLOR_FILL);
		MapView.MAPPOI.setColorborder(MAPPOI_COLOR_BORDER);
		/**
		 * 卫生间颜色样式，此属性已经实例化，请直接调用方法设置颜色
		 */
		MapView.MAPWC.setColorfill(MAPWC_COLOR_FILL);
		MapView.MAPWC.setColorborder(MAPWC_COLOR_BORDER);
		/**
		 * 通行设施：电梯，楼梯，扶梯颜色样式，此属性已经实例化，请直接调用方法设置颜色
		 */
		MapView.MAPSTAIRS.setColorfill(MAPSTAIRS_COLOR_FILL);
		MapView.MAPSTAIRS.setColorborder(MAPSTAIRS_COLOR_BORDER);
		/**
		 * 地图地面颜色样式，此属性已经实例化，请直接调用方法设置颜色
		 */
		MapView.MAPGROUND.setColorfill(MAPGROUND_COLOR_FILL);
		MapView.MAPGROUND.setColorborder(MAPGROUND_COLOR_BORDER);
		MapView.MAPGROUND.setWidthborder(MAPGROUND_COLOR_WIDTH);
		/**
		 * 地图文字样式，此属性已经实例化，请直接调用方法设置颜色
		 */
		MapView.MAPTEXT.setTextcolor(MAPTEXT_COLOR_FILL);
		MapView.MAPTEXT.setTextsize(context, MAPTEXT_SIZE);
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
	}

	@Override
	protected void initData(Bundle savedInstanceState) {
		RMlbsUtils.getInstance().initMap(mvRtm, getActivity(), handler);
		Bundle bundle = getArguments();
		buildInfo = (BuildInfo) bundle.getSerializable("data");
		boolean isFollow = bundle.getBoolean("follow");
		if (buildInfo == null) {
			return;
		}
		tvBuildName.setText(buildInfo.getBuildName());
		initLayers();
		if (!isFollow)
			switchFloor(buildInfo.getBuildId(), buildInfo.getFloorlist().get(0)
					.getFloor());
		mvRtm.initScale();
	}

	private POILayer mPoiLayer;
	private RouteLayer mRouteLayer;
	private TapPOILayer mTapPOILayer;

	private void initLayers() {
		mPoiLayer = new POILayer(mvRtm);
		mPoiLayer.setOnPOIDrawListener(new POILayer.OnPOIDrawListener() {
			@Override
			public Bitmap onPOIDraw(POI poi) {
				return BitmapFactory.decodeResource(getResources(),
						R.drawable.da_marker_red);
			}
		});
		mvRtm.addMapLayer(mPoiLayer);

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
		mvRtm.clearLayer();
		mvRtm.removeAllViews();
		mvRtm.destroyLayer();
		mvRtm = null;
		RtMapLocManager.instance().removeReceiver(this);
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

	private Location mCurrentLocation;

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
			// getLocBuildDetail("862300010010300013");
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
		dismissLoading();
		if (!this.isDetached()) {
			buildInfo = result;
			tvBuildName.setText(buildInfo.getBuildName());
			switchFloor(buildInfo.getBuildId(), mCurrentLocation.getFloor());
		}
	}

	@Override
	public void onGetNavigationFinish(RMRoute result) {
		dismissLoading();
		if (result != null) {
			mPoiLayer.clearLayer();
			mTapPOILayer.clearLayer();
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

	@Override
	public void onRtMapLocListenerReceiver(RMLocation rmLocation,
			boolean isFollowing) {

		// try {
		// tvBuildName.setText("error: " + rmLocation.error);
		// } catch (Exception e) {
		// ToastUtils.shortToast("RMLocation null");
		// }
		if (rmLocation == null || rmLocation.error != 0) {
			return;
		}
		// tvBuildName.setText(rmLocation.getInOutDoorFlg() + "  " +
		// rmLocation.getFloor()+"  x:"+rmLocation.getX()+" y:"+rmLocation.getY());
		// tvBuildName.setText(rmLocation.getInOutDoorFlg() + "  " +
		// rmLocation.getFloor()+"  id:"+rmLocation.getBuildID());

		if (rmLocation.getInOutDoorFlg() == RMLocation.LOC_OUTDOOR
				&& isFollowing) {
			finishActivity();
			return;
		}
		String locateFloor = RMlbsUtils.getInstance().getFloorById(
				rmLocation.getFloorID());
		String locateBuildId = rmLocation.getBuildID();

		// if (!locateBuildId.equals(mvRtm.getBuildId())) {//
		// 如果当前地图显示的buildID与定位buildId不同，则不显示定位点
		// // 提示切换建筑&& !mIsChangeBuild
		// return;
		// }
		// 判断是否显示当前位置点
		float x = rmLocation.getCoordX() / 1000;
		float y = rmLocation.getCoordY() / 1000;
		mCurrentLocation = new Location(x, y, locateFloor);
		mCurrentLocation.setBuildId(locateBuildId);

		if (!locateFloor.equals(mvRtm.getFloor())
				|| !locateBuildId.equals(mvRtm.getBuildId())) {

			if (RtMapLocManager.instance().isFollowing()) {
				if (!locateBuildId.equals(mvRtm.getBuildId())
						|| (buildInfo != null && !buildInfo.getBuildId()
								.equals(locateBuildId))) {
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
		mvRtm.setMyCurrentLocation(mCurrentLocation, RtMapLocManager.instance()
				.isFollowing(), 2);
	}

	/***
	 * 获取定位建筑的详情
	 * 
	 * @param buildId
	 */
	private void getLocBuildDetail(String buildId) {
		// 如果要请求的建筑ID，已经在错误map里，则不请求
		for (int i = 0; i < mCityInfo.getBuildlist().size(); i++) {
			BuildInfo info = mCityInfo.getBuildlist().get(i);
			if (info.getBuildId().equals(buildId)) {
				onGetBuildDetailFinish(info);
				break;
			}
		}
	}
}
