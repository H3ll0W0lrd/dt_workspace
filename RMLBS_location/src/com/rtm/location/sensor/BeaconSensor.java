package com.rtm.location.sensor;

import java.util.HashMap;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import com.rtm.common.utils.RMLog;
import com.rtm.location.LocationApp;
import com.rtm.location.entity.BeaconEntity;
import com.rtm.location.entity.BeaconInfo;
import com.rtm.location.entity.SpecialBeacon;
import com.rtm.location.entity.Type;
import com.rtm.location.entity.iBeaconClass;

/**
 * beacon扫描类;蓝牙扫描需要安卓机器BLE功能，android api开放接口是android 4.3之后，且必须蓝牙4.0以上。
 * 
 * @author dingtao
 *
 */
@SuppressLint("NewApi")
public class BeaconSensor {
	private static BeaconSensor instance = null;
	private static final String TAG = "BeaconSensor";
	private Context context;
	private BluetoothAdapter mBluetoothAdapter = null;

	/** 得到该类的单例 */
	public synchronized static BeaconSensor getInstance() {
		if (instance == null) {
			instance = new BeaconSensor();
		}
		return instance;
	}

	private BluetoothAdapter.LeScanCallback mLeScanCallback;
	private HashMap<String, Integer> mMap = new HashMap<String, Integer>();

	private BeaconSensor() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
			mLeScanCallback = new LeScanCallback() {

				@Override
				public void onLeScan(final BluetoothDevice device, int rssi,
						byte[] scanRecord) {
//					 RMLog.i("rtmap",
//					 "地址："+device.getAddress()+"   信号强度："+rssi+"    名字："+device.getName()+"   长度："+scanRecord.length);
					if (getBeaconType(device) == Type.X_BEACON) {
						BeaconInfo macRssVal = SpecialBeacon.decodeXbeacon(
								device, rssi, scanRecord);
						if (macRssVal != null) {
							BeaconEntity.getInstance().put(macRssVal);
						}
					} else {
						final BeaconInfo ibeacon = iBeaconClass.fromScanData(
								device, rssi, scanRecord);
						if (ibeacon != null) {
							String mac = ibeacon.proximityUuid.substring(0, 4)
									+ String.format("%04x", ibeacon.major)
									+ String.format("%04x", ibeacon.minor);
							ibeacon.mac = mac;
							ibeacon.chennal = Type.DEFAULT_BEACON;
							BeaconEntity.getInstance().put(ibeacon);
						}
					}

				}
			};
		}
	}

	private Type getBeaconType(final BluetoothDevice device) {
		if (device != null && device.getName() != null
				&& device.getName().equalsIgnoreCase("xbeacon")) {
			return Type.X_BEACON;
		}
		return Type.DEFAULT_BEACON;
	}

	public boolean init(Context c) {
		boolean ret = false;
		synchronized (BeaconSensor.this) {
			context = c;
			if (isSuportBeacon(context)) {
				final BluetoothManager bluetoothManager = (BluetoothManager) context
						.getSystemService(Context.BLUETOOTH_SERVICE);
				mBluetoothAdapter = bluetoothManager.getAdapter();
				ret = mBluetoothAdapter.isEnabled();
			}
		}
		return ret;
	}

	public static boolean isSuportBeacon(Context context) {
		boolean ret = false;
		try {
			ret = context.getPackageManager().hasSystemFeature(
					PackageManager.FEATURE_BLUETOOTH_LE);
		} catch (Exception e) {
			RMLog.e(TAG, "isSuportBeacon", e);
		}
		return ret
				&& Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
	}

	public boolean isBlueToothOpen() {
		boolean ret = false;
		if (mBluetoothAdapter != null) {
			ret = mBluetoothAdapter.isEnabled();
		}
		return ret;
	}

	@SuppressWarnings("deprecation")
	public void start() {
		boolean isOpen = isBlueToothOpen();
		LocationApp.getInstance().setBlueOpen(isOpen);
		if (isSuportBeacon(context) && isOpen)
			mBluetoothAdapter.startLeScan(mLeScanCallback);
	}

	@SuppressWarnings("deprecation")
	public void stop() {
		if (isSuportBeacon(context) && mLeScanCallback != null
				&& mBluetoothAdapter != null)
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
	}

	public void destory() {
		if (mBluetoothAdapter != null) {
			mBluetoothAdapter = null;
		}
	}

	// /**
	// * 得到该类的单例
	// *
	// * @return
	// */
	// public synchronized static BeaconSensor getInstance() {
	// if (instance == null) {
	// instance = new BeaconSensor();
	// }
	// return instance;
	// }
	//
	// private ScanCallback mScanCallback;
	//
	// private LeScanCallback mLeScanCallback;
	//
	// /**
	// * 构造方法中根据不同版本初始化扫描回调callback
	// */
	// private BeaconSensor() {
	// Log.i("rtmap", "版本；"+Build.VERSION.SDK_INT);
	// if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
	// && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
	// mLeScanCallback = new LeScanCallback() {
	//
	// @Override
	// public void onLeScan(final BluetoothDevice device, int rssi,
	// byte[] scanRecord) {
	// if (getBeaconType(device) == BeaconType.X_BEACON) {
	// MacRssEntity macRssVal = SpecialBeacon.decodeXbeacon(
	// device, rssi, scanRecord);
	// if (macRssVal != null) {
	// BeaconEntity.getInstance().put(macRssVal);
	// }
	// } else {
	// final BeaconInfo ibeacon = iBeaconClass.fromScanData(
	// device, rssi, scanRecord);
	// if (ibeacon != null) {
	// String mac = ibeacon.proximityUuid.substring(0, 4)
	// + String.format("%04x", ibeacon.major)
	// + String.format("%04x", ibeacon.minor);
	// MacRssEntity macRssVal = new MacRssEntity(mac,
	// ibeacon.rssi, MacRssEntity.Type.ibeacon);
	// BeaconEntity.getInstance().put(macRssVal);
	// }
	// }
	//
	// }
	// };
	// } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
	// mScanCallback = new ScanCallback() {
	// public void onScanResult(int callbackType, ScanResult result) {
	// BluetoothDevice device = result.getDevice();
	// if (getBeaconType(device) == BeaconType.X_BEACON) {
	// MacRssEntity macRssVal = SpecialBeacon.decodeXbeacon(
	// device, result.getRssi(), result
	// .getScanRecord().getBytes());
	// if (macRssVal != null) {
	// BeaconEntity.getInstance().put(macRssVal);
	// }
	// } else {
	// final BeaconInfo ibeacon = iBeaconClass.fromScanData(
	// device, result.getRssi(), result
	// .getScanRecord().getBytes());
	// if (ibeacon != null) {
	// String mac = ibeacon.proximityUuid.substring(0, 4)
	// + String.format("%04x", ibeacon.major)
	// + String.format("%04x", ibeacon.minor);
	// MacRssEntity macRssVal = new MacRssEntity(mac,
	// ibeacon.rssi, MacRssEntity.Type.ibeacon);
	// BeaconEntity.getInstance().put(macRssVal);
	// }
	// }
	// }
	//
	// public void onScanFailed(int errorCode) {
	// RMLog.e(TAG, "Rtmap beacon scanner errorCode ： "
	// + errorCode);
	// }
	// };
	// }
	// }
	//
	// private BeaconType getBeaconType(final BluetoothDevice device) {
	// if (device != null && device.getName() != null
	// && device.getName().equalsIgnoreCase("xbeacon")) {
	// return BeaconType.X_BEACON;
	// }
	// return BeaconType.DEFAULT_BEACON;
	// }
	//
	// public boolean init(Context c) {
	// boolean ret = false;
	// synchronized (BeaconSensor.this) {
	// context = c;
	// if (isSuportBeacon(context)) {
	// final BluetoothManager bluetoothManager = (BluetoothManager) context
	// .getSystemService(Context.BLUETOOTH_SERVICE);
	// mBluetoothAdapter = bluetoothManager.getAdapter();
	// ret = mBluetoothAdapter.isEnabled();
	// }
	// }
	// return ret;
	// }
	//
	// public static boolean isSuportBeacon(Context context) {
	// boolean ret = false;
	// try {
	// ret = context.getPackageManager().hasSystemFeature(
	// PackageManager.FEATURE_BLUETOOTH_LE);
	// } catch (Exception e) {
	// RMLog.e(TAG, "isSuportBeacon", e);
	// }
	// return ret;
	// }
	//
	// public boolean isBlueToothOpen() {
	// boolean ret = false;
	// if (mBluetoothAdapter != null) {
	// ret = mBluetoothAdapter.isEnabled();
	// }
	// return ret;
	// }
	//
	// @SuppressWarnings("deprecation")
	// public void start() {
	// if (isSuportBeacon(context) && isBlueToothOpen()) {
	// if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
	// && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
	// mBluetoothAdapter.startLeScan(mLeScanCallback);
	// } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
	// mBluetoothAdapter.getBluetoothLeScanner().startScan(
	// mScanCallback);
	// }
	// }
	// }
	//
	// @SuppressWarnings("deprecation")
	// public void stop() {
	// if (isSuportBeacon(context) && isBlueToothOpen()) {
	// if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
	// && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
	// mBluetoothAdapter.stopLeScan(mLeScanCallback);
	// } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
	// mBluetoothAdapter.getBluetoothLeScanner().stopScan(
	// mScanCallback);
	// }
	// }
	// }
}
