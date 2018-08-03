package com.rtm.frm.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 路线详情
 * 
 * @author dingtao
 *
 */
public class RMRoute implements Serializable {
	private static final long serialVersionUID = 1L;
	private int error_code = -1;
	private String error_msg;
	private int distance;
	private ArrayList<NavigatePoint> pointlist;

	/**
	 * 错误码
	 * 
	 * @return
	 */
	public int getError_code() {
		return error_code;
	}

	/**
	 * 设置错误码
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
	 * 得到路线距离
	 * 
	 * @return
	 */
	public int getDistance() {
		return distance;
	}

	/**
	 * 设置距离
	 * @param distance
	 */
	public void setDistance(int distance) {
		this.distance = distance;
	}

	/**
	 * 得到路线上点的集合
	 * 
	 * @return 路线点集合，具体使用请查看NavigatePoint
	 */
	public ArrayList<NavigatePoint> getPointlist() {
		return pointlist;
	}

	/**
	 * 设置路线点的集合
	 * @param pointlist 路线点集合，具体使用请查看NavigatePoint
	 */
	public void setPointlist(ArrayList<NavigatePoint> pointlist) {
		this.pointlist = pointlist;
	}
}
