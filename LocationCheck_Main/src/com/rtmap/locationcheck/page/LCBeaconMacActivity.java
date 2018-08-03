package com.rtmap.locationcheck.page;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.google.gson.Gson;
import com.rtm.common.utils.RMStringUtils;
import com.rtmap.locationcheck.R;
import com.rtmap.locationcheck.core.LCActivity;
import com.rtmap.locationcheck.core.LCApplication;
import com.rtmap.locationcheck.core.LCAsyncTask;
import com.rtmap.locationcheck.core.LCCallBack;
import com.rtmap.locationcheck.core.exception.LCException;
import com.rtmap.locationcheck.core.http.LCHttpClient;
import com.rtmap.locationcheck.core.http.LCHttpUrl;
import com.rtmap.locationcheck.core.model.BeaconInfo;
import com.rtmap.locationcheck.core.model.BroadcastInfo;
import com.rtmap.locationcheck.core.model.Build;
import com.rtmap.locationcheck.core.model.BuildList;
import com.rtmap.locationcheck.util.DTFileUtils;
import com.rtmap.locationcheck.util.DTLog;
import com.rtmap.locationcheck.util.DTStringUtils;
import com.rtmap.locationcheck.util.DTUIUtils;

public class LCBeaconMacActivity extends LCActivity implements OnClickListener {

	private TextView mTableInfo, mBeaconInfo;// 映射表信息，扫描信息
	private BuildList mBuildList;
	private Gson mGson = new Gson();

	public static void interActivity(Context context) {
		Intent intent = new Intent(context, LCBeaconMacActivity.class);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lc_beacon_mac);
		findViewById(R.id.update_table).setOnClickListener(this);
		findViewById(R.id.scanner).setOnClickListener(this);
		mTableInfo = (TextView) findViewById(R.id.table_info);
		mBeaconInfo = (TextView) findViewById(R.id.beacon_info);
		String str = LCApplication.getInstance().getShare()
				.getString("mapping", null);
		if (RMStringUtils.isEmpty(str)) {
			mTableInfo.setText("没有建筑物映射信息，请更新");
		} else {
			mBuildList = mGson.fromJson(str, BuildList.class);
			if (mBuildList.getMaplist() == null
					|| mBuildList.getMaplist().size() == 0) {
				mTableInfo.setText("没有建筑物映射信息，请更新");
			} else {
				mTableInfo.setText("本地有历史映射信息");
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.update_table:
			new LCAsyncTask(new TableInfo()).run();
			break;
		case R.id.scanner:
			mBeaconInfo.setText("");
			Intent intent1 = new Intent();
			intent1.setClass(this, LCScannerActivity.class);
			startActivityForResult(intent1, 100);
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_OK)
			return;
		if (requestCode == 100) {
			Bundle bundle = data.getExtras();
			// 显示扫描到的内容
			String result = bundle.getString("result");
			DTLog.i("beacon-info : " + result);
			if (!DTStringUtils.isEmpty(result)) {
				setBeaconInfoValue(result);
			} else {
				DTUIUtils.showToastSafe("无法识别信息");
			}
		}
	}

	/**
	 * 设置beacon值
	 * 
	 * @param result
	 */
	private void setBeaconInfoValue(String result) {
		DTLog.i(result);

		String[] params = result.split("_");
		String mac = null, major = null, minor = null, uuid, uuid_beta, value = "0000";

		uuid_beta = LCApplication.getInstance().getShare()
				.getString("uuid", "C91A").toUpperCase();
		if (!DTStringUtils.isEmpty(uuid_beta)) {
			if (uuid_beta.length() >= 4) {
				value = uuid_beta.substring(0, 4);
			} else {
				value = value.substring(0, 4 - uuid_beta.length()) + uuid_beta;
			}
		}
		if (params.length == 3) {
			mac = value + params[1] + params[2];
			major = Integer.parseInt(params[1], 16) + "";
			minor = Integer.parseInt(params[2], 16) + "";
			uuid = params[0];
		} else if (params.length == 7) {
			mac = value + params[2] + params[3];
			major = Integer.parseInt(params[2], 16) + "";
			minor = Integer.parseInt(params[3], 16) + "";
			uuid = params[1];
		} else if (params.length == 4) {
			mac = value + params[2] + params[3];
			major = Integer.parseInt(params[2], 16) + "";
			minor = Integer.parseInt(params[3], 16) + "";
			uuid = params[1];
		} else {
			DTUIUtils.showToastSafe("图片无法使用");
		}
		if (mac != null) {
			mBeaconInfo.setText("智慧图mac:" + mac + "\tmajor:" + major
					+ "\tminor:" + minor);
			new LCAsyncTask(new BeaconInfoCall()).run(mac);
		}
	}

	/**
	 * 获取映射表信息
	 * 
	 * @author dingtao
	 *
	 */
	class TableInfo implements LCCallBack {

		@Override
		public Object onCallBackStart(Object... obj) {
			try {
				String str = LCHttpClient.getOrDelete(LCHttpClient.GET,
						LCHttpUrl.BEACON_MAPPING, new String[] { "key" },
						new String[] { LCApplication.getInstance().getShare()
								.getString(DTFileUtils.PREFS_TOKEN, "") });
				BuildList list = mGson.fromJson(str, BuildList.class);
				LCApplication.getInstance().getShare().edit()
						.putString("mapping", str).commit();

				return list;
			} catch (LCException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public void onCallBackFinish(Object obj) {
			if (obj != null) {
				mBuildList = (BuildList) obj;
				if (mBuildList.getMaplist() == null
						|| mBuildList.getMaplist().size() == 0) {
					mTableInfo.setText("服务器没有建筑物映射信息");
				} else {
					mTableInfo.setText("更新成功");
				}
			} else {
				mTableInfo.setText("联网失败");
			}
		}
	}

	/**
	 * 获取beacon对应信息
	 * 
	 * @author dingtao
	 *
	 */
	class BeaconInfoCall implements LCCallBack {

		@Override
		public Object onCallBackStart(Object... obj) {
			try {
				String str = LCHttpClient
						.getOrDelete(
								LCHttpClient.GET,
								LCHttpUrl.BEACON_MAC,
								new String[] { "key", "mac" },
								new String[] {
										LCApplication
												.getInstance()
												.getShare()
												.getString(
														DTFileUtils.PREFS_TOKEN,
														""), (String) obj[0] });

				return mGson.fromJson(str, BeaconInfo.class);
			} catch (LCException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public void onCallBackFinish(Object obj) {
			if (obj != null) {
				BeaconInfo info = (BeaconInfo) obj;
				if (info.getMaclist() != null && info.getMaclist().size() != 0) {
					String text = mBeaconInfo.getText().toString();
					for (int i = 0; i < info.getMaclist().size(); i++) {
						BroadcastInfo broad = info.getMaclist().get(i);
						text += ("\nmac" + (i + 1) + ":" + broad.getMac());
						if (mBuildList != null
								&& mBuildList.getMaplist() != null) {
							for (int j = 0; j < mBuildList.getMaplist().size(); j++) {
								Build buildinfo = mBuildList.getMaplist()
										.get(j);
								if (buildinfo.getMajor().equals(
										broad.getMajor())
										&& buildinfo.getUuid().equals(
												broad.getUuid())) {
									text += "\t" + buildinfo.getAddress();
								}
							}
						}
					}
					mBeaconInfo.setText(text);
				} else {
					String text = mBeaconInfo.getText().toString();
					mBeaconInfo.setText(text + "\n没有映射信息");
				}
			}
		}
	}
}
