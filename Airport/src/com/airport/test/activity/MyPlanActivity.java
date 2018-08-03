package com.airport.test.activity;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.airport.test.R;
import com.airport.test.core.AirSqlite;
import com.airport.test.model.AirData;
import com.airport.test.model.MsgData;
import com.dingtao.libs.DTActivity;
import com.google.gson.Gson;
import com.rtm.common.model.BuildInfo;
import com.rtm.common.model.RMLocation;
import com.rtm.location.LocationApp;
import com.rtm.location.utils.RMLocationListener;

public class MyPlanActivity extends DTActivity implements OnClickListener,
		RMLocationListener {

	private ImageView mMsg;
	private ImageView imgBack, msgSign;
	private BuildInfo mBuild;

	public static void interActivity(Context context) {
		Intent intent = new Intent(context, MyPlanActivity.class);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_plan);

		Gson gson = new Gson();
		mBuild = gson.fromJson(AirData.AIR_DATA, BuildInfo.class);

		mMsg = (ImageView) findViewById(R.id.msg);
		imgBack = (ImageView) findViewById(R.id.img_back);
		findViewById(R.id.start).setOnClickListener(this);
		imgBack.setOnClickListener(this);
		mMsg.setOnClickListener(this);
		msgSign = (ImageView) findViewById(R.id.mag_sign);
	}

	@Override
	protected void onResume() {
		super.onResume();
		ArrayList<MsgData> list = AirSqlite.getInstance().getMsgInfoList();
		for (MsgData data : list) {
			if (data.getGone() == 1) {
				msgSign.setVisibility(View.VISIBLE);
				break;
			} else {
				msgSign.setVisibility(View.GONE);
			}
		}
		LocationApp.getInstance().registerLocationListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		LocationApp.getInstance().unRegisterLocationListener(this);
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
		case R.id.msg:
			MsgActivity.interActivity(this);
			break;
		case R.id.start:
			finish();
			if (mLocation != null && mLocation.getError() == 0) {
//					&& mLocation.getBuildID().equals(mBuild.getBuildId())
				APMapActivity.interActivity(this);
			} else {
				OutMapActivity.interActivity(this);
			}
			break;
		default:
			break;
		}
	}

	private RMLocation mLocation;

	@Override
	public void onReceiveLocation(RMLocation result) {
		mLocation = result;
	}

}
