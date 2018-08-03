package com.airport.test.activity;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RelativeLayout;

import com.airport.test.R;
import com.airport.test.adapter.UserOneAdapter;
import com.airport.test.adapter.UserTwoAdapter;
import com.airport.test.core.APActivity;
import com.airport.test.core.AirSqlite;
import com.airport.test.model.CateData;
import com.airport.test.util.view.DTCircleImage;
import com.dingtao.libs.DTApplication;

public class APUserActivity extends APActivity implements OnClickListener,
		OnItemClickListener {

	private Button mOK;
	private GridView mGridOne, mGridTwo;
	private RelativeLayout mLayoutOne, mLayoutTwo;
	private RelativeLayout mUpdateUser;// 完善资料
	private UserOneAdapter mOneAdapter;
	private UserTwoAdapter mTwoAdapter;
	private DTCircleImage mAvatar;

	public static void interActivity(Context context) {
		Intent intent = new Intent(context, APUserActivity.class);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user);
		findViewById(R.id.zhuxiao).setOnClickListener(this);
		findViewById(R.id.img_back).setOnClickListener(this);

		mAvatar = (DTCircleImage) findViewById(R.id.image);

		mOK = (Button) findViewById(R.id.wancheng);
		mGridOne = (GridView) findViewById(R.id.user_grid_one);
		mGridTwo = (GridView) findViewById(R.id.user_grid_two);
		mLayoutOne = (RelativeLayout) findViewById(R.id.user_info_one);
		mLayoutTwo = (RelativeLayout) findViewById(R.id.user_info_two);
		mUpdateUser = (RelativeLayout) findViewById(R.id.ziliao);

		mOneAdapter = new UserOneAdapter();
		mTwoAdapter = new UserTwoAdapter();
		ArrayList<CateData> list = AirSqlite.getInstance().getcatetbList();
		mTwoAdapter.addList(list);
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).isCheck())
				mOneAdapter.addItemLast(list.get(i));
		}
		mGridOne.setAdapter(mOneAdapter);
		mGridTwo.setAdapter(mTwoAdapter);
		mGridTwo.setOnItemClickListener(this);

		mOK.setOnClickListener(this);
		mUpdateUser.setOnClickListener(this);
		mAvatar.setImageBitmap(BitmapFactory.decodeResource(getResources(),
				R.drawable.touxiang));
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.zhuxiao:
			DTApplication.getInstance().getShare().edit()
					.putString("account", null).commit();
			finish();
			break;
		case R.id.ziliao:
			mLayoutOne.setVisibility(View.GONE);
			mLayoutTwo.setVisibility(View.VISIBLE);

			break;
		case R.id.wancheng:
			mLayoutOne.setVisibility(View.VISIBLE);
			mLayoutTwo.setVisibility(View.GONE);
			AirSqlite.getInstance().updatecatetb(mTwoAdapter.getList());
			ArrayList<CateData> list = AirSqlite.getInstance().getcatetbList();
			mOneAdapter.clearList();
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).isCheck())
					mOneAdapter.addItemLast(list.get(i));
			}
			mOneAdapter.notifyDataSetChanged();
			break;
		case R.id.img_back:
			finish();
			break;
		default:
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// switch (arg1.getId()) {
		// case R.id.user_grid_two:
		boolean e = mTwoAdapter.getItem(arg2).isCheck();
		mTwoAdapter.getItem(arg2).setCheck(!e);
		mTwoAdapter.notifyDataSetChanged();
		// break;
		// }
	}

}
