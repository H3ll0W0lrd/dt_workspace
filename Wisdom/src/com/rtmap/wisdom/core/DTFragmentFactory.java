package com.rtmap.wisdom.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.rtmap.wisdom.fragment.WDMainFragment;
import com.rtmap.wisdom.util.DTLog;

public class DTFragmentFactory {
	public static final int TAB_MAP = 0;
	public static final int TAB_BUILD = 1;
	public static final int TAP_SEARCH = 2;
	/** 记录所有的fragment，防止重复创建 */
	private static Map<Integer, DTBaseFragment> mFragmentMap = new HashMap<Integer, DTBaseFragment>();

	/** 采用工厂类进行创建Fragment，便于扩展，已经创建的Fragment不再创建 */
	public static DTBaseFragment createFragment(int index) {
		DTBaseFragment fragment = mFragmentMap.get(index);
		
		if (fragment == null) {
			DTLog.e("index : "+index);
			switch (index) {
				case TAB_MAP:
					fragment = new WDMainFragment();
					break;
				case TAB_BUILD:
					fragment = new WDMainFragment();
					break;
				case TAP_SEARCH:
					fragment = new WDMainFragment();
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
			DTBaseFragment fragment = mFragmentMap.get(i);
			fragment.onDetach();
		}
		mFragmentMap.clear();
	}
}
