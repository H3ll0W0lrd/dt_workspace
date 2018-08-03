package com.rtm.frm.AR;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.view.Surface;
import android.view.View;

import com.rtm.frm.XunluApplication;
import com.rtm.frm.fragment.controller.BaseManager;
import com.rtm.frm.model.MyLocation;
import com.rtm.frm.model.NavigatePoint;
import com.rtm.frm.model.POITargetInfo;

@SuppressLint("NewApi")
public class ARTestManager extends BaseManager {
	//老版的数据
	public static List<POITargetInfo> targetInfos;
	
	private volatile static ARTestManager mInstance;

	// 由于计算的是中心点的距离，所以需要减去这个距离表示到门口的距离
	public final Float defaultDisplayTargetDisance = 5f;
	public static final int eyeDegree = 60;// 视角范围
	public static final int eyeDegreeOutScreen = 60;// 视角范围外转过多少度让poi不可见
	public static final int maxDisance = 100;// 默认显示的最大距离
	public static boolean isInARMode = false;// 进入AR模式标志位,用于不暂停定位
	public static Float mapDegree = 0f;
	public static int statusBarHeight; // 状态栏的高度
	public static int screenWidth;
	public static int screenHeight;
	public static float sensorX;// 当前角度
	public static float sensorY;// 当前角度
	public static float mapx = 1f;
	public static float mapy = 1f;
	
	
	
	public static int selected = -1;
	public static float navix = 1f;
	public static float naviy = 1f;

	public List<arPoiItem> arItemsList = new ArrayList<arPoiItem>();// 放AROverlayItem
	public List<arPoiItem> arNaviList = new ArrayList<arPoiItem>();// 放AR导航的点
	ArrayList<NavigatePoint> navigatePoints;//线路规划的点，从第二个点开始

	protected ARTestManager(XunluApplication app) {
		super(app);
	}

	public static ARTestManager getInstance() {
		ARTestManager instance;
		if (mInstance == null) {
			synchronized (ARTestManager.class) {
				if (mInstance == null) {
					instance = new ARTestManager(XunluApplication.getApp());
					mInstance = instance;
				}
			}
		}
		return mInstance;
	}

	public static void setNullInstance() {
		mInstance = null;
	}

	@Override
	protected void initManager() {
	}

	@Override
	protected void DestroyManager() {
	}

	//用来更新屏幕上的poi点的位置坐标
	public void updatePois() {
		// kunge.hu
//		for (arPoiItem item : arItemsList) {
		for (int i = 0 ;i < arItemsList.size() ; i++) {
			
			
			float targetDegree = getMyDegree(arItemsList.get(i).getX(), arItemsList.get(i).getY());

			// 因为旋转了固定的角度，需要在这里减去
			float targerDistance = getDistance(arItemsList.get(i).getX(), arItemsList.get(i).getY())
					- defaultDisplayTargetDisance;

			targetDegree = (targetDegree + 360) % 360;

			float modifyDegree = sensorX - targetDegree;
			if (modifyDegree >= 360 - eyeDegree / 2) {
				modifyDegree = modifyDegree - 360;
			} else if (modifyDegree <= (eyeDegree + eyeDegreeOutScreen) / 2 - 360) {
				modifyDegree = 360 + modifyDegree;
			}
			// 有选择的重画
			if (Math.abs(modifyDegree) <= (eyeDegree + eyeDegreeOutScreen) / 2) {

				mapx = (-modifyDegree + eyeDegree / 2) * (screenWidth / eyeDegree);
//				mapy = (PreferencesUtil.getInt("screenHeight", 1280)- statusBarHeight - targerDistance	* ((PreferencesUtil.getInt("screenHeight", 1280) - statusBarHeight) / 50));
//				mapy = (screenHeight- statusBarHeight - targerDistance * ((screenHeight - statusBarHeight) / defaultDisplayMaxDisance));
				float h = (screenHeight - screenHeight * ( targerDistance/ maxDisance));

					mapy = - h/30 * sensorY - 2*h;
				
//				//将位置坐标存起来，导航的时候用
//				arItemsList.get(i).setMapX(mapx);
//				arItemsList.get(i).setMapY(mapy);
				
				//设置view在屏幕上的显示位置
				arItemsList.get(i).getView().setX(mapx);
				arItemsList.get(i).getView().setY(mapy);
				arItemsList.get(i).getView().setVisibility(View.VISIBLE);
			} else {
				arItemsList.get(i).getView().setVisibility(View.GONE);
			}
		}
		
	}
	
	//用来更新被选中的点的位置坐标，根据这个坐标来画线
	public void updateSelect(){
//		ARTestManager.getInstance().navigatePoints
		
//		float targetDegree = getMyDegree(arItemsList.get(selected).getX(), arItemsList.get(selected).getY());
		float targetDegree = getMyDegree(ARTestManager.getInstance().navigatePoints.get(0).getX(), ARTestManager.getInstance().navigatePoints.get(0).getY());
		

		// 因为旋转了固定的角度，需要在这里减去
		float targerDistance = getDistance(arItemsList.get(selected).getX(), arItemsList.get(selected).getY())
				- defaultDisplayTargetDisance;

		targetDegree = (targetDegree + 360) % 360;

		float modifyDegree = sensorX - targetDegree;
		if (modifyDegree >= 360 - eyeDegree / 2) {
			modifyDegree = modifyDegree - 360;
		} else if (modifyDegree <= (eyeDegree + eyeDegreeOutScreen) / 2 - 360) {
			modifyDegree = 360 + modifyDegree;
		}
		
		
		navix = (-modifyDegree + eyeDegree / 2) * (screenWidth / eyeDegree);
//		float h = (screenHeight - screenHeight * ( targerDistance/ maxDisance));
//			naviy = - h/30 * sensorY - 2*h;
			naviy = - 0.04f*screenHeight * sensorY - 2.5f * screenHeight;//默认-90度时候在屏幕中心
	}

	// 计算poi相对于我的位置在北方的角度
	public static float getMyDegree(float poi_x, float poi_y) {
		float my_x = MyLocation.getInstance().getX();
		float my_y = MyLocation.getInstance().getY();

		float myDegree = 0f;
		if (poi_x - my_x > 0 && poi_y - my_y > 0) {// 在第二象限
			myDegree = (float) (mapDegree + 90 + getDegree(poi_x, poi_y, my_x,
					my_y));
		} else if (poi_x - my_x > 0 && poi_y - my_y < 0) {// 在第一象限
			myDegree = (float) (mapDegree + 90 - getDegree(poi_x, poi_y, my_x,
					my_y));
		} else if (poi_x - my_x < 0 && poi_y - my_y > 0) {// 在第三象限
			myDegree = (float) (mapDegree + 270 - getDegree(poi_x, poi_y, my_x,
					my_y));
		} else if (poi_x - my_x < 0 && poi_y - my_y < 0) {// 在第四象限
			myDegree = (float) (mapDegree + 270 + getDegree(poi_x, poi_y, my_x,
					my_y));
		}
		return myDegree;
	}

	/**
	 * 
	 * @author hukunge
	 * @param float poi_x, float poi_y, float my_x, float my_y 获得两个poi点之间的距离
	 * */

	public static float getDistance(float poi_x, float poi_y) {
		float my_x = MyLocation.getInstance().getX();
		float my_y = MyLocation.getInstance().getY();
		return (float) Math.sqrt((poi_x - my_x) * (poi_x - my_x)
				+ (poi_y - my_y) * (poi_y - my_y));
	}

	/**
	 * 
	 * @author hukunge
	 * @param float poi_x, float poi_y, float my_x, float my_y 获得两个poi点之间的角度，
	 *        返回值为正
	 * */
	private static Float getDegree(float poi_x, float poi_y, float my_x,
			float my_y) {
		return (float) Math.abs(Math.toDegrees(Math.atan((my_y - poi_y)
				/ (my_x - poi_x))));
	}
	
	/**
	 * 提供一个静态方法，用于根据手机方向获得相机预览画面旋转的角度
	 * 
	 * @return 返回角度
	 */
	public int getPreviewDegree(int rotation) {
		int degree = 0;
		// 根据手机的方向计算相机预览画面应该选择的角度
		switch (rotation) {
		case Surface.ROTATION_0:
			degree = 90;
			break;
		case Surface.ROTATION_90:
			degree = 0;
			break;
		case Surface.ROTATION_180:
			degree = 270;
			break;
		case Surface.ROTATION_270:
			degree = 180;
			break;
		}
		return degree;
	}

}
