package com.rtm.frm.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 城市列表
 * @author dingtao
 *
 */
public class RMCityList implements Serializable {
	private static final long serialVersionUID = 1L;
	private int error_code = -1;
	private String error_msg;
	private ArrayList<String> citylist;

	/**
	 * 得到错误码
	 * @return
	 */
	public int getError_code() {
		return error_code;
	}

	/**
	 * 设置错误码，默认-1
	 * @param error_code
	 */
	public void setError_code(int error_code) {
		this.error_code = error_code;
	}

	/**
	 * 得到错误信息
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
	 * 获取城市列表
	 * @return
	 */
	public ArrayList<String> getCitylist() {
		return citylist;
	}

	/**
	 * 设置城市列表
	 * @param citylist
	 */
	public void setCitylist(ArrayList<String> citylist) {
		this.citylist = citylist;
	}
}
