package com.rtm.frm.fragment.mine;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.rtm.frm.R;
import com.rtm.frm.database.Builds;
import com.rtm.frm.database.DBOperation;
import com.rtm.frm.database.Floors;
import com.rtm.frm.dialogfragment.BaseDialogFragment;
import com.rtm.frm.dialogfragment.LoadingFragment;
import com.rtm.frm.fragment.controller.MyFragmentManager;
import com.rtm.frm.net.PostData;
import com.rtm.frm.thread.InitBuildsThread;
import com.rtm.frm.utils.ConstantsUtil;
import com.rtm.frm.utils.PreferencesUtil;
import com.rtm.frm.utils.ToastUtil;
import com.rtm.frm.utils.XunluUtil;

@SuppressLint("HandlerLeak")
public class LoginDialogFragment extends BaseDialogFragment implements
		View.OnClickListener {
	private EditText editUser;
	private EditText editPassword;
	private ImageButton close;
	private Button login;
	private LoadingFragment load;
	private String user;
	private String password;
	
	public LoginDialogFragment() {
		this.setStyle(DialogFragment.STYLE_NORMAL, R.style.dialogfragment_transparent_bg);
	}

	@Override
	public View onCreateView(LayoutInflater mLayoutInflater,
			ViewGroup mViewGroup, Bundle b) {
		super.onCreateView(mLayoutInflater, mViewGroup, b);
		View contentView = mLayoutInflater.inflate(R.layout.fragment_login,
				mViewGroup, false);
		initView(contentView);
		return contentView;
	}

	private void initView(View contentView) {
		editUser = (EditText) contentView.findViewById(R.id.edit_user);
		editPassword = (EditText) contentView.findViewById(R.id.edit_password);

		close = (ImageButton) contentView.findViewById(R.id.button_close);
		close.setOnClickListener(this);

		load = new LoadingFragment(R.string.loading_login);

		login = (Button) contentView.findViewById(R.id.button_login_ok);
		login.setOnClickListener(this);
	}

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case ConstantsUtil.HANDLER_POST_LOGIN:
				// 关闭进度条
				load.dismiss();
				dealResult((String) msg.obj);
				break;

			default:
				break;
			}
		}

		private void dealResult(String result) {
			// 判断是否登录成功
			if (result.contains("<error>0</error>")) {
				// 启动批量插入线程
				InitBuildsThread thread = new InitBuildsThread(result,
						mHandler, ConstantsUtil.HANDLER_THREAD_INIT_OK,
						Builds.TABLE_NAME, Floors.TABLE_NAME,true);
				thread.start();

				MineFragment.isLogin = true;
				PreferencesUtil.putString(ConstantsUtil.PREFS_USER, user);
				PreferencesUtil.putString(ConstantsUtil.PREFS_PASSWORD, password);
				ToastUtil.shortToast(R.string.message_login_succ);
			} else {
				ToastUtil.shortToast(R.string.message_wrong_password);
			}
			// 回传数据给启动页面
			MineFragment me = (MineFragment) MyFragmentManager
					.getFragmentByFlag(
							MyFragmentManager.PRCOCESS_DIALOGFRAGMENT_MINE,
							MyFragmentManager.DIALOGFRAGMENT_MINE);
			if(me != null) {
				me.onFinish(null, null);
			}
			dismiss();
		}
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_close:
			dismiss();
			break;
		case R.id.button_login_ok:
			user = editUser.getText().toString();
			password = editPassword.getText().toString();
			if (XunluUtil.isEmpty(user) || XunluUtil.isEmpty(password)) {
				ToastUtil.shortToast(R.string.message_fill_user_password);
				return;
			}
			// 登录
			if (!DBOperation.getInstance().isHaveLocalPrivateBuildsData()) {
				// 显示等待进度条
				MyFragmentManager.showFragmentdialog(load,
						MyFragmentManager.PROCESS_DIALOGFRAGEMENT_LOADING,
						MyFragmentManager.DIALOGFRAGMENT_LOADING);
				// 开始登陆
//				PostData.postLogin(mHandler, ConstantsUtil.HANDLER_POST_LOGIN,
//						"zhiht", "111111");
				PostData.postLogin(mHandler, ConstantsUtil.HANDLER_POST_LOGIN,
						user, password);
			}
			break;
		default:
			break;
		}
	}
}
