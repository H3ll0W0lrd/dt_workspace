package com.rtmap.indoor_switch.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.airport.test.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ly on 15-8-3.
 */
public class CityListAdapter extends BaseAdapter {
    private List<String> cityList = new ArrayList<>();
    private Context mContext;

    public CityListAdapter(Context context) {
        mContext = context;
    }

    public void setData(List<String> citys) {
        cityList.clear();
        cityList.addAll(citys);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return cityList.size();
    }

    @Override
    public Object getItem(int position) {
        return cityList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.fragment_city_list_item, null);
        }
        TextView city = (TextView) convertView.findViewById(R.id.tv_city);
        city.setText(cityList.get(position));

        return convertView;
    }
}
