package com.rtmap.locationdemo;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.rtm.common.utils.RMLog;
import com.rtm.location.LocationApp;
import com.rtmap.locationdemo.beta.R;
import com.rtmap.locationdemo.draw.DrawIconAnimatorMapActivity;

public class MainActivity extends Activity implements OnItemClickListener {
	private ListView mList;
	private int count;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		TextView text = (TextView) findViewById(R.id.version);
		RMLog.LOG_LEVEL = RMLog.LOG_LEVEL_DEBUG;
		// 版本号
		PackageManager manager = this.getPackageManager();
		PackageInfo info;
		try {
			info = manager.getPackageInfo(this.getPackageName(), 0);
			text.setText("应用版本：" + info.versionName);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		mList = (ListView) findViewById(R.id.main_list);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, getResources()
						.getStringArray(R.array.main_list));
		mList.setAdapter(adapter);
		mList.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		Intent intent = null;
		switch (position) {
		case 0:
			intent = new Intent(this, LocationActivity.class);
			startActivity(intent);
			break;
		case 1:
			intent = new Intent(this, MapActivity.class);
			startActivity(intent);
			break;
		case 2:
			intent = new Intent(this, LocMapActivity.class);
			startActivity(intent);
			break;
		case 3:
			intent = new Intent(this, BuildListActivity.class);
			startActivity(intent);
			break;
		case 4:
			intent = new Intent(this, HtmlLocMapActivity.class);
			startActivity(intent);
			break;
		case 5:
			intent = new Intent(this, MapColorActivity.class);
			startActivity(intent);
			break;
		case 6:
			intent = new Intent(this, DrawIconAnimatorMapActivity.class);
			startActivity(intent);
			break;
		case 7:
			intent = new Intent(this, MapLocationPoiActivity.class);
			startActivity(intent);
			break;
		case 8:
			intent = new Intent(this, MapPoiCenterActivity.class);
			startActivity(intent);
			break;
		case 9:
			intent = new Intent(this, MapLongClickActivity.class);
			startActivity(intent);
			break;
		case 10:
			intent = new Intent(this, NavigationActivity.class);
			startActivity(intent);
			break;
		case 11:
			intent = new Intent(this, TextNavigationActivity.class);
			startActivity(intent);
			break;
		}
	}
}
