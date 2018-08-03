package com.rtmap.ambassador.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rtmap.ambassador.R;
import com.rtmap.ambassador.core.DTBaseAdapter;
import com.rtmap.ambassador.model.Area;
import com.rtmap.ambassador.util.DTUIUtil;

public class AMAreaListAdapter extends DTBaseAdapter<Area> {

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		ViewHodler holder;
		if (view == null) {
			view = DTUIUtil.inflate(R.layout.inter_text_layout);
			holder = new ViewHodler();
			holder.text = (TextView) view.findViewById(R.id.inter_text);
			view.setTag(holder);
		}
		holder = (ViewHodler) view.getTag();
		Area area = mList.get(position);
		holder.text.setText(area.getAreaCode() + "-" + area.getAreaName());
		return view;
	}

	class ViewHodler {
		public TextView text;
	}
}
