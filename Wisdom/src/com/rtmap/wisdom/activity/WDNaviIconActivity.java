package com.rtmap.wisdom.activity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import com.rtmap.wisdom.R;
import com.rtmap.wisdom.core.DTActivity;

/**
 * 导航图标
 * @author dingtao
 *
 */
public class WDNaviIconActivity extends DTActivity implements OnClickListener {

	private LinearLayout mLinear1, mLinear2, mLinear3;
	private RadioButton mRadio1, mRadio2, mRadio3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_navigate_icon);
		mLinear1 = (LinearLayout) findViewById(R.id.linearLayout1);
		mLinear2 = (LinearLayout) findViewById(R.id.linearLayout2);
		mLinear3 = (LinearLayout) findViewById(R.id.linearLayout3);
		mRadio1 = (RadioButton) findViewById(R.id.radio0);
		mRadio1.setOnClickListener(this);
		mRadio2 = (RadioButton) findViewById(R.id.radio1);
		mRadio2.setOnClickListener(this);
		mRadio3 = (RadioButton) findViewById(R.id.radio2);
		mRadio3.setOnClickListener(this);

		findViewById(R.id.back).setOnClickListener(this);

		int sign = mShare.getInt("navigate_icon", 0);
		if (sign == 0) {
			mRadio1.setChecked(true);
			mLinear1.setBackgroundResource(R.drawable.me_avatar_bg);
		} else if (sign == 1) {
			mRadio2.setChecked(true);
			mLinear2.setBackgroundResource(R.drawable.me_avatar_bg);
		} else if (sign == 2) {
			mRadio3.setChecked(true);
			mLinear3.setBackgroundResource(R.drawable.me_avatar_bg);
		}
	}

	@Override
	public String getPageName() {
		return null;
	}

	@Override
	public void onClick(View v) {
		mLinear1.setBackgroundResource(R.drawable.dt_trans);
		mLinear2.setBackgroundResource(R.drawable.dt_trans);
		mLinear3.setBackgroundResource(R.drawable.dt_trans);
		mRadio1.setChecked(false);
		mRadio2.setChecked(false);
		mRadio3.setChecked(false);
		switch (v.getId()) {
		case R.id.radio0:
			mRadio1.setChecked(true);
			mShare.edit().putInt("navigate_icon", 0).commit();
			mLinear1.setBackgroundResource(R.drawable.me_avatar_bg);
			break;
		case R.id.radio1:
			mRadio2.setChecked(true);
			mShare.edit().putInt("navigate_icon", 1).commit();
			mLinear2.setBackgroundResource(R.drawable.me_avatar_bg);
			break;
		case R.id.radio2:
			mRadio3.setChecked(true);
			mShare.edit().putInt("navigate_icon", 2).commit();
			mLinear3.setBackgroundResource(R.drawable.me_avatar_bg);
			break;
		case R.id.back:
			finish();
			break;
		}
	}
}
