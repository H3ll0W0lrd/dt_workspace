package com.rtmap.experience.fragment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.google.gson.Gson;
import com.rtmap.experience.R;
import com.rtmap.experience.adapter.KPBuildListAdapter;
import com.rtmap.experience.core.KPAsyncTask;
import com.rtmap.experience.core.KPBaseFragment;
import com.rtmap.experience.core.KPCallBack;
import com.rtmap.experience.core.model.BuildInfo;
import com.rtmap.experience.page.KPAddBuildActivity;
import com.rtmap.experience.util.DTFileUtils;
import com.rtmap.experience.util.DTLog;
import com.rtmap.experience.util.DTStringUtils;
import com.rtmap.experience.util.DTUIUtils;
import com.rtmap.experience.util.DTViewUtils;
import com.rtmap.experience.util.menulist.SwipeMenu;
import com.rtmap.experience.util.menulist.SwipeMenuCreator;
import com.rtmap.experience.util.menulist.SwipeMenuItem;
import com.rtmap.experience.util.menulist.SwipeMenuListView;
import com.rtmap.experience.util.menulist.SwipeMenuListView.OnMenuItemClickListener;
import com.rtmap.experience.util.menulist.SwipeMenuListView.OnSwipeListener;
import com.rtmap.experience.util.view.DTLoadingPage;

public class KPSubmitFragment extends KPBaseFragment implements
		OnItemClickListener {

	private SwipeMenuListView mList;
	private KPBuildListAdapter mAdapter;
	private DTLoadingPage mPage;
	private RelativeLayout mContainer;
	private Gson mGson;

	@Override
	protected View createLoadedView() {
		View view = DTUIUtils.inflate(R.layout.map_submit_tmp);
		mPage = new DTLoadingPage(getActivity()) {
			@Override
			public void loadData() {
				super.loadData();
				new KPAsyncTask(new BuildListModel()).run();
			}
		};
		mGson = new Gson();
		mContainer = (RelativeLayout) view.findViewById(R.id.container);
		mList = (SwipeMenuListView) view.findViewById(R.id.list);
		mAdapter = new KPBuildListAdapter();
		mList.setAdapter(mAdapter);
		initList();
		mContainer.addView(mPage, LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		mList.setOnItemClickListener(this);
		new KPAsyncTask(new BuildListModel()).run();
		return view;
	}

	private void initList() {
		// step 1. create a MenuCreator
		SwipeMenuCreator creator = new SwipeMenuCreator() {

			@Override
			public void create(SwipeMenu menu) {
				// create "open" item
				SwipeMenuItem openItem = new SwipeMenuItem(getActivity());
				// set item background
				openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
						0xCE)));
				// set item width
				openItem.setWidth(DTUIUtils.dip2px(90));
				// set item title
				openItem.setTitle("上传");
				// set item title fontsize
				openItem.setTitleSize(18);
				// set item title font color
				openItem.setTitleColor(Color.WHITE);
				// add to menu
				menu.addMenuItem(openItem);

				// create "delete" item
				SwipeMenuItem deleteItem = new SwipeMenuItem(getActivity());
				// set item background
				deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
						0x3F, 0x25)));
				// set item width
				deleteItem.setWidth(DTUIUtils.dip2px(90));
				// set a icon
				deleteItem.setIcon(R.drawable.ic_delete);
				// add to menu
				menu.addMenuItem(deleteItem);
			}
		};
		// set creator
		mList.setMenuCreator(creator);

		// step 2. listener item click event
		mList.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(int position, SwipeMenu menu,
					int index) {
				BuildInfo info = mAdapter.getItem(position);
				switch (index) {
				case 0:// 上传

					break;
				case 1:
					// delete
					String path = DTFileUtils.getDataDir() + info.getBuildId()
							+ File.separator;
					DTFileUtils.deleteFile(new File(path));
					mAdapter.removeItem(info);
					mAdapter.notifyDataSetChanged();
					break;
				}
				return false;
			}
		});

		// set SwipeListener
		mList.setOnSwipeListener(new OnSwipeListener() {

			@Override
			public void onSwipeStart(int position) {
				// swipe start
			}

			@Override
			public void onSwipeEnd(int position) {
				// swipe end
			}
		});
	}

	/**
	 * 加载本地建筑物列表
	 * 
	 * @author dingtao
	 * 
	 */
	class BuildListModel implements KPCallBack {
		private ArrayList<BuildInfo> list = new ArrayList<BuildInfo>();

		@Override
		public Object onCallBackStart(Object... obj) {
			String[] files = DTFileUtils.listFiles(DTFileUtils.getDataDir(),
					new FilenameFilter() {// 只需要上传.walk1文件

						@Override
						public boolean accept(File dir, String filename) {
							return true;
						}
					});
			list.clear();
			for (String name : files) {
				String path = DTFileUtils.getDataDir() + name + File.separator
						+ name + ".build";
				File file = new File(path);
				if (file.exists()) {
					BufferedReader br;
					try {
						br = new BufferedReader(new FileReader(file));
						String line, result = "";
						while ((line = br.readLine()) != null) {
							// 将文本打印到控制台
							result += line;
						}
						if (!DTStringUtils.isEmpty(result)) {
							DTLog.i("buildInfo : " + result);
							list.add(mGson.fromJson(result, BuildInfo.class));
						}
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						file.delete();
						e.printStackTrace();
					}
				}
			}
			return null;
		}

		@Override
		public void onCallBackFinish(Object obj) {
			mAdapter.addList(list);
			if (mAdapter.getCount() != 0) {
				DTViewUtils.removeSelfFromParent(mPage);
			} else {
				mPage.setState(DTLoadingPage.STATE_ERROR);
			}
			mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		BuildInfo build = mAdapter.getItem(position);
		KPAddBuildActivity.interActivity(getActivity(), build);
	}
}
