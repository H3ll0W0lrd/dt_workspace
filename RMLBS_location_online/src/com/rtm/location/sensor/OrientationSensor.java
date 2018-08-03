package com.rtm.location.sensor;

import java.util.List;

import com.rtm.common.utils.RMLog;
import com.rtm.location.LocationApp;
import com.rtm.location.entity.AccelerometerEntity;
import com.rtm.location.entity.MagneticFieldEntity;
import com.rtm.location.entity.OrientationEntity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class OrientationSensor {

	// private static final String TAG = "OrientationSensor";

	private SensorManager sm = null;
	private Sensor aSensor = null;
	private Sensor mSensor = null;

	private boolean isValid = true;
	private boolean isOrientationSupport;

	float[] accelerometerValues = new float[3];
	float[] magneticFieldValues = new float[3];
	float[] values = new float[3];
	float[] rotate = new float[9];

	private Context context = null;
	private static OrientationSensor instance = null;

	private OrientationSensor() {

	}

	public static OrientationSensor getInstance() {
		if (instance == null) {
			instance = new OrientationSensor();
		}
		return instance;
	}

	public void init(Context c, boolean isValid) {
		boolean isAccelerometerSupport = false;
		boolean isMagnetometerSupport = false;
		context = c;
		this.isValid = isValid;
		sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		// 得到手机上所有的传感器
		List<Sensor> listSensor = sm.getSensorList(Sensor.TYPE_ALL);
		for (Sensor sensor : listSensor) {
			if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				isAccelerometerSupport = true;
			} else if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
				isMagnetometerSupport = true;
			}
		}
		isOrientationSupport = (isAccelerometerSupport && isMagnetometerSupport);
	}

	public void start() {
		if (isValid && isOrientationSupport) {
			RMLog.i("rtmap", "start OrientationSenser");
			aSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			mSensor = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
			sm.registerListener(sensorEventListener, aSensor,
					SensorManager.SENSOR_DELAY_GAME);
			sm.registerListener(sensorEventListener, mSensor,
					SensorManager.SENSOR_DELAY_GAME);
		}
	}

	public void stop() {
		if (isValid && isOrientationSupport) {
			RMLog.i("rtmap", "stop OrientationSenser");
			sm.unregisterListener(sensorEventListener, aSensor);
			sm.unregisterListener(sensorEventListener, mSensor);
		}
	}

	public void destory() {
		if (sm != null) {
			sm = null;
		}
		if (aSensor != null) {
			aSensor = null;
		}
		if (mSensor != null) {
			aSensor = null;
		}
	}

	long counter = 0;
	final SensorEventListener sensorEventListener = new SensorEventListener() {

		@Override
		public void onSensorChanged(SensorEvent event) {
			// counter++;
			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				accelerometerValues = event.values;
				AccelerometerEntity.getInstance().put(event.values);
				// if (counter % 20 == 0) {
				// Log.e("OrientationSensor", event.values[0] + ",  " +
				// event.values[1] + ", " + event.values[2]);
				// }
			}
			if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
				magneticFieldValues = event.values;
				MagneticFieldEntity.getInstance().put(event.values);
				// if (counter % 20 == 0) {
				// Log.e("OrientationSensor", event.values[0] + ",  " +
				// event.values[1] + ", " + event.values[2]);
				// }
			}

			SensorManager.getRotationMatrix(rotate, null, accelerometerValues,
					magneticFieldValues);
			SensorManager.getOrientation(rotate, values);
			// 经过SensorManager.getOrientation(rotate, values);得到的values值为弧度
			// ,转换为角度
			values[0] = (float) Math.toDegrees(values[0]);

			if (values[0] < 0) {
				values[0] += 360;
			}
			OrientationEntity.getInstance().put(values);
			// if (counter % 20 == 0) {
			// Log.e("OrientationSensor", values[0] + ",  " + values[1] + ", " +
			// values[2]);
			// }
		}

		@Override
		public void onAccuracyChanged(Sensor arg0, int arg1) {

		}

	};

}
