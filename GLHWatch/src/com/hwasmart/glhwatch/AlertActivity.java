package com.hwasmart.glhwatch;

import java.util.Date;

import com.hwasmart.glhwatch.service.LocationUploadService;
import com.rtm.location.LocationApp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class AlertActivity extends Activity {

	private Vibrator vibrator;
	
	private Button okBtn1;
	private Button okBtn2;
	
	private int type;
	private String msgText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alert);
		
		Intent intent = this.getIntent();
		type = intent.getExtras().getInt("type");
		msgText = intent.getStringExtra("msg_text");
		
		RelativeLayout titleBar = (RelativeLayout)findViewById(R.id.title_bar);
		TextView textView = (TextView)findViewById(R.id.msg_content);
		
		okBtn1 = (Button)findViewById(R.id.ok_btn1);
		okBtn2 = (Button)findViewById(R.id.ok_btn2);
		
		okBtn1.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				LocationUploadService.caseTimes.put(msgText, System.currentTimeMillis());
				AlertActivity.this.finish();
			}
		});
		
		okBtn2.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				LocationUploadService.caseTimes.put(msgText, System.currentTimeMillis());
				Intent intent = new Intent(AlertActivity.this, HelpActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
				AlertActivity.this.finish();
			}
		});

		if (type == 1){
			// 距离超出
//			titleBar.setBackgroundColor(0xFFE51400);
//			okBtn1.setBackgroundColor(0xFFE51400);
//			okBtn2.setBackgroundColor(0xFFE51400);
//			textView.setText("请注意您的小孩是否在身边！");
			
			
		} else if(type == 2) {
			// 电量过低
//			titleBar.setBackgroundColor(0xFFED7D31);
//			okBtn1.setBackgroundColor(0xFFED7D31);
//			okBtn2.setBackgroundColor(0xFFED7D31);
//			textView.setText("请注意您家人手表的电量！");
			
		} else if(type == 3) {
			// 位置丢失
//			titleBar.setBackgroundColor(0xFFED7D31);
//			okBtn1.setBackgroundColor(0xFFED7D31);
//			okBtn2.setBackgroundColor(0xFFED7D31);
//			textView.setText("请注意您的家人手表位置丢失！");
		}
		
		textView.setText(msgText);
		
		vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
		vibrator.vibrate(5000);
	}
	
	@Override
	public void onBackPressed() {
		
	}
	
	@Override
	protected void onDestroy() {
		vibrator.cancel();
		super.onDestroy();
	}
	
//	private void setCaseInternal(int type,int value){
//		switch (type) {
//		case 1:
//			LocationUploadService.case1time = new Date().getTime();
//			LocationUploadService.case1Internal = value;
//			break;
//		case 2:
//			LocationUploadService.case2time = new Date().getTime();
//			LocationUploadService.case2Internal = value;
//			break;
//		case 3:
//			LocationUploadService.case3time = new Date().getTime();
//			LocationUploadService.case3Internal = value;
//			break;
//		default:
//			break;
//		}
//	}
}
