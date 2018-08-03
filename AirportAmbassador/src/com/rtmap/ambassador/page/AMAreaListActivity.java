package com.rtmap.ambassador.page;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.rtmap.ambassador.R;
import com.rtmap.ambassador.adapter.AMAreaListAdapter;
import com.rtmap.ambassador.core.DTActivity;
import com.rtmap.ambassador.core.DTApplication;
import com.rtmap.ambassador.core.DTAsyncTask;
import com.rtmap.ambassador.core.DTCallBack;
import com.rtmap.ambassador.http.DTHttpUrl;
import com.rtmap.ambassador.http.DTHttpUtil;
import com.rtmap.ambassador.model.Area;
import com.rtmap.ambassador.model.AreaList;
import com.rtmap.ambassador.util.DTStringUtil;
import com.rtmap.ambassador.util.DTUIUtil;

public class AMAreaListActivity extends DTActivity implements
		OnItemClickListener,android.view.View.OnClickListener {

	private ListView mAreaList;
	private AMAreaListAdapter mAreaAdapter;
	private TextView mTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.am_area_list);

		mAreaList = (ListView) findViewById(R.id.list);
		mAreaAdapter = new AMAreaListAdapter();
		mAreaList.setAdapter(mAreaAdapter);
		mTitle = (TextView) findViewById(R.id.title);
		findViewById(R.id.back).setOnClickListener(this);

		String result = DTApplication.getInstance().getShare()
				.getString(DTStringUtil.PREFS_AREA_LIST, null);
		if (result != null) {
			AreaList list = mGson.fromJson(result, AreaList.class);
			mAreaAdapter.addList(list.getRst().getAreaList());
			mAreaAdapter.notifyDataSetChanged();
		}
		mAreaList.setOnItemClickListener(this);

		String w = DTApplication.getInstance().getShare()
				.getString(DTStringUtil.PREFS_AREA, null);
		Area area = mGson.fromJson(w, Area.class);
		mTitle.setText("正在执勤" + area.getAreaCode());
	}

	@Override
	public String getPageName() {
		return null;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		final Area area = mAreaAdapter.getItem(position);

		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setPositiveButton("确定", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				new DTAsyncTask(new DTCallBack() {

					@Override
					public Object onCallBackStart(Object... obj) {
						String result = DTHttpUtil.connInfo(
								DTHttpUtil.POST,
								DTHttpUrl.CHANGE_AREA,
								new String[] { "staffId", "staffName",
										"staffCode", "areaCode", "deviceId",
										"clientTime" },
								new Object[] { mUser.getId(),
										mUser.getStaffName(),
										mUser.getStaffCode(),
										area.getAreaCode(), DTApplication.MAC,
										System.currentTimeMillis() });
						if (result != null) {
							try {
								JSONObject o = new JSONObject(result);
								if (o.getInt("code") == 0) {
									return true;
								} else {
									DTUIUtil.showToastSafe(o.getString("msg"));
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
						return false;
					}

					@Override
					public void onCallBackFinish(Object obj) {
						if ((Boolean) obj) {
							DTApplication
									.getInstance()
									.getShare()
									.edit()
									.putString(DTStringUtil.PREFS_AREA,
											mGson.toJson(area)).commit();
							mTitle.setText("正在执勤" + area.getAreaCode());
							finish();
						}
					}
				}).run();
			}
		});
		dialog.setNegativeButton("取消", null);
		dialog.setMessage("确定选择工作区域为：\n" + area.getAreaCode());
		dialog.create().show();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			break;

		default:
			break;
		}
	}
}
