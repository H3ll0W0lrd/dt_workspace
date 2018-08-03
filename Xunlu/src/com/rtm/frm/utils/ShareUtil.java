package com.rtm.frm.utils;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import cn.sharesdk.onekeyshare.OnekeyShare;

import com.rtm.frm.R;

public class ShareUtil {
	public static void sharePhoto(String photoName, String message, String title, Activity activity) {
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		File file = new File(photoName);
		shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
		if(!XunluUtil.isEmpty(message)) {
			shareIntent.putExtra(Intent.EXTRA_TEXT, message);
		}
		shareIntent.setType("image/png");
		activity.startActivity(Intent.createChooser(shareIntent, title));
	}
	
	public static void shareMessage(String message, String title, Activity activity) {
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		if(!XunluUtil.isEmpty(message)) {
			shareIntent.putExtra(Intent.EXTRA_TEXT, message);
		}
		shareIntent.setType("text/plain");
		activity.startActivity(Intent.createChooser(shareIntent, title));
	}
	
	/**
	 * @param context
	 * @param message
	 * @param title
	 * @param openUrl
	 * @param imageUrl
	 * @param isPath
	 */
	public static void showOneKeyShare(Context context, String message, String title,String openUrl,
			String imageUrl,boolean isPath) {
		final OnekeyShare oks = new OnekeyShare();
		oks.setNotification(R.drawable.ic_launcher, "noti");
		oks.setAddress("");
		oks.setTitle(title);
		oks.setTitleUrl("http://card.cgbchina.com.cn");
		oks.setText(message);

		oks.setUrl("http://card.cgbchina.com.cn");
		oks.setComment("");
		oks.setSite("");
		oks.setSiteUrl("http://card.cgbchina.com.cn");
		oks.setVenueName("");
		oks.setVenueDescription("广发EASY GO");
		oks.setLatitude(23.056081f);
		oks.setLongitude(113.385708f);
		if (!XunluUtil.isEmpty(imageUrl)) {
			if(isPath) {
				oks.setImagePath(imageUrl);
			} else {
				oks.setImageUrl(imageUrl);
			}
		}
		// 令编辑页面显示为Dialog模式
		oks.setDialogMode();

		// 在自动授权时可以禁用SSO方式
		oks.disableSSOWhenAuthorize();

		// 去除注释，则快捷分享的操作结果将通过OneKeyShareCallback回调
		// oks.setCallback(new OneKeyShareCallback());

		// 去除注释，演示在九宫格设置自定义的图标
		// Bitmap logo = BitmapFactory.decodeResource(menu.getResources(),
		// R.drawable.ic_launcher);
		// String label = menu.getResources().getString(R.string.app_name);
		// OnClickListener listener = new OnClickListener() {
		// public void onClick(View v) {
		// String text = "Customer Logo -- ShareSDK " +
		// ShareSDK.getSDKVersionName();
		// Toast.makeText(menu.getContext(), text, Toast.LENGTH_SHORT).show();
		// oks.finish();
		// }
		// };
		// oks.setCustomerLogo(logo, label, listener);

		// 去除注释，则快捷分享九宫格中将隐藏新浪微博和腾讯微博
		// oks.addHiddenPlatform(SinaWeibo.NAME);
		// oks.addHiddenPlatform(TencentWeibo.NAME);

		oks.show(context);
	}
}
