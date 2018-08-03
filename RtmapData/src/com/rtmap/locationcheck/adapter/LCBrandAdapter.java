package com.rtmap.locationcheck.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.rtmap.checkpicker.R;
import com.rtmap.locationcheck.core.LCBaseAdapter;
import com.rtmap.locationcheck.util.DTUIUtils;

public class LCBrandAdapter extends LCBaseAdapter<String> {
	
	@Override
	public int getCount() {
		return super.getCount()+1;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		ViewHodler holder;
		if (view == null) {
			view = DTUIUtils.inflate(R.layout.brand_item);
			holder = new ViewHodler();
			holder.image = (ImageView) view.findViewById(R.id.image);
			view.setTag(holder);
		}
		holder = (ViewHodler) view.getTag();
		if(position==getCount()-1){
			holder.image.setImageResource(R.drawable.add_photo);
		}else{
			String fileUrl = getItem(position);
			final Options options = new Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(fileUrl, options);
			options.inSampleSize = 4;
			options.inJustDecodeBounds = false;
			Bitmap bitmap = BitmapFactory.decodeFile(fileUrl, options);// 得到缩小后的图片
			holder.image.setImageBitmap(bitmap);
		}
		return view;
	}

	class ViewHodler {
		public ImageView image;
	}
}
