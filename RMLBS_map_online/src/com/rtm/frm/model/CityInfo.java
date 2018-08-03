package com.rtm.frm.model;

import java.io.Serializable;
import java.util.ArrayList;

import com.rtm.common.model.BuildInfo;

/**
 * 城市信息
 * 
 * @author dingtao
 *
 */
public class CityInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	private String city;
	private ArrayList<BuildInfo> buildlist;

	/**
	 * 得到城市名称
	 * 
	 * @return
	 */
	public String getCity() {
		return city;
	}

	/**
	 * 设置城市名称
	 * 
	 * @param city
	 */
	public void setCity(String city) {
		this.city = city;
	}

	/**
	 * 得到该城市建筑物列表
	 * 
	 * @return 建筑物列表，具体使用请查看BuildInfo
	 */
	public ArrayList<BuildInfo> getBuildlist() {
		return buildlist;
	}

	/**
	 * 设置建筑物列表
	 * 
	 * @param buildlist
	 *            建筑物列表，具体使用请查看BuildInfo
	 */
	public void setBuildlist(ArrayList<BuildInfo> buildlist) {
		this.buildlist = buildlist;
	}
}
