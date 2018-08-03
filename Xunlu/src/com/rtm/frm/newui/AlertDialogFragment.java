package com.rtm.frm.newui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.rtm.frm.R;
import com.rtm.frm.XunluApplication;
import com.rtm.frm.dialogfragment.BaseDialogFragment;

/** 
 * ClassName: AlertDialogFragment 
 * date: 2014-9-8 上午10:04:04 
 * 
 * @author liyan 
 * @explain 提示框模板  
 */  
@SuppressLint("InflateParams")
public class AlertDialogFragment extends BaseDialogFragment {
	
	private TextView mMsgView;
	
	private Button mConfirmButton;

	private Button mCancelButton;
	
	private String mMessage;
	
	private OnClickListener mConfirmListener;
	
	private OnClickListener mCancelListener;
	
	private View contentView;
	
	public AlertDialogFragment(String message) {
		this.setStyle(DialogFragment.STYLE_NORMAL, R.style.dialogfragment_transparent_bg);
		mMessage = message;
		contentView = LayoutInflater.from(XunluApplication.mApp).inflate(R.layout.fragment_alert, null);
		mConfirmButton = (Button)contentView.findViewById(R.id.ok);
		mCancelButton = (Button)contentView.findViewById(R.id.cancel);
		mMsgView = (TextView)contentView.findViewById(R.id.msg);
		mMsgView.setText(mMessage);
		mConfirmButton.setOnClickListener(mConfirmListener);
		mCancelButton.setOnClickListener(mCancelListener);
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return contentView;
	}
	
	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}
	
	public void setConfirmOnClickListener(OnClickListener listener) {
		mConfirmButton.setOnClickListener(listener);
	}
	public void setCancelOnClickListener(OnClickListener listener) {
		mCancelButton.setOnClickListener(listener);
	}
}
