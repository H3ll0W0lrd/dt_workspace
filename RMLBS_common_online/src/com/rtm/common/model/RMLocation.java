/**
 * 定位与地图公共model
 */
package com.rtm.common.model;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.rtm.common.utils.RMStringUtils;

import android.location.Location;

/**
 * 包含定位位置信息;V2.0版本y坐标为正数，V2.1版本以后，y坐标按照GIS坐标系标准改为负数。强烈建议开发者设置各个属性的时候使用set方法设置，例：
 * x和croodX都表示x轴位置，只是单位是米和毫米，使用set方法会自动分配值给两个属性
 */
public class RMLocation implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * InOutDoorFlg参数，位置未知
	 */
	public static final int LOC_UNKNOW = 0;
	/**
	 * InOutDoorFlg参数，室内
	 */
	public static final int LOC_INDOOR = 1;
	/**
	 * InOutDoorFlg参数，室外
	 */
	public static final int LOC_OUTDOOR = 2;

	/**
	 * 用户的唯一标示
	 */
	public String userID;

	/**
	 * 新版用户ID
	 */
	private String lbsid;
	/**
	 * 错误码，只有当错误码为0时定位才有效
	 */
	public int error = -1;
	/**
	 * 错误详细信息
	 */
	public String errorInfo;
	/**
	 * 室内外标志
	 */
	public int inOutDoorFlg;

	/**
	 * 建筑物ID
	 */
	public String buildID;
	/**
	 * 楼层,例：20100；如果需要得到String类型的数据，可以读取另外一个属性floor，或者使用RMStringUtils.
	 * floorTransform( int floor)得到
	 */
	public int floorID;

	/**
	 * 楼层:例：F2；如果需要整型数据，可以读取另外一个属性floorID，或者使用RMStringUtils.floorTransform(
	 * String floor)得到
	 */
	public String floor;
	/**
	 * 室内定位精度，单位：米
	 */
	public int accuracy;
	/**
	 * x轴坐标,单位：毫米；地图中设置一般都是米，所以在与地图相结合的时候，一定要特别注意:需要传入int型数据，单位都是毫米，
	 * float型数据单位都是米，请使用另外一个属性x或者coordX/1000f
	 */
	public int coordX;

	/**
	 * y轴坐标，单位：毫米；地图中设置一般都是米，所以在与地图相结合的时候，一定要特别注意:需要传入int型数据，单位都是毫米，
	 * float型数据单位都是米，请使用另外一个属性y或者coordY/1000f
	 */
	public int coordY;

	/**
	 * x坐标，单位：米；跟地图结合可以直接使用，我们提供毫米单位coordX和米单位x两个参数，请开发者灵活使用。
	 */
	public float x;
	/**
	 * y坐标，单位：米；跟地图结合可以直接使用，我们提供毫米单位coordY和米单位y两个参数，请开发者灵活使用。
	 */
	public float y;

	/**
	 * 无线定位时间
	 */
	public long timestamp;
	/**
	 * PDR推算时间
	 */
	public long timestampPDR;

	private String calculateType;// 推算类型

	/**
	 * 经度
	 */
	public double longitude;
	/**
	 * 纬度
	 */
	public double latitude;
	/**
	 * 海拔高度
	 */
	public double altitude;
	/**
	 * GPS精度
	 */
	public float gpsAccuracy;
	/**
	 * poi名字
	 */
	public String poiName;

	/**
	 * 标签参数，可以存放用户想要存放的对象
	 */
	public Object tag;

	/**
	 * 构造方法，包括属性初始化
	 */
	public RMLocation() {
		userID = "0";
		error = -1;
		errorInfo = "init";
		buildID = "";
		inOutDoorFlg = LOC_UNKNOW;
	}

	/**
	 * 得到RTMAP的用户ID，方便我们核查日志使用
	 * 
	 * @return RTMAP的用户ID
	 */
	public String getLbsid() {
		return lbsid;
	}

	/**
	 * 设置RTMAP的用户ID
	 * 
	 * @param lbsid
	 *            RTMAP的用户ID
	 */
	public void setLbsid(String lbsid) {
		this.lbsid = lbsid;
	}

	/**
	 * 构造方法，用于封装定位库返回值
	 * 
	 * @param result
	 *            RMLocation返回值
	 */
	public RMLocation(RMLocation result) {
		super();
		this.userID = result.userID;
		this.error = result.error;
		this.errorInfo = result.errorInfo;
		this.timestamp = result.timestamp;
		this.timestampPDR = result.timestampPDR;
		this.buildID = result.buildID;
		this.floorID = result.floorID;
		this.floor = RMStringUtils.floorTransform(floorID);
		this.accuracy = result.accuracy;
		this.coordX = result.coordX;
		this.x = coordX / 1000f;
		this.coordY = result.coordY;
		this.y = coordY / 1000f;
		this.inOutDoorFlg = result.inOutDoorFlg;
		this.longitude = result.longitude;
		this.latitude = result.latitude;
		this.altitude = result.altitude;
		this.gpsAccuracy = result.gpsAccuracy;
		this.calculateType = result.getCalculateType();
	}

	/**
	 * 得到楼层，例：F2；如果需要整型数据，可以读取另外一个属性floorID，或者使用RMStringUtils.floorTransform(
	 * String floor)得到
	 * 
	 * @return 楼层，例：F2
	 */
	public String getFloor() {
		return floor;
	}

	/**
	 * 设置楼层，请按照规范设置，例：F1、B1
	 * 
	 * @param floor
	 *            楼层，例：F1、B1
	 */
	public void setFloor(String floor) {
		this.floor = floor;
		this.floorID = RMStringUtils.floorTransform(floor);
	}

	/**
	 * 得到横向坐标，单位：米；跟地图结合可以直接使用，我们提供毫米单位coordX和米单位x两个参数，请开发者灵活使用。
	 * 
	 * @return 横向坐标，单位：米
	 */
	public float getX() {
		return x;
	}

	/**
	 * 设置横向坐标，单位：米；跟地图结合可以直接使用，我们提供毫米单位coordX和米单位x两个参数，请开发者灵活使用。
	 * 
	 * @param x
	 *            横向坐标，单位：米
	 */
	public void setX(float x) {
		this.x = x;
		this.coordX = (int) (x * 1000);
	}

	/**
	 * 设置横向坐标，单位：毫米；跟地图结合可以直接使用，我们提供毫米单位coordX和米单位x两个参数，请开发者灵活使用。
	 * 
	 * @param x
	 *            横向坐标，单位：毫米
	 */
	public void setCoordX(int coordX) {
		this.coordX = coordX;
		this.x = coordX / 1000f;
	}

	/**
	 * 得到纵向坐标，单位：毫米；跟地图结合可以直接使用，我们提供毫米单位coordY和米单位y两个参数，请开发者灵活使用。
	 * 
	 * @return 纵向坐标 单位：毫米
	 */
	public void setCoordY(int coordY) {
		this.coordY = coordY;
		this.y = coordY / 1000f;
	}

	/**
	 * 得到纵向坐标，单位：米；跟地图结合可以直接使用，我们提供毫米单位coordY和米单位y两个参数，请开发者灵活使用。
	 * 
	 * @return 纵向坐标 单位：米
	 */
	public float getY() {
		return y;
	}

	/**
	 * 设置纵向坐标，单位：米;跟地图结合可以直接使用，我们提供毫米单位coordY和米单位y两个参数，请开发者灵活使用。
	 * 
	 * @param y
	 *            纵向坐标，单位：米
	 */
	public void setY(float y) {
		this.y = y;
		this.coordY = (int) (y * 1000);
	}

	/**
	 * 解析定位结果信息
	 * 
	 * @param val
	 *            定位库输出数据
	 * @param isWifiEmpty
	 *            是否有wifi信息
	 * @param isBeaconEmpty
	 *            是否有beacon信息
	 * @param location
	 *            位置信息
	 * @param build
	 *            建筑物ID
	 * @return 是否解析成功
	 */
	public boolean decode_jsn(String val, Location location, int build) {
		boolean ret = true;

		try {
			JSONObject testJsonObject = new JSONObject(val);
			userID = testJsonObject.getString("uid");
			error = testJsonObject.getInt("error");
			buildID = testJsonObject.getString("build");
			floorID = testJsonObject.getInt("floor");
			coordX = testJsonObject.getInt("coord_x");
			coordY = -testJsonObject.getInt("coord_y");
			accuracy = testJsonObject.getInt("accuracy");
			timestamp = testJsonObject.getLong("timestamp");
			timestampPDR = testJsonObject.getLong("timestamp_pdr");
			calculateType = testJsonObject.getString("result_type");
			// 获取gps信息
			setGpsLocation(location);
			inOutDoorFlg = build;
		} catch (JSONException e) {
			e.printStackTrace();
			ret = false;
		}

		return ret;
	}

	public String getCalculateType() {
		return calculateType;
	}

	public void setCalculateType(String calculateType) {
		this.calculateType = calculateType;
	}

	/**
	 * 设置错误信息
	 * 
	 * @param errorInfo
	 *            错误信息
	 */
	public void setErrorInfo(String errorInfo) {
		this.errorInfo = errorInfo;
	}

	/**
	 * 得到错误信息
	 * 
	 * @return 错误信息，定位的详细信息
	 */
	public String getErrorInfo() {
		return errorInfo;
	}

	/**
	 * 设置GPS属性
	 * 
	 * @param gpsLocation
	 *            GPS定位效果
	 */
	public void setGpsLocation(Location gpsLocation) {
		if (gpsLocation != null) {
			this.gpsAccuracy = gpsLocation.getAccuracy();
			this.altitude = gpsLocation.getAltitude();
			this.latitude = gpsLocation.getLatitude();
			this.longitude = gpsLocation.getLongitude();
		}
	}

	/**
	 * 得到用户ID，每个使用定位的设备都相当于一个单独的用户
	 * 
	 * @return 用户ID
	 */
	public String getUserID() {
		return this.userID;
	}

	/**
	 * 得到定位错误码，非0错误码，请核对错误码表
	 * 
	 * @return 错误码
	 */
	public int getError() {
		return this.error;
	}

	/**
	 * 设置定位错误码，非0错误码，请核对错误码表
	 * 
	 * @param error
	 *            错误码
	 */
	public void setError(int error) {
		this.error = error;
	}

	/**
	 * 室内外标志参数
	 * 
	 * @return 室内外标志，包含：LOC_UNKNOW、LOC_INDOOR、LOC_OUTDOOR
	 */
	public int getInOutDoorFlg() {
		return this.inOutDoorFlg;
	}

	/**
	 * 设置建筑物ID
	 * 
	 * @param buildID
	 */
	public void setBuildID(String buildID) {
		this.buildID = buildID;
	}

	/**
	 * 得到建筑物ID
	 * 
	 * @return 建筑物ID
	 */
	public String getBuildID() {
		return this.buildID;
	}

	/**
	 * 得到楼层,例：20100；如果需要得到String类型的数据，可以读取另外一个属性floor，或者使用RMStringUtils.
	 * floorTransform( int floor)得到
	 * 
	 * @return 楼层ID，返回整型数据floor，例：20010
	 */
	public int getFloorID() {
		return this.floorID;
	}

	/**
	 * 设置楼层，请按照规范设置，例：20010、10010
	 * 
	 * @param floorID
	 *            楼层，例：20010、10010
	 */
	public void setFloorID(int floorID) {
		this.floorID = floorID;
		this.floor = RMStringUtils.floorTransform(floorID);
	}

	/**
	 * 定位成功之后，定位库会计算出本次定位精度
	 * 
	 * @return 室内定位精度，单位：米
	 */
	public int getAccuracy() {
		return this.accuracy;
	}

	/**
	 * 设置定位精度
	 * 
	 * @param accuracy
	 *            室内定位精度，单位：米
	 */
	public void setAccuracy(int accuracy) {
		this.accuracy = accuracy;
	}

	/**
	 * x轴坐标,单位：毫米；地图中设置一般都是米，所以在与地图相结合的时候，一定要特别注意:需要传入int型数据，单位都是毫米，
	 * float型数据单位都是米，请使用另外一个属性x或者coordX/1000f
	 * 
	 * @return x轴坐标，单位：毫米
	 */
	public int getCoordX() {
		return this.coordX;
	}

	/**
	 * y轴坐标，单位：毫米；地图中设置一般都是米，所以在与地图相结合的时候，一定要特别注意:需要传入int型数据，单位都是毫米，
	 * float型数据单位都是米，请使用另外一个属性y或者coordY/1000f
	 * 
	 * @return y轴坐标，单位：毫米
	 */
	public int getCoordY() {
		return this.coordY;
	}

	/**
	 * @return 无线定位时间
	 */
	public long getTimestamp() {
		return this.timestamp;
	}

	/**
	 * 得到PDR推算时间
	 * 
	 * @return PDR推算时间
	 */
	public long getTimestampPDR() {
		return this.timestampPDR;
	}

	/**
	 * 得到大楼经度
	 * 
	 * @return 经度
	 */
	public double getLongitude() {
		return this.longitude;
	}

	/**
	 * 得到大楼纬度
	 * 
	 * @return 纬度
	 */
	public double getLatitude() {
		return this.latitude;
	}

	/**
	 * 得到海拔高度
	 * 
	 * @return 海拔高度
	 */
	public double getAltitude() {
		return this.altitude;
	}

	/**
	 * 得到GPS精度
	 * 
	 * @return GPS精度
	 */
	public float getGpsAccuracy() {
		return this.gpsAccuracy;
	}

	/**
	 * 得到标签值，此参数的值由用户指定
	 * 
	 * @return Object对象
	 */
	public Object getTag() {
		return tag;
	}

	/**
	 * 设置标签值，此参数请随意指定
	 * 
	 * @param tag
	 *            Object对象
	 */
	public void setTag(Object tag) {
		this.tag = tag;
	}

}
