package com.rtm.frm.fragment.controller;

import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.Fragment;
import android.util.Log;

import com.rtm.frm.R;
import com.rtm.frm.utils.ToastUtil;
import com.rtm.frm.utils.XunluUtil;



/**
 * @author liyan
 * fragment manager 数组管理类,每一个tab都是一个manager 
 */
public class FragmentManagerTab  {
	
	private List<FragmentManagerTabItem> mFragmentManagers = new ArrayList<FragmentManagerTabItem>();
	
	private String mCurrentMainFlag;
	
	private static FragmentManagerTab instance;
	
	private static int containerId = R.id.tab_frame_lay;//需要事先写好
	
	private OnFragmentTabChangeListener changeListener;
	
	private int mCurrentIndex = -1;
	
	public interface OnFragmentTabChangeListener {
		public void onFragmentTabChanged(int currentIndex,int lastIndex);
	}
	
	public static FragmentManagerTab getInstance() {
		if(instance == null) {
			instance = new FragmentManagerTab();
		}
		if(containerId == -1) {
			Log.e("FragmentManagerTab", "containerId  isn't set", new Throwable("containerId  isn't set"));
		}
		return instance;
	}
	
	public void addFragmentManagerTabItem(FragmentManagerTabItem manager) {
		mFragmentManagers.add(manager);
	}
	
	public FragmentManagerTabItem getFragmentManagerTabItem(int index) {
		if(mFragmentManagers != null && mFragmentManagers.size()>0) {
			return mFragmentManagers.get(index);
		}
		return null;
	}
	
	public void removeFragmentManagerTabItem(FragmentManagerTabItem manager) {
		mFragmentManagers.remove(manager);
	}
	
	public void removeFragmentManagerTabItem(int index) {
		mFragmentManagers.remove(index);
	}
	
	public void setOnFragmentTabChangeListener(OnFragmentTabChangeListener lintener) {
		changeListener = lintener;
	}
	
	/**
	 * 显示指定manager的当前页面（处于显示状态）
	 * @param index
	 */
	public void showFragmentManagerTabItem(int index) {
		if(index == mCurrentIndex) {
			return;
		}
		mFragmentManagers.get(index).showCurrentItemFragment();
		if(changeListener == null) {
			ToastUtil.shortToast("您没有设置FragmentTabChangeListener");
		} else {
			changeListener.onFragmentTabChanged(index,mCurrentIndex);
			mCurrentIndex = index;
		}
	}
	
	public int getCurrentFragmentManagerIndex() {
		return mCurrentIndex;
	}
	
	public void showFragmentTab(int index,int p){
		showFragmentManagerTabItem(index);
	}
	

	public void showFragment(Fragment fragment,String process_flag,String fragment_flag) {
		if(!XunluUtil.isEmpty(mCurrentMainFlag)) {
			String hideProcess = mCurrentMainFlag.split("-")[0];
			String hideFlag = mCurrentMainFlag.split("-")[1];
			Fragment fragmentHeid = MyFragmentManager.getFragmentByFlag(hideProcess, hideFlag);
			MyFragmentManager.getInstance().getTransaction().hide(fragmentHeid).commitAllowingStateLoss();
		} 
		mCurrentMainFlag = process_flag + "-" + fragment_flag;
		Fragment fragmentShow = MyFragmentManager.getFragmentByFlag(process_flag, fragment_flag);
		if(fragmentShow == null) {
			MyFragmentManager.getInstance().addFragment(containerId, fragment, process_flag, fragment_flag);
		} else {
			MyFragmentManager.getInstance().getTransaction().show(fragmentShow).commitAllowingStateLoss();
		}
	}
	
	public String getCurrentTabMainFlag() {
		return mCurrentMainFlag;
	}
	
	public void backTabFragment(String currentFlag,String backFlag) {
		mCurrentMainFlag = currentFlag;
		List<String> flags = new ArrayList<String>();
		flags.add(backFlag);
		MyFragmentManager.getInstance().backFragmentByFlags(flags);
		
		Fragment f = MyFragmentManager.getFragmentByFlag(currentFlag.split("-")[0], currentFlag.split("-")[1]);
		MyFragmentManager.getInstance().getTransaction().show(f).commitAllowingStateLoss();
	}
}
