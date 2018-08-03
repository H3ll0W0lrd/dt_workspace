package com.hwasmart.glhwatch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.ProgressBar;

public class WelcomeActivity extends Activity {
	// 正确按照顺序点击“我爱观澜湖”按钮解锁进入主界面
	
	private ArrayList<String> words = new ArrayList<String>();
	
	private boolean pass = true;
	private String lastWord = "";
	
	private Button btn1;
	private Button btn2;
	private Button btn3;
	private Button btn4;
	private Button btn5;
	private Button btn6;
	private Button btn7;
	private Button btn8;
	private Button btn9;
	private Button btn0;
	private Button refreshbtn;
	private ProgressBar batteryBar;
	
	private BroadcastReceiver batteryChangedReceiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		
		pass = true;
		lastWord = "";
		
//		words.add("我");
//		words.add("爱");
//		words.add("你");
//		words.add("观");
//		words.add("澜");
//		words.add("湖");
//		words.add("美");
//		words.add("丽");
//		words.add("家");
		
		btn1 = (Button)findViewById(R.id.welcome_btn1);
		btn2 = (Button)findViewById(R.id.welcome_btn2);
		btn3 = (Button)findViewById(R.id.welcome_btn3);
		btn4 = (Button)findViewById(R.id.welcome_btn4);
		btn5 = (Button)findViewById(R.id.welcome_btn5);
		btn6 = (Button)findViewById(R.id.welcome_btn6);
		btn7 = (Button)findViewById(R.id.welcome_btn7);
		btn8 = (Button)findViewById(R.id.welcome_btn8);
		btn9 = (Button)findViewById(R.id.welcome_btn9);
		btn0 = (Button)findViewById(R.id.welcome_btn0);
		refreshbtn = (Button)findViewById(R.id.welcome_btnrefresh);
		batteryBar = (ProgressBar) findViewById(R.id.battary_bar);
		
//		Collections.shuffle(words);
//		btn1.setText(words.get(0));
//		btn2.setText(words.get(1));
//		btn3.setText(words.get(2));
//		btn4.setText(words.get(3));
//		btn5.setText(words.get(4));
//		btn6.setText(words.get(5));
//		btn7.setText(words.get(6));
//		btn8.setText(words.get(7));
//		btn9.setText(words.get(8));
		
		btn1.setOnClickListener(btnClickListener);
		btn2.setOnClickListener(btnClickListener);
		btn3.setOnClickListener(btnClickListener);
		btn4.setOnClickListener(btnClickListener);
		btn5.setOnClickListener(btnClickListener);
		btn6.setOnClickListener(btnClickListener);
		btn7.setOnClickListener(btnClickListener);
		btn8.setOnClickListener(btnClickListener);
		btn9.setOnClickListener(btnClickListener);
		btn0.setOnClickListener(btnClickListener);
		refreshbtn.setOnClickListener(refreshbtnClickListener);
		refreshbtn.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				finish();
				return false;
			}
		});
		try {
			writeScannerInfo("test.txt","ceshis");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        String sDStateString  = Environment.getExternalStorageState();
        File scannerFile = null;
        if (sDStateString.equals(Environment.MEDIA_MOUNTED)) {
        	File SDFile = Environment.getExternalStorageDirectory();
        	scannerFile = new File(SDFile.getAbsoluteFile()+ "/scannerInfo.text");
        	if(!scannerFile.exists()){
				try {
					scannerFile.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        }

        
        for(int i=0;i<100;i++){
        	try {
				writeSDFile(scannerFile,(i+","));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
	    batteryChangedReceiver = new BroadcastReceiver() { 
			@Override
			public void onReceive(Context context, Intent intent) { 
		        int level = intent.getIntExtra("level", 0);  //电池电量等级 
		        int scale = intent.getIntExtra("scale", 100);  //电池满时百分比 
		        int status = intent.getIntExtra("status", 0);  //电池状态 
	  
//		        // 若正在充电 
//		        if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
//		        	batteryBar.setProgress(100);
//		        	batteryBar.setEnabled(false);
//		        } else {
//		        	batteryBar.setEnabled(true);
//		        	batteryBar.setProgress(level);
//		        }
		        batteryBar.setProgress(level);
		        if (level > 20)
		        	batteryBar.setProgressDrawable(WelcomeActivity.this.getResources().getDrawable(R.drawable.progressbar_1));
		        else
		        	batteryBar.setProgressDrawable(WelcomeActivity.this.getResources().getDrawable(R.drawable.progressbar_2));
			}
	    }; 
	}
	
	public void writeScannerInfo(String filename,String info) throws IOException{
		String sDStateString  = Environment.getExternalStorageState();
        File scannerFile = null;
        if (sDStateString.equals(Environment.MEDIA_MOUNTED)) {
        	File SDFile = Environment.getExternalStorageDirectory();
        	scannerFile = new File(SDFile.getAbsoluteFile()+ File.separator + filename);
        	if(!scannerFile.exists()){
					scannerFile.createNewFile();
        	}
        	FileOutputStream fos = new FileOutputStream(scannerFile,true);
    		byte[] bytes = info.getBytes();
    		fos.write(bytes);
    		fos.close();
        }
	}
	public void writeSDFile(File file, String write_str)
			throws IOException {
		FileOutputStream fos = new FileOutputStream(file,true);
		byte[] bytes = write_str.getBytes();
		fos.write(bytes);
		fos.close();
	}

	
	@Override
	protected void onResume() {
		// 注册电量监听
		IntentFilter batteryChangedReceiverFilter = new IntentFilter(); 
	    batteryChangedReceiverFilter.addAction(Intent.ACTION_BATTERY_CHANGED); 
	       
	    registerReceiver(batteryChangedReceiver, batteryChangedReceiverFilter);
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		unregisterReceiver(batteryChangedReceiver);
		super.onPause();
	}
	
	@Override
	public void onBackPressed() {
		// 屏蔽返回按钮
	};
	
	View.OnClickListener refreshbtnClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			
//			WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE); 
//	        WifiConnect wifiConnect = new WifiConnect(wifiManager);
//	        wifiConnect.Connect("MH Mall", "", WifiConnect.WifiCipherType.WIFICIPHER_NOPASS);
			
//			Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
//			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//			startActivity(intent);
//			WelcomeActivity.this.finish();
			

			pass = true;
			lastWord = "";
			btn1.setBackgroundColor(0xFF70AD47);
			btn1.setEnabled(true);
			btn2.setBackgroundColor(0xFF70AD47);
			btn2.setEnabled(true);
			btn3.setBackgroundColor(0xFF70AD47);
			btn3.setEnabled(true);
			btn4.setBackgroundColor(0xFF70AD47);
			btn4.setEnabled(true);
			btn5.setBackgroundColor(0xFF70AD47);
			btn5.setEnabled(true);
			btn6.setBackgroundColor(0xFF70AD47);
			btn6.setEnabled(true);
			btn7.setBackgroundColor(0xFF70AD47);
			btn7.setEnabled(true);
			btn8.setBackgroundColor(0xFF70AD47);
			btn8.setEnabled(true);
			btn9.setBackgroundColor(0xFF70AD47);
			btn9.setEnabled(true);
			btn0.setBackgroundColor(0xFF70AD47);
			btn0.setEnabled(true);

		}
		
		
	};
	
	View.OnClickListener btnClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			if (pass == true){
				String word = ((Button)v).getText().toString();
				if ("".equals(lastWord)){
					if ("0".equals(word)){
						lastWord = "0";
					} else {
						pass = false;
					}
				} else if ("0".equals(lastWord)){
					if ("9".equals(word)){
						lastWord = "9";
					} else {
						pass = false;
					}
				} else if ("9".equals(lastWord)){
					if ("2".equals(word)){
						lastWord = "2";
					} else {
						pass = false;
					}
				} else if ("2".equals(lastWord)){
					if ("5".equals(word)){
						lastWord = "5";
						Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
						startActivity(intent);
						WelcomeActivity.this.finish();
					} else {
						pass = false;
					}
				}
			}
			v.setBackgroundColor(0xFFED7D31);
			v.setEnabled(false);
			
		}
	};
}
