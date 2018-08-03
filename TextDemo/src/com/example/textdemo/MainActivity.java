package com.example.textdemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;

import com.dingtao.libs.util.DTIOUtil;
import com.example.textdemo.sql.RMSqlite;
import com.example.textdemo.view.DTCircleImage;
import com.example.textdemo.view.DTStatelliteView;

public class MainActivity extends Activity {

	private SensorManager sensorManager = null;
	private Sensor mPressure;
	private TextView mText, mContent;
	private DTStatelliteView mSatellite;

	SensorEventListener pressureListener = new SensorEventListener() {
		@Override
		public void onSensorChanged(SensorEvent event) {
			mText.setText(String.format("%.2f", event.values[0]));
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	};

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mText = (TextView) findViewById(R.id.text);
		mText.setGravity(Gravity.BOTTOM | Gravity.LEFT);
		mContent = (TextView) findViewById(R.id.content);

		RMSqlite.getInstance().getPoi();

		// mContent.setText("Product Model: " + android.os.Build.MODEL + ","
		// + android.os.Build.VERSION.SDK + ","
		// + android.os.Build.VERSION.RELEASE);

		// if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO
		// && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
		String s = "cpu_type2:" + android.os.Build.CPU_ABI2 + "   type:"
				+ android.os.Build.CPU_ABI;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			String[] str = android.os.Build.SUPPORTED_ABIS;
			for (int i = 0; i < str.length; i++)
				s += "\n" + str[i];
		}
		mContent.setText(s);

		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mPressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
		if (mPressure == null) {
			mText.setText("您的手机不支持气压传感器，无法使用本软件功能.");
		} else {

		}

		ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		ConfigurationInfo info = am.getDeviceConfigurationInfo();
		Log.i("dt", "openGl版本：" + info.reqGlEsVersion);
		// 假如是opengles 1.1 info.reqGlEsVersion= 0x00010001
		// 假如是opengles 2.0 info.reqGlEsVersion= 0x00020000

		Uri uri = Uri.parse("content://com.dingtao.com/image/1");
		Log.i("rtmap", uri.toString());
		DTCircleImage circleImage = (DTCircleImage) findViewById(R.id.dTCircleImage1);
		Drawable red = getResources().getDrawable(R.drawable.i);
		Bitmap mBitmap = DTIOUtil.drawableToBitmap(red);
		circleImage.setImageBitmap(mBitmap);
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mPressure != null)
			sensorManager.registerListener(pressureListener, mPressure,
					SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mPressure != null)
			sensorManager.unregisterListener(pressureListener);
	}
}
