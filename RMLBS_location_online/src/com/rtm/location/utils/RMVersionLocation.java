package com.rtm.location.utils;

import com.rtm.location.JNILocation;

/**
 * 定位库版本,rtmap_lbs_location_v*.jar包的版本号
 * 
 * @author dingtao
 *
 */
public interface RMVersionLocation {
	/**
	 * location.jar包版本号
	 */
	public final static String VERSION = "online3.1.1";

	/**
	 * 定位so库版本
	 */
	public final static String SO_VERSION = JNILocation.getSoVersion();

}
