/**
 * @author hukunge
 * @date 2014.09.02
 */
package com.rtm.frm.tab2;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.rtm.frm.R;
import com.rtm.frm.fragment.BaseFragment;
import com.rtm.frm.fragment.BaseFragment.OnFinishListener;
import com.rtm.frm.fragment.controller.MyFragmentManager;
import com.rtm.frm.newframe.NewFrameActivity;
import com.umeng.analytics.MobclickAgent;

@SuppressLint("InflateParams")
public class Tab2Fragment extends BaseFragment implements
		View.OnClickListener, OnFinishListener {
	View contentView;
	RelativeLayout relItemSetting;
	RelativeLayout relItemAbout;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		contentView = View.inflate(mContext,R.layout.fragment_tab2, null);
		initView(contentView);
		return contentView;
	}

	private void initView(View v) {
		relItemSetting = (RelativeLayout)v.findViewById(R.id.tab2_item1);
		relItemAbout = (RelativeLayout)v.findViewById(R.id.tab2_item2);
		
		relItemSetting.setOnClickListener(this);
		relItemAbout.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tab2_item1:
			MobclickAgent.onEvent(getActivity(),"event_click_setting");
			
			MyFragmentManager.getInstance().replaceFragment(
					NewFrameActivity.ID_ALL, 
					new TabSettingFragment(), 
					MyFragmentManager.PROCESS_SETTING,
					MyFragmentManager.FRAGMENT_SETTING);
			break;
		case R.id.tab2_item2:
			MobclickAgent.onEvent(mContext,"event_click_about");
			
			MyFragmentManager.getInstance().replaceFragment(
					NewFrameActivity.ID_ALL, 
					new TabAboutFragment(), 
					MyFragmentManager.PROCESS_ABOUT,
					MyFragmentManager.FRAGMENT_ABOUT);
			break;
		}
	}

	@Override
	public void onFinish(String flag, Bundle data) {
	}
}