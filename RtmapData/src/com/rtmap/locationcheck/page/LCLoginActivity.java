package com.rtmap.locationcheck.page;

import im.fir.sdk.FIR;
import im.fir.sdk.VersionCheckCallback;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.rtmap.checkpicker.R;
import com.rtmap.locationcheck.core.LCActivity;
import com.rtmap.locationcheck.core.LCApplication;
import com.rtmap.locationcheck.core.LCAsyncTask;
import com.rtmap.locationcheck.core.LCCallBack;
import com.rtmap.locationcheck.core.exception.LCException;
import com.rtmap.locationcheck.core.http.LCHttpClient;
import com.rtmap.locationcheck.core.http.LCHttpUrl;
import com.rtmap.locationcheck.core.model.LoginUser;
import com.rtmap.locationcheck.pageNew.LCMapListNewActivity;
import com.rtmap.locationcheck.util.DTFileUtils;
import com.rtmap.locationcheck.util.DTLog;
import com.rtmap.locationcheck.util.DTStringUtils;
import com.rtmap.locationcheck.util.DTUIUtils;
import com.rtmap.locationcheck.util.DownloadService;

/**
 * 登录页面
 * 
 * @author dingtao
 *
 */
public class LCLoginActivity extends LCActivity implements OnClickListener {
	private EditText mAccount;
	private EditText mPas;
	private Button mLogin;
	private LoginCall mLoginCall;// 登陆回调接口

	public static void interActivity(Context context) {
		Intent intent = new Intent(context, LCLoginActivity.class);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lc_login);

		mAccount = (EditText) findViewById(R.id.user_number);
		mPas = (EditText) findViewById(R.id.user_pas);
		mLogin = (Button) findViewById(R.id.login_btn);

		mAccount.setText(LCApplication.getInstance().getShare()
				.getString(DTFileUtils.PREFS_USERNAME, ""));
		mPas.setText(LCApplication.getInstance().getShare()
				.getString(DTFileUtils.PREFS_PASSWORD, ""));

		TextView mVersion = (TextView) findViewById(R.id.version);
		mVersion.setText("当前版本：" + LCApplication.VERSION);

		mLogin.setOnClickListener(this);
		checkUpdate();
		DTLog.i("报名：" + this.getPackageName());
	}

	/**
	 * 检查更新
	 */
	private void checkUpdate() {

		// {"name":"数据采集","version":"27","changelog":"1 修复复杂面上传导致面缺失的问题；\r\n
		// 5 添加照片审核流程。\r\n6 优化beacon状态选择。","updated_at":1472807278,
		// "versionShort":"4.3.1","build":"27",
		// "installUrl":"http://download.fir.im/v2/app/install/56f3b19d748aac1915000028?download_token=7499c5c8337dc38d8df4f718485042f9","install_url":"http://download.fir.im/v2/app/install/56f3b19d748aac1915000028?download_token=7499c5c8337dc38d8df4f718485042f9","direct_install_url":"http://download.fir.im/v2/app/install/56f3b19d748aac1915000028?download_token=7499c5c8337dc38d8df4f718485042f9","update_url":"http://fir.im/xwc3",
		// "binary":{"fsize":5522602}}

		FIR.checkForUpdateInFIR("70cedea02e5dfb7a81d5c6baabb666fc",
				new VersionCheckCallback() {
					@Override
					public void onSuccess(String versionJson) {
						DTLog.i("check from fir.im success! " + "\n"
								+ versionJson);
						try {
							JSONObject o = new JSONObject(versionJson);
							int versionCode = Integer.parseInt(o
									.getString("version"));
							if (versionCode > LCApplication.VERSION_CODE) {
								showUploadDialog(o.getString("changelog"),
										o.getString("installUrl"),
										o.getString("versionShort"),
										versionCode);
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}

					@Override
					public void onFail(Exception exception) {
						Log.i("fir",
								"check fir.im fail! " + "\n"
										+ exception.getMessage());
					}

					@Override
					public void onStart() {
					}

					@Override
					public void onFinish() {
					}
				});
	}

	@Override
	public void cancelLoadDialog() {
		if (mLoginCall != null)
			mLoginCall.setExecute(false);
	}

	@Override
	public void onClick(View v) {
		InputMethodManager imm1 = (InputMethodManager) this
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm1.hideSoftInputFromWindow(v.getWindowToken(), 0);
		switch (v.getId()) {
		case R.id.login_btn:
			String account = mAccount.getText().toString();
			String pas = mPas.getText().toString();
			if (DTStringUtils.isEmpty(account)) {
				DTUIUtils.showToastSafe(R.string.input_account);
				return;
			}
			if (DTStringUtils.isEmpty(pas)) {
				DTUIUtils.showToastSafe(R.string.input_pas);
				return;
			}
			LCApplication.getInstance().getShare().edit()
					.putString(DTFileUtils.PREFS_USERNAME, account).commit();
			LCApplication.getInstance().getShare().edit()
					.putString(DTFileUtils.PREFS_PASSWORD, pas).commit();
			mLoadDialog.show();
			mLoginCall = new LoginCall();
			new LCAsyncTask(mLoginCall).run(account, pas);
			break;
		}
	}

	/**
	 * 登录接口
	 * 
	 * @author dingtao
	 *
	 */
	class LoginCall implements LCCallBack {

		private boolean isExecute;// 是否继续执行

		public LoginCall() {
			isExecute = true;
		}

		public void setExecute(boolean isExecute) {
			this.isExecute = isExecute;
		}

		@Override
		public Object onCallBackStart(Object... obj) {
			try {
				String str = LCHttpClient.getOrDelete(LCHttpClient.GET,
						LCHttpUrl.LOGIN,
						new String[] { "username", "password" }, new String[] {
								(String) obj[0], (String) obj[1] });
				// JSONObject j = new JSONObject(str);
				// LoginUser user = new LoginUser();
				// user.setKey(j.getString("key"));
				// user.setMessage(j.getString("message"));
				// user.setStatus(j.getInt("status"));
				// JSONArray buildL = j.getJSONArray("results");
				// if(buildL.length()>0){
				// ArrayList<Build> l = new ArrayList<Build>();
				// user.setResults(l);
				// for(int i=0;i<buildL.length();i++){
				// JSONObject o = buildL.getJSONObject(i);
				// Build b = new Build();
				// b.setBuildId(o.getString("buildId"));
				// b.setBuildName(o.getString("buildName"));
				// JSONArray floorarray = o.getJSONArray("floor");
				// for(int j=0;j<floorarray.length();j++){
				//
				// }
				// b.setFloor(floor);
				// }
				// }
				Gson gson = new Gson();
				return gson.fromJson(str, LoginUser.class);
			} catch (LCException e) {
				e.printStackTrace();
			}
			return null;

		}

		@Override
		public void onCallBackFinish(Object obj) {
			if (isExecute) {
				if (mLoadDialog.isShowing()) {
					mLoadDialog.cancel();// 取消dialog
				}
				if (obj != null) {
					LoginUser user = (LoginUser) obj;
					if (user.getStatus() == 1) {
						LCApplication
								.getInstance()
								.getShare()
								.edit()
								.putString(DTFileUtils.PREFS_TOKEN,
										user.getKey()).commit();
						Intent intent = new Intent(LCLoginActivity.this,
								LCMapListNewActivity.class);
						Bundle bundle = new Bundle();
						bundle.putSerializable("map", user);
						intent.putExtras(bundle);
						startActivity(intent);
						finish();
					} else {
						DTUIUtils.showToastSafe(user.getMessage());
					}
				} else {
					DTUIUtils.showToastSafe(R.string.server_error);
				}
			}
		}
	}
}
