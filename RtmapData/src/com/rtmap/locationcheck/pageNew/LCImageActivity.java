package com.rtmap.locationcheck.pageNew;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.rtmap.checkpicker.R;
import com.rtmap.locationcheck.core.LCActivity;

public class LCImageActivity extends LCActivity {
	
	private ImageView mImage;
	private static String imageUrl;
	
	public static void interActivity(Activity context, String imageUrl) {
		LCImageActivity.imageUrl = imageUrl;
		Intent intent = new Intent(context, LCImageActivity.class);
		context.startActivity(intent);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lc_image);
		
		Bitmap bitmap = BitmapFactory.decodeFile(imageUrl);// 得到缩小后的图片
		mImage = (ImageView) findViewById(R.id.image);
		mImage.setImageBitmap(bitmap);
		mImage.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
}
