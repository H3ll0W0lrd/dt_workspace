package com.rtmap.indoor_switch.base;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ZoomControls;

import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.SupportMapFragment;

import java.util.Stack;

/**
 * 基本Activity
 * Created by fushenghua on 15/3/17.
 */
public abstract class BaseActivity extends FragmentActivity implements OnClickListener, BDLocationListener {

    protected Context mContext;
    private final int SHOW_NETERROR_DIALOG = 1;
    private final int SHOW_SETNET_DIALOG = 0;
    protected ProgressDialog progressDialog;

    private Stack<Fragment> fragmentStack;
    private final String BACK_STACK_FLAG = "BACK_STACK_FLAG";
    private FragmentManager fragmentManager;

    /**
     * Android生命周期回调方法-创建
     */
    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        fragmentManager = getSupportFragmentManager();
        mContext = this;
        initView();
    }

    /**
     * 初始化界面
     */
    private void initView() {
        loadViewLayout();
        findViewById();
    }

    /**
     * Android生命周期回调方法
     */
    @Override
    protected void onStart() {
        super.onStart();
        if (initBdLbs) {
            baiDuMapFragment.getBaiduMap().setMyLocationEnabled(true);
            baiDuMapFragment.getMapView().removeViewAt(1);
            int childCount = baiDuMapFragment.getMapView().getChildCount();
            for (int i = 0; i < childCount; ++i) {
                if (baiDuMapFragment.getMapView().getChildAt(i) instanceof ZoomControls) {
                    baiDuMapFragment.getMapView().getChildAt(i).setVisibility(View.GONE);
                }
            }
        }
        processLogic();
        setListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBdLoc();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopBdLoc();
    }

    @Override
    protected void onDestroy() {
        if (fragmentStack != null && fragmentStack.size() > 0) {
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.remove(fragmentStack.get(0));
            ft.commitAllowingStateLoss();
            fragmentStack.removeAllElements();
        }
        super.onDestroy();
    }

    /**
     * 添加显示fragment
     *
     * @param fragment
     * @param layoutID
     * @param title
     */
    public void pushFragment(Fragment fragment, int layoutID, String... title) {
        if (fragmentStack == null) {
            fragmentStack = new Stack<>();
        }
        fragmentStack.push(fragment);
        FragmentTransaction ft = fragmentManager.beginTransaction();

        ft.add(layoutID, fragment);
        if (fragmentStack.size() > 1) {
            ft.addToBackStack(BACK_STACK_FLAG);
        }
        ft.commit();
    }

    /**
     * 手动后退移除fragment
     */
    public void backStackFragment() {
        if (fragmentStack.size() == 1) {
            this.finish();
        } else {
            fragmentManager.popBackStack();
        }
        fragmentDestroy(null);
    }

    public void fragmentDestroy(Fragment fragment) {
        if (fragmentStack.size() > 0) {
            if (fragment == null) {
                fragmentStack.remove(fragmentStack.size() - 1);
            } else {
                fragmentStack.remove(fragment);
            }
        }
    }

    public Fragment getFragmentByClass(Class<?> fragment) {
        int size = fragmentStack.size();
        for (int i = 0; i < size; ++i) {
            Fragment f = fragmentStack.get(i);
            if (f.getClass() == fragment) {
                return f;
            }
        }
        return null;
    }

    /***
     * 获取fragment堆栈数量
     *
     * @return
     */
    protected int getStackSize() {
        return fragmentStack == null ? 0 : fragmentStack.size();
    }

    /**
     * 启动另外一个activity
     *
     * @param activityClass
     * @param data
     */
    public void pushActivity(Class<?> activityClass, Bundle data) {
        Intent intent = new Intent(this, activityClass);
        if (data != null) {
            intent.putExtras(data);
        }
        startActivity(intent);
    }


    /**
     * 设置titlebar
     */
    protected void setTitleBarView(boolean isShowBack, String middleStr,
                                   int funResource, boolean isShowHome) {
    }

    @Override
    public void onClick(View v) {

    }

    public void showNetFailedDialog() {
        showDialog(SHOW_SETNET_DIALOG);

    }

    public void showNetErrorDialog() {
        showDialog(SHOW_NETERROR_DIALOG);

    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
            case SHOW_NETERROR_DIALOG:
                if (!isFinishing()) {
                    dialog = new AlertDialog.Builder(this)
                            .setTitle("提示")
                            .setMessage("网络不稳定！请重试")
                            .setPositiveButton("确定",
                                    new DialogInterface.OnClickListener() {

                                        public void onClick(DialogInterface dialog,
                                                            int which) {
                                            getDataAgain();

                                        }
                                    })
                            .setNegativeButton("取消",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int whichButton) {
                                            dialog.cancel();
                                        }
                                    }).create();
                }
                break;
            case SHOW_SETNET_DIALOG:
                if (!isFinishing()) {
                    dialog = new AlertDialog.Builder(this)
                            .setTitle("提示")
                            .setMessage("没用网络连接，请设置网络！")
                            .setPositiveButton("设置网络",
                                    new DialogInterface.OnClickListener() {

                                        public void onClick(DialogInterface dialog,
                                                            int which) {
                                            startActivityForResult(
                                                    new Intent(
                                                            android.provider.Settings.ACTION_WIRELESS_SETTINGS),
                                                    0);
                                        }

                                    })
                            .setNegativeButton("取消",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int whichButton) {
                                            dialog.cancel();
                                            // closeApp();
                                        }
                                    }).create();
                }
                break;

            default:
                return super.onCreateDialog(id);
        }
        return dialog;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 0:
                getDataAgain();
                break;

            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public boolean checkApp(String packageName) {
        if (packageName == null || "".equals(packageName))
            return false;
        try {
            ApplicationInfo info = getPackageManager().getApplicationInfo(
                    packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }


    /**
     * 显示正在加载提示框
     */
    protected void showProgressDialog(String message) {
        if ((!isFinishing()) && (this.progressDialog == null)) {
            this.progressDialog = new ProgressDialog(this);
        }
        progressDialog.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode,
                                 KeyEvent event) {
                if (keyCode == event.KEYCODE_BACK) {
                    //取消当前网络请求
                }
                return false;
            }
        });
//		progressDialog.setCancelable(false);
        // this.progressDialog.setTitle(getString(R.string.loadTitle));
        this.progressDialog.setMessage(message);
        this.progressDialog.show();
    }

    /**
     * 关闭提示框
     */
    protected void closeProgressDialog() {
        if (this.progressDialog != null)
            this.progressDialog.dismiss();
    }

    /***
     * 该参数一定要在processLogic方法里面使用
     */
    protected SupportMapFragment baiDuMapFragment;
    private LocationClient baiDuLocation;
    private boolean initBdLbs = false;

    /**
     * 百度地图、百度定位相关,该方法一定要在onCrate中执行
     */
    protected void initBdLbs(Context context, boolean isOpenLoc, int attachLayoutId) {
        initBdLbs = true;
        //百度地图初始化
        MapStatus ms = new MapStatus.Builder().overlook(-20).zoom(15).build();
        BaiduMapOptions bo = new BaiduMapOptions().mapStatus(ms)
                .compassEnabled(false).zoomControlsEnabled(false);

        baiDuMapFragment = SupportMapFragment.newInstance();
        pushFragment(baiDuMapFragment, attachLayoutId);

        if (isOpenLoc) {
            //百度定位初始化
            openBdLoc(context, this);
        }
    }

    public void openBdLoc(Context context, BDLocationListener listener) {
        if (baiDuLocation == null) {
            baiDuLocation = new LocationClient(context);
            baiDuLocation.registerLocationListener(listener);
            LocationClientOption option = new LocationClientOption();
            option.setOpenGps(true);// 打开gps
            option.setAddrType("all");
            option.setIsNeedAddress(true);
            option.setCoorType("bd09ll"); // 设置坐标类型
            option.setScanSpan(1000);
            baiDuLocation.setLocOption(option);
        }
    }

    /**
     * 启动百度定位
     */
    protected void startBdLoc() {
        if (baiDuLocation != null) {
            baiDuLocation.start();
        }
    }

    protected void stopBdLoc() {
        if (baiDuLocation != null) {
            baiDuLocation.stop();
        }
    }

    /***
     * 销毁当前activity
     */
    public void finishActivity() {
        //先清空所有fragment
        int backStackCount = fragmentManager.getBackStackEntryCount();
        for (int i = 0; i < backStackCount; i++) {
            fragmentManager.popBackStack();
        }
        if (fragmentStack != null && fragmentStack.size() != 0) {
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.remove(fragmentStack.get(0));
            ft.commitAllowingStateLoss();
            fragmentStack.removeAllElements();
        }
        this.finish();
    }


    /**
     * 加载布局(该方法执行在onCreate中)
     */
    protected abstract void loadViewLayout();

    /**
     * find控件(该方法执行在onCreate中)
     */
    protected abstract void findViewById();

    /**
     * 后台获取数据(该方法执行在onStart中)
     */
    protected abstract void processLogic();

    /**
     * 设置监听(该方法执行在onStart中)
     */
    protected abstract void setListener();

    /**
     * 重新获取数据
     */
    protected abstract void getDataAgain();

    /**
     * 如果是tab的子activity，返回编号
     */
    protected int getTabIndex() {
        return 0;
    }
}
