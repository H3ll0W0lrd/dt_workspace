package com.rtm.location.entity;

import java.util.ArrayList;
import java.util.Locale;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.rtm.location.JNILocation;
import com.rtm.location.LocationApp;

public class iBeaconClass {

	static byte[] keyArray = new byte[] { 0xA5 - 256, 0xB5 - 256, 0xC1 - 256,
			0x46, 0xAD - 256, 0xA7 - 256, 0x29, 0x1E, 0x7F, 0xF5 - 256, 0x57,
			0x95 - 256, 0x39, 0xC0 - 256, 0x41, 0x81 - 256, 0xB2 - 256,
			0xE3 - 256, 0xF5 - 256, 0x8C - 256, 0x23, 0x26, 0x41, 0xD7 - 256,
			0x41, 0xD0 - 256, 0x3E, 0xED - 256, 0x59, 0x32, 0x40, 0x9D - 256 };
	public static ArrayList<BlueBeacocnStatus> BlueList;

	public synchronized static BeaconInfo fromScanData(BluetoothDevice device,
			int rssi, byte[] scanData) {

		int startByte = 2;
		boolean patternFound = false;
		try {
			while (startByte <= 5) {
				if (((int) scanData[startByte + 2] & 0xff) == 0x02
						&& ((int) scanData[startByte + 3] & 0xff) == 0x15) {
					// yes! This is an iBeacon
					patternFound = true;
					break;
				} else if (((int) scanData[startByte] & 0xff) == 0x2d
						&& ((int) scanData[startByte + 1] & 0xff) == 0x24
						&& ((int) scanData[startByte + 2] & 0xff) == 0xbf
						&& ((int) scanData[startByte + 3] & 0xff) == 0x16) {
					BeaconInfo iBeacon = new BeaconInfo();
					iBeacon.major = 0;
					iBeacon.minor = 0;
					iBeacon.proximityUuid = "00000000-0000-0000-0000-000000000000";
					iBeacon.txPower = -55;
					return iBeacon;
				} else if (((int) scanData[startByte] & 0xff) == 0xad
						&& ((int) scanData[startByte + 1] & 0xff) == 0x77
						&& ((int) scanData[startByte + 2] & 0xff) == 0x00
						&& ((int) scanData[startByte + 3] & 0xff) == 0xc6) {

					BeaconInfo iBeacon = new BeaconInfo();
					iBeacon.major = 0;
					iBeacon.minor = 0;
					iBeacon.proximityUuid = "00000000-0000-0000-0000-000000000000";
					iBeacon.txPower = -55;
					return iBeacon;
				}
				startByte++;
			}

			if (!patternFound) {
				// This is not an iBeacon
				return null;
			}

			BeaconInfo iBeacon = new BeaconInfo();

			iBeacon.major = (scanData[startByte + 20] & 0xff) * 0x100
					+ (scanData[startByte + 21] & 0xff);
			iBeacon.minor = (scanData[startByte + 22] & 0xff) * 0x100
					+ (scanData[startByte + 23] & 0xff);
			iBeacon.txPower = (int) scanData[startByte + 24]; // this one is
																// signed
			if (BlueList != null && BlueList.size() > 0) {
				for (int i = 0; i < BlueList.size(); i++) {
					BlueBeacocnStatus s = BlueList.get(i);
					if (rssi >= s.getMin()
							&& rssi <= s.getMax()
							&& s.getWifi() == LocationApp.getInstance()
									.getWifiopen()
							&& s.getBeacon() == LocationApp.getInstance()
									.getBlueOpen()) {
						int r = rssi;
						rssi *= s.getWeight();
						// Log.e("rtmap", "异常信号：原为：" + r + "   现 ：" + rssi);
						break;
					}
				}
			}
			if (rssi == 0) {
				return null;
			} else {
				iBeacon.rssi = rssi;
			}

			byte[] proximityUuidBytes = new byte[16];
			System.arraycopy(scanData, startByte + 4, proximityUuidBytes, 0, 16);
			String hexString = bytesToHexString(proximityUuidBytes);
			StringBuilder sb = new StringBuilder();
			sb.append(hexString.substring(0, 8));
			sb.append("-");
			sb.append(hexString.substring(8, 12));
			sb.append("-");
			sb.append(hexString.substring(12, 16));
			sb.append("-");
			sb.append(hexString.substring(16, 20));
			sb.append("-");
			sb.append(hexString.substring(20, 32));
			iBeacon.proximityUuid = sb.toString();
			String uuid = iBeacon.proximityUuid.substring(0, 4).toUpperCase(
					Locale.getDefault());
			if (uuid.equals("C91A") || uuid.equals("C91B")) {
				// Log.i("decode", "解密前： " + iBeacon.major + ", " +
				// iBeacon.minor);
				Integer oriMajor = iBeacon.major;
				Integer oriMinor = iBeacon.minor;
				// byte[] b_major = int2byteArray(oriMajor);
				// byte[] b_minor = int2byteArray(oriMinor);
				// JNILocation.GetUuidMajorMinor(proximityUuidBytes, b_major,
				// b_minor);

				JNILocation.GetUuidMajorMinor(uuid, oriMajor, oriMinor);

				iBeacon.major = oriMajor;
				iBeacon.minor = oriMinor;
				// Log.i("decode", "解密后： " + iBeacon.major + ", " +
				// iBeacon.minor);
			}
			if (device != null) {
				iBeacon.bluetoothAddress = device.getAddress();
				iBeacon.name = device.getName();
			}
			return iBeacon;
		} catch (Exception e) {
			return null;
		}

	}

	private static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

	public static byte[] int2byteArray(int num) {
		byte[] result = new byte[2];
		// result[0] = (byte)(num >> 24);//取最高8位放到0下标
		// result[1] = (byte)(num >> 16);//取次高8为放到1下标
		result[0] = (byte) (num >> 8); // 取次低8位放到2下标
		result[1] = (byte) (num); // 取最低8位放到3下标
		return result;
	}

	public static int byteArray2int(byte[] b) {
		byte[] a = new byte[4];
		int i = a.length - 1, j = b.length - 1;
		for (; i >= 0; i--, j--) {// 从b的尾部(即int值的低位)开始copy数据
			if (j >= 0)
				a[i] = b[j];
			else
				a[i] = 0;// 如果b.length不足4,则将高位补0
		}
		int v0 = (a[0] & 0xff) << 24;// &0xff将byte值无差异转成int,避免Java自动类型提升后,会保留高位的符号位
		int v1 = (a[1] & 0xff) << 16;
		int v2 = (a[2] & 0xff) << 8;
		int v3 = (a[3] & 0xff);
		return v0 + v1 + v2 + v3;
	}

}
