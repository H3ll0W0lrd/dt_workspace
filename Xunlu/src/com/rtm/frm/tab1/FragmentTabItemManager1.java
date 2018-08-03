/**
 * @date 2014.08.18 21:15
 */
package com.rtm.frm.tab1;

import com.rtm.frm.fragment.controller.FragmentManagerTabItem;

public class FragmentTabItemManager1 extends FragmentManagerTabItem {

	private static FragmentTabItemManager1 instance;
	
	public static FragmentTabItemManager1 getInstance() {
		if(instance == null) {
			instance = new FragmentTabItemManager1();
		}
		return instance;
	}

}