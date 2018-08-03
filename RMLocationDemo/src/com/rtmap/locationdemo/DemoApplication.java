package com.rtmap.locationdemo;

import android.app.Application;

import com.rtmap.locationdemo.exception.ExceptionHandler;

public class DemoApplication extends Application {
	private static DemoApplication instance;

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		// 异常注册
		Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler
				.getInstance());// 处理异常
	}

	public static DemoApplication getInstance() {
		return instance;
	}
}

