package com.rtmap.driver.util;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.rtmap.driver.App;
import com.rtmap.driver.R;

/**
 * Toast相关
 * */
public class T {

	/**
	 * 短提示
	 * 
	 * @param msg
	 */
	public static void s(String msg) {
		Toast.makeText(App.getInstance(), msg, Toast.LENGTH_SHORT).show();
	}

	/**
	 * 自定义的短提示
	 * 
	 * @param msg
	 * @param v
	 */
	public static void sCustom(String msg) {
		if (TextUtils.isEmpty(msg)) {
			return;
		}

		View v = View.inflate(App.getInstance(), R.layout.toast, null);
		TextView textViewMessage = (TextView) v.findViewById(R.id.toast_msg);
		textViewMessage.setText(msg);

		toastWithByView(v);
	}

	/**
	 * 通过View显示toast
	 * 
	 * @param v
	 */
	public static void toastWithByView(View v) {
		Toast toast = new Toast(App.getInstance());
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setView(v);
		toast.show();
	}
}
