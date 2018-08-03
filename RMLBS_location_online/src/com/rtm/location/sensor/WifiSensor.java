package com.rtm.location.sensor;

import java.util.List;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import com.rtm.location.LocationApp;
import com.rtm.location.entity.WifiEntity;
import com.rtm.location.utils.UtilLoc;

public class WifiSensor {

	private WifiManager mWifiManager;

	private static WifiSensor instance = null;

	private static Context context = null;

	private WifiSensor() {
	}

	public synchronized static WifiSensor getInstance() {
		if (instance == null) {
			instance = new WifiSensor();
		}
		return instance;
	}

	public void closeWifi() {
		if (mWifiManager != null) {
			mWifiManager.setWifiEnabled(false);
		}
	}

	public boolean init(Context c) {
		context = c;
		mWifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		return mWifiManager.isWifiEnabled();
	}

	public void scan(boolean isDataSave) {
		boolean isOpen = mWifiManager.isWifiEnabled();
		LocationApp.getInstance().setWifiopen(isOpen);
		if (mWifiManager != null && isOpen) {
			mWifiManager.startScan();
			if (isDataSave) {
				List<ScanResult> lsScanResult = mWifiManager.getScanResults();
				WifiEntity.getInstance().put(lsScanResult);
			}
		}
	}

	public String getIpAddress() {
		String ret = "0.0.0.0";
		try {
			if (mWifiManager != null) {
				int ip = mWifiManager.getConnectionInfo().getIpAddress();
				ret = UtilLoc.intToIp(ip);
			}
			if (ret.equals("0.0.0.0")) {
				ret = UtilLoc.getLocalIpAddress();
				if (ret == null) {
					ret = "0.0.0.0";
				}
			}
		} catch (Exception e) {
			ret = "0.0.0.0";
		}
		return ret;
	}

	public void destroy() {
		if (mWifiManager != null) {
			mWifiManager = null;
		}
	}
}
