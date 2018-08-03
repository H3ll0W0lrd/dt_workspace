package com.rtm.location.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.net.wifi.ScanResult;

public class WifiEntity {

	private Map<String, MacRssEntity> wifiAps;

	private static WifiEntity single = null;

	public synchronized static WifiEntity getInstance() {
		if (single == null) {
			single = new WifiEntity();
		}
		return single;
	}

	public void put(List<ScanResult> scanInfo) {
		synchronized (WifiEntity.this) {
			if (scanInfo != null) {
				for (ScanResult result : scanInfo) {
					String mac = result.BSSID.replace(":", "").toUpperCase(
							Locale.getDefault());
					int rss = result.level;
					Type frequenceType = Type.channel_24;
					int frequence = result.frequency;
					if (frequence > 3000) {
						frequenceType = Type.channel_5;
					}
					if (wifiAps.containsKey(mac)) {
						wifiAps.get(mac).rss += rss;
						wifiAps.get(mac).count++;
					} else {
						wifiAps.put(mac, new MacRssEntity(mac, rss,
								frequenceType));
					}
				}
			}
		}
	}

	public List<MacRssEntity> get() {
		List<MacRssEntity> retFloats = new ArrayList<MacRssEntity>();
		synchronized (WifiEntity.this) {
			if (wifiAps.size() != 0) {
				for (String m : wifiAps.keySet()) {
					MacRssEntity ap = wifiAps.get(m);
					if (ap != null && ap.count != 0) {
						ap.rss = ap.rss / ap.count;
						ap.count = 1;
						retFloats.add(ap);
					}
				}
				wifiAps.clear();
			}
		}
		return retFloats;
	}

	private WifiEntity() {
		wifiAps = new HashMap<String, MacRssEntity>();
	}

	public void clearAp() {
		wifiAps.clear();
	}
}
