package com.rtmap.ambassador.model;

import java.io.Serializable;
import java.util.ArrayList;

import com.rtm.common.utils.RMStringUtils;
import com.rtm.frm.model.Location;
import com.rtm.frm.vmap.Coord;
import com.rtmap.ambassador.util.DTLog;

public class Area implements Serializable {
	private String areaCode;
	private String areaName;
	private String buildingId;
	private String floorNo;
	private String theGeom;
	private String theGeomName;
	private ArrayList<Location> coords;
	private float top;
	private float bottom;
	private float left;
	private float right;

	public float getTop() {
		return top;
	}

	public void setTop(float top) {
		this.top = top;
	}

	public float getBottom() {
		return bottom;
	}

	public void setBottom(float bottom) {
		this.bottom = bottom;
	}

	public float getLeft() {
		return left;
	}

	public void setLeft(float left) {
		this.left = left;
	}

	public float getRight() {
		return right;
	}

	public void setRight(float right) {
		this.right = right;
	}

	public String getAreaCode() {
		return areaCode;
	}

	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}

	public String getAreaName() {
		return areaName;
	}

	public void setAreaName(String areaName) {
		this.areaName = areaName;
	}

	public String getBuildingId() {
		return buildingId;
	}

	public void setBuildingId(String buildingId) {
		this.buildingId = buildingId;
	}

	public String getFloorNo() {
		return floorNo;
	}

	public void setFloorNo(String floorNo) {
		this.floorNo = floorNo;
	}

	public String getTheGeom() {
		return theGeom;
	}

	public void setTheGeom(String theGeom) {
		this.theGeom = theGeom;
	}

	public ArrayList<Location> getCoords() {
		if (coords == null) {
			if (!RMStringUtils.isEmpty(theGeom)) {
				this.theGeomName = theGeom.substring(0, theGeom.indexOf("((("));
				String[] coordsStr = theGeom.substring(
						theGeom.indexOf("(((") + ")))".length(),
						theGeom.indexOf(")))")).split(",");// 切割坐标
				coords = new ArrayList<Location>();
				for (int i = 0; i < coordsStr.length; i++) {
					String[] coord = coordsStr[i].split(" ");
					float x = Float.valueOf(coord[0]);
					float y = Math.abs(Float.valueOf(coord[1]));

					if (bottom < y) {
						bottom = y;
					}
					if (top > y || top == 0) {
						top = y;
					}
					if (left > x || left == 0) {
						left = x;
					}
					if (right < x) {
						right = x;
					}
					coords.add(new Location(x, y, floorNo, buildingId));
				}
			}
		}
		return coords;
	}

	public void setTheGeomName(String theGeomName) {
		this.theGeomName = theGeomName;
	}

	public String getTheGeomName() {
		return theGeomName;
	}
}
