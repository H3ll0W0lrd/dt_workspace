package com.minnw.beacon;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;

import com.minnw.tools.Tools;

/**
 * Service for managing connection and data communication with a GATT server
 * hosted on a given Bluetooth LE device.
 */
public class UartService extends Service {
	private static String TAG = "tag";
	/**
	 * 基本信息数据  。
	 */
	public static List<String> listdataservice = new ArrayList<String>();
	/**
	 * 系统信息数据  。
	 */
	public static List<String> syslistdata = new ArrayList<String>();
	/**
	 * 基本信息的序列号 数据  。
	 */
	public static List<String> listserviceIndexdata = new ArrayList<String>();
	private BluetoothManager mBluetoothManager;
	private BluetoothAdapter mBluetoothAdapter;
	/**
	 * 系统信息  要发送到ibeacon的characteristic
	 */
	public int sys_characteristicIndex = 0;
	private BluetoothGatt mBluetoothGatt;
	private int mConnectionState = STATE_DISCONNECTED;
	/**
	 * 连接成功了，读取设备信息
	 */
	public boolean readSysinfo = false;
	private static int STATE_DISCONNECTED = 0;
	private static int STATE_CONNECTING = 1;
	private static int STATE_CONNECTED = 2;

	public static String ACTION_GATT_CONNECTED = "com.nordicsemi.nrfUART.ACTION_GATT_CONNECTED";
	public static String ACTION_GATT_DISCONNECTED = "com.nordicsemi.nrfUART.ACTION_GATT_DISCONNECTED";
	public static String ACTION_GATT_SERVICES_DISCOVERED = "com.nordicsemi.nrfUART.ACTION_GATT_SERVICES_DISCOVERED";
	public static String ACTION_DATA_AVAILABLE = "com.nordicsemi.nrfUART.ACTION_DATA_AVAILABLE";
	public static String EXTRA_DATA = "com.nordicsemi.nrfUART.EXTRA_DATA";
	public static String DEVICE_DOES_NOT_SUPPORT_UART = "com.nordicsemi.nrfUART.DEVICE_DOES_NOT_SUPPORT_UART";

	public static UUID TX_POWER_UUID = UUID
			.fromString("00001804-0000-1000-8000-00805f9b34fb");
	public static UUID TX_POWER_LEVEL_UUID = UUID
			.fromString("00002a07-0000-1000-8000-00805f9b34fb");
	public static UUID CCCD = UUID
			.fromString("00002902-0000-1000-8000-00805f9b34fb");
	public static UUID FIRMWARE_REVISON_UUID = UUID
			.fromString("00002a26-0000-1000-8000-00805f9b34fb");
	public static UUID DIS_UUID = UUID
			.fromString("0000180a-0000-1000-8000-00805f9b34fb");
	public static UUID RX_SERVICE_UUID = UUID
			.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
	public static UUID RX_CHAR_UUID = UUID
			.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
	public static UUID TX_CHAR_UUID = UUID
			.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
	/**电池服务
	 */
	private  int Battery_Service = 2;
	/**
	 *连接成功   3秒之内要把这个权鉴发送到Ibeacon
	 */
	private   String    md5="AcCrEdItiSOK";
	
	
	
	private IBinder mBinder = new LocalBinder();
	// private Map<String,BluetoothGattCallback> BluetoothGattCallbackMAP=new
	// HashMap<String, BluetoothGattCallback>();
	// Implements callback methods for GATT events that the app cares about. For
	// example,
	// connection change and services discovered.
	private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status,
				int newState) {
			int  i=BluetoothGatt.CONNECTION_PRIORITY_HIGH;
			String intentAction;
			if (newState == BluetoothProfile.STATE_CONNECTED && status != 133) {
				intentAction = ACTION_GATT_CONNECTED;
				mConnectionState = STATE_CONNECTED;
				Log.i(TAG, "Connected to GATT server.");
				broadcastUpdate(intentAction);
				mBluetoothGatt.discoverServices();
			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				intentAction = ACTION_GATT_DISCONNECTED;
				mConnectionState = STATE_DISCONNECTED;
				Log.d(TAG, "Disconnected from GATT server.");
				broadcastUpdate(intentAction);
				 
			}
			/***
			 * 5秒之内 执行到这里 就取消 消息发送。
			 */
		 
			DeviceListActivity.mHandler
					.removeMessages(DeviceListActivity.connectGatt_no_state);
			characteristicIndex = 0;
			sys_characteristicIndex = 0;
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				// BluetoothGattCallbackMAP.put(gatt.getDevice().getAddress(),
				// this);
				Log.i(TAG, "onServicesDiscovered = ");
				broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
				getServicesManage(gatt, status);
				getDeviceAllDataManage(gatt, status);
			} else {
				Log.w(TAG, "onServicesDiscovered received: " + status);
			}
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			Log.i(TAG, "onCharacteristicWrite");
			super.onCharacteristicWrite(gatt, characteristic, status);
			writeValueManage(gatt, status);
		}

		@Override
		public void onDescriptorWrite(BluetoothGatt gatt,
				BluetoothGattDescriptor descriptor, int status) {
			Log.i(TAG, "onDescriptorWrite");
			super.onDescriptorWrite(gatt, descriptor, status);
			writeValueManage(gatt, status);
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
//			Log.i(TAG, "onCharacteristicRead");
			if (status == BluetoothGatt.GATT_SUCCESS) {
				broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
			}
			readValueManage(gatt, characteristic, status);
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic) {
			Log.i(TAG, "onCharacteristicChanged");
			broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
		}
	};
	/**
	 * 连接成功了 写入数据到设备。
	 */
	private boolean write_uuid = false;

	private String getDeviceAddress(BluetoothGatt gatt) {
		return gatt.getDevice().getAddress();
	}

	public void write_uuid(String writeValue, int serviceIndex,
			int characteristicIndex) {
		write_uuid = true;
		Log.i(TAG, "write_uuid=" + writeValue + serviceIndex+ characteristicIndex);
		if (true) {
			BluetoothGattCharacteristic characteristic = mBluetoothGatt
					.getServices().get(serviceIndex).getCharacteristics()
					.get(characteristicIndex);
			byte[] value = Tools.decodeBase64(writeValue);
			characteristic.setValue(value);
			mBluetoothGatt.writeCharacteristic(characteristic);
		} else {
			// BluetoothGattDescriptor descriptor =
			// mBluetoothGatt.getServices().get(serviceIndex).getCharacteristics()
			// .get(characteristicIndex).getDescriptors().get(Integer.parseInt(descriptorIndex));
			// descriptor.setValue(Tools.decodeBase64(writeValue));
			// mBluetoothGatts.get(deviceAddress).writeDescriptor(descriptor);
		}

	}

	// 1 在onServicesDiscovered回调方法里面开始
	/**
	 * 这个服务"serviceUUID":"0000fff0-0000-1000-8000-00805f9b34fb"的serviceIndex写入 md权鉴
	 * @param gatt
	 * @param services
	 * @param deviceAddress
	 */
	public void write_AcCrEdItiSOK(BluetoothGatt gatt,
			List<BluetoothGattService> services, String deviceAddress) {
		/**
		 * serviceIndex和这个serviceIndex_readValue一样    但是为了提前发送md权鉴   
		 * serviceIndex_readValue这个时候还没有值。
		 * write_AcCrEdItiSOK这个方法先执行了   getDeviceAllDataManage后面执行
		 */
		int serviceIndex = 0;
		if (gatt.getServices().size() == 6) {
			serviceIndex = 4;
		} else {
			serviceIndex = services.size();
		}
		List<BluetoothGattCharacteristic> characteristics = services.get(
				serviceIndex - 1).getCharacteristics();
		BluetoothGattCharacteristic characteristic = null;
		for (BluetoothGattCharacteristic character : characteristics) {
			if (character.getUuid().compareTo(Tools.PRIVATE_UUID) != -1) {
				characteristic = character;
				break;
			}
		}
		if (characteristic != null) {
			
				//0x4A,0x66,0x33,0x36,0x30,0x74,0x73,0x41,0x75,0x74,0x68,0x4D        //Jf360tsAuthM
				byte[]  value={0x4A,0x66,0x33,0x36,0x30,0x74,0x73,0x41,0x75,0x74,0x68,0x4D };
				Log.i("tag","360ibeacon");
				characteristic.setValue(value);
				gatt.writeCharacteristic(characteristic);
		 
			
		} else {

		}
	}

	private void getServicesManage(BluetoothGatt gatt, int status) {
		String deviceAddress = getDeviceAddress(gatt);
		Log.i(TAG, "getServicesManage");
		if (!readSysinfo)
			write_AcCrEdItiSOK(gatt, gatt.getServices(), deviceAddress);
		JSONObject obj = new JSONObject();
		JSONArray ary = new JSONArray();
		Tools.addProperty(obj, Tools.DEVICE_ADDRESS, deviceAddress);
		for (int i = 0; i < gatt.getServices().size(); i++) {
			JSONObject infoObj = new JSONObject();
			Tools.addProperty(infoObj, Tools.SERVICE_INDEX, i);
			Tools.addProperty(infoObj, Tools.SERVICE_UUID, gatt.getServices()
					.get(i).getUuid());
			Tools.addProperty(infoObj, Tools.SERVICE_NAME,
					Tools.lookup(gatt.getServices().get(i).getUuid()));
			ary.put(infoObj);
		}
		Tools.addProperty(obj, Tools.SERVICES, ary);
		// Log.i(TAG, "getServicesManage" + obj.toString());
		// getServicesCC.get(deviceAddress).success(obj);
		// getServicesCC.remove(deviceAddress);

	}
	/**
	 * 基本信息的服务 
06-17 16:47:38.575: I/tag(12836): getDeviceAllDataManage====5
06-17 16:47:38.575: I/tag(12836): serviceInfo={"serviceIndex":0,"serviceUUID":"00001800-0000-1000-8000-00805f9b34fb","serviceName":"Generic Access"}
06-17 16:47:38.575: I/tag(12836): characteristicInfo={"characteristicIndex":0,"characteristicUUID":"00002a00-0000-1000-8000-00805f9b34fb","characteristicName":"Device Name","characteristicProperty":["read","write"]}
06-17 16:47:38.576: I/tag(12836): characteristicInfo={"characteristicIndex":1,"characteristicUUID":"00002a01-0000-1000-8000-00805f9b34fb","characteristicName":"Appearance","characteristicProperty":["read"]}
06-17 16:47:38.576: I/tag(12836): characteristicInfo={"characteristicIndex":2,"characteristicUUID":"00002a04-0000-1000-8000-00805f9b34fb","characteristicName":"Peripheral Preferred Connection Parameters","characteristicProperty":["read"]}
06-17 16:47:38.576: I/tag(12836): serviceInfo={"serviceIndex":1,"serviceUUID":"00001801-0000-1000-8000-00805f9b34fb","serviceName":"Generic Attribute"}
06-17 16:47:38.577: I/tag(12836): characteristicInfo={"characteristicIndex":0,"characteristicUUID":"00002a05-0000-1000-8000-00805f9b34fb","characteristicName":"Service Changed","characteristicProperty":["indicate"]}
06-17 16:47:38.577: I/tag(12836): serviceInfo={"serviceIndex":2,"serviceUUID":"0000180f-0000-1000-8000-00805f9b34fb","serviceName":"Battery Service"}
06-17 16:47:38.577: I/tag(12836): characteristicInfo={"characteristicIndex":0,"characteristicUUID":"00002a19-0000-1000-8000-00805f9b34fb","characteristicName":"Battery Level","characteristicProperty":["read","notify"]}
06-17 16:47:38.577: I/tag(12836): serviceInfo={"serviceIndex":3,"serviceUUID":"0000180a-0000-1000-8000-00805f9b34fb","serviceName":"Device Information"}
06-17 16:47:38.577: I/tag(12836): characteristicInfo={"characteristicIndex":0,"characteristicUUID":"00002a29-0000-1000-8000-00805f9b34fb","characteristicName":"Manufacturer Name String","characteristicProperty":["read"]}
06-17 16:47:38.578: I/tag(12836): characteristicInfo={"characteristicIndex":1,"characteristicUUID":"00002a24-0000-1000-8000-00805f9b34fb","characteristicName":"Model Number String","characteristicProperty":["read"]}
06-17 16:47:38.578: I/tag(12836): characteristicInfo={"characteristicIndex":2,"characteristicUUID":"00002a25-0000-1000-8000-00805f9b34fb","characteristicName":"Serial Number String","characteristicProperty":["read"]}
06-17 16:47:38.578: I/tag(12836): characteristicInfo={"characteristicIndex":3,"characteristicUUID":"00002a27-0000-1000-8000-00805f9b34fb","characteristicName":"Hardware Revision String","characteristicProperty":["read"]}
06-17 16:47:38.578: I/tag(12836): characteristicInfo={"characteristicIndex":4,"characteristicUUID":"00002a26-0000-1000-8000-00805f9b34fb","characteristicName":"Firmware Revision String","characteristicProperty":["read"]}
06-17 16:47:38.578: I/tag(12836): characteristicInfo={"characteristicIndex":5,"characteristicUUID":"00002a28-0000-1000-8000-00805f9b34fb","characteristicName":"Software Revision String","characteristicProperty":["read"]}
06-17 16:47:38.578: I/tag(12836): characteristicInfo={"characteristicIndex":6,"characteristicUUID":"00002a23-0000-1000-8000-00805f9b34fb","characteristicName":"System ID","characteristicProperty":["read"]}
06-17 16:47:38.579: I/tag(12836): characteristicInfo={"characteristicIndex":7,"characteristicUUID":"00002a2a-0000-1000-8000-00805f9b34fb","characteristicName":"IEEE 11073-20601 Regulatory Certification Data List","characteristicProperty":["read"]}
06-17 16:47:38.579: I/tag(12836): serviceInfo={"serviceIndex":4,"serviceUUID":"0000fff0-0000-1000-8000-00805f9b34fb","serviceName":"unknown"}
06-17 16:47:38.579: I/tag(12836): characteristicInfo={"characteristicIndex":0,"characteristicUUID":"0000fff1-0000-1000-8000-00805f9b34fb","characteristicName":"unknown","characteristicProperty":["read","write"]}
06-17 16:47:38.579: I/tag(12836): characteristicInfo={"characteristicIndex":1,"characteristicUUID":"0000fff2-0000-1000-8000-00805f9b34fb","characteristicName":"unknown","characteristicProperty":["read","write"]}
06-17 16:47:38.579: I/tag(12836): characteristicInfo={"characteristicIndex":2,"characteristicUUID":"0000fff3-0000-1000-8000-00805f9b34fb","characteristicName":"unknown","characteristicProperty":["read","write"]}
06-17 16:47:38.580: I/tag(12836): characteristicInfo={"characteristicIndex":3,"characteristicUUID":"0000fff4-0000-1000-8000-00805f9b34fb","characteristicName":"unknown","characteristicProperty":["read","write"]}
06-17 16:47:38.580: I/tag(12836): characteristicInfo={"characteristicIndex":4,"characteristicUUID":"0000fff5-0000-1000-8000-00805f9b34fb","characteristicName":"unknown","characteristicProperty":["read","write"]}
06-17 16:47:38.580: I/tag(12836): characteristicInfo={"characteristicIndex":5,"characteristicUUID":"0000fff6-0000-1000-8000-00805f9b34fb","characteristicName":"unknown","characteristicProperty":["write"]}
06-17 16:47:38.580: I/tag(12836): characteristicInfo={"characteristicIndex":6,"characteristicUUID":"0000fff7-0000-1000-8000-00805f9b34fb","characteristicName":"unknown","characteristicProperty":["read","write"]}
06-17 16:47:38.580: I/tag(12836): characteristicInfo={"characteristicIndex":7,"characteristicUUID":"0000fff8-0000-1000-8000-00805f9b34fb","characteristicName":"unknown","characteristicProperty":["read","write"]}
06-17 16:47:38.581: I/tag(12836): characteristicInfo={"characteristicIndex":8,"characteristicUUID":"0000fff9-0000-1000-8000-00805f9b34fb","characteristicName":"unknown","characteristicProperty":["read","write"]}
06-17 16:47:38.581: I/tag(12836): characteristicInfo={"characteristicIndex":9,"characteristicUUID":"0000fffa-0000-1000-8000-00805f9b34fb","characteristicName":"unknown","characteristicProperty":["read","write"]}
06-17 16:47:38.581: I/tag(12836): characteristicInfo={"characteristicIndex":10,"characteristicUUID":"0000fffe-0000-1000-8000-00805f9b34fb","characteristicName":"unknown","characteristicProperty":["read","write"]}
06-17 16:47:38.581: I/tag(12836): characteristicInfo={"characteristicIndex":11,"characteristicUUID":"0000ffff-0000-1000-8000-00805f9b34fb","characteristicName":"unknown","characteristicProperty":["write"]}

	 */
	private void getDeviceAllDataManage(BluetoothGatt gatt, int status) {
		String deviceAddress = getDeviceAddress(gatt);
		if (status == BluetoothGatt.GATT_SUCCESS) {
			JSONObject obj = new JSONObject();
			JSONArray servicesInfo = new JSONArray();
			List<BluetoothGattService> services = gatt.getServices();
			 Log.i(TAG, "getDeviceAllDataManage====" );
			for (int i = 0; i < services.size(); i++) {
				JSONObject serviceInfo = new JSONObject();
				Tools.addProperty(serviceInfo, Tools.SERVICE_INDEX, i);
				Tools.addProperty(serviceInfo, Tools.SERVICE_UUID, services
						.get(i).getUuid());
				Tools.addProperty(serviceInfo, Tools.SERVICE_NAME,
						Tools.lookup(services.get(i).getUuid()));
				List<BluetoothGattCharacteristic> characteristics = services
						.get(i).getCharacteristics();
				if (services.get(i).getUuid().toString().startsWith("0000180a")) {
					sys_serviceIndex = i;
				}
				if (services.get(i).getUuid().toString().startsWith("0000fff0")) {
					serviceIndex_readValue = i;
				}
				if (services.get(i).getUuid().toString().startsWith("0000180f")) {
					Battery_Service = i;
				}
				 Log.i(TAG, "serviceInfo="+serviceInfo.toString());
				JSONArray characteristicsInfo = new JSONArray();
				for (int j = 0; j < characteristics.size(); j++) {
					JSONObject characteristicInfo = new JSONObject();
					Tools.addProperty(characteristicInfo,
							Tools.CHARACTERISTIC_INDEX, j);
					Tools.addProperty(characteristicInfo,
							Tools.CHARACTERISTIC_UUID, characteristics.get(j)
									.getUuid());
					Tools.addProperty(characteristicInfo,
							Tools.CHARACTERISTIC_NAME,
							Tools.lookup(characteristics.get(j).getUuid()));
					Tools.addProperty(characteristicInfo,
							Tools.CHARACTERISTIC_PROPERTY, Tools
									.decodeProperty(characteristics.get(j)
											.getProperties()));
					 Log.i(TAG,
					 "characteristicInfo="+characteristicInfo.toString());
					List<BluetoothGattDescriptor> descriptors = new ArrayList<BluetoothGattDescriptor>();
					JSONArray descriptorsInfo = new JSONArray();
					for (int k = 0; k < descriptors.size(); k++) {
						JSONObject descriptorInfo = new JSONObject();
						Tools.addProperty(descriptorInfo,
								Tools.DESCRIPTOR_INDEX, k);
						Tools.addProperty(descriptorInfo,
								Tools.DESCRIPTOR_UUID, descriptors.get(k)
										.getUuid());
						Tools.addProperty(descriptorInfo,
								Tools.DESCRIPTOR_NAME,
								Tools.lookup(descriptors.get(k).getUuid()));
						descriptorsInfo.put(descriptorInfo);
						 Log.i(TAG,
						 "descriptorInfo="+descriptorInfo.toString());
					}
					Tools.addProperty(characteristicInfo, Tools.DESCRIPTORS,descriptorsInfo);
					characteristicsInfo.put(characteristicInfo);
				}
				Tools.addProperty(serviceInfo, Tools.CHARACTERISTICS,
						characteristicsInfo);
				servicesInfo.put(serviceInfo);
			}
			Tools.addProperty(obj, Tools.DEVICE_ADDRESS, deviceAddress);
			Tools.addProperty(obj, Tools.SERVICES, servicesInfo);
			// getDeviceAllDataCC.get(deviceAddress).success(obj);
			// getDeviceAllDataCC.remove(deviceAddress);
			// deviceServices.put(deviceAddress, services);
			// Log.i(TAG, "getDeviceAllDataManage" + obj.toString());
			
		} else {
			// Tools.sendErrorMsg(getDeviceAllDataCC.get(deviceAddress));
			// getDeviceAllDataCC.remove(deviceAddress);
		}

	}
	// 2
	private void writeValueManage(BluetoothGatt gatt, int status) {
		Log.d(TAG, "writeValueManage-----------"+write_uuid+readSysinfo);
		if (status == BluetoothGatt.GATT_SUCCESS) {
			// Tools.sendSuccessMsg(writeValueCC.get(deviceAddress));
			// writeValueCC.remove(deviceAddress);
		} else {
			// Tools.sendErrorMsg(writeValueCC.get(deviceAddress));
			// writeValueCC.remove(deviceAddress);
		}
		if (!write_uuid) {
			if (!readSysinfo)
				readValueBluetoothGatt(gatt);
		}
		write_uuid = false;
	}

	/**
	 * 基本信息的服务 
06-17 16:47:38.575: I/tag(12836): getDeviceAllDataManage====5
06-17 16:47:38.575: I/tag(12836): serviceInfo={"serviceIndex":0,"serviceUUID":"00001800-0000-1000-8000-00805f9b34fb","serviceName":"Generic Access"}
06-17 16:47:38.575: I/tag(12836): characteristicInfo={"characteristicIndex":0,"characteristicUUID":"00002a00-0000-1000-8000-00805f9b34fb","characteristicName":"Device Name","characteristicProperty":["read","write"]}
06-17 16:47:38.576: I/tag(12836): characteristicInfo={"characteristicIndex":1,"characteristicUUID":"00002a01-0000-1000-8000-00805f9b34fb","characteristicName":"Appearance","characteristicProperty":["read"]}
06-17 16:47:38.576: I/tag(12836): characteristicInfo={"characteristicIndex":2,"characteristicUUID":"00002a04-0000-1000-8000-00805f9b34fb","characteristicName":"Peripheral Preferred Connection Parameters","characteristicProperty":["read"]}
06-17 16:47:38.576: I/tag(12836): serviceInfo={"serviceIndex":1,"serviceUUID":"00001801-0000-1000-8000-00805f9b34fb","serviceName":"Generic Attribute"}
06-17 16:47:38.577: I/tag(12836): characteristicInfo={"characteristicIndex":0,"characteristicUUID":"00002a05-0000-1000-8000-00805f9b34fb","characteristicName":"Service Changed","characteristicProperty":["indicate"]}
06-17 16:47:38.577: I/tag(12836): serviceInfo={"serviceIndex":2,"serviceUUID":"0000180f-0000-1000-8000-00805f9b34fb","serviceName":"Battery Service"}
06-17 16:47:38.577: I/tag(12836): characteristicInfo={"characteristicIndex":0,"characteristicUUID":"00002a19-0000-1000-8000-00805f9b34fb","characteristicName":"Battery Level","characteristicProperty":["read","notify"]}
06-17 16:47:38.577: I/tag(12836): serviceInfo={"serviceIndex":3,"serviceUUID":"0000180a-0000-1000-8000-00805f9b34fb","serviceName":"Device Information"}
06-17 16:47:38.577: I/tag(12836): characteristicInfo={"characteristicIndex":0,"characteristicUUID":"00002a29-0000-1000-8000-00805f9b34fb","characteristicName":"Manufacturer Name String","characteristicProperty":["read"]}
06-17 16:47:38.578: I/tag(12836): characteristicInfo={"characteristicIndex":1,"characteristicUUID":"00002a24-0000-1000-8000-00805f9b34fb","characteristicName":"Model Number String","characteristicProperty":["read"]}
06-17 16:47:38.578: I/tag(12836): characteristicInfo={"characteristicIndex":2,"characteristicUUID":"00002a25-0000-1000-8000-00805f9b34fb","characteristicName":"Serial Number String","characteristicProperty":["read"]}
06-17 16:47:38.578: I/tag(12836): characteristicInfo={"characteristicIndex":3,"characteristicUUID":"00002a27-0000-1000-8000-00805f9b34fb","characteristicName":"Hardware Revision String","characteristicProperty":["read"]}
06-17 16:47:38.578: I/tag(12836): characteristicInfo={"characteristicIndex":4,"characteristicUUID":"00002a26-0000-1000-8000-00805f9b34fb","characteristicName":"Firmware Revision String","characteristicProperty":["read"]}
06-17 16:47:38.578: I/tag(12836): characteristicInfo={"characteristicIndex":5,"characteristicUUID":"00002a28-0000-1000-8000-00805f9b34fb","characteristicName":"Software Revision String","characteristicProperty":["read"]}
06-17 16:47:38.578: I/tag(12836): characteristicInfo={"characteristicIndex":6,"characteristicUUID":"00002a23-0000-1000-8000-00805f9b34fb","characteristicName":"System ID","characteristicProperty":["read"]}
06-17 16:47:38.579: I/tag(12836): characteristicInfo={"characteristicIndex":7,"characteristicUUID":"00002a2a-0000-1000-8000-00805f9b34fb","characteristicName":"IEEE 11073-20601 Regulatory Certification Data List","characteristicProperty":["read"]}
06-17 16:47:38.579: I/tag(12836): serviceInfo={"serviceIndex":4,"serviceUUID":"0000fff0-0000-1000-8000-00805f9b34fb","serviceName":"unknown"}
06-17 16:47:38.579: I/tag(12836): characteristicInfo={"characteristicIndex":0,"characteristicUUID":"0000fff1-0000-1000-8000-00805f9b34fb","characteristicName":"unknown","characteristicProperty":["read","write"]}
06-17 16:47:38.579: I/tag(12836): characteristicInfo={"characteristicIndex":1,"characteristicUUID":"0000fff2-0000-1000-8000-00805f9b34fb","characteristicName":"unknown","characteristicProperty":["read","write"]}
06-17 16:47:38.579: I/tag(12836): characteristicInfo={"characteristicIndex":2,"characteristicUUID":"0000fff3-0000-1000-8000-00805f9b34fb","characteristicName":"unknown","characteristicProperty":["read","write"]}
06-17 16:47:38.580: I/tag(12836): characteristicInfo={"characteristicIndex":3,"characteristicUUID":"0000fff4-0000-1000-8000-00805f9b34fb","characteristicName":"unknown","characteristicProperty":["read","write"]}
06-17 16:47:38.580: I/tag(12836): characteristicInfo={"characteristicIndex":4,"characteristicUUID":"0000fff5-0000-1000-8000-00805f9b34fb","characteristicName":"unknown","characteristicProperty":["read","write"]}
06-17 16:47:38.580: I/tag(12836): characteristicInfo={"characteristicIndex":5,"characteristicUUID":"0000fff6-0000-1000-8000-00805f9b34fb","characteristicName":"unknown","characteristicProperty":["write"]}
06-17 16:47:38.580: I/tag(12836): characteristicInfo={"characteristicIndex":6,"characteristicUUID":"0000fff7-0000-1000-8000-00805f9b34fb","characteristicName":"unknown","characteristicProperty":["read","write"]}
06-17 16:47:38.580: I/tag(12836): characteristicInfo={"characteristicIndex":7,"characteristicUUID":"0000fff8-0000-1000-8000-00805f9b34fb","characteristicName":"unknown","characteristicProperty":["read","write"]}
06-17 16:47:38.581: I/tag(12836): characteristicInfo={"characteristicIndex":8,"characteristicUUID":"0000fff9-0000-1000-8000-00805f9b34fb","characteristicName":"unknown","characteristicProperty":["read","write"]}
06-17 16:47:38.581: I/tag(12836): characteristicInfo={"characteristicIndex":9,"characteristicUUID":"0000fffa-0000-1000-8000-00805f9b34fb","characteristicName":"unknown","characteristicProperty":["read","write"]}
06-17 16:47:38.581: I/tag(12836): characteristicInfo={"characteristicIndex":10,"characteristicUUID":"0000fffe-0000-1000-8000-00805f9b34fb","characteristicName":"unknown","characteristicProperty":["read","write"]}
06-17 16:47:38.581: I/tag(12836): characteristicInfo={"characteristicIndex":11,"characteristicUUID":"0000ffff-0000-1000-8000-00805f9b34fb","characteristicName":"unknown","characteristicProperty":["write"]}

	 */
	/**
	 * 在"serviceIndex":4中  6  10不去读。因为不需要这两个数据。
	 */
	int characteristicIndex = 0;
	/**
	 * 在这个数组里的数字  ，   需要多次编码  解码。
	 */
	int[] hexArray = { 0, 2, 3, 5, 7, 8, 11 };

	// 4
	/**
	 * 有些  characteristic需要多次编解码  
	 * @param gatt
	 * @param characteristic
	 * @param status
	 */
	private void readValueManage(BluetoothGatt gatt,
			BluetoothGattCharacteristic characteristic, int status) {
//		Log.i(TAG, "readValueManage   " + characteristic.toString() + "  "+ readSysinfo);
//		String deviceAddress = getDeviceAddress(gatt);
		JSONObject obj = new JSONObject();
//		String uuid = characteristic.getUuid().toString();
		// if (readValueCC.get(uuid) != null) {
		if (status == BluetoothGatt.GATT_SUCCESS) {
//			Tools.addProperty(obj, Tools.DEVICE_ADDRESS, deviceAddress);
//			Tools.addProperty(obj, Tools.VALUE,
//					Tools.encodeBase64(characteristic.getValue()));
//			Tools.addProperty(obj, Tools.DATE, Tools.getDateString());
			// readValueCC.get(uuid).success(obj);
			// readValueCC.remove(uuid);
			// }else {
			// Tools.sendErrorMsg(readValueCC.get(uuid));
			// readValueCC.remove(uuid);
			// }
			byte[] aa = characteristic.getValue();
			int[] arr = new int[characteristic.getValue().length];
			StringBuffer sb = new StringBuffer();
			StringBuffer sb1 = new StringBuffer();
		
				if (readSysinfo) {
					for (int i = 0; i < aa.length; i++) {
					if (aa[i] < 0) {
						arr[i] = Integer.valueOf(Integer.toBinaryString(aa[i])
								.substring(24), 2);
						if (sys_characteristicIndex == 7
								|| sys_characteristicIndex == 6)
							sb1.append(arr[i]);
						else
							sb1.append((char) arr[i]);
					} else {
						arr[i] = aa[i];
						if (sys_characteristicIndex == 7|| sys_characteristicIndex == 6)
							sb1.append(arr[i]);
						else
							sb1.append((char) arr[i]);
					}
					if (sys_characteristicIndex == 7|| sys_characteristicIndex == 6) {
						if (arr[i] < 16) {
							String m = "0" + Integer.toHexString(arr[i]);
							sb.append(m);
						} else {
							String m = Integer.toHexString(arr[i]);
							sb.append(m);
						}
					}}
					

					if (sys_characteristicIndex == 7
							|| sys_characteristicIndex == 6)
						syslistdata.add(sb.toString().trim());
					else
						syslistdata.add(sb1.toString().trim());
					Log.i(TAG, "readValueManage    sys_characteristicIndex="
							+ sys_characteristicIndex + " ---" + sb1.toString()
							+ "--" + sb.toString() + " ---" + obj.toString());

					if (syslistdata.size() >= 8) {
						readSysinfo = false;
						sys_characteristicIndex = 0;
					}
					if (sys_characteristicIndex <= 7 && readSysinfo) {
						sys_characteristicIndex++;
						readValueSys(false);
					}

				
				} else {

					
				 for (int i = 0; i < aa.length; i++) {
					
					if (aa[i] < 0) {
						arr[i] = Integer.valueOf(Integer.toBinaryString(aa[i])
								.substring(24), 2);
						sb1.append(arr[i]);
					} else {
						arr[i] = aa[i];
						sb1.append(arr[i]);
					}
					if (characteristicIndex == 9) {
						sb.append((char) arr[i]);
					} else if (characteristicIndex == 4) {
						if (aa.length == 1) {
							sb.append(aa[i]);
							sb.append(" dBm");
						} else {
							if (i == 0) {
								sb.append(aa[i]);
							} else if (i == (aa.length - 1)) {
								sb.append(aa[i]);
								sb.append(" dBm");
							} else {
								sb.append(aa[i]);
							}
						}

					} else {
						if (arr[i] < 16) {
							String m = "0" + Integer.toHexString(arr[i]);
							sb.append(m);
						} else {
							String m = Integer.toHexString(arr[i]);
							sb.append(m);
						}
					}
				}

					if (contains(hexArray, characteristicIndex)) {
						listdataservice.add(Long.valueOf(sb.toString().trim(), 16)
								.toString());
					 
					} else {
						if (characteristicIndex == 1) {
							listdataservice.add(formatUUID(sb.toString().trim().toUpperCase()));
						} else
							listdataservice.add(sb.toString().trim());
					}
					listserviceIndexdata.add(serviceIndex_readValue + ";"+ (characteristicIndex - 1));

//					Log.i(TAG, "readValueManage    characteristicIndex="+ characteristicIndex + " ---" + sb1.toString() + "--"+ sb.toString() + obj.toString());

					if (characteristicIndex <= 10) {
						if (characteristicIndex == 5) {
							characteristicIndex++;
							readValue(gatt, characteristicIndex);
							characteristicIndex++;
						} else if (characteristicIndex == 9) {
							characteristicIndex++;
							readValue(gatt, characteristicIndex);
							characteristicIndex++;
						} else {
							readValue(gatt, characteristicIndex);
							characteristicIndex++;
						}
					}
					if (listdataservice.size() >= 10) {
						DeviceListActivity.mHandler.sendEmptyMessage(DeviceListActivity.start_detilactivity);
					}
				
			}
			 
		}

		 
	}

	private String formatUUID(String str) {
		if (str.length() < 32) {
			return str;
		}
		return str.substring(0, 8) + '-' + str.substring(8, 12) + '-'
				+ str.substring(12, 16) + '-' + str.substring(16, 20) + '-'
				+ str.substring(20, 32);
	}

	public boolean contains(int[] array, int v) {
		for (int e : array) {
			if (e == v)
				return true;
		}
		return false;
	}

	public String asciiToString(String text) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) == '1' && i < text.length() - 2) {
				int code = Integer.parseInt(text.substring(i, i + 3));
				builder.append((char) code);
				i += 2;
			} else if (i < text.length() - 1) {
				int code = Integer.parseInt(text.substring(i, i + 2));
				builder.append((char) code);
				i += 1;
			}
		}
		return builder.toString();
	}

	/**
	 * 服务总数中， 系统信息服务的排位
	 */
	int sys_serviceIndex = 2;

	// 1
	/**
	 * 连接成功了，读取设备信息。
	 * 
	 */
	public void readValueSys(boolean first) {
		readSysinfo = true;
		if (first) {
			mBluetoothGatt.discoverServices();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
		}
		BluetoothGattCharacteristic characteristic = mBluetoothGatt
				.getServices().get(sys_serviceIndex).getCharacteristics()
				.get(sys_characteristicIndex);
		mBluetoothGatt.readCharacteristic(characteristic);
		Log.i(TAG, "readValueSys    " + sys_serviceIndex + "  "
				+ sys_characteristicIndex);

	}

	// 5
	/**
	 * 服务总数中    基本信息服务的排位        
	 */
	int serviceIndex_readValue = 4;
    /**
     * 读取基本信息   
     * @param gatt
     * @param characteristicIndex
     */
	public void readValue(BluetoothGatt gatt, int characteristicIndex) {
		BluetoothGattCharacteristic characteristic = gatt.getServices()
				.get(serviceIndex_readValue).getCharacteristics()
				.get(characteristicIndex);
		gatt.readCharacteristic(characteristic);
//		Log.i(TAG, "readValue       " +serviceIndex_readValue+ characteristicIndex);

	}

	// 3   
	/**
	 * 发送电池characteristic    ibeacon会返回电池电量
	 */
	public void readValueBluetoothGatt(BluetoothGatt gatt) {
		int characteristicIndex = 0;
		BluetoothGattCharacteristic characteristic = gatt.getServices()
				.get(Battery_Service).getCharacteristics()
				.get(characteristicIndex);
		gatt.readCharacteristic(characteristic);
		Log.i(TAG, "readValueBluetoothGatt  " +Battery_Service+ 0);

	}

	private void broadcastUpdate(String action) {
		Intent intent = new Intent(action);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}

	private void broadcastUpdate(String action,
			BluetoothGattCharacteristic characteristic) {
		Intent intent = new Intent(action);

		// This is special handling for the Heart Rate Measurement profile. Data
		// parsing is
		// carried out as per profile specifications:
		// http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
		if (TX_CHAR_UUID.equals(characteristic.getUuid())) {

			// Log.d(TAG,
			// String.format("Received TX: %d",characteristic.getValue() ));
			intent.putExtra(EXTRA_DATA, characteristic.getValue());
		} else {

		}
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}

	public class LocalBinder extends Binder {
		public UartService getService() {
			return UartService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// After using a given device, you should make sure that
		// BluetoothGatt.close() is called
		// such that resources are cleaned up properly. In this particular
		// example, close() is
		// invoked when the UI is disconnected from the Service.
		close();
		return super.onUnbind(intent);
	}

	/**
	 * Initializes a reference to the local Bluetooth adapter.
	 * 
	 * @return Return true if the initialization is successful.
	 */
	public boolean initialize() {
		// For API level 18 and above, get a reference to BluetoothAdapter
		// through
		// BluetoothManager.
		if (mBluetoothManager == null) {
			mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			if (mBluetoothManager == null) {
				Log.e(TAG, "Unable to initialize BluetoothManager.");
				return false;
			}
		}
		mBluetoothAdapter = mBluetoothManager.getAdapter();
		if (mBluetoothAdapter == null) {
			Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
			return false;
		}

		return true;
	}

	/**
	 * Connects to the GATT server hosted on the Bluetooth LE device.
	 * 
	 * @param address
	 *            The device address of the destination device.
	 * 
	 * @return Return true if the connection is initiated successfully. The
	 *         connection result is reported asynchronously through the
	 *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
	 *         callback.
	 */
	public boolean connect(String address) {
		write_uuid = false;
		if (mBluetoothAdapter == null) {
			Log.w(TAG, "BluetoothAdapter not initialized  ");
			initialize();
			return false;
		}
		if (address == null) {
			Log.w(TAG, "  unspecified address.");
			return false;
		}
		// Previously connected device. Try to reconnect.
		if (DeviceListActivity.mBluetoothDeviceAddress != null
				&& address.equals(DeviceListActivity.mBluetoothDeviceAddress)
				&& mBluetoothGatt != null) {
			Log.d(TAG,
					"Trying to use an existing mBluetoothGatt for connection.");
			if (mBluetoothGatt.connect()) {
				mBluetoothGatt.disconnect();
//				mConnectionState = STATE_CONNECTING;
//				return false;
//			} else {
//				return false;
			}
		}
		// try {
		// Thread.sleep(1000);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		 
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
		if (device == null) {
			Log.w(TAG, "Device not found.  Unable to connect.");
			return false;
		}
		// We want to directly connect to the device, so we are setting the
		// autoConnect
		// parameter to false.
		/**
		 * 5秒钟没有响应  重新连接
		 */
		DeviceListActivity.mHandler.sendEmptyMessageDelayed(DeviceListActivity.connectGatt_no_state, 5000);
		mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
		DeviceListActivity.mBluetoothDeviceAddress = address;
		Log.d(TAG, "Trying to create a new connection.----" + address);
		mConnectionState = STATE_CONNECTING;
		return false;
	}

	/**
	 * Disconnects an existing connection or cancel a pending connection. The
	 * disconnection result is reported asynchronously through the
	 * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
	 * callback.
	 */
	public void disconnect() {
		if (mBluetoothAdapter == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			initialize();
			return;
		}
		if (mBluetoothGatt == null) {
			Log.w(TAG, "mBluetoothGatt not initialized");
			return;
		}
		Log.w(TAG, "mBluetoothGatt disconnect");
		mBluetoothGatt.disconnect();
		// mBluetoothGatt.close();
	}

	/**
	 * After using a given BLE device, the app must call this method to ensure
	 * resources are released properly.
	 */
	public void close() {
		if (mBluetoothGatt == null) {
			return;
		}
		Log.d(TAG, "mBluetoothGatt closed");
		if (mBluetoothGatt.connect()) {
			disconnect();
		}
		// DeviceListActivity.mBluetoothDeviceAddress = null;
		mBluetoothGatt.close();
		mBluetoothGatt = null;
	}

	/**
	 * Request a read on a given {@code BluetoothGattCharacteristic}. The read
	 * result is reported asynchronously through the
	 * {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
	 * callback.
	 * 
	 * @param characteristic
	 *            The characteristic to read from.
	 */
	public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}
		mBluetoothGatt.readCharacteristic(characteristic);
	}

	/**
	 * Enables or disables notification on a give characteristic.
	 * 
	 * @param characteristic
	 *            Characteristic to act on.
	 * @param enabled
	 *            If true, enable notification. False otherwise.
	 */
	/*
	 * public void setCharacteristicNotification(BluetoothGattCharacteristic
	 * characteristic, boolean enabled) { if (mBluetoothAdapter == null ||
	 * mBluetoothGatt == null) { Log.w(TAG, "BluetoothAdapter not initialized");
	 * return; } mBluetoothGatt.setCharacteristicNotification(characteristic,
	 * enabled);
	 * 
	 * 
	 * if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
	 * BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
	 * UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
	 * descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
	 * mBluetoothGatt.writeDescriptor(descriptor); } }
	 */

	/**
	 * Enable TXNotification
	 * 
	 * @return
	 */
	public void enableTXNotification() {
		/*
		 * if (mBluetoothGatt == null) { showMessage("mBluetoothGatt null" +
		 * mBluetoothGatt); broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
		 * return; }
		 */
		// BluetoothGattService RxService = mBluetoothGatt
		// .getService(RX_SERVICE_UUID);
		// if (RxService == null) {
		// showMessage("Rx service not found!");
		// broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
		// return;
		// }
		// BluetoothGattCharacteristic TxChar = RxService
		// .getCharacteristic(TX_CHAR_UUID);
		// if (TxChar == null) {
		// showMessage("Tx charateristic not found!");
		// broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
		// return;
		// }
		// mBluetoothGatt.setCharacteristicNotification(TxChar, true);
		//
		// BluetoothGattDescriptor descriptor = TxChar.getDescriptor(CCCD);
		// descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
		// mBluetoothGatt.writeDescriptor(descriptor);

	}

	public void writeRXCharacteristic(byte[] value) {

		BluetoothGattService RxService = mBluetoothGatt
				.getService(RX_SERVICE_UUID);
		showMessage("mBluetoothGatt null" + mBluetoothGatt);
		if (RxService == null) {
			showMessage("Rx service not found!");
			broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
			return;
		}
		BluetoothGattCharacteristic RxChar = RxService
				.getCharacteristic(RX_CHAR_UUID);
		if (RxChar == null) {
			showMessage("Rx charateristic not found!");
			broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
			return;
		}
		RxChar.setValue(value);
		boolean status = mBluetoothGatt.writeCharacteristic(RxChar);

		Log.d(TAG, "write TXchar - status=" + status);
	}

	private void showMessage(String msg) {
		Log.e(TAG, msg);
	}

	/**
	 * Retrieves a list of supported GATT services on the connected device. This
	 * should be invoked only after {@code BluetoothGatt#discoverServices()}
	 * completes successfully.
	 * 
	 * @return A {@code List} of supported services.
	 */
	public List<BluetoothGattService> getSupportedGattServices() {
		if (mBluetoothGatt == null)
			return null;

		return mBluetoothGatt.getServices();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}
