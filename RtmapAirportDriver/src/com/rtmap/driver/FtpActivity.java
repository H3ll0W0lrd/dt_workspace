package com.rtmap.driver;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import com.rtmap.driver.ftp.Ftp;
import com.rtmap.driver.ftp.FtpUtil;
import com.rtmap.driver.util.PreferencesUtil;
import com.rtmap.driver.util.T;

import java.io.File;

public class FtpActivity extends Activity {

    private TextView stateTextView;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String state = msg.getData().getString("state");
            String str = stateTextView.getText().toString() + "\n";
            stateTextView.setText(str + state);
        }
    };

    private String hostStr, portStr, userNameStr, passWordStr;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ftp);

        stateTextView = (TextView) findViewById(R.id.tv_state);

        findViewById(R.id.btn_upload).setOnClickListener(new View.OnClickListener() {
                                                             @Override
                                                             public void onClick(View view) {
                                                                 if (hostStr.length() == 0 ||
                                                                         portStr.length() == 0 ||
                                                                         userNameStr.length() == 0 ||
                                                                         passWordStr.length() == 0) {
                                                                     T.s("请设置ftp服务器参数");
                                                                     Intent intent = new Intent(FtpActivity.this, FtpSetActivity.class);
                                                                     FtpActivity.this.startActivity(intent);
                                                                 } else {
                                                                     stateTextView.setText("准备中...");
                                                                     Ftp.Config config = new Ftp.Config(hostStr,
                                                                             Integer.valueOf(portStr),
                                                                             userNameStr, passWordStr);
                                                                     new UploadThread(handler, config).start();
                                                                 }
                                                             }

                                                         }
        );
        findViewById(R.id.btn_set).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FtpActivity.this, FtpSetActivity.class);
                FtpActivity.this.startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        initParam();
        super.onResume();
    }

    private void initParam() {
        hostStr = PreferencesUtil.getString("host", "");
        portStr = PreferencesUtil.getString("port", "21");
        userNameStr = PreferencesUtil.getString("userName", "");
        passWordStr = PreferencesUtil.getString("passWord", "");
    }

    class UploadThread extends Thread {
        private Ftp.Config config;
        private Handler handler;

        public UploadThread(Handler handler, Ftp.Config config) {
            this.config = config;
            this.handler = handler;
        }

        private boolean isConnSucc = false;
        private boolean isUploadSucc = false;
        private boolean isUploading = false;

        @Override
        public void run() {

            FtpUtil.instance().upLoadLog(config, new Ftp.UploadProgressListener() {
                @Override
                public void onUploadProgress(String currentStep, long uploadSize, File file) {
                    if (currentStep.equals(Ftp.FTP_CONNECT_SUCCESSS)) {
                        if (isConnSucc) {
                            return;
                        } else {
                            isConnSucc = true;
                        }
                    }
                    if (currentStep.equals(Ftp.FTP_UPLOAD_SUCCESS)) {
                        if (isUploadSucc) {
                            return;
                        } else {
                            isUploadSucc = true;
                        }
                    }
                    if (currentStep.equals(Ftp.FTP_UPLOAD_LOADING)) {
                        if (isUploading) {
                            return;
                        } else {
                            isUploading = true;
                        }
                    }
                    Message msg = handler.obtainMessage();
                    Bundle bundle = new Bundle();
                    bundle.putString("state", currentStep);
                    msg.setData(bundle);
                    msg.what = 1;
                    handler.sendMessage(msg);
                }
            });
        }
    }

}