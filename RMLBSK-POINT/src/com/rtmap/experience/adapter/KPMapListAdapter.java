package com.rtmap.experience.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rtmap.experience.R;
import com.rtmap.experience.core.KPBaseAdapter;
import com.rtmap.experience.core.model.Floor;
import com.rtmap.experience.core.model.UserInfo;
import com.rtmap.experience.util.DTUIUtils;

public class KPMapListAdapter extends KPBaseAdapter<Floor> {

	private Activity mActivity;

	public KPMapListAdapter(Activity activity) {
		super();
		mActivity = activity;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = DTUIUtils.inflate(R.layout.map_list_item);
			holder = new ViewHolder();
			holder.mTextMap = (TextView) convertView
					.findViewById(R.id.text_map);
			holder.mDownload = (TextView) convertView
					.findViewById(R.id.button_download);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		final Floor info = mList.get(position);
		holder.mTextMap.setText(info.getName());

		holder.mDownload.setVisibility(View.GONE);
		return convertView;
	}

	static class ViewHolder {
		TextView mTextMap;
		TextView mDownload;
	}

}
