package com.rtm.frm.dialogfragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.rtm.frm.fragment.BaseFragment.OnFinishListener;

/**
 * @author hukunge
 * @date 2014.08.19 14:19
 */
public abstract class BaseDialogFragment extends DialogFragment {

	private static String FLAG = "flag";
	protected Context mContext;

	/**
	 * 获取Fragment的标记字符串,每个Fragment都是唯一的。 暂时不用。
	 */
	public String getFlagStr() {
		return FLAG;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 透明
//		 setStyle(DialogFragment.STYLE_NORMAL,
//		 R.style.dialogfragment_transparent_bg);
		mContext = getActivity().getApplicationContext();

		Bundle args = getArguments();
		if (args != null) {
			FLAG = args.getString(FLAG);
		}
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public void onFragmentResult(int requestCode, int resultCode,
			final Intent data) {
	}

	public OnFinishListener mFinishListener;

	/** 设置监听回调 */
	public void setOnFinishListener(OnFinishListener l) {
		mFinishListener = l;
	}

	/**
	 * 供子类调用，用以回传数据
	 * 
	 * @param data
	 *            要回传的数据包
	 */
	protected void callOnFinish(Bundle data) {
		if (mFinishListener != null) {
			mFinishListener.onFinish(getFlagStr(), data);
		}
	}

}
