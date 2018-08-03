package com.example.fragmentdemo.core;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.fragmentdemo.util.MTStringUtils;
import com.example.fragmentdemo.util.MTViewUtils;

public abstract class MTBaseFragment extends Fragment {
	private View mView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// 每次ViewPager要展示该页面时，均会调用该方法获取显示的View
		if (mView == null)
			mView = createLoadedView();
		MTViewUtils.removeSelfFromParent(mView);
		return mView;
	}

//	@Override
//	public void onResume() {
//		super.onResume();
//		if (!MTStringUtils.isEmpty(getPageName()))
//			MobclickAgent.onPageStart(getPageName()); // 统计页面
//	}
//
//	@Override
//	public void onPause() {
//		super.onPause();
//		if (!MTStringUtils.isEmpty(getPageName()))
//			MobclickAgent.onPageEnd(getPageName());// 统计页面
//	}

	/**
	 * 设置页面名字 用于友盟统计
	 */
	public abstract String getPageName();
	/** 加载完成的View */
	protected abstract View createLoadedView();
}
