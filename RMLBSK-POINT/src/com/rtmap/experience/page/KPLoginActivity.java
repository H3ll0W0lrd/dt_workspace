package com.rtmap.experience.page;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.rtmap.experience.R;
import com.rtmap.experience.core.KPActivity;
import com.rtmap.experience.core.KPApplication;
import com.rtmap.experience.core.KPAsyncTask;
import com.rtmap.experience.core.KPCallBack;
import com.rtmap.experience.core.exception.KPException;
import com.rtmap.experience.core.http.KPHttpClient;
import com.rtmap.experience.core.http.KPHttpUrl;
import com.rtmap.experience.core.model.UserInfo;
import com.rtmap.experience.util.DTFileUtils;
import com.rtmap.experience.util.DTStringUtils;
import com.rtmap.experience.util.DTUIUtils;

/**
 * 登录页面
 * 
 * @author dingtao
 *
 */
public class KPLoginActivity extends KPActivity implements OnClickListener {
	private EditText mPhone;
	private EditText mSms;
	private Button mLogin;
	private LinearLayout mLoginLayout;
	private LoginCall mLoginCall;// 登陆回调接口
	private Button mSmsBtn;
	private static final int SECOND = 60;
	private int count = SECOND;

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == 100) {
				count--;
				if (count > 0) {
					mSmsBtn.setText(getString(R.string.surplus_second, count));
					mHandler.sendEmptyMessageDelayed(100, 1000);
				} else {
					count = SECOND;
					mSmsBtn.setText("重新获取");
				}
			} else if (msg.what == 200) {
				KPMainActivity.interActivity(KPLoginActivity.this);
			} else if (msg.what == 300) {
				mLoginLayout.setVisibility(View.VISIBLE);
				mLoginLayout.startAnimation(AnimationUtils.loadAnimation(
						KPLoginActivity.this, R.anim.trans_left_in_center));
			}
		};
	};

	public static void interActivity(Context context) {
		Intent intent = new Intent(context, KPLoginActivity.class);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		KPApplication.getInstance().clearActivity();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.kp_welcome);
		mLoginLayout = (LinearLayout) findViewById(R.id.linearlayout);
		mPhone = (EditText) findViewById(R.id.phone);
		mSms = (EditText) findViewById(R.id.sms);
		mLogin = (Button) findViewById(R.id.login_btn);
		mSmsBtn = (Button) findViewById(R.id.send_auth_sms);
		mSmsBtn.setOnClickListener(this);
		mLoginLayout.setVisibility(View.GONE);
		mPhone.setText(KPApplication.getInstance().getShare()
				.getString(DTFileUtils.PHONE, ""));

		mLogin.setOnClickListener(this);
		if (!DTStringUtils.isEmpty(mUser.getKey())) {// 用户已经登录
			mHandler.sendEmptyMessageDelayed(200, 1000);
		} else {
			mHandler.sendEmptyMessageDelayed(300, 500);
		}
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
			String account = mPhone.getText().toString();
			String pas = mSms.getText().toString();
			if (DTStringUtils.isEmpty(account)) {
				DTUIUtils.showToastSafe(R.string.phone_input);
				return;
			}
			if (DTStringUtils.isEmpty(pas)) {
				DTUIUtils.showToastSafe(R.string.input_pas);
				return;
			}
			KPApplication.getInstance().getShare().edit()
					.putString(DTFileUtils.PHONE, account).commit();
			mLoadDialog.show();
			mLoginCall = new LoginCall();
			new KPAsyncTask(mLoginCall).run(account, pas);
			break;
		case R.id.send_auth_sms:
			if (mSmsBtn.getText().toString().contains("\u5269\u4F59"))
				return;
			String phone1 = mPhone.getText().toString();
			if (!DTStringUtils.isEmpty(phone1)) {
				if (isMobileNO(phone1)) {
					new KPAsyncTask(new SendSms()).run(phone1);
				} else {
					DTUIUtils.showToastSafe(R.string.phone_input_corrent);
				}
			} else {
				DTUIUtils.showToastSafe(R.string.phone_input);
			}
			break;
		}
	}

	/**
	 * 登录接口
	 * 
	 * @author dingtao
	 *
	 */
	class LoginCall implements KPCallBack {

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
				String str = KPHttpClient.getInfo(KPHttpClient.GET,
						KPHttpUrl.LOGIN, new String[] { "mobileNum",
								"verifiCode" }, new String[] { (String) obj[0],
								(String) obj[1] });
				Gson gson = new Gson();
				return gson.fromJson(str, UserInfo.class);
			} catch (KPException e) {
				e.printStackTrace();
				DTUIUtils.showToastSafe(e.getMsg());
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
					UserInfo user = (UserInfo) obj;
					KPApplication.getInstance().getShare().edit()
							.putString(DTFileUtils.PREFS_TOKEN, user.getKey())
							.commit();
					KPMainActivity.interActivity(KPLoginActivity.this);
				}
			}
		}
	}

	/**
	 * 发送短信
	 * 
	 * @author DingTao
	 * 
	 */
	class SendSms implements KPCallBack {

		@Override
		public Object onCallBackStart(Object... obj) {
			try {
				String str = KPHttpClient.getInfo(KPHttpClient.GET,
						KPHttpUrl.SMS, new String[] { "mobileNum" },
						new String[] { (String) obj[0] });
				return str;
			} catch (KPException e) {
				DTUIUtils.showToastSafe(e.getMsg());
			}
			return null;
		}

		@Override
		public void onCallBackFinish(Object obj) {
			if (obj != null) {
				mHandler.sendEmptyMessage(100);
			}
		}

	}
}
