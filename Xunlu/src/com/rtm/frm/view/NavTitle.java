package com.rtm.frm.view;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rtm.frm.R;
import com.rtm.frm.utils.XunluUtil;

public class NavTitle extends RelativeLayout implements OnTouchListener{

	public static String NAV_KEY_TITLE_TEXT = "navKeyTitleText";
	
	public static String NAV_KEY_BUILD_NAME = "navKeyBuildName";

	public static String NAV_BROAD_CAST_FILTER = "navBroadCastFilter";

	private TextView mLeftText;

	private TextView mTitleText;

	private ImageView mRightImgBtn;

	private ImageView mLeftImgBtn;

	private TextView mRightText;
	
	private String mBackgroundColor = "#EEFFFFFF";

	private Context mContext;

	private NavReceiver mNavReceiver;
	
	private int mTextSize = 18;

	public NavTitle(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init(context, attrs);
	}

	@SuppressLint({ "Recycle", "ClickableViewAccessibility" })
	private void init(Context context, AttributeSet attrs) {
		this.setOnTouchListener(this);
		TypedArray tArray = context.obtainStyledAttributes(attrs,
				R.styleable.rtmap);

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);

		params.addRule(RelativeLayout.CENTER_VERTICAL);
		String leftText = tArray.getString(R.styleable.rtmap_navLeftText);
		if (leftText != null && !leftText.equals("")) {
			mLeftText = new TextView(context);
			int mMarginLeftId = tArray.getResourceId(R.styleable.rtmap_navPaddingLeft,-1);
			if(mMarginLeftId != -1){
				params.setMargins((int) getResources().getDimension(mMarginLeftId), 0, 0, 0);
			} else {
				params.setMargins(0, 0, 0, 0);
			}
			this.addView(mLeftText, params);
			mLeftText.setText(leftText);
			mLeftText.setTextColor(getResources().getColor(R.color.mine_tab_text_blue));
			mLeftText.setTextSize(mTextSize);
		}

		// 左侧按钮
		mLeftImgBtn = new ImageView(context);
		final int leftImgId = tArray.getResourceId(
				R.styleable.rtmap_navLeftImg, -1);
		if (leftImgId > -1) {
			int wId = tArray.getResourceId(R.styleable.rtmap_navLeftImgWidth, -1);
			int hId = tArray.getResourceId(R.styleable.rtmap_navLeftImgHeight, -1);
			
			if(wId != -1 && hId != -1) {
				params = new RelativeLayout.LayoutParams((int)getResources().getDimension(wId),(int)getResources().getDimension(hId));
			} else {
				params = new RelativeLayout.LayoutParams(0,0);
			}
			params.addRule(RelativeLayout.CENTER_VERTICAL);
			int mMarginLeftId = tArray.getResourceId(R.styleable.rtmap_navPaddingLeft,-1);
			if(mMarginLeftId != -1){
				params.setMargins((int) getResources().getDimension(mMarginLeftId), 0, 0, 0);
			} else {
				params.setMargins(0, 0, 0, 0);
			}
			mLeftImgBtn = new ImageView(context);
			mLeftImgBtn.setImageResource(leftImgId);
			this.addView(mLeftImgBtn, params);
		}

		// title文字
		mTitleText = new TextView(context);
		params = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.CENTER_IN_PARENT);
		this.addView(mTitleText, params);
		String titleText = tArray.getString(R.styleable.rtmap_navTitleText);
		mTitleText.setText(titleText);
		mTitleText.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
		mTitleText.setTextSize(mTextSize);

		// 右侧按钮
		final int rightImgId = tArray.getResourceId(
				R.styleable.rtmap_navRightImg, -1);
		if (rightImgId > -1) {
			int wId = tArray.getResourceId(R.styleable.rtmap_navRightImgWidth, -1);
			int hId = tArray.getResourceId(R.styleable.rtmap_navRightImgHeight, -1);
			if(wId != -1 && hId != -1) {
				params = new RelativeLayout.LayoutParams((int) getResources().getDimension(wId), (int) getResources().getDimension(hId));
			} else {
				params = new RelativeLayout.LayoutParams(0,0);
			}
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			params.addRule(RelativeLayout.CENTER_VERTICAL);
			int mMarginRightId = tArray.getResourceId(R.styleable.rtmap_navPaddingRight,-1);
			if(mMarginRightId != -1){
				params.setMargins(0, 0,(int) getResources().getDimension(mMarginRightId),  0);
			} else {
				params.setMargins(0, 0, 0, 0);
			}
			mRightImgBtn = new ImageView(context);
			mRightImgBtn.setImageResource(rightImgId);

			this.addView(mRightImgBtn, params);
		}

		String rightText = tArray.getString(R.styleable.rtmap_navRightText);
		if (rightText != null && !rightText.equals("")) {
			params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			params.addRule(RelativeLayout.CENTER_VERTICAL);
			int mMarginRightId = tArray.getResourceId(R.styleable.rtmap_navPaddingRight,-1);
			if(mMarginRightId != -1){
				params.setMargins(0, 0,(int) getResources().getDimension(mMarginRightId),  0);
			} else {
				params.setMargins(0, 0, 0, 0);
			}
			mRightText = new TextView(context);
			mRightText.setText(rightText);
			mRightText.setTextColor(getResources().getColor(R.color.nav_right_text));
			mRightText.setTextSize(mTextSize);
			this.addView(mRightText, params);
		}

		this.setBackgroundColor(Color.parseColor(mBackgroundColor));

		// 加入下边线
		params = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT, 1);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		View line = new View(context);
		line.setBackgroundColor(getResources().getColor(R.color.line));
		this.addView(line, params);
		if (mNavReceiver == null) {
			mNavReceiver = new NavReceiver();
			IntentFilter intentFilter = new IntentFilter(NAV_BROAD_CAST_FILTER);
			mContext.registerReceiver(mNavReceiver, intentFilter);
		}
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (mNavReceiver != null) {
			mContext.unregisterReceiver(mNavReceiver);
		}
	}
	
	/**
	 * @author LiYan
	 * @date 2014-9-5 下午1:45:11  
	 * @explain 取消广播接收
	 * @return void 
	 */
	public void unRegisterReceiver() {
		if (mNavReceiver != null) {
			mContext.unregisterReceiver(mNavReceiver);
			mNavReceiver = null;
		}
	}

	/**
	 * @author LiYan
	 * @date 2014-9-4 下午5:40:04  
	 * @explain 设置左侧按钮点击监听
	 * @return void
	 * @param listener 
	 */
	public void setLeftOnClickListener(OnClickListener listener) {
		if (mLeftText != null) {
			mLeftText.setOnClickListener(listener);
		}

		if (mLeftImgBtn != null) {
			mLeftImgBtn.setOnClickListener(listener);
		}
	}
	
	/**
	 * @author LiYan
	 * @date 2014-9-4 下午5:40:04  
	 * @explain 设置右侧按钮点击监听
	 * @return void
	 * @param listener 
	 */
	public void setRightOnClickListener(OnClickListener listener) {
		if (mRightText != null) {
			mRightText.setOnClickListener(listener);
		}
		
		if (mRightImgBtn != null) {
			mRightImgBtn.setOnClickListener(listener);
		}
	}
	
	/**
	 * @author LiYan
	 * @date 2014-9-7 下午4:12:33  
	 * @explain 设计title文字
	 * @return void
	 * @param str 
	 */
	public void setTitleText(String str) {
		mTitleText.setText(str);
	}

	private class NavReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (mTitleText != null) {
				String buildName = intent.getStringExtra(NAV_KEY_BUILD_NAME);
				if(buildName != null && !XunluUtil.isEmpty(buildName)) {
					mTitleText.setText(buildName);
				} 
			}
		}

	}
	
	/**
	 * @author LiYan
	 * @date 2014-9-7 下午4:12:05  
	 * @explain 设置title点击事件
	 * @return void
	 * @param listener 
	 */
	public void setTitleOnClickListener(OnClickListener listener) {
		if(mTitleText != null) {
			mTitleText.setOnClickListener(listener);
		}
	}
	
	public void setRightViewVisibility(boolean isVisible) {
		if(mRightImgBtn != null) {
			if(isVisible) {				
				mRightImgBtn.setVisibility(View.VISIBLE);
			} else {
				mRightImgBtn.setVisibility(View.GONE);
			}
		}
		if(mRightText != null) {
			if(isVisible) {				
				mRightText.setVisibility(View.VISIBLE);
			} else {
				mRightText.setVisibility(View.GONE);
			}
		}
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return true;
	}
}
