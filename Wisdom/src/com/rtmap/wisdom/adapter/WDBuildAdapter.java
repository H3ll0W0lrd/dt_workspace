package com.rtmap.wisdom.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rtm.common.model.BuildInfo;
import com.rtmap.wisdom.R;
import com.rtmap.wisdom.core.DTBaseAdapter;
import com.rtmap.wisdom.util.DTUIUtil;

public class WDBuildAdapter extends DTBaseAdapter<BuildInfo> {

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = DTUIUtil.inflate(R.layout.build_item);
			holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(R.id.name);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		BuildInfo info = getItem(position);
		holder.name.setText(info.getBuildName());
		return convertView;
	}

	static class ViewHolder {
		TextView name;
	}
}
