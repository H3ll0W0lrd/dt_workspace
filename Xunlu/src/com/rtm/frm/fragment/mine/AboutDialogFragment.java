package com.rtm.frm.fragment.mine;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.rtm.frm.R;
import com.rtm.frm.XunluApplication;
import com.rtm.frm.dialogfragment.BaseDialogFragment;

public class AboutDialogFragment extends BaseDialogFragment implements
		View.OnClickListener {

	public View contentView;
	private TextView mTextWeibo;
	private TextView mTextPage;
	private TextView mTextVersion;
	private ImageButton button_back;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup vg, Bundle b) {
		super.onCreateView(inflater, vg, b);
		contentView = inflater.inflate(R.layout.fragment_about, vg, false);
		initView(contentView);
		return contentView;
	}

	private void initView(View contentView) {
		mTextWeibo = (TextView) contentView.findViewById(R.id.text_weibo);
		mTextPage = (TextView) contentView.findViewById(R.id.text_page);
		mTextVersion = (TextView) contentView.findViewById(R.id.text_version);
		mTextWeibo.setOnClickListener(this);
		mTextPage.setOnClickListener(this);
		mTextVersion.setText(XunluApplication.mApp.getCurrentVersion());

		TextView title = (TextView) contentView.findViewById(R.id.text_title);
		title.setText(R.string.title_about);

		button_back = (ImageButton) contentView.findViewById(R.id.about_button_back);
		button_back.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		switch (v.getId()) {
		case R.id.about_button_back:// 返回
			dismiss();
			break;
		case R.id.text_weibo:
			// 微博链接
			Uri uri = Uri.parse(getString(R.string.title_about_weibo));
			intent.setData(uri);
			startActivity(intent);
			break;
		case R.id.text_page:
			// 官网链接
			uri = Uri.parse(getString(R.string.title_about_page));
			intent.setData(uri);
			startActivity(intent);
			break;
		case R.id.text_version:
			// 点击版本信息，检测升级
			break;
		default:
			break;
		}

	}

}
