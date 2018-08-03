package com.rtmap.indoor_switch;

import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import com.airport.test.R;
import com.baidu.location.BDLocation;
import com.rtmap.indoor_switch.base.BaseActivity;
import com.rtmap.indoor_switch.pages.MainFragment;
import com.rtmap.indoor_switch.utils.RMlbsUtils;
import com.rtmap.indoor_switch.utils.ToastUtils;

/***
 * 此版本代码，基于数据组定制APP框架分支
 */
public class MainActivity extends BaseActivity{

    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.activity_main);
        pushFragment(new MainFragment(),R.id.main_content);
    }

    @Override
    protected void findViewById() {
    }

    @Override
    protected void processLogic() {
    }

    @Override
    protected void setListener() {
    }

    @Override
    protected void getDataAgain() {

    }

    @Override
    public void onClick(View v) {
    }



    private long firstTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) { // 按下的如果是BACK，同时没有重复
            if (getStackSize() == 1) {
                long secondTime = System.currentTimeMillis();
                if (secondTime - firstTime > 2000) { // 如果两次按键时间间隔大于2秒，则不退出
                    ToastUtils.shortToast("再按一次退出程序");
                    firstTime = secondTime;// 更新firstTime
                    return true;
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onReceiveLocation(BDLocation bdLocation) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RMlbsUtils.getInstance().setNull();
    }
}
