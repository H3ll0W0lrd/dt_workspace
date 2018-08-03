package com.rtmap.driver;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.rtm.common.model.RMLocation;
import com.rtm.common.utils.Constants;
import com.rtm.frm.map.CompassLayer;
import com.rtm.frm.map.MapView;
import com.rtm.frm.map.XunluMap;
import com.rtm.frm.utils.Handlerlist;
import com.rtm.location.LocationApp;
import com.rtm.location.utils.RMLocationListener;
import com.rtmap.driver.util.BuildUitl;
import com.rtmap.driver.util.T;
import com.rtmap.driver.util.TimeUtil;

public class MapActivity extends Activity implements RMLocationListener {

    private MapView mMapView;// 地图view

    private CompassLayer mCompassLayer;// 指南针图层

    private TextView mTvTest;

    private EditText mEdtFloor;

    private Handler mHandler = new Handler() {// 下载地图过程中下载进度消息
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.RTMAP_MAP:
                    int progress = msg.arg1;
                    Log.e("rtmap", "SDK进度码" + progress);
                    if (progress == Constants.MAP_LOAD_START) {// 开始加载
                        Log.e("rtmap", "开始加载");
                    } else if (progress == Constants.MAP_FailNetResult) {// 校验结果失败
                        Log.e("rtmap", "校验结果：" + (String) msg.obj);
                    } else if (progress == Constants.MAP_FailCheckNet) {// 联网检测失败
                        Log.e("rtmap", "校验联网失败");
                    } else if (progress == Constants.MAP_Down_Success) {
                        Log.e("rtmap", "地图下载成功");
                    } else if (progress == Constants.MAP_Down_Fail) {
                        Log.e("rtmap", "地图下载失败");
                    } else if (progress == Constants.MAP_LOAD_END) {
                        Log.e("rtmap", "地图加载完成");
                    }
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_main);

        mTvTest = (TextView) findViewById(R.id.tv_test);
        mEdtFloor = (EditText) findViewById(R.id.edt_floor);
        findViewById(R.id.btn_set_flor).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.btn_set_flor: {
                        String floor = mEdtFloor.getText().toString();
                        mMapView.initMapConfig("860100010030100003", floor);
                    }
                    break;
                }
            }
        });

        Handlerlist.getInstance().register(mHandler);
        XunluMap.getInstance().init(this);// 初始化
        mMapView = (MapView) findViewById(R.id.map_view);
        initLayers();// 初始化图层
        mMapView.initMapConfig("860100010030100003", "F4");// 打开地图（建筑物id，楼层id）
        mMapView.initScale();// 初始化比例尺


        LocationApp.getInstance().init(getApplicationContext());// 定位服务初始化
        LocationApp.getInstance().registerLocationListener(this);
        T.s("init floor:F4");

        findViewById(R.id.btn_ftp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapActivity.this, FtpActivity.class);
                MapActivity.this.startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocationApp.getInstance().start();

    }

    @Override
    protected void onPause() {
        super.onPause();
        LocationApp.getInstance().stop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.clearMapLayer();
        LocationApp.getInstance().unRegisterLocationListener(this);
    }


    /**
     * 初始化图层
     */
    private void initLayers() {
        mCompassLayer = new CompassLayer(mMapView);
        mMapView.addMapLayer(mCompassLayer);
        mMapView.refreshMap();
    }

    @Override
    public void onReceiveLocation(RMLocation rmLocation) {
        if (rmLocation != null && rmLocation.error == 0) {

            String floor = BuildUitl.floorTransform(rmLocation.getFloorID());
            int x = rmLocation.coordX / 1000;
            int y = rmLocation.coordY / 1000;
            String buildId = rmLocation.getBuildID();
//            Location mLocation = new Location(x, y, floor);

            if (!buildId.equals(mMapView.getBuildId()) || !floor.equals(mMapView.getFloor())) {
                mMapView.initMapConfig(buildId, floor);
            }

            String timeString = TimeUtil.getFormatNowDate();
            mTvTest.setText("楼层：" + floor + "  X:" + rmLocation.coordX + "  Y:" + rmLocation.coordY + "  " + timeString.split(" ")[1]);
//            mMapView.setMyCurrentLocation(mLocation);
            mMapView.setMyCurrentLocation(rmLocation);
//            mMapView.refreshMap();
        } else {
            if (rmLocation == null) {
                mTvTest.setText("loc：" + rmLocation);

            } else {
                mTvTest.setText("error:" + rmLocation.error);

            }
        }
    }
}