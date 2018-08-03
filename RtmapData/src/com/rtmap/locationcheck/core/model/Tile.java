package com.rtmap.locationcheck.core.model;

import java.io.Serializable;

public class Tile implements Serializable{
	String floor;
	String description;
	String width;
	String height;
	String level_tile;
	String description_;
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
	public String getLevel_tile() {
		return level_tile;
	}
	public void setLevel_tile(String level_tile) {
		this.level_tile = level_tile;
	}
	public String getDescription_() {
		return description_;
	}
	public void setDescription_(String description_) {
		this.description_ = description_;
	}
}
