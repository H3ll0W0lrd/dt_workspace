package com.baidu.push.example;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Audio;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.baidu.android.pushservice.CustomPushNotificationBuilder;
import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.rtm.gf.R;

/*
 * 云推送Demo主Activity。
 * 代码中，注释以Push标注开头的，表示接下来的代码块是Push接口调用示例
 */
public class PushDemoActivity extends Activity implements View.OnClickListener {

    private static final String TAG = PushDemoActivity.class.getSimpleName();
    RelativeLayout mainLayout = null;
    int akBtnId = 0;
    int clearLogBtnId = 0;
    Button initWithApiKey = null;
    Button clearLog = null;
    TextView logText = null;
    ScrollView scrollView = null;
    public static int initialCnt = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // checkApikey();
        Utils.logStringCache = Utils.getLogText(getApplicationContext());

        Resources resource = this.getResources();
        String pkgName = this.getPackageName();

        setContentView(resource.getIdentifier("main", "layout", pkgName));
        akBtnId = resource.getIdentifier("btn_initAK", "id", pkgName);
        clearLogBtnId = resource.getIdentifier("btn_clear_log", "id", pkgName);

        initWithApiKey = (Button) findViewById(akBtnId);
        clearLog = (Button) findViewById(clearLogBtnId);

        logText = (TextView) findViewById(resource.getIdentifier("text_log",
                "id", pkgName));
        scrollView = (ScrollView) findViewById(resource.getIdentifier(
                "stroll_text", "id", pkgName));

        initWithApiKey.setOnClickListener(this);
        clearLog.setOnClickListener(this);

        // Push: 以apikey的方式登录，一般放在主Activity的onCreate中。
        // 这里把apikey存放于manifest文件中，只是一种存放方式，
        // 您可以用自定义常量等其它方式实现，来替换参数中的Utils.getMetaValue(PushDemoActivity.this,
        // "api_key")
//        ！！ 请将AndroidManifest.xml 128 api_key 字段值修改为自己的 api_key 方可使用 ！！
//        ！！ ATTENTION：You need to modify the value of api_key to your own at row 128 in AndroidManifest.xml to use this Demo !!
        PushManager.startWork(getApplicationContext(),

                PushConstants.LOGIN_TYPE_API_KEY,
                Utils.getMetaValue(PushDemoActivity.this, "api_key"));
        String url = getIntent().getStringExtra("url");
        logText.setText(url);
    }
    
    @Override
    public void onClick(View v) {
        if (v.getId() == akBtnId) {
            initWithApiKey();
        } else if (v.getId() == clearLogBtnId) {
            Utils.logStringCache = "";
            Utils.setLogText(getApplicationContext(), Utils.logStringCache);
        }

    }


    // 以apikey的方式绑定
    private void initWithApiKey() {
        // Push: 无账号初始化，用api key绑定
        // checkApikey();
        PushManager.startWork(getApplicationContext(),
                PushConstants.LOGIN_TYPE_API_KEY,
                Utils.getMetaValue(PushDemoActivity.this, "api_key"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        menu.add(Menu.NONE, Menu.FIRST + 1, 1, "关于").setIcon(
                android.R.drawable.ic_menu_info_details);

        menu.add(Menu.NONE, Menu.FIRST + 2, 2, "帮助").setIcon(
                android.R.drawable.ic_menu_help);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (Menu.FIRST + 1 == item.getItemId()) {
            showAbout();
            return true;
        }
        if (Menu.FIRST + 2 == item.getItemId()) {
            showHelp();
            return true;
        }

        return false;
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        return true;
    }

    // 关于
    private void showAbout() {
        Dialog alertDialog = new AlertDialog.Builder(PushDemoActivity.this)
                .setTitle("关于").setMessage(R.string.text_about)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        // TODO Auto-generated method stub
                    }

                }).create();
        alertDialog.show();
    }

    // 帮助
    private void showHelp() {
        Dialog alertDialog = new AlertDialog.Builder(PushDemoActivity.this)
                .setTitle("帮助").setMessage(R.string.text_help)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        // TODO Auto-generated method stub
                    }

                }).create();
        alertDialog.show();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "onResume");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        String url = intent.getStringExtra("url");
        logText.setText(logText.getText().toString()+url);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Utils.setLogText(getApplicationContext(), Utils.logStringCache);
        super.onDestroy();
    }
}
