package com.rtmap.wifipicker.data;

import java.io.Serializable;

public class MapInfo implements Serializable {
	private String mName;
	private String buildName;//建筑物名字
	private String mMD5;
	private float scale; // 地图比例尺
	private float offsetX; // 地图起偏移，即起始点坐标
	private float offsetY;
	
	public void setName(String name) {
		mName = name;
	}
	
	public String getBuildName() {
		return buildName;
	}

	public void setBuildName(String buildName) {
		this.buildName = buildName;
	}

	public void setMD5(String md5) {
		mMD5 = md5;
	}
	
	public String getName() {
		return mName;
	}
	
	public String getMD5() {
		return mMD5;
	}

    public float getScale() {
        return this.scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public float getOffsetX() {
        return this.offsetX;
    }

    public void setOffsetX(float offsetX) {
        this.offsetX = offsetX;
    }

    public float getOffsetY() {
        return this.offsetY;
    }

    public void setOffsetY(float offsetY) {
        this.offsetY = offsetY;
    }
    
    
	
	
}
