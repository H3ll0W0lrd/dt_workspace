package com.example.fragmentdemo.fragment;

import android.os.AsyncTask;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.example.fragmentdemo.R;
import com.example.fragmentdemo.core.MTBaseFragment;
import com.example.fragmentdemo.util.MTUIUtils;
import com.example.fragmentdemo.util.MTViewUtils;
import com.example.fragmentdemo.util.view.MTLoadingPage;

public class MTOrderFragment extends MTBaseFragment {

	private MTLoadingPage mPage;
	private RelativeLayout mContainer;

	@Override
	protected View createLoadedView() {
		View view = MTUIUtils.inflate(R.layout.main_order);
		mPage = new MTLoadingPage(getActivity()) {
			@Override
			public void loadData() {
				super.loadData();
				new MyAsync().execute("load_click");
			}
		};
		mContainer = (RelativeLayout) view.findViewById(R.id.container);
		mContainer.addView(mPage, LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		
		new MyAsync().execute("create");
		return view;
	}
	
	@Override
	public String getPageName() {
		return null;
	}
	
	class MyAsync extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... params) {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return params[0];
		}
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if("create".equals(result)){
				mPage.setState(MTLoadingPage.STATE_ERROR);
			}else{
				MTViewUtils.removeSelfFromParent(mPage);
			}
		}
	}
}
