package com.rtmap.indoor_switch.pages;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.airport.test.R;
import com.rtmap.indoor_switch.adapter.CityListAdapter;
import com.rtmap.indoor_switch.base.BaseActivity;
import com.rtmap.indoor_switch.base.BaseFragment;
import com.rtmap.indoor_switch.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ly on 15-7-24.
 */
public class CityListFragment extends BaseFragment implements AdapterView.OnItemClickListener {

    private ListView lvCityList;
    private CityListAdapter cityListAdapter;
    private View attchedView;
    private LinearLayout llCityLayout;

    private List<String> cityList = new ArrayList<>();


    public CityListFragment() {

    }

    public void setCityList(List<String> cityList, View view) {
        this.cityList = cityList;
        attchedView = view;
    }

    @Override
    protected View initView(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.fragment_city_list, null);
        lvCityList = (ListView) view.findViewById(R.id.lv_city_list);
        llCityLayout = (LinearLayout) view.findViewById(R.id.ll_city_list);
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
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
        lvCityList.setOnItemClickListener(this);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        cityListAdapter = new CityListAdapter(getActivity());
        lvCityList.setAdapter(cityListAdapter);
        if (cityList != null) {
            cityListAdapter.setData(cityList);
        }

        Animation alphaAnimation = new AlphaAnimation( 0, 1 );
        alphaAnimation.setDuration(250);
        alphaAnimation.setInterpolator(new LinearInterpolator());
        alphaAnimation.setRepeatCount(0);
        alphaAnimation.setFillAfter(true);
        alphaAnimation.setRepeatMode(Animation.REVERSE);

        llCityLayout.startAnimation(alphaAnimation);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Adapter adapter = parent.getAdapter();
        String city = (String) adapter.getItem(position);
        if (!StringUtils.isEmpty(city)) {
            MainFragment fragment = (MainFragment) ((BaseActivity) getActivity()).getFragmentByClass(MainFragment.class);
            if (fragment != null) {
                fragment.getBuildByCity(city);
            }
        }
        ((BaseActivity) getActivity()).backStackFragment();
    }
}
