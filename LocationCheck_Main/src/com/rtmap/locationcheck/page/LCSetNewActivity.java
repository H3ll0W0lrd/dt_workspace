package com.rtmap.locationcheck.page;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.gson.Gson;
import com.rtm.common.model.RMLocation;
import com.rtm.common.utils.RMStringUtils;
import com.rtm.common.utils.RMVersionCommon;
import com.rtm.frm.utils.RMVersionMap;
import com.rtm.location.utils.RMVersionLocation;
import com.rtmap.locationcheck.R;
import com.rtmap.locationcheck.adapter.LCUuidAdapter;
import com.rtmap.locationcheck.core.LCActivity;
import com.rtmap.locationcheck.core.LCApplication;
import com.rtmap.locationcheck.core.model.UuidList;
import com.rtmap.locationcheck.util.DTLog;
import com.rtmap.locationcheck.util.DTUIUtils;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;

public class LCSetNewActivity extends LCActivity implements OnClickListener {

	private EditText mAdjust;// 步进值
	private RadioButton mTestBtn, mPublicBtn;
	private TextView mVersion;// 版本号
	private boolean isUpdate;// 正在更新吗
	private TextView mAddUuid;// 添加uuid按钮
	private TextView mDisplayUuid;// 选择UUid

	public static void interActivity(Context context) {
		Intent intent = new Intent(context, LCSetNewActivity.class);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lc_set_new);
		mAdjust = (EditText) findViewById(R.id.adjust);
		mAdjust.setText(LCApplication.getInstance().getShare()
				.getString("step_adjust", "5"));
		mTestBtn = (RadioButton) findViewById(R.id.radio_test);
		mPublicBtn = (RadioButton) findViewById(R.id.radio_public);
		boolean istext = LCApplication.getInstance().getShare()
				.getBoolean("istest", false);
		if (istext)
			mTestBtn.setChecked(true);
		else
			mPublicBtn.setChecked(true);

		mAddUuid = (TextView) findViewById(R.id.add_uuid);
		mDisplayUuid = (TextView) findViewById(R.id.display_uuid);

		mDisplayUuid.setOnClickListener(this);
		mAddUuid.setOnClickListener(this);
		mDisplayUuid.setText(LCApplication.getInstance().getShare()
				.getString("uuid", "C91A"));
		initUUidListDialog();

		mVersion = (TextView) findViewById(R.id.version);
		mVersion.setText("当前版本：" + LCApplication.VERSION);
		mVersion.setOnClickListener(this);
		TextView libVersion = (TextView) findViewById(R.id.lib_version);

		libVersion.setText("定位库：" + RMVersionLocation.VERSION + "     SO库："
				+ RMVersionLocation.SO_VERSION + "\n地图库："
				+ RMVersionMap.VERSION + "    公共库：" + RMVersionCommon.VERSION);

		findViewById(R.id.qq).setOnClickListener(this);
		findViewById(R.id.phone).setOnClickListener(this);
		findViewById(R.id.beacon_mapping).setOnClickListener(this);
		checkUpdate();
	}

	/**
	 * 检查更新
	 */
	private void checkUpdate() {
		if (!isUpdate) {
			UmengUpdateAgent.setUpdateAutoPopup(false);
			UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {
				@Override
				public void onUpdateReturned(int updateStatus,
						UpdateResponse updateInfo) {
					isUpdate = false;
					switch (updateStatus) {
					case UpdateStatus.Yes: // has update
						DTLog.i("更新：" + updateInfo.version);
						mVersion.setText("当前版本：" + LCApplication.VERSION
								+ "(点击更新V" + updateInfo.version + ")");
						UmengUpdateAgent.showUpdateDialog(
								getApplicationContext(), updateInfo);
						break;
					}
				}
			});
			UmengUpdateAgent.update(this);
		} else {
			DTUIUtils.showToastSafe("正在更新..");
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		LCApplication.getInstance().getShare().edit()
				.putString("step_adjust", mAdjust.getText().toString())
				.commit();
		LCApplication
				.getInstance()
				.getShare()
				.edit()
				.putString(
						"major",
						((EditText) findViewById(R.id.display_major)).getText()
								.toString()).commit();
		LCApplication.getInstance().getShare().edit()
				.putBoolean("istest", mTestBtn.isChecked()).commit();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.beacon_mapping:
			LCBeaconMacActivity.interActivity(this);
			break;
		case R.id.qq:
			ClipboardManager clip = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			clip.setText("953022119"); // 复制
			DTUIUtils.showToastSafe("已复制到剪切板");
			break;
		case R.id.phone:
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_DIAL);
			intent.setData(Uri.parse("tel:" + "13126965104"));
			startActivity(intent);
			break;
		case R.id.version:
			checkUpdate();
			break;
		case R.id.add_uuid:
			initUuidDialog();
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

	private Dialog mUUidListDialog;
	private LCUuidAdapter mUuidAdapter;
	private AlertDialog.Builder mUuidDialog;
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

}
