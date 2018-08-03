package com.rtm.frm.utils;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build.VERSION;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.rtm.common.model.POI;
import com.rtm.frm.R;
import com.rtm.frm.XunluApplication;
import com.rtm.frm.dialogfragment.UpdateAlertDialogFragment;
import com.rtm.frm.fragment.controller.MyFragmentManager;
import com.rtm.frm.model.Version;
import com.rtm.frm.service.UpdateVersionService;

@SuppressLint("ShowToast")
public class XunluUtil {
	/**
	 * 
	 * 方法描述 : 字符串判空,若 空true/非空false
	 * 
	 * @param s
	 * @return boolean
	 */
	public static boolean isEmpty(String s) {
		if (s == null || s.length() == 0 || s.equals("null")) {
			return true;
		}
		return false;
	}

	public static int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	public static int px2dip(Context context, float px) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (px / scale + 0.5f);
	}

	public static int getTextWidth(Paint paint, String text) {
		int width = 0;
		if (text != null) {
			int size = text.length();
			float[] widths = new float[size];
			paint.getTextWidths(text, widths);
			for (int i = 0; i < size; i++) {
				width += Math.ceil(widths[i]);
			}
		}

		return width;
	}

	public static String getSubString(String string, int maxLength) {
		if (string == null || maxLength <= 0) {
			return null;
		}
		int length = string.length();
		if (length > maxLength) {
			return String.format("%s%s", string.substring(0, maxLength), "…");
		} else {
			return string;
		}
	}

	@SuppressLint("DefaultLocale")
	public static String getFloor(int floor) {
		boolean isUp = ((floor / 10000 == 2) ? true : false);
		floor = floor % 10000;
		boolean isSharpFloor = (floor % 10 == 0);
		if (isUp) {
			if (isSharpFloor) {
				return String.format("F%d", floor / 10);
			} else {
				return String.format("F%.1f", floor / 10f);
			}
		} else {
			if (isSharpFloor) {
				return String.format("B%d", Math.abs(floor) / 10);
			} else {
				return String.format("B%.1f", Math.abs(floor) / 10f);
			}
		}
	}

	public static String toMd5(String s) {
		try {
			if (isEmpty(s)) {
				return null;
			}
			byte[] bytes = s.getBytes("UTF-8");
			MessageDigest algorithm = MessageDigest.getInstance("MD5");
			algorithm.reset();
			algorithm.update(bytes);
			return toHexString(algorithm.digest());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String toMd5(InputStream in) {
		if (in == null) {
			return null;
		}

		try {
			byte[] buffer = new byte[1024];
			int number = 0;
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			while ((number = in.read(buffer)) > 0) {
				md5.update(buffer, 0, number);
			}
			in.close();
			return toHexString(md5.digest());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private static String toHexString(byte[] bytes) {
		StringBuilder hexString = new StringBuilder();
		for (byte b : bytes) {
			hexString.append(Integer.toHexString(0xFF & b));
		}
		return hexString.toString();
	}

	public static Bitmap decodeBitmap(Context context, int resId) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),
				resId, options);
		return bitmap;
	}

	public static Bitmap decodeBitmap(int resId) {
		return decodeBitmap(XunluApplication.mApp, resId);
	}

	public static Bitmap decodeBitmap(String file) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		return BitmapFactory.decodeFile(file, options);
	}

	public static Bitmap decodeBitmap(byte[] data) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		return BitmapFactory.decodeByteArray(data, 0, data.length, options);
	}

	public static int getEquipmentWidth(Context context) {
		return context.getResources().getDisplayMetrics().widthPixels;
	}

	public static int getEquipmentHeight(Context context) {
		return context.getResources().getDisplayMetrics().heightPixels;
	}

	public static boolean checkUpdateTime() {
		int lastUpdateTime = PreferencesUtil.getInt(
				ConstantsUtil.PREFS_UPDATE_TIME, -1);
		if (lastUpdateTime == -1
				|| (System.currentTimeMillis() / 1000 / 60 / 60 - lastUpdateTime) > 24) {
			return true;
		}

		return false;
	}

	public static void saveUpdateTime() {
		int lastUpdateTime = (int) (System.currentTimeMillis() / 1000 / 60 / 60);
		PreferencesUtil.putInt(ConstantsUtil.PREFS_UPDATE_TIME, lastUpdateTime);
	}

	public static void installApk(Context context, String path) {
		if (XunluUtil.isEmpty(path)) {
			return;
		}

		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);
		File file = FileUtil.getFile(path);
		String type = "application/vnd.android.package-archive";
		intent.setDataAndType(Uri.fromFile(file), type);
		context.startActivity(intent);
	}

	public static void hideSoftKeyPad(Context context, View view) {
		try {
			InputMethodManager inputManager = (InputMethodManager) context
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			inputManager.hideSoftInputFromWindow(view.getWindowToken(),
					InputMethodManager.HIDE_NOT_ALWAYS);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void showSoftKeyPad(Context context, View view) {
		try {
			InputMethodManager inputManager = (InputMethodManager) context
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			inputManager.showSoftInput(view,
					InputMethodManager.RESULT_UNCHANGED_SHOWN);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean isUpdate() {
		int lastUpdateTime = PreferencesUtil.getInt(
				ConstantsUtil.PREFS_UPDATE_TIME, -1);
		if (lastUpdateTime == -1
				|| (System.currentTimeMillis() / 1000 / 60 / 60 - lastUpdateTime) > 24) {
			return true;
		}

		return false;
	}

	public static boolean equalsLocale(String language) {
		Locale locale = XunluApplication.mApp.getResources().getConfiguration().locale;
		return (locale.getLanguage().equals(language));
	}

	public static void killApp(boolean killSafely) {
		if (killSafely) {
			// System.runFinalizersOnExit(true);
			System.exit(0);
		} else {
			android.os.Process.killProcess(android.os.Process.myPid());
		}
	}

	// 去除+86，空格或者-的电话号码
	public static String getFormatNumber(String number) {
		if (number.startsWith("+86")) {
			number = number.substring(number.indexOf("+86") + "+86".length());
		}

		return number.replace(" ", "").replace("-", "");
	}

	/**
	 * 
	 * 方法描述 : 判断前面是否包含在后者中 创建者：veekenwong 版本： v1.0 创建时间： 2014-1-21 上午11:54:15
	 * 
	 * @param obj
	 * @param objs
	 * @return boolean
	 */
	public static boolean contains(Object obj, Object[] objs) {
		int length = objs.length;
		for (int i = 0; i < length; i++) {
			if (objs[i].equals(obj)) {
				return true;
			}
		}
		return false;
	}

	// /**
	// *
	// * 方法描述 : 创建者：brillantzhao 版本： v1.0 创建时间： 2014-4-4 下午3:11:26
	// *
	// * @param context
	// * @param poiName
	// * @return int
	// */
	// public static int getResourceID(Context context, String poiName,
	// ArrayList<com.rtm.frm.map.data.POI> couponPOIs, int poiID) {
	// int resourceID = R.drawable.arguide_endpoibg;
	//
	// // 电梯等公共设施
	// if (StaticData.AR_MAP.containsKey(poiName)) {
	// resourceID = XunluUtil.getDrawableIdByName(context,
	// StaticData.AR_MAP.get(poiName));
	// }
	// // 商家自己的map
	// if (com.rtm.frm.map.utils.Constants.LOGO_MAP.containsKey(poiName)) {
	// resourceID = XunluUtil.getDrawableIdByName(context,
	// com.rtm.frm.map.utils.Constants.LOGO_MAP.get(poiName)
	// .toString());
	// }
	//
	// // 优惠信用卡团购的图片
	// if (couponPOIs != null && poiID != 0) {
	// for (int j = 0; j < couponPOIs.size(); j++) {
	// POI couponPOI = couponPOIs.get(j);
	// if (couponPOI.getId() == poiID) {
	// String type = couponPOI.getType() + "";
	// if (type.equals(Coupon.TYPE_CREDIT_CARD)) {
	// // 信用卡
	// resourceID = R.drawable.ar_xinicon;
	// break;
	// } else if (type.equals(Coupon.TYPE_COUPON)) {
	// // 优惠
	// resourceID = R.drawable.ar_huiicon;
	// break;
	// } else if (type.equals(Coupon.TYPE_GROUPON)) {
	// // 团购
	// resourceID = R.drawable.ar_tuanicon;
	// break;
	// }
	// }
	// }
	// }
	// return resourceID;
	// }

	/**
	 * 
	 * 方法描述 : 根据资源名称获取资源id 创建者：brillantzhao 版本： v1.0 创建时间： 2014-3-17 下午1:05:56
	 * 
	 * @param paramContext
	 * @param paramString
	 * @return int
	 */
	public static int getDrawableIdByName(Context paramContext,
			String paramString) {
		if (paramString == null) {
			return R.drawable.arguide_endpoibg;
		}
		return paramContext.getResources().getIdentifier(paramString,
				"drawable", paramContext.getPackageName());
	}

	/**
	 * 
	 * 方法描述 : 获取状态栏的高度 创建者：BrillantZhao_rtmap 版本： v1.0 创建时间： 2014-5-22 下午2:27:30
	 * 
	 * @param context
	 * @return int
	 */
	public static int getStatusBarHeight(Context context) {
		Class<?> c = null;
		Object obj = null;
		Field field = null;
		int x = 0, statusBarHeight = 0;
		try {
			c = Class.forName("com.android.internal.R$dimen");
			obj = c.newInstance();
			field = c.getField("status_bar_height");
			x = Integer.parseInt(field.get(obj).toString());
			statusBarHeight = context.getResources().getDimensionPixelSize(x);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return statusBarHeight;
	}

	/**
	 * 
	 * 方法描述 : 判断是否有SD卡 创建者：brillantzhao 版本： v1.0 创建时间： 2014-3-28 下午8:56:41
	 * 
	 * @return String
	 */
	public static String getSDPath() {
		File SDdir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
		if (sdCardExist) {
			SDdir = Environment.getExternalStorageDirectory();
		}
		if (SDdir != null) {
			return SDdir.toString();
		} else {
			return null;
		}
	}

	/**
	 * /** 获取手机的mac地址
	 * 
	 * @param mContext
	 *            设备上下文
	 * @return 本机的mac
	 */
	@SuppressLint("DefaultLocale")
	public static String getMac() {
		WifiManager wifi = (WifiManager) XunluApplication.mApp
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifi.getConnectionInfo();
		String macAddress = info.getMacAddress();
		if (macAddress != null) {
			return macAddress.toUpperCase();
		} else {
			return "000000000000";
		}
	}

	/**
	 * 以最省内存的方式读取本地资源的图片
	 * 
	 * @param context
	 * @param resId
	 * @return
	 */
	public static Bitmap getBitMap(Context context, int resId) {
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inPreferredConfig = Bitmap.Config.RGB_565;
		opt.inPurgeable = true;
		opt.inInputShareable = true;
		// 获取资源图片
		InputStream is = context.getResources().openRawResource(resId);
		return BitmapFactory.decodeStream(is, null, opt);
	}

	/**
	 * 
	 * 方法描述 : 判断某个传感器是否存在 创建者：BrillantZhao_rtmap 版本： v1.0 创建时间： 2014-5-20
	 * 下午1:07:56
	 * 
	 * @param context
	 * @param sensorType
	 * @return boolean
	 */
	public static boolean isSensorValid(Context context, int sensorType) {
		boolean ret = false;
		SensorManager sm = (SensorManager) context
				.getSystemService(Context.SENSOR_SERVICE);
		List<Sensor> allSensors = sm.getSensorList(Sensor.TYPE_ALL);

		for (int i = 0; i < allSensors.size(); i++) {
			Sensor sensor = allSensors.get(i);
			if (sensorType == sensor.getType()) {
				ret = true;
			}
		}

		return ret;
	}

	/**
	 * @author liYan
	 * @explain 显示更新提示对话框
	 * @param context
	 * @param mVersion
	 */
	public static void showUpdate(final Context context, final Version mVersion) {
		final UpdateAlertDialogFragment alertDialogFragment = new 
				UpdateAlertDialogFragment(context.getResources().getString(R.string.title_message), mVersion.getNewFeatures());
		alertDialogFragment.setConfirmText("取消");
		alertDialogFragment.setCancelText("更新");
		alertDialogFragment.setConfirmOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PreferencesUtil.putBoolean("isShowUpdate",false);
				alertDialogFragment.dismiss();
			}
		});
		alertDialogFragment.setCancelOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startUpdate(context, mVersion);
				alertDialogFragment.dismiss();
			}
		});
		MyFragmentManager.showFragmentdialog(alertDialogFragment, MyFragmentManager.PRCOCESS_DIALOGFRAGMENT_ALERT, 
				MyFragmentManager.DIALOGFRAGMENT_ALERT);
		
//		Dialog mWarningDialog = new AlertDialog.Builder(context)
//				.setTitle(R.string.title_message)
//				.setMessage(mVersion.getNewFeatures())
//				.setPositiveButton(android.R.string.ok,
//						new DialogInterface.OnClickListener() {
//							@Override
//							public void onClick(DialogInterface arg0, int arg1) {
//								startUpdate(context, mVersion);
//							}
//						})
//				.setNegativeButton(android.R.string.cancel,
//						new DialogInterface.OnClickListener() {
//
//							@Override
//							public void onClick(DialogInterface dialog,
//									int which) {
//								PreferencesUtil.putBoolean("isShowUpdate",
//										false);
//							}
//						}).create();
//		mWarningDialog.show();
	}

	/**
	 * 
	 * 方法描述 : 检测SD卡存储状态，启动服务开始版本更新 创建者：veekenwong 版本： v1.0 创建时间： 2014-1-21
	 * 上午11:08:08 void
	 */
	private static void startUpdate(Context context, Version mVersion) {
		if (!FileUtil.checkExternalStorageState()) {
			Toast.makeText(context, R.string.error_sd_error, Toast.LENGTH_LONG);
			return;
		}
		Intent updateIntent = new Intent(context, UpdateVersionService.class);
		updateIntent.putExtra("app_name",
				context.getResources().getString(R.string.app_name));
		updateIntent.putExtra("downloadURL", mVersion.getNewClientUrl());
		context.startService(updateIntent);
	}

	/**
	 * @explain 获取当前日期
	 * @return 时间字符串
	 */
	public static String getCurrentDate() {
		String today = "";
		int y, m, d;
		Calendar cal = Calendar.getInstance();
		y = cal.get(Calendar.YEAR);
		today += y;
		m = cal.get(Calendar.MONTH);
		if (++m < 10) {
			today = today + "0" + m;
		} else {
			today = today + m;
		}
		d = cal.get(Calendar.DATE);
		today += d;
		return today;
	}

	public static void DeleteFile(File file) {
		if (file.exists() == false) {
			// TODO do something maketoast
			return;
		} else {
			if (file.isFile()) {
				file.delete();
				return;
			}
			if (file.isDirectory()) {
				File[] childFile = file.listFiles();
				if (childFile == null || childFile.length == 0) {
					file.delete();
					return;
				}
				for (File f : childFile) {
					DeleteFile(f);
				}
				file.delete();
			}
		}
	}

	/**
	 * 
	 * 方法描述 : 创建者：brillantzhao 版本： v1.0 创建时间： 2014-4-4 下午3:11:26
	 * 
	 * @param context
	 * @param poiName
	 * @return int
	 */
	public static int getResourceID(Context context, String poiName,
			ArrayList<POI> couponPOIs, int poiID) {
		int resourceID = R.drawable.arguide_endpoibg;

		// 电梯等公共设施
		if (StaticData.AR_MAP.containsKey(poiName)) {
			resourceID = XunluUtil.getDrawableIdByName(context,
					StaticData.AR_MAP.get(poiName));
		}
		return resourceID;
	}

	/**
	 * @author LiYan
	 * @date 2014-9-10 下午7:12:01  
	 * @explain 计算百度经纬度两点距离,返回单位KM
	 * @return double
	 * @param lat_a
	 * @param lng_a
	 * @param lat_b
	 * @param lng_b
	 * @return 
	 */
	public static double distanceByLatLng(double lat_a, double lng_a,
			double lat_b, double lng_b) {
		double pk = 180 / 3.14169;
		double a1 = lat_a / pk;
		double a2 = lng_a / pk;
		double b1 = lat_b / pk;
		double b2 = lng_b / pk;
		double t1 = Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math.cos(b2);
		double t2 = Math.cos(a1) * Math.sin(a2) * Math.cos(b1) * Math.sin(b2);
		double t3 = Math.sin(a1) * Math.sin(b1);
		double tt = Math.acos(t1 + t2 + t3) * 6366000;
		tt = ((int)tt) / 1000.0;
		return tt;
	}
	
	/**
     * 用来判断服务是否运行.
     * @param context
     * @param className 判断的服务名字
     * @return true 在运行 false 不在运行
     */
    public static boolean isServiceRunning(Context mContext,String className) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager)
        mContext.getSystemService(Context.ACTIVITY_SERVICE); 
        List<ActivityManager.RunningServiceInfo> serviceList  = activityManager.getRunningServices(30);
        if (!(serviceList.size()>0)) {
            return false;
        }
        for (int i=0; i<serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(className) == true) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }
    
    public static void scanWifi(Context mContext) {
		// 取得WifiManager对象
		WifiManager	mWifiManager = (WifiManager) mContext
						.getSystemService(Context.WIFI_SERVICE);
		if (mWifiManager.isWifiEnabled()) {
			mWifiManager.startScan();
		}
    }
    
    /**
	 * 隐藏软键盘
	 */
	public static void hideKeyboard(Activity activity) {
		InputMethodManager manager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (activity.getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
			if (activity.getCurrentFocus() != null)
				manager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}
	
	public static double twoPointLineDis(int x1,int y1,int x2,int y2) {
		double dis = 0;
//		Math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2));
		dis = Math.abs(Math.sqrt((x1-x2)^2+(y1-y2)^2));
		return dis;
	}
	
	/**
	 * @explain 将楼层字符串，找到默认楼层
	 * @param floors
	 * @return
	 */
	public static String getDefaultFloor(String floors) {
		String[] floorsArray = floors.split("_");
		for(int i = 0; i < floorsArray.length;++i) {
			if(i+1 < floorsArray.length) {
				if(floorsArray[i+1].charAt(0) == 'B') {
					return floorsArray[i];
				} 
			}
		}
		return floorsArray[floorsArray.length-1];
	}
	
	public static void initImageLoader(Context context) {
		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
				.cacheInMemory().cacheOnDisc().build();

		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				context).defaultDisplayImageOptions(defaultOptions)
				.threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()
				.discCacheFileNameGenerator(new Md5FileNameGenerator())
				.tasksProcessingOrder(QueueProcessingType.LIFO).build();
		ImageLoader.getInstance().init(config);
	}
	
	/**
	 * 蓝牙相关
	 * 是否有蓝牙
	 * */
	public static boolean isHaveBlueTooth(){
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(bluetoothAdapter == null) {
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * 蓝牙相关
	 * 蓝牙是否可用
	 * */
	public static boolean isBlueToothEnable(){
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(bluetoothAdapter != null &&bluetoothAdapter.isEnabled()){
			return true;
		}
		return false;
	}
	
	
	/**
	 * 蓝牙相关
	 * 打开蓝牙,不弹提示
	 * */
	
	@SuppressLint("NewApi") 
	public static void openBlueTooth(){
		 final BluetoothManager bluetoothManager =
	                (BluetoothManager)XunluApplication.mApp.getSystemService(Context.BLUETOOTH_SERVICE);
		 BluetoothAdapter  mBluetoothAdapter = bluetoothManager.getAdapter();
	        
	        // Checks if Bluetooth is supported on the device.
	        if (mBluetoothAdapter == null) {
	            return;
	        }
	        
	        if(mBluetoothAdapter.isEnabled()){
	        	return;
	        }
	        //开启蓝牙
	        mBluetoothAdapter.enable();
	}
	
	/**
	 * 蓝牙相关，
	 * 关闭蓝牙
	 * **/
	@SuppressLint("NewApi")
	public static void closeBlueTooth(){
		if(!XunluUtil.isHaveBlueTooth()||XunluUtil.getAndroidVersion() < 18){
			return;
		}
			final BluetoothManager bluetoothManager =
					(BluetoothManager)XunluApplication.getApp().getSystemService(Context.BLUETOOTH_SERVICE);
			BluetoothAdapter  mBluetoothAdapter = bluetoothManager.getAdapter();
			
			// Checks if Bluetooth is supported on the device.
			if (mBluetoothAdapter == null) {
				return;
			}
			//关闭蓝牙
			mBluetoothAdapter.disable();
	}
	
	/**判断当前用户手机的版本*/
	public static int getAndroidVersion(){
		return Integer.parseInt(VERSION.SDK);
	}
	
	/**返回程序版本号，如5.2.0*/
	public static String getVersionName() throws Exception {
		PackageManager packageManager = XunluApplication.getApp().getPackageManager();
		PackageInfo packInfo = packageManager.getPackageInfo(XunluApplication.getApp().getPackageName(), 0);
		String version = packInfo.versionName;
		return version;
	}
}
