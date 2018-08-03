/**
 * 地图定位共同使用的工具
 */
package com.rtm.common.utils;

/**
 * 地图回调接口
 * 
 * @author dingtao
 *
 */
public interface RMCallBack {
	Object onCallBackStart(Object... obj);

	void onCallBackFinish(Object obj);
}