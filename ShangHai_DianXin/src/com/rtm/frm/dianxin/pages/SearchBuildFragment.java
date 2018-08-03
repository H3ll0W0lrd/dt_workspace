package com.rtm.frm.dianxin.pages;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.rtm.frm.dianxin.R;
import com.rtm.frm.dianxin.adapter.SearchBuildAdapter;
import com.rtm.frm.dianxin.base.BaseActivity;
import com.rtm.frm.dianxin.base.BaseFragment;
import com.rtm.frm.dianxin.bean.PrivateBuild;
import com.rtm.frm.dianxin.manager.AppContext;
import com.rtm.frm.dianxin.utils.DialogUtil;
import com.rtm.frm.dianxin.utils.InputUtil;
import com.rtm.frm.dianxin.utils.RMlbsUtils;
import com.rtm.frm.dianxin.utils.SharePrefUtil;
import com.rtm.frm.dianxin.utils.StringUtils;
import com.rtm.frm.dianxin.utils.ToastUtils;
import com.rtm.frm.model.BuildInfo;
import com.rtm.frm.model.POI;
import com.rtm.frm.model.RMRoute;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ly on 15-7-24.
 */
public class SearchBuildFragment extends BaseFragment implements View.OnClickListener, RMlbsUtils.OnRmGetFinishListener, AdapterView.OnItemClickListener {

    private EditText edtKeyWord;
    private TextView tvSearch;
    private ListView lvSearchResult;

    private MyLocationData myLocationData;

    private String chooseCityName;

    private ImageView imgBack;

    private SearchBuildAdapter searchBuildAdapter;


    public SearchBuildFragment() {

    }

    public void setLocationData(MyLocationData locationData, String cityName) {
        myLocationData = locationData;
        chooseCityName = cityName;
    }

    @Override
    protected View initView(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.fragment_search_build, null);
        edtKeyWord = (EditText) view.findViewById(R.id.edt_key_word);
        tvSearch = (TextView) view.findViewById(R.id.tv_search);
        lvSearchResult = (ListView) view.findViewById(R.id.lv_search_result);
        imgBack = (ImageView) view.findViewById(R.id.img_back);
        if (myLocationData != null) {
            searchBuildAdapter = new SearchBuildAdapter(getActivity(), new LatLng(myLocationData.latitude, myLocationData.longitude));
        } else {
            searchBuildAdapter = new SearchBuildAdapter(getActivity(), null);
        }
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        InputUtil.closeInputMethod(edtKeyWord, getActivity());
                        ((BaseActivity) getActivity()).backStackFragment();
                        break;
                }
                return true;
            }
        });
        return view;
    }

    @Override
    protected void setListener() {
        edtKeyWord.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(edtKeyWord.getText().toString())) {

                } else {

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        tvSearch.setOnClickListener(this);
        lvSearchResult.setAdapter(searchBuildAdapter);
        lvSearchResult.setOnItemClickListener(this);
        if (chooseCityName == null) {
            edtKeyWord.setHint("搜索私有建筑");
        }
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        RotateAnimation ro = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        ro.setDuration(300);
        imgBack.startAnimation(ro);
    }

    private void searchKeyWord(String keyWord) {
//        if (!StringUtils.isEmpty(keyWord)) {
        InputUtil.closeInputMethod(edtKeyWord, getActivity());
        showLoading();
        if (chooseCityName == null) {//如果城市为null，搜索本地私有建筑
            String username = SharePrefUtil.getString(AppContext.instance(), "username", "");
            String password = SharePrefUtil.getString(AppContext.instance(), "password", "");
            RMlbsUtils.getInstance().getPrivateBuildList(username,password, this);
        } else {
            RMlbsUtils.getInstance().getBuildList(chooseCityName, this);
        }
//        } else {
//            ToastUtils.shortToast(R.string.please_input_key_word);
//        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_search: {
                searchKeyWord(edtKeyWord.getText().toString());
            }
            break;
        }
    }

    private Dialog loading;

    private void showLoading() {
        try {

            if (loading == null) {
                loading = DialogUtil.getLoadingDialog(null, false, null);
            }
            if (loading.isShowing()) {
                return;
            }
            loading.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dismissLoading() {
        if (loading != null && loading.isShowing()) {
            loading.dismiss();
        }
    }

    @Override
    public void onGetCityListFinish(List<String> result) {

    }

    @Override
    public void onGetBuildListFinish(List<BuildInfo> result) {
        String keyword = edtKeyWord.getText().toString();
        if (StringUtils.isEmpty(keyword)) {
            searchBuildAdapter.setData(result);
            ToastUtils.shortToastCenter(chooseCityName + "全市");
        } else if (result != null && result.size() != 0) {

            List<BuildInfo> buildInfos = filterBuildByKeyWord(keyword,result);
            searchBuildAdapter.setData(buildInfos);
        } else {
            ToastUtils.shortToast(R.string.no_result);
            searchBuildAdapter.clearData();
        }
        dismissLoading();
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
        String keyword = edtKeyWord.getText().toString();
        List<BuildInfo> buildInfos = new ArrayList<BuildInfo>();
        if (result != null && result.size()!=0) {
            for (PrivateBuild build : result) {
                BuildInfo buildInfo = new BuildInfo();

                buildInfo.setBuildName(build.getBuildName());
                buildInfo.setBuildId(build.getBuildId());
                buildInfos.add(buildInfo);
            }
            if (StringUtils.isEmpty(keyword)) {//如果关键字为空，显示全部私有建筑
                searchBuildAdapter.setData(buildInfos);
            } else {
                List<BuildInfo> filterBuildInfo = filterBuildByKeyWord(keyword,buildInfos);
                searchBuildAdapter.setData(filterBuildInfo);
            }
        } else {
            ToastUtils.shortToast(R.string.no_result);
            searchBuildAdapter.clearData();
        }
        dismissLoading();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Adapter adapter = parent.getAdapter();
        BuildInfo buildInfo = (BuildInfo) adapter.getItem(position);
        if (buildInfo != null) {
            MainFragment fragment = (MainFragment) ((BaseActivity) getActivity()).getFragmentByClass(MainFragment.class);
            if (fragment != null) {
                fragment.findBuildOnMap(buildInfo);
            }
        }
        ((BaseActivity) getActivity()).backStackFragment();
    }

    private List<BuildInfo> filterBuildByKeyWord(String keyword,List<BuildInfo> result) {
        List<BuildInfo> filteredBuildInfos = new ArrayList<BuildInfo>();
        if (result.size() == 1) {
            BuildInfo b = result.get(0);
            if (b != null) {
                int index = b.getBuildName().indexOf(keyword);
                if (index >= 0) {//说明建筑名称包含关键字
                    filteredBuildInfos.add(b);
                }
            }
        } else {
            boolean isOdd = result.size() % 2 == 0 ? false : true;//长度是否为奇数

            for (int i = 0; i < result.size() / 2; ++i) {
                BuildInfo frontBuild = result.get(i);
                BuildInfo behindBuild = result.get((result.size() - 1 - i));
                if (frontBuild != null) {
                    int index = frontBuild.getBuildName().indexOf(keyword);
                    if (index >= 0) {//说明建筑名称包含关键字
                        filteredBuildInfos.add(frontBuild);
                    }
                }
                if (behindBuild != null) {
                    int index = behindBuild.getBuildName().indexOf(keyword);
                    if (index >= 0) {//说明建筑名称包含关键字
                        filteredBuildInfos.add(behindBuild);
                    }
                }
            }

            if (isOdd) {
                BuildInfo lastBuild = result.get(result.size() / 2 + 1);
                if (lastBuild != null) {
                    int index = lastBuild.getBuildName().indexOf(keyword);
                    if (index >= 0) {//说明建筑名称包含关键字
                        filteredBuildInfos.add(lastBuild);
                    }
                }
            }
        }
        return filteredBuildInfos;
    }
}
