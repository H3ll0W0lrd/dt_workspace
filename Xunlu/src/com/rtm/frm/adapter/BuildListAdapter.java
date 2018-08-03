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
import com.rtm.frm.model.Build;

/**
 * @author liYan
 * @explain 城市选择adapter
 */
public class BuildListAdapter extends BaseAdapter {
	
	private List<Build> mBuildsData = new ArrayList<Build>();

	@Override
	public int getCount() {
		return mBuildsData.size();
	}

	@Override
	public Object getItem(int position) {
		return mBuildsData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup viewGroup) {
		if(view == null) {
			ViewHolder holder = new ViewHolder();
			view = LayoutInflater.from(XunluApplication.mApp).inflate(R.layout.fragment_buildlist_item, null);
			holder.buildNameView = (TextView) view.findViewById(R.id.bulid_liste_item_build_name);
			view.setTag(holder);
		}
		ViewHolder h = (ViewHolder) view.getTag();
		Build b = mBuildsData.get(position);
		h.buildNameView.setText(b.name);
		return view;
	}
	
	public void setData(List<Build> data) {
		mBuildsData = data;
	}
	
	private class ViewHolder {
		public TextView buildNameView;
	}

}
