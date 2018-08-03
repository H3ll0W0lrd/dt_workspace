package com.rtmap.locationcheck.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rtmap.checkpicker.R;
import com.rtmap.locationcheck.core.LCBaseAdapter;
import com.rtmap.locationcheck.util.DTUIUtils;

public class LCUuidAdapter extends LCBaseAdapter<String> {

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		ViewHodler holder;
		if (view == null) {
			view = DTUIUtils.inflate(R.layout.inter_text_layout);
			holder = new ViewHodler();
			holder.text = (TextView) view.findViewById(R.id.inter_text);
			view.setTag(holder);
		}
		holder = (ViewHodler) view.getTag();
		holder.text.setText(mList.get(position));
		return view;
	}

	class ViewHodler {
		public TextView text;
	}
}
