package com.example.textdemo.model;

import java.io.Serializable;

public class StatelliteIitem implements Serializable{
	int id;
	float x;//屏幕坐标
	float y;//屏幕坐标
	float degrees;
	String name;
	boolean select;
	
	public StatelliteIitem(int id, String name) {
		super();
		this.id = id;
		this.name = name;
	}
	
	public boolean isSelect() {
		return select;
	}


	public void setSelect(boolean select) {
		this.select = select;
	}


	public void setDegrees(float degrees) {
		this.degrees = degrees;
	}
	public float getDegrees() {
		return degrees;
	}
	public float getX() {
		return x;
	}
	public void setX(float x) {
		this.x = x;
	}
	public float getY() {
		return y;
	}
	public void setY(float y) {
		this.y = y;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
