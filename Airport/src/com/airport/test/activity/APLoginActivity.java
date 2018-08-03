package com.airport.test.activity;

import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.airport.test.R;
import com.dingtao.libs.DTActivity;
import com.dingtao.libs.DTApplication;
import com.dingtao.libs.util.DTLog;
import com.dingtao.libs.util.DTStringUtil;
import com.dingtao.libs.util.DTUIUtil;
import com.google.gson.Gson;

/**
 * 登录页面
 * 
 * @author dingtao
 *
 */
public class APLoginActivity extends DTActivity implements OnClickListener {
	private EditText mAccount;
	private EditText mPas;
	private Button mLogin;

	public static void interActivity(Context context) {
		Intent intent = new Intent(context, APLoginActivity.class);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		mAccount = (EditText) findViewById(R.id.user_number);
		mPas = (EditText) findViewById(R.id.user_pas);
		mLogin = (Button) findViewById(R.id.login_btn);

		mAccount.setText(DTApplication.getInstance().getShare()
				.getString("account", ""));
		mPas.setText(DTApplication.getInstance().getShare()
				.getString("pass", ""));
		findViewById(R.id.img_back).setOnClickListener(this);
		
		mLogin.setOnClickListener(this);
		DTLog.i("报名：" + this.getPackageName());
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
			if (DTStringUtil.isEmpty(account)) {
				DTUIUtil.showToastSafe(R.string.input_account);
				return;
			}
			if (DTStringUtil.isEmpty(pas)) {
				DTUIUtil.showToastSafe(R.string.input_pas);
				return;
			}
			DTApplication.getInstance().getShare().edit()
					.putString("account", account).commit();
			DTApplication.getInstance().getShare().edit()
					.putString("pass", pas).commit();
			finish();
			break;
		case R.id.img_back:
			finish();
			break;
		}
	}

	@Override
	public String getPageName() {
		return null;
	}

}
