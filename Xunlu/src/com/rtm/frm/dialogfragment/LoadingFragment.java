package com.rtm.frm.dialogfragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rtm.frm.R;
import com.rtm.frm.XunluApplication;
import com.rtm.frm.dialogfragment.BaseDialogFragment;

@SuppressLint({ "NewApi", "InflateParams", "ValidFragment" })
public class LoadingFragment extends BaseDialogFragment {

	private View layout;

	private TextView mMsgTextView;

	private String msgString;
	
	//kunge.hu此处必须要有一个没有参数的构造函数，不然会出错
	public LoadingFragment(){}
	

	public LoadingFragment(int msg) {
		this.setStyle(DialogFragment.STYLE_NORMAL, R.style.dialogfragment_transparent_bg);
		msgString = XunluApplication.mApp.getResources().getString(msg);
	}
	
	public LoadingFragment(String msg) {
		msgString = msg;
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		layout = inflater.inflate(R.layout.fragment_loading, container, false);
		mMsgTextView = (TextView) layout.findViewById(R.id.loading_text);

		return layout;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mMsgTextView.setText(msgString);
	}

}
