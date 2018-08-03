package com.rtmap.wifipicker.wifi;

import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

import com.rtmap.wifipicker.util.DTLog;
import com.rtmap.wifipicker.util.ConstantLoc.UIEventCode;

/** 网络侧采集对外接口函数
 * @author hotstar */
public class NetGather {
    public static final int       GATHER_STATUS    = 4000;
    public static final int       GATHER_END       = 4001;
    private static NetGather instance         = null;
//    private WifiUpdateTask        mWifiUpdateTask  = null;
//    private Timer                 mWifiUpdateTimer = null;

    private NetGather() {
//        mWifiUpdateTimer = new Timer();
//        if (mWifiUpdateTask == null) {
//            mWifiUpdateTask = new WifiUpdateTask();
//            mWifiUpdateTimer.schedule(mWifiUpdateTask, 0, 1000);
//        }
    }

    static public NetGather GetInstance() {
        if (instance == null) {
            instance = new NetGather();
        }
        return instance;
    }

    /** 开启采集 */
    public void start(NetGatherTask.CompleteListener listener,String file,String map) {
        String valString = NetGatherData.startGather(map,file);//(xml格式)用户id，地图名，macs
        DTLog.e("开始发送："+valString);
        new NetGatherTask(listener).execute(valString, NetGatherData.sServiceIp);
    }
   
}
