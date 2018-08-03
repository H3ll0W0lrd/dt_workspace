package com.rtm.frm.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.rtm.frm.fragment.controller.MyFragmentManager;

public abstract class BaseFragment extends Fragment {

	static final String FLAG = "flag";
	protected Context mContext;
	protected boolean isTabFragment = false;

	/**
	 * 获取Fragment的标记字符串,每个Fragment都是唯一的。 暂时不用。
	 */
	public String getFlagStr() {
		return "";
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity().getApplicationContext();

		Bundle args = getArguments();
		if (args != null) {
			MyFragmentManager.getFragmentFlagList().add(args.getString(FLAG));
		}
	}

	@Override
	public void onDestroy() {
		if (MyFragmentManager.getFragmentFlagList().size() != 0) {
			MyFragmentManager.getFragmentFlagList().remove(MyFragmentManager.getFragmentFlagList().size() - 1);
		}
		
		super.onDestroy();
	}

	/** Fragment在结束（隐藏）的时候向调用者回传数据 */
	public interface OnFinishListener {
		/**
		 * 回传数据
		 * 
		 * @param flag
		 *            结束的Fragment的flag
		 * @param data
		 *            数据包
		 */
		public void onFinish(String flag, Bundle data);
	}

	private OnFinishListener mFinishListener;

	/** 设置监听回调 */
	public void setOnFinishListener(OnFinishListener l) {
		mFinishListener = l;
	}

	/**
	 * 供子类调用，用以回传数据
	 * 
	 * @param data
	 *            要回传的数据包
	 */
	protected void callOnFinish(Bundle data) {
		if (mFinishListener != null) {
			mFinishListener.onFinish(getFlagStr(), data);
		}
	}

    public boolean isTabFragment(){
    	return isTabFragment;
    }
	
	public void setTabFragmentBoolean(boolean isTab) {
    	isTabFragment = isTab; 
    }
}
