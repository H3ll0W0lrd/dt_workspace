/**
 * @date 2014.08.18 21:15
 */
package com.rtm.frm.tab2;

import com.rtm.frm.fragment.controller.FragmentManagerTabItem;
import com.rtm.frm.fragment.controller.MyFragmentManager;
import com.rtm.frm.utils.XunluUtil;

public class FragmentTabItemManager2 extends FragmentManagerTabItem {

	private static FragmentTabItemManager2 instance;
	
	public static FragmentTabItemManager2 getInstance() {
		if(instance == null) {
			instance = new FragmentTabItemManager2();
		}
		return instance;
	}

//	public void updateCurrentFragmentList() {
//		String currentShowFlag = getCurrFragmentFlag();
//		if(!XunluUtil.isEmpty(currentShowFlag)) {
//			FragmentTabMain2 fragmentTabMain2 = (FragmentTabMain2) MyFragmentManager.getFragmentByFlag(currentShowFlag.split("-")[0], currentShowFlag.split("-")[1]);
//			fragmentTabMain2.updateHotList();
//			fragmentTabMain2.refresh();
//		}
//	}
}