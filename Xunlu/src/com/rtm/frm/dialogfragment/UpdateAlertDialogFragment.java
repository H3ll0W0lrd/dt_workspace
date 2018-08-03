package com.rtm.frm.dialogfragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rtm.frm.R;
import com.rtm.frm.XunluApplication;

/** 
 * ClassName: AlertDialogFragment 
 * date: 2014-9-8 上午10:04:04 
 * 
 * @author liyan 
 * @explain 提示框模板  
 */  
@SuppressLint("InflateParams")
public class UpdateAlertDialogFragment extends BaseDialogFragment {
	
	private TextView mMsgView;
	
	private TextView mTitleView;
	
	private TextView mConfirmButton;

	private TextView mCancelButton;
	
	private String mMessage;
	
	private OnClickListener mConfirmListener;
	
	private OnClickListener mCancelListener;
	
	private View contentView;
	
	public UpdateAlertDialogFragment() {
	}
	
	public UpdateAlertDialogFragment(String title,String message) {
		this.setStyle(DialogFragment.STYLE_NORMAL, R.style.dialogfragment_transparent_bg);
		mMessage = message;
		contentView = LayoutInflater.from(XunluApplication.mApp).inflate(R.layout.fragment_dialog_update_alert, null);
		mConfirmButton = (TextView)contentView.findViewById(R.id.ok);
		mCancelButton = (TextView)contentView.findViewById(R.id.cancel);
		mMsgView = (TextView)contentView.findViewById(R.id.msg);
		mTitleView = (TextView)contentView.findViewById(R.id.title);
		mMsgView.setText(mMessage);
		mTitleView.setText(title);
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
	
	public void setConfirmText(String confirm) {
		mConfirmButton.setText(confirm);
	}
	
	public void setCancelText(String cancel) {
		mCancelButton.setText(cancel);
	}
}
