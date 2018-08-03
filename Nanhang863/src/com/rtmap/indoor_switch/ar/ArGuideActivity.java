package com.rtmap.indoor_switch.ar;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airport.test.R;
import com.rtmap.indoor_switch.pages.RtmFragment;
import com.rtmap.indoor_switch.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

public class ArGuideActivity extends FragmentActivity implements ArManager.OnLocationChangedListener {

    public static final String KEY_MAP_DEGREE = "KEY_MAP_DEGREE";

    public static ArGuideActivity instance;
    // 摄像头相关---------
    private Camera camera;
    private Camera.Parameters parameters = null;
    // 传感器相关---------
    private SensorManager mSensorManager;// 传感器管理对象
    private Sensor mOrientationSensor;// 传感器对象
    public static float sensorX;// 当前角度
    public static float sensorY;// 当前角度
    // view相关-------------
    private TextView textViewARAngle;
    private SurfaceView surfaceView;
    private ImageView imageViewArrow;
    // 箭头指向相关
    private boolean isStopArrowRun = true;
    protected final Handler mHandler = new Handler();
    private Float fromDegree = 0f;
    private Float destinationDegree = 0f;
    private float mTargetDirection = 0f;// 目标浮点方向

    //AR导航
//    private int poi_x;
//    private int poi_y;

    private TextView mTextViewDis;
    private String mBuildName;


    private int screenWidth = 720;
    private int screenHeight = 1280;
    private int statusBarHeight;
    private List<ArShowView> mARShowViewsByFloor = new ArrayList<>();

    private Float showDistance = 60f;

    private RelativeLayout mRlArItem;
    private RelativeLayout dialogRel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar_guide);
        // AR工具类初始化
        Intent i = getIntent();

        ArUtils.getInstance().initARUtils(this, i.getFloatExtra(KEY_MAP_DEGREE, 0f));
        mBuildName = i.getStringExtra("build_name");
        ((TextView) findViewById(R.id.tv_build_name)).setText(mBuildName);
        findViewById(R.id.img_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArGuideActivity.this.finish();
            }
        });

        instance = this;

        initView();

    }

    @Override
    protected void onStart() {
        super.onStart();

        init();
    }

    @Override
    protected void onResume() {

        registerSensorListener();
        startArrowRun();

        ArManager.instance().addLocationChangedListener(this);

        registerCloseReceiver();
        super.onResume();
    }

    @Override
    protected void onPause() {

        unRegisterSensorListener();
        stopArrowRun();
        ArManager.instance().removeLocationChangedListener(this);

        unregisterCloseReceiver();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        ArManager.instance().destroy();
        destroySurfaceView();
        stopTimer();
        super.onDestroy();
    }

    public void myClick(View v) {
        int i = v.getId();
        if (i == R.id.close) {
            this.finish();

        }
    }

    private void init() {
        initSurfaceView();
        initSensor();
    }


    private void initView() {
        mRlArItem = (RelativeLayout) findViewById(R.id.rl_ar_item);

        mTextViewDis = (TextView) findViewById(R.id.tv_des);
        textViewARAngle = (TextView) findViewById(R.id.tv_ar_angle);
        surfaceView = (SurfaceView) findViewById(R.id.surfaceview_ar);
        imageViewArrow = (ImageView) findViewById(R.id.imageview_arrow);


        DisplayMetrics dm = this.getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;

        statusBarHeight = ArUtils.getStatusBarHeight(this);

        dialogRel = (RelativeLayout)findViewById(R.id.rl_dialog_content);
        show(dialogRel,LAST_TIME);
        show(dialogRel,INTEREST);
    }

    /***
     * 设置显示的view
     *
     * @param floor
     */
    private void setARShowViewsByFloor(String floor) {
        mARShowViewsByFloor.clear();
        mARShowViewsByFloor = ArManager.instance().getArShowViewsByFloor(floor);
        ((Button) findViewById(R.id.btn_floor)).setText(floor);

        mRlArItem.removeAllViews();
        for (ArShowView showView : mARShowViewsByFloor) {
            mRlArItem.addView(showView.getLayoutView());
        }
    }

    @SuppressWarnings("deprecation")
    private void initSensor() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mOrientationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
    }

    private void registerSensorListener() {
        if (mOrientationSensor != null) {
            mSensorManager.registerListener(sensorEventListener, mOrientationSensor, SensorManager.SENSOR_DELAY_GAME);
        } else {
            ToastUtils.shortToast("获取传感器失败");
        }
    }

    private void unRegisterSensorListener() {
        if (mOrientationSensor != null) {
            mSensorManager.unregisterListener(sensorEventListener);
        }
    }

    // 方向传感器变化监听
    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float direction = event.values[0] * -1.0f;
            mTargetDirection = ArUtils.getInstance().normalizeDegree(direction);// 赋值给全局变量，让指南针旋转

            sensorX = event.values[0];
            sensorY = event.values[1];
//            textViewARAngle.setText("sensorX:" + sensorX + "\n" + "sensorY:" + sensorY + "\n");
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    @SuppressWarnings("deprecation")
    private void initSurfaceView() {
        try {
            surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            surfaceView.getHolder().setFixedSize(1280, 780); // 设置Surface分辨率
            surfaceView.getHolder().setKeepScreenOn(true);// 屏幕常亮
            surfaceView.getHolder().addCallback(new SurfaceCallback());// 为SurfaceView的句柄添加一个回调函数
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(ArManager.ArLocation arLocation) {
        if (arLocation == null || arLocation.getError() != 0) {
            return;
        }
        if (mARShowViewsByFloor.size() == 0) {
            String floor = ArManager.instance().getCurrentArlocation().getFloor();
            setARShowViewsByFloor(floor);
        }

        float coordX = arLocation.getTargetX() / 1000;
        float coordY = arLocation.getTargetY() / 1000;

        if (mARShowViewsByFloor.size() > 0) {

            ArShowView mARShowView = mARShowViewsByFloor.get(0);

            float targetDegreeAbs = ArUtils.getTargetDegreeAbs(coordX, coordY, mARShowView.getPoiTargetX(), mARShowView.getPoiTargetY());

            destinationDegree = targetDegreeAbs / 2 * (-1);


            //显示距离
            float dis = ArUtils.getDistanceByXy(mARShowView.getPoiTargetX(), mARShowView.getPoiTargetY(), coordX, coordY);
            mTextViewDis.setText((int) dis + "米");

            mARShowView.setDistance(dis);

            //如果当前定位点楼层与arview的楼层不一致，则获取定位楼层的arview
            if (!arLocation.getFloor().equals(mARShowView.getFloor())) {
                setARShowViewsByFloor(arLocation.getFloor());
            }
        }

    }


    private final class SurfaceCallback implements Callback {
        // 拍照状态变化时调用该方法
        @SuppressWarnings("deprecation")
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            try {
                parameters = camera.getParameters(); // 获取各项参数
                parameters.setPictureFormat(PixelFormat.JPEG); // 设置图片格式
                parameters.setPreviewSize(width, height); // 设置预览大小
                parameters.setPreviewFrameRate(5); // 设置每秒显示4帧
                parameters.setPictureSize(width, height); // 设置保存的图片尺寸
                parameters.setJpegQuality(80); // 设置照片质量
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 开始拍照时调用该方法
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                camera = Camera.open(); // 打开摄像头
                camera.setPreviewDisplay(holder); // 设置用于显示拍照影像的SurfaceHolder对象
                // 获得手机的方向
                int rotation = getWindowManager().getDefaultDisplay().getRotation();
                camera.setDisplayOrientation(ArUtils.getPreviewDegree(rotation));
                camera.startPreview(); // 开始预览
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 停止拍照时调用该方法
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

            destroySurfaceView();
        }
    }

    private void destroySurfaceView() {
        if (camera != null) {
            camera.stopPreview();
            camera.release(); // 释放照相机
            camera = null;
        }
    }

    /**
     * 旋转箭头
     *
     * @param fromDegree
     * @param toDegree
     */
    RotateAnimation animation;

    private void rotateArrow(float toDegree) {
        animation = new RotateAnimation(-fromDegree, -toDegree, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        animation.setDuration(50);// 设置动画持续时间
        animation.setFillAfter(true);// 动画执行完后是否停留在执行完的状态
        animation.start();
        imageViewArrow.setAnimation(animation);

        this.fromDegree = toDegree;
    }

    private void startArrowRun() {
        isStopArrowRun = false;
        mHandler.post(arrowRun);
    }

    private void stopArrowRun() {
        isStopArrowRun = true;
    }

    /**
     * 用来实时刷新箭头方向和arViews，时间间隔x毫秒
     */
    protected Runnable arrowRun = new Runnable() {
        @Override
        public void run() {
            if (!isStopArrowRun) {
                updateDirection();
                mHandler.postDelayed(this, 50);
            }
        }
    };

    // 更新顶部方向显示的方法
    private void updateDirection() {
        Float direction = ArUtils.getInstance().normalizeDegree(
                mTargetDirection * -1.0f);

        drawView(direction);
    }

    /**
     * 刷新ar view
     *
     * @param direction void
     */
    @SuppressLint("NewApi")
    private void drawView(Float direction) {
        ArManager.ArLocation location = ArManager.instance().getCurrentArlocation();

        if (location == null) {
            return;
        }

        if (mARShowViewsByFloor.size() > 0) {//单点箭头指向，默认只拿第0个view进行显示。

            ArShowView mARShowView = mARShowViewsByFloor.get(0);

            Float drawDirection = direction;
            Float degreeBetween = ArUtils.getInstance()
                    .getDegreeBetweenWithThround(location, mARShowView);
            Float targetDegree = ArUtils.getInstance().getTargetDegreeInARGuide(
                    location, mARShowView, degreeBetween);

            // 因为旋转了固定的角度，需要在这里减去
            Float targerDistance = ArUtils.getInstance()
                    .getTargetDistanceInARShow(location, mARShowView);

            targetDegree = (targetDegree + 360) % 360;
            Log.e("ar", "  targetDegree:" + targetDegree + "   degreeBetween:" + degreeBetween);


            Float modifyDegree = drawDirection - targetDegree;

            if (modifyDegree >= 360 - ArUtils.getInstance().eyeDegree / 2) {
                modifyDegree = modifyDegree - 360;
            } else if (modifyDegree <= (ArUtils.getInstance().eyeDegree + ArUtils
                    .getInstance().eyeDegreeOutScreen) / 2 - 360) {
                modifyDegree = 360 + modifyDegree;
            }
            // 有选择的重画
//        if (targerDistance <= (showDistance + 10)
            if (Math.abs(modifyDegree) <= (ArUtils.getInstance().eyeDegree + ArUtils
                    .getInstance().eyeDegreeOutScreen) / 2) {
                //显示view，若设置动画，则启动动画
                mARShowView.show();

                Float currentX = (-modifyDegree + ArUtils.getInstance().eyeDegree / 2)
                        * (screenWidth / ArUtils
                        .getInstance().eyeDegree);


                showDistance = targerDistance * 1.25f;
                Float currentY = (showDistance - targerDistance) * ((screenHeight
                        - statusBarHeight) / showDistance);


                // 只有在API LEVEL 11及之后才添加，所有使用之前的低版本可能会有问题
                mARShowView.setScreenX(currentX);
                mARShowView.setScreenY(currentY);

                //旋转摄像头
                rotateArrow(modifyDegree);
            } else {
                //超出范围隐藏view，若设置动画，则停止动画
                mARShowView.hide();
            }
        }

    }

    private BroadcastReceiver closeReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ArGuideActivity.this.finish();
        }
    };

    private void registerCloseReceiver() {
        IntentFilter intentFilter = new IntentFilter(RtmFragment.ACTION_AR_SHOW_CLOSE);
        this.registerReceiver(closeReciever, intentFilter);
    }

    private void unregisterCloseReceiver() {
        this.unregisterReceiver(closeReciever);
    }


    /***
     * /提示框
     */
    TextView mTransform;
    TextView mClassify;
    TextView mInteresting;
    TextView mTime;
    Object senMessage;
    ImageView mDiaglog;


    public static final int TRANSFORM = 0;//室内外切换
    public static final int BOARDING_INFORMATION = 1;//登记卡信息
    public static final int INTEREST = 2;//兴趣
    public static final int CLASSIFY = 3;//排队
    public static final int LAST_TIME = 4;//登机时间


    private Handler hhhh = new Handler();
    private Runnable timerRun = new Runnable() {
        @Override
        public void run() {
            mTransform.setVisibility(View.GONE);
            mClassify.setVisibility(View.GONE);
            mInteresting.setVisibility(View.GONE);
        }
    };

    private void startTimer(int delayTime) {
        hhhh.postDelayed(timerRun, delayTime);
    }

    private void stopTimer() {
        hhhh.removeCallbacks(timerRun);
    }


    private void show(RelativeLayout relativeLayout, int i) {

        relativeLayout.addView(diaglog_1());
        relativeLayout.addView(diaglog_2());
        relativeLayout.addView(diaglog_3());
        relativeLayout.addView(diaglog_4());
        relativeLayout.addView(diaglog_5());
        switch (i) {
            case TRANSFORM://室内外切换
                mTransform.setVisibility(View.VISIBLE);
                Message message_1 = new Message();
                message_1.obj = senMessage;
//                hhhh.sendMessage(message_1);
                startTimer(5000);
                break;
            case BOARDING_INFORMATION://登记卡信息
                mDiaglog.setVisibility(View.VISIBLE);
//                dialogContentRel.setAlpha(0.85f);
                break;
            case INTEREST://兴趣
                mInteresting.setVisibility(View.VISIBLE);
                Message message_3 = new Message();
                message_3.obj = senMessage;
//                hhhh.sendMessage(message_3);
                startTimer(8000);
                break;
            case CLASSIFY://排队
                mClassify.setVisibility(View.VISIBLE);
                Message message_4 = new Message();
                message_4.obj = senMessage;
//                hhhh.sendMessage(message_4);
                startTimer(5000);
                break;
            case LAST_TIME://登机时间
                mTime.setVisibility(View.VISIBLE);
                break;
        }
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private View diaglog_5() {
        mTime = new TextView(this);
        mTime.setText("您离登机时间还有30分");
        mTime.setTextColor(Color.parseColor("#FFF9FB"));
        mTime.setBackgroundColor(Color.parseColor("#797981"));
        mTime.setGravity(Gravity.CENTER);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 80);
        mTime.setAlpha((float) 0.85);
        mTime.setLayoutParams(layoutParams);
        mTime.setVisibility(View.GONE);
        return mTime;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private View diaglog_3() {
        mInteresting = new TextView(this);
        mInteresting.setText("您想看的《芈月传》本店已上架");
        mInteresting.setTextColor(Color.parseColor("#FFF9FB"));
        mInteresting.setBackgroundColor(Color.parseColor("#ED3970"));
        mInteresting.setGravity(Gravity.CENTER);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 150);
        mInteresting.setAlpha((float) 0.9);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        mInteresting.setLayoutParams(layoutParams);
        mInteresting.setVisibility(View.GONE);
        mInteresting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //        POI poi = new POI();
//        poi.setBuildId("860100010030100002");
//        poi.setName("书刊店");
//        poi.setFloor("F2");
//        poi.setX(43.539f);
//        poi.setY(274.748f);
//        poi.setPoiNo(219);
                ArShowView mARShowView = mARShowViewsByFloor.get(0);
                mARShowView.setTargetName("书刊店");
                mARShowView.setPoiTargetX(43.539f);
                mARShowView.setPoiTargetY(274.748f);
            }
        });

        return mInteresting;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private View diaglog_4() {
        mClassify = new TextView(this);
        mClassify.setText("当前安检排队人数较多，请尽快前往");
        mClassify.setTextColor(Color.parseColor("#FFF9FB"));
        mClassify.setBackgroundColor(Color.parseColor("#ED3970"));
        mClassify.setGravity(Gravity.CENTER);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 150);
        mClassify.setAlpha((float) 0.9);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        mClassify.setLayoutParams(layoutParams);
        mClassify.setVisibility(View.GONE);

        return mClassify;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private View diaglog_1() {
        mTransform = new TextView(this);
        mTransform.setText("欢迎来到首都机场");
        mTransform.setTextColor(Color.parseColor("#FFF9FB"));
        mTransform.setBackgroundColor(Color.parseColor("#ED3970"));
        mTransform.setGravity(Gravity.CENTER);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 150);
        mTransform.setAlpha((float) 0.9);
        mTransform.setLayoutParams(layoutParams);
        mTransform.setVisibility(View.GONE);

        return mTransform;
    }

    private ImageView diaglog_2() {
        mDiaglog = new ImageView(this);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        mDiaglog.setBackgroundResource(R.drawable.passmessage);
        mDiaglog.setLayoutParams(layoutParams);
        mDiaglog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                onBackPressed();
                mDiaglog.setVisibility(View.GONE);
//                dialogContentRel.setAlpha(0.0f);
            }
        });
        mDiaglog.setVisibility(View.GONE);

        return mDiaglog;
    }
}
