package com.rtmap.indoor_switch.pages;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;

import com.rtm.common.model.POI;
import com.airport.test.R;
import com.rtmap.indoor_switch.base.BaseActivity;
import com.rtmap.indoor_switch.base.BaseFragment;

public class PoiDetailFragment extends BaseFragment implements View.OnClickListener, OnTouchListener {

    private Button btnNavi;
    private Button btnCancel;
    private PoiDetailCallBack callback;
    private TextView contentTextView;
    private POI poi;

    public PoiDetailFragment() {
    }


    @Override
    protected View initView(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.fragment_poi_detail, null);
        init(view);
        return view;
    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    public void setPoiDetailCallBack(PoiDetailCallBack callback) {
        this.callback = callback;
    }

    public void setPoi(POI poi) {
        this.poi = poi;

    }

    private void init(View v) {
        btnNavi = (Button) v.findViewById(R.id.btn_navi_start);
        btnCancel = (Button) v.findViewById(R.id.btn_navi_end);
        contentTextView = (TextView) v.findViewById(R.id.textView1);

        v.setOnTouchListener(this);
        btnNavi.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        contentTextView.setText(poi.getName());
    }

    @Override
    public void onClick(View arg0) {
        switch (arg0.getId()) {
            case R.id.btn_navi_start:
                if (callback != null) {
                    callback.onSetStart(poi);
                }
                ((BaseActivity) getActivity()).backStackFragment();
                break;
            case R.id.btn_navi_end:
                if (callback != null) {
                    callback.onSetEnd(poi);
                }
                ((BaseActivity) getActivity()).backStackFragment();
                break;

            default:
                break;
        }
    }

    public interface PoiDetailCallBack {
        public void onSetStart(POI poi);

        public void onSetEnd(POI poi);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                ((BaseActivity) getActivity()).backStackFragment();
                break;
        }
        return true;
    }
}
