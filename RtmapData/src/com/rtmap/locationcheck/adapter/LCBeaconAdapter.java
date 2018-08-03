package com.rtmap.locationcheck.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rtm.common.utils.RMStringUtils;
import com.rtm.location.entity.BeaconInfo;
import com.rtmap.checkpicker.R;
import com.rtmap.locationcheck.core.LCBaseAdapter;
import com.rtmap.locationcheck.util.DTUIUtils;

public class LCBeaconAdapter extends LCBaseAdapter<BeaconInfo> {

	HashMap<String, BeaconInfo> mMap = new HashMap<String, BeaconInfo>();
	private int mPosition = 0;
	private String mac, mVisibilityMac, mLockMac;

	public void setSelect(int o) {
		mPosition = o;
		mMap.clear();
	}

	@Override
	public void addList(List<BeaconInfo> list1) {
		ArrayList<BeaconInfo> list = new ArrayList<BeaconInfo>(list1);
		for (int i = 0; i < list.size(); i++) {
			BeaconInfo info = list.get(i);
			if (mMap.containsKey(info.mac)) {
				BeaconInfo bea = mMap.get(info.mac);
				Log.i("rtmap", bea.toString()+"    "+info.toString()+"    "+bea.equals(info)+"    "+info.count);
				if (!bea.equals(info)) {
					info.count += bea.count;
				}
			}
			mMap.put(info.mac, info);
		}
		mList.clear();
		list.clear();
		list.addAll(mMap.values());
		for (int i = 0; i < list.size(); i++) {
			BeaconInfo info = list.get(i);
			if (!RMStringUtils.isEmpty(mac)
					&& !info.mac.toUpperCase().contains(mac.toUpperCase())) {
				continue;
			}
			if (mPosition == 1) {
				if (!info.mac.toUpperCase().contains("C91A")) {
					continue;
				}
			} else if (mPosition == 2) {
				if (!info.mac.toUpperCase().contains("FDA5")) {
					continue;
				}
			} else if (mPosition == 3) {
				if (!info.mac.toUpperCase().contains("C91B")) {
					continue;
				}
			} else if (mPosition == 4) {
				if (!info.mac.toUpperCase().contains("ABD3")) {
					continue;
				}
			}
			mList.add(info);
		}
		if (!RMStringUtils.isEmpty(mLockMac)) {
			BeaconInfo beacon = null;
			for (int i = 0; i < mList.size(); i++) {
				if (mLockMac.equals(mList.get(i).mac)) {
					beacon = mList.get(i);
					break;
				}
			}
			if (beacon != null) {
				mList.remove(beacon);
				mList.add(0, beacon);
			}
		}
	}

	public void setVisibility(String v) {
		if (!RMStringUtils.isEmpty(mVisibilityMac) && mVisibilityMac.equals(v)) {
			mVisibilityMac = null;
		} else {
			mVisibilityMac = v;
		}
	}

	public void setLock(String v) {
		if (!RMStringUtils.isEmpty(mLockMac) && mLockMac.equals(v)) {
			mLockMac = null;
		} else {
			mLockMac = v;
		}
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = DTUIUtils.inflate(R.layout.beacon_item);
			holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(R.id.name);
			holder.desc = (TextView) convertView.findViewById(R.id.desc);
			holder.layout = (RelativeLayout) convertView
					.findViewById(R.id.beacon_desc_layout);
			holder.sign = (ImageView) convertView.findViewById(R.id.lock_icon);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		final BeaconInfo p = mList.get(position);
		holder.name.setText(p.mac + "," + p.getRssi() + "db," + p.count);
		holder.desc.setText(p.name + "\n" + p.getBluetoothAddress() + "\n"
				+ p.proximityUuid + "\n" + "major:" + p.major + ",minor:"
				+ p.minor + ",mac" + p.mac + "\ntxPower" + p.txPower + ",rssi:"
				+ p.rssi + ",count:" + p.count);
		if (!RMStringUtils.isEmpty(mVisibilityMac)
				&& mVisibilityMac.equals(p.mac)) {
			holder.layout.setVisibility(View.VISIBLE);
		} else {
			holder.layout.setVisibility(View.GONE);
		}
		if (!RMStringUtils.isEmpty(mLockMac) && mLockMac.equals(p.mac)) {
			holder.sign.setImageResource(R.drawable.icon_down);
		} else {
			holder.sign.setImageResource(R.drawable.icon_import);
		}
		holder.sign.setTag(p.mac);
		holder.sign.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String mac = (String) v.getTag();
				setLock(mac);
			}
		});
		return convertView;
	}

	static class ViewHolder {
		TextView name;
		TextView desc;
		RelativeLayout layout;
		ImageView sign;
	}

	public void setSelect(String string) {
		mac = string;
		mMap.clear();
	}

	public HashMap<String, BeaconInfo> getMap() {
		return mMap;
	}
}