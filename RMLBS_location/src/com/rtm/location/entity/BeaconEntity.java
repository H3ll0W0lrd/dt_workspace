package com.rtm.location.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeaconEntity {

	private Map<String, BeaconInfo> beacons;

	private static BeaconEntity single = null;

	public synchronized static BeaconEntity getInstance() {
		if (single == null) {
			single = new BeaconEntity();
		}
		return single;
	}

	public synchronized void put(BeaconInfo beacon) {
		String mac = beacon.mac;
		int rss = beacon.rssi;
		if (beacons.containsKey(mac)) {
			beacons.get(mac).rssi += rss;
			beacons.get(mac).count++;
		} else {
			beacons.put(mac, beacon);
		}
	}

	public synchronized List<BeaconInfo> get() {
		List<BeaconInfo> retFloats = new ArrayList<BeaconInfo>();

		for (String m : beacons.keySet()) {
			BeaconInfo ap = beacons.get(m);
			if (ap.count != 0) {
				ap.rssi = ap.rssi / ap.count;
//				ap.count = 1;
				retFloats.add(ap);
			}
		}
		beacons.clear();
		return retFloats;
	}

	private BeaconEntity() {
		beacons = new HashMap<String, BeaconInfo>();
	}

	public void clearBeacon() {
		beacons.clear();
	}
}
