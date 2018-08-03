package com.rtmap.wifipicker.page;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.rtm.common.utils.Constants;
import com.rtm.frm.utils.Handlerlist;
import com.rtmap.wifipicker.R;
import com.rtmap.wifipicker.core.WPApplication;
import com.rtmap.wifipicker.util.DTFileUtils;
import com.rtmap.wifipicker.util.DTLog;
import com.rtmap.wifipicker.util.Utils;
import com.rtmap.wifipicker.widget.LoadDialog;

public abstract class WPBaseActivity extends Activity {
	private long mTime;
	public static final String UTF_8 = "UTF-8";

	protected LoadDialog mDialogLoad;
	public float adjustLength = 5;// 调整距离为1像素
	protected String mTag;
	/** 记录处于前台的Activity */
	private static WPBaseActivity mForegroundActivity = null;
	public String mUserName;

	private Handler handler = new Handler() {// 下载地图过程中下载进度消息
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Constants.RTMAP_MAP:
				int progress = msg.arg1;
				DTLog.i("SDK进度码" + progress);
				if (progress == Constants.MAP_LOAD_START) {// 开始加载
					DTLog.i("开始加载");
				} else if (progress == Constants.MAP_FailNetResult) {// 校验结果失败
					DTLog.i("校验结果：" + (String) msg.obj);
				} else if (progress == Constants.MAP_FailCheckNet) {// 联网检测失败
					DTLog.i("校验联网失败");
				} else if (progress == Constants.MAP_Down_Success) {
					DTLog.i("地图下载成功");
				} else if (progress == Constants.MAP_Down_Fail) {
					DTLog.i("地图下载失败");
				} else if (progress == Constants.MAP_LOAD_END) {
					DTLog.i("地图加载完成");
				}
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Handlerlist.getInstance().register(handler);
		adjustLength = Float.parseFloat(WPApplication.getInstance().getShare()
				.getString("step_adjust", "5"));
		mTime = System.currentTimeMillis();
		mTag = getClass().getSimpleName();
		mDialogLoad = new LoadDialog(this, R.style.style_xunlu_dialog);
		mUserName = WPApplication.getInstance().getShare()
				.getString(DTFileUtils.PREFS_USERNAME, "");
		super.onCreate(savedInstanceState);

	}

	@Override
	protected void onResume() {
		super.onResume();
		mForegroundActivity = this;
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	protected void setLeftPanelButtonVisibility(int visibility) {
		Button left = (Button) findViewById(R.id.button_left);
		if (left != null) {
			left.setVisibility(visibility);
		}
	}

	protected void setRightPanelButtonVisibility(int visibility) {
		Button right = (Button) findViewById(R.id.button_right);
		if (right != null) {
			right.setVisibility(visibility);
		}
	}

	protected void setLeftPanelButtonText(int resId) {
		Button left = (Button) findViewById(R.id.button_left);
		if (left != null) {
			left.setText(resId);
		}
	}

	protected void setRightPanelButtonText(int resId) {
		Button right = (Button) findViewById(R.id.button_right);
		if (right != null) {
			right.setText(resId);
		}
	}

	protected void setRightPanelButtonText(String str) {
		Button right = (Button) findViewById(R.id.button_right);
		if (right != null) {
			right.setText(str);
		}
	}

	protected void setLeftPanelButtonListener(View.OnClickListener listener) {
		Button left = (Button) findViewById(R.id.button_left);
		if (left != null) {
			left.setOnClickListener(listener);
		}
	}

	protected void setRightPanelButtonListener(View.OnClickListener listener) {
		Button right = (Button) findViewById(R.id.button_right);
		if (right != null) {
			right.setOnClickListener(listener);
		}
	}

	protected void setLeftPanelButtonBackground(int resId) {
		Button left = (Button) findViewById(R.id.button_left);
		if (left != null) {
			left.setBackgroundResource(resId);
		}
	}

	protected void setRightPanelButtonBackground(int resId) {
		Button right = (Button) findViewById(R.id.button_right);
		if (right != null) {
			right.setBackgroundResource(resId);
		}
	}

	protected void setRightPanelButtonEnabled(boolean enabled) {
		Button right = (Button) findViewById(R.id.button_right);
		if (right != null) {
			right.setEnabled(enabled);
		}
	}

	protected void setTitleText(int resId) {
		TextView title = (TextView) findViewById(R.id.text_title);
		if (title != null) {
			title.setText(resId);
		}
	}

	protected void setTitleText(String s) {
		TextView title = (TextView) findViewById(R.id.text_title);
		if (title != null) {
			title.setText(s);
		}
	}

	// protected void setOnNetworkErrorClick(View.OnClickListener listener) {
	// Button error = (Button) findViewById(R.id.button_net_error);
	// if(error != null) {
	// error.setOnClickListener(listener);
	// }
	// }

	protected void showToast(int resId, int duration) {
		Utils.showToast(this, resId, duration);
	}

	protected void showToast(String message, int duration) {
		Utils.showToast(this, message, duration);
	}

	protected boolean saveStringPrefs(String key, String value) {
		return WPApplication.getInstance().getShare().edit()
				.putString(key, value).commit();
	}

	protected String getStringPrefs(String key) {
		return WPApplication.getInstance().getShare().getString(key, null);
	}

	protected boolean saveBooleanPrefs(String key, boolean value) {
		return WPApplication.getInstance().getShare().edit()
				.putBoolean(key, value).commit();
	}

	protected boolean getBooleanPrefs(String key) {
		return WPApplication.getInstance().getShare().getBoolean(key, false);
	}

	protected void showLoad() {
		try {
			if (isFinishing()) {
				return;
			}

			mDialogLoad.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void hideLoad() {
		if (mDialogLoad != null && mDialogLoad.isShowing()) {
			mDialogLoad.dismiss();
		}
	}

	/** 获取当前处于前台的activity */
	public static WPBaseActivity getForegroundActivity() {
		return mForegroundActivity;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Handlerlist.getInstance().remove(handler);
	}
}
