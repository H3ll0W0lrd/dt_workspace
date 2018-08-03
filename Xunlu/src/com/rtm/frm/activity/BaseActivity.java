package com.rtm.frm.activity;

import android.os.Bundle;
import android.view.Window;

import com.rtm.frm.stack.BackStackActivity;
import com.umeng.analytics.MobclickAgent;


public class BaseActivity extends BackStackActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		this.requestWindowFeature(Window.FEATURE_NO_TITLE); 
		MobclickAgent.setDebugMode(true); 
		 
        // MobclickAgent.setAutoLocation(false); 
        // MobclickAgent.setSessionContinueMillis(10000); 
        // MobclickAgent.setUpdateOnlyWifi(false); 
 
        MobclickAgent.onError(this); 
        MobclickAgent.updateOnlineConfig(this); 
    }

}
