package com.rtmap.experience.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rtmap.experience.R;
import com.rtmap.experience.core.KPBaseAdapter;
import com.rtmap.experience.core.model.BeaconInfo;
import com.rtmap.experience.util.DTStringUtils;
import com.rtmap.experience.util.DTUIUtils;

public class KPBeaconDialogAdapter extends KPBaseAdapter<BeaconInfo> {

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		ViewHodler holder;
		if (view == null) {
			view = DTUIUtils.inflate(R.layout.dialog_text_layout);
			holder = new ViewHodler();
			holder.text = (TextView) view.findViewById(R.id.inter_text);
			view.setTag(holder);
		}
		holder = (ViewHodler) view.getTag();
		BeaconInfo beacon = mList.get(position);
		String text = "major:" + beacon.getMajor() + "-->"
				+ String.format("%04x", beacon.getMajor()) + "  minor:"
				+ beacon.getMinor() + "-->"
				+ String.format("%04x", beacon.getMinor());
		if (!DTStringUtils.isEmpty(beacon.getBuildId())) {
			text += "  已用";
		}
		holder.text.setText(text);
		return view;
	}

	class ViewHodler {
		public TextView text;
	}
}
