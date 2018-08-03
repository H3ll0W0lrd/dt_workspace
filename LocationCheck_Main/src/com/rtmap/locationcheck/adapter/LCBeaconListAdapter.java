package com.rtmap.locationcheck.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.rtmap.locationcheck.R;
import com.rtmap.locationcheck.core.LCBaseAdapter;
import com.rtmap.locationcheck.core.model.BeaconInfo;
import com.rtmap.locationcheck.util.DTUIUtils;

public class LCBeaconListAdapter extends LCBaseAdapter<BeaconInfo> {

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = DTUIUtils.inflate(R.layout.beacon_list_item);
			holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(R.id.name);
			holder.desc = (TextView) convertView.findViewById(R.id.desc);
			holder.sign = (ImageView) convertView.findViewById(R.id.sign);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		final BeaconInfo p = mList.get(position);
		if (p.getWork_status() == -4) {
			if (p.getEdit_status() == 1) {
				holder.sign.setImageResource(R.drawable.sign_gray);
			} else
				holder.sign.setImageResource(R.drawable.sign_red);
		} else if (p.getWork_status() == 0) {
			if (p.getEdit_status() == 0 || p.getEdit_status() == 2) {// 正常
				holder.sign.setImageResource(R.drawable.sign_green);
			} else if (p.getEdit_status() == 1) {
				holder.sign.setImageResource(R.drawable.sign_gray);
			} else {
				holder.sign.setImageResource(R.drawable.sign_purple);
			}
		} else {
			if (p.getEdit_status() == 1) {
				holder.sign.setImageResource(R.drawable.sign_gray);
			} else {
				holder.sign.setImageResource(R.drawable.sign_purple);
			}
		}
		holder.name.setText(p.getMac());
		holder.desc.setText("X/Y : " + p.getX() + "/" + p.getY() + "   major:"
				+ p.getMajor() + "   minor:" + p.getMinor());

		return convertView;
	}

	static class ViewHolder {
		TextView name;
		TextView desc;
		ImageView sign;
	}
}