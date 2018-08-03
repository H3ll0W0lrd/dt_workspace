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
import com.rtm.frm.model.BaoZangSuccData;

/**
 * @author kunge
 * 寻到的宝物list item
 */
public class BZListAdapter extends BaseAdapter {
	
	ArrayList<BaoZangSuccData> dataArray = new ArrayList<BaoZangSuccData>();

	public void setData(ArrayList<BaoZangSuccData> d){
		dataArray = d;
	}
	
	@Override
	public int getCount() {
		return dataArray.size();
	}

	@Override
	public Object getItem(int position) {
		return dataArray.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup viewGroup) {
		if(view == null) {
			ViewHolder holder = new ViewHolder();
			view = LayoutInflater.from(XunluApplication.mApp).inflate(R.layout.fragment_bz_list_item, null);
			
			holder.tvName = (TextView) view.findViewById(R.id.tv_name);
			holder.tvId = (TextView) view.findViewById(R.id.tv_bz_key);
			holder.tvStatus = (TextView) view.findViewById(R.id.tv_bz_get);
			
			view.setTag(holder);
		}
		ViewHolder h = (ViewHolder) view.getTag();
		h.tvName.setText(dataArray.get(position).getPoiName());
		h.tvId.setText(dataArray.get(position).getPoiId());
		if("0".equals(dataArray.get(position).getStatus())){
			h.tvStatus.setText("未领取");
			h.tvStatus.setTextColor(XunluApplication.mApp.getResources().getColor(R.color.bz_list_item_tv_status_0));
		}else{
			h.tvStatus.setText("已领取");
			h.tvStatus.setTextColor(XunluApplication.mApp.getResources().getColor(R.color.bz_list_item_tv_name));
		}
		
		return view;
	}
	
	private class ViewHolder {
		public TextView tvName;
		public TextView tvId;
		public TextView tvStatus;
	}

}
