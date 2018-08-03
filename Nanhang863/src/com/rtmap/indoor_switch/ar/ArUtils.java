package com.rtmap.indoor_switch.ar;

import android.app.Activity;
import android.content.Context;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.lang.reflect.Field;

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
    public Float mapDegree = (float) 0;

    /**
     * 得到该类对象的一个单例
     **/
    public static ArUtils getInstance() {
        if (instance == null) {
//			synchronized (LocationApp.class) {
            if (instance == null) {
                instance = new ArUtils();
            }
//			}
        }
        return instance;
    }

    /***
     * 初始化工具类，初始化地图偏转角
     *
     * @param context
     * @param mapDegree
     */
    public void initARUtils(Context context, float mapDegree) {
        this.mapDegree = -(float) Math.toDegrees(mapDegree);
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
     * 方法描述 : 创建者：BrillantZhao_rtmap 版本： v1.0 创建时间： 2014-5-20 下午5:04:27
     *
     * @param surfaceView void
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
     * 方法描述 : 创建者：BrillantZhao_rtmap 版本： v1.0 创建时间： 2014-5-22 下午3:04:22
     *
     * @param location
     * @param arShowView
     * @return Float
     */
    public Float getDegreeBetween(ArManager.ArLocation location, ArShowView arShowView) {
        return (float) Math
                .abs(Math.toDegrees(Math.atan((location.getTargetY() - arShowView
                        .getPoiTargetY())
                        / (arShowView.getPoiTargetX() - location.getTargetX()))));
    }

    /**
     * 方法描述 : 创建者：BrillantZhao_rtmap 版本： v1.0 创建时间： 2014-5-22 下午2:51:01
     *
     * @param location
     * @param arShowView
     * @param degreeBetween
     * @return Float
     */
    public Float getTargetDegreeInARGuide(ArManager.ArLocation location,
                                          ArShowView arShowView, Float degreeBetween) {
        Float targetDegree = (float) 0;
        if ((location.getTargetY() - arShowView.getPoiTargetY()) > 0
                && (arShowView.getPoiTargetX() - location.getTargetX()) < 0) {
            targetDegree = (float) (270 + mapDegree + degreeBetween);
        } else if ((location.getTargetY() - arShowView.getPoiTargetY()) < 0
                && (arShowView.getPoiTargetX() - location.getTargetX()) > 0) {
            targetDegree = (float) (90 + mapDegree + degreeBetween);
        } else if ((location.getTargetY() - arShowView.getPoiTargetY()) > 0
                && (arShowView.getPoiTargetX() - location.getTargetX()) > 0) {
            targetDegree = (float) (90 + mapDegree - degreeBetween);
        } else if ((location.getTargetY() - arShowView.getPoiTargetY()) < 0
                && (arShowView.getPoiTargetX() - location.getTargetX()) < 0) {
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
     * @return Float
     */
//	public Float getTargentDistanceInARGuide(Location location,
//			POITargetInfo targetInfos) {
//		return (float) (Math.sqrt((targetInfos.getPoiTargetY() - location
//				.getY())
//				* (targetInfos.getPoiTargetY() - location.getY())
//				+ (targetInfos.getPoiTargetX() - location.getX())
//				* (targetInfos.getPoiTargetX() - location.getX())) - defaultDisplayTargetDisance);
//	}

    /**
     * 方法描述 : 创建者：BrillantZhao_rtmap 版本： v1.0 创建时间： 2014-5-22 下午2:55:54
     *
     * @param location
     * @param arShowView
     * @return Float
     */
    public Float getTargentDistanceInARGuide(ArManager.ArLocation location,
                                             ArShowView arShowView) {
        return (float) (Math
                .sqrt((location.getTargetY() - arShowView.getPoiTargetY())
                        * (location.getTargetY() - arShowView.getPoiTargetY())
                        + (arShowView.getPoiTargetX() - location.getTargetX())
                        * (arShowView.getPoiTargetX() - location.getTargetX())) - defaultDisplayTargetDisance);
    }

    // =============== ARGuide end =====================
    // =============== ARShow start =====================

    /**
     * 方法描述 : arShowView的单位是毫米 创建者：BrillantZhao_rtmap 版本： v1.0 创建时间： 2014-5-22
     * 下午2:06:27
     *
     * @param location
     * @param arShowView
     * @return Float
     */
    public Float getDegreeBetweenWithThround(ArManager.ArLocation location,
                                             ArShowView arShowView) {
        return (float) Math
                .abs(Math.toDegrees(Math.atan(Math.abs((location.getTargetY() - arShowView
                        .getPoiTargetY() / 1000))
                        / Math.abs((arShowView.getPoiTargetX() / 1000 - location
                        .getTargetX())))));
    }

    /**
     * 方法描述 : arShowView的单位是毫米 创建者：BrillantZhao_rtmap 版本： v1.0 创建时间： 2014-5-22
     * 下午2:06:27
     *
     * @param location
     * @return Float
     */
    public Float getDegreeBetweenWithThround(ArManager.ArLocation location,
                                             float x, float y) {
        return (float) Math
                .abs(Math.toDegrees(Math.atan(Math.abs((location.getTargetY() - y / 1000))
                        / Math.abs((x / 1000 - location
                        .getTargetX())))));
    }

    /**
     * 方法描述 : 创建者：BrillantZhao_rtmap 版本： v1.0 创建时间： 2014-5-22 下午2:15:08
     *
     * @param location
     * @param arShowView
     * @param degreeBetween
     * @return Float
     */
    public Float getTargetDegreeInARShow(ArManager.ArLocation location,
                                         ArShowView arShowView, Float degreeBetween) {
        Float targetDegree = (float) 0;
        if ((location.getTargetY() / 1000 - arShowView.getPoiTargetY()) > 0
                && (arShowView.getPoiTargetX() - location.getTargetX() / 1000) < 0) {
            targetDegree = (float) (270 + mapDegree + degreeBetween);
        } else if ((location.getTargetY() / 1000 - arShowView.getPoiTargetY()) < 0
                && (arShowView.getPoiTargetX() - location.getTargetX() / 1000) > 0) {
            targetDegree = (float) (90 + mapDegree + degreeBetween);
        } else if ((location.getTargetY() / 1000 - arShowView.getPoiTargetY()) > 0
                && (arShowView.getPoiTargetX() - location.getTargetX() / 1000) > 0) {
            targetDegree = (float) (90 + mapDegree - degreeBetween);
        } else if ((location.getTargetY() / 1000 - arShowView.getPoiTargetY()) < 0
                && (arShowView.getPoiTargetX() - location.getTargetX() / 1000) < 0) {
            targetDegree = (float) (270 + mapDegree - degreeBetween);
        }
        return targetDegree;
    }

    /**
     * 方法描述 : 创建者：BrillantZhao_rtmap 版本： v1.0 创建时间： 2014-5-22 下午2:15:08
     *
     * @param location
     * @param degreeBetween
     * @return Float
     */
    public Float getTargetDegreeInARShow(ArManager.ArLocation location,
                                         float x, float y, Float degreeBetween) {
        Float targetDegree = (float) 0;
        if ((location.getTargetY() - y / 1000) > 0
                && (x / 1000 - location.getTargetX()) < 0) {
            targetDegree = (float) (270 + mapDegree + degreeBetween);
        } else if ((location.getTargetY() - y / 1000) < 0
                && (x / 1000 - location.getTargetX()) > 0) {
            targetDegree = (float) (90 + mapDegree + degreeBetween);
        } else if ((location.getTargetY() - y / 1000) > 0
                && (x / 1000 - location.getTargetX()) > 0) {
            targetDegree = (float) (90 + mapDegree - degreeBetween);
        } else if ((location.getTargetY() - y / 1000) < 0
                && (x / 1000 - location.getTargetX()) < 0) {
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
//	public Float getTargetDistanceInARShow(Location location,
//			POITargetInfo targetInfos) {
//		return (float) (Math
//				.sqrt((targetInfos.getPoiTargetY() / 1000 - location.getY())
//						* (targetInfos.getPoiTargetY() / 1000 - location.getY())
//						+ (targetInfos.getPoiTargetX() / 1000 - location.getX())
//						* (targetInfos.getPoiTargetX() / 1000 - location.getX())) - defaultDisplayTargetDisance);
//	}

    /**
     * 方法描述 : 创建者：BrillantZhao_rtmap 版本： v1.0 创建时间： 2014-5-22 下午3:01:18
     *
     * @param location
     * @param arShowView
     * @return Float
     */
    public Float getTargetDistanceInARShow(ArManager.ArLocation location,
                                           ArShowView arShowView) {
//        return (float) (Math
//                .sqrt((location.getTargetY() / 1000 - arShowView.getPoiTargetY())
//                        * (location.getTargetY() / 1000 - arShowView.getPoiTargetY())
//                        + (arShowView.getPoiTargetX() - location.getTargetX() / 1000)
//                        * (arShowView.getPoiTargetX() - location.getTargetX() / 1000)) - defaultDisplayTargetDisance);
        return (float) (Math
                .sqrt((location.getTargetY() / 1000 - arShowView.getPoiTargetY())
                        * (location.getTargetY() / 1000 - arShowView.getPoiTargetY())
                        + (arShowView.getPoiTargetX() - location.getTargetX() / 1000)
                        * (arShowView.getPoiTargetX() - location.getTargetX() / 1000)));
    }

    public Float getTargetDistanceInARShow(ArManager.ArLocation location,
                                           float x, float y) {
        return (float) (Math
                .sqrt((location.getTargetY() - y / 1000)
                        * (location.getTargetY() - y / 1000)
                        + (x / 1000 - location.getTargetX())
                        * (x / 1000 - location.getTargetX())) - defaultDisplayTargetDisance);
    }
    // =============== ARShow end =====================


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

    /***
     * 获得MapView偏移的角度
     *
     * @param mMapView
     * @return
     */
//	public float getMapDegree(MapView mMapView) {
//		return -(float) Math.toDegrees(mMapView.mConfig.getDrawMap().getAngle());
//	}

    /***
     * 计算两个poi点之间的绝对角度，结果为正值，且大小在九十度之内
     *
     * @param my_x  我的x坐标
     * @param my_y  我的y坐标
     * @param poi_x poi的x坐标
     * @param poi_y poi的y坐标
     * @return
     */
    public static float getTargetDegreeAbs(float my_x, float my_y, float poi_x, float poi_y) {
        return (float) Math.abs(Math.toDegrees(Math.atan((my_y - poi_y) / (my_x - poi_x))));
    }

    /**
     * *
     * 计算两个坐标点之间的距离，传入的坐标单位为米
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return
     */
    public static float getDistanceByXy(float x1, float y1, float x2, float y2) {
        x1 = Math.abs(x1);
        x2 = Math.abs(x2);
        y1 = Math.abs(y1);
        y2 = Math.abs(y2);
        float dis = (float) (Math.sqrt((y1 - y2) * (y1 - y2) + (x1 - x2) * (x1 - x2)));
        return dis;
    }
}
