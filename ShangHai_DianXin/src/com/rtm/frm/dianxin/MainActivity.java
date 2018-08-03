package com.rtm.frm.dianxin;

import android.view.KeyEvent;
import android.view.View;

import com.baidu.location.BDLocation;
import com.rtm.frm.dianxin.base.BaseActivity;
import com.rtm.frm.dianxin.pages.MainFragment;
import com.rtm.frm.dianxin.utils.DTClockReceiver;
import com.rtm.frm.dianxin.utils.PollingService;
import com.rtm.frm.dianxin.utils.PollingUtils;
import com.rtm.frm.dianxin.utils.RMlbsUtils;
import com.rtm.frm.dianxin.utils.ToastUtils;

/***
 * 此版本代码，基于数据组定制APP框架分支
 */
public class MainActivity extends BaseActivity {

    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.activity_main);
        pushFragment(new MainFragment(), R.id.main_content);
        PollingUtils.startPollingService(this, 2, PollingService.class, DTClockReceiver.ALARM_ALERT_ACTION);
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
