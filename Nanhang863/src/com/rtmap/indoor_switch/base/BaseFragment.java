package com.rtmap.indoor_switch.base;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 基本Fragment
 * Created by fushenghua on 15/3/17.
 */
public abstract class BaseFragment extends Fragment {
    protected Context context;
    public View rootView;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        context = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = initView(inflater);
        setListener();
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        ((BaseActivity) getActivity()).fragmentDestroy(this);
        super.onDestroy();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        initData(savedInstanceState);
    }

    public View getRootView() {
        return rootView;
    }

    protected abstract View initView(LayoutInflater inflater);

    protected abstract void setListener();

    protected abstract void initData(Bundle savedInstanceState);

    public void backFragment() {
        ((BaseActivity) getActivity()).backStackFragment();
    }

    public void finishActivity() {
        ((BaseActivity) getActivity()).finishActivity();
    }

    public void pushActivity(Class<?> tClass,Bundle data) {
        ((BaseActivity)getActivity()).pushActivity(tClass,data);
    }
}
