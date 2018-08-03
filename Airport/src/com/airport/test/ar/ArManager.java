package com.airport.test.ar;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liyan on 15/11/10.
 */
public class ArManager {

    private static ArManager instanceObj;
    public static final String ACTION_AR_SHOW_CLOSE = "ACTION_AR_SHOW_CLOSE";

    private ArLocation mCurrentArLocation;

    public interface OnLocationChangedListener {
        public void onLocationChanged(ArLocation arLocation);
    }

    /***
     * 通知接收者
     */
    private List<OnLocationChangedListener> locationChangedListeners = new ArrayList<OnLocationChangedListener>();
    /***
     * 显示的view集合
     */
    private List<ArShowView> arShowViews = new ArrayList<ArShowView>();


    public static synchronized ArManager instance() {
        if (instanceObj == null) {
            instanceObj = new ArManager();
        }
        return instanceObj;
    }

    /***
     * 添加监听通知对象
     *
     * @param listener
     */
    public void addLocationChangedListener(OnLocationChangedListener listener) {
        synchronized (locationChangedListeners) {
            locationChangedListeners.add(listener);
        }
    }

    /****
     * 移除监听通知对象
     *
     * @param listener
     */
    public void removeLocationChangedListener(OnLocationChangedListener listener) {
        synchronized (locationChangedListeners) {
            locationChangedListeners.remove(listener);
        }
    }

    /****
     * 通知所有监听对象
     *
     * @param arLocation
     */
    public void notifyLocationChanged(ArLocation arLocation) {
        mCurrentArLocation = arLocation;
        synchronized (locationChangedListeners) {
            for (OnLocationChangedListener listener : locationChangedListeners) {
                listener.onLocationChanged(arLocation);
            }
        }
    }

    /***
     * 获取当前室内定位位置
     * @return
     */
    public ArLocation getCurrentArlocation() {
        return mCurrentArLocation;
    }

//    public boolean isNeedResetData() {
//        if (mCurrentArLocation != null && (arShowViews.size() == 0 || !mCurrentArLocation.getFloor().equals(arShowViews.get(0).getFloor()))) {
//            return true;
//        }
//        return false;
//    }

    /***
     * 获取所有显示的view集合
     * @return
     */
    public List<ArShowView> getArShowViews() {
        return arShowViews;
    }

    /***
     * 获取指定楼层的view集合
     * @param floor
     */
    public List<ArShowView> getArShowViewsByFloor(String floor) {
        List<ArShowView> floorShowViews = new ArrayList<ArShowView>();
        for (ArShowView showView : arShowViews) {
            if (floor.equals(showView.getFloor())) {
                floorShowViews.add(showView);
            }
        }
        return floorShowViews;
    }

    /***
     * 设置显示的view集合
     * @param showViews
     */
    public void setArShowViews(List<ArShowView> showViews) {
        arShowViews.clear();
        arShowViews.addAll(showViews);
    }
    /***
     * 设置显示的view集合
     * @param showViews
     */
    public void setArShowView(ArShowView showView) {
        arShowViews.clear();
        arShowViews.add(showView);
    }

    /***
     * 程序退出时，要销毁，将所有静态对象内容清空
     */
    public void destroy() {
        locationChangedListeners.clear();
        mCurrentArLocation = null;
        instanceObj = null;
    }


    /*******************************************
     * 内部实体类
     ***************************************************/
    public static class ArLocation {
        private String buildId;
        private String floor;
        private float targetX;
        private float targetY;
        private int error;

        public ArLocation(String buildId, String floor, float targetX,
				float targetY, int error) {
			super();
			this.buildId = buildId;
			this.floor = floor;
			this.targetX = targetX;
			this.targetY = targetY;
			this.error = error;
		}

		public String getBuildId() {
            return buildId;
        }

        public void setBuildId(String buildId) {
            this.buildId = buildId;
        }

        public String getFloor() {
            return floor;
        }

        public void setFloor(String floor) {
            this.floor = floor;
        }

        public float getTargetX() {
            return targetX;
        }

        /**
         * 不需要进行除1000换算
         */
        public void setTargetX(float targetX) {
            this.targetX = targetX;
        }

        public float getTargetY() {
            return targetY;
        }

        /**
         * 不需要进行除1000换算
         */
        public void setTargetY(float targetY) {
            this.targetY = targetY;
        }

        public int getError() {
            return error;
        }

        public void setError(int error) {
            this.error = error;
        }
    }

//    public static class ArPointObj {
//        public String floor = "";
//        /***
//         * 无需再除1000
//         */
//        public float indoorX = 0f;
//        /***
//         * 无需再除1000
//         */
//        public float indoorY = 0f;
//        public String name = "view name";
//    }
    /*******************************************内部实体类   end ***************************************************/

}
