package com.airport.test.activity;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airport.test.R;
import com.airport.test.adapter.MsgAdapter;
import com.airport.test.core.AirSqlite;
import com.airport.test.model.MsgData;
import com.dingtao.libs.DTActivity;
import com.dingtao.libs.DTApplication;
import com.dingtao.libs.util.DTUIUtil;

public class MsgActivity extends DTActivity implements OnClickListener,
		OnItemClickListener {

	private RelativeLayout mDelete;
	private ListView mMsgList;
	private ImageView imgBack;
	private MsgAdapter mAdapter;

	public static void interActivity(Context context) {
		Intent intent = new Intent(context, MsgActivity.class);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_msg);

		mDelete = (RelativeLayout) findViewById(R.id.delete);
		mMsgList = (ListView) findViewById(R.id.msg_list);
		imgBack = (ImageView) findViewById(R.id.img_back);
		mAdapter = new MsgAdapter();
		mMsgList.setAdapter(mAdapter);
		ArrayList<MsgData> list = AirSqlite.getInstance().getMsgInfoList();
		for (MsgData data : list) {
			if (data.getGone() == 1) {
				mAdapter.addItemLast(data);
			}
		}
		if (mAdapter.getCount() == 0) {
			mDelete.setVisibility(View.GONE);
		} else {
			mDelete.setVisibility(View.VISIBLE);
		}
		mAdapter.notifyDataSetChanged();

		mMsgList.setOnItemClickListener(this);

		initDialog();
		imgBack.setOnClickListener(this);
		mDelete.setOnClickListener(this);
	}

	private Dialog mDialog;
	private TextView mSignText, mOK, mCancel;

	private void initDialog() {
		mDialog = new Dialog(this, R.style.dialog);
		mDialog.setContentView(R.layout.msg_dialog);
		mDialog.setCanceledOnTouchOutside(true);
		mSignText = (TextView) mDialog.findViewById(R.id.sign);
		mOK = (TextView) mDialog.findViewById(R.id.ok);
		mCancel = (TextView) mDialog.findViewById(R.id.cancel);

		mCancel.setOnClickListener(this);
		mOK.setOnClickListener(this);
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
		case R.id.delete:
			mAdapter.clearList();
			AirSqlite.getInstance().delete();
			mAdapter.notifyDataSetChanged();
			break;
		case R.id.ok:
			mDialog.cancel();
			finish();
			break;
		case R.id.cancel:
			mDialog.cancel();
			break;
		default:
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		if (position > 0)
			mDialog.show();
	}

}
