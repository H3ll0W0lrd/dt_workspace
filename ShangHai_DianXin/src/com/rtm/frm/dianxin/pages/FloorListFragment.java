package com.rtm.frm.dianxin.pages;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListView;

import com.rtm.frm.dianxin.R;
import com.rtm.frm.dianxin.adapter.FloorListAdapter;
import com.rtm.frm.dianxin.base.BaseActivity;
import com.rtm.frm.dianxin.base.BaseFragment;
import com.rtm.frm.dianxin.utils.RMlbsUtils;
import com.rtm.frm.model.Floor;

import java.util.List;

/**
 * Created by ly on 15-7-24.
 */
public class FloorListFragment extends BaseFragment implements AdapterView.OnItemClickListener {

    private ListView lvFloorList;
    private FloorListAdapter floorListAdapter;

    private String buildId;


    public FloorListFragment() {

    }

    public void setBuildId(String buildId) {
        this.buildId = buildId;

    }

    @Override
    protected View initView(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.fragment_floor_list, null);
        lvFloorList = (ListView) view.findViewById(R.id.lv_floor_list);
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
        lvFloorList.setOnItemClickListener(this);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        List<Floor> floors = RMlbsUtils.getInstance().getLocFloorByBuildId(this.buildId);
        floorListAdapter = new FloorListAdapter(getActivity());
        lvFloorList.setAdapter(floorListAdapter);
        if (floors != null) {
            floorListAdapter.setData(floors);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Adapter adapter = parent.getAdapter();
        Floor floor = (Floor) adapter.getItem(position);
        if (floor != null) {
            RtmFragment fragment = (RtmFragment) ((BaseActivity) getActivity()).getFragmentByClass(RtmFragment.class);
            if (fragment != null) {
                fragment.switchFloor(this.buildId,floor.getFloor());
            }
        }
        ((BaseActivity) getActivity()).backStackFragment();
    }
}
