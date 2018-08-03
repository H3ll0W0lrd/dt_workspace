
package com.minnw.beacon;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import no.nordicsemi.android.nrftoolbox.dfu.DfuActivity;
import no.nordicsemi.android.nrftoolbox.scanner.DeviceListAdapter;
import no.nordicsemi.android.nrftoolbox.scanner.ExtendedBluetoothDevice;
import no.nordicsemi.android.nrftoolbox.scanner.ScannerServiceParser;
import no.nordicsemi.android.nrftoolbox.utility.DebugLogger;

import com.minnw.beacon.UartService;
import com.minnw.beacon.R;
import com.minnw.beacon.adapter.DetilAdapter;
import com.minnw.tools.Base64Tool;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class DetilActivity extends FinishActivity   {
	private   ProgressDialog mpDialog;
	public static  boolean isSysInfoActivity=false;
	private boolean mIsScanning = false;

	private static final boolean DEVICE_IS_BONDED = true;
	private static final boolean DEVICE_NOT_BONDED = false;
	/* package */static final int NO_RSSI = -1000;
	private BluetoothAdapter mBluetoothAdapter;
	public static  DeviceListAdapter mAdapter;
	
	public    Handler  handler=new Handler(){
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				mpDialog.cancel();
				if(!isSysInfoActivity){
				isSysInfoActivity=true;
				Intent  intent=new Intent(context,SysInfoActivity.class);
				context.startActivity(intent);
				}
				break;
			case 1:
				if(UartService.syslistdata.size()==8){
					  handler.sendEmptyMessage(0);
				}else
				    handler.sendEmptyMessageDelayed(1, 500);
				break;
			case 2:
				removeMessages(3);
				mpDialog.cancel();
				if(!isSysInfoActivity){
				 isSysInfoActivity=true;
				mBluetoothAdapter.stopLeScan(mLEScanCallback);
				Intent  intent=new Intent(DetilActivity.this,DfuActivity.class);
		    	startActivity(intent);
		    	finish();
				}
				break;
			case 3:
				mBluetoothAdapter.stopLeScan(mLEScanCallback);
				mpDialog.cancel();
				Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.qingquedingshifoudfu), 1).show();
				break;
			default:
				break;
			}
		
			 
			}
	};
	private static  Context  context;
	private   DetilAdapter  detiladapter;
	 @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detil);
		isSysInfoActivity=false;
		context=this;
		DeviceListActivity.mService.readSysinfo=false;
		init();
		show();
		final BluetoothManager manager = (BluetoothManager)  getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = manager.getAdapter();
		mAdapter = new DeviceListAdapter(this);
	 }
		protected void show() {
			mpDialog = new ProgressDialog(DetilActivity.this);
			mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);//  
			mpDialog.setTitle(null);// 
			mpDialog.setIcon(null);// 
			mpDialog.setMessage(getString(R.string.connecting));
			mpDialog.setCancelable(true);//  
			mpDialog.setCanceledOnTouchOutside(false);
		}
	private void init() {
		List<String>  listname=new  ArrayList<String>();
		
		listname.add(getString(R.string.batterytext));
		listname.add(getString(R.string.uuid));
		listname.add(getString(R.string.major));
		listname.add(getString(R.string.minor));
		listname.add(getString(R.string.measured_power));
		listname.add(getString(R.string.transimssion_power));
		listname.add(getString(R.string.broadcasting_interval));
		listname.add(getString(R.string.serial_id));
		listname.add(getString(R.string.ibeacon_name));
		listname.add(getString(R.string.connection_mode));
		
		listname.add(getString(R.string.soft_reboot));
		listname.add(getString(R.string.change_password));
		listname.add(getString(R.string.device_information)); 
		listname.add(getString(R.string.device_upload)); 
		ListView    detillistview=(ListView)findViewById(R.id.detillistview);
		detiladapter = new  DetilAdapter(this, listname,UartService.listdataservice);
		detillistview.setAdapter(detiladapter);
		detillistview.setOnItemClickListener(mItemDeviceClickListener);
		ImageButton   btn_back=(ImageButton)findViewById(R.id.btn_back);
		btn_back.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				   finish();
			}
		});
		
	}
	private OnItemClickListener mItemDeviceClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			//[4;-1, 4;0, 4;1, 4;2, 4;3, 4;4, 4;6, 4;7, 4;8, 4;10][4;-1, 4;0, 4;1, 4;2, 4;3, 4;4, 4;6, 4;7, 4;8, 4;10]
			    if(position==1){
			    	Intent  intent=new Intent(DetilActivity.this,UUIDActivity.class);
			    	intent.putExtra("uuid", UartService.listdataservice.get(position));
			    	intent.putExtra("position", position+"");
			    	intent.putExtra("serviceIndex", UartService.listserviceIndexdata.get(position));
			    	startActivity(intent);
			    }else if(position==3||position==2){
			    	Intent  intent=new Intent(DetilActivity.this,MajorMinorActivity.class);
			    	intent.putExtra("data", UartService.listdataservice.get(position));
			    	intent.putExtra("position", position+"");
			    	intent.putExtra("serviceIndex", UartService.listserviceIndexdata.get(position));
			    	startActivity(intent);
			    }else if(position==4||position==7){
			    	Intent  intent=new Intent(DetilActivity.this,Measured_PowerActivity.class);
			    	intent.putExtra("data", UartService.listdataservice.get(position));
			    	intent.putExtra("position", position+"");
			    	intent.putExtra("serviceIndex", UartService.listserviceIndexdata.get(position));
			    	startActivity(intent);
			    }else if(position==8){
			    	Intent  intent=new Intent(DetilActivity.this,Measured_PowerActivity.class);
			    	intent.putExtra("data", UartService.listdataservice.get(position));
			    	intent.putExtra("position", position+"");
			    	intent.putExtra("serviceIndex", UartService.listserviceIndexdata.get(position));
			    	startActivity(intent);
			    }else if(position==5){
			    	Intent  intent=new Intent(DetilActivity.this,TransimssionPowerActivity.class);
			    	intent.putExtra("data", UartService.listdataservice.get(position));
			    	intent.putExtra("position", position+"");
			    	intent.putExtra("serviceIndex", UartService.listserviceIndexdata.get(position));
			    	startActivity(intent);
			    }else if(position==6||position==9){
			    	Intent  intent=new Intent(DetilActivity.this,BroadCastingIntervalActivity.class);
			    	intent.putExtra("data", UartService.listdataservice.get(position));
			    	intent.putExtra("position", position+"");
			    	intent.putExtra("serviceIndex", UartService.listserviceIndexdata.get(position));
			    	startActivity(intent);
			    }else if(position==10){
			    	/**
			    	 *  characteristicInfo={"characteristicIndex":11,"characteristicUUID":"0000ffff-0000-1000-8000-00805f9b34fb",
			    	 */
			    	Intent  intent=new Intent(DetilActivity.this,ResetActivity.class);
			    	intent.putExtra("position", position+"");
			    	intent.putExtra("serviceIndex", DeviceListActivity.mService.serviceIndex_readValue+";11");
			    	startActivity(intent);
			    }else if(position==11){
			    	/**
			    	 *  characteristicInfo={"characteristicIndex":5,"characteristicUUID":"0000fff6-0000-1000-8000-00805f9b34fb
			    	 */
			    	Intent  intent=new Intent(DetilActivity.this,ChangPassWordActivity.class);
			    	intent.putExtra("position", position+"");
			    	intent.putExtra("serviceIndex", DeviceListActivity.mService.serviceIndex_readValue+";5");
			    	startActivity(intent);
			    }else if(position==12){
			    	mpDialog.setMessage(getString(R.string.connecting));
			    	mpDialog.show();
			    	new Thread(){
			    		public void run() {
			    			if(UartService.syslistdata.size()<8)
			    			     DeviceListActivity.mService.readValueSys(true);
			    			handler.sendEmptyMessage(1);
			    		};
			    	}.start();
			    }else if(position==13){
			      /**扫描到DFU  beacon才跳转
			       * {"characteristicIndex":9,"characteristicUUID":"0000fffa-0000-1000-8000-00805f9b34fb","characteristicName":"unknown","characteristicProperty":["read","write"]}
			       */
			      new AlertDialog.Builder(DetilActivity.this)
					.setIcon(null)
					.setTitle(null)
					.setMessage(R.string.device_upload)
					.setPositiveButton(R.string.popup_yes,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									String s = DeviceListActivity.OAT;
									String vlaues = Base64Tool.ASCIIToBase64(s);
									DeviceListActivity.mService.write_uuid(vlaues,DeviceListActivity.mService.serviceIndex_readValue ,9);
									mpDialog.setMessage(getString(R.string.search));
									mpDialog.show();
									startScanFormDFU();
									new Thread(){
										public void run() {
											handler.sendEmptyMessageDelayed(3, 30000);
										};
									}.start();
								}
							}).setNegativeButton(R.string.popup_no, null).show();
					
			    }else{
			    	Toast.makeText(getApplicationContext(), getString(R.string.bunengxiugai), 1).show();
			    }
		}
	};  
 
	private void startScanFormDFU() {
		mBluetoothAdapter.startLeScan(mLEScanCallback);
 
	}
	
	/**
	 * Callback for scanned devices class {@link ScannerServiceParser} will be used to filter devices with custom BLE service UUID then the device will be added in a list.
	 */
	private final BluetoothAdapter.LeScanCallback mLEScanCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
			if (device != null) {
				try {
					if (ScannerServiceParser.decodeDeviceAdvData(scanRecord, null, false)) {
						// On some devices device.getName() is always null. We have to parse the name manually :(
						// This bug has been found on Sony Xperia Z1 (C6903) with Android 4.3.
						// https://devzone.nordicsemi.com/index.php/cannot-see-device-name-in-sony-z1
						addScannedDevice(device, ScannerServiceParser.decodeDeviceName(scanRecord), rssi, DEVICE_NOT_BONDED);
					}
				} catch (Exception e) {
				}
			}
		}
	};
	

	/**
	 * if scanned device already in the list then update it otherwise add as a new device
	 */
	private void addScannedDevice(final BluetoothDevice device, final String name, final int rssi, final boolean isBonded) {
		 
			 runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if(name!=null&&name.startsWith(DeviceListActivity.dfu)){
				    	mAdapter.addOrUpdateDevice(new ExtendedBluetoothDevice(device, name, rssi, isBonded));
				    	handler.sendEmptyMessageDelayed(2, 2000);
					}
				}
			});
	}
 
	protected void onResume() {
		super.onResume();
		if(detiladapter!=null) 
			detiladapter.notifyDataSetChanged();
	};
	
	@Override
	protected void onStop() {
		super.onStop();
		Log.i("tag", "onDestroy");
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		DeviceListActivity.disconnectCurrentTime=System.currentTimeMillis();
		DeviceListActivity.connect_device=false;
		 DeviceListActivity.mService.close();
		if(mpDialog!=null)mpDialog.cancel();
//		DeviceListActivity.mHandler.sendEmptyMessage(DeviceListActivity.mService_close);
		DeviceListActivity.detilactivity=false;
	}   
}
