package com.rtm.common.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * poi列表信息，包含poi信息列表、错误码和错误信息。
 * 
 * @author dingtao
 *
 */
public class RMPois implements Serializable {
	private static final long serialVersionUID = 1L;
	private int error_code = -1;
	private String error_msg;
	private ArrayList<POI> poilist;

	/**
	 * 错误码
	 * 
	 * @return
	 */
	public int getError_code() {
		return error_code;
	}

	/**
	 * 设置错误码，默认是-1
	 * @param error_code
	 */
	public void setError_code(int error_code) {
		this.error_code = error_code;
	}

	/**
	 * 错误信息
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
	 * poi列表
	 * 
	 * @return 具体使用请查看POI
	 */
	public ArrayList<POI> getPoilist() {
		return poilist;
	}

	/**
	 * 设置POI列表
	 * @param poilist 具体使用请查看POI
	 */
	public void setPoilist(ArrayList<POI> poilist) {
		this.poilist = poilist;
	}
}
