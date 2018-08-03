package com.rtmap.experience.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.rtmap.experience.R;
import com.rtmap.experience.core.KPBaseAdapter;
import com.rtmap.experience.core.model.BuildInfo;
import com.rtmap.experience.util.DTStringUtils;
import com.rtmap.experience.util.DTUIUtils;

public class KPBuildListAdapter extends KPBaseAdapter<BuildInfo> {

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		ViewHodler holder;
		if (view == null) {
			view = DTUIUtils.inflate(R.layout.build_list_item);
			holder = new ViewHodler();
			holder.text = (TextView) view.findViewById(R.id.text);
			view.setTag(holder);
		}
		holder = (ViewHodler) view.getTag();
		BuildInfo build = mList.get(position);
		if (DTStringUtils.isEmpty(build.getName()))
			holder.text.setText(build.getBuildId());
		else
			holder.text.setText(build.getName());
		return view;
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	class ViewHodler {
		public TextView text;
		public ImageView image;
	}
}
