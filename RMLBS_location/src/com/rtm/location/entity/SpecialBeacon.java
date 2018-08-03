/*
 * ThirdPartyBeacon.java
 * classes : com.rtm.location.entity.ThirdPartyBeacon
 * @author zny
 * V 1.0.0
 * Create at 2014年12月24日 下午4:39:14
 */
package com.rtm.location.entity;

import android.bluetooth.BluetoothDevice;

import com.rtm.common.utils.RMStringUtils;

/**
 * com.rtm.location.entity.ThirdPartyBeacon
 * 
 * @author zny <br/>
 *         create at 2014年12月24日 下午4:39:14
 */
public class SpecialBeacon {

	public static BeaconInfo decodeXbeacon(BluetoothDevice device, int rssi,
			byte[] scanData) {
		BeaconInfo ret = null;
		try {
			if (scanData.length > 26) {
				byte keyA = scanData[17];
				byte keyB = scanData[18];
				byte[] ble_addrs = new byte[6];
				for (int i = 0; i < ble_addrs.length; i++) {
					ble_addrs[i] = (byte) (scanData[i + 19] ^ keyA ^ keyB);
				}
				String ble_addr = RMStringUtils.bytesToHexString(ble_addrs);
				ret = new BeaconInfo();
				ret.mac = ble_addr;
				ret.rssi = rssi;
				ret.chennal = Type.X_BEACON;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

}
