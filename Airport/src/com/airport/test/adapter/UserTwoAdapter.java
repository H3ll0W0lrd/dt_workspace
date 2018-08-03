package com.airport.test.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.airport.test.R;
import com.airport.test.adapter.UserOneAdapter.ViewHodler;
import com.airport.test.model.CateData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.dingtao.libs.util.DTUIUtil;
import com.rtm.common.model.BuildInfo;

public class UserTwoAdapter extends LCBaseAdapter<CateData> {

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHodler holder;
		if (convertView == null) {
			convertView = DTUIUtil.inflate(R.layout.user_hobby_item2);
			holder = new ViewHodler();
			holder.text = (TextView) convertView.findViewById(R.id.text);
			holder.icon = (ImageView) convertView.findViewById(R.id.icon);
			convertView.setTag(holder);
		}
		holder = (ViewHodler) convertView.getTag();
		CateData cate = getItem(position);
		holder.text.setText(cate.getName());
		if (cate.isCheck())
			holder.icon.setImageResource(R.drawable.hobby_ok);
		else {
			holder.icon.setImageResource(R.drawable.hobby_no);
		}
		return convertView;
	}

	class ViewHodler {
		public TextView text;
		public ImageView icon;
	}

}
