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
import com.minnw.beacon.data.TransimssionPower;

public class TransimssionPowerAdapter   extends BaseAdapter {
		Context context;
		List<TransimssionPower> names;
		LayoutInflater inflater;
		public TransimssionPowerAdapter(Context context,
				List<TransimssionPower> names ) {
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
				vg = (ViewGroup) inflater.inflate(R.layout.transimssionpower_item, null);
			}
			
			TextView mitextview = ((TextView) vg.findViewById(R.id.mitextview));
			mitextview.setText(context.getString(R.string.fugai)+names.get(position).getMeters()+context.getString(R.string.mi));
			TextView uuidtextview = ((TextView) vg.findViewById(R.id.uuidtextview));
			uuidtextview.setText(context.getString(R.string.power)+names.get(position).getText()+" dBm  ,  ");
			TextView uuidtextviewtype = ((TextView) vg.findViewById(R.id.uuidtextviewtype));
			if(position==0){
				uuidtextviewtype.setText(context.getString(R.string.zuidi) +context.getString(R.string.dang));
			}else
			   uuidtextviewtype.setText((Integer.parseInt(names.get(position).getValue()))+context.getString(R.string.dang));
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
