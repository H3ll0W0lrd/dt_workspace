package com.rtm.frm.adapter;

import java.util.ArrayList;
import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.rtm.frm.R;
import com.rtm.frm.XunluApplication;

/**
 * @author liYan
 * @explain 城市选择adapter
 */
public class CitysAdapter extends BaseAdapter {
	
	private List<String> mCitysData = new ArrayList<String>();

	@Override
	public int getCount() {
		return mCitysData.size();
	}

	@Override
	public Object getItem(int position) {
		return mCitysData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup viewGroup) {
		if(view == null) {
			ViewHolder holder = new ViewHolder();
			view = LayoutInflater.from(XunluApplication.mApp).inflate(R.layout.fragment_citys_item, null);
			holder.cityNameView = (TextView) view.findViewById(R.id.slide_item_city_name);
			view.setTag(holder);
		}
		ViewHolder h = (ViewHolder) view.getTag();
		h.cityNameView.setText(mCitysData.get(position));
		return view;
	}
	
	public void setData(List<String> data) {
		mCitysData = data;
	}
	
	private class ViewHolder {
		public TextView cityNameView;
	}

}
