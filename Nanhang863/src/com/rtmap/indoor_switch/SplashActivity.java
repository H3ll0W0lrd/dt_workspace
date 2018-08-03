package com.rtmap.indoor_switch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.airport.test.R;
import com.baidu.location.BDLocation;
import com.rtmap.indoor_switch.base.BaseActivity;
import com.rtmap.indoor_switch.manager.AppContext;
import com.rtmap.indoor_switch.utils.DTLog;

public class SplashActivity extends BaseActivity {

	private final int JUMP_TO_MAIN = 1;
	private final int JUMP_TO_LOGIN = 2;
	private int jump = JUMP_TO_MAIN;
	private ImageView mScanner,mInput;

	@Override
	protected void loadViewLayout() {
		setContentView(R.layout.activity_splash);
	}

	@Override
	protected void findViewById() {
		// 设置版本号
		TextView tv_vision = (TextView) findViewById(R.id.tv_splash_vision);
		tv_vision.setText("V " + AppContext.instance().versionName);
		mScanner = (ImageView) findViewById(R.id.scanner);
		mInput = (ImageView) findViewById(R.id.input);
		mInput.setOnClickListener(this);
		mScanner.setOnClickListener(this);

		// 不判断是否登录
		// String username =
		// SharePrefUtil.getString(AppContext.instance(),"username","");
		// String password =
		// SharePrefUtil.getString(AppContext.instance(),"password","");
		// if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
		// jump = JUMP_TO_LOGIN;
		// }
	}
	
	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
		case R.id.scanner:
			Intent intent = new Intent(this, LCScannerActivity.class);
			startActivityForResult(intent, 1);
			break;
		case R.id.input:
			break;
		default:
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode!=Activity.RESULT_OK)
			return;
		if(requestCode==1){
			Bundle bundle = data.getExtras();
			// 显示扫描到的内容
			String result = bundle.getString("result");
			DTLog.i("beacon-info : " + result);
			pushActivity(MainActivity.class, null);
		}
	}

	@Override
	protected void processLogic() {
	}

	@Override
	protected void setListener() {

	}

	@Override
	protected void getDataAgain() {

	}

	@Override
	public void onReceiveLocation(BDLocation bdLocation) {

	}
}
