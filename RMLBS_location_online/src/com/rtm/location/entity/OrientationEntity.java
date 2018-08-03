package com.rtm.location.entity;

import java.util.ArrayList;
import java.util.List;

public class OrientationEntity {

	private static final int SENSOR_ORI_BUFFER_MAX = 30;

	private List<float[]> orientation;

	private static OrientationEntity single = null;

	/** 得到该类的单例 */
	public synchronized static OrientationEntity getInstance() {
		if (single == null) {
			single = new OrientationEntity();
		}
		return single;
	}

	public void put(float[] angle) {
		synchronized (OrientationEntity.this) {
			orientation.add(angle);
			if (orientation.size() > SENSOR_ORI_BUFFER_MAX) {
				orientation.remove(0);
			}
		}
	}

	public List<float[]> get() {
		List<float[]> retFloats = new ArrayList<float[]>();
		synchronized (OrientationEntity.this) {
			retFloats.addAll(orientation);
			orientation.clear();
		}
		return retFloats;
	}

	private OrientationEntity() {
		orientation = new ArrayList<float[]>();
	}
}
