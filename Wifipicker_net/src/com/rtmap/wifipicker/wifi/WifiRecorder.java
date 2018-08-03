package com.rtmap.wifipicker.wifi;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.net.wifi.ScanResult;

import com.rtm.location.entity.AccelerometerEntity;
import com.rtm.location.entity.BeaconEntity;
import com.rtm.location.entity.MacRssEntity;
import com.rtm.location.entity.MagneticFieldEntity;
import com.rtm.location.entity.OrientationEntity;
import com.rtm.location.entity.PressureEntity;
import com.rtmap.wifipicker.BuildSession;
import com.rtmap.wifipicker.core.WPApplication;
import com.rtmap.wifipicker.page.WPLoginActivity;
import com.rtmap.wifipicker.util.ConfigLoc;
import com.rtmap.wifipicker.util.ConstantLoc.UIEventCode;
import com.rtmap.wifipicker.util.Constants;
import com.rtmap.wifipicker.util.DTFileUtils;
import com.rtmap.wifipicker.util.DTLog;
import com.rtmap.wifipicker.util.WPDBService;
import com.rtmap.wifipicker.util.UtilLoc;
import com.rtmap.wifipicker.util.Utils;

/**
 * 记录wifi各种状态
 * 
 * @author zhengnengyuan
 *
 */
public class WifiRecorder {

	private static final int MAX_SSID_LENGTH = 16;

	private int noSignalCount = 0;// wifi扫描信号为空计数

	private final String TAG = "WifiRecorder";

	// 分隔符
	String spitData = "#", spitSpace = "$", replace = "_";

	/** 类的静态对象，用于保存类的单例 **/
	private static volatile WifiRecorder instance = null;

	/** 同步锁，用于数据的一致性 **/
	private static Object lockObject = new Object();

	/** wiif信息buffer，存储所有采集到的信息 **/
	ArrayList<String> wifiData;

	/** wiif信息扫描次数 **/
	private int mScanCount = 40;

	/** 指纹点信息 **/
	private CollectFingerPoint finger = new CollectFingerPoint();

	/** 采集wifi指纹的附件信息 **/
	public static CollectAppendInfo appendInfo = new CollectAppendInfo();

	/** 文件后缀,wifi原始数据 **/
	public final static String wifiFileSuffix = ".wifi";

	/** 前一次采集到的ap信息 **/
	private String gatherAPBefore = "";

	/** 采集时间 **/
	@SuppressWarnings("unused")
	private String gatherTime = "";

	private boolean mGatherOnWalk;

	private boolean mGathering;

	private boolean mPaused;

	private WifiUpdateTask mWifiUpdateTask;

	private WifiSensorHardware mWifiSensor;

	private WifiRecorder() {
		wifiData = new ArrayList<String>();
		onResiteTime();
	}

	/**
	 * 重置采集文件名的时间戳
	 */
	public void onResiteTime() {
		gatherTime = UtilLoc.getTimeMillis();
	}

	/** 得到该类对象的一个单例 **/
	public static WifiRecorder getInstance() {
		synchronized (lockObject) {
			if (instance == null) {
				instance = new WifiRecorder();
			}
		}
		return instance;
	}

	public boolean isGathering() {
		return mGathering;
	}

	/**
	 * 启动计时器，开始进行定位运算
	 * 
	 * @param fg
	 *            {@link CollectFingerPoint} 指纹点的信息
	 */
	public void onStart(CollectFingerPoint fg) {
		onSetPointInfo(fg); // 设置采集点的信息
		// SettingInfo.saveFileInfo(filePath + wifiFileSuffix,
		// Util.getCurrDay());

		// 打开wifi并启动
		mWifiSensor = WifiSensorHardware.getInstance();
		mWifiSensor.onStart();
		mWifiSensor.openWifi();

		// 数据清0
		mScanCount = 0;
		wifiData.clear();

		// 启动定位器进行扫描
		if (mWifiUpdateTask == null) {
			mWifiUpdateTask = new WifiUpdateTask();
			mGathering = true;
		}
		mWifiUpdateTask.run();
	}

	/** 停止传感器 **/
	public void onStop() {
		if (mWifiUpdateTask != null) {
			mWifiUpdateTask = null;
			mGathering = false;
		}
	}

	public void onPause() {
		mPaused = true;
	}

	public void onResume() {
		mPaused = false;
	}

	/**
	 * 获得.wifi文件的附件信息，包括手机信息
	 */
	private void generatePhoneInf() {
		// appendInfo.deviceId = phoneManager.getDeviceId();
		appendInfo.deviceKind = android.os.Build.MODEL;
		appendInfo.floorid = BuildSession.getInstance().getBuildId();
	}

	/**
	 * 设置指纹点信息
	 * 
	 * @param f
	 *            指纹点
	 */
	private synchronized void onSetPointInfo(CollectFingerPoint f) {
		finger.sPointX = f.sPointX;
		finger.sPointY = f.sPointY;
		finger.sPointFloor = f.sPointFloor;
		finger.sPointFingerId = f.sPointFingerId;
		finger.sPointScanCount = f.sPointScanCount;
		finger.sCanRepeated = f.sCanRepeated;
	}

	/** 注销，释放内存空间 **/
	public void onDestroy() {
		onStop();
		if (mWifiSensor != null) {
			mWifiSensor.onDestroy();
			mWifiUpdateTask = null;
		}

		synchronized (lockObject) {
			instance = null;
		}
	}

	/**
	 * 保存.wifi的头信息
	 * 
	 * @param fileName
	 *            文件名
	 * @param otherInfo
	 */
	public String getWifiHead() {
		generatePhoneInf();
		StringBuffer inf = new StringBuffer();
		inf.append(String.format("#%d#\n", appendInfo.secretcod));
		if (mGatherOnWalk) {
			inf.append(String.format("%s=%s\n", "fileVersion", 1));
		} else {
			inf.append(String.format("%s=%s\n", "fileVersion",
					appendInfo.fileVersion));
		}
		inf.append(String
				.format("%s=%s\n", "deviceKind", appendInfo.deviceKind));
		inf.append(String.format("%s=%s\n", "deviceId", appendInfo.deviceId));
		inf.append(String.format("%s=%s\n", "mapId", appendInfo.floorid));
		inf.append(String.format("%s=%s\n", "user", WPApplication.getInstance()
				.getShare().getString(DTFileUtils.PREFS_USERNAME, "")));
		inf.append(String.format("%s=%s\n", "refPtLeftUp",
				appendInfo.longitude_1 + "," + appendInfo.latitude_1 + ","
						+ appendInfo.refPlatForm));
		inf.append(String.format("%s=%s\n", "refPtLeftDown",
				appendInfo.longitude_1 + "," + appendInfo.latitude_1 + ","
						+ appendInfo.refPlatForm));
		inf.append(String.format("%s=%s\n", "refCoordLeftDown", appendInfo.x_2
				+ "," + appendInfo.y_2));
		inf.append(String.format("%s=%s\n", "refPtRightDown",
				appendInfo.longitude_2 + "," + appendInfo.latitude_2 + ","
						+ appendInfo.refPlatForm));
		inf.append(String.format("%s=%s\n", "refCoordRightDown",
				+appendInfo.x_3 + "," + appendInfo.y_3));
		return inf.toString();
	}

	/**
	 * 把采集到的数据存入文件中,buffer中存的数据为详细的ap信息
	 */
	private String flush(ArrayList<String> info, CollectFingerPoint point) {
		int size = info.size();
		StringBuffer gatherBuffer = new StringBuffer();
		gatherBuffer.append("<point><pid>" + point.sPointFingerId + "</pid>");
		gatherBuffer.append("<coord>" + (int) (finger.sPointX * 1000) + ","
				+ (int) (finger.sPointY * 1000) + "</coord>");
		gatherBuffer.append("<count>" + size + "</count>");
		for (int i = 0; i < size; i++) {
			try {
				gatherBuffer.append(info.get(i));
				gatherBuffer.append(getBeaconData());
                gatherBuffer.append(getSensorData());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		gatherBuffer.append("</point>\n");

		return gatherBuffer.toString();
	}

	public void setGatherOnWalk(boolean gatherOnWalk) {
		mGatherOnWalk = gatherOnWalk;
	}

	/**
	 * wifi扫描定时器 <br/>
	 * 每隔若干ms采集一次wifi数据
	 */
	class WifiUpdateTask implements Runnable {

		ArrayList<String> list = new ArrayList<String>();

		/**
		 * 得到采集到的wifi信息</br> 格式： #mac$rssi$ssid$chnl
		 * 
		 * @param lsScanResult
		 * @return
		 */
		protected String getWifiData(List<ScanResult> lsScanResult) {

			if (lsScanResult == null)
				return "";

			// 扫描的得到的ap个数必须要大于0
			if (lsScanResult.size() < 1)
				return "";

			// 记录每一个采集到的信息
			StringBuffer strBuffer = new StringBuffer();
			for (ScanResult result : lsScanResult) {
				int rssi = result.level;
				int Chanel = (Integer.valueOf(result.frequency).intValue() - 2412) / 5 + 1;
				String SSID = result.SSID.trim();
				if (SSID.length() > MAX_SSID_LENGTH) {
					SSID = SSID.substring(0, MAX_SSID_LENGTH);
				}
				String MAC = result.BSSID.replace(":", "").toUpperCase();
				strBuffer.append(spitData + MAC + spitSpace + rssi + spitSpace
						+ SSID + spitSpace + Chanel);
			}
			return strBuffer.toString();
		}

		@Override
		public void run() {
			if (mWifiSensor == null || mPaused) {
				return;
			}
			List<ScanResult> lsScanResult = mWifiSensor.onGetApList(); // 扫描wifi信息
			String apsStr = getWifiData(lsScanResult);// 为空时返回"",不返回null

			// System.out.println(apsStr);

			// wifi扫描5次为空，提示重启wifi
			if (Utils.isEmpty(apsStr)) {
				noSignalCount++;
			}
			if (noSignalCount >= 5) {
				UIEvent.getInstance().notifications(
						UIEventCode.NO_WIFI_SIGNAL_REMINDER);// 提示用户重启wifi
				mWifiSensor.restartWifi();
				noSignalCount = 0;
			}

			if ((gatherAPBefore.equals(apsStr) || Utils.isEmpty(apsStr))
					&& (!finger.sCanRepeated)) {
				// 采集为空或者与上次相同
				if (list != null) {
					if (apsStr != null) {
						list.add(apsStr);
					} else {
						list.add("");
					}
				}
			} else {
				gatherAPBefore = apsStr;
				mScanCount++;
				wifiData.add("<sample time=" + UtilLoc.getCurTimeMillis() + ">"
						+ apsStr + "</sample>");
				list.clear();
			}
			if (list != null && list.size() >= 2) {
				mScanCount = list.size();
				if (!Utils.isEmpty(list.get(0))) {
					gatherAPBefore = list.get(0);
					wifiData.add("<sample time=" + UtilLoc.getCurTimeMillis()
							+ ">" + list.get(0) + "</sample>");
				} else if (!Utils.isEmpty(list.get(1))) {
					gatherAPBefore = list.get(1);
					wifiData.add("<sample time=" + UtilLoc.getCurTimeMillis()
							+ ">" + list.get(1) + "</sample>");
				} else {
					gatherAPBefore = "";
					wifiData.add("<sample time=" + UtilLoc.getCurTimeMillis()
							+ ">" + "</sample>");
				}
				list.clear();
			}
			if (wifiData != null && (!wifiData.isEmpty())) {

				// 将坐标位置告知ui显示界面
				int size = 0;
				if (lsScanResult != null) {
					size = lsScanResult.size();
				}
				UIEvent.getInstance().notifications(UIEventCode.WIFI_SCAN,
						mScanCount, size);

				if (mScanCount >= finger.sPointScanCount) {
					String info = flush(wifiData, finger);
					DTLog.i(info);
					// 执行操作 数据存储
					if (mGatherOnWalk) {
						WPDBService.getInstance().insertPoint(BuildSession.getInstance()
								.getBuildId(), BuildSession.getInstance()
								.getFloor(), finger.sPointX, finger.sPointY,
								BuildSession.getInstance().getBuildId()
										+ BuildSession.getInstance().getFloor()
										+ "-0", info,
								Constants.TYPE_WIFI_WALK);
					} else {
						WPDBService.getInstance().insertPoint(BuildSession.getInstance()
								.getBuildId(), BuildSession.getInstance()
								.getFloor(), finger.sPointX, finger.sPointY,
								BuildSession.getInstance().getBuildId()
										+ BuildSession.getInstance().getFloor()
										+ "-0", info,
								Constants.TYPE_WIFI_NORMAL);
					}
					int apCount = 0;// 本次采集接收到的ap个数
					if (gatherAPBefore != null && (!gatherAPBefore.equals(""))) {
						for (int i = 0; i < gatherAPBefore.length(); i++) {
							if (gatherAPBefore.charAt(i) == '#') {
								apCount++;
							}
						}
						UIEvent.getInstance().notifications(
								UIEventCode.WIFI_SCAN_END, apCount);
					}
					// UIEvent.getInstance().notifications(UIEventCode.WIFI_SCAN_END,
					// mScanCount);
				}
			}
		}
	}
	
	
	private String getBeaconData() {
        String ret = "";
        String beaconStr = "";
        List<MacRssEntity> beaconEntity = BeaconEntity.getInstance().get();
        Collections.sort(beaconEntity, new Comparator<MacRssEntity>() {

            @Override
            public int compare(MacRssEntity lhs, MacRssEntity rhs) {
                if (lhs.rss > rhs.rss) {
                    return -1;
                }
                return 0;
            }
        });
        for (int i = 0; i < beaconEntity.size(); i++) {
            beaconStr += "#" + beaconEntity.get(i).mac + "$" + (beaconEntity.get(i).rss);
        }
        if (!beaconStr.equals("")) {
            ret += "<beacons>" + beaconStr + "</beacons>";
        }
        return ret;
    }

    private String getSensorData() {
        String ret = "";
        String accStr = AccelerometerEntity.getInstance().get();
        if (!accStr.equals("")) {
            ret += "<acc>" + accStr + "</acc>";
            DTLog.i("accStr:"+accStr.split("#").length);
        }
        // 磁场
        String magStr = MagneticFieldEntity.getInstance().get();
        if (!magStr.equals("")) {
            ret += "<mag>" + magStr + "</mag>";
        }
        List<float[]> ori = OrientationEntity.getInstance().get();
        String dataString = "";
        for (int i = 0; i < ori.size(); i++) {
            float[] values = ori.get(i);
            float cmpVal = values[0];
            // 这里为了防止罗盘值很大，这里多做几次判断
            cmpVal = cmpVal > 1080.0f ? cmpVal - 1080.0f : cmpVal;
            cmpVal = cmpVal > 720.0f ? cmpVal - 720.0f : cmpVal;
            cmpVal = cmpVal > 360.0f ? cmpVal - 360.0f : cmpVal;
            dataString += "#" + new DecimalFormat("###.##").format(cmpVal) + "$"
                    + new DecimalFormat("###.##").format(values[1]) + "$"
                    + new DecimalFormat("###.##").format(values[2]);
        }
        if (!dataString.equals("")) {
            ret += "<cp>" + dataString + "</cp>";
        }
        // pressure 气压
        List<Float> pre = PressureEntity.getInstance().get();
        String predata = "";
        for (Float float1 : pre) {
            predata += "#" + float1;
        }
        if (!predata.equals("")) {
            ret += "<pre>" + predata + "</pre>";
        }

        return ret;
    }
}
