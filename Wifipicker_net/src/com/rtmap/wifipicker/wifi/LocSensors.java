package com.rtmap.wifipicker.wifi;

import com.rtmap.wifipicker.util.ConfigLoc;
import com.rtmap.wifipicker.util.ConstantLoc.ModeLoc;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * 传感器 com.rtm.location.sensor <br/>
 * 包含：orientation、accelerometer、pressure
 * 
 * @author lixinxin <br/>
 *         create at 2012-8-1 下午21:11:57
 *         lwl
 *         12.11.01
 */
public class LocSensors {
    //private String TAG = "LocSensors";
    /** 传感器管理器 **/
    private SensorManager mSensorManager;

    /** SensorBuffer,数据存储需要和{@link lockObject}一起使用 **/
    public static LocSensorDt sensorsVal;
    /** 同步锁，用于数据的一致性 **/
    public static Object lockSensors = new Object();
    /** 设备上下文 **/
    private static Context context = null;
    /** 该类的静态对象，用于单例 **/
    private static LocSensors LocSensors = null;

    private LocSensors() {
    }

    public void setContext(Context c) {
        synchronized (lockSensors) {
            context = c;
        }
    }

    /**
     * 得到该类的单例
     */
    public synchronized static LocSensors getInstance() {
        if (LocSensors == null) {
            LocSensors = new LocSensors();
        }
        return LocSensors;
    }

    /**
     * 加速度计Listener <br/>
     * 每触发一次调用一次setSensorValues,并把电子罗盘的值传入
     */
    private SensorEventListener accelerometerListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            synchronized (lockSensors) {
                if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    sensorsVal.accelerometer_add(sensorEvent.values);
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    /** 电子罗盘Listener **/
    private SensorEventListener orientationListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
                sensorsVal.orientation_add(event.values[0]);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    /** 气压计 **/
    private SensorEventListener pressureListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {
                sensorsVal.press_add(event.values[0]);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    /**
     * 注消传感器，停止数据接收
     */
    public void onStop() {
        if (sensorsVal != null) {
            synchronized (lockSensors) {
                sensorsVal.clear();
            }
        }
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(orientationListener);
            mSensorManager.unregisterListener(accelerometerListener);
            mSensorManager.unregisterListener(pressureListener);
        }
    }

    /**
     * 注册所需的传感器，并开始接收传感器数据
     */
    public void onStart() {
        synchronized (LocSensors) {
            if (sensorsVal == null) {
                sensorsVal = new LocSensorDt();
            }
        }

        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (ConfigLoc.isMode(ModeLoc.MODE_SERNSOR_ACCELEROMETER)) {
            mSensorManager.registerListener(accelerometerListener,
                    mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
        }
        if (ConfigLoc.isMode(ModeLoc.MODE_SERNSOR_PRESSURE)) {
            mSensorManager.registerListener(pressureListener, mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE),
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (ConfigLoc.isMode(ModeLoc.MODE_SERNSOR_ORIENTATION)) {
            mSensorManager
                    .registerListener(orientationListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                            SensorManager.SENSOR_STATUS_ACCURACY_HIGH);
        }
    }

    public void onDestroy() {
        onStop();

        synchronized (lockSensors) {
            if (sensorsVal != null) {
                sensorsVal.clear();
                sensorsVal = null;
            }
        }
    }
}