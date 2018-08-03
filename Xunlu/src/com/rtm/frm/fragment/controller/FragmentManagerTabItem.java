package com.rtm.frm.fragment.controller;

import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.Fragment;

import com.rtm.frm.fragment.BaseFragment;


/**
 * @author liyan
 * fragment manager 数组管理类,每一个tab都是一个manager 
 */
public class FragmentManagerTabItem  {
	protected String itemCurrentFragmentFlag = "";
	protected int currentIndex;
	protected List<String> mTabItemFlags = new ArrayList<String>();
	
	/**
	 * @explain 该方法只用由FragmentManagerTab调用
	 */
	public void showCurrentItemFragment() {
		String process_flag  = itemCurrentFragmentFlag.split("-")[0];
		String fragment_flag  = itemCurrentFragmentFlag.split("-")[1];
		Fragment fragmentCurrent = MyFragmentManager.getFragmentByFlag(process_flag, fragment_flag);
		showItemFragment(fragmentCurrent, process_flag, fragment_flag);
	}
	
	public String getCurrFragmentFlag(){
		return itemCurrentFragmentFlag;
	}
	
	public void showItemFragment(Fragment fragment,String process_flag,String fragment_flag) {
		if(!itemCurrentFragmentFlag.equals(process_flag + "-" + fragment_flag)) {
			mTabItemFlags.add(process_flag + "-" + fragment_flag);
		}
		itemCurrentFragmentFlag = process_flag + "-" + fragment_flag;
		if(fragment!=null &&((BaseFragment) fragment)!=null){
			((BaseFragment) fragment).setTabFragmentBoolean(true);
		}
		FragmentManagerTab.getInstance().showFragment(fragment, process_flag, fragment_flag);
	}
	
	public void backTabFragment() {
		if(mTabItemFlags.size() > 1) {
			itemCurrentFragmentFlag = mTabItemFlags.get(mTabItemFlags.size() - 2);
			
			FragmentManagerTab.getInstance().backTabFragment(itemCurrentFragmentFlag, mTabItemFlags.get(mTabItemFlags.size() - 1));
			
			mTabItemFlags.remove(mTabItemFlags.get(mTabItemFlags.size() - 1));
		}
	}
}
