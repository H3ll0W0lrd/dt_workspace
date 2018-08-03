package com.rtm.frm.dianxin.manager;

import android.content.Context;

/**
 * 应用程序配置类：用于保存用户相关信息及设置
 * Created by fushenghua on 15/3/17.
 */
public class AppConfig {

    public final static String CONF_APP_UNIQUEID = "APP_UNIQUEID";

    public final static String CONF_EXPIRESIN = "expiresIn";
    public final static String CONF_LOAD_IMAGE = "perf_loadimage";
    public final static String CONF_SCROLL = "perf_scroll";
    public final static String CONF_HTTPS_LOGIN = "perf_httpslogin";
    public final static String CONF_VOICE = "perf_voice";

    private static AppConfig appConfig;
    private Context mContext;
    public static AppConfig getAppConfig(Context context)
    {
        if(appConfig == null){
            appConfig = new AppConfig();
            appConfig.mContext = context;
        }
        return appConfig;
    }

}
