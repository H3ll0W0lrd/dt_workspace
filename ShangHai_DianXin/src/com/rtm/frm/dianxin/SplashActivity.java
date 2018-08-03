package com.rtm.frm.dianxin;

import android.os.Handler;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.rtm.frm.dianxin.base.BaseActivity;
import com.rtm.frm.dianxin.manager.AppContext;


public class SplashActivity extends BaseActivity {

    private final int JUMP_TO_MAIN = 1;
    private final int JUMP_TO_LOGIN = 2;
    private int jump = JUMP_TO_MAIN;
    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.activity_splash);
    }

    @Override
    protected void findViewById() {
        //设置版本号
        TextView tv_vision = (TextView) findViewById(R.id.tv_splash_vision);
        tv_vision.setText("V " + AppContext.instance().versionName);

        //不判断是否登录
//        String username = SharePrefUtil.getString(AppContext.instance(),"username","");
//        String password = SharePrefUtil.getString(AppContext.instance(),"password","");
//        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
//            jump = JUMP_TO_LOGIN;
//        }
    }

    private boolean isDestory = false;
    @Override
    protected void processLogic() {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (isDestory) {
                    return;
                }
                switch (jump) {
                    case JUMP_TO_MAIN:{
                        pushActivity(MainActivity.class,null);
                    }
                    break;
                    case JUMP_TO_LOGIN:{
                        pushActivity(LoginActivity.class,null);
                    }
                    break;
                }
                SplashActivity.this.finish();
            }
        }, 2000);
    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void getDataAgain() {

    }


    @Override
    public void onReceiveLocation(BDLocation bdLocation) {

    }

    @Override
    protected void onDestroy() {
        isDestory = true;
        super.onDestroy();
    }
}
