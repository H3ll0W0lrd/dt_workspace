package com.rtmap.wisdom.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.rtmap.wisdom.R;
import com.rtmap.wisdom.core.DTActivity;
import com.rtmap.wisdom.util.DTUIUtil;

/**
 * 我的页面
 * @author dingtao
 *
 */
public class WDMeActivity extends DTActivity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_me);
		findViewById(R.id.back).setOnClickListener(this);
		findViewById(R.id.me_build_like).setOnClickListener(this);
		findViewById(R.id.me_msg).setOnClickListener(this);
		findViewById(R.id.me_navigate_icon).setOnClickListener(this);
		findViewById(R.id.me_advice).setOnClickListener(this);
		findViewById(R.id.me_store).setOnClickListener(this);
		findViewById(R.id.me_update).setOnClickListener(this);
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
		case R.id.me_build_like:
			Intent intent = new Intent(this,
					WDBuildLikeActivity.class);
			startActivity(intent);
			break;
		case R.id.me_msg:

			break;
		case R.id.me_navigate_icon:
			Intent intent1 = new Intent(this,
					WDNaviIconActivity.class);
			startActivity(intent1);
			break;
		case R.id.me_advice:
			Intent intent2 = new Intent(this,
					WDAdviceActivity.class);
			startActivity(intent2);
			break;
		case R.id.me_store:
			try {
				Uri uri = Uri.parse("market://details?id=" + getPackageName());
				Intent intent3 = new Intent(Intent.ACTION_VIEW, uri);
				intent3.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent3);
			} catch (ActivityNotFoundException e) {
				DTUIUtil.showToastSafe("未安装应用市场");
			}
			break;
		case R.id.me_update:
			DTUIUtil.showToastSafe("进入修改页");
			break;
		}
	}
}
