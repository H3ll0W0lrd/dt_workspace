package com.rtmap.wisdom.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rtmap.wisdom.R;
import com.rtmap.wisdom.core.DTBaseAdapter;
import com.rtmap.wisdom.util.DTUIUtil;

public class CityListAdapter extends DTBaseAdapter<String> {

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = DTUIUtil.inflate(R.layout.city_list_item);
			holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(R.id.name);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.name.setText(getItem(position));
		return convertView;
	}

	static class ViewHolder {
		TextView name;
	}
}
