package com.rtmap.locationcheck.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.rtmap.checkpicker.R;

public class LCMapDialogAdapter extends BaseAdapter {

	private Activity activity;
	private LayoutInflater mInflater;
	private String[] interList;

	public LCMapDialogAdapter(Activity activity,String[] interList) {
		this.activity = activity;
		mInflater = LayoutInflater.from(activity);
		this.interList = interList;
	}
	
	public void setInterList(String[] interList) {
		this.interList = interList;
	}

	@Override
	public int getCount() {
		return interList.length;
	}

	@Override
	public Object getItem(int position) {
		return interList[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		ViewHodler holder;
		if (view == null) {
			view = mInflater.inflate(R.layout.inter_text_layout, null);
			holder = new ViewHodler();
			holder.text = (TextView) view.findViewById(R.id.inter_text);
			view.setTag(holder);
		}
		holder = (ViewHodler) view.getTag();
		holder.text.setText(interList[position]);
		return view;
	}

	class ViewHodler {
		public TextView text;
	}
}
