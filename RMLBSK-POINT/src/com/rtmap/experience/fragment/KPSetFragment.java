package com.rtmap.experience.fragment;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.google.gson.Gson;
import com.rtmap.experience.R;
import com.rtmap.experience.core.KPApplication;
import com.rtmap.experience.core.KPBaseFragment;
import com.rtmap.experience.page.KPLoginActivity;
import com.rtmap.experience.util.DTFileUtils;
import com.rtmap.experience.util.DTUIUtils;

public class KPSetFragment extends KPBaseFragment implements OnClickListener {

	private Gson mGson;

	@Override
	protected View createLoadedView() {
		View view = DTUIUtils.inflate(R.layout.main_set);
		mGson = new Gson();
		view.findViewById(R.id.exit).setOnClickListener(this);
		((TextView) view.findViewById(R.id.account)).setText("账号："
				+ mUser.getPhone());
		view.findViewById(R.id.message).setOnClickListener(this);
		view.findViewById(R.id.about).setOnClickListener(this);
		return view;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.exit:
			KPApplication.getInstance().getShare().edit()
					.putString(DTFileUtils.PREFS_TOKEN, "").commit();
			KPLoginActivity.interActivity(getActivity());
			break;
		}
	}
}
