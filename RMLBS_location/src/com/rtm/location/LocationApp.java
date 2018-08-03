package com.rtm.location;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import com.rtm.common.http.RMHttpUrl;
import com.rtm.common.http.RMHttpUtil;
import com.rtm.common.model.RMLocation;
import com.rtm.common.utils.OnSearchPoiListener;
import com.rtm.common.utils.RMAsyncTask;
import com.rtm.common.utils.RMConfig;
import com.rtm.common.utils.RMFileUtil;
import com.rtm.common.utils.RMLog;
import com.rtm.common.utils.RMStringUtils;
import com.rtm.location.entity.FenceInfo;
import com.rtm.location.entity.RMUser;
import com.rtm.location.entity.iBeaconClass;
import com.rtm.location.logic.GatherData;
import com.rtm.location.logic.RtmapLbsService;
import com.rtm.location.logic.SensorsLogic;
import com.rtm.location.logic.SensorsLogic.SensorType;
import com.rtm.location.sensor.BeaconSensor;
import com.rtm.location.sensor.GpsSensor;
//import com.rtm.location.sensor.GpsSensor;
import com.rtm.location.sensor.WifiSensor;
import com.rtm.location.utils.OnFenceListener;
import com.rtm.location.utils.PhoneManager;
import com.rtm.location.utils.RMDownloadFingerUtil;
import com.rtm.location.utils.RMLocationListener;
import com.rtm.location.utils.RMSearchLbsPoiUtil;
import com.rtm.location.utils.RMSqlite;
import com.rtm.location.utils.RMUserUtil;
import com.rtm.location.utils.RMUserUtil.OnGetUserListener;
import com.rtm.location.utils.RMVersionLocation;
import com.rtm.location.utils.RMWhiteUtil;
import com.rtm.location.utils.RMWhiteUtil.OnGetWhiteListener;

/**
 * 定位库接口类，包含初始化、开启、结束定位等接口，还包括对定位参数（回调频率、定位文件路径、），供开发者使用;V2.0版本y坐标为正数，V2.1版本以后，
 * y坐标按照GIS坐标系标准改为负数。
 * 
 * @author dingtao
 */
public class LocationApp {

	private static volatile LocationApp instance = null;
	private Context mContext = null;

	private RMLocation mCurrentLocation;// 当前位置
	private boolean isStartLocate;// 是否开启定位

	private ReentrantLock lock, lockServerAddress;
	/**
	 * 回调接口管理List
	 */
	private ArrayList<RMLocationListener> LocateList = new ArrayList<RMLocationListener>();
	private ArrayList<OnFenceListener> FenceList = new ArrayList<OnFenceListener>();

	/**
	 * 地图偏转角度，用于PDR推算
	 */
	private float mapAngle;
	/**
	 * 扫描信息
	 */
	public String mScannerInfo;
	/**
	 * 定位标记
	 */
	public int mLbsSign;
	/**
	 * 离线定位标记
	 */
	public static final int OFFLINE = 101;
	/**
	 * 混合定位标记
	 */
	public static final int MIX = 102;
	/**
	 * 在线定位标记
	 */
	public static final int ONLINE = 103;
	/**
	 * 线程执行器
	 */
	public static ExecutorService EXECUTOR;
	/**
	 * 实时定位时时间间隔/2(ms)
	 */
	private int mRequestSpanTime = 1000;

	/**
	 * 智慧图API_KEY
	 */
	private String apiKey;

	private boolean isWifiopen;// wifi是否开启
	private boolean isBlueOpen;// 蓝牙是否开启

	/**
	 * 是否使用智慧图错误码
	 */
	private boolean isUseRtmapError;

	/**
	 * 判断是否已经初始化
	 */
	private boolean isInit = false;

	private LocationApp() {
		mLbsSign = MIX;// 默认混合定位
		lock = new ReentrantLock();
		lockServerAddress = new ReentrantLock();
	}

	/**
	 * 得到定位实例
	 * 
	 * @return
	 */
	public static LocationApp getInstance() {
		if (instance == null) {
			synchronized (LocationApp.class) {
				if (instance == null) {
					instance = new LocationApp();
				}
			}
		}
		return instance;
	}

	/**
	 * 定位初始化
	 * 
	 * @param context
	 */
	@SuppressLint("NewApi")
	public void init(Context context) {
		if (isInit) {
			return;
		} else {
			isInit = true;
		}
		EXECUTOR = Executors.newCachedThreadPool();
		loadLocationData();
		mContext = context;
		GatherData.getInstance().setContext(context);
		isWifiopen = WifiSensor.getInstance().init(context);
		RMLog.i("rtmap", "wifi网络是否开启：" + isWifiopen);
		boolean isNetOpen = PhoneManager.isNetworkConnected(context);
		RMLog.i("rtmap", "网络是否连接：" + isNetOpen);
		PackageManager pm = context.getPackageManager();
		boolean permission = (PackageManager.PERMISSION_GRANTED == pm
				.checkPermission("android.permission.ACCESS_FINE_LOCATION",
						context.getPackageName()));
		if (permission) {
			GpsSensor.getInstance().init(context);
		}
		SensorsLogic.getInstance().setSensor(SensorType.ACCELEROMETER, true);
		SensorsLogic.getInstance().setSensor(SensorType.MAGNETOMETER, true);
		SensorsLogic.getInstance().setSensor(SensorType.BAROMETER, true);// 打开气压计
		String key = RMConfig.getMetaData(context, RMFileUtil.RTMAP_KEY);
		if (!RMStringUtils.isEmpty(key)) {
			apiKey = key;
		}
		RMUser user = RMSqlite.getInstance().getUser();
		RMConfig.mac = (user == null || RMStringUtils.isEmpty(user.getLbsid())) ? PhoneManager
				.getMac(context) : user.getLbsid().substring(0, 12);
		RMConfig.imei = PhoneManager.getIMEI(context);
		RMConfig.pakageName = context.getPackageName();
		RMConfig.deviceType = PhoneManager.getPhoneType();
		SensorsLogic.getInstance().setContext(context);
		boolean isBTscan = BeaconSensor.isSuportBeacon(context);
		RMLog.i("rtmap", "蓝牙硬件是否支持扫描: " + isBTscan);
		if (isBTscan) {
			isBlueOpen = BeaconSensor.getInstance().init(context);
			RMLog.i("rtmap", "蓝牙是否开启：" + isBlueOpen);
		}
		JNILocation
				.Init(RMFileUtil.getBuildJudgeDir(),
						getXmlLicense(apiKey, RMVersionLocation.VERSION,
								RMConfig.pakageName, RMConfig.mac,
								RMConfig.deviceType));
		// RtmapLbsService.setStopTime();
		RMDownloadFingerUtil.updateWifiBuildJudgeFile(null);
		RMDownloadFingerUtil.updateBeaconBuildJudgeFile(null);
		if (user == null
				|| Long.valueOf(user.getExpiration_time())
						- System.currentTimeMillis() < 0) {// 没有用户或者有效期过期
			// 获取用户ID
			RMUserUtil.getUserInfo(context, apiKey, new OnGetUserListener() {

				@Override
				public void onGetUser(RMUser result) {
					userWork(result);
				}
			});
		} else {
			userWork(user);
		}
	}

	private static String getXmlLicense(String key, String version,
			String packageName, String mac, String deviceType) {
		String ret = "<Identify><key>" + key + "</key><pn>" + packageName
				+ "</pn><uid>" + mac + "</uid><dm>" + mac + "</dm><v>"
				+ version + "</v><dp>" + deviceType + "</dp></Identify>";
		return ret;
	}

	private boolean isUpload;// 正在上传吗

	private void userWork(RMUser user) {
		if (!isUpload && user.getIsbadlog_return() == 1) {// 如果没有上传，则开启
			uploadLogFile();
		}
		if (user.getIsphone_whitelist() == 1) {// 有白名单我就请求
			final SharedPreferences share = mContext.getSharedPreferences(
					RMFileUtil.WHITE_LIST, 0);
			String white = share.getString("white", null);// 得到白名单
			long whiteTime = share.getLong("whiteTime", 0);
			boolean isWhite = false;
			if (!RMStringUtils.isEmpty(white) && whiteTime != 0) {// 说明本地没有白名单
				iBeaconClass.BlueList = RMWhiteUtil.getWhite(white);
				if (System.currentTimeMillis() - whiteTime > 24 * 3600 * 1000) {
					isWhite = true;
				}
			} else {
				isWhite = true;
			}
			if (isWhite) {
				RMWhiteUtil.getWhiteInfo(apiKey, new OnGetWhiteListener() {
					@Override
					public void onGetWhite(String result) {
						share.edit().putString("white", result).commit();
						share.edit()
								.putLong("whiteTime",
										System.currentTimeMillis()).commit();
						iBeaconClass.BlueList = RMWhiteUtil.getWhite(result);
					}
				});
			}
		}
	}

	/**
	 * 上传log日志
	 */
	private void uploadLogFile() {
		isUpload = true;
		RMAsyncTask.EXECUTOR.execute(new Runnable() {

			@Override
			public void run() {
				RMFileUtil.createPath(RMFileUtil.getLogDir());// 创建log文件夹
				String logpath = RMFileUtil.getLogDir() + "location-log-"
						+ RMConfig.mac + ".txt";
				ArrayList<String> mLogList = new ArrayList<String>();
				File file = new File(logpath);
				try {
					if (file.exists()) {
						InputStreamReader sr = new InputStreamReader(
								new FileInputStream(file), "utf-8");
						BufferedReader reader = new BufferedReader(sr);
						String tmp;
						while ((tmp = reader.readLine()) != null) {
							mLogList.add(tmp);
						}
						sr.close();
						reader.close();
						if (mLogList.size() >= 10) {
							String str_log_switch = RMHttpUtil.connInfo(
									RMHttpUtil.GET, RMHttpUrl.LOG_SWITCH);
							if (!RMStringUtils.isEmpty(str_log_switch)
									&& !RMHttpUtil.NET_ERROR
											.equals(str_log_switch)) {
								JSONObject obj = new JSONObject(str_log_switch);
								if ("1".equals(obj.getString("capability"))) {// 可以上传日志
									Date date = new Date(System
											.currentTimeMillis());
									SimpleDateFormat format = new SimpleDateFormat(
											"yyyyMMddHHmm");
									String new_logpath = RMFileUtil.getLogDir()
											+ "location_log_"
											+ format.format(date) + "_"
											+ RMConfig.mac;
									boolean isCopy = RMFileUtil.copyFile(
											logpath, new_logpath + ".txt",
											false);
									if (isCopy) {// 如果拷贝成功
										File new_log = new File(new_logpath
												+ ".txt");
										File new_log_zip = new File(new_logpath
												+ ".zip");
										RMFileUtil
												.zipFile(new_log, new_log_zip);
										String upload_str = RMHttpUtil
												.uploadFile(
														new_log_zip,
														RMHttpUrl.UPLOAD_LOG_FILE);
										new_log_zip.delete();
										new_log.delete();
										if (!RMStringUtils.isEmpty(upload_str)
												&& !RMHttpUtil.NET_ERROR
														.equals(upload_str)) {
											obj = new JSONObject(upload_str);
											if ("0".equals(obj.getJSONObject(
													"result").getString(
													"error_code"))) {
												file.delete();
											}
										}
									}
								}
							}
						}
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				isUpload = false;
			}
		});
	}

	/**
	 * 是否设置为测试地址，默认是正式
	 * 
	 * @param isTest
	 *            true为测试，false为正式，默认是正式
	 */
	public void setTestStatus(boolean isTest) {
		if (isTest) {
			lockServerAddress.lock();
			RMHttpUrl.FILE_INFO_URL = "http://lbsdata.rtmap.com:8099/open2project/fileinfor/getFileInfor";
			RMHttpUrl.DOWNLOAD_URL = "http://lbsdata.rtmap.com:8099/open2project/fileinfor/downloadFile";
			RMHttpUrl.UPLOAD_ADDR = "http://lbsdata.rtmap.com:8099/open2project/fileinfor/uploadFile";
			setServerAddress("42.96.128.76", "18192");
			RMLog.i("rtmap", "已经修改为测试服务器");
			loadLocationData();
			lockServerAddress.unlock();
		} else {
			RMAsyncTask.EXECUTOR.execute(new Runnable() {

				@Override
				public void run() {
					lockServerAddress.lock();
					String ip = getIP("position.rtmap.com");
					if (!RMStringUtils.isEmpty(ip)) {
						RMHttpUrl.FILE_INFO_URL = "http://lbsdata.rtmap.com:8091/open2project/fileinfor/getFileInfor";
						RMHttpUrl.DOWNLOAD_URL = "http://lbsdata.rtmap.com:8091/open2project/fileinfor/downloadFile";
						RMHttpUrl.UPLOAD_ADDR = "http://lbsdata.rtmap.com:8091/open2project/fileinfor/uploadFile";
						setServerAddress(ip, "18092");
					}
					RMLog.i("rtmap", "已经修改为正式服务器");
					loadLocationData();
					lockServerAddress.unlock();
				}
			});
		}
	}

	private String getIP(String name) {
		InetAddress address = null;
		try {
			address = InetAddress.getByName(name);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		if (address != null) {
			return address.getHostAddress().toString();
		}
		return null;
	}

	/**
	 * 得到请求间隔时间
	 * 
	 * @return 请求间隔时间
	 */
	public int getRequestSpanTime() {
		return mRequestSpanTime;
	}

	/**
	 * 设置请求间隔时间，默认是1000ms
	 * 
	 * @param mRequestSpanTime
	 *            间隔时间，单位毫秒ms
	 */
	public void setRequestSpanTime(int mRequestSpanTime) {
		if (mRequestSpanTime >= 1000)
			this.mRequestSpanTime = mRequestSpanTime;
	}

	/**
	 * 得到智慧图API_KEY,此方法需要在初始化init方法调用之后使用
	 * ，初始化中我们会加载AndroidManifest.xml中的RTMAP_KEY
	 * 
	 * @return 智慧图API_KEY
	 */
	public String getApiKey() {
		return apiKey;
	}

	/**
	 * 设置智慧图API_KEY
	 * 
	 * @param apiKey
	 *            智慧图API_KEY
	 */
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	/**
	 * 得到定位标记，LocationApp.MIX 混合模式 LocationApp.OFFLINE 离线模式 LocationApp.ONLINE
	 * 在线模式
	 * 
	 * @return 定位模式
	 */
	public int getLbsSign() {
		return mLbsSign;
	}

	/**
	 * 得到context，前提是必须调用了init方法
	 * 
	 * @return context
	 */
	public Context getContext() {
		return mContext;
	}

	public void setPersistentMotionEnable() {
		JNILocation.SetPersistentMotionEnable();
	}

	public void setPersistentMotionDisable() {
		JNILocation.SetPersistentMotionDisable();
	}

	/**
	 * 设置定位模式，LocationApp.MIX 混合模式 LocationApp.OFFLINE 离线模式 LocationApp.ONLINE
	 * 在线模式
	 * 
	 * @param mLbsSign
	 *            请使用LocationApp的常量参数，不要自定义
	 */
	public void setLbsSign(int mLbsSign) {
		this.mLbsSign = mLbsSign;
	}

	/**
	 * 注册定位监听器
	 * 
	 * @param listener
	 *            具体使用请查看RMLocationListener
	 */
	public void registerLocationListener(RMLocationListener listener) {
		if (listener != null) {
			LocateList.add(listener);
		}
	}

	/**
	 * 得到扫描信息，包含了蓝牙与wifi扫描的信息
	 * 
	 * @return 扫描信息
	 */
	public String getScannerInfo() {
		return mScannerInfo;
	}

	/**
	 * 设置扫描信息
	 * 
	 * @param mScannerInfo
	 *            蓝牙与wifi扫描的信息
	 */
	public void setScannerInfo(String mScannerInfo) {
		this.mScannerInfo = mScannerInfo;
	}

	/**
	 * 移除定位监听器，建议页面关闭时或者关闭功能时候使用
	 * 
	 * @param listener
	 *            具体使用请查看RMLocationListener
	 */
	public void unRegisterLocationListener(RMLocationListener listener) {
		if (listener != null && LocateList.contains(listener)) {
			LocateList.remove(listener);
		}
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		JNILocation.clear();
	}

	/**
	 * 设置定位主文件名，默认是rtmap
	 * 
	 * @param folderName
	 *            文件名即可，例"rtmap",或者"rtmap/test"这样的方式也都OK
	 */
	public boolean setRootFolder(String folderName) {
		if (folderName != null && !folderName.equals("")) {
			RMFileUtil.LOCATION_FILEROOT = folderName;
			RMAsyncTask.EXECUTOR.execute(new Runnable() {

				@Override
				public void run() {
					JNILocation.setFingerPath(RMFileUtil.getFingerDir());
					JNILocation.setMapPath(RMFileUtil.getMapDir());
				}
			});
			return true;
		}
		return false;
	}

	/**
	 * 设置是否使用智慧图错误码
	 * 
	 * @param isUseRtmapError
	 *            true：使用智慧图错误码，false：不使用
	 */
	public void setUseRtmapError(boolean isUseRtmapError) {
		this.isUseRtmapError = isUseRtmapError;
	}

	private String userid;

	/**
	 * 设置第三方应用用户的唯一ID
	 * 
	 * @param userid
	 *            用于我方快速核对定位错误信息
	 */
	public void getUserid(String userid) {
		if (userid != null) {
			userid = userid.replaceAll("#", "");
			userid = userid.replaceAll("\\$", "");
		}
		this.userid = userid;
	}

	/**
	 * 返回第三方用户唯一ID
	 * 
	 * @return 第三方用户唯一ID
	 */
	public String getUserid() {
		return userid;
	}

	/**
	 * 添加围栏监听器
	 * 
	 * @param listener
	 *            围栏监听器，参见{@link OnFenceListener}
	 */
	public void addFenceListener(OnFenceListener listener) {
		if (listener != null) {
			FenceList.add(listener);
		}
	}

	/**
	 * 移除围栏监听器
	 * 
	 * @param listener
	 *            围栏监听器，参见{@link OnFenceListener}
	 */
	public void removeFenceListener(OnFenceListener listener) {
		if (FenceList.contains(listener)) {
			FenceList.remove(listener);
		}
	}

	private int mCount;

	/**
	 * 处理定位结果
	 * 
	 * @param location
	 *            具体使用请查看RMLocation
	 */
	public void onReceive(RMLocation location) {
		if (location.getError() == 0 && location.getCoordX() == 0
				&& location.getCoordY() == 0) {
			location.error = -1;
		}
		if (!isUseRtmapError) {// 如果使用智慧图错误码
			if (location.error == 1001 || location.error == 6010
					|| location.error == 6011 || location.error == 6016) {
				location.error = 1;
			} else if (location.error == 1004) {
				location.error = 3;
			} else if (location.error == 3018 || location.error == 3020) {
				location.error = 4;
			} else if (location.error == 3017 || location.error == 6012
					|| location.error == 6013) {
				location.error = 5;
			} else if (location.error == 601) {
				location.error = 601;
			} else if (location.error == 5004) {
				location.error = 11;
			} else if (location.error == 0) {
				location.error = 0;
			} else if (location.error == -1) {
				location.error = -1;
			} else {
				location.error = 2;
			}
		}
		if (location.error == 0) {
			if (mCurrentLocation == null) {
				mCount = 0;
			} else {
				if (location.getBuildID().equals(mCurrentLocation.getBuildID())
						&& location.floorID == mCurrentLocation.floorID) {
					mCount++;
				} else {
					mCount = 0;
				}
			}
			if (mCount == 4) {
				for (OnFenceListener listener : FenceList) {
					FenceInfo info = new FenceInfo();
					info.setBuildId(location.getBuildID());
					info.setFloor(location.floor);
					listener.onFenceListener(info);
				}
			}
		}
		location.setFloor(RMStringUtils.floorTransform(location.getFloorID()));
		location.setX(location.getCoordX() / 1000.0f);
		location.setY(location.getCoordY() / 1000.0f);
		RMUser user = RMSqlite.getInstance().getUser();
		if (user != null) {
			location.setLbsid(user.getLbsid());
		}
		mCurrentLocation = location;
		for (int i = 0; i < LocateList.size(); i++) {
			LocateList.get(i).onReceiveLocation(location);
		}
	}

	/**
	 * 当开启定位后，非回调可以满足的业务，可以通过此方法获取定位结果
	 * 
	 * @return 定位结果，参照RMLocation
	 */
	public RMLocation getCurrentLocation() {
		return mCurrentLocation;
	}

	/**
	 * 搜索定位点附近的POI，如果传入location，那么则返回离此位置最近的3个POI，如果不传入，则自动采用定位位置
	 * 
	 * @param location
	 *            定位点（可使用定位返回结果或者自定义）
	 * @return 是否允许搜索，当位置信息空或者错误码不为0，则不允许搜索，返回false
	 */
	public boolean getLocatePoiInfo(RMLocation location,
			OnSearchPoiListener onSearchPoiListener) {
		if (location == null) {
			location = mCurrentLocation;
		}
		if (location == null || location.error != 0) {
			return false;
		}
		RMSearchLbsPoiUtil.searchLbsPoi(apiKey, location, onSearchPoiListener);
		return true;
	}

	/**
	 * 定位是否启动
	 * 
	 * @return 是否启动，如果已经start()，则返回true;
	 */
	public boolean isStartLocate() {
		return isStartLocate;
	}

	/**
	 * 开启定位，没有配置key则无法开启
	 * 
	 * @return 如果没有key,开启失败，返回false
	 */
	public boolean start() {
		if (RMStringUtils.isEmpty(apiKey)) {
			return false;
		}
		// GpsSensor.getInstance().start();
		isStartLocate = true;
		Intent intent = new Intent(mContext, RtmapLbsService.class);
		mContext.startService(intent);
		return true;
	}

	/**
	 * 停止定位
	 * 
	 * @return 停止定位成功返回true
	 */
	public boolean stop() {
		isStartLocate = false;
		Intent intent = new Intent(mContext, RtmapLbsService.class);
		intent.putExtra("stop", 1);
		mContext.startService(intent);
		return true;
	}

	/**
	 * 设置地图角度,用于PDR推算，配合地图使用，推算的定位结果更加精确
	 * 
	 * @param mapAngle
	 *            角度，单位：度
	 */
	public void setMapAngle(float mapAngle) {
		this.mapAngle = mapAngle;
	}

	/**
	 * 得到地图角度
	 * 
	 * @return 角度，单位：度
	 */
	public float getMapAngle() {
		return mapAngle;
	}

	/**
	 * 设置服务器地址
	 * 
	 * @param ip
	 *            ip地址
	 * @param port
	 *            端口号
	 */
	public void setServerAddress(String ip, String port) {
		JNILocation.setServerAddress(ip, port);
	}

	/**
	 * 加载定位数据
	 */
	private void loadLocationData() {
		RMAsyncTask.EXECUTOR.execute(new Runnable() {

			@Override
			public void run() {
				lock.lock();
				RMLog.i("rtmap", "开始加载定位数据。。");
				JNILocation.clear();
				JNILocation.setFingerPath(RMFileUtil.getFingerDir());
				JNILocation.setMapPath(RMFileUtil.getMapDir());
				RMLog.i("rtmap", "加载定位数据完成。。");
				lock.unlock();
			}
		});
	}

	/**
	 * 设置地理坐标
	 * 
	 * @param longitude
	 *            经度
	 * @param latitude
	 *            纬度
	 */
	public void setGpsCoordinate(double longitude, double latitude) {
		GatherData.longitude = longitude;
		GatherData.latitude = latitude;
	}

	public void setWifiopen(boolean isWifiopen) {
		this.isWifiopen = isWifiopen;
	}

	public int getWifiopen() {
		return isWifiopen ? 1 : 0;
	}

	public void setBlueOpen(boolean isBlueOpen) {
		this.isBlueOpen = isBlueOpen;
	}

	public int getBlueOpen() {
		return isBlueOpen ? 1 : 0;
	}
}