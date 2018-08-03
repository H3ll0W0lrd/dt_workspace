package com.baidu.push.util;

import java.util.HashMap;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import com.baidu.push.model.BeaconInfo;

@SuppressLint("NewApi")
public class BeaconSensor {
	private static BeaconSensor instance;
	private static final String TAG = "BeaconSensor";
	private Context context;
	private BluetoothAdapter mBluetoothAdapter;
	private HashMap<String, BeaconInfo> mBeaconMap;

	/** 得到该类的单例 */
	public synchronized static BeaconSensor getInstance() {
		if (instance == null) {
			instance = new BeaconSensor();
		}
		return instance;
	}
	

	public HashMap<String, BeaconInfo> getBeaconData() {
		return mBeaconMap;
	}

	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, int rssi,
				byte[] scanRecord) {
			if (getBeaconType(device) == BeaconType.X_BEACON) {
				BeaconInfo info = decodeXbeacon(device, rssi, scanRecord);
				if (info != null) {
					mBeaconMap.put(
							info.getUuid() + info.getMajor() + info.getMinor(),
							info);
				}
			} else {
				final BeaconInfo ibeacon = iBeaconClass.fromScanData(device,
						rssi, scanRecord);
				if (ibeacon != null) {
					mBeaconMap.put(ibeacon.getUuid() + ibeacon.getMajor()
							+ ibeacon.getMinor(), ibeacon);
				}
			}

		}
	};

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
				String mac = bytesToHexString(ble_addrs);
				ret = new BeaconInfo();
				ret.setRssi(rssi + "");
				ret.setUuid(mac.substring(0, 4));
				ret.setMajor("" + Integer.parseInt(mac.substring(4, 8), 16));
				ret.setMinor("" + Integer.parseInt(mac.substring(8, 12), 16));
			}
		} catch (Exception e) {
		}
		return ret;
	}

	/**
	 * Convert byte[] to hex
	 * string.这里我们可以将byte转换成int，然后利用Integer.toHexString(int)来转换成16进制字符串。
	 * 
	 * @param src
	 *            byte[] data
	 * @return hex string
	 */
	public static String bytesToHexString(byte[] src) {
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

	private BeaconSensor() {
		mBeaconMap = new HashMap<String, BeaconInfo>();
	}

	private BeaconType getBeaconType(final BluetoothDevice device) {
		if (device != null && device.getName() != null
				&& device.getName().equalsIgnoreCase("xbeacon")) {
			return BeaconType.X_BEACON;
		}
		return BeaconType.DEFAULT_BEACON;
	}

	public enum BeaconType {
		X_BEACON, DEFAULT_BEACON
	}

	public boolean init(Context c) {
		boolean ret = false;
		synchronized (BeaconSensor.this) {
			context = c;
			if (isSuportBeacon()) {
				final BluetoothManager bluetoothManager = (BluetoothManager) context
						.getSystemService(Context.BLUETOOTH_SERVICE);
				mBluetoothAdapter = bluetoothManager.getAdapter();
				ret = mBluetoothAdapter.isEnabled();
			}
		}
		return ret;
	}

	public boolean isSuportBeacon() {
		boolean ret = false;
		try {
			ret = context.getPackageManager().hasSystemFeature(
					PackageManager.FEATURE_BLUETOOTH_LE);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	public boolean isBlueToothOpen() {
		boolean ret = false;
		if (mBluetoothAdapter != null) {
			ret = mBluetoothAdapter.isEnabled();
		}
		return ret;
	}

	public void start() {
		if (isSuportBeacon() && isBlueToothOpen())
			mBluetoothAdapter.startLeScan(mLeScanCallback);
	}
	public void setContext(Context context) {
		this.context = context;
	}

	public void stop() {
		if (isSuportBeacon())
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
	}

	// public void openBlueTooth() {
	// if (isSuportBeacon(context) && mBluetoothAdapter != null) {
	// mBluetoothAdapter.enable();
	// }
	// }
	// public void closeBlueTooth() {
	// if (isSuportBeacon(context) && mBluetoothAdapter != null) {
	// mBluetoothAdapter.disable();
	// }
	// }

}
