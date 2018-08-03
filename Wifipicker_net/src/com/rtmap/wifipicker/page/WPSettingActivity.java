package com.rtmap.wifipicker.page;

import java.util.regex.Pattern;

import com.rtmap.wifipicker.core.WPApplication;
import com.rtmap.wifipicker.core.http.WPHttpUrl;
import com.rtmap.wifipicker.wifi.NetGatherData;
import com.rtmap.wifipicker.R;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.view.KeyEvent;
import android.widget.Toast;

public class WPSettingActivity extends PreferenceActivity implements
		OnPreferenceChangeListener {

	private EditTextPreference mServerType;
	private EditTextPreference mIp;
	private EditTextPreference mTag1;
	private EditTextPreference mPort;
	private EditTextPreference mStepAdjust;
	private EditTextPreference mNetTime;

	private Preference pfModel;
	private Preference pfOsVersion;
	private Preference pfSoftVersion;

	private SharedPreferences sp = WPApplication.getInstance()
			.getShare();

	private boolean checkIP(String str) {
		Pattern pattern = Pattern
				.compile("^((\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]"
						+ "|[*])\\.){3}(\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])$");
		return pattern.matcher(str).matches();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.setting);

		// 服务器类型
		mServerType = (EditTextPreference) this.findPreference("server_type");
		mServerType.setOnPreferenceChangeListener(this);
		mServerType.setText(sp.getString("server_type", "1"));
		if (mServerType.getText().toString().equals("0")) {
			mServerType.setSummary("windows");
		} else {
			mServerType.setSummary("linux");
		}

		// 服务器ip地址
		mIp = (EditTextPreference) this.findPreference("ip");
		mIp.setOnPreferenceChangeListener(this);
		if (checkIP(sp.getString("ip", WPHttpUrl.WEB_URL))) {
			mIp.setText(sp.getString("ip", WPHttpUrl.WEB_URL));
			mIp.setSummary(mIp.getText());
		}

		// 端口号
		mPort = (EditTextPreference) this.findPreference("port");
		mPort.setOnPreferenceChangeListener(this);
		mPort.setText(sp.getString("port", "8081"));
		mPort.setSummary(mPort.getText());

		// MAC地址
		mTag1 = (EditTextPreference) this.findPreference("tag1");
		mTag1.setOnPreferenceChangeListener(this);
		mTag1.setText(sp.getString("tag1", "000000000000"));
		mTag1.setSummary(mTag1.getText());

		// 步进微调
		mStepAdjust = (EditTextPreference) this.findPreference("step_adjust");
		mStepAdjust.setOnPreferenceChangeListener(this);
		mStepAdjust.setText(sp.getString("step_adjust", "5"));
		mStepAdjust.setSummary(mStepAdjust.getText());

		// 网络时间
		mNetTime = (EditTextPreference) this.findPreference("net_time");
		mNetTime.setOnPreferenceChangeListener(this);
		mNetTime.setText(sp.getString("net_time", "60"));
		mNetTime.setSummary(mNetTime.getText());

		// 手机型号
		pfModel = findPreference("Model");
		pfModel.setSummary(Build.MODEL);

		// 操作系统版本
		pfOsVersion = findPreference("OSVersion");
		pfOsVersion.setSummary(Build.VERSION.RELEASE);

		// 软件版本
		try {
			pfSoftVersion = findPreference("SoftwareVersion");
			PackageManager pm = this.getPackageManager();
			PackageInfo info = pm.getPackageInfo(this.getPackageName(), 0);
			String version = info.versionName;
			pfSoftVersion.setSummary("" + version);
		} catch (Exception e) {
			pfSoftVersion.setSummary("VERSION TEST");
		}

	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (newValue == null) {
			Toast.makeText(this, "输入为空", Toast.LENGTH_LONG).show();
			return false;
		}

		// 服务器类型
		if (preference.getKey().equalsIgnoreCase("server_type")) {
			Pattern pattern = Pattern.compile("[0-1.]*");
			if (newValue.toString().equals("")) {
				Toast.makeText(this, "输入不能为空", Toast.LENGTH_LONG).show();
				return false;
			} else if (!pattern.matcher(newValue.toString()).matches()) {
				Toast.makeText(this, "输入错误", Toast.LENGTH_LONG).show();
				return false;
			}
		}

		// ip地址
		if (preference.getKey().equalsIgnoreCase("ip")) {
			if (!checkIP(newValue.toString())) {
				Toast.makeText(this, "IP输入错误", Toast.LENGTH_LONG).show();
				return false;
			}
		}

		// 端口号
		if (preference.getKey().equalsIgnoreCase("port")) {
			Pattern pattern = Pattern.compile("[0-9]*");
			if (newValue.toString().equals("")) {
				Toast.makeText(this, "输入不能为空", Toast.LENGTH_LONG).show();
				return false;
			} else if (!pattern.matcher(newValue.toString()).matches()) {
				Toast.makeText(this, "端口号输入错误", Toast.LENGTH_LONG).show();
				return false;
			}
		}

		// MAC
		if (preference.getKey().equalsIgnoreCase("tag1")) {
			Pattern pattern = Pattern.compile("[0-9a-fA-F]*");
			if (newValue.toString().equals("")) {
				Toast.makeText(this, "输入不能为空", Toast.LENGTH_LONG).show();
				return false;
			} else if (!pattern.matcher(newValue.toString()).matches()) {
				Toast.makeText(this, "mac地址输入错误", Toast.LENGTH_LONG).show();
				return false;
			}
		}

		if (preference.getKey().equalsIgnoreCase("step_adjust")) {
			// Pattern pattern = Pattern.compile("[0-9]*");
			// if (newValue.toString().equals("")) {
			// Toast.makeText(this, "输入不能为空", Toast.LENGTH_LONG).show();
			// return false;
			// } else if (!pattern.matcher(newValue.toString()).matches()) {
			// Toast.makeText(this, "输入错误", Toast.LENGTH_LONG).show();
			// return false;
			// }
		}

		preference.setSummary(newValue.toString());

		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);
		if (settings == null)
			return;

		NetGatherData.sServiceIp = mIp.getText();
		NetGatherData.sPORT = mPort.getText();
		NetGatherData.sMac1 = mTag1.getText();
		// WebGatherData.sStepAdjust = mStepAdjust.getText();

		WPApplication.getInstance().getShare().edit()
				.putString("ip", mIp.getText()).commit();
		WPApplication.getInstance().getShare().edit()
				.putString("port", mPort.getText()).commit();
		WPApplication.getInstance().getShare().edit()
				.putString("tag1", mTag1.getText()).commit();
		WPApplication.getInstance().getShare().edit()
				.putString("step_adjust", mStepAdjust.getText()).commit();
		WPApplication.getInstance().getShare().edit()
				.putString("net_time", mNetTime.getText()).commit();
		WPApplication.getInstance().getShare().edit()
				.putString("server_type", mServerType.getText()).commit();
	}

	@Override
	protected void onPause() {
		super.onPause();
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);
		if (settings == null)
			return;

		NetGatherData.sServiceIp = mIp.getText();
		NetGatherData.sPORT = mPort.getText();
		NetGatherData.sMac1 = mTag1.getText();
		// WebGatherData.sStepAdjust = mStepAdjust.getText();

		WPApplication.getInstance().getShare().edit()
				.putString("ip", mIp.getText()).commit();
		WPApplication.getInstance().getShare().edit()
				.putString("port", mPort.getText()).commit();
		WPApplication.getInstance().getShare().edit()
				.putString("tag1", mTag1.getText()).commit();
		WPApplication.getInstance().getShare().edit()
				.putString("step_adjust", mStepAdjust.getText()).commit();
		WPApplication.getInstance().getShare().edit()
				.putString("net_time", mNetTime.getText()).commit();
		WPApplication.getInstance().getShare().edit()
				.putString("server_type", mServerType.getText()).commit();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getAction() != KeyEvent.ACTION_DOWN) {
			return false;
		}
		this.finish();
		super.onBackPressed();
		return super.onKeyDown(keyCode, event);
	}

}
