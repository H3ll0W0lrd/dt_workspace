package com.rtmap.locationdemo;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.rtm.common.model.RMLocation;
import com.rtm.common.model.RMPois;
import com.rtm.common.utils.RMConfig;
import com.rtm.common.utils.RMFileUtil;
import com.rtm.common.utils.RMLog;
import com.rtm.common.utils.RMStringUtils;
import com.rtm.frm.model.RMBuildDetail;
import com.rtm.frm.utils.RMBuildDetailUtil;
import com.rtm.location.LocationApp;
import com.rtm.location.entity.FenceInfo;
import com.rtm.location.entity.ReceiveLocationMode;
import com.rtm.location.utils.OnFenceListener;
import com.rtm.location.utils.RMLocationListener;
import com.rtm.location.utils.RMSearchLbsPoiUtil.OnSearchLbsPoiListener;
import com.rtm.location.utils.RMVersionLocation;
import com.rtmap.locationdemo.beta.R;

public class LocationActivity extends Activity implements OnClickListener,
		RMLocationListener {

	private TextView locateView, versionView, mPoi, mBuild, mLogText,
			mFingerText;
	private Button mButton, mSpanOk;
	private EditText mTimeText;
	private long time;
	private RadioButton mPublicRio, mTestRio, mDataRecycle, mServerOutPut;
	private Gson mGson;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.location);
		initView();
		time = System.currentTimeMillis();
		mGson = new Gson();
		// 定位服务初始化
		LocationApp.getInstance().registerLocationListener(this);
		RMLog.LOG_LEVEL = RMLog.LOG_LEVEL_INFO;
		LocationApp.getInstance().setTestStatus(false);
		LocationApp.getInstance().setUseRtmapError(true);
		LocationApp.getInstance().addFenceListener(new OnFenceListener() {

			@Override
			public void onFenceListener(FenceInfo fence) {
				Toast.makeText(
						getApplicationContext(),
						"围栏提示啦:" + fence.getBuildId() + "  " + fence.getFloor(),
						5000).show();
			}
		});
		// LocationApp.getInstance().setRequestSpanTime(3000);
		mPublicRio = (RadioButton) findViewById(R.id.server_public);
		mTestRio = (RadioButton) findViewById(R.id.server_test);
		mPublicRio.setOnClickListener(this);
		mTestRio.setOnClickListener(this);
		mDataRecycle = (RadioButton) findViewById(R.id.data_recycle);
		mServerOutPut = (RadioButton) findViewById(R.id.server_output);

		if (LocationApp.getInstance().getReceiveLocationMode() == ReceiveLocationMode.DATA_RECYCLE) {
			mDataRecycle.setChecked(true);
		} else {
			mServerOutPut.setChecked(true);
		}
		mDataRecycle.setOnClickListener(this);
		mServerOutPut.setOnClickListener(this);

		LocationApp.getInstance().setRootFolder("TestDingtao/public");
		mFingerText.setText("定位Finger文件路径：sdcard/TestDingtao/public");

		mTimeText.setText(LocationApp.getInstance().getRequestSpanTime() / 1000
				+ "");

		String logpath = RMFileUtil.getLogDir() + "location-log-"
				+ RMConfig.mac + ".txt";
		File file = new File(logpath);
		if (file.exists()) {
			mLogText.setText("错误Log路径：" + logpath);
		} else {
			mLogText.setText("无错误log日志");
		}
		LocationApp.getInstance().init(getApplicationContext());
	}

	@Override
	protected void onResume() {
		super.onResume();
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
				time = System.currentTimeMillis();
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
			boolean isLocate = LocationApp.getInstance().getLocatePoiInfo(null,
					new OnSearchLbsPoiListener() {

						@Override
						public void onFinished(RMPois result) {
							mPoi.setText(mGson.toJson(result));
						}
					});
			if (!isLocate) {
				mPoi.setText("缺少位置信息");
			}
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
		case R.id.data_recycle:
			LocationApp.getInstance().setReceiveLocationMode(
					ReceiveLocationMode.DATA_RECYCLE);
			mDataRecycle.setChecked(true);
			break;
		case R.id.server_output:
			LocationApp.getInstance().setReceiveLocationMode(
					ReceiveLocationMode.SERVER_OUTPUT);
			mServerOutPut.setChecked(true);
			break;
		}
	}

	@Override
	protected void onDestroy() {
		LocationApp.getInstance().unRegisterLocationListener(this);
		LocationApp.getInstance().stop();
		super.onDestroy();
	}

	private RMLocation mLocation;

	@Override
	public void onReceiveLocation(RMLocation location) {
		mLocation = location;
		long t = System.currentTimeMillis() - time;
		locateView.setText("最终结果\nlbsid:" + location.getLbsid() + "\nUserId: "
				+ location.getUserID() + "\nerror: " + location.getError()
				+ "\nbuild: " + location.getBuildID() + "\nfloor: "
				+ location.getFloor() + "\nx: " + location.getX() + "\ny:  "
				+ location.getY() + "\n精度（米）:" + location.getAccuracy()
				+ "\n推算类型：" + location.getCalculateType() + "\n纬度："
				+ location.getLatitude() + "\n经度：" + location.getLongitude()
				+ "\n已运行s：" + t / 1000f);
		// Log.i("rtmap", "扫描信息：" + LocationApp.getInstance().getScannerInfo());
		versionView.setText(getString(R.string.locateSdkVersion)
				+ RMVersionLocation.VERSION);
	}

}
