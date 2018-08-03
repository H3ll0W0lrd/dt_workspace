package com.rtmap.wifipicker.wifi;

import java.util.ArrayList;
import java.util.List;

import com.rtmap.wifipicker.util.ConstantLoc;

/**
 * 传感器信息实体
 * 
 * @author lixinxin <br/>
 *         create at 2012-8-2 上午10:10:11
 *         lwl modified 12.11.01
 */
public class LocSensorDt {
    /** 加速度传感器 **/
    private List<float[]> accelerometer;
    /** 电子罗盘的方向角 **/
    private List<Float> orientation;
    /** 压力传感器 **/
    private List<Float> press;
    /** 同步锁，用于数据的一致性 **/
    private static Object lockSensors = new Object();

    /** 采集时的时间戳 **/
    public long time;

    public void press_add(float pre) {
        synchronized (lockSensors) {
            press.add(pre);
            if (press.size() > ConstantLoc.SENSOR_PRESS_BUFFER_MAX) {
                press.remove(0);
            }
        }
    }

    public List<Float> press_get() {
        List<Float> retFloats = new ArrayList<Float>();
        synchronized (lockSensors) {
            retFloats.addAll(press);
            press.clear();
        }
        return retFloats;
    }

    public void orientation_add(float acc) {
        synchronized (lockSensors) {
            orientation.add(acc);
            if (orientation.size() > ConstantLoc.SENSOR_ORI_BUFFER_MAX) {
                orientation.remove(0);
            }
        }
    }

    public List<Float> orientation_get() {
        List<Float> retFloats = new ArrayList<Float>();
        synchronized (lockSensors) {
            retFloats.addAll(orientation);
            orientation.clear();
        }
        return retFloats;
    }

    public void accelerometer_add(float[] acc) {
        synchronized (lockSensors) {
            accelerometer.add(acc);
            if (accelerometer.size() > ConstantLoc.SENSOR_ACC_BUFFER_MAX) {
                accelerometer.remove(0);
            }
        }
    }

    public List<float[]> accelerometer_get() {
        List<float[]> retList = new ArrayList<float[]>();
        synchronized (lockSensors) {
            retList.addAll(accelerometer);
            accelerometer.clear();
        }
        return retList;
    }

    /** 构造函数 **/
    public LocSensorDt() {
        accelerometer = new ArrayList<float[]>();
        orientation = new ArrayList<Float>();
        press = new ArrayList<Float>();
    }

    /** 清除 **/
    public void clear() {
        if (accelerometer != null) {
            accelerometer.clear();
        }
        if (orientation != null) {
            orientation.clear();
        }
        if (press != null) {
            press.clear();
        }
    }
}