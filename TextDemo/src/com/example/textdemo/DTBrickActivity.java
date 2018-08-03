package com.example.textdemo;

import android.os.Bundle;
import android.widget.TextView;

import com.dingtao.libs.DTActivity;
import com.example.textdemo.view.DTBrickView;
import com.example.textdemo.view.DTBrickView.OnCrashBrickListener;

public class DTBrickActivity extends DTActivity implements OnCrashBrickListener {
	private DTBrickView mBrickView;
	private TextView mStatus;
	private int count;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.brickz_layout);
		mBrickView = (DTBrickView) findViewById(R.id.brick);
		mBrickView.setOnCrashBrickListener(this);
		mStatus = (TextView) findViewById(R.id.status);
	}

	@Override
	public String getPageName() {
		return null;
	}

	@Override
	public void onCrashBrick() {
		count++;
		mStatus.setText("共击落："+count);
	}

}
