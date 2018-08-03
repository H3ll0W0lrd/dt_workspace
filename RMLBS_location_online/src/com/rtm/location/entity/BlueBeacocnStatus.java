package com.rtm.location.entity;

import java.io.Serializable;

public class BlueBeacocnStatus implements Serializable {
	private int wifi;
	private int beacon;
	private int max;
	private int min;
	private double weight;
	
	public BlueBeacocnStatus() {
	}
	
	public BlueBeacocnStatus(int wifi,int beacon,int max,int min,double weight){
		this.wifi = wifi;
		this.beacon = beacon;
		this.max = max;
		this.min = min;
		this.weight = weight;
	}

	public int getWifi() {
		return wifi;
	}

	public void setWifi(int wifi) {
		this.wifi = wifi;
	}

	public int getBeacon() {
		return beacon;
	}

	public void setBeacon(int beacon) {
		this.beacon = beacon;
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

}
