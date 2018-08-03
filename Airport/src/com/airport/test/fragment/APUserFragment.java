package com.airport.test.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airport.test.R;
import com.airport.test.activity.APLoginActivity;
import com.airport.test.activity.APUserActivity;
import com.airport.test.activity.InputPlanActivity;
import com.airport.test.activity.LCScannerActivity;
import com.airport.test.activity.MsgActivity;
import com.airport.test.activity.MyPlanActivity;
import com.airport.test.adapter.UserFragAdapter;
import com.airport.test.util.view.DTCircleImage;
import com.dingtao.libs.DTApplication;
import com.rtm.common.utils.RMStringUtils;

public class APUserFragment extends Fragment implements OnClickListener,
		OnItemClickListener {

	private ListView mList;
	private RelativeLayout mAvatarLayout;
	private DTCircleImage mAvatar;
	private String mAccount;
	private TextView mLoginText;
	private LinearLayout mUserInfo;
	private UserFragAdapter mUserAdapter;
	private Bitmap mLoginBitmap,mLogoutBitmap;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_user, null);
		mList = (ListView) view.findViewById(R.id.user_list);
		mUserAdapter = new UserFragAdapter();
		mList.setAdapter(mUserAdapter);
		mAvatar = (DTCircleImage) view.findViewById(R.id.avatar);
		mLoginBitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.avatar);
		mLogoutBitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.touxiang);
		mAvatarLayout = (RelativeLayout) view.findViewById(R.id.avatar_layout);
		mAvatarLayout.setOnClickListener(this);
		mList.setOnItemClickListener(this);

		mLoginText = (TextView) view.findViewById(R.id.login_text);
		mUserInfo = (LinearLayout) view.findViewById(R.id.userinfo);
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		mAccount = DTApplication.getInstance().getShare()
				.getString("account", null);
		if (RMStringUtils.isEmpty(mAccount)) {
			mLoginText.setVisibility(View.VISIBLE);
			mUserInfo.setVisibility(View.GONE);
			mUserAdapter.setCount(2);
			mUserAdapter.notifyDataSetChanged();
			mAvatar.setImageBitmap(mLoginBitmap);
		} else {
			mLoginText.setVisibility(View.GONE);
			mUserInfo.setVisibility(View.VISIBLE);
			mUserAdapter.setCount(4);
			mUserAdapter.notifyDataSetChanged();
			mAvatar.setImageBitmap(mLogoutBitmap);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if (mUserAdapter.getCount() == 4) {
			if (arg2 < 2)
				arg2 += 2;
			else
				arg2 -= 2;
		}
		if (arg2 == 0) {
			Intent intent = new Intent(getActivity(), LCScannerActivity.class);
			startActivityForResult(intent, 1);
		} else if (arg2 == 1) {
			InputPlanActivity.interActivity(getActivity());
		} else if (arg2 == 2) {
			MyPlanActivity.interActivity(getActivity());
		} else if (arg2 == 3) {
			MsgActivity.interActivity(getActivity());
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.avatar_layout:
			if (RMStringUtils.isEmpty(mAccount)) {
				APLoginActivity.interActivity(getActivity());
			} else {
				APUserActivity.interActivity(getActivity());
			}
			break;
		}
	}

}
