package demo;

import android.app.Activity;
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
 * 定位整个过程需要三步，全程基本只需拷贝： 1.Libs库(so库是必须的，还有rtmap_lbs_location_v*.
 * jar和rtmap_lbs_common_v*.jar包)；
 * 2.配置(AndroidManifest.xml中智慧图服务请完全拷贝到你应用中，记得替换key哦
 * )；3.代码(LocationApp为定位类，采用单例模式
 * ，调用方法快捷高效，init()、start()、stop()除了这三个，别忘了注册和取消回调哦registerLocationListener
 * ()unRegisterLocationListener())
 * 
 * 更多方法请查看智慧图开发者平台(www.lbs.rtmap.com)定位API
 * 
 * @author dingtao
 *
 */
public class LocationActivity extends Activity implements OnClickListener,
		RMLocationListener {

	private TextView locateView;
	private Button mButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.location);
		locateView = (TextView) findViewById(R.id.tv_indoor_locate_info);
		mButton = (Button) findViewById(R.id.btn_start_locate);
		mButton.setOnClickListener(this);

		/**
		 * 设置log级别，默认是错误_LEVEL_ERROR，如果无法定位请打开log，我们会打印在初始化和定位过程中打印一些必要的信息用于核对错误
		 */
		// RMLog.LOG_LEVEL = RMLog.LOG_LEVEL_INFO;

		/**
		 * 身边有智慧图的beacon设备，但长时间无法定位，使用此方法，方便技术支持分析log
		 */
		// LocationApp.getInstance().setUseRtmapError(true);
		LocationApp.getInstance().init(getApplicationContext());// 定位服务初始化
		LocationApp.getInstance().registerLocationListener(this);// 注册回调接口，回调结果在onReceiveLocation()方法中查看
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_start_locate:
			if (mButton.getText().equals(getString(R.string.startLocate))) {
				/**
				 * 我们发现使用者在集成的时候，经常会调用start()方法发现没有回调定位结果，所以，我们给start()
				 * 返回值表示是否有api_key，key和service配置请参照文档或者本demo的AndroidManifest.xml
				 */
				boolean result = LocationApp.getInstance().start();// 开始定位，如果没有key，定位无法启动
				time = System.currentTimeMillis();
				if (result)
					mButton.setText(getString(R.string.stopLocate));
				else
					Toast.makeText(this, "请输入key", Toast.LENGTH_LONG).show();
			} else {
				LocationApp.getInstance().stop(); // 停止定位
				mButton.setText(getString(R.string.startLocate));
			}
			break;
		}
	}

	@Override
	protected void onDestroy() {
		/**
		 * 我们定位库中已经做了重复启动的判断，但是依旧建议用户在页面关闭的时候调用stop()方法停止定位
		 */
		LocationApp.getInstance().stop();// 停止定位
		LocationApp.getInstance().unRegisterLocationListener(this);// 取消回调
		super.onDestroy();
	}

	private long time;

	/**
	 * 注册回调接口RMLocationListener，重写此方法，你可以在这里处理定位结果 当错误码为0时，说明定位成功
	 */
	@Override
	public void onReceiveLocation(RMLocation location) {
		/**
		 * 这个是手机扫描数据，保存这个数据我们可以定位错误码，分析出来为啥没有定位，非调试阶段直接注释掉就好
		 */
		String scanner = LocationApp.getInstance().getScannerInfo();
		if (!RMStringUtils.isEmpty(scanner)) {
			// 文件保存scanner
		}
		Log.i("rtmap", location.getErrorInfo());
		locateView.setText("错误码: " + location.getError() + "\nlbsid:"
				+ location.getLbsid() + "\n建筑物ID: " + location.getBuildID()
				+ "\n楼层: " + location.getFloorID() + "\nx坐标(米): "
				+ location.getX() + "\ny坐标(米):  " + location.getY()
				+ "\n精度(米):" + location.getAccuracy() + "\ntime:"
				+ (System.currentTimeMillis() - time) / 1000.0 + "s");
	}
}
