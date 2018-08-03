package com.example.fragmentdemo.fragment;

import android.view.View;

import com.example.fragmentdemo.R;
import com.example.fragmentdemo.core.MTBaseFragment;
import com.example.fragmentdemo.util.MTUIUtils;

public class MTHomePageFragment extends MTBaseFragment {
	@Override
	protected View createLoadedView() {
		View view = MTUIUtils.inflate(R.layout.mt_home);
		return view;
	}

	@Override
	public String getPageName() {
		return null;
	}
}
