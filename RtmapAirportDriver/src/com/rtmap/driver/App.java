package com.rtmap.driver;

import android.app.Application;

import com.rtmap.driver.util.ExceptionHandler;

public class App extends Application {
    public static App instance = null;

    public String floor ="no";
    public int coordX = 0;
    public int coordY = 0;
    public int error = -1;


    /***
     * 电量百分比
     */
    public double batteryScale = 0;

    public static synchronized App getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        init();
    }

    private void init() {
        ExceptionHandler.getInstence(instance);
    }
}
