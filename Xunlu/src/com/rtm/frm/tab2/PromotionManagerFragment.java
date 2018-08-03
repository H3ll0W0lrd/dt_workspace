package com.rtm.frm.tab2;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rtm.frm.R;
import com.rtm.frm.dialogfragment.BaseDialogFragment;
import com.rtm.frm.fragment.BaseFragment;
import com.rtm.frm.fragment.controller.MyFragmentManager;
import com.rtm.frm.newui.PromotionModel;
import com.rtm.frm.utils.PreferencesUtil;
import com.rtm.frm.view.NavTitle;

public class PromotionManagerFragment extends BaseFragment implements
		View.OnClickListener {
	View contentView;
	Button btBack;
	CheckBox cBox1;
	CheckBox cBox2;
	CheckBox cBox3;
	private NavTitle mNavTitle;
	private LayoutInflater mLayoutInflater;
	private LinearLayout mPromotionListLay;
	private List<PromotionModel> mPromotions = new ArrayList<PromotionModel>();

	public PromotionManagerFragment() {
//		setStyle(DialogFragment.STYLE_NORMAL,
//				R.style.dialogfragment_transparent_bg);
	}

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		contentView = inflater.inflate(R.layout.fragment_promotion_manager,null);
		mLayoutInflater = inflater;
		return contentView;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initView(contentView);
	}

	@SuppressLint("InflateParams")
	private void initView(View contentView) {
		mNavTitle = (NavTitle) contentView.findViewById(R.id.nav_title);
		mNavTitle.unRegisterReceiver();
		mNavTitle.setTitleText(getResources().getString(
				R.string.promotion_manager_title));
		mNavTitle.setLeftOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MyFragmentManager.getInstance().backFragment();
			}
		});

		mPromotionListLay = (LinearLayout) contentView
				.findViewById(R.id.promotion_manager_list_lay);

		// 初始化数据
		for (int i = 0; i < 3; ++i) {
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
			promotion.isShow = PreferencesUtil.getBoolean(promotion.promotionName, true);
			mPromotions.add(promotion);
		}

		for (PromotionModel p : mPromotions) {
			View itemView = mLayoutInflater.inflate(
					R.layout.fragment_promotion_manager_item, null);
			TextView nameView = (TextView) itemView
					.findViewById(R.id.promotion_item_name);
			CheckBox checkBox = (CheckBox) itemView
					.findViewById(R.id.promotion_switch2);
			checkBox.setTag(p);
			checkBox.setChecked(p.isShow);
			checkBox.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					PromotionModel xxP = (PromotionModel) v.getTag();
					PreferencesUtil.putBoolean(xxP.promotionName,
							((CheckBox) v).isChecked());
					Log.e("manager p",xxP.promotionName+" is check:"+((CheckBox) v).isChecked());
				}
			});
			nameView.setText(p.promotionName);
			mPromotionListLay.addView(itemView);
		}
	}

	@Override
	public void onClick(View v) {
		// switch (v.getId()) {
		// case R.id.bt_back:
		// dismiss();
		// break;
		// case R.id.promotion_switch1:
		// PreferencesUtil.putBoolean("cBox1", cBox1.isChecked());
		// break;
		// case R.id.promotion_switch2:
		// PreferencesUtil.putBoolean("cBox2", cBox2.isChecked());
		// break;
		// case R.id.promotion_switch3:
		// PreferencesUtil.putBoolean("cBox3", cBox3.isChecked());
		// break;
		// default:
		// break;
		// }
	}

	@Override
	public void onDestroy() {
		callOnFinish(null);
		super.onDestroy();
	}
}
