package com.rtmap.experience.core.model;

public class BeaconInfo extends LCPoint {
	private String mac;
	private int Threshold_switch_min;
	private int Threshold_switch_max;
	private String uuid;

	private String shopId;
	private int inshop;
	private int finger;
	private int output_power;

	private int major;
	private int minor;
	private int rssi_max;// 信号强弱
	private int rssi;// 实时信号

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public int getRssi() {
		return rssi;
	}

	public void setRssi(int rssi) {
		this.rssi = rssi;
	}

	public int getRssi_max() {
		return rssi_max;
	}

	public void setRssi_max(int rssi_max) {
		this.rssi_max = rssi_max;
	}

	public int getMajor() {
		return major;
	}

	public void setMajor(int major) {
		this.major = major;
	}

	public int getMinor() {
		return minor;
	}

	public void setMinor(int minor) {
		this.minor = minor;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public int getThreshold_switch_min() {
		return Threshold_switch_min;
	}

	public void setThreshold_switch_min(int threshold_switch_min) {
		Threshold_switch_min = threshold_switch_min;
	}

	public int getThreshold_switch_max() {
		return Threshold_switch_max;
	}

	public void setThreshold_switch_max(int threshold_switch_max) {
		Threshold_switch_max = threshold_switch_max;
	}

	public String getShopId() {
		return shopId;
	}

	public void setShopId(String shopId) {
		this.shopId = shopId;
	}

	public int getInshop() {
		return inshop;
	}

	public void setInshop(int inshop) {
		this.inshop = inshop;
	}

	public int getFinger() {
		return finger;
	}

	public void setFinger(int finger) {
		this.finger = finger;
	}

	public int getOutput_power() {
		return output_power;
	}

	public void setOutput_power(int output_power) {
		this.output_power = output_power;
	}
}
