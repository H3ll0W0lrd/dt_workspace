package com.rtm.location.logic;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.location.Location;

import com.rtm.common.utils.RMConfig;
import com.rtm.common.utils.RMFileUtil;
import com.rtm.common.utils.RMLog;
import com.rtm.common.utils.RMMode;
import com.rtm.location.LocationApp;
import com.rtm.location.entity.AccelerometerEntity;
import com.rtm.location.entity.BeaconEntity;
import com.rtm.location.entity.BeaconInfo;
import com.rtm.location.entity.GpsEntity;
import com.rtm.location.entity.MacRssEntity;
import com.rtm.location.entity.MagneticFieldEntity;
import com.rtm.location.entity.OfflineDataEntity;
import com.rtm.location.entity.OrientationEntity;
import com.rtm.location.entity.PressureEntity;
import com.rtm.location.entity.Type;
import com.rtm.location.entity.WifiEntity;
import com.rtm.location.utils.PhoneManager;
import com.rtm.location.utils.RMVersionLocation;
import com.rtm.location.utils.UtilLoc;

public class GatherData {

	private static final String TAG = "GatherData";
	private static final int REQUEST_BEACON_NUM_THRESHOLD = 50;
	private static final int REQUEST_AP_NUM_THRESHOLD = 50;
	private static final int BEACON_RSS_COMPENSATION = 0;

	private static GatherData instance = null;
	private Context context = null;

	// 用户输入GPS坐标
	public static double longitude = 0;
	public static double latitude = 0;

	public static int WIFI_COUNT;
	public static int BEACON_COUNT;
	private List<BeaconInfo> beaconEntity;
	private String userInfo;

	private GatherData() {
	}

	public static GatherData getInstance() {
		synchronized (LocationApp.class) {
			if (instance == null) {
				instance = new GatherData();
			}
		}
		return instance;
	}

	/**
	 * 得到当前扫描的beacon列表
	 */
	public List<BeaconInfo> getBeaconEntity() {
		return beaconEntity;
	}

	public void setUserInfo(String userInfo) {
		this.userInfo = userInfo;
	}

	public String getLocateXml(RMMode mode) {
		String retString = "";

		if (mode == RMMode.COLLECT_OFFLINE_DATA) {
			retString = getLocateXml();
			OfflineDataEntity.getInstance().write(retString,
					RMFileUtil.getLibsDir());
		} else if (mode == RMMode.RUN_OFFLINE_DATA) {
			if (OfflineDataEntity.getInstance().isOffFileExist(
					RMFileUtil.getLibsDir())) {
				retString = OfflineDataEntity.getInstance().readLine(
						RMFileUtil.getLibsDir());
			}
		} else {
			retString = getLocateXml();
		}
		return retString;
	}

	public String getLocateXml() {
		if (context != null && RMConfig.mac.equals("")) {
			RMConfig.mac = PhoneManager.getMac(context);
		}
		StringBuilder ret = new StringBuilder();
		ret.append("<Locating><ver>" + RMVersionLocation.VERSION
				+ "</ver><key>" + LocationApp.getInstance().getApiKey()
				+ "</key><u>" + RMConfig.mac + "</u><t>" + UtilLoc.getUtcTime()
				+ "</t><p>" + RMConfig.deviceType + "</p>");
		if (userInfo != null) {
			ret.append("<user_info>" + userInfo + "</user_info>");
		}
		// 最新定位结果(只传最近1分钟的gps定位结果)
		Location location = GpsEntity.getInstance().getLocation();
		if (location != null
				&& (System.currentTimeMillis() - location.getTime()) < 60 * 1000) {
			ret.append("<gps>" + location.getLongitude() + "#"
					+ location.getLatitude() + "#" + location.getAccuracy()
					+ "</gps>");
		} else if (longitude != 0 && latitude != 0) {
			// 用户输入GPS坐标默认精度为-1
			ret.append("<gps>" + longitude + "#" + latitude + "#" + "-1"
					+ "</gps>");
		}else{
			GpsEntity.getInstance().setLocation(null);
		}
		// ap
		StringBuilder aps = new StringBuilder();
		List<MacRssEntity> apsEntities = WifiEntity.getInstance().get();
		WIFI_COUNT = apsEntities.size();
		Collections.sort(apsEntities, new Comparator<MacRssEntity>() {

			@Override
			public int compare(MacRssEntity lhs, MacRssEntity rhs) {
				if (lhs.rss > rhs.rss) {
					return -1;
				} else if (lhs.rss < rhs.rss) {
					return 1;
				} else {
					return 0;
				}
			}
		});
		RMLog.i(TAG, "Request AP Number : " + apsEntities.size());
		int sendApSize = (apsEntities.size() > REQUEST_AP_NUM_THRESHOLD ? REQUEST_AP_NUM_THRESHOLD
				: apsEntities.size());
		for (int i = 0; i < sendApSize; i++) {
			int frequence = 2;
			if (apsEntities.get(i).chennal == Type.channel_5) {
				frequence = 5;
			}
			aps.append("#" + apsEntities.get(i).mac + "$"
					+ apsEntities.get(i).rss + "$" + frequence);
		}
		if (!aps.equals("")) {
			ret.append("<aps>" + aps.toString() + "</aps>");
		}

		// beacon
		StringBuilder apsBeacon = new StringBuilder();
		beaconEntity = BeaconEntity.getInstance().get();
		BEACON_COUNT = beaconEntity.size();
		Collections.sort(beaconEntity, new Comparator<BeaconInfo>() {

			@Override
			public int compare(BeaconInfo lhs, BeaconInfo rhs) {
				if (lhs.rssi > rhs.rssi) {
					return -1;
				} else if (lhs.rssi < rhs.rssi) {
					return 1;
				} else {
					return 0;
				}
			}
		});
		RMLog.i(TAG, "Request Beacon Num : " + beaconEntity.size());
		int sendBeaconSize = (beaconEntity.size() > REQUEST_BEACON_NUM_THRESHOLD ? REQUEST_BEACON_NUM_THRESHOLD
				: beaconEntity.size());
		for (int i = 0; i < sendBeaconSize; i++) {
			apsBeacon.append("#" + beaconEntity.get(i).mac + "$"
					+ (beaconEntity.get(i).rssi + BEACON_RSS_COMPENSATION));
		}
		if (!apsBeacon.equals("")) {
			ret.append("<beacons>" + apsBeacon + "</beacons>");
		}

		// 加速度计
		String accStr = AccelerometerEntity.getInstance().get();
		if (!accStr.equals("")) {
			ret.append("<acc>" + accStr + "</acc>");
		}

		// 磁场
		String magStr = MagneticFieldEntity.getInstance().get();
		if (!magStr.equals("")) {
			ret.append("<mag>" + magStr.toString() + "</mag>");
		}

		// 罗盘 compass, pitch, roll
		if (RtmapLbsService.isPdrOpen) {
			List<float[]> ori = OrientationEntity.getInstance().get();
			StringBuilder dataString = new StringBuilder();
			for (int i = 0; i < ori.size(); i++) {
				float[] values = ori.get(i);
				float cmpVal = values[0]
						+ LocationApp.getInstance().getMapAngle();
				// 这里为了防止罗盘值很大，这里多做几次判断
				cmpVal = cmpVal > 1080.0f ? cmpVal - 1080.0f : cmpVal;
				cmpVal = cmpVal > 720.0f ? cmpVal - 720.0f : cmpVal;
				cmpVal = cmpVal > 360.0f ? cmpVal - 360.0f : cmpVal;
				dataString.append("#"
						+ new DecimalFormat("###.##").format(cmpVal) + "$"
						+ new DecimalFormat("###.##").format(values[1]) + "$"
						+ new DecimalFormat("###.##").format(values[2]));
			}
			if (!dataString.equals("")) {
				ret.append("<cp>" + dataString.toString() + "</cp>");
			}
		}

		// pressure 气压
		List<Float> pre = PressureEntity.getInstance().get();
		StringBuilder predata = new StringBuilder();
		for (Float float1 : pre) {
			predata.append("#" + new DecimalFormat("####.##").format(float1));
		}
		if (!predata.equals("")) {
			ret.append("<pre>" + predata.toString() + "</pre>");
		}

		ret.append("</Locating>");
		return ret.toString();
	}

	public void setContext(Context c) {
		context = c;
	}

}
