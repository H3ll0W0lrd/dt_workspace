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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rtm.frm.R;
import com.rtm.frm.fragment.BaseFragment;
import com.rtm.frm.fragment.BaseFragment.OnFinishListener;
import com.rtm.frm.fragment.controller.MyFragmentManager;
import com.rtm.frm.utils.XunluUtil;

@SuppressLint("InflateParams")
public class TabAboutFragment extends BaseFragment implements
		View.OnClickListener, OnFinishListener {
	View contentView;
	RelativeLayout relItemSetting;
	RelativeLayout relItemAbout;
	LinearLayout linBack;
	TextView tvVision;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		contentView = inflater.inflate(R.layout.fragment_tab_about, container,
				false);
		initView(contentView);
		return contentView;
	}

	private void initView(View v) {
		linBack = (LinearLayout)v.findViewById(R.id.lin_back);
		linBack.setOnClickListener(this);
		
		tvVision = (TextView)v.findViewById(R.id.tv_vision);
		try {
			tvVision.setText("RTMAP V"+XunluUtil.getVersionName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.lin_back:
			MyFragmentManager.getInstance().backFragment();
			break;
		}
	}

	@Override
	public void onFinish(String flag, Bundle data) {
	}
}