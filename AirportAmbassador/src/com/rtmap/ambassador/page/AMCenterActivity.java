package com.rtmap.ambassador.page;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

import com.rtmap.ambassador.R;
import com.rtmap.ambassador.core.DTActivity;
import com.rtmap.ambassador.core.DTApplication;
import com.rtmap.ambassador.core.DTAsyncTask;
import com.rtmap.ambassador.core.DTCallBack;
import com.rtmap.ambassador.core.DTSqlite;
import com.rtmap.ambassador.http.DTHttpUrl;
import com.rtmap.ambassador.http.DTHttpUtil;
import com.rtmap.ambassador.model.Area;
import com.rtmap.ambassador.util.DTStringUtil;
import com.rtmap.ambassador.util.DTUIUtil;
import com.rtmap.ambassador.util.QRCodeUtil;

public class AMCenterActivity extends DTActivity implements OnClickListener {

	private ImageView mScanner;
	private Button mChangeArea, mAlertBtn, mLogout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.am_center);
		mScanner = (ImageView) findViewById(R.id.scanner);
		mChangeArea = (Button) findViewById(R.id.change_area);
		mAlertBtn = (Button) findViewById(R.id.notify_alert);
		mLogout = (Button) findViewById(R.id.logout_btn);

		mChangeArea.setOnClickListener(this);
		mAlertBtn.setOnClickListener(this);
		mLogout.setOnClickListener(this);
		findViewById(R.id.back).setOnClickListener(this);

		boolean open = DTApplication.getInstance().getShare()
				.getBoolean(DTStringUtil.PREFS_ALERT, true);
		if (open) {
			mAlertBtn.setText("出区提示音：开");
		} else {
			mAlertBtn.setText("出区提示音：关");
		}

		mScanner.setImageBitmap(QRCodeUtil.createQRImage(mUser.getQrCode(),
				400, 400, null));
	}

	@Override
	public String getPageName() {
		return null;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.change_area:
			Intent intent = new Intent(getApplicationContext(),
					AMAreaListActivity.class);
			startActivity(intent);
			finish();
			break;
		case R.id.notify_alert:
			boolean open = DTApplication.getInstance().getShare()
					.getBoolean(DTStringUtil.PREFS_ALERT, true);
			DTApplication.getInstance().getShare().edit()
					.putBoolean(DTStringUtil.PREFS_ALERT, !open).commit();
			if (open) {
				mAlertBtn.setText("出区提示音：关");
			} else {
				mAlertBtn.setText("出区提示音：开");
			}
			break;
		case R.id.logout_btn:
			mLoadDialog.show();
			new DTAsyncTask(new LogoutCall()).run();
			break;
		case R.id.back:
			finish();
			break;
		default:
			break;
		}
	}

	class LogoutCall implements DTCallBack {

		@Override
		public Object onCallBackStart(Object... obj) {
			String r = DTApplication.getInstance().getShare()
					.getString(DTStringUtil.PREFS_AREA, null);
			Area mArea = mGson.fromJson(r, Area.class);

			try {
				JSONObject json = new JSONObject();
				json.put("staffId", mUser.getId());
				json.put("staffName", mUser.getStaffName());
				json.put("staffCode", mUser.getStaffCode());
				json.put("areaCode", mArea.getAreaCode());
				json.put("deviceId", DTApplication.MAC);
				json.put("clientTime", System.currentTimeMillis());

				String result = DTHttpUtil.postConnection(DTHttpUrl.LOGOUT,
						json.toString());
				if (result != null) {
					JSONObject s = new JSONObject(result);
					if (s.getInt("code") == 0) {
						return true;
					} else {
						DTUIUtil.showToastSafe(s.getString("msg"));
					}
				}
				DTSqlite.getInstance().insertRequest(DTHttpUrl.LOGOUT,
						json.toString());
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return false;
		}

		@Override
		public void onCallBackFinish(Object obj) {
			mLoadDialog.cancel();
			if ((Boolean) obj) {// 确认登出

			}
			DTSqlite.getInstance().exitLogin();
			DTApplication.getInstance().clearActivity();
			Intent intent = new Intent(getApplicationContext(),
					AMLoginActivity.class);
			startActivity(intent);
		}

	}

}
