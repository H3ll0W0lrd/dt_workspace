package com.rtm.frm.dianxin;

import android.app.Dialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.baidu.location.BDLocation;
import com.rtm.frm.model.BuildInfo;
import com.rtm.frm.model.POI;
import com.rtm.frm.model.RMRoute;
import com.rtm.frm.dianxin.base.BaseActivity;
import com.rtm.frm.dianxin.bean.PrivateBuild;
import com.rtm.frm.dianxin.manager.AppContext;
import com.rtm.frm.dianxin.utils.RMlbsUtils;
import com.rtm.frm.dianxin.utils.SharePrefUtil;
import com.rtm.frm.dianxin.utils.ToastUtils;
import com.rtm.frm.dianxin.utils.DialogUtil;
import com.rtm.frm.dianxin.utils.StringUtils;

import java.util.List;


public class LoginActivity extends BaseActivity implements RMlbsUtils.OnRmGetFinishListener {

    private EditText edtUserName;
    private EditText edtPassWord;
    private Button btnLogin;

    @Override
    protected void loadViewLayout() {
        setContentView(R.layout.activity_login);
    }

    @Override
    protected void findViewById() {
        edtUserName = (EditText) findViewById(R.id.edt_user_name);
        edtPassWord = (EditText) findViewById(R.id.edt_pass_word);
        btnLogin = (Button) findViewById(R.id.btn_login);
    }

    @Override
    protected void processLogic() {
    }

    @Override
    protected void setListener() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doLogin();
            }
        });
    }

    private void doLogin() {
        String username = edtUserName.getText().toString();
        String password = edtPassWord.getText().toString();

        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            ToastUtils.shortToast(R.string.input_username_password);
        } else {
            showLoading();
            RMlbsUtils.getInstance().getPrivateBuildList(username, password, this);
        }

    }

    @Override
    protected void getDataAgain() {

    }


    @Override
    public void onReceiveLocation(BDLocation bdLocation) {

    }

    @Override
    public void onGetCityListFinish(List<String> result) {

    }

    @Override
    public void onGetBuildListFinish(List<BuildInfo> result) {

    }

    @Override
    public void onGetBuildDetailFinish(BuildInfo result) {

    }

    @Override
    public void onGetNavigationFinish(RMRoute result) {

    }

    @Override
    public void onGetPoiSearchFinish(List<POI> result) {

    }

    @Override
    public void onGetPrivateBuildFinish(List<PrivateBuild> result) {
        dismissLoading();
        if (result == null) {
            ToastUtils.shortToast(R.string.login_error);
        } else {
            String username = edtUserName.getText().toString();
            String password = edtPassWord.getText().toString();
            SharePrefUtil.saveString(AppContext.instance(), "username", username);
            SharePrefUtil.saveString(AppContext.instance(), "password", password);

            ToastUtils.shortToast(R.string.login_success);
            pushActivity(MainActivity.class, null);
            this.finish();
        }
    }

    private Dialog loading;

    private void showLoading() {
        if (loading == null) {
            loading = DialogUtil.getLoadingDialog(this, false, null);
        }
        if (loading.isShowing()) {
            return;
        }
        loading.show();
    }

    private void dismissLoading() {
        if (loading != null && loading.isShowing()) {
            loading.dismiss();
        }
    }
}
