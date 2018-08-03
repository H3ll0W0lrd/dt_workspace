package com.airport.test.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.airport.test.R;
import com.airport.test.model.CateData;
import com.dingtao.libs.util.DTUIUtil;
import com.rtm.common.model.BuildInfo;

public class UserOneAdapter extends LCBaseAdapter<CateData> {

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHodler holder;
		if (convertView == null) {
			convertView = DTUIUtil.inflate(R.layout.user_hobby_item1);
			holder = new ViewHodler();
			holder.text = (TextView) convertView.findViewById(R.id.text);
			holder.icon = (ImageView) convertView.findViewById(R.id.icon);
			convertView.setTag(holder);
		}
		holder = (ViewHodler) convertView.getTag();
		CateData cate = getItem(position);
		holder.text.setText(cate.getName());
		holder.icon.setImageResource(cate.getIconid());
		return convertView;
	}

	class ViewHodler {
		public TextView text;
		public ImageView icon;
	}

}
