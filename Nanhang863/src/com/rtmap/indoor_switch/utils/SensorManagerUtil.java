package com.rtmap.indoor_switch.utils;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class SensorManagerUtil implements SensorEventListener {

	private SensorManager mSensorManager;
	private Context context;
	public SensorEventListener sensorEventListener;
	private boolean isFlat = false;// true 是平放
	private String tag = "SensorManagerUtil";

	public void registerSensor() {
		Sensor sensor_orientation = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mSensorManager.registerListener(this, sensor_orientation,
				SensorManager.SENSOR_DELAY_UI);
	}

	public SensorManagerUtil(Context context) {
		this.context = context;
		initSensor();
	}

	public SensorManagerUtil(Context context,
			SensorEventListener sensorEventListener) {
		this(context);
		this.sensorEventListener = sensorEventListener;
	}

	private void initSensor() {
		mSensorManager = (SensorManager) context.getApplicationContext()
				.getSystemService(Context.SENSOR_SERVICE);
	}

	public void setSensorEventListener(SensorEventListener sensorEventListener) {
		this.sensorEventListener = sensorEventListener;
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {

	}

	boolean isFloatType = false;

	@Override
	public void onSensorChanged(SensorEvent event) {

		// 加速度传感器
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

			// x表示手机指向的方位，0表示北,90表示东，180表示南，270表示西

			float x = event.values[0];
			float y = event.values[1];
			float z = event.values[2];

			// tvResult.setText("Orientation:" + x + "," + y + "," + z);

			Log.v(tag, "Orientation:" + x + "," + y + "," + z);
			if (x < 1 && y < 3 && z > 1) {
				isFlat = true;
			} else if (x < 1 && y > 7 && z < 1) {
				isFlat = false;
			}
			if (sensorEventListener != null) {
				if (isFlat != isFloatType) {
					isFloatType = isFlat;
					sensorEventListener.onSensorChanged(isFlat);
				}
			} else {
				Log.e(tag, "SensorEventListener is Null");
			}

		}
	}

	public interface SensorEventListener {
		void onSensorChanged(boolean isFlat);
	}

	public void unRegisterSensor() {
		mSensorManager.unregisterListener(this);
	}

}
