package com.rtmap.locationcheck.page;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import com.rtm.common.model.RMLocation;
import com.rtm.location.LocationApp;
import com.rtm.location.logic.GatherData;
import com.rtm.location.utils.RMLocationListener;
import com.rtmap.checkpicker.R;
import com.rtmap.locationcheck.adapter.LCBeaconAdapter;
import com.rtmap.locationcheck.core.LCActivity;

public class LCBeaconLIstActivity extends LCActivity implements
		OnItemSelectedListener,RMLocationListener {

	private Spinner mUuidSpinner;
	private EditText mMac;
	private ListView mList;
	private LCBeaconAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lc_beacon_list);
		mUuidSpinner = (Spinner) findViewById(R.id.uuid_spinner);
		mMac = (EditText) findViewById(R.id.mac_edit);
		mList = (ListView) findViewById(R.id.list);

		final ArrayAdapter<String> titleAdapter = new ArrayAdapter<String>(
				this, R.layout.work_text, new String[] { "全部", "C91A",
						"FDA5", "C91B", "ABD3" });
		mUuidSpinner.setAdapter(titleAdapter);
		mUuidSpinner.setSelection(0, true); // 设置默认选中项，此处为默认选中第4个值
		mUuidSpinner.setOnItemSelectedListener(this);
		mMac.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				mAdapter.setSelect(s.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				
			}
		});

		mAdapter = new LCBeaconAdapter();
		mList.setAdapter(mAdapter);
		mList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				mAdapter.setVisibility(mAdapter.getItem(arg2).mac);
				mAdapter.notifyDataSetChanged();
			}
		});
		LocationApp.getInstance().init(getApplicationContext());
	}
	
	@Override
	public void onBackPressed() {
		if(getIntent().getIntExtra("sign", 0)==2){
			Intent intent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putSerializable("list", mAdapter.getMap());
			intent.putExtras(bundle);
			setResult(RESULT_OK, intent);
		}
		super.onBackPressed();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		LocationApp.getInstance().registerLocationListener(this);
		LocationApp.getInstance().start();
	}
	@Override
	protected void onPause() {
		super.onPause();
		LocationApp.getInstance().stop();
		LocationApp.getInstance().unRegisterLocationListener(this);
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View view, int position,
			long arg3) {
		mAdapter.setSelect(position);
	}

	@Override
	public void onReceiveLocation(RMLocation location) {
		mAdapter.addList(GatherData.getInstance().getBeaconEntity());
		mAdapter.notifyDataSetChanged();
	}
}
