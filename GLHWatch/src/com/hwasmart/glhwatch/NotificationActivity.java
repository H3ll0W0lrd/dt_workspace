package com.hwasmart.glhwatch;

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

public class NotificationActivity extends Activity {

	private Vibrator vibrator;

	private Button okBtn;
	RelativeLayout titleBar;
	TextView textView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notification);

		titleBar = (RelativeLayout) findViewById(R.id.title_bar);
		textView = (TextView) findViewById(R.id.msg_content);

		okBtn = (Button) findViewById(R.id.ok_btn);

		okBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				NotificationActivity.this.finish();
			}
		});

		vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		refresh(getIntent());
	}

	private void refresh(Intent intent) {
		int type = intent.getExtras().getInt("type");
		String msgText = intent.getStringExtra("msg_text");
		if (type == 6) {// 进门提示

			titleBar.setBackgroundColor(0xFFED1C24);
			okBtn.setBackgroundColor(0xFFED1C24);
			textView.setText(msgText);

		} else if (type == 7) {// 出门预警

			titleBar.setBackgroundColor(0xFF70AD47);
			okBtn.setBackgroundColor(0xFF70AD47);
			textView.setText(msgText);
		}
		vibrator.vibrate(5000);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		refresh(intent);
	}

	@Override
	protected void onDestroy() {
		vibrator.cancel();
		super.onDestroy();
	}
}
