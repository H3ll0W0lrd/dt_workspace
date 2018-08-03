package com.rtmap.locationcheck.page;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Switch;
import android.widget.TextView;

import com.google.gson.Gson;
import com.rtm.common.utils.RMStringUtils;
import com.rtmap.checkpicker.R;
import com.rtmap.locationcheck.adapter.LCUuidAdapter;
import com.rtmap.locationcheck.core.LCActivity;
import com.rtmap.locationcheck.core.LCApplication;
import com.rtmap.locationcheck.core.LCAsyncTask;
import com.rtmap.locationcheck.core.LCCallBack;
import com.rtmap.locationcheck.core.http.LCHttpClient;
import com.rtmap.locationcheck.core.http.LCHttpUrl;
import com.rtmap.locationcheck.core.model.Floor;
import com.rtmap.locationcheck.core.model.UuidList;
import com.rtmap.locationcheck.util.DTFileUtils;
import com.rtmap.locationcheck.util.DTStringUtils;
import com.rtmap.locationcheck.util.DTUIUtils;

public class LCBeaconSetActivity extends LCActivity implements
		OnSeekBarChangeListener, OnClickListener {
	private TextView mThreText;
	private Switch mThreSwitch, mMajorSwitch, mMinorSwitch, mInt16Swith;
	private SeekBar mThreSeek;
	private int mValue;
	private TextView mUpdateTime;
	private TextView mAddUuid;// 添加uuid按钮
	private TextView mDisplayUuid;// 选择UUid
	private Floor mFloor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lc_beacon_set);
		mFloor = (Floor) getIntent().getExtras().getSerializable("floor");
		mThreText = (TextView) findViewById(R.id.threshold_text);
		mThreSeek = (SeekBar) findViewById(R.id.threshold_seek);
		mThreSwitch = (Switch) findViewById(R.id.threshold_switch);
		mMajorSwitch = (Switch) findViewById(R.id.major_switch);
		mMinorSwitch = (Switch) findViewById(R.id.minor_switch);
		mInt16Swith = (Switch) findViewById(R.id.int16_switch);
		mValue = LCApplication.getInstance().getShare()
				.getInt("threshold", -99);
		mThreSeek.setProgress((int) ((mValue + 99) / 74.0f * 100));
		mThreText.setText(getString(R.string.threshold_value, mValue));
		mThreSeek.setOnSeekBarChangeListener(this);
		mThreSwitch.setChecked(LCApplication.getInstance().getShare()
				.getBoolean("threshold_switch", false));
		mMajorSwitch.setChecked(LCApplication.getInstance().getShare()
				.getBoolean("major_switch", false));
		mMinorSwitch.setChecked(LCApplication.getInstance().getShare()
				.getBoolean("minor_switch", false));
		mInt16Swith.setChecked(LCApplication.getInstance().getShare()
				.getBoolean("int16_switch", false));

		mAddUuid = (TextView) findViewById(R.id.add_uuid);
		mDisplayUuid = (TextView) findViewById(R.id.display_uuid);
		mUpdateTime = (TextView) findViewById(R.id.update_time);

		mDisplayUuid.setOnClickListener(this);
		mAddUuid.setOnClickListener(this);
		mDisplayUuid.setText(LCApplication.getInstance().getShare()
				.getString("uuid", "C91A"));
		findViewById(R.id.update).setOnClickListener(this);
		initUUidListDialog();
		getFileTime();
	}

	private void getFileTime() {
		long time = LCApplication.getInstance().getShare()
				.getLong("update_beacon", 0);
		if (time == 0) {
			mUpdateTime.setText("更新beacon点位");
		} else {
			SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
			mUpdateTime.setText("更新beacon点位\n更新时间：" + format.format(new Date(time)));
		}
	}

	/**
	 * 初始化列表
	 */
	private void initUUidListDialog() {
		mUUidListDialog = new Dialog(this, R.style.dialog);
		mUUidListDialog.setContentView(R.layout.dialog_map_layout);
		mUUidListDialog.setCanceledOnTouchOutside(true);
		ListView list = (ListView) mUUidListDialog.findViewById(R.id.set_list);
		mUuidAdapter = new LCUuidAdapter();
		list.setAdapter(mUuidAdapter);
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				LCApplication.getInstance().getShare().edit()
						.putString("uuid", mUuidAdapter.getItem(position))
						.commit();
				mDisplayUuid.setText(mUuidAdapter.getItem(position));
				mUUidListDialog.cancel();
			}
		});
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		mValue = progress * 74 / 100 - 99;
		mThreText.setText(getString(R.string.threshold_value, mValue));
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {

	}

	@Override
	protected void onPause() {
		super.onPause();
		LCApplication.getInstance().getShare().edit()
				.putInt("threshold", mValue).commit();
		LCApplication.getInstance().getShare().edit()
				.putBoolean("threshold_switch", mThreSwitch.isChecked())
				.commit();
		LCApplication.getInstance().getShare().edit()
				.putBoolean("major_switch", mMajorSwitch.isChecked()).commit();
		LCApplication.getInstance().getShare().edit()
				.putBoolean("minor_switch", mMinorSwitch.isChecked()).commit();
		LCApplication.getInstance().getShare().edit()
				.putBoolean("int16_switch", mInt16Swith.isChecked()).commit();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.add_uuid:
			initUuidDialog();
			break;
		case R.id.update:
			mLoadDialog.show();
			new LCAsyncTask(new BeaconDownLoadCall()).run();
			break;
		case R.id.display_uuid:
			String uuid_list = LCApplication.getInstance().getShare()
					.getString("uuid_list", "");
			UuidList list;
			if (!RMStringUtils.isEmpty(uuid_list)) {
				Gson gson = new Gson();
				list = gson.fromJson(uuid_list, UuidList.class);
			} else {
				list = new UuidList();
				list.setList(new ArrayList<String>());
				list.getList().add("C91A");
			}
			mUuidAdapter.clearList();
			mUuidAdapter.addList(list.getList());
			mUUidListDialog.show();
			break;
		}
	}

	/**
	 * 下载beacon信息
	 * 
	 * @author dingtao
	 *
	 */
	class BeaconDownLoadCall implements LCCallBack {

		@Override
		public Object onCallBackStart(Object... obj) {
			String url = String.format(
					LCHttpUrl.DOWNLOAD_BEACON,
					LCApplication.getInstance().getShare()
							.getString(DTFileUtils.PREFS_TOKEN, ""),
					mFloor.getBuildId(),
					DTStringUtils.floorTransform(mFloor.getFloor()));
			String path = DTFileUtils.getDownloadDir() + "data.zip";
			if (LCHttpClient.downloadFile(path, url)) {
				DTUIUtils.showToastSafe(R.string.beacon_down_success);
				String zippath = DTFileUtils.getDownloadDir() + "data.zip";
				String filepath = DTFileUtils.getDataDir()+mFloor.getBuildId()+File.separator
						+ mFloor.getBuildId() + "_" + mFloor.getFloor()
						+ ".txt";
				DTFileUtils.zipToFile(zippath, filepath);
				LCApplication.getInstance().getShare().edit()
						.putLong("update_beacon", System.currentTimeMillis())
						.commit();
				return filepath;
			} else {
				DTUIUtils.showToastSafe(R.string.beacon_down_fail);
			}
			return null;
		}

		@Override
		public void onCallBackFinish(Object obj) {
			mLoadDialog.cancel();
			isUpdate = true;
			getFileTime();
		}
	}

	public static boolean isUpdate;

	private Dialog mUUidListDialog;
	private LCUuidAdapter mUuidAdapter;
	private EditText mUuidEdit;

	private void initUuidDialog() {
		mUuidEdit = new EditText(this);
		new AlertDialog.Builder(this).setTitle("添加Uuid").setView(mUuidEdit)
				.setNegativeButton("取消", null)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String uuid = mUuidEdit.getText().toString();
						if (!RMStringUtils.isEmpty(uuid)) {
							String uuid_list = LCApplication.getInstance()
									.getShare().getString("uuid_list", "");
							UuidList list;
							Gson gson = new Gson();
							if (!RMStringUtils.isEmpty(uuid_list)) {
								list = gson.fromJson(uuid_list, UuidList.class);
							} else {
								list = new UuidList();
								list.setList(new ArrayList<String>());
								list.getList().add("C91A");
							}
							list.getList().add(uuid);
							LCApplication.getInstance().getShare().edit()
									.putString("uuid_list", gson.toJson(list))
									.commit();
						}
						dialog.cancel();
					}
				}).create().show();
	}

}
