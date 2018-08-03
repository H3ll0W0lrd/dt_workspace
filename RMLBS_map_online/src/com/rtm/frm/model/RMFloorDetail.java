package com.rtm.frm.model;

import java.io.Serializable;

import com.rtm.common.model.Floor;

/**
 * 楼层详情
 * 
 * @author dingtao
 *
 */
public class RMFloorDetail implements Serializable {
	private static final long serialVersionUID = 1L;
	private int error_code = -1;
	private String error_msg;
	private Floor floor;

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
	 * 
	 * @param error_code
	 *            默认-1
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
	 * 
	 * @param error_msg
	 */
	public void setError_msg(String error_msg) {
		this.error_msg = error_msg;
	}

	/**
	 * 得到楼层信息
	 * 
	 * @return 具体使用请查看Floor
	 */
	public Floor getFloor() {
		return floor;
	}

	/**
	 * 设置楼层信息
	 * 
	 * @param floor
	 *            具体使用请查看Floor
	 */
	public void setFloor(Floor floor) {
		this.floor = floor;
	}

}
