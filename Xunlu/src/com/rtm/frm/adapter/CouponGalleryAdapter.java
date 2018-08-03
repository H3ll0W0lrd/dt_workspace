package com.rtm.frm.adapter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.rtm.frm.R;

@SuppressLint("InflateParams")
public class CouponGalleryAdapter extends BaseAdapter {

	private List<String> urlList;
	private LayoutInflater mInflater;

	private DisplayImageOptions options; // DisplayImageOptions是用于设置图片显示的类
	private ImageLoader imageLoader;
	private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

	@SuppressWarnings("deprecation")
	public CouponGalleryAdapter(Context context, List<String> urlList,
			ImageLoader imageLoader) {
		mInflater = LayoutInflater.from(context);
		this.urlList = urlList;
		this.imageLoader = imageLoader;
		// 使用DisplayImageOptions.Builder()创建DisplayImageOptions
		options = new DisplayImageOptions.Builder()
				.showStubImage(R.drawable.detail_defbg) // 设置图片下载期间显示的图片
				.showImageForEmptyUri(R.drawable.detail_defbg) // 设置图片Uri为空或是错误的时候显示的图片
				.showImageOnFail(R.drawable.detail_defbg) // 设置图片加载或解码过程中发生错误显示的图片
				.cacheInMemory(true) // 设置下载的图片是否缓存在内存中
				.cacheOnDisc(true) // 设置下载的图片是否缓存在SD卡中
				// .displayer(new RoundedBitmapDisplayer(20)) // 设置成圆角图片
				.build(); // 创建配置过得DisplayImageOption对象
	}

	@Override
	public int getCount() {
		return urlList.size();
	}

	@Override
	public Object getItem(int position) {
		return urlList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.imagegallery_item, null);
			holder = new ViewHolder();
			holder.galleryImage = (ImageView) convertView
					.findViewById(R.id.galleryImage);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		/**
		 * 显示图片 参数1：图片url 参数2：显示图片的控件 参数3：显示图片的设置 参数4：监听器
		 */
		imageLoader.displayImage(urlList.get(position), holder.galleryImage,
				options, animateFirstListener);
		holder.galleryImage.setScaleType(ScaleType.FIT_CENTER);
		return convertView;
	}

	public class ViewHolder {
		public ImageView galleryImage = null;
	}

	/**
	 * 图片加载第一次显示监听器
	 * 
	 * @author Administrator
	 * 
	 */
	private static class AnimateFirstDisplayListener extends
			SimpleImageLoadingListener {

		static final List<String> displayedImages = Collections
				.synchronizedList(new LinkedList<String>());

		@Override
		public void onLoadingComplete(String imageUri, View view,
				Bitmap loadedImage) {
			if (loadedImage != null) {
				// ImageView imageView = (ImageView) view;
				// 是否第一次显示
				boolean firstDisplay = !displayedImages.contains(imageUri);
				if (firstDisplay) {
					// 图片淡入效果
					// FadeInBitmapDisplayer.animate(imageView, 500);
					displayedImages.add(imageUri);
				}
			}
		}
	}
}
