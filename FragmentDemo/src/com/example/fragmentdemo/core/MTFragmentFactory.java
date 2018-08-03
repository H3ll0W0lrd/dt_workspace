package com.example.fragmentdemo.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.example.fragmentdemo.fragment.MTHomePageFragment;
import com.example.fragmentdemo.fragment.MTOrderFragment;
import com.example.fragmentdemo.util.MTLog;

public class MTFragmentFactory {
	public static final int TAB_HOME = 0;
	public static final int TAB_ORDER = 1;
	/** 记录所有的fragment，防止重复创建 */
	private static Map<Integer, MTBaseFragment> mFragmentMap = new HashMap<Integer, MTBaseFragment>();

	/** 采用工厂类进行创建Fragment，便于扩展，已经创建的Fragment不再创建 */
	public static MTBaseFragment createFragment(int index) {
		MTBaseFragment fragment = mFragmentMap.get(index);
		
		if (fragment == null) {
			switch (index) {
				case TAB_HOME:
					MTLog.e("home");
					fragment = new MTHomePageFragment();
					break;
				case TAB_ORDER:
					MTLog.e("order");
					fragment = new MTOrderFragment();
					break;
			}
			mFragmentMap.put(index, fragment);
		}
		return fragment;
	}
	
	/**
	 * 清空fragment
	 */
	public static void clearFragment() {
		Set<Integer> keySet = mFragmentMap.keySet();
		for(Integer i:keySet){
			MTBaseFragment fragment = mFragmentMap.get(i);
			fragment.onDetach();
		}
		mFragmentMap.clear();
	}
}
