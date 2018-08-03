package com.rtmap.wisdom.activity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.rtmap.wisdom.R;
import com.rtmap.wisdom.core.DTActivity;

/**
 * 意见反馈
 * @author dingtao
 *
 */
public class WDAdviceActivity extends DTActivity implements OnClickListener{
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_advice);
		
		findViewById(R.id.back).setOnClickListener(this);
	}

	@Override
	public String getPageName() {
		return null;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			break;

		default:
			break;
		}
	}
}
