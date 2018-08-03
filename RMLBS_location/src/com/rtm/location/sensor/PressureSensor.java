package com.rtm.location.sensor;

import java.util.List;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.rtm.location.entity.PressureEntity;

public class PressureSensor {

	private SensorManager sm;

	private Context context = null;

	private boolean valid = false;

	private static PressureSensor pSensor = null;

	private PressureSensor() {
	}

	public void init(Context c, boolean isValid) {
		synchronized (PressureSensor.this) {
			context = c;
			valid = isValid;
		}
	}

	public synchronized static PressureSensor getInstance() {
		if (pSensor == null) {
			pSensor = new PressureSensor();
		}
		return pSensor;
	}

	private SensorEventListener pressureListener = new SensorEventListener() {

		@Override
		public void onSensorChanged(SensorEvent event) {
			if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {
				PressureEntity.getInstance().put(event.values[0]);
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	};

	public void stop() {
		if (sm != null) {
			sm.unregisterListener(pressureListener);
		}
	}

	public boolean start() {
		if (isValid()) {
			sm = (SensorManager) context
					.getSystemService(Context.SENSOR_SERVICE);
			sm.registerListener(pressureListener,
					sm.getDefaultSensor(Sensor.TYPE_PRESSURE),
					SensorManager.SENSOR_DELAY_NORMAL);
			return true;
		} else {
			return false;
		}
	}

	public void destory() {
		if (sm != null) {
			sm = null;
		}
		if (pSensor != null) {
			pSensor = null;
		}
	}

	public int getType() {
		return Sensor.TYPE_PRESSURE;
	}

	private boolean isValid() {
		boolean ret = false;
		SensorManager sm = (SensorManager) context
				.getSystemService(Context.SENSOR_SERVICE);
		List<Sensor> allSensors = sm.getSensorList(Sensor.TYPE_ALL);

		if (valid) {
			for (int i = 0; i < allSensors.size(); i++) {
				Sensor sensor = allSensors.get(i);
				if (getType() == sensor.getType()) {
					ret = true;
				}
			}
		}

		return ret;
	}
}
