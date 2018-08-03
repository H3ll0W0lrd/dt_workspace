package com.rtmap.locationcheck.core;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class LCBaseFragment extends Fragment {
	private View mView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// 每次ViewPager要展示该页面时，均会调用该方法获取显示的View
		if (mView == null)
			mView = createLoadedView();
		return mView;
	}

	/** 加载完成的View */
	protected abstract View createLoadedView();
	
	public View getView() {
		return mView;
	}
}
