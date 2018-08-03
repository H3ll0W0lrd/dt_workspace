package com.rtmap.locationcheck.pageNew;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
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
import com.rtmap.locationcheck.util.DTStringUtils;
import com.rtmap.locationcheck.util.DTUIUtils;

public class LCReportActivity extends LCActivity implements OnClickListener {

	private TextView mTitle;
	private TextView mDate;
	private EditText mContent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lc_report);
		mTitle = (TextView) findViewById(R.id.title);
		mDate = (TextView) findViewById(R.id.date);
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd");
		mDate.setText(format.format(new Date(System.currentTimeMillis())));
		mContent = (EditText) findViewById(R.id.content);
		findViewById(R.id.ok).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ok:
			String pas = mContent.getText().toString();
			if (DTStringUtils.isEmpty(pas)) {
				DTUIUtils.showToastSafe(R.string.input_account);
				return;
			}
			mLoadDialog.show();
			new LCAsyncTask(new InfoCall()).run(pas);
			break;
		}
	}
	
	class InfoCall implements LCCallBack {

		@Override
		public Object onCallBackStart(Object... obj) {
			try {
				String str = LCHttpClient.post(String.format(
						LCHttpUrl.INFO_UPLOAD,
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
				if(info.getStatus().equals("1")){
					DTUIUtils.showToastSafe("汇报成功");
					finish();
				}else{
					DTUIUtils.showToastSafe(info.getMessage());
				}
			}
		}

	}
}
