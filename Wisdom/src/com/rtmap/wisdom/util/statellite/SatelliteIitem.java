package com.rtmap.wisdom.util.statellite;

import java.io.Serializable;

import android.graphics.Bitmap;

public class SatelliteIitem implements Serializable{
	int id;
	float x;//屏幕坐标
	float y;//屏幕坐标
	float degrees;
	String name;
	boolean select;
	Bitmap nomorlBitmap;
	Bitmap selectedBtimap;
	
	public SatelliteIitem(int id, String name, Bitmap nomorlBitmap,
			Bitmap selectedBtimap) {
		super();
		this.id = id;
		this.name = name;
		this.nomorlBitmap = nomorlBitmap;
		this.selectedBtimap = selectedBtimap;
	}

	public Bitmap getNomorlBitmap() {
		return nomorlBitmap;
	}

	public void setNomorlBitmap(Bitmap nomorlBitmap) {
		this.nomorlBitmap = nomorlBitmap;
	}

	public Bitmap getSelectedBtimap() {
		return selectedBtimap;
	}

	public void setSelectedBtimap(Bitmap selectedBtimap) {
		this.selectedBtimap = selectedBtimap;
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
