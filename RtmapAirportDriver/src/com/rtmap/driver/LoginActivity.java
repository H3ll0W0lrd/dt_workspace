package com.rtmap.driver;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.rtmap.driver.beans.LoginBean;
import com.rtmap.driver.util.MyUtil;
import com.rtmap.driver.util.PreferencesUtil;
import com.rtmap.driver.util.ScreenUtil;
import com.rtmap.driver.util.T;
import com.rtmap.driver.util.TimeUtil;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends BaseActivity implements View.OnClickListener {
    private static boolean isLogin = false;

    private TextView etDriverId;
    private TextView etCarId;
    private EditText etPassword;
    private EditText etDeviceId;
    private TextView etTerminalNo;

    private String driverId;
    private String carId;
    private String password;
    private String deviceId;
    private String terminalNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initView();
    }

    @Override
    protected void onStart() {

        init();

        super.onStart();
    }

    private void init() {
        isLogin = PreferencesUtil.getObj(LoginBean.PRE_LOGIN) == null ? false : true;

        if (isLogin) {
            toMainActivity();
            this.finish();
        }

    }

    private void initView() {
        etDriverId = (TextView) findViewById(R.id.et_driver_id);
        etCarId = (TextView) findViewById(R.id.et_car_id);
        etPassword = (EditText) findViewById(R.id.et_password);
        etDeviceId = (EditText) findViewById(R.id.et_device_id);
        etTerminalNo = (TextView) findViewById(R.id.et_terminal_no);

        etDriverId.setOnClickListener(this);
        etCarId.setOnClickListener(this);
        etTerminalNo.setOnClickListener(this);

        String defaultDeviceId = PreferencesUtil.getString("deviceId", "");
        etDeviceId.setText(defaultDeviceId);

        String terminalNo = PreferencesUtil.getString("terminalNo", "");
        etTerminalNo.setText(terminalNo);

    }

    public void myClick(View v) {
        switch (v.getId()) {
            case R.id.lonin_bt: {

                saveLogin();
            }

            break;
            case R.id.upload_ftp_bt: {
                Intent intent = new Intent(this, FtpActivity.class);
                startActivity(intent);
            }
            break;

            default:
                break;
        }
    }

    private void toMainActivity() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
    }

    private void saveLogin() {
        driverId = etDriverId.getText().toString().trim();
        carId = etCarId.getText().toString().trim();
        password = etPassword.getText().toString().trim();
        deviceId = etDeviceId.getText().toString().trim();
        terminalNo = etTerminalNo.getText().toString().trim();

        if (TextUtils.isEmpty(deviceId)) {
            T.sCustom("请输入设备ID");
            return;
        }
        if (TextUtils.isEmpty(terminalNo)) {
            T.sCustom("请输航站楼编号");
            return;
        }
        if (TextUtils.isEmpty(driverId)) {
            T.sCustom("请输入司机ID");
            return;
        }
        if (TextUtils.isEmpty(carId)) {
            T.sCustom("请输入车辆ID");
            return;
        }
        if (TextUtils.isEmpty(carId)) {
            T.sCustom("请输入密码");
            return;
        }

        driverId = driverId.toUpperCase();
        terminalNo = terminalNo.toUpperCase();

        LoginBean l = new LoginBean();
        l.setDriverId(driverId);
        l.setCarId(carId);
        l.setPassword(password);
        l.setDeviceId(deviceId);
        l.setLoginTime(TimeUtil.getTime1());
        l.setTerminalNo(terminalNo);

        T.sCustom("登陆成功");
        PreferencesUtil.putObj(LoginBean.PRE_LOGIN, l);

        PreferencesUtil.putString("deviceId", deviceId);
        PreferencesUtil.putString("terminalNo", terminalNo);
        PreferencesUtil.putString("driverId", driverId);

        MyUtil.doLogin(driverId, carId, deviceId, terminalNo);

        toMainActivity();
        this.finish();
    }

    /***
     * 显示列表选择框
     *
     * @param textView
     * @param listStr
     */
    private void showUnHappyPopWindow(final TextView textView, List<String> listStr) {

        // 一个自定义的布局，作为显示的内容
        View contentView = LayoutInflater.from(this).inflate(
                R.layout.layout_pop_window_unhappy, null);


        int width = textView.getWidth();
        int height = ScreenUtil.dip2px(this, 100);

        final PopupWindow popupWindow = new PopupWindow(contentView, width, height, true);

        popupWindow.setTouchable(true);

        popupWindow.setTouchInterceptor(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {


                return false;
                // 这里如果返回true的话，touch事件将被拦截
                // 拦截后 PopupWindow的onTouchEvent不被调用，这样点击外部区域无法dismiss
            }
        });

        // 设置按钮的点击事件
        ListView listView = (ListView) contentView.findViewById(R.id.pop_list_view);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String s = (String) parent.getAdapter().getItem(position);
                textView.setText(s);
                popupWindow.dismiss();
            }
        });
        listView.setAdapter(new PopWindowAdapter(listStr));

        // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
        // 我觉得这里是API的一个bug
        popupWindow.setBackgroundDrawable(getResources().getDrawable(
                R.drawable.bg_pop_window_transparent));

        // 设置好参数之后再show
        popupWindow.showAsDropDown(textView);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.et_driver_id: {
                List<String> data = new ArrayList<String>();
                data.add("A01");
                data.add("A02");
                data.add("A03");
                data.add("A04");
                data.add("A05");
                data.add("A06");
                data.add("B01");
                data.add("B02");
                data.add("B03");
                data.add("B04");
                data.add("B05");
                data.add("B06");
                showUnHappyPopWindow((TextView) view, data);
            }
            break;
            case R.id.et_car_id: {
                if (etTerminalNo.getText().toString().equals("")) {
                    T.s("请选择航站楼");
                } else {
                    List<String> data = new ArrayList<String>();

                    if (etTerminalNo.getText().toString().equals("T3C")) {
//                  T3C      19、20、26、38、39、43
                        data.add("19");
                        data.add("20");
                        data.add("26");
                        data.add("38");
                        data.add("39");
                        data.add("43");
                    } else {
                        //T3E有 2、14、27、28、29、36
                        data.add("2");
                        data.add("14");
                        data.add("27");
                        data.add("28");
                        data.add("29");
                        data.add("36");
                    }

                    showUnHappyPopWindow((TextView) view, data);
                }
            }
            break;
            case R.id.et_terminal_no: {
                List<String> data = new ArrayList<String>();
                data.add("T3C");
                data.add("T3E");
                showUnHappyPopWindow((TextView) view, data);
            }
            break;
        }
    }

    private class PopWindowAdapter extends BaseAdapter {
        private List<String> dataStrs;

        public PopWindowAdapter(List<String> lists) {
            dataStrs = new ArrayList<String>();
            dataStrs.addAll(lists);
        }

        @Override
        public int getCount() {
            return dataStrs.size();
        }

        @Override
        public Object getItem(int position) {
            return dataStrs.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(LoginActivity.this).inflate(R.layout.layout_pop_window_unhappy_item, null);
            }
            TextView text = (TextView) convertView.findViewById(R.id.item_text);
            String unHappyStr = dataStrs.get(position);
            text.setText(unHappyStr);
            return convertView;
        }
    }
}
