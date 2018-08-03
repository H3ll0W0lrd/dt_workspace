package com.rtm.frm.model;

import java.io.Serializable;

/**
 * 导航点详情
 * 
 * @author dingtao
 *
 */
public class NavigatePoint extends PointInfo {
	private static final long serialVersionUID = 1L;

	private int distance;// 当important为true，则返回与下个关键节点距离
	private String aroundPoiName;
	/**
	 * 当为关键节点时，1：直行，2：右前，3：右转，4：右后，5：左后，6：左转， 7：左前，8：直梯上行，9：直梯下行，10：扶梯上行，11扶梯下行。
	 */
	private int action;
	private boolean important;// 是否为关键节点
	private String desc;

	public NavigatePoint() {
	}

	/**
	 * 得到y坐标
	 * 
	 * @return
	 */
	public float getY() {
		return Math.abs(y);
	}

	public void setAroundPoiName(String aroundPoiName) {
		this.aroundPoiName = aroundPoiName;
	}

	public String getAroundPoiName() {
		return aroundPoiName;
	}

	/**
	 * 得到距离
	 * 
	 * @return
	 */
	public int getDistance() {
		return distance;
	}

	/**
	 * 设置距离
	 * 
	 * @param mDistance
	 */
	public void setDistance(int distance) {
		this.distance = distance;
	}

	/**
	 * 当为关键节点时，1：直行，2：右前，3：右转，4：右后，5：左后，6：左转， 7：左前，8：直梯上行，9：直梯下行，10：扶梯上行，11扶梯下行。
	 * 
	 * @return
	 */
	public int getAction() {
		return action;
	}

	public void setAction(int action) {
		this.action = action;
	}

	public boolean isImportant() {
		return important;
	}

	public void setImportant(boolean important) {
		this.important = important;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

}
