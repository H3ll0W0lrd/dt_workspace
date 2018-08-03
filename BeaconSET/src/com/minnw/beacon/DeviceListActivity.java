 

package com.minnw.beacon;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.minnw.beacon.R;
import com.minnw.beacon.adapter.DeviceAdapter;
import com.minnw.beacon.data.BluetoothDeviceAndRssi;
import com.minnw.tools.Tools;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class DeviceListActivity extends Activity {
	private BluetoothAdapter mBluetoothAdapter;
	private static LinearLayout progressbar;
	ImageButton   btn_refresh;
	ListView    newDevicesListView;
	/**
	 * 进入升级模式     
	 */
	public static String OAT = "EnterOTAmode";
	public static String dfu = "Dfu";
	public    static   String mBluetoothDeviceAddress=null;
	public     String TAG = "tag";
	public static Context context;
	private static final int progressbar_gone = 0x0001;
	public static final int start_detilactivity = 0x0002;
	public static final int connectfail_dismissdialog = 0x0003;
	public static final int mService_close = 0x0004;
	/** 执行connectGatt方法，5秒之内都没有反应 ，也没有执行回调类onConnectionStateChange方法
	 */
	public static final int connectGatt_no_state = 0x0005;
	private List<BluetoothDeviceAndRssi>  deviceList;
	public static BluetoothDeviceAndRssi   bluetoothdeviceandrssi_onItem;
	private DeviceAdapter deviceAdapter;
	private static final long SCAN_PERIOD = 10 * 1000; //
	public static UartService mService = null;
	public static boolean detilactivity = false;
	private boolean mScanning;
	private static final int REQUEST_ENABLE_BT = 2;
	private static final int UART_PROFILE_CONNECTED = 20;
	private static final int UART_PROFILE_DISCONNECTED = 21;
	private int rang = -55;
	/**是否连接设备
	 */
	public static boolean connect_device;
	public static long connectCurrentTime=0;
	public static long disconnectCurrentTime=0;
	public static Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			 
			case connectGatt_no_state:
				broadcastUpdate("com.nordicsemi.nrfUART.ACTION_GATT_DISCONNECTED");
				break;
			case progressbar_gone:
				progressbar.setVisibility(View.GONE);
				removeMessages(progressbar_gone);
				break;
			case start_detilactivity:
				mpDialog.cancel();
				if (!detilactivity) {
					detilactivity = true;
					Intent intent = new Intent(context, DetilActivity.class);
					context.startActivity(intent);
				}
				break;
			case connectfail_dismissdialog:
				mpDialog.cancel();
				Toast.makeText(context,
						context.getString(R.string.connect_failed), 1).show();

				break;
			case mService_close:
				if (mService != null)
					mService.close();
				break;
			default:
				break;
			}

		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
		setContentView(R.layout.device);
		// Use this check to determine whether BLE is supported on the device.
		// Then you can
		// selectively disable BLE-related features.
		 
		if (!getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT)
					.show();
			finish();
		}
		context = this;
		// Initializes a Bluetooth adapter. For API level 18 and above, get a
		// reference to
		// BluetoothAdapter through BluetoothManager.
		// final BluetoothManager bluetoothManager = (BluetoothManager)
		// getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		// Checks if Bluetooth is supported on the device.
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		System.gc();
		if(!mBluetoothAdapter.isEnabled()){
			Toast.makeText(this, R.string.ble_not_open, Toast.LENGTH_SHORT).show();
			showBLEDialog();
		}
		uiInit();
		dataToList();
		service_init();
		dialogshow();
		if (mScanning == false) {
			scanLeDevice(true);
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (mScanning == false) {
		     scanLeDevice(true);
		}
		if(deviceAdapter!=null)
			deviceAdapter.notifyDataSetChanged();
	}
	protected void showBLEDialog() {
		  Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
	}
	private void addBondedDevices() {
		final Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
//		for (BluetoothDevice device : devices) {
//			mAdapter.addBondedDevice(new ExtendedBluetoothDevice(device, device.getName(), NO_RSSI, DEVICE_IS_BONDED));
//		}
	}
	private   BroadcastReceiver changeReceiver = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			  Intent mIntent = intent;
			// *********************//
			if (action.equals(UartService.ACTION_GATT_CONNECTED)) {
				runOnUiThread(new Runnable() {
					public void run() {
						String currentDateTimeString = DateFormat
								.getTimeInstance().format(new Date());
						 
						// btnConnectDisconnect.setText("Disconnect");
						// edtMessage.setEnabled(true);
						// btnSend.setEnabled(true);
						// ((TextView) findViewById(R.id.deviceName))
						// .setText(mDevice.getName() + " - ready");
						// listAdapter.add("[" + currentDateTimeString
						// + "] Connected to: " + mDevice.getName());
						// messageListView.smoothScrollToPosition(listAdapter
						// .getCount() - 1);
						// mState = UART_PROFILE_CONNECTED;
					}
				});
			}

			/**监听断开广播         在连接过程中  30秒之内没有响应就重新连接 
			 */
			if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) {
				runOnUiThread(new Runnable() {
					public void run() {
						try {
							if (!mScanning)
							     scanLeDevice(true);
							long currenttime=System.currentTimeMillis();
							int i=(int) (currenttime-connectCurrentTime);
							Log.e(TAG, i+"UartService.ACTION_GATT_DISCONNECTED-----"+bluetoothdeviceandrssi_onItem.getBluetoothdevice().getName());
							if(i>0&&i<=30000){
								if(!connect_device) return;
								if(mService!=null){
								      mService.close();
								  	if(mService==null)return;
									bluetoothdeviceandrssi_onItem.setCONN(false);//连接的时候  设置为不可连接。
									if(mService.connect(mBluetoothDeviceAddress)){
										 DeviceListActivity.mHandler.sendEmptyMessage(DeviceListActivity.start_detilactivity);
									   }
								}
							}else{
								/**有的时候  连接上了   返回到这个界面  还是会执行这里
								 */
								mHandler.sendEmptyMessage(connectfail_dismissdialog);
								if(mService!=null)
								    mService.close();
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
			}

			// *********************//
			if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
				mService.enableTXNotification();
			}
			// *********************//
			if (action.equals(UartService.ACTION_DATA_AVAILABLE)) {

				  final byte[] txValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);
				runOnUiThread(new Runnable() {
					public void run() {
						try {
							String text = new String(txValue, "UTF-8");
							String currentDateTimeString = DateFormat
									.getTimeInstance().format(new Date());
							// listAdapter.add("[" + currentDateTimeString
							// + "] RX: " + text);
							// messageListView.smoothScrollToPosition(listAdapter
							// .getCount() - 1);

						} catch (Exception e) {
							// Log.e(TAG, e.toString());
						}
					}
				});
			}
			// *********************//
			if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART)) {
				showMessage("Device doesn't support connecting. ");
				mService.disconnect();
			}

		}
	};

	private static IntentFilter makeGattUpdateIntentFilter() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
		intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
		intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
		return intentFilter;
	}

	// UART service connected/disconnected
	private ServiceConnection mServiceConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className,
				IBinder rawBinder) {
			mService = ((UartService.LocalBinder) rawBinder).getService();
			Log.d(TAG, "onServiceConnected mService= " + mService);
			if (!mService.initialize()) {
				Log.e(TAG, "Unable to initialize Bluetooth");
				finish();
			}

		}

		public void onServiceDisconnected(ComponentName classname) {
			// // mService.disconnect(mDevice);
			mService = null;
		}
	};

	private void service_init() {
		Intent bindIntent = new Intent(this, UartService.class);
		bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
		LocalBroadcastManager.getInstance(this).registerReceiver(
				changeReceiver, makeGattUpdateIntentFilter());
	}

	private void uiInit() {
		newDevicesListView = (ListView) findViewById(R.id.new_devices);
		progressbar = (LinearLayout) findViewById(R.id.progressBar2);
		ImageButton btn_menu = (ImageButton) findViewById(R.id.btn_menu);
		btn_menu.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
                Intent  intent=new Intent(DeviceListActivity.this,AboutActivity.class);
                startActivity(intent);
				  
			}
		});
		btn_refresh = (ImageButton) findViewById(R.id.btn_refresh);
		btn_refresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				deviceList.clear();
				if (mScanning) {
					scanLeDevice(false);
				  }
				
				if (mScanning == false) {
					progressbar.setVisibility(View.VISIBLE);
					scanLeDevice(true);
				  }

			}
		});
	}

	private void dataToList() {
		/* Initialize device list container */
		deviceList = new ArrayList<BluetoothDeviceAndRssi>();
		deviceAdapter = new DeviceAdapter(this, deviceList);
		newDevicesListView.setAdapter(deviceAdapter);
		newDevicesListView.setOnItemClickListener(mItemDeviceClickListener);

	}
	private void scanLeDevice(final boolean enable) {
		Log.d(TAG, "scanLeDevice    "+enable);
		if (enable) {
			// Stops scanning after a pre-defined scan period.
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					
					if(mScanning)
					   mBluetoothAdapter.stopLeScan(mLeScanCallback);
					mScanning = false;
//					btn_refresh.setEnabled(true);

				}
			}, SCAN_PERIOD+5000);
			addBondedDevices();
			mScanning = true;
			mBluetoothAdapter.startLeScan(mLeScanCallback);
			/**
			 * 会执行多次。但是不影响程序。
			 */
			mHandler.sendEmptyMessageDelayed(progressbar_gone, SCAN_PERIOD+5000);
//			btn_refresh.setEnabled(false);
		} else {
			mScanning = false;
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
//			btn_refresh.setEnabled(true);
		}

	}
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, final int rssi,
				  final byte[] scanRecord) {

			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					addDevice(device, rssi, scanRecord);
				}
			});

		
		}
	};

	private void addDevice(BluetoothDevice device, int rssi, byte[] scanRecord) {
		boolean deviceFound = false;
	
		for (BluetoothDeviceAndRssi listDev : deviceList) {
			if (listDev.getBluetoothdevice().getAddress().equals(device.getAddress())) {
				    listDev.setRssi(rssi);
				try {
					listDev.setBluetoothdevice(device);
					JSONObject scanRecordobj = Tools.decodeAdvData(scanRecord);
					listDev.setServiceData(scanRecordobj.getString("serviceData"));
					listDev.setDistance(calculateAccuracy(rang, rssi) + "");
					listDev.setCONN(scanRecordobj.getString("serviceData").equals("")?false:true);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				deviceFound = true;
				break;
			}
		}
		if (!deviceFound) {
			JSONObject obj = new JSONObject();
			JSONObject scanRecordobj = Tools.decodeAdvData(scanRecord);
			Tools.addProperty(obj, Tools.DEVICE_ADDRESS, device.getAddress());
			Tools.addProperty(obj, Tools.DEVICE_NAME, device.getName());
			Tools.addProperty(obj, Tools.IS_CONNECTED, Tools.IS_FALSE);
			Tools.addProperty(obj, Tools.RSSI, rssi);
			Tools.addProperty(obj, Tools.ADVERTISEMENT_DATA, scanRecordobj);
			Tools.addProperty(obj, Tools.TYPE, "BLE");
			 
			BluetoothDeviceAndRssi bluetoothdeviceandrssi = new BluetoothDeviceAndRssi();
			bluetoothdeviceandrssi.setBluetoothdevice(device);
			bluetoothdeviceandrssi.setRssi(rssi);
			bluetoothdeviceandrssi.setName(device.getName());
			bluetoothdeviceandrssi.setObj(obj);
			bluetoothdeviceandrssi.setDistance(calculateAccuracy(rang, rssi)+ "");
			try {
				String advDatas = scanRecordobj.getString("advData").substring(4, 6);
				boolean isconn = (Integer.parseInt(advDatas, 16) & (2 | 1)) > 0 ? true: false;
				bluetoothdeviceandrssi.setCONN(scanRecordobj.getString("serviceData").equals("")?false:true);
				bluetoothdeviceandrssi.setServiceData(scanRecordobj.getString("serviceData"));
			
			} catch (JSONException e) {
				 
			}
			 
				if (bluetoothdeviceandrssi.getServiceData().startsWith("F0FF")) {
					if (bluetoothdeviceandrssi.getName()!=null)
					deviceList.add(bluetoothdeviceandrssi);
				}else{
					bluetoothdeviceandrssi.setCONN( false);
					deviceList.add(bluetoothdeviceandrssi);
				}
			 
		}
		Collections.sort(deviceList);
		deviceAdapter.notifyDataSetChanged();
		
	}

	private double calculateAccuracy(int txPower, int rssi) {
		if (rssi == 0) {
			return -1.0; // if we cannot determine accuracy, return -1.
		}
		double ratio = rssi * 1.0 / txPower;
		if (ratio < 1.0) {
			return changeTwoDecimal_f(Math.pow(ratio, 10));
		} else {
			double accuracy = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
			return changeTwoDecimal_f(accuracy);
		}
	}

	private double changeTwoDecimal_f(double floatvar) {
		double i = Math.round(floatvar * 100);
		double f_x = i / 100;
		String s_x = f_x + "";
		int pos_decimal = s_x.indexOf('.');
		if (pos_decimal < 0) {
			pos_decimal = s_x.length();
			s_x += '.';
		}
		while (s_x.length() <= pos_decimal + 2) {
			s_x += "0";
		}
		return Double.parseDouble(s_x);
	}

	@Override
	public void onStart() {
		super.onStart();
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
	}

	@Override
	public void onStop() {
		super.onStop();
		if (mScanning)
			   scanLeDevice(false);

	}
 

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(mpDialog!=null)
			 mpDialog.cancel();
		if(mHandler!=null)
		      mHandler.removeMessages(connectfail_dismissdialog);
		if (mScanning)
		     scanLeDevice(false);
		
		if (mService != null)
			mService.close();
		try {
			LocalBroadcastManager.getInstance(this).unregisterReceiver(
					changeReceiver);
		} catch (Exception ignore) {
			Log.e(TAG, ignore.toString());
		}
		unbindService(mServiceConnection);
		
		mService.stopSelf();
		mService = null;
		bluetoothdeviceandrssi_onItem=null;
	}

	@Override
	public void onBackPressed() {
		new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.popup_title)
				.setMessage(R.string.popup_message)
				.setPositiveButton(R.string.popup_yes,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								finish();
							}
						}).setNegativeButton(R.string.popup_no, null).show();

	}

	private OnItemClickListener mItemDeviceClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			UartService.listdataservice.clear();
			UartService.listserviceIndexdata.clear();
			UartService.syslistdata.clear();
			bluetoothdeviceandrssi_onItem = deviceList.get(position);
			 if(!bluetoothdeviceandrssi_onItem.isCONN()){
					if (!mScanning)
						  scanLeDevice(true);
					Toast.makeText(getApplicationContext(), getString(R.string.gangcailianjieguole), 1).show();
					return ;
			   }
			bluetoothdeviceandrssi_onItem.setCONN(false);//连接的时候  设置为不可连接。
			mpDialog.setMessage(getString(R.string.connecting)+bluetoothdeviceandrssi_onItem.getBluetoothdevice().getName());
			mpDialog.show();	
			if (mScanning)
				  scanLeDevice(false);
			connectCurrentTime=System.currentTimeMillis();
			new Thread() {
				public void run() {
					connect_device=true;
					if(mService.connect(bluetoothdeviceandrssi_onItem.getBluetoothdevice().getAddress())){
						 DeviceListActivity.mHandler.sendEmptyMessage(DeviceListActivity.start_detilactivity);
					}else{
						UartService.listdataservice.clear();
						UartService.syslistdata.clear();
					}
				};
			}.start();

		}
	};

	protected void onPause() {
		super.onPause();
		if (mScanning)
		    scanLeDevice(false);
	}

	static ProgressDialog mpDialog;

	protected void dialogshow() {
		mpDialog = new ProgressDialog(DeviceListActivity.this);
		mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);//  
		mpDialog.setTitle(null);// 
		mpDialog.setIcon(null);// 
		mpDialog.setOnCancelListener(new OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface arg0) {
				if(deviceAdapter!=null)
					deviceAdapter.notifyDataSetChanged();
				
			}
		});
		mpDialog.setCancelable(true);//  
		mpDialog.setCanceledOnTouchOutside(false);
	}
	private static void broadcastUpdate( String action) {
		  Intent intent = new Intent(action);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
	}
	private void showMessage(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}
}
