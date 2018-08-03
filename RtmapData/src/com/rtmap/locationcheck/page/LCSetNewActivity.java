package com.rtmap.locationcheck.page;

import im.fir.sdk.FIR;
import im.fir.sdk.VersionCheckCallback;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;

import com.rtm.common.utils.RMVersionCommon;
import com.rtm.frm.utils.RMVersionMap;
import com.rtm.location.utils.RMVersionLocation;
import com.rtmap.checkpicker.R;
import com.rtmap.locationcheck.adapter.LCMapDialogAdapter;
import com.rtmap.locationcheck.core.LCActivity;
import com.rtmap.locationcheck.core.LCApplication;
import com.rtmap.locationcheck.util.DTFileUtils;
import com.rtmap.locationcheck.util.DTLog;
import com.rtmap.locationcheck.util.DTUIUtils;
import com.rtmap.locationcheck.util.DownloadService;

public class LCSetNewActivity extends LCActivity implements OnClickListener {

	private EditText mAdjust;// 步进值
	private RadioButton mTestBtn, mPublicBtn;
	private Switch mOpenLoc;
	private TextView mVersion;// 版本号
	private boolean isUpdate;// 正在更新吗
	private TextView mPickText;// 采集模式
	private String[] mModeArray;
	private Dialog mCollectDialog;// 间隔dialog

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
		mOpenLoc = (Switch) findViewById(R.id.location_switch);
		mPublicBtn = (RadioButton) findViewById(R.id.radio_public);
		boolean istext = LCApplication.getInstance().getShare()
				.getBoolean("istest", false);
		if (istext)
			mTestBtn.setChecked(true);
		else
			mPublicBtn.setChecked(true);

		boolean isOpen = LCApplication.getInstance().getShare()
				.getBoolean("open_location", true);
		mOpenLoc.setChecked(isOpen);

		findViewById(R.id.web_url).setOnClickListener(this);
		findViewById(R.id.open_beacon_scanner).setOnClickListener(this);
		findViewById(R.id.logout).setOnClickListener(this);
		mPickText = (TextView) findViewById(R.id.pick_mode);
		mModeArray = getResources().getStringArray(R.array.map_dialog_item);
		mPickText.setText(mModeArray[LCApplication.getInstance().getShare()
				.getInt(DTFileUtils.PICK_MODE, 0)]);
		mPickText.setOnClickListener(this);

		mVersion = (TextView) findViewById(R.id.version);
		mVersion.setText("当前版本：" + LCApplication.VERSION);
		mVersion.setOnClickListener(this);
		TextView libVersion = (TextView) findViewById(R.id.lib_version);

		libVersion.setText("定位库：" + RMVersionLocation.VERSION + "     SO库："
				+ RMVersionLocation.SO_VERSION + "\n地图库："
				+ RMVersionMap.VERSION + "    公共库：" + RMVersionCommon.VERSION);

		findViewById(R.id.qq).setOnClickListener(this);
		findViewById(R.id.phone).setOnClickListener(this);
		checkUpdate();

		initCollectDialog();
	}

	/**
	 * 检查更新
	 */
	private void checkUpdate() {
		if (!isUpdate) {
			isUpdate = true;
			FIR.checkForUpdateInFIR("70cedea02e5dfb7a81d5c6baabb666fc",
					new VersionCheckCallback() {
						@Override
						public void onSuccess(String versionJson) {
							DTLog.i("check from fir.im success! " + "\n"
									+ versionJson);
							isUpdate = false;
							try {
								JSONObject o = new JSONObject(versionJson);
								int versionCode = Integer.parseInt(o
										.getString("version"));
								if (versionCode > LCApplication.VERSION_CODE) {
									showUploadDialog(o.getString("changelog"),
											o.getString("installUrl"),
											o.getString("versionShort"),
											versionCode);
									mVersion.setText("当前版本："
											+ LCApplication.VERSION + "(点击更新V"
											+ o.getString("versionShort") + ")");
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}

						@Override
						public void onFail(Exception exception) {
							Log.i("fir", "check fir.im fail! " + "\n"
									+ exception.getMessage());
						}

						@Override
						public void onStart() {
						}

						@Override
						public void onFinish() {
						}
					});
		} else {
			DTUIUtils.showToastSafe("正在更新..");
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
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
		LCApplication.getInstance().getShare().edit()
				.putBoolean("open_location", mOpenLoc.isChecked()).commit();

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.qq:
			ClipboardManager clip = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			clip.setText("953022119"); // 复制
			DTUIUtils.showToastSafe("已复制到剪切板");
			break;
		case R.id.open_beacon_scanner:
			Intent intent4 = new Intent(this, LCBeaconLIstActivity.class);
			intent4.putExtra("sign", 1);
			startActivity(intent4);
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
		case R.id.web_url:
			Uri uri = Uri
					.parse("http://api.rtmap.com:30006/mobile_page/index.html");
			Intent intent1 = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(intent1);
			break;
		case R.id.logout:
			LCApplication.getInstance().getShare().edit()
					.putString(DTFileUtils.PREFS_PASSWORD, "").commit();
			for (int i = 0; i < mActivityList.size(); i++) {
				if (!mActivityList.get(i).isFinishing())
					mActivityList.get(i).finish();
			}
			mActivityList.clear();
			LCLoginActivity.interActivity(this);
			finish();
			break;
		case R.id.pick_mode:// 采集模式修改
			mCollectDialog.show();
			break;
		}
	}

	/**
	 * show弹出框
	 */
	private void initCollectDialog() {
		mCollectDialog = new Dialog(this, R.style.dialog);
		mCollectDialog.setContentView(R.layout.dialog_map_layout);
		mCollectDialog.setCanceledOnTouchOutside(true);
		ListView mInterList = (ListView) mCollectDialog
				.findViewById(R.id.set_list);

		mInterList.setAdapter(new LCMapDialogAdapter(this, mModeArray));
		mInterList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				LCApplication.getInstance().getShare().edit()
						.putInt(DTFileUtils.PICK_MODE, position).commit();
				mPickText.setText(mModeArray[position]);
				mCollectDialog.cancel();
			}
		});
	}

}
