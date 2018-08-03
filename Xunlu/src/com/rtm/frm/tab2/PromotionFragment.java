/**
 * @author hukunge
 * @date 2014.09.02
 */
package com.rtm.frm.tab2;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rtm.frm.R;
import com.rtm.frm.fragment.BaseFragment;
import com.rtm.frm.fragment.BaseFragment.OnFinishListener;
import com.rtm.frm.fragment.controller.MyFragmentManager;
import com.rtm.frm.newframe.NewFrameActivity;
import com.rtm.frm.newui.PromotionModel;
import com.rtm.frm.newui.TestWebDialogFragment;
import com.rtm.frm.utils.PreferencesUtil;
import com.rtm.frm.view.NavTitle;

@SuppressLint("InflateParams")
public class PromotionFragment extends BaseFragment implements
		View.OnClickListener, OnFinishListener {
	View contentView;
	Button btBack;
	Button btManager;
	RelativeLayout relItem1;
	RelativeLayout relItem2;
	RelativeLayout relItem3;
	LinearLayout linItem1;
	
	private LinearLayout mGroupLayout1;
	private LinearLayout mGroupLayout2;
	private LinearLayout mGroupLayout3;
	private NavTitle mNavTitle;
	private List<PromotionModel> mPromotions = new ArrayList<PromotionModel>();
	
	private LayoutInflater mLayoutInflater;
	
	private View mPromotionAbout;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		mLayoutInflater = inflater;
		contentView = inflater.inflate(R.layout.fragment_promotion, container,
				false);
		initView(contentView);
		return contentView;
	}

	private void initView(View contentView) {
		
		mNavTitle = (NavTitle) contentView.findViewById(R.id.nav_title);
		mNavTitle.unRegisterReceiver();
		mNavTitle.setTitleText(getResources().getString(R.string.promotion_title));
		
		mNavTitle.setRightOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PromotionManagerFragment pro = new PromotionManagerFragment();
				pro.setOnFinishListener(PromotionFragment.this);
				MyFragmentManager.getInstance().replaceFragment(
						NewFrameActivity.ID_ALL, 
						pro, 
						MyFragmentManager.PRCOCESS_DIALOGFRAGMENT_PROMOTION_MANAGER,
						MyFragmentManager.DIALOGFRAGMENT_PROMOTION_MANAGER);
			}
		});
		
		mPromotionAbout = contentView.findViewById(R.id.promotion_about);
		mPromotionAbout.findViewById(R.id.promotion_item_img).setBackgroundResource(R.drawable.icon_about);
		mPromotionAbout.findViewById(R.id.top_long).setVisibility(View.VISIBLE);
		mPromotionAbout.findViewById(R.id.bottom_long).setVisibility(View.VISIBLE);
		((TextView)mPromotionAbout.findViewById(R.id.promotion_item_tv)).setText("关于");
		mPromotionAbout.setOnClickListener(this);

		mGroupLayout1 = (LinearLayout) contentView.findViewById(R.id.group1);
		mGroupLayout2 = (LinearLayout) contentView.findViewById(R.id.group2);
		mGroupLayout3 = (LinearLayout) contentView.findViewById(R.id.group3);
		
		for(int i = 0;i < 3;++i) {
			PromotionModel promotion = new PromotionModel();
			promotion.imgId = R.drawable.icon_sale_price;
			if (i < 1) {
				promotion.groupId = 1;//第一组
				promotion.detailUrl = "http://www.rtmap.com";
				promotion.promotionName = "特卖汇";
			} else if (i >= 1 && i <= 1) {
				promotion.groupId = 2;//第二组
				promotion.detailUrl = "http://www.rtmap.com";
				promotion.promotionName = "金币任务";
			} else {
				promotion.groupId = 3;//第三组
				promotion.detailUrl = "http://www.rtmap.com";
				promotion.promotionName = "减价关注";
			}
			mPromotions.add(promotion);
		}
		setVisiable();
	}

	/**
	 * @author LiYan
	 * @date 2014-9-9 下午7:58:22  
	 * @explain 设置显示项目
	 * @return void 
	 */
	public void setVisiable() {
		for(int i = 0;i < mPromotions.size();++i) {
			PromotionModel p = mPromotions.get(i);
			p.isShow = PreferencesUtil.getBoolean(p.promotionName,true);
			switch (p.groupId) {
			case 1:
				p.imgId = R.drawable.icon_sale_price;
				showPromotionView(p,mGroupLayout1);
				break;
			case 2:
				p.imgId = R.drawable.icon_glod_list;
				showPromotionView(p,mGroupLayout2);
				break;
			case 3:
				p.imgId = R.drawable.icon_sale_price;
				showPromotionView(p,mGroupLayout3);
				break;
			}
			
		}
	}
	
	/**
	 * @author LiYan
	 * @date 2014-9-9 下午7:58:32  
	 * @explain 根据对应的layout，显示/隐藏指定的item
	 * @return void
	 * @param promotion
	 * @param groupLayout 
	 */
	private void showPromotionView(PromotionModel promotion,LinearLayout groupLayout) {
		View itemView = mLayoutInflater.inflate(R.layout.fragment_promotion_item, null);
		View lineTopShort = itemView.findViewById(R.id.top_short);
		View lineTopLong = itemView.findViewById(R.id.top_long);
//		View lineBottomShort = itemView.findViewById(R.id.bottom_short);
		View lineBottomLong = itemView.findViewById(R.id.bottom_long);
		if(promotion.isShow) {
			for(int i = 0;i < groupLayout.getChildCount();++i) {
				PromotionModel promotionTag = (PromotionModel) groupLayout.getChildAt(i).getTag();
				if(promotion.promotionName.equals(promotionTag.promotionName)) {
					return;
				}
			}
			//显示操作
			if(groupLayout.getChildCount() == 0) {
				lineTopLong.setVisibility(View.VISIBLE);
				lineBottomLong.setVisibility(View.VISIBLE);
			} else {
				View frontView = groupLayout.getChildAt(groupLayout.getChildCount()-1);
				frontView.findViewById(R.id.bottom_long).setVisibility(View.GONE);
				frontView.findViewById(R.id.bottom_short).setVisibility(View.GONE);
				lineTopShort.setVisibility(View.VISIBLE);
				lineBottomLong.setVisibility(View.VISIBLE);
			}
			itemView.setTag(promotion);
			TextView itemName = (TextView) itemView.findViewById(R.id.promotion_item_tv);
			itemName.setText(promotion.promotionName);
			ImageView iconImg = (ImageView) itemView.findViewById(R.id.promotion_item_img);
			iconImg.setBackgroundResource(promotion.imgId);
			groupLayout.addView(itemView);
			itemView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					openWebView(((PromotionModel)v.getTag()).detailUrl,"活动详情");
				}
			});
		} else {
			// 隐藏操作
			for(int i = 0;i < groupLayout.getChildCount();++i) {
				View removeItemView = groupLayout.getChildAt(i);
				PromotionModel removePromotion = (PromotionModel) removeItemView.getTag();
				if(promotion.promotionName.equals(removePromotion.promotionName)) {//移除操作
					if(i == 0) {
						if(groupLayout.getChildCount() > 1) {//如果子view个数大于1，则要把后面的viewtop设置为长线
							View nextItemView = groupLayout.getChildAt(i + 1);
							nextItemView.findViewById(R.id.top_long).setVisibility(View.VISIBLE);
							nextItemView.findViewById(R.id.top_short).setVisibility(View.GONE);
						}
						groupLayout.removeViewAt(i); 
					} else {
						if(i+1 != groupLayout.getChildCount()) {//如果当前位置不在group 末尾，直接移除
							groupLayout.removeViewAt(i); 
						} else {//如果在末尾，需要将前一个viewbottom设置为长线
							View frontItemView = groupLayout.getChildAt(i - 1);
							frontItemView.findViewById(R.id.bottom_long).setVisibility(View.VISIBLE);
							frontItemView.findViewById(R.id.bottom_short).setVisibility(View.GONE);
							groupLayout.removeViewAt(i); 
						}
					}
				}
			}
		}
	}
	
	/**
	 * @author LiYan
	 * @date 2014-9-9 下午7:27:15  
	 * @explain 打开详情web页面
	 * @return void
	 * @param url 
	 */
	private void openWebView(String url,String title) {
		TestWebDialogFragment webDialogFragment = new TestWebDialogFragment(url,title);
//		MyFragmentManager.showFragmentdialog(webDialogFragment, MyFragmentManager.PRCOCESS_DIALOGFRAGMENT_PROMOTION_DETAIL, MyFragmentManager.DIALOGFRAGMENT_PROMOTION_DETAIL);
		MyFragmentManager.getInstance().replaceFragment(NewFrameActivity.ID_ALL, webDialogFragment,  MyFragmentManager.PRCOCESS_DIALOGFRAGMENT_PROMOTION_DETAIL, MyFragmentManager.DIALOGFRAGMENT_PROMOTION_DETAIL);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.lin_1:
//			MyFragmentManager.showFragmentdialog(new FindDialogFragment(),
//					MyFragmentManager.PRCOCESS_DIALOGFRAGMENT_FIND,
//					MyFragmentManager.DIALOGFRAGMENT_FIND);
//			break;
		case R.id.bt_manager:
			PromotionManagerFragment pro = new PromotionManagerFragment();
			pro.setOnFinishListener(this);
//
//			MyFragmentManager
//					.showFragmentdialog(
//							pro,
//							MyFragmentManager.PRCOCESS_DIALOGFRAGMENT_PROMOTION_MANAGER,
//							MyFragmentManager.DIALOGFRAGMENT_PROMOTION_MANAGER);
			MyFragmentManager.getInstance().replaceFragment(
					NewFrameActivity.ID_ALL, 
					pro, 
					MyFragmentManager.PRCOCESS_DIALOGFRAGMENT_PROMOTION_MANAGER,
					MyFragmentManager.DIALOGFRAGMENT_PROMOTION_MANAGER);
			break;
		case R.id.promotion_about:
			openWebView("http://www.rtmap.com","关于");
			break;
		}
	}

	@Override
	public void onFinish(String flag, Bundle data) {
		setVisiable();
	}
}