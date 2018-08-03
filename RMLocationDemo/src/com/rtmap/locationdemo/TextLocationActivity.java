package com.rtmap.locationdemo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.rtm.common.model.POI;
import com.rtm.common.model.RMLocation;
import com.rtm.common.utils.RMConfig;
import com.rtm.common.utils.RMFileUtil;
import com.rtm.common.utils.RMLog;
import com.rtm.common.utils.RMStringUtils;
import com.rtm.frm.model.NavigatePoint;
import com.rtm.frm.model.RMBuildDetail;
import com.rtm.frm.model.RMPoiDetail;
import com.rtm.frm.utils.RMBuildDetailUtil;
import com.rtm.frm.utils.RMPoiDetailUtil;
import com.rtm.location.LocationApp;
import com.rtm.location.utils.RMLocationListener;
import com.rtm.location.utils.RMVersionLocation;
import com.rtmap.locationdemo.beta.R;

public class TextLocationActivity extends Activity implements OnClickListener,
		RMLocationListener, OnCheckedChangeListener {

	private TextView locateView, versionView, mPoi, mBuild, mLogText,
			mFingerText;
	private Button mButton, mSpanOk;
	private EditText mTimeText;
	private long time;
	private RadioButton mPublicRio, mTestRio;
	private RadioButton mLocOnline, mLocOffline, mLocMix;
	String logpath, logSuccess;
	boolean isStart = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.location);
		initView();
		time = System.currentTimeMillis();

		// 定位服务初始化
		LocationApp.getInstance().init(getApplicationContext());
		LocationApp.getInstance().registerLocationListener(this);
		RMLog.LOG_LEVEL = RMLog.LOG_LEVEL_INFO;
		LocationApp.getInstance().setTestStatus(true);
		NavigatePoint point;
		// LocationApp.getInstance().setUseRtmapError(true);
		// // LocationApp.getInstance().setRequestSpanTime(3000);
		// mPublicRio = (RadioButton) findViewById(R.id.server_public);
		// mTestRio = (RadioButton) findViewById(R.id.server_test);
		// mPublicRio.setOnCheckedChangeListener(this);
		// mTestRio.setOnCheckedChangeListener(this);
		//
		// mLocOnline = (RadioButton) findViewById(R.id.location_online);
		// mLocOffline = (RadioButton) findViewById(R.id.location_offline);
		// mLocMix = (RadioButton) findViewById(R.id.location_mix);
		// mLocOnline.setOnCheckedChangeListener(this);
		// mLocOffline.setOnCheckedChangeListener(this);
		// mLocMix.setOnCheckedChangeListener(this);
		// if (LocationApp.getInstance().getLbsSign() == LocationApp.MIX) {
		// mLocMix.setChecked(true);
		// } else if (LocationApp.getInstance().getLbsSign() ==
		// LocationApp.OFFLINE) {
		// mLocOffline.setChecked(true);
		// } else {
		// mLocOnline.setChecked(true);
		// }
		//
		LocationApp.getInstance().setRootFolder("TestDingtao/public");
		// mFingerText.setText("定位Finger文件路径：sdcard/TestDingtao/public");
		//
		// mTimeText.setText(LocationApp.getInstance().getRequestSpanTime() /
		// 1000
		// + "");

		// String logpath = RMFileUtil.getLogDir() + "location-log-"
		// + RMConfig.mac + ".txt";
		// File file = new File(logpath);
		// if (file.exists()) {
		// mLogText.setText("错误Log路径：" + logpath);
		// } else {
		// mLogText.setText("无错误log日志");
		// }
		// logpath = RMFileUtil.getLogDir() + "log-"
		// + getIntent().getIntExtra("count", 0) + "-" + RMConfig.mac
		// + ".txt";
		// logSuccess = RMFileUtil.getLogDir() + "log-success.txt";
		// LocationApp.getInstance().start();
		boolean result = LocationApp.getInstance().start();
	}

	private void initView() {
		locateView = (TextView) findViewById(R.id.tv_indoor_locate_info);
		mButton = (Button) findViewById(R.id.btn_start_locate);
		versionView = (TextView) findViewById(R.id.tv_sdk_version);
		mPoi = (TextView) findViewById(R.id.poi);
		mLogText = (TextView) findViewById(R.id.log_text);
		mTimeText = (EditText) findViewById(R.id.loc_span);
		mFingerText = (TextView) findViewById(R.id.finger_text);
		mSpanOk = (Button) findViewById(R.id.span_ok);
		mButton.setOnClickListener(this);
		mSpanOk.setOnClickListener(this);
		versionView.setText(getString(R.string.locateSdkVersion)
				+ RMVersionLocation.VERSION + "   "
				+ RMVersionLocation.SO_VERSION);
		mBuild = (TextView) findViewById(R.id.build);
		findViewById(R.id.poi_btn).setOnClickListener(this);
		findViewById(R.id.build_btn).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		InputMethodManager imm1 = (InputMethodManager) this
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm1.hideSoftInputFromWindow(v.getWindowToken(), 0);
		switch (v.getId()) {
		case R.id.btn_start_locate:
			if (mButton.getText().equals(getString(R.string.startLocate))) {
				boolean result = LocationApp.getInstance().start();
				if (result)
					mButton.setText(getString(R.string.stopLocate));
				else
					Toast.makeText(this, "请输入key", Toast.LENGTH_LONG).show();
			} else {
				LocationApp.getInstance().stop(); // 停止定位
				mButton.setText(getString(R.string.startLocate));
			}
			break;
		case R.id.poi_btn:
			RMPoiDetailUtil.requestPoiDetail(LocationApp.getInstance()
					.getApiKey(), "860100010020300001", "F5", "43",
					new RMPoiDetailUtil.OnGetPoiDetailListener() {

						@Override
						public void onFinished(RMPoiDetail result) {
							Toast.makeText(getApplicationContext(),
									result.getPoi().getName(), 3000).show();
						}
					});
			break;
		case R.id.span_ok:
			if (RMStringUtils.isEmpty(mTimeText.getText().toString()))
				Toast.makeText(getApplicationContext(), "请输入时间", 3000).show();
			else
				LocationApp
						.getInstance()
						.setRequestSpanTime(
								Integer.parseInt(mTimeText.getText().toString()) * 1000);
			break;
		case R.id.build_btn:
			if (mLocation != null && mLocation.getBuildID() != null
					&& !"".equals(mLocation.getBuildID())) {
				RMBuildDetailUtil.requestBuildDetail(LocationApp.getInstance()
						.getApiKey(), mLocation.getBuildID(),
						new RMBuildDetailUtil.OnGetBuildDetailListener() {

							@Override
							public void onFinished(RMBuildDetail result) {
								if (result.getError_code() == 0)
									mBuild.setText(result.getBuild()
											.getBuildName()
											+ result.getBuild().getName_qp()
											+ result.getBuild().getName_jp());
								if (result.getError_code() == 403) {
									mBuild.setText(result.getError_msg());
								}
							}
						});
			} else {
				Toast.makeText(getApplicationContext(), "请开启定位", 5000).show();
			}
			break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		LocationApp.getInstance().unRegisterLocationListener(this);
		LocationApp.getInstance().stop();
	}

	private RMLocation mLocation;

	@Override
	public void onReceiveLocation(RMLocation location) {
		mLocation = location;
		long t = System.currentTimeMillis() - time;
		locateView.setText("\nUserId: " + location.getUserID() + "\nerror: "
				+ location.getError() + "\nbuild: " + location.getBuildID()
				+ "\nfloor: " + location.getFloor() + "\nx: " + location.getX()
				+ "\ny:  " + location.getY() + "\n精度（米）:"
				+ location.getAccuracy() + "\n已运行s：" + t / 1000f);
		versionView.setText(getString(R.string.locateSdkVersion));
		if (t > 10000) {
			setResult(Activity.RESULT_OK);
			finish();
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isChecked) {
			switch (buttonView.getId()) {
			case R.id.server_public:
				LocationApp.getInstance().setTestStatus(false);
				LocationApp.getInstance().setRootFolder("TestDingtao/public");
				mFingerText.setText("定位Finger文件路径：sdcard/TestDingtao/public");
				break;

			case R.id.server_test:
				LocationApp.getInstance().setTestStatus(true);
				LocationApp.getInstance().setRootFolder("TestDingtao/test");
				mFingerText.setText("定位Finger文件路径：sdcard/TestDingtao/test");
				break;
			case R.id.location_mix:
				LocationApp.getInstance().setLbsSign(LocationApp.MIX);
				break;
			case R.id.location_offline:
				LocationApp.getInstance().setLbsSign(LocationApp.OFFLINE);
				break;
			case R.id.location_online:
				LocationApp.getInstance().setLbsSign(LocationApp.ONLINE);
				break;
			}
		}
	}
}
