package com.hwasmart.glhwatch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class HelpNotificationActivity extends Activity {
	
	private Vibrator vibrator;
	
	private Button okBtn;
	
	private String msgText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help_notification);
		
		Intent intent = this.getIntent();
		msgText = intent.getStringExtra("msg_text");
		
		TextView textView = (TextView)findViewById(R.id.msg_content);
		
		okBtn = (Button)findViewById(R.id.ok_btn);
		
		okBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				HelpNotificationActivity.this.finish();
			}
		});
		
		textView.setText(msgText);
			
		vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
		vibrator.vibrate(3000);
	}
	
	@Override
	protected void onDestroy() {
		vibrator.cancel();
		super.onDestroy();
	}
}
