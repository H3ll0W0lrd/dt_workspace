package com.rtm.frm.adapter;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.rtm.common.model.POI;
import com.rtm.frm.R;
import com.rtm.frm.XunluApplication;

/**
 * @author liYan
 * @explain 联想搜索adapter
 */
@SuppressLint("InflateParams")
public class AssociativeSearchAdapter extends BaseAdapter {

	private List<POI> mPoisData = new ArrayList<POI>();

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
					R.layout.fragment_rtmap_search_item, null);
			holder.floornameView = (TextView) view
					.findViewById(R.id.floor_name);
			holder.descriptionView = (TextView) view
					.findViewById(R.id.description);
			view.setTag(holder);
		}
		ViewHolder h = (ViewHolder) view.getTag();
		POI poi = mPoisData.get(position);
		h.floornameView.setText(poi.getFloor());
		h.descriptionView.setText(poi.getName().trim());
		return view;
	}

	public void setData(ArrayList<POI> arrayList) {
		mPoisData = arrayList;
		notifyDataSetChanged();
	}

	private class ViewHolder {
		public TextView floornameView;
		public TextView descriptionView;
	}

}
