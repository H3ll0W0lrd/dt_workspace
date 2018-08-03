package com.rtmap.wisdom.core;

import com.google.gson.Gson;
import com.rtmap.wisdom.util.DTLog;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class DTBaseFragment extends Fragment {
	private View mView;
	public Gson mGson = new Gson();
	public SharedPreferences mShare = DTApplication.getInstance().getShare();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// 每次ViewPager要展示该页面时，均会调用该方法获取显示的View
		DTLog.e(this.toString()+"   "+System.currentTimeMillis());
		if (mView == null)
			mView = createLoadedView();
		DTLog.e(this.toString()+"   "+System.currentTimeMillis());
//		MTViewUtils.removeSelfFromParent(mView);
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
