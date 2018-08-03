package com.airport.test.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.airport.test.R;
import com.airport.test.model.MsgData;
import com.dingtao.libs.util.DTUIUtil;

public class MsgAdapter extends LCBaseAdapter<MsgData> {

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		ViewHodler holder;
		if (view == null) {
			view = DTUIUtil.inflate(R.layout.msg_item);
			holder = new ViewHodler();
			holder.text = (TextView) view.findViewById(R.id.text);
			holder.image = (ImageView) view.findViewById(R.id.imageView1);
			view.setTag(holder);
		}
		MsgData info = getItem(position);
		holder = (ViewHodler) view.getTag();
		if (info.getMid() == 0) {
			holder.image.setImageResource(R.drawable.anjianxiaoxi);
		} else if (info.getMid() == 1) {
			holder.image.setImageResource(R.drawable.tingzhidengji);
		} else if (info.getMid() == 2) {
			holder.image.setImageResource(R.drawable.canyinxiaoxi);
		} else{
			holder.image.setImageResource(R.drawable.book_info);
		}
		holder.text.setText(info.getText());
		return view;
	}

	class ViewHodler {
		public TextView text;
		public ImageView image;
	}
}
