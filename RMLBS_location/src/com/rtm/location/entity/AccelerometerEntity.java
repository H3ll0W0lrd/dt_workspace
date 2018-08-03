package com.rtm.location.entity;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class AccelerometerEntity {

	private List<String> accelerometerStr;

	private static final int SENSOR_ACC_BUFFER_MAX = 100;

	private static AccelerometerEntity single = null;

	public synchronized static AccelerometerEntity getInstance() {
		if (single == null) {
			single = new AccelerometerEntity();
		}
		return single;
	}

	public void put(float[] acc) {
		synchronized (AccelerometerEntity.this) {
			String accStr = new DecimalFormat("###.##").format(acc[0]) + "$"
					+ new DecimalFormat("###.##").format(acc[1]) + "$"
					+ new DecimalFormat("###.##").format(acc[2]);
			accelerometerStr.add(accStr);
			if (accelerometerStr.size() > SENSOR_ACC_BUFFER_MAX) {
				accelerometerStr.remove(0);
			}
		}
	}

	public String get() {
		String retList = "";
		synchronized (AccelerometerEntity.this) {
			for (String it : accelerometerStr) {
				retList += "#" + it;
			}
			accelerometerStr.clear();
		}
		return retList;
	}

	private AccelerometerEntity() {
		accelerometerStr = new ArrayList<String>();
	}
}
