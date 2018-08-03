package com.rtm.frm.tab1;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.rtm.frm.R;
import com.rtm.frm.XunluApplication;
import com.rtm.frm.model.FavorablePoiDbModel;

/**
 * @author liYan
 * @explain 全局搜索adapter
 */
@SuppressLint("InflateParams")
public class TabMineListAdapter extends BaseAdapter {
	List<FavorablePoiDbModel> data = new ArrayList<FavorablePoiDbModel>();
	// imageloader
	private ImageLoader loader;
	private DisplayImageOptions options;
	public TabMineListAdapter(){
		loader = ImageLoader.getInstance();
		options = new DisplayImageOptions.Builder()
//		.showImageOnLoading(R.drawable.img_loading)
		// 正在加载
		.showImageForEmptyUri(R.drawable.no_img_big)
		// 空图片
		.showImageOnFail(R.drawable.no_img_big)
		// 错误图片
		.cacheInMemory(false)
		.cacheOnDisc(true)
		.considerExifParams(true)
		.bitmapConfig(Bitmap.Config.RGB_565)
		.build();
	}
	
	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup viewGroup) {
		ViewHolder holder = null;
		if (view == null) {
			holder = new ViewHolder();
			view = View.inflate(XunluApplication.mApp,R.layout.fragment_tab1_list_item, null);
			holder.title = (TextView) view.findViewById(R.id.tv_title);
			holder.date = (TextView) view.findViewById(R.id.tv_date);
			holder.imgBig = (ImageView)view.findViewById(R.id.img_big);
			holder.intruduction = (TextView)view.findViewById(R.id.tv_intruduction);
			view.setTag(holder);
		}else{
			holder = (ViewHolder) view.getTag();
		}
		
		FavorablePoiDbModel poi = data.get(position);
		holder.title.setText(poi.poiName);
		holder.date.setText(poi.endTime);
		loader.displayImage(poi.adUrl, holder.imgBig, options);
		holder.intruduction.setText(poi.discription);
		
		return view;
	}

	public void setData(List<FavorablePoiDbModel> d) {
		data = d;
		notifyDataSetChanged();
	}
	
	public void addData(List<FavorablePoiDbModel> d) {
		for(FavorablePoiDbModel fav : d) {
			data.add(fav);
		}
		notifyDataSetChanged();
	}

	private class ViewHolder {
		public TextView title;
		public TextView date;
		public ImageView imgBig;
		public TextView intruduction;
	}
}
