package com.airport.test.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.airport.test.R;
import com.dingtao.libs.util.DTUIUtil;
import com.rtm.common.model.POI;

public class POIAdapter extends LCBaseAdapter<POI> {

	private boolean mGone;

	public void setCurrentIndex(boolean gone) {
		mGone = gone;
	}
	
	public boolean isHistory() {
		return mGone;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		ViewHodler holder;
		if (view == null) {
			view = DTUIUtil.inflate(R.layout.poi_item);
			holder = new ViewHodler();
			holder.text = (TextView) view.findViewById(R.id.text);
			holder.image = (ImageView) view.findViewById(R.id.imageView1);
			view.setTag(holder);
		}
		POI poi = getItem(position);
		holder = (ViewHodler) view.getTag();
		if (mGone) {
			holder.text.setText(poi.getFloor() + "-" + poi.getName());
			holder.image.setVisibility(View.INVISIBLE);
		} else {
			holder.text.setText(poi.getName());
			holder.image.setVisibility(View.VISIBLE);
		}
		return view;
	}

	class ViewHodler {
		public TextView text;
		public ImageView image;
	}
}
