package demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.location.R;
import com.rtm.common.model.RMLocation;
import com.rtm.common.utils.RMStringUtils;
import com.rtm.location.LocationApp;
import com.rtm.location.entity.RMUser;
import com.rtm.location.utils.RMLocationListener;
import com.rtm.location.utils.RMSqlite;

/**
 * 配置定位参数
 * 
 * @author dingtao
 *
 */
public class LocationParamsActivity extends Activity implements OnClickListener {

	private TextView mTimeText;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.location_params);
		mTimeText = (TextView) findViewById(R.id.time_text);
		/**
		 * 得到请求间隔时间
		 */
		mTimeText.setText(LocationApp.getInstance().getRequestSpanTime()+"");
		findViewById(R.id.ok).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ok:
			/**
			 * 设置请求间隔时间，默认是1000ms
			 */
			LocationApp.getInstance().setRequestSpanTime(
					Integer.parseInt(mTimeText.getText().toString()));
			Intent intent = new Intent(this, LocationActivity.class);
			startActivity(intent);
			break;
		}
	}
}
