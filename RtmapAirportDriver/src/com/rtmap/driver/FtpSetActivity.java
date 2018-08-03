package com.rtmap.driver;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.rtmap.driver.util.PreferencesUtil;
import com.rtmap.driver.util.T;

public class FtpSetActivity extends Activity {

    private EditText hostText, portText, userNameText, passWordText;
    private TextView stateTextView;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String state = msg.getData().getString("state");
            String str = stateTextView.getText().toString()+"\n";
            stateTextView.setText(str+state);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ftp_set);

        hostText = (EditText) findViewById(R.id.edt_host);
        portText = (EditText) findViewById(R.id.edt_port);
        userNameText = (EditText) findViewById(R.id.edt_user_name);
        passWordText = (EditText) findViewById(R.id.edt_pass_word);
        stateTextView = (TextView) findViewById(R.id.tv_state);

        findViewById(R.id.btn_conf).setOnClickListener(new View.OnClickListener() {
                                                             @Override
                                                             public void onClick(View view) {
                                                                 if (hostText.getText().toString().length() == 0 ||
                                                                         portText.getText().toString().length() == 0 ||
                                                                         userNameText.getText().toString().length() == 0 ||
                                                                         passWordText.getText().toString().length() == 0) {
                                                                     T.s("参数不能为空");
                                                                 }
                                                                 saveParam();
                                                             }

                                                         }
        );
        initParam();
    }

    private void initParam() {
        String host = PreferencesUtil.getString("host", "");
        String port = PreferencesUtil.getString("port", "21");
        String userName =  PreferencesUtil.getString("userName", "");
        String passWord = PreferencesUtil.getString("passWord","");
        hostText.setText(host);
        portText.setText(port);
        userNameText.setText(userName);
        passWordText.setText(passWord);
    }

    private void saveParam() {
        PreferencesUtil.putString("host",hostText.getText().toString());
        PreferencesUtil.putString("port",portText.getText().toString());
        PreferencesUtil.putString("userName",userNameText.getText().toString());
        PreferencesUtil.putString("passWord",passWordText.getText().toString());
        this.finish();
    }


}