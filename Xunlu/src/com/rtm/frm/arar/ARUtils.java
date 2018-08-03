package com.rtm.frm.arar;

import android.app.Activity;
import android.content.Context;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.rtm.frm.fragment.controller.MyFragmentManager;
import com.rtm.frm.model.Location;
import com.rtm.frm.model.POITargetInfo;
import com.rtm.frm.newframe.NewFrameActivity;
import com.rtm.frm.tab0.TestRtmapFragment;
import com.rtm.location.LocationApp;

public class ARUtils {

	// 视角范围
	public final int eyeDegree = 60;
	// 视角范围外转过多少度让poi不可见
	public final int eyeDegreeOutScreen = 60;
	// 默认显示的最大距离
	public final Float defaultDisplayMaxDisance = 50f;
	// 默认显示的最小距离
	public final Float defaultDisplayMinDisance = 0f;
	// 由于计算的是中心点的距离，所以需要减去这个距离表示到门口的距离
	public final Float defaultDisplayTargetDisance = 5f;
	// 判断距离在这个范围内就显示在附近
	public final Float defaultIsNearDisance = 10f;
	// 由于poi显示的太多会影响体验，所以屏幕上显示这么多个poi
	public final int defaultShowPOICount = 10;

	private static volatile ARUtils instance = null;
	public Float mapDegree = (float) 0;

	/** 得到该类对象的一个单例 **/
	public static ARUtils getInstance() {
		if (instance == null) {
			synchronized (LocationApp.class) {
				if (instance == null) {
					instance = new ARUtils();
				}
			}
		}
		return instance;
	}

	/**
	 * 
	 * 方法描述 : 创建者：BrillantZhao_rtmap 版本： v1.0 创建时间： 2014-5-23 上午10:34:12
	 * 
	 * @param context
	 *            void
	 */
	public void initARUtils(Context context) {
		TestRtmapFragment frag = NewFrameActivity.getInstance().getTab0();
		mapDegree = -(float) Math
				.toDegrees(frag.getMapView().getConfig().getDrawMap().getAngle());
	}

	/**
	 * 提供一个静态方法，用于根据手机方向获得相机预览画面旋转的角度
	 * 
	 * @param activity
	 * @return
	 */
	public int getPreviewDegree(Activity activity) {
		// 获得手机的方向
		int rotation = activity.getWindowManager().getDefaultDisplay()
				.getRotation();
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

	// 调整方向传感器获取的值
	public float normalizeDegree(float degree) {
		return (degree + 720) % 360;
	}

	/**
	 * 
	 * 方法描述 : 创建者：BrillantZhao_rtmap 版本： v1.0 创建时间： 2014-5-20 下午5:04:27
	 * 
	 * @param surfaceView
	 *            void
	 */
	@SuppressWarnings("deprecation")
	public void initSurfaceView(SurfaceView surfaceView) {
		surfaceView.getHolder()
				.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		surfaceView.getHolder().setFixedSize(1280, 780); // 设置Surface分辨率
		surfaceView.getHolder().setKeepScreenOn(true);// 屏幕常亮
	}

	// =============== ARGuide start =====================
	/**
	 * 
	 * 方法描述 : targetInfos的单位是米 创建者：BrillantZhao_rtmap 版本： v1.0 创建时间： 2014-5-22
	 * 下午2:08:05
	 * 
	 * @param location
	 * @param targetInfos
	 * @return Float
	 */
	public Float getDegreeBetween(Location location, POITargetInfo targetInfos) {
		return (float) Math
				.toDegrees(Math.atan((targetInfos.getPoiTargetY() - location
						.getY())
						/ (targetInfos.getPoiTargetX() - location.getX())));
	}

	/**
	 * 
	 * 方法描述 : 创建者：BrillantZhao_rtmap 版本： v1.0 创建时间： 2014-5-22 下午3:04:22
	 * 
	 * @param location
	 * @param arShowView
	 * @return Float
	 */
	public Float getDegreeBetween(Location location, ARShowView arShowView) {
		return (float) Math
				.abs(Math.toDegrees(Math.atan((location.getY() - arShowView
						.getPoiTargetY())
						/ (arShowView.getPoiTargetX() - location.getX()))));
	}

	/**
	 * 
	 * 方法描述 : 创建者：BrillantZhao_rtmap 版本： v1.0 创建时间： 2014-5-22 下午2:51:01
	 * 
	 * @param location
	 * @param arShowView
	 * @param degreeBetween
	 * @return Float
	 */
	public Float getTargetDegreeInARGuide(Location location,
			ARShowView arShowView, Float degreeBetween) {
		Float targetDegree = (float) 0;
		if ((location.getY() - arShowView.getPoiTargetY()) > 0
				&& (arShowView.getPoiTargetX() - location.getX()) < 0) {
			targetDegree = (float) (270 + mapDegree + degreeBetween);
		} else if ((location.getY() - arShowView.getPoiTargetY()) < 0
				&& (arShowView.getPoiTargetX() - location.getX()) > 0) {
			targetDegree = (float) (90 + mapDegree + degreeBetween);
		} else if ((location.getY() - arShowView.getPoiTargetY()) > 0
				&& (arShowView.getPoiTargetX() - location.getX()) > 0) {
			targetDegree = (float) (90 + mapDegree - degreeBetween);
		} else if ((location.getY() - arShowView.getPoiTargetY()) < 0
				&& (arShowView.getPoiTargetX() - location.getX()) < 0) {
			targetDegree = (float) (270 + mapDegree - degreeBetween);
		}
		return targetDegree;
	}

	/**
	 * 
	 * 方法描述 : 创建者：BrillantZhao_rtmap 版本： v1.0 创建时间： 2014-5-22 下午2:13:03
	 * 
	 * @param location
	 * @param targetInfos
	 * @param defaultDisplayTargetDisance
	 * @return Float
	 */
	public Float getTargentDistanceInARGuide(Location location,
			POITargetInfo targetInfos) {
		return (float) (Math.sqrt((targetInfos.getPoiTargetY() - location
				.getY())
				* (targetInfos.getPoiTargetY() - location.getY())
				+ (targetInfos.getPoiTargetX() - location.getX())
				* (targetInfos.getPoiTargetX() - location.getX())) - defaultDisplayTargetDisance);
	}

	/**
	 * 
	 * 方法描述 : 创建者：BrillantZhao_rtmap 版本： v1.0 创建时间： 2014-5-22 下午2:55:54
	 * 
	 * @param location
	 * @param arShowView
	 * @return Float
	 */
	public Float getTargentDistanceInARGuide(Location location,
			ARShowView arShowView) {
		return (float) (Math
				.sqrt((location.getY() - arShowView.getPoiTargetY())
						* (location.getY() - arShowView.getPoiTargetY())
						+ (arShowView.getPoiTargetX() - location.getX())
						* (arShowView.getPoiTargetX() - location.getX())) - defaultDisplayTargetDisance);
	}

	// =============== ARGuide end =====================
	// =============== ARShow start =====================
	/**
	 * 
	 * 方法描述 : arShowView的单位是毫米 创建者：BrillantZhao_rtmap 版本： v1.0 创建时间： 2014-5-22
	 * 下午2:06:27
	 * 
	 * @param location
	 * @param arShowView
	 * @return Float
	 */
	public Float getDegreeBetweenWithThround(Location location,
			ARShowView arShowView) {
		return (float) Math
				.abs(Math.toDegrees(Math.atan(Math.abs((location.getY() - arShowView
						.getPoiTargetY() / 1000))
						/ Math.abs((arShowView.getPoiTargetX() / 1000 - location
								.getX())))));
	}
	/**
	 * 
	 * 方法描述 : arShowView的单位是毫米 创建者：BrillantZhao_rtmap 版本： v1.0 创建时间： 2014-5-22
	 * 下午2:06:27
	 * 
	 * @param location
	 * @param arShowView
	 * @return Float
	 */
	public Float getDegreeBetweenWithThround(Location location,
			float x,float y) {
		return (float) Math
				.abs(Math.toDegrees(Math.atan(Math.abs((location.getY() -y / 1000))
						/ Math.abs((x/ 1000 - location
								.getX())))));
	}
	/**
	 * 
	 * 方法描述 : 创建者：BrillantZhao_rtmap 版本： v1.0 创建时间： 2014-5-22 下午2:59:30
	 * 
	 * @param location
	 * @param targetInfos
	 * @return Float
	 */
	public Float getDegreeBetweenWithThround(Location location,
			POITargetInfo targetInfos) {
		return (float) Math
				.toDegrees(Math.atan((targetInfos.getPoiTargetY() / 1000 - location
						.getY())
						/ (targetInfos.getPoiTargetX() / 1000 - location.getX())));
	}

	/**
	 * 
	 * 方法描述 : 创建者：BrillantZhao_rtmap 版本： v1.0 创建时间： 2014-5-22 下午2:15:08
	 * 
	 * @param location
	 * @param arShowView
	 * @param mapDegree
	 * @param degreeBetween
	 * @return Float
	 */
	public Float getTargetDegreeInARShow(Location location,
			ARShowView arShowView, Float degreeBetween) {
		Float targetDegree = (float) 0;
		if ((location.getY() - arShowView.getPoiTargetY() / 1000) > 0
				&& (arShowView.getPoiTargetX() / 1000 - location.getX()) < 0) {
			targetDegree = (float) (270 + mapDegree + degreeBetween);
		} else if ((location.getY() - arShowView.getPoiTargetY() / 1000) < 0
				&& (arShowView.getPoiTargetX() / 1000 - location.getX()) > 0) {
			targetDegree = (float) (90 + mapDegree + degreeBetween);
		} else if ((location.getY() - arShowView.getPoiTargetY() / 1000) > 0
				&& (arShowView.getPoiTargetX() / 1000 - location.getX()) > 0) {
			targetDegree = (float) (90 + mapDegree - degreeBetween);
		} else if ((location.getY() - arShowView.getPoiTargetY() / 1000) < 0
				&& (arShowView.getPoiTargetX() / 1000 - location.getX()) < 0) {
			targetDegree = (float) (270 + mapDegree - degreeBetween);
		}
		return targetDegree;
	}
	/**
	 * 
	 * 方法描述 : 创建者：BrillantZhao_rtmap 版本： v1.0 创建时间： 2014-5-22 下午2:15:08
	 * 
	 * @param location
	 * @param arShowView
	 * @param mapDegree
	 * @param degreeBetween
	 * @return Float
	 */
	public Float getTargetDegreeInARShow(Location location,
			float x,float y, Float degreeBetween) {
		Float targetDegree = (float) 0;
		if ((location.getY() - y / 1000) > 0
				&& (x/ 1000 - location.getX()) < 0) {
			targetDegree = (float) (270 + mapDegree + degreeBetween);
		} else if ((location.getY() - y/ 1000) < 0
				&& (x / 1000 - location.getX()) > 0) {
			targetDegree = (float) (90 + mapDegree + degreeBetween);
		} else if ((location.getY() - y / 1000) > 0
				&& (x/ 1000 - location.getX()) > 0) {
			targetDegree = (float) (90 + mapDegree - degreeBetween);
		} else if ((location.getY() - y/ 1000) < 0
				&& (x / 1000 - location.getX()) < 0) {
			targetDegree = (float) (270 + mapDegree - degreeBetween);
		}
		return targetDegree;
	}
	/**
	 * 
	 * 方法描述 : 创建者：BrillantZhao_rtmap 版本： v1.0 创建时间： 2014-5-22 下午2:43:26
	 * 
	 * @param location
	 * @param targetInfos
	 * @return Float
	 */
	public Float getTargetDistanceInARShow(Location location,
			POITargetInfo targetInfos) {
		return (float) (Math
				.sqrt((targetInfos.getPoiTargetY() / 1000 - location.getY())
						* (targetInfos.getPoiTargetY() / 1000 - location.getY())
						+ (targetInfos.getPoiTargetX() / 1000 - location.getX())
						* (targetInfos.getPoiTargetX() / 1000 - location.getX())) - defaultDisplayTargetDisance);
	}

	/**
	 * 
	 * 方法描述 : 创建者：BrillantZhao_rtmap 版本： v1.0 创建时间： 2014-5-22 下午3:01:18
	 * 
	 * @param location
	 * @param arShowView
	 * @return Float
	 */
	public Float getTargetDistanceInARShow(Location location,
			ARShowView arShowView) {
		return (float) (Math
				.sqrt((location.getY() - arShowView.getPoiTargetY() / 1000)
						* (location.getY() - arShowView.getPoiTargetY() / 1000)
						+ (arShowView.getPoiTargetX() / 1000 - location.getX())
						* (arShowView.getPoiTargetX() / 1000 - location.getX())) - defaultDisplayTargetDisance);
	}
	
	public Float getTargetDistanceInARShow(Location location,
			float x, float y) {
		return (float) (Math
				.sqrt((location.getY() - y / 1000)
						* (location.getY() - y/ 1000)
						+ (x / 1000 - location.getX())
						* (x/ 1000 - location.getX())) - defaultDisplayTargetDisance);
	}
	// =============== ARShow end =====================
}
