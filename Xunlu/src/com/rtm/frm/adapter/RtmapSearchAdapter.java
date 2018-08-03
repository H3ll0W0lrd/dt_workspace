package com.rtm.frm.adapter;

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
import com.rtm.frm.model.Floor;

/**
 * @author liYan
 * @explain rtmap搜索默认显示的列表adapter
 */
@SuppressLint("InflateParams") 
public class RtmapSearchAdapter extends BaseAdapter {
	
	private List<Floor> mFloorsData = new ArrayList<Floor>();

	@Override
	public int getCount() {
		return mFloorsData.size();
	}

	@Override
	public Object getItem(int position) {
		return mFloorsData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup viewGroup) {
		if(view == null) {
			ViewHolder holder = new ViewHolder();
			view = LayoutInflater.from(XunluApplication.mApp).inflate(R.layout.fragment_rtmap_search_floor_item, null);
			holder.floornameView = (TextView) view.findViewById(R.id.floor_name);
			holder.floordescriptionView = (TextView) view.findViewById(R.id.floor_description);
			view.setTag(holder);
		}
		ViewHolder h = (ViewHolder) view.getTag();
		Floor f = mFloorsData.get(position);
		h.floornameView.setText(f.floor);
		h.floordescriptionView.setText(f.description_);
		return view;
	}
	
	public void setData(List<Floor> data) {
		mFloorsData = data;
	}
	
	private class ViewHolder {
		public TextView floornameView;
		public TextView floordescriptionView;
	}

}
