package com.rtmap.wifipicker.wifi;

/**
 * 离线时数据采集，指纹点信息
 * @author hotstar
 *
 */
public class CollectFingerPoint {
	/** x坐标 单位米 **/
	public float sPointX;
	/** y坐标 单位米 **/
	public float sPointY;
	/** 指纹点id **/
	public String sPointFingerId = "";
	/** 楼层信息 **/
	public int sPointFloor = 1;
	/** 扫描次数 **/
	public int sPointScanCount = 50;
	/** 是否去除重复采集，true为可重复 **/
	public boolean sCanRepeated = false;

	public static int sPointmidfloor = 0;
	public static int sPointGain = 0;
	public static int sPointgruond = 1;

	public String onGetPointId() {
	    return String.format("%05d%05d%05d", sPointFloor , (int)sPointX , (int)sPointY);
	}
}
