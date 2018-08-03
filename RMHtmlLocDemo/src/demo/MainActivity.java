package demo;

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
import com.rtm.location.utils.RMVersionLocation;
import com.example.location.R;

public class MainActivity extends Activity implements OnItemClickListener {
	private ListView mList;
	private int count;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		TextView text = (TextView) findViewById(R.id.version);
//		RMLog.LOG_LEVEL = RMLog.LOG_LEVEL_DEBUG;
		// 版本号
		text.setText("SDK版本："+RMVersionLocation.VERSION);
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
			intent = new Intent(this, LocationParamsActivity.class);
			startActivity(intent);
			break;
		case 2:
			intent = new Intent(this, HtmlLocMapActivity.class);
			startActivity(intent);
			break;
		case 3:
			intent = new Intent(this, FAQActivity.class);
			startActivity(intent);
			break;
		}
	}

}
