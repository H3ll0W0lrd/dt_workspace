package com.rtm.location.entity;

import java.util.ArrayList;
import java.util.List;

public class PressureEntity {

	private static final int PRESS_BUFFER_MAX = 30;
	private List<Float> press;
	private static PressureEntity single = null;

	public synchronized static PressureEntity getInstance() {
		if (single == null) {
			single = new PressureEntity();
		}
		return single;
	}

	public void put(float pre) {
		synchronized (PressureEntity.this) {
			press.add(pre);
			if (press.size() > PRESS_BUFFER_MAX) {
				press.remove(0);
			}
		}
	}

	public List<Float> get() {
		List<Float> retFloats = new ArrayList<Float>();
		synchronized (PressureEntity.this) {
			retFloats.addAll(press);
			press.clear();
		}
		return retFloats;
	}

	private PressureEntity() {
		press = new ArrayList<Float>();
	}
}
