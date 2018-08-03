package com.rtm.frm.tab1;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.rtm.common.model.POI;
import com.rtm.frm.R;
import com.rtm.frm.XunluApplication;
import com.rtm.frm.database.DBOperation;
import com.rtm.frm.fragment.BaseFragment;
import com.rtm.frm.fragment.controller.MyFragmentManager;
import com.rtm.frm.map.CompassLayer;
import com.rtm.frm.map.MapView;
import com.rtm.frm.map.POILayer;
import com.rtm.frm.map.RouteLayer;
import com.rtm.frm.map.TapPOILayer;
import com.rtm.frm.map.TapPOILayer.OnPOISelectedListener;
import com.rtm.frm.map.XunluMap;
import com.rtm.frm.model.Build;
import com.rtm.frm.model.FavorablePoiDbModel;
import com.rtm.frm.view.NavTitle;

public class ThumbnailMapFragment extends BaseFragment {

	// 地图
	private MapView mMapView;

	private POILayer mPoiLayer;

	public TapPOILayer mTapPOILayer;

	private CompassLayer mCompassLayer;// 指南针
	public RouteLayer mRouteLayer;// 导航

	private FavorablePoiDbModel mFavorablePoi;

	private NavTitle mNavTitle;

	private final String KEY_ON_SAVE_INSTANCE_FAV_POI = "key_on_save_instance_fav_poi";

	// listener
	private OnPOISelectedListener mOnPOISelectedListener = new OnPOISelectedListener() {

		@Override
		public void onPOISelected(POI poi) {
		}
	};

	public ThumbnailMapFragment() {

	}

	public ThumbnailMapFragment(FavorablePoiDbModel poi) {
		mFavorablePoi = poi;
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		if (savedInstanceState != null) {
			mFavorablePoi = (FavorablePoiDbModel) savedInstanceState
					.getSerializable(KEY_ON_SAVE_INSTANCE_FAV_POI);
		}

		return inflater.inflate(R.layout.fragment_thumbnail_map, null);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initViews(view);
	}

	public void initViews(View view) {
		mNavTitle = (NavTitle) view.findViewById(R.id.nav_title);
		mNavTitle.setLeftOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {// 搜索楼层
				MyFragmentManager.getInstance().backFragment();
			}
		});
		mNavTitle.unRegisterReceiver();

		XunluMap.getInstance().init(XunluApplication.mApp);
		mMapView = (MapView) view.findViewById(R.id.test_map_view);

		/* layer */
		mTapPOILayer = new TapPOILayer(mMapView);
		mMapView.addMapLayer(mTapPOILayer);
		mPoiLayer = new POILayer(mMapView);
		mMapView.addMapLayer(mPoiLayer);

		mCompassLayer = new CompassLayer(mMapView);
		mMapView.addMapLayer(mCompassLayer);
		mRouteLayer = new RouteLayer(mMapView);
		mMapView.addMapLayer(mRouteLayer);

		mTapPOILayer.setOnPOISelectedListener(mOnPOISelectedListener);

		POI poi = new POI(Integer.valueOf(mFavorablePoi.poiId),
				mFavorablePoi.poiName, mFavorablePoi.buildId,
				mFavorablePoi.floor, Float.valueOf(mFavorablePoi.poiX),
				Float.valueOf(mFavorablePoi.poiY));
		poi.setPoiImage(mFavorablePoi.adBigUrl);
		poi.setLogoImage(mFavorablePoi.adUrl);
		poi.setDesc(mFavorablePoi.discription);

		Build build = DBOperation.getInstance().queryBuildById(
				mFavorablePoi.buildId);
		String buildName = "";
		if (build != null) {
			buildName = build.name;
		}
		showSearchPoiPoint(poi, buildName);
	}

	/**
	 * 显示搜索后的poi点
	 * 
	 * @param poi
	 * @param buildName
	 *            如果buildName为空，则默认将poi点显示在当前地图建筑物，否则显示其他建筑物
	 */
	public void showSearchPoiPoint(POI poi, String buildName) {
		ArrayList<POI> pois = new ArrayList<POI>();
		pois.add(poi);
		switchBuild(buildName, poi.getBuildId(), poi.getFloor(), true);
		// 默认让搜索出来的第一个poi变亮
		mPoiLayer.addPoiList(pois);// pois，poi列表，0，第几个poi设置为地图中心店
		mMapView.setCenter(poi.getX(), poi.getY());
		mMapView.refreshMap();
		mMapView.getTapPOILayer().setDisableTap(true);
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
	public void switchBuild(String buildName, String buildId, String floor,
			boolean isInitScale) {
		mNavTitle.setTitleText(buildName + " " + mFavorablePoi.floor);

		// 清空地图所有图层数据
		mMapView.resetscale();// 重新加载地图
		mMapView.initMapConfig(buildId, floor);
		if (isInitScale) {
			mMapView.initScale();//
		}
		mMapView.refreshMap();// 刷新地图
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(KEY_ON_SAVE_INSTANCE_FAV_POI, mFavorablePoi);
		super.onSaveInstanceState(outState);
	}
}