package com.rtm.location.utils;

import com.rtm.common.model.RMLocation;

/**
 * 定位结果回调接口，请实现Listener，使用LocationApp注册监听器
 * 
 * @author dingtao
 *
 */
public interface RMLocationListener {
	/**
	 * 实现Listener，使用LocationApp注册监听器，在这个方法里拿到RMLocation进行处理
	 * 
	 * @param location
	 *            具体使用查看RMLocation
	 */
	void onReceiveLocation(RMLocation location);
}
