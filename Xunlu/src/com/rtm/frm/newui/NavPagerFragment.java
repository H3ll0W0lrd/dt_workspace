package com.rtm.frm.newui;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.rtm.frm.R;
import com.rtm.frm.adapter.NavViewPagerAdapter;
import com.rtm.frm.fragment.BaseFragment;
import com.rtm.frm.fragment.controller.MyFragmentManager;
import com.rtm.frm.fragment.map.NavImgFragment;
import com.rtm.frm.map.MapView;
import com.rtm.frm.map.RouteLayer;
import com.rtm.frm.model.NavigatePoint;
import com.rtm.frm.model.RMRoute;
import com.rtm.frm.newframe.NewFrameActivity;
import com.rtm.frm.stack.BackStackManager.OnBackStackChangedListener;
import com.rtm.frm.tab0.TestRtmapFragment;
import com.rtm.frm.utils.ToastUtil;
import com.rtm.frm.view.NavTitle;
import com.umeng.analytics.MobclickAgent;
import com.umeng.common.net.s;

@SuppressLint("InflateParams")
public class NavPagerFragment extends BaseFragment implements OnClickListener,
		OnBackStackChangedListener, OnPageChangeListener, OnTouchListener {

	private ViewPager mViewPager;

	private RouteLayer mRouteLayer;

	private LayoutInflater mLayoutInflater;

	private String mBuildId;

	private String mBuildName;

	private RMRoute mModel;

	private NavTitle mNavTitle;

	private NavViewPagerAdapter mPagerAdapter;

	private List<View> mViews = new ArrayList<View>();

	private MapView mMapView;
	private boolean mIsShowRightBtn;

	public NavPagerFragment(MapView mapview, RouteLayer routeLayer,
			String buildId, String buildName, RMRoute model,
			boolean isShowRightBtn) {
		mRouteLayer = routeLayer;
		mModel = model;
		mBuildId = buildId;
		mBuildName = buildName;
		mIsShowRightBtn = isShowRightBtn;
		mMapView = mapview;
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		mLayoutInflater = inflater;

		return inflater.inflate(R.layout.fragment_nav_pager, null);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		initViews(view);
	}

	public void navi(Message msg) {
		int sign = msg.arg1;
		if (sign == RouteLayer.PLAN_ROUTE_START) {// 开始规划路线

		} else if (sign == RouteLayer.PLAN_ROUTE_ERROR) {// 规划路线请求服务器错误

		} else if (sign == RouteLayer.NAVIGATE_START) {// 开始导航

		} else if (sign == RouteLayer.NAVIGATE_CURRENT_POINT) {// 导航关键节点
			int distance = 0;
			for (int i = 0; i < mRouteLayer.getNavigateRoutePoints().size(); i++) {
				NavigatePoint p = mRouteLayer.getNavigateRoutePoints().get(i);
				if (p.isImportant())
					distance += p.getDistance();
			}
			mDistance.setText((distance / 1000 + 1) + "米");
			if (msg.obj != null) {
				NavigatePoint point = (NavigatePoint) msg.obj;
				// 1：直行，2：右前，3：右转，4：右后，5：左后，6：左转，
				// 7：左前，8：直梯上行，9：直梯下行，10：扶梯上行，11扶梯下行。
				if (point.getAction() == 1) {
					mNaText.setText("步行" + (point.getDistance() / 1000 + 1)
							+ "米\n在" + point.getAroundPoiName() + "直行");
				} else if (point.getAction() == 2) {
					mNaText.setText("步行" + (point.getDistance() / 1000 + 1)
							+ "米\n在" + point.getAroundPoiName() + "往右前方转");
				} else if (point.getAction() == 3) {
					mNaText.setText("步行" + (point.getDistance() / 1000 + 1)
							+ "米\n在" + point.getAroundPoiName() + "往右转");
				} else if (point.getAction() == 4) {
					mNaText.setText("步行" + (point.getDistance() / 1000 + 1)
							+ "米\n在" + point.getAroundPoiName() + "往右后方转");
				} else if (point.getAction() == 5) {
					mNaText.setText("步行" + (point.getDistance() / 1000 + 1)
							+ "米\n在" + point.getAroundPoiName() + "往左后方转");
				} else if (point.getAction() == 6) {
					mNaText.setText("步行" + (point.getDistance() / 1000 + 1)
							+ "米\n在" + point.getAroundPoiName() + "往左转");
				} else if (point.getAction() == 7) {
					mNaText.setText("步行" + (point.getDistance() / 1000 + 1)
							+ "米\n在" + point.getAroundPoiName() + "往左前方转");
				}
			}
		} else if (sign == RouteLayer.NAVIGATE_STOP) {// 结束导航

		} else if (sign == RouteLayer.ARRIVED) {// 到达终点
			Toast.makeText(getActivity(), "已到达终点附近", 5000).show();
			mMapView.clearLocationIconStyle();
		} else if (sign == RouteLayer.REPLAN_ROUTE_START) {// 已经偏离路线，重新规划
			Toast.makeText(getActivity(), "已偏离路线，重新规划", Toast.LENGTH_LONG)
					.show();
		} else if (sign == RouteLayer.NAVIGATE_FAIL) {// 导航无法开启

		}
	}

	TextView mDistance, mNaText;

	private void initViews(View contentView) {
		mNavTitle = (NavTitle) contentView.findViewById(R.id.nav_title);
		mNavTitle.setLeftOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				MyFragmentManager.getInstance().backFragment();
				NewFrameActivity.getInstance().getTab0().mPoiLayer
						.destroyLayer();
				mRouteLayer.stopNavigate();
				mMapView.setLocationIcon(R.drawable.default_location,
						R.drawable.default_location);
				mRouteLayer.destroyLayer();
			}
		});

		mNavTitle.setRightOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				NavImgFragment imgFragment = new NavImgFragment(mModel,
						mRouteLayer, false);
				MyFragmentManager.getInstance().addFragment(
						R.id.test_main_container, imgFragment,
						MyFragmentManager.PRCOCESS_DIALOGFRAGMENT_NAV_IMG,
						MyFragmentManager.DIALOGFRAGMENT_NAV_IMG);
			}
		});

		mNavTitle.setRightViewVisibility(false);

		mViewPager = (ViewPager) contentView.findViewById(R.id.view_pager);
		int sizeOfKeyPoints = mRouteLayer.getNavigatePoints().size();
		for (int i = 0; i < sizeOfKeyPoints; ++i) {
			View v = mLayoutInflater.inflate(R.layout.fragment_nav_pager_item,
					null);
			TextView step = (TextView) v.findViewById(R.id.nav_step);
			step.setText(i + 1 + "/" + sizeOfKeyPoints);

			NavigatePoint point = (NavigatePoint) mRouteLayer
					.getNavigatePoints().get(i);

			mNaText = (TextView) v.findViewById(R.id.nav_text);
			mNaText.setText(point.getAroundPoiName());
			mDistance = (TextView) v.findViewById(R.id.nav_distance);
			mDistance.setText("全程" + ((int) mModel.getDistance() / 1000) + "米");
			mViews.add(v);
		}

		mViewPager.setOnPageChangeListener(this);
		mPagerAdapter = new NavViewPagerAdapter(mViews, this);
		mViewPager.setAdapter(mPagerAdapter);

		TestRtmapFragment fragment = NewFrameActivity.getInstance().getTab0();
		fragment.switchBuild(mBuildName, mBuildId, mRouteLayer
				.getNavigatePoints().get(0).getFloor());
		fragment.getTapPOILayer().setDisableTap(true);
	}

	@Override
	public void onBackStackCleared() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onClick(View arg0) {
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageSelected(int position) {
		MobclickAgent.onEvent(mContext, "event_click_indoor_route_switch_step");

		if (!isChangeByMap) {
			TestRtmapFragment fragment = NewFrameActivity.getInstance()
					.getTab0();
			NavigatePoint p = mRouteLayer.getNavigatePoints().get(position);
			fragment.switchBuild(mBuildName, mBuildId, p.getFloor());
			fragment.cancelMapRouteFloowMode();
		}
		isChangeByMap = false;
	}

	@Override
	public void onDestroyView() {
		TestRtmapFragment fragment = NewFrameActivity.getInstance().getTab0();
		fragment.closeNav();
		fragment.getTapPOILayer().setDisableTap(false);
		super.onDestroyView();
	}

	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		// TODO Auto-generated method stub
		return true;
	}

	private boolean isChangeByMap = false;

	// private boolean isFirstChangeByMap = true;
	public void changeFloorByMap(String floor) {
		// if(isFirstChangeByMap) {
		// isFirstChangeByMap = false;
		// return;
		// }
		// int i = mViewPager.getCurrentItem();
		int i = 0;
		for (; i < mRouteLayer.getNavigatePoints().size(); ++i) {
			NavigatePoint point = (NavigatePoint) mRouteLayer
					.getNavigatePoints().get(i);
			if (point.getFloor().equals(floor)
					&& i != mViewPager.getCurrentItem()) {
				isChangeByMap = true;
				mViewPager.setCurrentItem(i);
				ToastUtil.shortToast("主动切换");
				break;
			}
		}
	}

}
