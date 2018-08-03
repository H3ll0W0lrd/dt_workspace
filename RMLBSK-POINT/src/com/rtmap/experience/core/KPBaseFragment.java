package com.rtmap.experience.core;

import com.rtmap.experience.core.model.UserInfo;
import com.rtmap.experience.util.DTFileUtils;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class KPBaseFragment extends Fragment {
	private View mView;
	public UserInfo mUser;
	public ProgressDialog mLoadDialog;// 加载框

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// 每次ViewPager要展示该页面时，均会调用该方法获取显示的View
		mUser = new UserInfo();
		mUser.setKey(KPApplication.getInstance().getShare()
				.getString(DTFileUtils.PREFS_TOKEN, null));
		mUser.setPhone(KPApplication.getInstance().getShare()
				.getString(DTFileUtils.PHONE, ""));
		initLoad();
		if (mView == null)
			mView = createLoadedView();
		return mView;
	}

	/** 加载完成的View */
	protected abstract View createLoadedView();
	
	@Override
	public void onStop() {
		super.onStop();
		mLoadDialog.dismiss();
	}
	/**
	 * 初始化加载框
	 */
	private void initLoad() {
		mLoadDialog = new ProgressDialog(getActivity());// 加载框
		mLoadDialog.setCanceledOnTouchOutside(false);
		mLoadDialog.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				if (mLoadDialog.isShowing() && keyCode == KeyEvent.KEYCODE_BACK) {
					cancelLoadDialog();
					mLoadDialog.cancel();
				}
				return false;
			}
		});
	}
	public View getView() {
		return mView;
	}
	public void cancelLoadDialog() {
	}
}
