package com.rtm.frm.model;

import java.io.Serializable;

public class Floor implements Serializable{
	
	private static final long serialVersionUID = 4958641683003698174L;
	public String buildId;
	public String buildName;
	public String floor;
	public String description;
	public String description_1;
	public String description_;
	public String width;
	public String height;
	public String levelTile;
	public int isPrivate;//
	
	public String getBuildId() {
		return buildId;
	}
	public void setBuildId(String buildId) {
		this.buildId = buildId;
	}
	public String getBuildName() {
		return buildName;
	}
	public void setBuildName(String buildName) {
		this.buildName = buildName;
	}
	public String getFloor() {
		return floor;
	}
	public void setFloor(String floor) {
		this.floor = floor;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getDescription_1() {
		return description_1;
	}
	public void setDescription_1(String description_1) {
		this.description_1 = description_1;
	}
	public String getDescription_() {
		return description_;
	}
	public void setDescription_(String description_) {
		this.description_ = description_;
	}
	public String getWidth() {
		return width;
	}
	public void setWidth(String width) {
		this.width = width;
	}
	public String getHeight() {
		return height;
	}
	public void setHeight(String height) {
		this.height = height;
	}
	public String getLevelTile() {
		return levelTile;
	}
	public void setLevelTile(String levelTile) {
		this.levelTile = levelTile;
	}
	public int getIsPrivate() {
		return isPrivate;
	}
	public void setIsPrivate(int isPrivate) {
		this.isPrivate = isPrivate;
	}
	
}
