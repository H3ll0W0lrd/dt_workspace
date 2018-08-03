package com.rtmap.locationdemo.ar;

import java.lang.reflect.Field;

import android.content.Context;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.rtm.common.model.RMLocation;

public class ArUtils {

	// 视角范围
	public final int eyeDegree = 60;
	// 视角范围外转过多少度让poi不可见
	public final int eyeDegreeOutScreen = 60;
	// 默认显示的最大距离
	public final Float defaultDisplayMaxDisance = 100f;
	// 默认显示的最小距离
	public final Float defaultDisplayMinDisance = 0f;
	// 由于计算的是中心点的距离，所以需要减去这个距离表示到门口的距离
	public final Float defaultDisplayTargetDisance = 5f;
	// 判断距离在这个范围内就显示在附近
	public final Float defaultIsNearDisance = 10f;
	// 由于poi显示的太多会影响体验，所以屏幕上显示这么多个poi
	public final int defaultShowPOICount = 10;

	private static volatile ArUtils instance = null;

	/**
	 * 得到该类对象的一个单例
	 **/
	public static ArUtils getInstance() {
		if (instance == null) {
			if (instance == null) {
				instance = new ArUtils();
			}
		}
		return instance;
	}

	// 调整方向传感器获取的值
	public float normalizeDegree(double degree) {
		return (float) ((degree + 720) % 360);
	}

	/**
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

	/**
	 * 方法描述 : arShowView的单位是毫米 创建者：BrillantZhao_rtmap 版本： v1.0 创建时间： 2014-5-22
	 * 下午2:06:27
	 *
	 * @param location
	 * @param arShowView
	 * @return Float
	 */
	public float getDegreeBetweenWithThround(RMLocation location,
			ArShowView arShowView) {
		float x = location.x-arShowView.getPoiTargetX();
		float y = Math.abs(location.y) - Math.abs(arShowView.getPoiTargetY());
		// Log.i("rtmap",
		// "x差值："+arShowView.getPoiTargetX()+"   "+arShowView.getPoiTargetY()+"    "+location.getTargetX()+"     "+location.getTargetY()+"    y差值："+y+"    "+Math.atan(y
		// / x));
		return (float) Math.toDegrees(Math.atan(y / x));
	}

	public Float getTargetDistanceInARShow(ArManager.ArLocation location,
			float x, float y) {
		return (float) (Math.sqrt((location.getTargetY() - y / 1000)
				* (location.getTargetY() - y / 1000)
				+ (x / 1000 - location.getTargetX())
				* (x / 1000 - location.getTargetX())) - defaultDisplayTargetDisance);
	}

	/**
	 * 获取状态栏高度
	 * 
	 * @param context
	 * @return
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
	 * 提供一个静态方法，用于根据手机方向获得相机预览画面旋转的角度
	 *
	 * @return 返回角度
	 */
	public static int getPreviewDegree(int rotation) {
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
