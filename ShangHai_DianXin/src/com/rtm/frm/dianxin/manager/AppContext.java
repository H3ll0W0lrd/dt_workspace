package com.rtm.frm.dianxin.manager;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;

import com.baidu.mapapi.SDKInitializer;
import com.rtm.frm.dianxin.utils.ExceptionHandler;
import com.rtm.frm.dianxin.utils.StringUtils;

import java.util.Hashtable;

/**
 * 全局应用程序类：用于保存和调用全局应用配置及访问网络数据
 * Created by fushenghua on 15/3/17.
 */
public class AppContext extends Application {

    public static final int NETTYPE_WIFI = 0x01;
    public static final int NETTYPE_CMWAP = 0x02;
    public static final int NETTYPE_CMNET = 0x03;

    public static final int PAGE_SIZE = 20;//默认分页大小
    private static final int CACHE_TIME = 60 * 60000;//缓存失效时间

    private boolean login = false;    //登录状态
    private int loginUid = 0;    //登录用户的id
    private Hashtable<String, Object> memCacheRegion = new Hashtable<String, Object>();

    public String versionName = "";

    private static AppContext mInistance;

    public String mChooseCity = "";

    private Handler unLoginHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
//				UIHelper.ToastMessage(AppContext.this, getString(R.string.msg_login_error));
//				UIHelper.showLoginDialog(AppContext.this);
            }
        }
    };

    private ExceptionHandler excp;

    @Override
    public void onCreate() {
        super.onCreate();
        mInistance = this;
        // 在使用 SDK 各组间之前初始化 context 信息，传入 ApplicationContext
        SDKInitializer.initialize(this);

        //初始化崩溃时抓log助手
        excp = ExceptionHandler.getInstence(this);
        //获取版本号
        initVersion();
    }

    public static AppContext instance() {
        return mInistance;
    }

    /**
     * @author liYan
     * @version 创建时间：2014-8-14 上午10:12:14
     * @explain 初始化当前版本信息
     */
    private void initVersion() {
        try {
            PackageInfo pinfo = this.getPackageManager().getPackageInfo(
                    getPackageName(), PackageManager.GET_CONFIGURATIONS);
            versionName = pinfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 检测网络是否可用
     *
     * @return
     */
    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnectedOrConnecting();
    }

    /**
     * 获取当前网络类型
     *
     * @return 0：没有网络   1：WIFI网络   2：WAP网络    3：NET网络
     */
    public int getNetworkType() {
        int netType = 0;
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null) {
            return netType;
        }
        int nType = networkInfo.getType();
        if (nType == ConnectivityManager.TYPE_MOBILE) {
            String extraInfo = networkInfo.getExtraInfo();
            if (!StringUtils.isEmpty(extraInfo)) {
                if (extraInfo.toLowerCase().equals("cmnet")) {
                    netType = NETTYPE_CMNET;
                } else {
                    netType = NETTYPE_CMWAP;
                }
            }
        } else if (nType == ConnectivityManager.TYPE_WIFI) {
            netType = NETTYPE_WIFI;
        }
        return netType;
    }

    /**
     * 判断当前版本是否兼容目标版本的方法
     *
     * @param VersionCode
     * @return
     */
    public static boolean isMethodsCompat(int VersionCode) {
        int currentVersion = android.os.Build.VERSION.SDK_INT;
        return currentVersion >= VersionCode;
    }

    /**
     * 获取App安装包信息
     *
     * @return
     */
    public PackageInfo getPackageInfo() {
        PackageInfo info = null;
        try {
            info = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace(System.err);
        }
        if (info == null) info = new PackageInfo();
        return info;
    }


    /**
     * 用户是否登录
     *
     * @return
     */
    public boolean isLogin() {
        return login;
    }

    /**
     * 获取登录用户id
     *
     * @return
     */
    public int getLoginUid() {
        return this.loginUid;
    }

    /**
     * 用户注销
     */
    public void Logout() {

    }

    /**
     * 未登录或修改密码后的处理
     */
    public Handler getUnLoginHandler() {
        return this.unLoginHandler;
    }

    /**
     * 用户登录验证
     *
     * @param account
     * @param pwd
     * @return
     * @throws AppException
     */
    public void loginVerify(String account, String pwd) throws AppException {
        //return ApiClient.login(this, account, pwd);
    }

}
