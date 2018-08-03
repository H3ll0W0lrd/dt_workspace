package com.rtmap.experience.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rtmap.experience.R;
import com.rtmap.experience.core.KPBaseAdapter;
import com.rtmap.experience.core.model.CateInfo;
import com.rtmap.experience.util.DTUIUtils;

public class KPBuildCateAdapter extends KPBaseAdapter<CateInfo> {

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		ViewHodler holder;
		if (view == null) {
			view = DTUIUtils.inflate(R.layout.cate_text);
			holder = new ViewHodler();
			holder.text = (TextView) view.findViewById(R.id.text);
			holder.image = (ImageView) view.findViewById(R.id.add);
			holder.layout = (LinearLayout) view.findViewById(R.id.cate_layout);
			view.setTag(holder);
		}
		holder = (ViewHodler) view.getTag();
		holder.text.setVisibility(View.GONE);
		holder.image.setVisibility(View.GONE);
		if (position < mList.size()) {
			CateInfo cate = mList.get(position);
			holder.text.setText(cate.getName());
			holder.text.setVisibility(View.VISIBLE);
			if(cate.isClick()){
				holder.text.setTextColor(DTUIUtils.getColor(R.color.grid_item_text_down));
				holder.layout.setBackgroundResource(R.drawable.cate_rounded_down);
			}else{
				holder.text.setTextColor(DTUIUtils.getColor(R.color.grid_item_text_up));
				holder.layout.setBackgroundResource(R.drawable.cate_rounded_up);
			}
		}else{
			holder.layout.setBackgroundResource(R.drawable.dt_trans);
			holder.image.setVisibility(View.VISIBLE);
		}
		return view;
	}

	@Override
	public int getCount() {
		return mList.size() + 1;
	}

	class ViewHodler {
		public TextView text;
		public ImageView image;
		public LinearLayout layout;
	}
}
