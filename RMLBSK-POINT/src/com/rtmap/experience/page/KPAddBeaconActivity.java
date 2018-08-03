package com.rtmap.experience.page;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.rtmap.experience.R;
import com.rtmap.experience.core.KPActivity;
import com.rtmap.experience.core.model.BeaconInfo;
import com.rtmap.experience.util.DTStringUtils;
import com.rtmap.experience.util.DTUIUtils;

/**
 * 添加beacon页面
 * 
 * @author dingtao
 *
 */
public class KPAddBeaconActivity extends KPActivity implements OnClickListener,TextWatcher {

	private TextView mMac;// mac地址
	private EditText mMajor, mMinor;
	private EditText mMin, mMax;
	private TextView mCoord;// 坐标文本
	private TextView mUUID;// uuid默认是C91A
	private Button mAdd;// 添加按钮
	public static ArrayList<BeaconInfo> mBeaconList;
	private BeaconInfo mBeaconInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.kp_add_beacon);
		mMac = (TextView) findViewById(R.id.mac);
		mMin = (EditText) findViewById(R.id.min);
		mMax = (EditText) findViewById(R.id.max);
		mMajor = (EditText) findViewById(R.id.major);
		mMinor = (EditText) findViewById(R.id.minor);
		mCoord = (TextView) findViewById(R.id.coord);
		mAdd = (Button) findViewById(R.id.add);
		mUUID = (TextView) findViewById(R.id.uuid);
		mMajor.addTextChangedListener(this);
		mMinor.addTextChangedListener(this);
		
		mMinor.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				String value = "0000";
				String mac = mMac.getText().toString();
				if (!DTStringUtils.isEmpty(s.toString())) {
					String minor16 = Integer.toHexString(Integer.parseInt(s
							.toString()));
					if (minor16.length() >= 4) {
						value = minor16.substring(0, 4);
					} else {
						value = value.substring(0, 4 - minor16.length())
								+ minor16;
					}
				}
				mMac.setText((mac.subSequence(0, 8) + value).toUpperCase());
			}
		});

		Bundle bundle = getIntent().getExtras();
		mBeaconInfo = (BeaconInfo) bundle.getSerializable("beacon");

		mUUID.setText(mBeaconInfo.getUuid());
		
		String major16 = String.format("%04x", mBeaconInfo.getMajor());
		String minor16 = String.format("%04x", mBeaconInfo.getMinor());
		mMinor.setText(mBeaconInfo.getMinor()+"");
		mMajor.setText(mBeaconInfo.getMajor()+"");

		String mac = "C91A"+major16+minor16;
		mMac.setText(mac);

		mCoord.setText(mBeaconInfo.getX() + "/" + mBeaconInfo.getY());
		mAdd.setOnClickListener(this);
	}
	
	@Override
	public void afterTextChanged(Editable s) {
		String value = "0000";
		String mac = mMac.getText().toString();
		if (!DTStringUtils.isEmpty(s.toString())) {
			String major16 = Integer
					.toHexString(Integer.parseInt(s.toString()));
			if (major16.length() >= 4) {
				value = major16.substring(0, 4);
			} else {
				value = value.substring(0, 4 - major16.length()) + major16;
			}
		}
		mMac.setText((mac.subSequence(0, 4) + value + mac.substring(8, 12))
				.toString());
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.add:
			String mac = mMac.getText().toString();
			String min = mMin.getText().toString();
			String max = mMax.getText().toString();
			mBeaconInfo.setMac(mac);// MAC一定要大写
			mBeaconInfo.setThreshold_switch_min(Integer.parseInt(min));
			mBeaconInfo.setThreshold_switch_max(Integer.parseInt(max));
			mBeaconInfo.setFloor(20010);
			Bundle bundle = new Bundle();
			bundle.putSerializable("beacon", mBeaconInfo);
			Intent intent = new Intent();
			getIntent().putExtras(bundle);
			intent.putExtras(bundle);
			setResult(Activity.RESULT_OK, intent);
			DTUIUtils.showToastSafe("添加成功");
			finish();
			break;
		}
	}
}
