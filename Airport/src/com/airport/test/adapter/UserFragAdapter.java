package com.airport.test.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.airport.test.R;
import com.airport.test.model.MsgData;
import com.dingtao.libs.util.DTUIUtil;

public class UserFragAdapter extends LCBaseAdapter<MsgData> {

	int[] iconArray = new int[] { R.drawable.plan, R.drawable.user_msg_icon,
			R.drawable.scanner, R.drawable.input };
	String[] textArray = new String[] { "我的航班", "消息中心", "扫登机牌", "输航班号" };

	private int mCount = 2;

	public void setCount(int count) {
		mCount = count;
	}

	@Override
	public int getCount() {
		return mCount;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		ViewHodler holder;
		if (view == null) {
			view = DTUIUtil.inflate(R.layout.user_item);
			holder = new ViewHodler();
			holder.text = (TextView) view.findViewById(R.id.text);
			holder.image = (ImageView) view.findViewById(R.id.icon);
			view.setTag(holder);
		}
		holder = (ViewHodler) view.getTag();
		if (mCount == 2)
			position += 2;
		holder.image.setImageResource(iconArray[position]);
		holder.text.setText(textArray[position]);
		return view;
	}

	class ViewHodler {
		public TextView text;
		public ImageView image;
	}
}
