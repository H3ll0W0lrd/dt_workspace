package com.rtm.common.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 建筑物信息
 * 
 * @author dingtao
 *
 */

public class BuildInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	private String mBuildId;
	private String name_chn;
	private ArrayList<Floor> floorlist;
	private float mLat;
	private float mLong;
	private String name_en;
	private String name_qp;
	private String name_jp;
	private String desc;
	private float mapAngle;

	public void setMapAngle(float mapAngle) {
		this.mapAngle = mapAngle;
	}
	
	public float getMapAngle() {
		return mapAngle;
	}
	
	/**
	 * 得到建筑物列表
	 * 
	 * @return 楼层列表，详情请查看Floor
	 */
	public ArrayList<Floor> getFloorlist() {
		return floorlist;
	}

	/**
	 * 设置楼层列表
	 * 
	 * @param floorlist
	 *            楼层列表，详情请查看Floor
	 */
	public void setFloorlist(ArrayList<Floor> floorlist) {
		this.floorlist = floorlist;
	}

	/**
	 * 得到英文名称
	 * 
	 * @return
	 */
	public String getName_en() {
		return name_en;
	}

	/**
	 * 设置英文全称
	 * 
	 * @param name_en
	 */
	public void setName_en(String name_en) {
		this.name_en = name_en;
	}

	/**
	 * 得到建筑物全称
	 * 
	 * @return
	 */
	public String getName_qp() {
		return name_qp;
	}

	/**
	 * 设置建筑物全称
	 * 
	 * @param name_qp
	 */
	public void setName_qp(String name_qp) {
		this.name_qp = name_qp;
	}

	/**
	 * 得到建筑物简称
	 * 
	 * @return
	 */
	public String getName_jp() {
		return name_jp;
	}

	/**
	 * 设置建筑物简称
	 * 
	 * @param name_jp
	 */
	public void setName_jp(String name_jp) {
		this.name_jp = name_jp;
	}

	/**
	 * 得到建筑物描述
	 * 
	 * @return
	 */
	public String getDesc() {
		return desc;
	}

	/**
	 * 添加描述
	 * 
	 * @param desc
	 */
	public void setDesc(String desc) {
		this.desc = desc;
	}

	/**
	 * 得到建筑物ID
	 * 
	 * @return
	 */
	public String getBuildId() {
		return mBuildId;
	}

	/**
	 * 设置建筑物ID
	 * 
	 * @param buildId
	 */
	public void setBuildId(String buildId) {
		mBuildId = buildId;
	}

	/**
	 * 设置建筑物名字，中文名
	 * 
	 * @param name
	 */
	public void setBuildName(String name) {
		name_chn = name;
	}

	/**
	 * 得到建筑物名字
	 * 
	 * @return
	 */
	public String getBuildName() {
		return name_chn;
	}

	/**
	 * 得到纬度
	 * 
	 * @return
	 */
	public float getLat() {
		return mLat;
	}

	/**
	 * 得到经度
	 * 
	 * @return
	 */
	public float getLong() {
		return mLong;
	}

	/**
	 * 设置经纬度
	 * 
	 * @param latitude
	 *            纬度
	 * @param longitude
	 *            精度
	 */
	public void setLatLong(float latitude, float longitude) {
		mLat = latitude;
		mLong = longitude;
	}
}
