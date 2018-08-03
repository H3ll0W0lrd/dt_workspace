package com.minnw.beacon.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.minnw.beacon.R;
import com.minnw.beacon.data.BluetoothDeviceAndRssi;

public class UUIDAdapter   extends BaseAdapter {
		Context context;
		List<String> names;
		LayoutInflater inflater;
		public UUIDAdapter(Context context,
				List<String> names ) {
			this.context = context;
			inflater = LayoutInflater.from(context);
			this.names = names;
		}

		@Override
		public int getCount() {
			return names.size();
		}
		private int  mposition=0;
		public void setPosition(int position) {
			 this.mposition=position;
			 notifyDataSetChanged();
		}
		@Override
		public Object getItem(int position) {
			return names.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewGroup vg = null;
			if (convertView != null) {
				vg = (ViewGroup) convertView;
			} else {
				vg = (ViewGroup) inflater.inflate(R.layout.uuid_item, null);
			}
			TextView uuidtextview = ((TextView) vg.findViewById(R.id.uuidtextview));
			uuidtextview.setText(names.get(position).split(";")[0]);
			TextView uuidtextviewtype = ((TextView) vg.findViewById(R.id.uuidtextviewtype));
			uuidtextviewtype.setText(names.get(position).split(";")[1]);
			ImageView uuidselectimage = ((ImageView) vg.findViewById(R.id.uuidselectimage));
			View dvidline = ((View) vg.findViewById(R.id.dvidline));
		    if(mposition==position){
		    	uuidselectimage.setVisibility(View.VISIBLE);
		    }else{
		    	uuidselectimage.setVisibility(View.INVISIBLE);
		    }
             if(position==(names.size()-1)){
            	 dvidline.setVisibility(View.INVISIBLE);
             }else{
            	 dvidline.setVisibility(View.VISIBLE);
             }
		
			return vg;
		}
	
}
