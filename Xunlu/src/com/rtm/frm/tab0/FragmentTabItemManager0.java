/**
 * @date 2014.08.18 21:15
 */
package com.rtm.frm.tab0;

import com.rtm.frm.fragment.controller.FragmentManagerTabItem;

public class FragmentTabItemManager0 extends FragmentManagerTabItem {

	private static FragmentTabItemManager0 instance;
	
	public static FragmentTabItemManager0 getInstance() {
		if(instance == null) {
			instance = new FragmentTabItemManager0();
		}
		return instance;
	}

}