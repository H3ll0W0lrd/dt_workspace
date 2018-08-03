package com.hwasmart.utils;

/* 
 *  WifiConnect.java 
 *  Author: cscmaker 
 */

import java.util.List;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

public class WifiConnect {
	
	private WifiManager wifiManager;
	private static String TAG = "WifiConnect";

	// 定义几种加密方式，一种是WEP，一种是WPA，还有没有密码的情况
	public enum WifiCipherType {
		WIFICIPHER_WEP, WIFICIPHER_WPA, WIFICIPHER_NOPASS, WIFICIPHER_INVALID
	}

	// 构造函数
	public WifiConnect(WifiManager wifiManager) {
		this.wifiManager = wifiManager;
	}

	/**
	 * 打开wifi功能
	 * @return
	 */
	public boolean openWifi() {
		boolean bRet = false;
		if (!wifiManager.isWifiEnabled()) {
//			bRet = wifiManager.setWifiEnabled(true);
		}
		return bRet;
	}

	/**
	 * 进行默认Wifi链接，通过enableNetwork方式链接
	 * @return
	 */
//	public boolean connect(){
//		return connect("MH Mall", "", WifiConnect.WifiCipherType.WIFICIPHER_NOPASS);
//	}
	
	/**
	 * 提供一个外部接口，传入要连接的无线网，通过enableNetwork方式链接
	 * @param SSID
	 * @param Password
	 * @param Type
	 * @return
	 */
//	public boolean connect(String SSID, String Password, WifiCipherType Type) {
//		Log.i(TAG, "Wifi Connect!");
//		if (!this.openWifi()) {
//			Log.d(TAG, "OpenWifi false");
//			return false;
//		}
//		// 开启wifi功能需要一段时间(我在手机上测试一般需要1-3秒左右)，所以要等到wifi
//		// 状态变成WIFI_STATE_ENABLED的时候才能执行下面的语句
//		while (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING) {
//			try {
//				// 为了避免程序一直while循环，让它睡个100毫秒在检测……
//				Thread.currentThread();
//				Thread.sleep(100);
//			} catch (InterruptedException ie) {
//			}
//		}
//
//		WifiConfiguration wifiConfig = this.createWifiInfo(SSID, Password, Type);
//		//
//		if (wifiConfig == null) {
//			Log.d(TAG, "wifiConfig false");
//			return false;
//		}
//
//		WifiConfiguration tempConfig = this.isExsits(SSID);
//
//		if (tempConfig != null) {
//			wifiManager.removeNetwork(tempConfig.networkId);
//		}
//
//		int netID = wifiManager.addNetwork(wifiConfig);
//		boolean bRet = wifiManager.enableNetwork(netID, true);
//
////		发起连接
////		if (bRet)
////			bRet = wifiManager.reconnect();
////		else
////			Log.d(TAG, "enableNetwork false");
//		
//		return bRet;
//	}
	
	/**
	 * 尝试重连
	 */	
	public void reconnect(){
		wifiManager.disconnect();
		wifiManager.reconnect();
	}

	// 查看以前是否也配置过这个网络
	private WifiConfiguration isExsits(String SSID) {
		List<WifiConfiguration> existingConfigs = wifiManager.getConfiguredNetworks();
		if (existingConfigs == null)
			return null;
		
		for (WifiConfiguration existingConfig : existingConfigs) {
			Log.d(TAG, "SSID:" + existingConfig.SSID);
			if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
				return existingConfig;
			}
		}
		return null;
	}

	private WifiConfiguration createWifiInfo(String SSID, String Password, WifiCipherType Type) {
		WifiConfiguration config = new WifiConfiguration();
		config.allowedAuthAlgorithms.clear();
		config.allowedGroupCiphers.clear();
		config.allowedKeyManagement.clear();
		config.allowedPairwiseCiphers.clear();
		config.allowedProtocols.clear();
		config.SSID = "\"" + SSID + "\"";
		if (Type == WifiCipherType.WIFICIPHER_NOPASS) {
//			config.wepKeys[0] = "";
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
//			config.wepTxKeyIndex = 0;
//			config.status = WifiConfiguration.Status.ENABLED;
		} else if (Type == WifiCipherType.WIFICIPHER_WEP) {
			config.preSharedKey = "\"" + Password + "\"";
			config.hiddenSSID = true;
			config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
		} else if (Type == WifiCipherType.WIFICIPHER_WPA) {
			config.preSharedKey = "\"" + Password + "\"";
			config.hiddenSSID = true;
			config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP); 
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);  
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
			config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
			config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);  
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.NONE); 
			config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
			config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
			config.status = WifiConfiguration.Status.ENABLED;
		} else {
			return null;
		}
		return config;
	}

}
