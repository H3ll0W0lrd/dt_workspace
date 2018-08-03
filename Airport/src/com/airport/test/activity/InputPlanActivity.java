package com.airport.test.activity;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.airport.test.R;
import com.airport.test.adapter.MsgAdapter;
import com.airport.test.adapter.SearchAdapter;
import com.airport.test.model.AirData;
import com.baidu.mapapi.model.LatLng;
import com.dingtao.libs.DTActivity;
import com.dingtao.libs.DTApplication;
import com.dingtao.libs.util.DTStringUtil;
import com.dingtao.libs.util.DTUIUtil;
import com.google.gson.Gson;
import com.rtm.common.model.BuildInfo;

public class InputPlanActivity extends DTActivity implements OnClickListener {

	private ImageView imgBack;
	private TextView mDate;// 日期

	public static void interActivity(Context context) {
		Intent intent = new Intent(context, InputPlanActivity.class);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.input_plan);
		mDate = (TextView) findViewById(R.id.date);
		imgBack = (ImageView) findViewById(R.id.img_back);
		findViewById(R.id.start).setOnClickListener(this);
		imgBack.setOnClickListener(this);
		mDate.setOnClickListener(this);
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		mDate.setText(format.format(date));
	}

	@Override
	public String getPageName() {
		return null;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.img_back:
			finish();
			break;
		case R.id.start:
			MyPlanActivity.interActivity(this);
			finish();
			break;
		case R.id.date:
			Intent intent = new Intent(this, DateActivity.class);
			startActivityForResult(intent, 100);
			break;
		default:
			break;
		}
	}

	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		super.onActivityResult(arg0, arg1, arg2);
		if (arg0 == 100 && arg1 == Activity.RESULT_OK) {
			mDate.setText(arg2.getStringExtra("date"));
		}
	}

}
