package com.rtm.frm.dianxin.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.rtm.frm.dianxin.R;
import com.rtm.frm.model.POI;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ly on 15-8-3.
 */
public class RtmSearchAdapter extends BaseAdapter {
    private List<POI> poiList = new ArrayList<POI>();
    private Context mContext;

    public RtmSearchAdapter(Context context) {
        mContext = context;
    }

    public void setData(List<POI> pois) {
        poiList.clear();
        poiList.addAll(pois);
        notifyDataSetChanged();
    }

    public void clearData() {
        poiList.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return poiList.size();
    }

    @Override
    public Object getItem(int position) {
        return poiList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.fragment_rtm_search_item, null);
        }
        POI poi = poiList.get(position);
        TextView tvPoiName = (TextView) convertView.findViewById(R.id.tv_poi_name);
        TextView tvPoiFloor = (TextView) convertView.findViewById(R.id.tv_poi_floor);
        tvPoiName.setText(poi.getName());
        tvPoiFloor.setText(poi.getFloor());

        return convertView;
    }
}
