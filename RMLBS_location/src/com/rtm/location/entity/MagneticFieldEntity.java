package com.rtm.location.entity;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.rtm.location.utils.UtilLoc;

public class MagneticFieldEntity {

	public long time; // 上一次取数据的时间

	private List<String> magneticFieldStr;

	private static final int SENSOR_MAG_BUFFER_MAX = 100;

	private static MagneticFieldEntity single = null;

	public synchronized static MagneticFieldEntity getInstance() {
		if (single == null) {
			single = new MagneticFieldEntity();
		}
		return single;
	}

	public void put(float[] mag) {
		synchronized (MagneticFieldEntity.this) {
			String accStr = new DecimalFormat("###.##").format(mag[0]) + "$"
					+ new DecimalFormat("###.##").format(mag[1]) + "$"
					+ new DecimalFormat("###.##").format(mag[2]);
			magneticFieldStr.add(accStr);
			if (magneticFieldStr.size() > SENSOR_MAG_BUFFER_MAX) {
				magneticFieldStr.remove(0);
			}
		}
	}

	public String get() {
		String retList = "";
		synchronized (MagneticFieldEntity.this) {
			for (String it : magneticFieldStr) {
				retList += "#" + it;
			}
			magneticFieldStr.clear();
		}
		time = UtilLoc.getUtcTime();
		return retList;
	}

	private MagneticFieldEntity() {
		magneticFieldStr = new ArrayList<String>();
	}

}
