package com.rtmap.wisdom.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.rtm.common.model.POI;
import com.rtmap.wisdom.R;
import com.rtmap.wisdom.core.DTBaseAdapter;
import com.rtmap.wisdom.util.DTUIUtil;

public class SearchListAdapter extends DTBaseAdapter<POI> {

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = DTUIUtil.inflate(R.layout.search_list_item);
			holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(R.id.name);
			holder.floor = (TextView) convertView.findViewById(R.id.floor);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.name.setText(getItem(position).getName());
		holder.floor.setText(getItem(position).getFloor());
		return convertView;
	}

	static class ViewHolder {
		TextView name;
		TextView floor;
	}
}
