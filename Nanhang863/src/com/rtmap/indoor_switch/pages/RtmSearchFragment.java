package com.rtmap.indoor_switch.pages;

import java.util.List;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.rtm.common.model.BuildInfo;
import com.rtm.common.model.POI;
import com.rtm.frm.model.RMRoute;
import com.airport.test.R;
import com.rtmap.indoor_switch.adapter.RtmSearchAdapter;
import com.rtmap.indoor_switch.base.BaseActivity;
import com.rtmap.indoor_switch.base.BaseFragment;
import com.rtmap.indoor_switch.bean.PrivateBuild;
import com.rtmap.indoor_switch.utils.DialogUtil;
import com.rtmap.indoor_switch.utils.InputUtil;
import com.rtmap.indoor_switch.utils.RMlbsUtils;
import com.rtmap.indoor_switch.utils.ToastUtils;

/**
 * Created by ly on 15-7-24.
 */
public class RtmSearchFragment extends BaseFragment implements AdapterView.OnItemClickListener, View.OnClickListener, RMlbsUtils.OnRmGetFinishListener {

    private ListView lvSearchResult;
    private RtmSearchAdapter rtmSearchAdapter;

    private String buildId;

    private TextView tvSearch;

    private ImageView imgBack;

    private EditText edtKeyWord;

    public RtmSearchFragment() {

    }

    public void setBuildId(String buildId) {
        this.buildId = buildId;

    }

    @Override
    protected View initView(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.fragment_rtm_search, null);
        lvSearchResult = (ListView) view.findViewById(R.id.lv_search_result);
        tvSearch = (TextView) view.findViewById(R.id.tv_search);
        edtKeyWord = (EditText) view.findViewById(R.id.edt_key_word);
        imgBack = (ImageView) view.findViewById(R.id.img_back);

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
        lvSearchResult.setOnItemClickListener(this);
        tvSearch.setOnClickListener(this);
        edtKeyWord.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (edtKeyWord.getText().toString().length() == 0) {

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        edtKeyWord.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchKeyWord(edtKeyWord.getText().toString());
                }
                return false;
            }
        });
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        rtmSearchAdapter = new RtmSearchAdapter(getActivity());
        lvSearchResult.setAdapter(rtmSearchAdapter);

        RotateAnimation ro = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        ro.setDuration(300);
        imgBack.startAnimation(ro);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        InputUtil.closeInputMethod(edtKeyWord, getActivity());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Adapter adapter = parent.getAdapter();
        POI poi = (POI) adapter.getItem(position);
        if (poi != null) {
            RtmFragment fragment = (RtmFragment) ((BaseActivity) getActivity()).getFragmentByClass(RtmFragment.class);
            if (fragment != null) {
                fragment.showPoiNoMap(poi);
            }
        }
        ((BaseActivity) getActivity()).backStackFragment();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_search: {
                if (TextUtils.isEmpty(edtKeyWord.getText().toString())) {
                    ToastUtils.shortToast(R.string.please_input_key_word);
                } else {
                    searchKeyWord(edtKeyWord.getText().toString());
                }
            }
            break;
        }
    }

    private void searchKeyWord(String keyWord) {
        InputUtil.closeInputMethod(edtKeyWord, getActivity());
        showLoading();
        RMlbsUtils.getInstance().searchPoi(this.buildId, keyWord, this);
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
        dismissLoading();
        if (result == null || result.size() == 0) {
            ToastUtils.shortToast(R.string.no_result);
            clearListViewData();
            return;
        }
        rtmSearchAdapter.setData(result);
    }

    @Override
    public void onGetPrivateBuildFinish(List<PrivateBuild> result) {

    }

    private void clearListViewData() {
        rtmSearchAdapter.clearData();
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
}
