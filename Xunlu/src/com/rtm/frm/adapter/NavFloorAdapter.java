package com.rtm.frm.adapter;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.rtm.frm.R;
import com.rtm.frm.XunluApplication;
import com.rtm.frm.map.RouteLayer;

@SuppressLint("InflateParams")
public class NavFloorAdapter extends BaseAdapter{
	private List<String> mNavFloorList = new ArrayList<String>();
	private RouteLayer mRouteLayer;
	private Bitmap mBitmap;
	
	public NavFloorAdapter(RouteLayer routeLayer,List<String> navFloorList){
		mRouteLayer = routeLayer;
		mNavFloorList = navFloorList;
	}
	
	@Override
	public int getCount() {
		return mNavFloorList.size();
	}

	@Override
	public Object getItem(int position) {
		return mNavFloorList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null){
			ViewHolder holder = new ViewHolder();
			convertView = LayoutInflater.from(XunluApplication.mApp).inflate(R.layout.fragment_nav_img_item,null);
			holder.navFloorItemImageView = (ImageView) convertView.findViewById(R.id.nav_img_item_img);
			holder.floorNameTextView = (TextView) convertView.findViewById(R.id.nav_img_floor);
			convertView.setTag(holder);
		}
//		mBitmap = mRouteLayer.getNavigateBitmapByFlag(position);
		ViewHolder h = (ViewHolder) convertView.getTag();
//		h.navFloorItemImageView.setImageBitmap(mBitmap);
		h.floorNameTextView.setText(mNavFloorList.get(position));
		return convertView;
	}
	
	public void recycleBitmap() {
		if(mBitmap != null){
			mBitmap.recycle();
			mBitmap = null;
		}
	}
	
	private class ViewHolder {
		public ImageView navFloorItemImageView;
		public TextView floorNameTextView;
	}
}
