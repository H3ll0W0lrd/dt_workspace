package com.rtm.frm.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.graphics.Bitmap;

import com.rtm.common.model.POI;
import com.rtm.frm.model.XunluLocation;

public class StaticData {

	// 导航页的导航图片
	public static Bitmap navigateBitmap = null;
	// 导航页选择的终点
	public static POI navigateEndPOI = null;
	// 导航页选择的终点所在的建筑
	public static String navigateBuild = null;

	// 导航页返回时需要使用的
	public static POI navigate_poi = null;
	public static XunluLocation navigate_start = null;

	// 优惠或者团购信息里展示使用的list
	public static List<String> galleryURLList = new ArrayList<String>();

	public static Map<String, String> AR_MAP = new HashMap<String, String>() {
		/**
		 * @Fields serialVersionUID : （用一句话描述这个变量表示什么）
		 */

		private static final long serialVersionUID = 1L;
		{
			put("电梯", "ar_shopelevator");
			put("自动扶梯", "ar_shopescalator");
			put("卫生间", "ar_shoptoilet");
			put("洗手间", "ar_shoptoilet");
			put("电话机", "ar_shopphone");
			put("楼梯", "ar_shopstairs");
			put("ATM", "ar_shopatm");
			put("问讯处", "ar_shopinquiry");
			put("收银台", "ar_shopcash");
			put("出入口", "ar_shopgate");
		}
	};

	/** 重力感应X轴 Y轴 Z轴的重力值 **/
	public static float mGX = 0;
	public static float mGY = 0;
	public static float mGZ = 0;

	// 判断是否提示过切换建筑物
	public static boolean isHadShowSwitchBuildDialog = false;

	// 判断是否提示过切换城市
	public static boolean isHadShowSwitchCityDialog = false;

	// 设定延迟时间
	public static final int interval = 300;

	/**
	 * 
	 * 方法描述 : 分享里面的语句 创建者：BrillantZhao_rtmap 版本： v1.0 创建时间： 2014-6-16 下午3:30:07
	 * 
	 * @param floor
	 * @return String
	 */
	public static String changeToFloor(String floor) {
		String changeFloor = "";
		////////////////优化之后
		if(floor.startsWith("B")){
			changeFloor = "地下" + floor.trim().substring(1) + "层";
		}else if(floor.startsWith("F")){
			changeFloor =  floor.trim().substring(1) + "层";
		}
		return changeFloor + "的";
	}
}
