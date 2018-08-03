package com.rtm.frm.arar;

import android.view.View;

public class ARShowView {

	private View layoutView;
	// poi点在屏幕上的位置坐标
	private float currentx = 0;
	private float currenty = 0;

	private float targetDegree;
	private float targerDistance;
	private String targetName;
	// 这个poi在地图上的x,y坐标
	private float poiTargetX;
	private float poiTargetY;


	private String poiTargetFloor;
	private String poiTargetRouteInfo;
	private Float poiTargetDefaultDistance;

	private int targetImageResource;

	public View getLayoutView() {
		return layoutView;
	}

	public void setLayoutView(View layoutView) {
		this.layoutView = layoutView;
	}

	public float getCurrentx() {
		return currentx;
	}

	public void setCurrentx(float currentx) {
		this.currentx = currentx;
	}

	public float getCurrenty() {
		return currenty;
	}

	public void setCurrenty(float currenty) {
		this.currenty = currenty;
	}

	public float getTargetDegree() {
		return targetDegree;
	}

	public void setTargetDegree(float targetDegree) {
		this.targetDegree = targetDegree;
	}

	public float getTargerDistance() {
		return targerDistance;
	}

	public void setTargerDistance(float targerDistance) {
		this.targerDistance = targerDistance;
	}

	public String getTargetName() {
		return targetName;
	}

	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}

	public float getPoiTargetX() {
		return poiTargetX;
	}

	public void setPoiTargetX(float poiTargetX) {
		this.poiTargetX = poiTargetX;
	}

	public float getPoiTargetY() {
		return poiTargetY;
	}

	public void setPoiTargetY(float poiTargetY) {
		this.poiTargetY = poiTargetY;
	}

	public String getPoiTargetFloor() {
		return poiTargetFloor;
	}

	public void setPoiTargetFloor(String poiTargetFloor) {
		this.poiTargetFloor = poiTargetFloor;
	}

	public String getPoiTargetRouteInfo() {
		return poiTargetRouteInfo;
	}

	public void setPoiTargetRouteInfo(String poiTargetRouteInfo) {
		this.poiTargetRouteInfo = poiTargetRouteInfo;
	}

	public Float getPoiTargetDefaultDistance() {
		return poiTargetDefaultDistance;
	}

	public void setPoiTargetDefaultDistance(Float poiTargetDefaultDistance) {
		this.poiTargetDefaultDistance = poiTargetDefaultDistance;
	}

	public int getTargetImageResource() {
		return targetImageResource;
	}

	public void setTargetImageResource(int targetImageResource) {
		this.targetImageResource = targetImageResource;
	}



}
