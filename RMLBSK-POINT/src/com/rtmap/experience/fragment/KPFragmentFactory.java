package com.rtmap.experience.fragment;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.rtmap.experience.R;
import com.rtmap.experience.core.KPBaseFragment;
import com.rtmap.experience.page.KPMapListNewFragment;
import com.rtmap.experience.util.DTLog;

public class KPFragmentFactory {
	public static final int TAB_SUBMIT_TMP = R.id.main_submit_tmp;
	public static final int TAB_SUBMIT = R.id.main_submit;
	public static final int TAB_PREVIEW = R.id.main_preview;

	public static final int TAB_MAIN_SET = R.id.set;
	public static final int TAB_MAIN_RECORD = R.id.record;
	public static final int TAB_MAIN_MAKE = R.id.make;
	/** 记录所有的fragment，防止重复创建 */
	private static Map<Integer, KPBaseFragment> mFragmentMap = new HashMap<Integer, KPBaseFragment>();

	/** 采用工厂类进行创建Fragment，便于扩展，已经创建的Fragment不再创建 */
	public static KPBaseFragment createFragment(int index) {
		KPBaseFragment fragment = mFragmentMap.get(index);
		
		if (fragment == null) {
			switch (index) {
				case TAB_SUBMIT_TMP:
					DTLog.e("home吗");
					fragment = new KPSubmitFragment();
					break;
				case TAB_SUBMIT:
					fragment = new KPSubmitFragment();
					break;
				case TAB_PREVIEW:
					fragment = new KPSubmitFragment();
					break;
				case TAB_MAIN_MAKE:
					fragment = new KPMakeFragment();
					break;
				case TAB_MAIN_RECORD:
					fragment = new KPMapListNewFragment();
					break;
				case TAB_MAIN_SET:
					fragment = new KPSetFragment();
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
		for (Integer i : keySet) {
			KPBaseFragment fragment = mFragmentMap.get(i);
			fragment.onDetach();
		}
		mFragmentMap.clear();
	}
}
