package com.rtm.location;

/**
 * 读取so库的解析数据
 * 
 * @author dingtao
 *
 */
public class JNILocation {

	static {
		System.loadLibrary("RtmapOnlineLoc");
	}

	/**
	 * 加载beacons.bei
	 * 
	 * @param build_id
	 */
	public static native void loadBeaconsInfo(String build_id);

	/**
	 * 获取so库版本
	 * 
	 * @return so库版本
	 */
	public static native String getSoVersion();

	/**
	 * 定位库初始化（beacon解密key以及license）
	 * 
	 * @param key
	 *            beacon解密key
	 * @param _licenseXml
	 *            key的xml封装
	 */
	public native static void init(String jfolder_path, String _licenseXml);

	/**
	 * 设置服务器地址
	 * 
	 * @param ip
	 *            ip地址，例：192.168.1.1
	 * @param port
	 *            端口号，例：8080
	 */
	public static native void setServerAddress(String ip, String port);

	/**
	 * 发送定位请求，不可主线程中调用
	 * 
	 * @param _xmlStr
	 *            XML形式的数据，包含包名，key，ap，beacon等
	 * @return 暂时没用
	 */
	public static native String serverInput(String _xmlStr);

	/**
	 * 定位返回结果，不可在主线程中调用
	 * 
	 * @return 暂时没用
	 */
	public static native String serverOutput();

	/**
	 * 获取定位结果
	 * 
	 * @param locate_type
	 *            定位类型：0是在线，1是离线
	 * @return JSON形式的定位结果
	 */
	public static native String getPosition(int locate_type);

	/**
	 * 退出时清理so库数据
	 * 
	 * @return 暂时没用
	 */
	public static native int clear();
	
	/**
	 * 设置日志记录
	 * @param joptions
	 */
	public static native void setOption(String joptions);

	/**
	 * 解密C91B类型beacon
	 * @param juuid_2bytes
	 * @param jmajor
	 * @param jminor
	 * @return
	 */
	public static native int decryptBeacon(String juuid_2bytes,
			Object jmajor, Object jminor);
	public static native int crash();
}
