package com.airport.test.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.airport.test.R;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.dingtao.libs.util.DTUIUtil;
import com.rtm.common.model.BuildInfo;

public class SearchAdapter extends LCBaseAdapter<BuildInfo> {

	private LatLng myLatLng;

	public SearchAdapter(LatLng myLatLng) {
		this.myLatLng = myLatLng;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHodler holder;
		if (convertView == null) {
			convertView = DTUIUtil.inflate(R.layout.search_build_item);
			holder = new ViewHodler();
			holder.text = (TextView) convertView
					.findViewById(R.id.tv_build_name);
			holder.dis = (TextView) convertView.findViewById(R.id.tv_build_dis);
			convertView.setTag(holder);
		}
		holder = (ViewHodler) convertView.getTag();
		BuildInfo buildInfo = getItem(position);
		holder.text.setText(buildInfo.getBuildName());
		if (myLatLng != null) {
			double d = DistanceUtil.getDistance(myLatLng,
					new LatLng(buildInfo.getLat(), buildInfo.getLong()));// 单位为米
			d = d / 1000;
			if (d < 1) {
				holder.dis.setText("<1km");
			} else {
				int i = (int) d * 100;
				d = i / 100d;
				holder.dis.setText(d + "km");
			}
		} else {
			holder.dis.setText("");
		}
		return convertView;
	}

	class ViewHodler {
		public TextView text;
		public TextView dis;
	}

}
