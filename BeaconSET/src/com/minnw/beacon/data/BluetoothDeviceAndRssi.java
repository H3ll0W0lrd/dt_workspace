package com.minnw.beacon.data;

import org.json.JSONObject;

import android.bluetooth.BluetoothDevice;

public class BluetoothDeviceAndRssi implements
		Comparable<BluetoothDeviceAndRssi> {
	private BluetoothDevice bluetoothdevice;
	private int rssi;
	private JSONObject obj;
	private String serviceData = "";
	private String major="0";
	private String minor="0";
	private String Distance;
	private boolean CONN;
	private int battery;
	private int temp;
	private  String  name;
	private boolean tempVisvityy;
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	
	
	
	public boolean isTempVisvityy() {
		 if (serviceData.length() >= 16)
				return  true;
			else
				return false;
		 
	}

	public void setTempVisvityy(boolean tempVisvityy) {
		this.tempVisvityy = tempVisvityy;
	}

	public int getTemp() {
		 if (serviceData.length() >= 16)
			return  Integer.parseInt(serviceData.substring(6, 8),16);
		else
			return 0;
	}

	public void setTemp(int temp) {
		this.temp = temp;
	}

	public String getDistance() {
		return Distance;
	}

	public void setDistance(String distance) {
		Distance = distance;
	}

	public boolean isCONN() {
		return CONN;
	}

	public void setCONN(boolean cONN) {
		CONN = cONN;
	}



	public String getMajor() {
		 if (serviceData.length() >= 16)
				return serviceData.substring(8, 12);
	    else 	if (serviceData.length() >= 14)
			return serviceData.substring(6, 10);
		 
		else
			return major;
	}

	public void setMajor(String major) {
		this.major = major;
	}

	public String getMinor() {
		if (serviceData.length() >= 16)
			return serviceData.substring(12, 16);
		else if (serviceData.length() >= 14)
			return serviceData.substring(10, 14);
		else 
			return major;
	}

	public void setMinor(String minor) {
		this.minor = minor;
	}

	public int getBattery() {
		if (serviceData.length() >= 6)
			return Integer.parseInt(serviceData.substring(4, 6), 16);
		else
			return 0;
	}

	public void setBattery(int battery) {
		this.battery = battery;
	}

	public String getServiceData() {
		return serviceData;
	}

	public void setServiceData(String serviceData) {
		this.serviceData = serviceData;
	}

	public JSONObject getObj() {
		return obj;
	}

	public void setObj(JSONObject obj) {
		this.obj = obj;
	}

	public BluetoothDevice getBluetoothdevice() {
		return bluetoothdevice;
	}

	public void setBluetoothdevice(BluetoothDevice bluetoothdevice) {
		this.bluetoothdevice = bluetoothdevice;
	}

	public int getRssi() {
		return rssi;
	}

	public void setRssi(int rssi) {
		this.rssi = rssi;
	}

	@Override
	public int compareTo(BluetoothDeviceAndRssi o) {
		if (this.rssi < o.rssi)
			return 1;
		else if (this.rssi == o.rssi)
			return 0;
		else
			return -1;
	}
}
