package com.rtm.location.entity;

import java.io.Serializable;

/**
 * 围栏信息
 * @author dingtao
 *
 */
public class FenceInfo implements Serializable {
	private String buildId;
	private String floor;

	/**
	 * 得到建筑物Id
	 * @return 建筑物ID
	 */
	public String getBuildId() {
		return buildId;
	}

	/**
	 * 设置建筑物ID
	 * @param buildId 建筑物ID
	 */
	public void setBuildId(String buildId) {
		this.buildId = buildId;
	}

	/**
	 * 得到楼层，例：F2
	 * @retur 楼层，例：F2
	 */
	public String getFloor() {
		return floor;
	}

	/**
	 * 设置楼层，例：F2
	 * @param floor 楼层，例：F2
	 */
	public void setFloor(String floor) {
		this.floor = floor;
	}
}
