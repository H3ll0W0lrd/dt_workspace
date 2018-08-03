package com.rtm.frm.model;

import java.io.Serializable;

public class Build implements Serializable{
	
	private static final long serialVersionUID = -184174365716452469L;
	
	public String cityName;//建筑物所在城市名称
	public String id;//建筑物id
	public String name;//建筑物名
	public String size;//大小
	public String nameJp2;//简拼
	public String lat;//纬度
	public String lng;//经度
	public String floors;//楼层总数
	public String versionData;//
	public String versionMap;//
	public int isPrivate;//
	public String googleLat;//谷歌维度
	public String googleLng;//谷歌精度
//	public int dis;
//	
//	public int getDis(){
//		return dis;
//	}
//	
//	public void setDis(int dis){
//		this.dis = dis;
//	}
	
	public String getCityName() {
		return cityName;
	}
	public void setCityName(String cityName) {
		this.cityName = cityName;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}
	public String getNameJp2() {
		return nameJp2;
	}
	public void setNameJp2(String nameJp2) {
		this.nameJp2 = nameJp2;
	}
	public String getLat() {
		return lat;
	}
	public void setLat(String lat) {
		this.lat = lat;
	}
	public String getLng() {
		return lng;
	}
	public void setLng(String lng) {
		this.lng = lng;
	}
	public String getFloors() {
		return floors;
	}
	public void setFloors(String floors) {
		this.floors = floors;
	}
	public String getVersionData() {
		return versionData;
	}
	public void setVersionData(String versionData) {
		this.versionData = versionData;
	}
	public String getVersionMap() {
		return versionMap;
	}
	public void setVersionMap(String versionMap) {
		this.versionMap = versionMap;
	}
	public int getIsPrivate() {
		return isPrivate;
	}
	public void setIsPrivate(int isPrivate) {
		this.isPrivate = isPrivate;
	}
	public String getGoogleLat() {
		return googleLat;
	}
	public void setGoogleLat(String googleLat) {
		this.googleLat = googleLat;
	}
	public String getGoogleLng() {
		return googleLng;
	}
	public void setGoogleLng(String googleLng) {
		this.googleLng = googleLng;
	}
}


