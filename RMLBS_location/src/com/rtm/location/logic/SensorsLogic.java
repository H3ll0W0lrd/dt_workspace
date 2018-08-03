package com.rtm.location.logic;

import android.content.Context;
import com.rtm.location.sensor.OrientationSensor;
import com.rtm.location.sensor.PressureSensor;

public class SensorsLogic {
	public enum SensorType {
		ACCELEROMETER, MAGNETOMETER, BAROMETER
	};

	private static SensorsLogic singleSensorsLogic = null;
	private boolean enableAccelerometer;
	private boolean enableMagnetometer;
	private boolean enableBarometer;

	private SensorsLogic() {
		enableAccelerometer = true;
		enableMagnetometer = true;
		enableBarometer = true;
	}

	/** 得到该类的单例 */
	public synchronized static SensorsLogic getInstance() {
		if (singleSensorsLogic == null) {
			singleSensorsLogic = new SensorsLogic();
		}
		return singleSensorsLogic;
	}

	public void setSensor(SensorType type, boolean enable) {

		switch (type) {
		case ACCELEROMETER:
			enableAccelerometer = enable;
			break;
		case MAGNETOMETER:
			enableMagnetometer = enable;
			break;
		case BAROMETER:
			enableBarometer = enable;
			break;
		}
	}

	public void setContext(Context c) {
		OrientationSensor.getInstance().init(c,
				(enableAccelerometer && enableMagnetometer));
		PressureSensor.getInstance().init(c, enableBarometer);
	}

	/** 注消传感器，停止数据接收 */
	public void stop() {
		OrientationSensor.getInstance().stop();
		PressureSensor.getInstance().stop();
	}

	/** 注册所需的传感器，并开始接收传感器数据 */
	public void start() {
		OrientationSensor.getInstance().start();
		PressureSensor.getInstance().start();
	}

	public void destroy() {

	}
}