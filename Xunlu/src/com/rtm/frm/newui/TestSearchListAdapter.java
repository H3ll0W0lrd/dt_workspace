package com.rtm.frm.newui;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.rtm.frm.R;
import com.rtm.frm.XunluApplication;
import com.rtm.frm.model.Build;
import com.rtm.frm.utils.XunluUtil;

/**
 * @author liYan
 * @explain 全局搜索adapter
 */
@SuppressLint("InflateParams")
public class TestSearchListAdapter extends BaseAdapter {

	private List<Build> mPoisData = new ArrayList<Build>();

	@Override
	public int getCount() {
		return mPoisData.size();
	}

	@Override
	public Object getItem(int position) {
		return mPoisData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup viewGroup) {
		if (view == null) {
			ViewHolder holder = new ViewHolder();
			view = LayoutInflater.from(XunluApplication.mApp).inflate(
					R.layout.fragment_test_search_item, null);
			holder.floornameView = (TextView) view
					.findViewById(R.id.floor_name);
			holder.descriptionView = (TextView) view
					.findViewById(R.id.description);
			view.setTag(holder);
		}
		ViewHolder h = (ViewHolder) view.getTag();
		Build build = mPoisData.get(position);
		h.floornameView.setText(build.getName());
		String cityName = build.getCityName();
		if(XunluUtil.isEmpty(cityName)) {
			cityName = XunluApplication.mApp.getResources().getString(R.string.db_collect_build);
		} 
		h.descriptionView.setText(cityName);
		return view;
	}

	public void setData(List<Build> data) {
		mPoisData = data;
		notifyDataSetChanged();
	}

	private class ViewHolder {
		public TextView floornameView;
		public TextView descriptionView;
	}

}
