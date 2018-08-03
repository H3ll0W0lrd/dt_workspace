package com.minnw.beacon.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.minnw.beacon.R;
import com.minnw.beacon.data.BluetoothDeviceAndRssi;

public class DetilAdapter   extends BaseAdapter {
		Context context;
		List<String> names;
		LayoutInflater inflater;
		List<String> values;
		public DetilAdapter(Context context,
				List<String> names,List<String> values) {
			this.context = context;
			inflater = LayoutInflater.from(context);
			this.names = names;
			this.values = values;
		}

		@Override
		public int getCount() {
			return names.size();
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
				vg = (ViewGroup) inflater.inflate(R.layout.detil_item, null);
			}
			
			TextView item_name = ((TextView) vg.findViewById(R.id.item_name));
			item_name.setText(names.get(position));
			TextView item_value = ((TextView) vg.findViewById(R.id.item_value));
			if(position<values.size())
			  item_value.setText(values.get(position));
			else 
				item_value.setText("");

		
			return vg;
		}
	
}
