package com.rtmap.indoor_switch.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.rtm.common.model.Floor;
import com.airport.test.R;

/**
 * Created by ly on 15-8-3.
 */
public class FloorListAdapter extends BaseAdapter {
    private List<Floor> floorList = new ArrayList<>();
    private Context mContext;

    public FloorListAdapter(Context context) {
        mContext = context;
    }

    public void setData(List<Floor> floors) {
        floorList.clear();
        floorList.addAll(floors);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return floorList.size();
    }

    @Override
    public Object getItem(int position) {
        return floorList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.fragment_floor_list_item, null);
        }
        Floor floor = floorList.get(position);
        TextView tvFloor = (TextView) convertView.findViewById(R.id.tv_floor);
        TextView tvDes = (TextView) convertView.findViewById(R.id.tv_floor_des);
        tvFloor.setText(floor.getFloor());
        tvDes.setText(floor.getDescription());

        return convertView;
    }
}
