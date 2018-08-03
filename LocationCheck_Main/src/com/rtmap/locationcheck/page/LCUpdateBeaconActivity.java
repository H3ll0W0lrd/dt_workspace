package com.rtmap.locationcheck.page;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.rtmap.locationcheck.R;
import com.rtmap.locationcheck.adapter.LCMapDialogAdapter;
import com.rtmap.locationcheck.core.LCActivity;
import com.rtmap.locationcheck.core.model.BeaconInfo;
import com.rtmap.locationcheck.util.DTStringUtils;
import com.rtmap.locationcheck.util.DTUIUtils;

public class LCUpdateBeaconActivity extends LCActivity implements
		OnClickListener, TextWatcher {

	private TextView mMac;
	private TextView mWorkStatus;// 工作状态
	private EditText mMin, mMax;
	private TextView mCoord;// 坐标
	private Button mUpadate;// 添加按钮
	private BeaconInfo mInfo;
	private Dialog mWorkDialog;

	private String[] mStatusStr;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lc_update_beacon);
		mMac = (TextView) findViewById(R.id.mac);
		mMin = (EditText) findViewById(R.id.min);
		mMax = (EditText) findViewById(R.id.max);
		mCoord = (TextView) findViewById(R.id.coord);
		mUpadate = (Button) findViewById(R.id.add);
		mWorkStatus = (TextView) findViewById(R.id.work_status);

		mStatusStr = getResources().getStringArray(R.array.beacon_work_status);

		mInfo = (BeaconInfo) getIntent().getExtras().getSerializable("beacon");

		if (mInfo != null) {
			mWorkStatus.setText(mStatusStr[Math.abs(mInfo.getWork_status())]);
			mMac.setText(mInfo.getMac());
			mMin.setText(mInfo.getThreshold_switch_min() + "");
			mMax.setText(mInfo.getThreshold_switch_max() + "");
			mCoord.setText(mInfo.getX() + "/" + mInfo.getY());
		}
		mWorkStatus.setOnClickListener(this);
		initWorkDialog();
		mUpadate.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.add:
			String min = mMin.getText().toString();
			String max = mMax.getText().toString();
			if (DTStringUtils.isEmpty(min) || DTStringUtils.isEmpty(max)) {
				DTUIUtils.showToastSafe(R.string.add_beacon_please);
				return;
			}
			if (mInfo.getEdit_status() != 2) {// 如果不属于新建
				if (mInfo.getThreshold_switch_max() != Integer.parseInt(max))
					mInfo.setEdit_status(3);// 编辑状态：0正常，1删除，2新建，3修改
				if (mInfo.getThreshold_switch_min() != Integer.parseInt(min))
					mInfo.setEdit_status(3);
			}

			mInfo.setThreshold_switch_min(Integer.parseInt(min));
			mInfo.setThreshold_switch_max(Integer.parseInt(max));
			Bundle bundle = new Bundle();
			bundle.putSerializable("beacon", mInfo);
			Intent intent = new Intent();
			intent.putExtras(bundle);
			setResult(Activity.RESULT_OK, intent);
			DTUIUtils.showToastSafe(R.string.update_beacon_success);
			finish();
			break;
		case R.id.work_status:
			mWorkDialog.show();
			break;
		}
	}

	/**
	 * 初始化工作状态弹出框
	 */
	private void initWorkDialog() {
		mWorkDialog = new Dialog(this, R.style.dialog);
		mWorkDialog.setContentView(R.layout.dialog_map_layout);
		mWorkDialog.setCanceledOnTouchOutside(true);
		ListView mInterList = (ListView) mWorkDialog
				.findViewById(R.id.set_list);
		mInterList.setAdapter(new LCMapDialogAdapter(this, mStatusStr));
		mInterList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				int status = mInfo.getWork_status();// 得到工作状态
				if (status == position)
					return;
				mInfo.setWork_status(-position);
				mWorkStatus.setText(mStatusStr[position]);
				mWorkDialog.cancel();
			}
		});
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
}
