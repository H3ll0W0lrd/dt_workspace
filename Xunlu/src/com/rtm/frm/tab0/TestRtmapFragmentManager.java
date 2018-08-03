package com.rtm.frm.tab0;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

import com.rtm.common.model.POI;
import com.rtm.frm.R;
import com.rtm.frm.XunluApplication;
import com.rtm.frm.database.DBOperation;
import com.rtm.frm.dialogfragment.LoadingFragment;
import com.rtm.frm.fragment.controller.BaseManager;
import com.rtm.frm.fragment.controller.MyFragmentManager;
import com.rtm.frm.map.XunluMap;
import com.rtm.frm.model.Floor;
import com.rtm.frm.model.MyLocation;
import com.rtm.frm.model.RMRoute;
import com.rtm.frm.newframe.NewFrameActivity;
import com.rtm.frm.utils.ConstantsUtil;
import com.rtm.frm.utils.RMNavigationUtil;
import com.rtm.frm.utils.RMNavigationUtil.OnNavigationListener;
import com.rtm.frm.utils.ToastUtil;
import com.rtm.frm.utils.XunluUtil;

/**
 * ClassName: TestRtmapFragmentManager date: 2014-9-5 上午11:32:23
 * 
 * @explain 
 *          主要管理与rtmap相关的Fragment，rtmapFragment,poidetailFragment，floorListFragment
 * @author liyan
 * @version
 */
@SuppressLint("HandlerLeak")
public class TestRtmapFragmentManager extends BaseManager {

	private static TestRtmapFragmentManager instance;

	// 导航起终点
	public POI navRouteStartPoi;
	public POI navRouteEndPoi;

	public static TestRtmapFragmentManager getInstance() {
		if (instance == null) {
			instance = new TestRtmapFragmentManager(XunluApplication.mApp);
		}
		return instance;
	}

	protected TestRtmapFragmentManager(XunluApplication app) {
		super(app);
	}

	@Override
	protected void initManager() {

	}

	@Override
	protected void DestroyManager() {
		instance = null;
	}

	/**
	 * @explain 请求导航数据
	 * @param handler
	 * @param what
	 * @param buildId
	 * @param start
	 * @param end
	 */
	private void postNavRoutePoint(String buildId, POI start, POI end) {
		navLoadingFragment = new LoadingFragment(R.string.loading);
		MyFragmentManager.showFragmentdialog(navLoadingFragment,
				MyFragmentManager.PROCESS_DIALOGFRAGEMENT_LOADING,
				MyFragmentManager.DIALOGFRAGMENT_LOADING);
		RMNavigationUtil.requestNavigation(XunluMap.getInstance().getApiKey(),
				buildId, start, end, null, new OnNavigationListener() {

					@Override
					public void onFinished(RMRoute route) {
						if (route.getError_code() == 0) {
							postNavRouteResult(route);
						} else {
							ToastUtil.shortToast(route.getError_msg());
						}
					}
				});
	}

	private LoadingFragment navLoadingFragment;

	public void dismissLoading() {
		if (navLoadingFragment != null && navLoadingFragment.isAdded()) {
			navLoadingFragment.dismiss();
			navLoadingFragment = null;
		}
	}

	/**
	 * @explain 请求导航服务器接口，返回的数据
	 * @param result
	 */
	private void postNavRouteResult(RMRoute result) {
		TestRtmapFragment fragment = NewFrameActivity.getInstance().getTab0();
		if (fragment != null) {
			fragment.showNavLine(result, navRouteStartPoi, navRouteEndPoi);
		}
		navRouteStartPoi = null;
		navRouteEndPoi = null;
		if (!fragment.isOnSaveInstance) {
			dismissLoading();
		}
	}

	/**
	 * 设置导航起点
	 */
	public void setNavStartPoint(POI poi) {
		// 设置导航起点
		navRouteStartPoi = poi;
		TestRtmapFragment fragment = NewFrameActivity.getInstance().getTab0();
		if (fragment != null) {
			navRouteStartPoi = poi;
			if (navRouteEndPoi != null
					&& navRouteStartPoi != null
					&& navRouteStartPoi.getName().equals(
							navRouteEndPoi.getName())
					&& navRouteStartPoi.getFloor().equals(
							navRouteEndPoi.getFloor())) {
				ToastUtil.shortToast("起点和终点，不能为同一位置");
				navRouteStartPoi = null;
				return;
			}
			// 地图上放入起点图标
			if (fragment.getMapView() != null) {
				// 修改成新的地图之后需要手动刷新地图才能显示出来
				fragment.getMapView().refreshMap();
			}

			if (navRouteEndPoi != null) {
				// 请求导航数据点
				postNavRoutePoint(fragment.mMapShowBuildId, navRouteStartPoi,
						navRouteEndPoi);
			}
		}
	}

	/**
	 * 设置导航终点
	 */
	public void setNavEndPoint(POI poi) {
		// 设置导航终点
		navRouteEndPoi = poi;
		TestRtmapFragment fragment = NewFrameActivity.getInstance().getTab0();
		if (fragment != null) {
			// 地图上放入终点图标
			navRouteEndPoi = poi;
			if (navRouteEndPoi != null
					&& navRouteStartPoi != null
					&& navRouteStartPoi.getName().equals(
							navRouteEndPoi.getName())
					&& navRouteStartPoi.getFloor().equals(
							navRouteEndPoi.getFloor())) {
				ToastUtil.shortToast("起点和终点，不能为同一位置");
				navRouteEndPoi = null;
				return;
			}
			if (fragment.getMapView() != null) {
				// 修改成新的地图之后需要手动刷新地图才能显示出来
				fragment.getMapView().refreshMap();
			}

			if (navRouteStartPoi != null) {
				// 请求导航数据点
				postNavRoutePoint(fragment.mMapShowBuildId, navRouteStartPoi,
						navRouteEndPoi);
			} else {
				if (fragment.mMapShowBuildId.equals(MyLocation.getInstance()
						.getBuildId())) {
					navRouteStartPoi = new POI(-1, "我的位置", MyLocation
							.getInstance().getBuildId(), MyLocation
							.getInstance().getFloor(), MyLocation.getInstance()
							.getX(), MyLocation.getInstance().getY());
					// 修改成新的地图之后需要手动刷新地图才能显示出来
					fragment.getMapView().refreshMap();
					postNavRoutePoint(fragment.mMapShowBuildId,
							navRouteStartPoi, navRouteEndPoi);
				}
			}
		}
	}

	/**
	 * @explain 根据建筑物ID获取楼层信息
	 * @param buildId
	 * @param isPrivate
	 *            是否为私有建筑
	 * @return 楼层列表
	 */
	public List<Floor> queryFloorsByBuildId(String buildId) {
		List<Floor> floors = new ArrayList<Floor>();
		floors = DBOperation.getInstance().queryFloorByBuildId(buildId);
		return floors;
	}

	public void clearFragmentMangerPoint() {
		navRouteEndPoi = null;
		navRouteStartPoi = null;
	}

}
