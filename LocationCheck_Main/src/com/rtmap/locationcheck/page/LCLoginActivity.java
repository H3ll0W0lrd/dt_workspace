package com.rtmap.locationcheck.page;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;
import com.rtmap.locationcheck.R;
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
								LCMapListActivity.class);
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
