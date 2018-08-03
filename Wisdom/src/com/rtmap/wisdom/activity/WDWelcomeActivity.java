package com.rtmap.wisdom.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.rtm.common.model.RMLocation;
import com.rtm.location.LocationApp;
import com.rtmap.wisdom.R;
import com.rtmap.wisdom.core.DTActivity;
import com.rtmap.wisdom.util.view.DTCircleView;

/**
 * 欢迎页
 * 
 * @author dingtao
 *
 */
public class WDWelcomeActivity extends DTActivity {
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == 1) {
				RMLocation location = LocationApp.getInstance()
						.getCurrentLocation();
				 Intent intent = new Intent(WDWelcomeActivity.this,
				 WDMainActivity.class);
				 if (location.getError() != 0) {
				 intent.putExtra("sign", 0);
				 } else {
				 intent.putExtra("sign", 1);
				 }
				 startActivity(intent);
//				 finish();
			} else if (msg.what == 2) {
				TranslateAnimation anim = new TranslateAnimation(0, 0,
						mLayout1.getBottom() - mLayout2.getTop() - 100, 0);
				anim.setFillAfter(true);
				anim.setDuration(1500);
				anim.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {

					}

					@Override
					public void onAnimationRepeat(Animation animation) {

					}

					@Override
					public void onAnimationEnd(Animation animation) {

						mLayout2.startAnimation(AnimationUtils.loadAnimation(
								getApplicationContext(), R.anim.welcome_zoom2));
						findViewById(R.id.imageView2).startAnimation(
								AnimationUtils.loadAnimation(
										getApplicationContext(),
										R.anim.welcome_zoom));
						sendEmptyMessageDelayed(3, 1500);

					}
				});
				mLayout2.startAnimation(anim);
			} else if (msg.what == 3) {
				DTCircleView view = (DTCircleView) findViewById(R.id.dTCircleView1);
				view.start();
			}
		};
	};

	private RelativeLayout mLayout1;
	private LinearLayout mLayout2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_welcome);
		mLayout2 = (LinearLayout) findViewById(R.id.welcome_layout);
		mLayout1 = (RelativeLayout) findViewById(R.id.welcome_center_layout);
		LocationApp.getInstance().start();
		mHandler.sendEmptyMessageDelayed(2, 500);
		mHandler.sendEmptyMessageDelayed(1, 5500);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		mHandler.removeMessages(1);
	}

	@Override
	public String getPageName() {
		return null;
	}
}
