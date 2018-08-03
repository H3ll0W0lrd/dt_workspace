/*
 * BeaconInfo.java
 * classes : com.rtm.location.entity.BeaconInfo
 * @author zny
 * V 1.0.0
 * Create at 2015年1月19日 下午4:49:08
 */
package com.rtm.location.entity;

import java.io.Serializable;

/**
 * com.rtm.location.entity.BeaconInfo
 * 
 * @author zny <br/>
 *         create at 2015年1月19日 下午4:49:08
 */
public class BeaconInfo implements Serializable{
	public String name;// 名字
	public int major;// major
	public int minor;// minor
	public String proximityUuid;// uuid
	public String bluetoothAddress;// 蓝牙地址
	public int txPower;// 出口功率
	public int rssi;// 信号
	public String mac;
	
	public Type chennal;
	
	public int count=1;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public String getProximityUuid() {
		return proximityUuid;
	}

	public void setProximityUuid(String proximityUuid) {
		this.proximityUuid = proximityUuid;
	}

	public String getBluetoothAddress() {
		return bluetoothAddress;
	}

	public void setBluetoothAddress(String bluetoothAddress) {
		this.bluetoothAddress = bluetoothAddress;
	}

	public int getTxPower() {
		return txPower;
	}

	public void setTxPower(int txPower) {
		this.txPower = txPower;
	}

	public int getRssi() {
		return rssi;
	}

	public void setRssi(int rssi) {
		this.rssi = rssi;
	}
}
