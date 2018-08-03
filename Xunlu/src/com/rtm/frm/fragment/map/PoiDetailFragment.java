package com.rtm.frm.fragment.map;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rtm.common.model.POI;
import com.rtm.frm.R;
import com.rtm.frm.fragment.BaseFragment;
import com.rtm.frm.fragment.controller.MyFragmentManager;
import com.rtm.frm.tab0.TestRtmapFragmentManager;
import com.rtm.frm.view.NavTitle;
import com.umeng.analytics.MobclickAgent;

/**
 * @author liyan
 * @explain poi详情
 */
public class PoiDetailFragment extends BaseFragment implements OnClickListener,OnTouchListener{
	
	private POI mPOI;
	
	private TextView mStartButton;
	
	private TextView mEndButton;
	
	private NavTitle mNavTitle;
	
	private RelativeLayout mDetaiLayout;

	public PoiDetailFragment(POI poi) {
		mPOI = poi;
//		setStyle(DialogFragment.STYLE_NORMAL,R.style.dialogfragment_transparent_bg);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_poi_detail, container,
				false);
		return rootView;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initViews(view);
	}
	
	private void initViews(View view) {
		mNavTitle = (NavTitle) view.findViewById(R.id.nav_title);
		mNavTitle.setTitleText(mPOI.getName());
		mStartButton = (TextView) view.findViewById(R.id.button_start);
		mStartButton.setVisibility(View.GONE);
		mStartButton.setOnClickListener(this);
		mEndButton = (TextView) view.findViewById(R.id.button_end);
		mEndButton.setOnClickListener(this);
		mNavTitle.setLeftOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MyFragmentManager.getInstance().backFragment();
			}
		});
//		mNavTitle.setRightOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				TestRtmapFragment fragment = NewFrameActivity.getInstance().getTab0();
//				ShareUtil.shareMessage(
//						getString(R.string.message_share_poi, fragment.getMapShowBuildName(),
//								mPOI.getFloor(), mPOI.getName()), mPOI
//								.getName(),getActivity());
//				MyFragmentManager.getInstance().backFragment();
//			}
//		});
		mDetaiLayout = (RelativeLayout) view.findViewById(R.id.poi_detail_lay);
		mDetaiLayout.setOnTouchListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_start:
			MobclickAgent.onEvent(mContext,"event_click_indoor_item_startpoint");
			
			TestRtmapFragmentManager.getInstance().setNavStartPoint(mPOI);
			MyFragmentManager.getInstance().backFragment();
			break;
		case R.id.button_end:
			MobclickAgent.onEvent(mContext,"event_click_indoor_item_endpoint");
			
			TestRtmapFragmentManager.getInstance().setNavEndPoint(mPOI);
			MyFragmentManager.getInstance().backFragment();
			break;
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if(event.getAction() == MotionEvent.ACTION_DOWN) {
			switch (v.getId()) {
			case R.id.poi_detail_lay:
				MyFragmentManager.getInstance().backFragment();
				return true;
			}
		}
		return false;
	}
}


