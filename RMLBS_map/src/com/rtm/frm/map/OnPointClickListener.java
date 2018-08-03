package com.rtm.frm.map;

import com.rtm.frm.model.NavigatePoint;

/**
 * 地图上的点点击事件
 * @author zhengnengyuan
 *
 */
public interface OnPointClickListener {
	/**
	 * 处理点击
	 * @param point 点
	 * @param key 所在路线的key，可以为null
	 */
	void onClick(NavigatePoint point,String key);
}
