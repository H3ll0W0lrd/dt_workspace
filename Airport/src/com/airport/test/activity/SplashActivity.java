package com.airport.test.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.airport.test.R;
import com.airport.test.ar.ArShowActivity;
import com.dingtao.libs.DTActivity;
import com.dingtao.libs.DTApplication;
import com.dingtao.libs.util.DTLog;
import com.google.gson.Gson;
import com.rtm.frm.model.RMBuildDetail;
import com.rtm.frm.utils.RMBuildDetailUtil;
import com.rtm.location.LocationApp;

public class SplashActivity extends DTActivity implements OnClickListener {

	private ImageView mScanner, mInput;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

		TextView tv_vision = (TextView) findViewById(R.id.tv_splash_vision);
		PackageManager manager = DTApplication.getInstance()
				.getPackageManager();
		PackageInfo info;
		try {
			info = manager.getPackageInfo(DTApplication.getInstance()
					.getPackageName(), 0);
			tv_vision.setText("V " + info.versionName);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		mScanner = (ImageView) findViewById(R.id.scanner);
		mInput = (ImageView) findViewById(R.id.input);
		findViewById(R.id.login).setOnClickListener(this);
		mInput.setOnClickListener(this);
		mScanner.setOnClickListener(this);
		
		LocationApp.getInstance().init(getApplicationContext());
		LocationApp.getInstance().start();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.scanner:
			Intent intent = new Intent(this, LCScannerActivity.class);
			startActivityForResult(intent, 1);
			break;
		case R.id.input:
			InputPlanActivity.interActivity(this);
			break;
		case R.id.login:
//			LoginActivity.interActivity(this);
			Intent intent1 = new Intent(this, ArShowActivity.class);
			intent1.putExtra(ArShowActivity.KEY_MAP_DEGREE, 0f);
			startActivity(intent1);
			break;
		default:
			break;
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		LocationApp.getInstance().stop();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != Activity.RESULT_OK)
			return;
		if (requestCode == 1) {
			Bundle bundle = data.getExtras();
			// 显示扫描到的内容
			String result = bundle.getString("result");
			DTLog.i("beacon-info : " + result);
			MyPlanActivity.interActivity(this);
			// pushActivity(MainActivity.class, null);
		}
	}

	@Override
	public String getPageName() {
		return null;
	}
}
