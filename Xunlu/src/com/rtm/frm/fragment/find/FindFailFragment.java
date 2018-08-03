package com.rtm.frm.fragment.find;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rtm.frm.R;
import com.rtm.frm.fragment.BaseFragment;
import com.rtm.frm.fragment.controller.MyFragmentManager;

@SuppressLint({ "ValidFragment", "NewApi" }) 
public class FindFailFragment extends BaseFragment implements
		View.OnClickListener ,OnTouchListener{
	View contentView;
	RelativeLayout relSuccBg;
	ImageView imgBaoXiang;
	TextView tvTitle;
	Button btnKeep;
	Button btnQuit;
	
	public static boolean isShowing = false;
	boolean isSucc = false;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		contentView = View.inflate(mContext,R.layout.fragment_fail, null);
		contentView.setOnTouchListener(this);

		initView(contentView);
		return contentView;
	}

	private void initView(View v) {
		relSuccBg = (RelativeLayout)v.findViewById(R.id.succ_rel_bg);
		btnKeep = (Button) v.findViewById(R.id.fail_keep);
		btnKeep.setOnClickListener(this);
		btnQuit = (Button) v.findViewById(R.id.fail_quit);
		btnQuit.setOnClickListener(this);
		
		imgBaoXiang = (ImageView)v.findViewById(R.id.succ_img_baoxiang);
		tvTitle = (TextView)v.findViewById(R.id.succ_title);
	}
	Bundle b = new Bundle();
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.fail_keep:
			b.putBoolean("isKeepSucc", true);
			MyFragmentManager.getInstance().backFragment();
			break;
		case R.id.fail_quit:
			b.putBoolean("isCloseAll", true);
			MyFragmentManager.getInstance().backFragment();
			break;
		default:
			break;
		}
	}

	@Override
	public void onDestroy() {
		b.putBoolean("isKeepSucc", true);
		callOnFinish(b);
		
		super.onDestroy();
		
		isShowing = false;
	}

	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		return true;
	}
}
