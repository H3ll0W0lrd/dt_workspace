package com.hwasmart.glhwatch.receiver;

import com.hwasmart.glhwatch.AlertActivity;
import com.hwasmart.glhwatch.HelpActivity;
import com.hwasmart.glhwatch.MainActivity;
import com.hwasmart.glhwatch.NoWifiActivity;
import com.hwasmart.glhwatch.service.LocationUploadService;
import com.hwasmart.utils.Utils;
import com.hwasmart.utils.WifiConnect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class WifiBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) { 
			NetworkInfo networkInfo_ = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO); 
            if(networkInfo_.isConnected()) { 
                // WIFI is connected 
//            	Utils.showShotToast(context, "Wifi连接");
            	Intent intent0 = new Intent(context, LocationUploadService.class);
				intent0.putExtra("operation", "stop_scan_wifi");
				context.startService(intent0);
            }else{
//            	Utils.showShotToast(context, "Wifi其他状态：" + networkInfo_.getState().toString());
            	if (networkInfo_.getState().equals(NetworkInfo.State.DISCONNECTED)) {      
            		Intent intent2 = new Intent(context, LocationUploadService.class);
					intent2.putExtra("operation", "scan_wifi");
					context.startService(intent2);            		
            	}
            }
		} else if (intent.getAction().equals(WifiManager.RSSI_CHANGED_ACTION)) {

		}
	}

}
