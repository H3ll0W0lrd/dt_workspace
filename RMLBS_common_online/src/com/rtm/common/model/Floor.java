package com.rtm.common.model;

import java.io.Serializable;

/**
 * 楼层详情
 * @author dingtao
 *
 */
public class Floor implements Serializable {
	private static final long serialVersionUID = 1L;
	private String mFloor;
	private String mDescription;
	private String buildid;

	/**
	 * 得到建筑物ID
	 * @return
	 */
	public String getBuildid() {
		return buildid;
	}

	/**
	 * 设置建筑物ID
	 * @param buildid
	 */
	public void setBuildid(String buildid) {
		this.buildid = buildid;
	}

	/**
	 * 设置楼层
	 * @param floor
	 */
	public void setFloor(String floor) {
		mFloor = floor;
	}
	
	/**
	 * 得到楼层
	 * @return
	 */
	public String getFloor() {
		return mFloor;
	}
	/**
	 * 设置楼层描述
	 * @param description
	 */
	public void setDescription(String description) {
		mDescription = description;
	}
	
	/**
	 * 得到楼层描述
	 * @return
	 */
	public String getDescription() {
		return mDescription;
	}
}
