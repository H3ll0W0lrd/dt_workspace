package com.rtm.frm.AR;

import android.view.View;


public class arPoiItem {

	// poi点在屏幕上的位置坐标
//	private float mapx=0f;
//	private float mapy=0f;
	//在地图上坐标
	private float x = 0f;
	private float y = 0f;
//	private float degree = 0f;
	private float distance=0f;
	private String msgTurn;
	private String name;
	private String targetFloor;
	private String currFloor;
	private View v;

	//X方向坐标
	public float getX() {
		return x;
	}
	public void setX(float x) {
		this.x = x;
	}

	//Y方向坐标
	public float getY() {
		return y;
	}
	public void setY(float y) {
		this.y = y;
	}
//	//在手机屏幕上的x坐标
//	public float getMapX(){
//		return mapx;
//	}
//	public void setMapX(float mapx){
//		this.mapx = mapx;
//	}
//	
//	//在手机屏幕上的y坐标
//	public float getMapY(){
//		return mapy;
//	}
//	public void setMapY(float mapy){
//		this.mapy = mapy;
//	}
//	
	//name
	public String getName(){
		return name;
	}
	public void setName(String name){
		this.name = name;
	}
	//上一个点的name
	public void setRouteInfo(String routeinfo){
		msgTurn = routeinfo;
	}
	public String getRouteInfo(){
		return msgTurn;
	}
	//当前所在楼层
	public void setCurrFloor(String currFloor){
		this.currFloor = currFloor;
	}
	public String getCurrFloor(){
		return currFloor;
	}
	
	//floor目标所在楼层
	public void setTargetFloor(String floor){
		this.targetFloor = floor;
	}
	public String getTargetFloor(){
		return targetFloor;
	}
	
	//distance当前位置和poi点之间的距离
	public void setDistance(float dis){
		distance = dis;
	}
	public float getDistance(){
		return distance;
	}
	
//	//当前位置和poi之间的真实角度
//	public float getDegree(){
//		return degree;
//	}
//	public void setDegree(float degree){
//		this.degree = degree;
//	}
	
	//设置view
	public void setView(View v){
		this.v = v;
	}
	public View getView(){
		return v;
	}
	
}
