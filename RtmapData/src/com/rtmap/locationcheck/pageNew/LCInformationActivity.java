package com.rtmap.locationcheck.pageNew;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.rtmap.checkpicker.R;
import com.rtmap.locationcheck.core.LCActivity;
import com.rtmap.locationcheck.core.LCApplication;
import com.rtmap.locationcheck.core.LCAsyncTask;
import com.rtmap.locationcheck.core.LCCallBack;
import com.rtmap.locationcheck.core.exception.LCException;
import com.rtmap.locationcheck.core.http.LCHttpClient;
import com.rtmap.locationcheck.core.http.LCHttpUrl;
import com.rtmap.locationcheck.core.model.InfoModel;
import com.rtmap.locationcheck.util.DTFileUtils;
import com.rtmap.locationcheck.util.DTUIUtils;

public class LCInformationActivity extends LCActivity implements
		OnClickListener {

	private TextView mUserName;
	private TextView mContent;
	private Spinner mDateSpinner;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lc_work_info);
		mUserName = (TextView) findViewById(R.id.user);
		mContent = (TextView) findViewById(R.id.content);
		findViewById(R.id.report).setOnClickListener(this);
		mUserName.setText("人员："
				+ LCApplication.getInstance().getShare()
						.getString(DTFileUtils.PREFS_USERNAME, ""));
		mDateSpinner = (Spinner) findViewById(R.id.date);
		final ArrayAdapter<String> titleAdapter = new ArrayAdapter<String>(
				this, R.layout.work_text, new String[] { "今天", "一天前", "两天前",
						"三天前", "四天前", "五天前", "六天前" });
		mDateSpinner.setAdapter(titleAdapter);
		mDateSpinner.setSelection(0, true); // 设置默认选中项，此处为默认选中第4个值
		mDateSpinner.setOnItemSelectedListener(this);
		new LCAsyncTask(new InfoCall()).run("0");
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View view, int position,
			long arg3) {
		mLoadDialog.show();
		new LCAsyncTask(new InfoCall()).run(position + "");
	}

	class InfoCall implements LCCallBack {

		@Override
		public Object onCallBackStart(Object... obj) {
			try {
				String str = LCHttpClient.post(String.format(
						LCHttpUrl.INFO_SElECT,
						LCApplication.getInstance().getShare()
								.getString(DTFileUtils.PREFS_TOKEN, ""),
						(String) obj[0]), null, null);
				return mGson.fromJson(str, InfoModel.class);
			} catch (LCException e) {
				e.printStackTrace();
				DTUIUtils.showToastSafe("连接超时");
			}
			return null;
		}

		@Override
		public void onCallBackFinish(Object obj) {
			mLoadDialog.cancel();
			if (obj != null) {
				InfoModel info = (InfoModel) obj;
				if (info.getResult() != null)
					mContent.setText(info.getResult().getOadaily() + "\n"
							+ info.getResult().getUserdaily());
			}
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.report:
			Intent intent = new Intent(this, LCReportActivity.class);
			startActivity(intent);
			break;
		}
	}
}
