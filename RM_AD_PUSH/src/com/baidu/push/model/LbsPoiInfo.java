package com.baidu.push.model;

import java.io.Serializable;

public class LbsPoiInfo implements Serializable {
	private String build_name;
	private String poi_id;
	private String poi_name;
	private String is_inside;
	private String floor;

	public String getBuild_name() {
		return build_name;
	}

	public void setBuild_name(String build_name) {
		this.build_name = build_name;
	}

	public String getPoi_id() {
		return poi_id;
	}

	public void setPoi_id(String poi_id) {
		this.poi_id = poi_id;
	}

	public String getPoi_name() {
		return poi_name;
	}

	public void setPoi_name(String poi_name) {
		this.poi_name = poi_name;
	}

	public String getIs_inside() {
		return is_inside;
	}

	public void setIs_inside(String is_inside) {
		this.is_inside = is_inside;
	}

	public String getFloor() {
		return floor;
	}

	public void setFloor(String floor) {
		this.floor = floor;
	}

}
