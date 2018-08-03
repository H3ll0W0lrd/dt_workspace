package com.rtm.frm.fragment.map;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.rtm.common.utils.Constants;
import com.rtm.frm.R;
import com.rtm.frm.adapter.NavFloorAdapter;
import com.rtm.frm.fragment.BaseFragment;
import com.rtm.frm.fragment.controller.MyFragmentManager;
import com.rtm.frm.map.RouteLayer;
import com.rtm.frm.model.NavigatePoint;
import com.rtm.frm.model.RMRoute;
import com.rtm.frm.view.NavTitle;

/**
 * @author liyan
 * @explain 显示跨楼层导航缩略图片
 */
public class NavImgFragment extends BaseFragment implements OnClickListener {

	private TextView infoView;
	private ListView mNavFloorList;
	private Button closeButton;
	private RMRoute navigatePointModel;
	private RouteLayer mRouteLayer;
	private NavFloorAdapter mNavFloorAdapter;
	private NavTitle mNavTitle;
	private boolean mIsShowButton;

	public NavImgFragment(RMRoute navigateModel, RouteLayer layer,
			boolean isShowBtn) {
		navigatePointModel = navigateModel;
		mRouteLayer = layer;
		mIsShowButton = isShowBtn;
		// setStyle(DialogFragment.STYLE_NORMAL,R.style.dialogfragment_transparent_bg);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_nav_img, container,
				false);
		mNavFloorList = (ListView) rootView.findViewById(R.id.nav_floor_list);
		return rootView;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initViews(view);
	}

	private void initViews(View view) {
		mNavTitle = (NavTitle) view.findViewById(R.id.nav_title);
		mNavTitle.setTitleText("导航信息");
		infoView = (TextView) view.findViewById(R.id.nav_info);
		ArrayList<NavigatePoint> l = navigatePointModel.getPointlist();
		String textString = "从" + l.get(0).getAroundPoiName() + "至"
				+ l.get(l.size() - 1).getAroundPoiName() + "全程"
				+ (int) (navigatePointModel.getDistance() / 1000) + "米";
		infoView.setText(textString);
		closeButton = (Button) view.findViewById(R.id.btn_nav_close);
		closeButton.setOnClickListener(this);
		if (!mIsShowButton) {
			closeButton.setVisibility(View.GONE);
		}
		ArrayList<String> floorlist = new ArrayList<String>();
		for (int i = 0; i < l.size(); i++) {
			if (!floorlist.contains(l.get(i).getFloor())) {
				floorlist.add(l.get(i).getFloor());
			}
		}
		mNavFloorAdapter = new NavFloorAdapter(mRouteLayer, floorlist);
		mNavFloorList.setAdapter(mNavFloorAdapter);
	}

	private boolean isStartNav = false;

	@Override
	public void onDestroyView() {
		mNavFloorAdapter.recycleBitmap();
		if (!isStartNav && mIsShowButton) {
			List<String> flags = new ArrayList<String>();
			flags.add(MyFragmentManager.PROCESS_NAV_FLOOR_CHANGE + "-"
					+ MyFragmentManager.FRAGMENT_NAV_FLOOR_CHANGE);
			MyFragmentManager.getInstance().backFragmentByFlags(flags);
		}

		super.onDestroyView();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_nav_close:
			isStartNav = true;
			MyFragmentManager.getInstance().backFragment();
			// this.dismiss();
			break;
		}
	}
}
