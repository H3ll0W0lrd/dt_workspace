package com.rtmap.experience.page;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.gson.Gson;
import com.rtmap.experience.R;
import com.rtmap.experience.core.KPBaseFragment;
import com.rtmap.experience.fragment.KPFragmentFactory;
import com.rtmap.experience.util.DTUIUtils;

public class KPMapListNewFragment extends KPBaseFragment implements
		OnClickListener {

	private RadioGroup mGroup;// 底部按钮组
	private RadioButton mSubmitTmpBtn, mSubmitBtn, mPreviewBtn;
	public static int tag;
	private Gson mGson;

	@Override
	protected View createLoadedView() {
		View view = DTUIUtils.inflate(R.layout.kp_map_list_new);
		mGson = new Gson();

		mGroup = (RadioGroup) view.findViewById(R.id.main_footer);
		mSubmitTmpBtn = (RadioButton) view.findViewById(R.id.main_submit_tmp);
		mSubmitBtn = (RadioButton) view.findViewById(R.id.main_submit);
		mPreviewBtn = (RadioButton) view.findViewById(R.id.main_preview);

		mSubmitTmpBtn.setOnClickListener(this);
		mSubmitBtn.setOnClickListener(this);
		mPreviewBtn.setOnClickListener(this);
		tag = KPFragmentFactory.TAB_SUBMIT_TMP;
		((CompoundButton) view.findViewById(tag)).setChecked(true);
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		setPage(tag);
	}

	/**
	 * 改变页面
	 * 
	 * @param position
	 */
	private void setPage(int position) {
		FragmentManager fragmentManager = getChildFragmentManager();
		// ViewPager页面被选中的回调
		KPBaseFragment fragment = KPFragmentFactory.createFragment(position);
		// 当页面被选中 再显示要加载的页面....防止ViewPager提前加载(ViewPager一般加载三个，自己，左一个，右一个)
		fragmentManager.beginTransaction().replace(R.id.list_layout, fragment)
				.commit();
	}

	@Override
	public void onClick(View v) {
		int checkedId = v.getId();
		if (checkedId == mSubmitTmpBtn.getId()) {
			setPage(KPFragmentFactory.TAB_SUBMIT_TMP);
		} else if (checkedId == mSubmitBtn.getId()) {
			setPage(KPFragmentFactory.TAB_SUBMIT);
		} else if (checkedId == mPreviewBtn.getId()) {
			setPage(KPFragmentFactory.TAB_PREVIEW);
		}
		tag = checkedId;
	}
}
