package com.baidu.push.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import com.baidu.push.model.ApInfo;
import com.dingtao.libs.util.DTLog;

public class WifiSensor {

	private WifiManager mWifiManager;

	private static WifiSensor instance = null;

	private static Context context = null;
	private HashMap<String, ApInfo> mApMap;// 扫描的AP

	private WifiSensor() {
		mApMap = new HashMap<String, ApInfo>();
	}
	
	public HashMap<String, ApInfo> getAPData() {
		return mApMap;
	}

	public synchronized static WifiSensor getInstance() {
		if (instance == null) {
			instance = new WifiSensor();
		}
		return instance;
	}

	public boolean isWifiOpen() {
		if (mWifiManager != null) {
			return mWifiManager.isWifiEnabled();
		}
		return false;
	}

	public void closeWifi() {
		if (mWifiManager != null) {
			mWifiManager.setWifiEnabled(false);
		}
	}

	public void setContext(Context c) {
		context = c;
		if (mWifiManager == null) {
			mWifiManager = (WifiManager) context
					.getSystemService(Context.WIFI_SERVICE);
		}
	}

	public void scan() {
		if (mWifiManager != null && mWifiManager.isWifiEnabled()) {
			mWifiManager.startScan();
			List<ScanResult> lsScanResult = mWifiManager.getScanResults();
			for (int i = 0; i < lsScanResult.size(); i++) {
				ScanResult result = lsScanResult.get(i);
				ApInfo info = new ApInfo();

				String mac = result.BSSID.replace(":", "").toUpperCase();
				info.setRssi(result.level + "");
				info.setMac(mac);
				info.setSsid(result.SSID);
				mApMap.put(mac, info);
			}
		}
	}

	public String getIpAddress() {
		String ret = "0.0.0.0";
		try {
			if (mWifiManager != null) {
				int ip = mWifiManager.getConnectionInfo().getIpAddress();
				ret = intToIp(ip);
			}
			if (ret.equals("0.0.0.0")) {
				ret = getLocalIpAddress();
				if (ret == null) {
					ret = "0.0.0.0";
				}
			}
		} catch (Exception e) {
			ret = "0.0.0.0";
		}
		return ret;
	}

	public static String intToIp(int i) {
		return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF)
				+ "." + (i >> 24 & 0xFF);
	}

	public static String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public void destroy() {
		if (mWifiManager != null) {
			mWifiManager = null;
		}
	}
}
