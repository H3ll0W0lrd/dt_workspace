package com.rtmap.locationcheck.core.model;

import java.util.ArrayList;

public class BeaconInfo extends LCPoint {
	private String mac;
	private int Threshold_switch_min;
	private int Threshold_switch_max;

	private int inshop;
	private int finger;
	private int output_power;

	private String broadcast_id;// 设备ID
	private String uuid;// UUID

	private String major;
	private String minor;
	private String major16;
	private String minor16;
	private int edit_status;// 编辑状态：0正常，1删除，2新建，3修改
	private int work_status;// 工作状态：0正常，-1低电量，-2故障，-3缺失，-4未知
	private int rssi_max;// 信号强弱
	private int rssi;// 实时信号

	private ArrayList<BroadcastInfo> maclist;

	public ArrayList<BroadcastInfo> getMaclist() {
		return maclist;
	}

	public void setMaclist(ArrayList<BroadcastInfo> maclist) {
		this.maclist = maclist;
	}

	public int getRssi() {
		return rssi;
	}

	public String getBroadcast_id() {
		return broadcast_id;
	}

	public void setBroadcast_id(String broadcast_id) {
		this.broadcast_id = broadcast_id;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public void setRssi(int rssi) {
		this.rssi = rssi;
	}

	public String getMajor16() {
		return major16;
	}

	public void setMajor16(String major16) {
		this.major16 = major16;
	}

	public String getMinor16() {
		return minor16;
	}

	public void setMinor16(String minor16) {
		this.minor16 = minor16;
	}

	public int getRssi_max() {
		return rssi_max;
	}

	public void setRssi_max(int rssi_max) {
		this.rssi_max = rssi_max;
	}

	public int getWork_status() {
		return work_status;
	}

	public void setWork_status(int work_status) {
		this.work_status = work_status;
	}

	public int getEdit_status() {
		return edit_status;
	}

	public void setEdit_status(int edit_status) {
		this.edit_status = edit_status;
	}

	public String getMajor() {
		return major;
	}

	public void setMajor(String major) {
		this.major = major;
	}

	public String getMinor() {
		return minor;
	}

	public void setMinor(String minor) {
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
