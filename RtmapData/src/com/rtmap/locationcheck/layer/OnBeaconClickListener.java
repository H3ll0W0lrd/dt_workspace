package com.rtmap.locationcheck.layer;

import com.rtmap.locationcheck.core.model.LCPoint;

/**
 * 地图上的点点击事件
 * @author zhengnengyuan
 *
 */
public interface OnBeaconClickListener {
	/**
	 * 处理点击
	 * @param point 点
	 * @param key 所在路线的key，可以为null
	 */
	void onBeaconClick(LCPoint point,String key);
}
