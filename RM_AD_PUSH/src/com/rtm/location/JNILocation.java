package com.rtm.location;

public class JNILocation {

	static {
		System.loadLibrary("IndoorLoc");
	}

	// 获取so库版本
	public static native String getSoVersion();

	// 定位库初始化（beacon解密key以及license）
	public native static void Init(byte key[], String _licenseXml);

	// 设置服务器地址
	public static native void setServerAddress(String ip, String port);

	// 发送定位请求，不可主线程中调用
	public static native String serverInput(String _xmlStr);

	// 定位返回结果，不可在主线程中调用
	public static native String serverOutput();

	// 获取定位结果
	public static native String getPosition(int locate_type);

	// 设置地图匹配文件路径
	public static native int setMapPath(String map_Path);

	// 设置指纹文件路径
	public static native int setFingerPath(String fingerPath);

	// 加载建筑物判断文件
	public static native int loadBuildJudge();

	// 加载指纹文件
	public static native int loadFinger(String bid);

	// 解密beacon
	public native static int GetUuidMajorMinor(byte uuid[], byte major[],
			byte minor[]);

	// 获取beacon电量信息
	public native static int Electricity(byte electricity);

	// 退出时清理so库
	public static native int clear();

	// 获取定位库输出结果（未经过外围so库pdr推算的定位结果）
	public static native String getCorePosition(int type);

	// 被动定位用，告知定位库最新被动定位结果
	public static native int setLocResult(String locResult, int delay);

	// 获取当前的指南针角度 ;返回方向值0-360，与正北方向逆时针夹角 ;返回值为-1时为错误值
	public static native int getDirection();

	public static native void setIntegrationTimes(int integration_times);

	// 获取离线定位解算使用的信标
	public static native String getLocateTags();

}
