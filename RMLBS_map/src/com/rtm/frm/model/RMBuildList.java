package com.rtm.frm.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 建筑物列表
 * @author dingtao
 *
 */
public class RMBuildList implements Serializable {
	private static final long serialVersionUID = 1L;
	private int error_code = -1;
	private String error_msg;
	private ArrayList<CityInfo> citylist;

	/**
	 * 得到错误码
	 * 
	 * @return
	 */
	public int getError_code() {
		return error_code;
	}

	/**
	 * 设置错误码
	 * @param error_code 默认是-1
	 */
	public void setError_code(int error_code) {
		this.error_code = error_code;
	}

	/**
	 * 得到错误信息
	 * 
	 * @return
	 */
	public String getError_msg() {
		return error_msg;
	}

	/**
	 * 设置错误信息
	 * @param error_msg
	 */
	public void setError_msg(String error_msg) {
		this.error_msg = error_msg;
	}
	/**
	 * 得到城市列表
	 * @return 具体使用请查看CityInfo
	 */
	public ArrayList<CityInfo> getCitylist() {
		return citylist;
	}

	/**
	 * 设置城市列表
	 * @param citylist 具体使用请查看CityInfo
	 */
	public void setCitylist(ArrayList<CityInfo> citylist) {
		this.citylist = citylist;
	}
}
