package com.dt.screen;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ScreenService extends Service {

	@Override
	public void onCreate() {
		super.onCreate();
//		showPopupWindow();

	}

	

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
