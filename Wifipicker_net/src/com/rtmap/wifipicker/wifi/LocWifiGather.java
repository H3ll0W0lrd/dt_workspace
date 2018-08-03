package com.rtmap.wifipicker.wifi;

import java.util.ArrayList;
import java.util.List;

import com.rtmap.wifipicker.util.UtilLoc;

import android.net.wifi.ScanResult;

/**
 * 实时定时用到的wifi数据包
 * wifi的一次采集数据包
 * @author hotstar
 *
 */
public class LocWifiGather implements Cloneable{
	public String gatherTime;
	public List<LocWifiGatherAp> apList;
	
	public LocWifiGather()
	{
		apList = new ArrayList<LocWifiGatherAp>();
	}
	
	public void putGatherInfo(List<ScanResult> scanInfo){
		if (scanInfo != null) {
			for (ScanResult result : scanInfo) {
				LocWifiGatherAp wifiAp = new LocWifiGatherAp();
				wifiAp.mac = result.BSSID.replace(":", "").toUpperCase();
				wifiAp.rss = result.level;
				apList.add(wifiAp);
			}
			gatherTime = UtilLoc.getUtcTime() + "";
		}
	}	
}
