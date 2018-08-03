package com.rtmap.wifipicker.wifi;

import java.util.List;

import com.rtmap.wifipicker.core.WPApplication;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

public class WifiSensorHardware {
	/** 无线网卡控制器 **/
	public WifiManager mWifiManager;
	/** 该类的静态对象，用于单例 **/
	private static WifiSensorHardware mWifiHardware = null;
	/** 同步锁，用于pdr数据的一致性 **/
	public static Object lockWifi = new Object();

	public synchronized static WifiSensorHardware getInstance() {
		if (mWifiHardware == null) {
			mWifiHardware = new WifiSensorHardware();
		}
		return mWifiHardware;
	}

	private WifiSensorHardware() {
	}

	public String onGenerator() {
		if (mWifiManager == null)
			return "";
		mWifiManager.startScan();
		// TODO err null pr
		List<ScanResult> lsScanResult = mWifiManager.getScanResults();
		LocWifiGather locwifi = new LocWifiGather();
		locwifi.putGatherInfo(lsScanResult);
		if (locwifi.apList != null) {
			LocationIndoorInput.getInstance().putWifiData(locwifi);
		}
		return "";
	}

	/**
	 * 得到AP信息列表
	 */
	public List<ScanResult> onGetApList() {
		if (mWifiManager == null)
			return null;
		else {
			mWifiManager.startScan();
			return mWifiManager.getScanResults();
		}
	}

	public void onStart() {
		// 判断wifi网卡是否存在
		if (mWifiManager == null) {
			mWifiManager = (WifiManager) WPApplication.getInstance()
					.getSystemService(Context.WIFI_SERVICE);// 获取Wifi服务
		}
	}

	public void openWifi() {
		// 判断wifi网卡是否启动
		if (!mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(true);
		}
	}

	public void restartWifi() {
		if (mWifiManager != null) {
			mWifiManager.setWifiEnabled(false);
			try {
				while (mWifiManager.isWifiEnabled()) {
					Thread.sleep(50);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			openWifi();
		}
	}

	/**
	 * 检测wifi网卡是否开启
	 * 
	 * @return true 已经开启
	 */
	public boolean isWifiEnable() {
		return mWifiManager == null ? false : mWifiManager.isWifiEnabled();
	}

	public void onDestroy() {
		if (mWifiManager != null) {
			mWifiManager = null;
		}
	}
}
