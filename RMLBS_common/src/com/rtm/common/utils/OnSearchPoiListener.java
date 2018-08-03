package com.rtm.common.utils;

import com.rtm.common.model.RMPois;

/**
 * POI列表结果回调接口
 * 
 * @author dingtao
 */
public interface OnSearchPoiListener {
	/**
	 * POI列表结果回调方法
	 * 
	 * @param result
	 *            详情参见RMPois
	 */
	public void onSearchPoi(RMPois result);
}
