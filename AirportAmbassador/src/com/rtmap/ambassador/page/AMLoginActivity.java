package com.rtmap.ambassador.page;

import im.fir.sdk.FIR;
import im.fir.sdk.VersionCheckCallback;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.rtm.location.sensor.BeaconSensor;
import com.rtmap.ambassador.R;
import com.rtmap.ambassador.adapter.AMAreaAdapter;
import com.rtmap.ambassador.core.DTActivity;
import com.rtmap.ambassador.core.DTApplication;
import com.rtmap.ambassador.core.DTAsyncTask;
import com.rtmap.ambassador.core.DTCallBack;
import com.rtmap.ambassador.core.DTSqlite;
import com.rtmap.ambassador.http.DTHttpUrl;
import com.rtmap.ambassador.http.DTHttpUtil;
import com.rtmap.ambassador.model.Area;
import com.rtmap.ambassador.model.AreaList;
import com.rtmap.ambassador.model.LoginUser;
import com.rtmap.ambassador.model.User;
import com.rtmap.ambassador.util.DTLog;
import com.rtmap.ambassador.util.DTStringUtil;
import com.rtmap.ambassador.util.DTUIUtil;

/**
 * 登录页面
 * 
 * @author dingtao
 *
 */
public class AMLoginActivity extends DTActivity implements OnClickListener {
	private EditText mAccount;
	private EditText mPas;
	private TextView mAreaText;
	private Button mLogin;
	private LoginCall mLoginCall;// 登陆回调接口
	private GetAreaListCall mAreaCall;// 获取区域接口

	public static void interActivity(Context context) {
		Intent intent = new Intent(context, AMLoginActivity.class);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String result = DTApplication.getInstance().getShare()
				.getString(DTStringUtil.PREFS_AREA, null);
		mArea = mGson.fromJson(result, Area.class);
		User user = DTSqlite.getInstance().getUser();
		if (user != null && user.getLogin() == DTSqlite.STATELOGIN) {// 是登录
			if (mArea == null) {
				DTSqlite.getInstance().exitLogin();
			} else {
				Intent intent = new Intent(getApplicationContext(),
						AMapActivity.class);
				startActivity(intent);
				finish();
				return;
			}
		}
		setContentView(R.layout.am_login);

		mAccount = (EditText) findViewById(R.id.user_number);
		mPas = (EditText) findViewById(R.id.user_pas);
		mLogin = (Button) findViewById(R.id.login_btn);
		mAreaText = (TextView) findViewById(R.id.user_area);

		mAccount.setText(DTApplication.getInstance().getShare()
				.getString(DTStringUtil.PREFS_USERNAME, ""));
		mPas.setText(DTApplication.getInstance().getShare()
				.getString(DTStringUtil.PREFS_PASSWORD, ""));

		TextView mVersion = (TextView) findViewById(R.id.version);
		mVersion.setText("当前版本：" + DTApplication.VERSION);

		mAreaText.setOnClickListener(this);
		mLogin.setOnClickListener(this);
		mAreaCall = new GetAreaListCall();

		if (result != null) {
			mArea = mGson.fromJson(result, Area.class);
			mAreaText.setText(mArea.getAreaName());
		}

		initAreaDialog();
		checkUpdate();
		if (!mAreaCall.isExecute) {
			mAreaCall.setShow(false);
			new DTAsyncTask(mAreaCall).run();
		}
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
							if (versionCode > DTApplication.VERSION_CODE) {
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
			if (DTStringUtil.isEmpty(account)) {
				DTUIUtil.showToastSafe(R.string.input_account);
				return;
			}
			if (DTStringUtil.isEmpty(pas)) {
				DTUIUtil.showToastSafe(R.string.input_pas);
				return;
			}
			if (mArea == null) {
				DTUIUtil.showToastSafe(R.string.input_area);
				return;
			}
			DTApplication.getInstance().getShare().edit()
					.putString(DTStringUtil.PREFS_USERNAME, account).commit();
			DTApplication.getInstance().getShare().edit()
					.putString(DTStringUtil.PREFS_PASSWORD, pas).commit();
			mLoadDialog.show();
			mLoginCall = new LoginCall();
			new DTAsyncTask(mLoginCall).run(account, pas);
			break;
		case R.id.user_area:
			mLoadDialog.cancel();
			mAreaAdapter.clearList();
			String result = DTApplication.getInstance().getShare()
					.getString(DTStringUtil.PREFS_AREA_LIST, null);
			if (result != null) {
				AreaList list = mGson.fromJson(result, AreaList.class);
				mAreaAdapter.addList(list.getRst().getAreaList());
				mAreaAdapter.notifyDataSetChanged();
				mAreaDialog.show();
			} else {
				mLoadDialog.show();
				if (!mAreaCall.isExecute) {
					mAreaCall.setShow(true);
					new DTAsyncTask(mAreaCall).run();
				} else {
					DTUIUtil.showToastSafe("获取区域中。。");
				}
			}
			break;
		}
	}

	private Dialog mAreaDialog;
	private AMAreaAdapter mAreaAdapter;
	private Area mArea;

	/**
	 * show弹出框
	 */
	private void initAreaDialog() {
		mAreaDialog = new Dialog(this, R.style.dialog);
		mAreaDialog.setContentView(R.layout.dialog_map_layout);
		mAreaDialog.setCanceledOnTouchOutside(true);
		ListView mInterList = (ListView) mAreaDialog
				.findViewById(R.id.set_list);
		mAreaAdapter = new AMAreaAdapter();
		mInterList.setAdapter(mAreaAdapter);
		mInterList.setOnItemClickListener(new OnItemClickListener() {// 当区域列表点击之后

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int position, long arg3) {
						mAreaDialog.cancel();
						mArea = mAreaAdapter.getItem(position);
						DTApplication
								.getInstance()
								.getShare()
								.edit()
								.putString(DTStringUtil.PREFS_AREA,
										mGson.toJson(mArea)).commit();
						mAreaText.setText(mArea.getAreaName());
					}
				});
	}

	/**
	 * 获取区域列表
	 * 
	 * @author dingtao
	 *
	 */
	class GetAreaListCall implements DTCallBack {
		private boolean isExecute;// 是否继续执行

		private boolean isShow;

		public GetAreaListCall() {
		}

		/**
		 * 是否显示弹出框
		 * 
		 * @param isShow
		 */
		public void setShow(boolean isShow) {
			this.isShow = isShow;
		}

		public void setExecute(boolean isExecute) {
			this.isExecute = isExecute;
		}

		@Override
		public Object onCallBackStart(Object... obj) {
			isExecute = true;
			String result = DTHttpUtil
					.connInfo(DTHttpUtil.POST, DTHttpUrl.AREA);
			if (result != null) {
				DTApplication.getInstance().getShare().edit()
						.putString(DTStringUtil.PREFS_AREA_LIST, result)
						.commit();
			}
			return null;
		}

		@Override
		public void onCallBackFinish(Object obj) {
			if (!isShow)
				return;
			mLoadDialog.cancel();
			isExecute = false;
			if (!mAreaDialog.isShowing()) {
				mAreaAdapter.clearList();
				String result = DTApplication.getInstance().getShare()
						.getString(DTStringUtil.PREFS_AREA_LIST, null);
				if (result != null) {
					AreaList list = mGson.fromJson(result, AreaList.class);
					mAreaAdapter.addList(list.getRst().getAreaList());
					mAreaAdapter.notifyDataSetChanged();
					mAreaDialog.show();
				} else {
					DTUIUtil.showToastSafe("获取区域失败，点击重新获取");
				}
			}
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
			String str = DTHttpUtil.connInfo(DTHttpUtil.POST, DTHttpUrl.LOGIN,
					new String[] { "staffCode", "password", "areaCode",
							"deviceId", "clientTime" },
					new Object[] { obj[0], obj[1], mArea.getAreaCode(), DTApplication.MAC,
							System.currentTimeMillis() });
			if (str != null) {
				LoginUser user = mGson.fromJson(str, LoginUser.class);
				if (user.getCode() == 0) {
					DTSqlite.getInstance().insertUser(user.getRst().getStaff());
				} else {
					DTUIUtil.showToastSafe(user.getMsg());
				}
			}
			return null;
		}

		@Override
		public void onCallBackFinish(Object obj) {
			if (isExecute) {
				if (mLoadDialog.isShowing()) {
					mLoadDialog.cancel();// 取消dialog
				}
				User user = DTSqlite.getInstance().getUser();
				if (user != null && user.getLogin() == DTSqlite.STATELOGIN) {// 是登录
					Intent intent = new Intent(getApplicationContext(),
							AMapActivity.class);
					startActivity(intent);
					finish();
				}
			}
		}
	}

	@Override
	public String getPageName() {
		return null;
	}
}
