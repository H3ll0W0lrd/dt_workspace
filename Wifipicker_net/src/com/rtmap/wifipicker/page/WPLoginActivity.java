package com.rtmap.wifipicker.page;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.rtmap.wifipicker.R;
import com.rtmap.wifipicker.core.DTAsyncTask;
import com.rtmap.wifipicker.core.DTCallBack;
import com.rtmap.wifipicker.core.WPApplication;
import com.rtmap.wifipicker.core.exception.RMException;
import com.rtmap.wifipicker.core.http.WPHttpClient;
import com.rtmap.wifipicker.core.http.WPHttpUrl;
import com.rtmap.wifipicker.core.model.LoginUser;
import com.rtmap.wifipicker.util.DTFileUtils;
import com.rtmap.wifipicker.util.DTLog;
import com.rtmap.wifipicker.util.DTStringUtils;
import com.rtmap.wifipicker.util.DTUIUtils;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;

/**
 * 登录页面
 * 
 * @author dingtao
 *
 */
public class WPLoginActivity extends WPBaseActivity implements OnClickListener {
	private EditText mAccount;
	private EditText mPas;
	private Button mLogin;
	private LoginCall mLoginCall;// 登陆回调接口

	public static void interActivity(Context context) {
		Intent intent = new Intent(context, WPLoginActivity.class);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_activity);

		mAccount = (EditText) findViewById(R.id.username);
		mPas = (EditText) findViewById(R.id.password);
		mLogin = (Button) findViewById(R.id.login_btn);
		TextView text = (TextView) findViewById(R.id.version);
		text.setText("版本号："+WPApplication.VERSION);
		mAccount.setText(WPApplication.getInstance().getShare()
				.getString(DTFileUtils.PREFS_USERNAME, ""));
		mPas.setText(WPApplication.getInstance().getShare()
				.getString(DTFileUtils.PREFS_PASSWORD, ""));

		mLogin.setOnClickListener(this);
		checkUpdate();
		DTLog.i("报名：" + this.getPackageName());
	}

	/**
	 * 检查更新
	 */
	private void checkUpdate() {
		UmengUpdateAgent.setUpdateAutoPopup(false);
		UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {
			@Override
			public void onUpdateReturned(int updateStatus,
					UpdateResponse updateInfo) {
				switch (updateStatus) {
				case UpdateStatus.Yes: // has update
					DTLog.i("更新：" + updateInfo.version);
					UmengUpdateAgent.showUpdateDialog(getApplicationContext(),
							updateInfo);
					break;
				}
			}
		});
		UmengUpdateAgent.update(this);
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
			WPApplication.getInstance().getShare().edit()
					.putString(DTFileUtils.PREFS_USERNAME, account).commit();
			WPApplication.getInstance().getShare().edit()
					.putString(DTFileUtils.PREFS_PASSWORD, pas).commit();
			mDialogLoad.show();
			mLoginCall = new LoginCall();
			new DTAsyncTask(mLoginCall).run(account, pas);
			break;
		}
	}

	/**
	 * 登录接口
	 * 
	 * @author dingtao
	 *
	 */
	class LoginCall implements DTCallBack {

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
				String str = WPHttpClient.getOrDelete(WPHttpClient.GET,
						WPHttpUrl.LOGIN,
						new String[] { "username", "password" }, new String[] {
								(String) obj[0], (String) obj[1] });
				Gson gson = new Gson();
				return gson.fromJson(str, LoginUser.class);
			} catch (RMException e) {
				e.printStackTrace();
			}
			return null;

		}

		@Override
		public void onCallBackFinish(Object obj) {
			if (isExecute) {
				if (mDialogLoad.isShowing()) {
					mDialogLoad.cancel();// 取消dialog
				}
				if (obj != null) {
					LoginUser user = (LoginUser) obj;
					if (user.getStatus() == 1) {
						WPApplication
								.getInstance()
								.getShare()
								.edit()
								.putString(DTFileUtils.PREFS_TOKEN,
										user.getKey()).commit();
						Intent intent = new Intent(WPLoginActivity.this,
								WPMapListNewActivity.class);
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
