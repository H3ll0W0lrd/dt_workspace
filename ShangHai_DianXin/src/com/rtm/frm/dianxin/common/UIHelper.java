package com.rtm.frm.dianxin.common;

import android.content.Context;
import android.content.Intent;

import com.rtm.frm.dianxin.MainActivity;


/**
 * 应用程序UI工具包：封装UI相关的一些操作
 * Created by fushenghua on 15/3/17.
 */
public class UIHelper {

	
	public static void startActivity(Context context){
		Intent  intent =new Intent();
		
	}

    /**
     * 显示登录页面
     * @param context
     */
    public static void showLoginDialog(Context context)
    {
        Intent intent = new Intent(context,MainActivity.class);
        if(context instanceof MainActivity)
        context.startActivity(intent);
    }

    /**
     * 发送异常Log到服务器
     * @param context
     * crashReport
     */
    public static void  sendAppCrashReport(Context context,String crashReport){


    }
    
    
}
