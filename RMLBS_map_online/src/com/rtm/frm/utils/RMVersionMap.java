package com.rtm.frm.utils;

import com.rtm.frm.vmap.readmap;

/**
 * 地图库版本，rtmap_lbs_map_v*.jar包的版本号
 * @author dingtao
 *
 */
public interface RMVersionMap {
	/**
	 * map.jar包版本号
	 */
	public final static String VERSION = "online3.1";
	/**
	 * 地图so库版本
	 */
	public final static String SO_VERSION = readmap.soVersion();
}
