package com.rtm.frm.dianxin.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.rtm.frm.dianxin.R;
import com.rtm.frm.model.BuildInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ly on 15-8-3.
 */
public class SearchBuildAdapter extends BaseAdapter {
    private List<BuildInfo> buildInfos = new ArrayList<BuildInfo>();
    private Context mContext;
    private LatLng myLatLng;

    public SearchBuildAdapter(Context context, LatLng latLng) {
        mContext = context;
        this.myLatLng = latLng;
    }

    public void setData(List<BuildInfo> builds) {
        buildInfos.clear();
        buildInfos.addAll(builds);
        notifyDataSetChanged();
    }

    public void clearData() {
        buildInfos.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return buildInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return buildInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.fragment_search_build_item, null);
        }
        BuildInfo buildInfo = buildInfos.get(position);
        TextView bn = (TextView) convertView.findViewById(R.id.tv_build_name);
        TextView dis = (TextView) convertView.findViewById(R.id.tv_build_dis);
        bn.setText(buildInfo.getBuildName());
        if (myLatLng != null) {

            double d = DistanceUtil.getDistance(myLatLng, new LatLng(buildInfo.getLat(), buildInfo.getLong()));//单位为米
            d = d / 1000;
            if (d < 1) {
                dis.setText("<1km");
            } else {
                int i = (int) d * 100;
                d = i / 100d;
                dis.setText(d + "km");
            }
        } else {
            dis.setText("");
        }
        return convertView;
    }
}
