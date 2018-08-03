package com.airport.test.adapter;

import android.annotation.SuppressLint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.airport.test.R;
import com.dingtao.libs.util.DTUIUtil;
import com.rtm.common.model.Floor;

public class FloorAdapter extends LCBaseAdapter<Floor> {

	private String mfloor;

	public void setCurrentIndex(String floor) {
		this.mfloor = floor;
	}

	@SuppressLint("ResourceAsColor")
	@Override
	public View getView(int position, View view, ViewGroup parent) {
		ViewHodler holder;
		if (view == null) {
			view = DTUIUtil.inflate(R.layout.spinner_text);
			holder = new ViewHodler();
			holder.text = (TextView) view.findViewById(R.id.text);
			holder.line = (ImageView) view.findViewById(R.id.line);
			view.setTag(holder);
		}
		Floor floor = getItem(position);
		holder = (ViewHodler) view.getTag();
		holder.text.setText(floor.getFloor());
		if (floor.getFloor().equals(mfloor)) {
			holder.text.setBackgroundResource(R.drawable.floor_selected_bg);
			holder.text.setTextColor(DTUIUtil.getColor(R.color.white));
		} else {
			holder.text.setTextColor(DTUIUtil.getColor(R.color.floor_text_on_color));
			holder.text.setBackgroundResource(R.drawable.dt_trans);
		}
		if(position==getCount()-1){
			holder.line.setVisibility(View.GONE);
		}else{
			holder.line.setVisibility(View.VISIBLE);
		}
		return view;
	}

	class ViewHodler {
		public TextView text;
		public ImageView line;
	}
}
