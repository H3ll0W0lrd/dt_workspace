package com.rtmap.experience.fragment;

import java.io.File;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.rtmap.experience.R;
import com.rtmap.experience.core.KPAsyncTask;
import com.rtmap.experience.core.KPBaseFragment;
import com.rtmap.experience.core.KPCallBack;
import com.rtmap.experience.core.exception.KPException;
import com.rtmap.experience.core.http.KPHttpClient;
import com.rtmap.experience.core.http.KPHttpUrl;
import com.rtmap.experience.core.model.BuildInfo;
import com.rtmap.experience.page.KPAddBuildActivity;
import com.rtmap.experience.util.DTFileUtils;
import com.rtmap.experience.util.DTUIUtils;

public class KPMakeFragment extends KPBaseFragment implements OnClickListener {

	private Gson mGson;
	private ImageView mAddBuild;

	@Override
	protected View createLoadedView() {
		View view = DTUIUtils.inflate(R.layout.main_make);
		mGson = new Gson();
		mAddBuild = (ImageView) view.findViewById(R.id.add_build);
		mAddBuild.setOnClickListener(this);
		return view;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.add_build:// 新增建筑物
			mLoadDialog.show();
			new KPAsyncTask(new AddBuildCall()).run();
			break;
		}
	}
	class AddBuildCall implements KPCallBack {

		@Override
		public Object onCallBackStart(Object... obj) {
			try {
				String result = KPHttpClient.getInfo(KPHttpClient.GET,
						KPHttpUrl.GET_BUILD_ID, new String[] { "key" },
						new String[] { mUser.getKey() });
				return result;
			} catch (KPException e) {
				e.printStackTrace();
				DTUIUtils.showToastSafe(e.getMessage());
			}
			return null;
		}

		@Override
		public void onCallBackFinish(Object obj) {
			mLoadDialog.cancel();
			if(obj!=null){
				String result = (String) obj;
				Gson gson = new Gson();
				BuildInfo build = gson.fromJson(result, BuildInfo.class);
				String path = DTFileUtils.getDataDir()+build.getBuildId()+File.separator;
				DTFileUtils.createDirs(path);
				DTFileUtils.writeFile(result, path+build.getBuildId()+".build", false);
				KPAddBuildActivity.interActivity(getActivity(),build);
			}
		}
	}
}
