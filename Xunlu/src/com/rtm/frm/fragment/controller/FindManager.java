package com.rtm.frm.fragment.controller;

import android.os.Handler;
import android.os.Message;

import com.rtm.common.model.POI;
import com.rtm.frm.XunluApplication;
import com.rtm.frm.utils.ConstantsUtil;

public class FindManager extends BaseManager {
	public static boolean isFindShowing = false;
	public static float center_x;// 我的位置的坐标
	public static float center_y;// 我的位置的坐标
	public static String mCurrentFloor;// 我所在的楼层，所有的优惠必须在同一楼层

	private volatile static FindManager mInstance;
	public static float mapDegree = 0.0f;// map相对于北方偏移的角度
	public static int width, height;// 屏幕宽高，高为去掉bar的高度
	public static int height_T;// 屏幕的整体高度，包括bar
	public static int statusBarHeight;// 屏幕bar的高度
	public int currentAngle = 0; // 扫描动画的旋转角度
	public static float compassAngle = 0;// 指南针当前角度
	float distance[]; // 我的位置和所有优惠点的距离
	Handler mHandler;
	public static float map_poi_x;// poi在屏幕上的坐标
	public static float map_poi_y;// poi在屏幕上的坐标
	public static float myDegree;// 建筑物和北方的夹角
	public static int quadrant;// 面对正北方向，建筑物相对于我的位置在第几象限，
	public static int radius = 1;// 圆半径
	public static float imgWidth = 0f;// poi图片宽度
	public static float imgHeight = 0f;// poi图片高度

	public POI nearestPOI;
	public float nearestDis;

	protected FindManager(XunluApplication app) {
		super(app);
		initManager();
	}

	@Override
	protected void initManager() {
	}

	@Override
	protected void DestroyManager() {
	}

	public static void setNullInstance() {
	}

	public static FindManager getInstance() {
		FindManager instance;
		if (mInstance == null) {
			synchronized (FindManager.class) {
				if (mInstance == null) {
					instance = new FindManager(XunluApplication.getApp());
					mInstance = instance;
				}
			}
		}
		return mInstance;
	}

	public void parseResult(Handler mHandler, final String result) {
		this.mHandler = mHandler;

	}

	private void sendMessage(int what) {
		// 通知addPOI
		Message msg = new Message();
		msg.what = what;
		mHandler.sendMessage(msg);
	}

	public static int i;
	public void getNearestPoi(){
		//poi的个数肯定不为0，为0会直接返回
		i = 0;
	}


	private float computeMyDegree() {
		if(nearestPOI == null){
			return 0.0f;
		}
		
		float poi_x = nearestPOI.getX();
		float poi_y = nearestPOI.getY();

		if (poi_x - center_x > 0 && poi_y - center_y > 0) {// 在第二象限
			myDegree = (float) (mapDegree + 90 + getDegree(poi_x, poi_y,
					center_x, center_y));
		} else if (poi_x - center_x > 0 && poi_y - center_y < 0) {// 在第一象限
			// 计算出夹角
			myDegree = (float) (mapDegree + 90 - getDegree(poi_x, poi_y,
					center_x, center_y));
		} else if (poi_x - center_x < 0 && poi_y - center_y > 0) {// 在第三象限
			myDegree = (float) (mapDegree + 270 - getDegree(poi_x, poi_y,
					center_x, center_y));
		} else if (poi_x - center_x < 0 && poi_y - center_y < 0) {// 在第四象限
			myDegree = (float) (mapDegree + 270 + getDegree(poi_x, poi_y,
					center_x, center_y));
		}
		return myDegree;
	}

	/**
	 * 注意：后面的角度，度数一定要记得转换成弧度。 此方法计算出图片在屏幕上的x，y坐标
	 **/
	public void updateXY() {
		map_poi_x = (float) (width / 2 - radius
				* Math.sin(Math.toRadians(compassAngle - myDegree)) - imgWidth / 2);
		map_poi_y = (float) (height / 2 - radius
				* Math.cos(Math.toRadians(compassAngle - myDegree)) - imgHeight / 2);
		
	}

	/**
	 * 
	 * @author hukunge
	 * @param float poi_x, float poi_y, float my_x, float my_y 获得两个poi点之间的距离
	 * */

	public float getDistance(float poi_x, float poi_y, float my_x,
			float my_y) {
		return (float) Math.sqrt((poi_x - my_x) * (poi_x - my_x)
				+ (poi_y - my_y) * (poi_y - my_y));
	}

	/**
	 * 
	 * @author hukunge
	 * @param float poi_x, float poi_y, float my_x, float my_y 获得两个poi点之间的角度，
	 *        返回值为正
	 * */
	private Float getDegree(float poi_x, float poi_y, float my_x,
			float my_y) {
		return (float) Math.abs(Math.toDegrees(Math.atan((my_y - poi_y)
				/ (my_x - poi_x))));
	}

	/**
	 * */
	public float getNearestDistance() {
		if (nearestPOI == null) {
			return -1f;
		}
		
		nearestDis = getDistance(nearestPOI.getX(), nearestPOI.getY(), center_x, center_y);
		
		return nearestDis;
	}

}
