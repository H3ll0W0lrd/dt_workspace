package com.rtm.frm.utils;

import com.rtm.common.model.POI;
import com.rtm.frm.model.Location;

/**
 * 点击POI监听 V2版，点击地图后，返回POI和点击位置，如果点击位置在POI上，则有POI，否则只有位置
 * @author dingtao
 *
 */
public interface OnMapTapedListener {
	
	/**
	 * 点击POI监听 V2版，点击地图后，返回POI和点击位置，如果点击位置在POI上，则有POI，否则只有位置
	 * @param poi 如果点击位置在POI上，则返回POI
	 * @param location 点击位置的坐标
	 */
	public void onMapTaped(POI poi,Location location);
}
