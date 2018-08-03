package com.rtm.frm.receiver;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.rtm.frm.service.UpdateDataService;
import com.rtm.frm.utils.ConstantsUtil;
import com.rtm.frm.utils.PreferencesUtil;
import com.rtm.frm.utils.XunluUtil;

public class NetStateReceiver extends BroadcastReceiver {

	private boolean showLog = false;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		ConnectivityManager connectMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mobNetInfo = connectMgr
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		NetworkInfo wifiNetInfo = connectMgr
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (mobNetInfo.isConnected() || wifiNetInfo.isConnected()) {
			//启动数据更新服务
			shoLog("connected");
			if(!isAlreadyUpdateInToday()) {//1.检查当天是否已经更新过数据
				shoLog("today not update");
				if(!isServiceRunning(context,UpdateDataService.class.getSimpleName())) {//2.判断当前是否处于正在更新状态
					shoLog("main application not run");
					if(!isAppIsRunning(context)) {//3.判断主程序是否运行
						//4.启动更新数据服务
						shoLog("start service");
						Intent updateDataIntent = new Intent(context,UpdateDataService.class); 
						context.startService(updateDataIntent);
					}
				}
			} 
		} 
	}
	
	/**
	 * @param context
	 * @return true 在运行 false 不在运行
	 * @explain 判断当前主程序是否正在运行
	 */
	private boolean isAppIsRunning(Context context){
		ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
	    List<RunningTaskInfo> list = am.getRunningTasks(100);
	    String packageName = context.getPackageName();
	    boolean isAppRunning = false;
	    for (RunningTaskInfo info : list) {
	        if (info.topActivity.getPackageName().equals(packageName) && info.baseActivity.getPackageName().equals(packageName)) {
	            isAppRunning = true;
	            break;
	        }
	    }
	    return isAppRunning;
	}
	
	/**
     * 用来判断服务是否运行.
     * @param context
     * @param className 判断的服务名字
     * @return true 在运行 false 不在运行
     */
    private boolean isServiceRunning(Context mContext,String className) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager)
        mContext.getSystemService(Context.ACTIVITY_SERVICE); 
        List<ActivityManager.RunningServiceInfo> serviceList  = activityManager.getRunningServices(30);
        if (!(serviceList.size()>0)) {
            return false;
        }
        for (int i=0; i<serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(className) == true) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }
    
    /**
     * @explain 判断当天是否已经更新过
     * @return true 当天已更新  false 当天未更新
     */
    private boolean isAlreadyUpdateInToday(){
    	boolean update = false;
		String lastUpdateDate = PreferencesUtil.getString(ConstantsUtil.PREFS_LAST_UPDATE_DATA_DATE, "");
    	String currentDate = XunluUtil.getCurrentDate();
		if(lastUpdateDate.equals(currentDate)) {
			update = true;
    	}
    	return update;
    }
    
    private void shoLog(String msg) {
    	String TAG = "UpdateDataService";
    	if(showLog) {
    		Log.e(TAG,msg);
    	}
    }

}
