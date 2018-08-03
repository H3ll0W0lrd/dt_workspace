package com.rtmap.locationcheck.core.model;

import java.io.Serializable;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "beacon_table")
public class LCPoint implements Serializable {

	@DatabaseField(columnName ="x")
	private int x;// 采集点X坐标
	@DatabaseField(columnName ="y")
	private int y;// 采集点Y坐标
	@DatabaseField(columnName ="name",canBeNull=true)
	private String name;// 点的名字
	@DatabaseField(columnName ="buildId")
	private String buildId;
	@DatabaseField(columnName ="floor")
	private String floor;// "20100"
	@DatabaseField(columnName ="isClick",dataType=DataType.BOOLEAN)
	private boolean isClick;// 是否点击

	public boolean isClick() {
		return isClick;
	}

	public void setClick(boolean isClick) {
		this.isClick = isClick;
	}

	public String getBuildId() {
		return buildId;
	}

	public void setBuildId(String buildId) {
		this.buildId = buildId;
	}

	public String getFloor() {
		return floor;
	}

	public void setFloor(String floor) {
		this.floor = floor;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
