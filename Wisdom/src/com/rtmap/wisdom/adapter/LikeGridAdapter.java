package com.rtmap.wisdom.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.rtmap.wisdom.R;
import com.rtmap.wisdom.core.DTBaseAdapter;
import com.rtmap.wisdom.util.DTUIUtil;
import com.rtmap.wisdom.util.view.DTSatelliteLayout;

public class LikeGridAdapter extends DTBaseAdapter<Integer> {

	@Override
	public int getCount() {
		return DTSatelliteLayout.BG_ICON_ARRAY.length;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = DTUIUtil.inflate(R.layout.search_grid_item);
			holder = new ViewHolder();
			holder.image = (ImageView) convertView.findViewById(R.id.image);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.image
				.setImageResource(DTSatelliteLayout.BG_ICON_ARRAY[position]);
		return convertView;
	}

	static class ViewHolder {
		ImageView image;
	}
}
