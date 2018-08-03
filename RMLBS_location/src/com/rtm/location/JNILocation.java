package com.rtm.location;

/**
 * 读取so库的解析数据
 * 
 * @author dingtao
 *
 */
public class JNILocation {

	static {
		System.loadLibrary("IndoorLoc");
	}
	
	/**
	 * 关闭设备移动状态：依赖手机传感器数据，判断设备动静状态，默认为关闭状态
	 */
	public static native void SetPersistentMotionDisable();
	/**
	 * 设置设备为移动状态：不依赖手机传感器数据，判断设备动静状态
	 */
    public static native void SetPersistentMotionEnable(); 

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
	public native static void Init(String jfolder_path, String _licenseXml);

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
	 * 设置地图匹配约束文件路径：约束文件主要是防止定位点计算到地图之外
	 * 
	 * @param map_Path
	 *            地图匹配文件路径
	 * @return 暂时没用
	 */
	public static native int setMapPath(String map_Path);

	/**
	 * 设置指纹文件路径：指纹文件夹主要包括建筑物判断文件和定位计算文件
	 * 
	 * @param fingerPath
	 *            指纹文件路径
	 * @return 暂时没用
	 */
	public static native int setFingerPath(String fingerPath);

	/**
	 * 加载建筑物判断文件
	 * 
	 * @return 暂时没用
	 */
	public static native int loadBuildJudge();

	/**
	 * 加载指纹定位文件
	 * 
	 * @param bid
	 *            建筑物id
	 * @return 暂时没用
	 */
	public static native int loadFinger(String bid);


	/**
	 * 退出时清理so库数据
	 * 
	 * @return 暂时没用
	 */
	public static native int clear();

	/**
	 * 获取定位库输出结果（未经过外围so库pdr推算的定位结果）
	 * 
	 * @param type
	 * @return
	 */
	public static native String getCorePosition(int type);

	/**
	 * 被动定位用，告知定位库最新被动定位结果
	 * 
	 * @param locResult
	 *            被动定位结果
	 * @param delay
	 *            延迟时间
	 * @return
	 */
	public static native int setLocResult(String locResult, int delay);

	/**
	 * 获取当前的指南针角度 ;返回方向值0-360，与正北方向逆时针夹角 ;返回值为-1时为错误值
	 * 
	 * @return 角度值，数学上的角度值还是
	 */
	public static native int getDirection();

	/**
	 * 
	 * @param integration_times
	 */
	public static native void setIntegrationTimes(int integration_times);

	/**
	 * 获取离线定位解算使用的信标
	 * 
	 * @return
	 */
	public static native String getLocateTags();
	
	public static native void InitBykey(String juuid_2bytes, int jmajor, byte[] jkey);

	public static native int GetUuidMajorMinor(String juuid_2bytes, Object jmajor, Object jminor);
	
	public static native void SetPdrDisable();
	public static native void SetPdrEnable();

}
