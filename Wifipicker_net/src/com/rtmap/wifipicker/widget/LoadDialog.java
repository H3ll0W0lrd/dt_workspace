package com.rtmap.wifipicker.widget;

import com.rtmap.wifipicker.R;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class LoadDialog extends Dialog {
	private static final int TIME_ANIMATION = 100;
	
	private Context mContext;
	
	private ImageView mImageL;
	private ImageView mImageO;
	private ImageView mImageA;
	private ImageView mImageD;
	private ImageView mImageI;
	private ImageView mImageN;
	private ImageView mImageG;

	public LoadDialog(Context context, int theme) {
		super(context, theme);
		mContext = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.load_dialog);
		
		init();
	}

	private void init() {
		mImageL = (ImageView) findViewById(R.id.image_l);
		mImageO = (ImageView) findViewById(R.id.image_o);
		mImageA = (ImageView) findViewById(R.id.image_a);
		mImageD = (ImageView) findViewById(R.id.image_d);
		mImageI = (ImageView) findViewById(R.id.image_i);
		mImageN = (ImageView) findViewById(R.id.image_n);
		mImageG = (ImageView) findViewById(R.id.image_g);
		
		setOnCancelListener(new OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface arg0) {
				clearAnimations();
			}
		});
		setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface dialog) {
				clearAnimations();
			}
		});
		
		startLoad();
	}
	
	private void clearAnimations() {
		mImageL.clearAnimation();
		mImageO.clearAnimation();
		mImageA.clearAnimation();
		mImageD.clearAnimation();
		mImageI.clearAnimation();
		mImageN.clearAnimation();
		mImageG.clearAnimation();
	}
	
	private void startLoad() {
		mImageL.startAnimation(getAnimation(R.id.image_l));
		mImageO.startAnimation(getAnimation(R.id.image_o));
		mImageA.startAnimation(getAnimation(R.id.image_a));
		mImageD.startAnimation(getAnimation(R.id.image_d));
		mImageI.startAnimation(getAnimation(R.id.image_i));
		mImageN.startAnimation(getAnimation(R.id.image_n));
		
		Animation last = getAnimation(R.id.image_g);
		last.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				startLoad();
			}
		});
		mImageG.startAnimation(last);
	}
	
	private Animation getAnimation(int resId) {
		Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.anim_alpha2opaque);
		switch (resId) {
		case R.id.image_l:
			animation.setStartOffset(0);
			break;
		case R.id.image_o:
			animation.setStartOffset(TIME_ANIMATION);
			break;
		case R.id.image_a:
			animation.setStartOffset(2 * TIME_ANIMATION);
			break;
		case R.id.image_d:
			animation.setStartOffset(3 * TIME_ANIMATION);
			break;
		case R.id.image_i:
			animation.setStartOffset(4 * TIME_ANIMATION);
			break;
		case R.id.image_n:
			animation.setStartOffset(5 * TIME_ANIMATION);
			break;
		case R.id.image_g:
			animation.setStartOffset(6 * TIME_ANIMATION);
			break;
		}
		return animation;
	}
}
