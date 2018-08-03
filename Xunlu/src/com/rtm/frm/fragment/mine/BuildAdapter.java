package com.rtm.frm.fragment.mine;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.rtm.frm.R;
import com.rtm.frm.model.Build;

public class BuildAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private ArrayList<Build> mBuilds;

	public BuildAdapter(Context context) {
		mInflater = LayoutInflater.from(context);
	}

	public void setData(ArrayList<Build> builds) {
		mBuilds = builds;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		if (mBuilds == null) {
			return 0;
		}
		return mBuilds.size();
	}

	@Override
	public Object getItem(int position) {
		return mBuilds.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.item_build, null);

			holder = new ViewHolder();
			holder.index = (TextView) convertView.findViewById(R.id.text_index);
			holder.build = (TextView) convertView.findViewById(R.id.text_build);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		Build build = mBuilds.get(position);
		holder.index.setText(String.valueOf(position + 1));
		holder.index.setTypeface(Typeface.createFromAsset(convertView
				.getContext().getAssets(), "fonts/ultralight.ttf"));
		holder.build.setText(build.getName());

		holder.index.setTextColor(holder.index.getResources().getColor(
				R.color.airport_index_color));
		return convertView;
	}

	static class ViewHolder {
		TextView index;
		TextView build;
	}
}
