/**
 * File name: SettingsFragment.java 
 *
 * Version information: 1.0.0
 *
 * Date: 2014-3-20 下午4:02:29
 *
 * Copyright 2014 Autonavi Software Co. Ltd. All Rights Reserved.
 *
 */

package com.rtm.frm.tab1;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;

import com.rtm.frm.R;
import com.rtm.frm.XunluApplication;
import com.rtm.frm.database.DBOperation;
import com.rtm.frm.fragment.BaseFragment;
import com.rtm.frm.fragment.controller.MyFragmentManager;
import com.rtm.frm.model.FavorablePoiDbModel;
import com.rtm.frm.net.NetWorkPost;
import com.rtm.frm.net.PostData;
import com.rtm.frm.newframe.NewFrameActivity;
import com.rtm.frm.newui.TestWebDialogFragment;
import com.rtm.frm.utils.ConstantsUtil;
import com.rtm.frm.utils.FavorablePoiParseUtil;
import com.rtm.frm.utils.ToastUtil;
import com.rtm.frm.utils.XunluUtil;
import com.umeng.analytics.MobclickAgent;

/**
 * ClassName: TestMineFragment date: 2014-9-4 下午9:21:04 我的好友页面
 * 
 * @author liyan
 * @version
 */
public class TabMineFragment extends BaseFragment implements View.OnClickListener, OnTouchListener {

	ListView mListView;
	TabMineListAdapter mListAdapter;

	private View footer;// 页脚-正在加载中.....
	private NextPageScrollListener mScrollListener;
	public static final int WHAT_NEXT_PAGE = -1;
	private List<String> mBuildIdList = new ArrayList<String>();
	private boolean mIsPosting = false;
	private boolean mIsSaveFavorablePois = false;
	private String mPostFavorableId = "";
	private String mPostFavorableCityName = "";

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case ConstantsUtil.HANDLER_POST_FAVORABLE_BY_CITY:
				if (msg.arg1 != ConstantsUtil.STATE_NET_ERR_UNUSED) {
					showFravorableList((String) msg.obj);
				} else {
					ToastUtil.shortToast(R.string.error_net_unuse);
				}
				mIsPosting = false;
				break;
			case WHAT_NEXT_PAGE:
				requestNextPage();
				break;
			default:
				break;
			}
		};
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View contentView = inflater.inflate(R.layout.fragment_tab_mine, container, false);
		footer = inflater.inflate(R.layout.layout_list_footer_loading, null);
		contentView.setOnTouchListener(this);
		initView(contentView);
		return contentView;
	}

	private void initView(View contentView) {
		// mBuildIdList.add("860100010010300002");//apm
		// mBuildIdList.add("860100010040300005");//当代
		mListView = (ListView) contentView.findViewById(R.id.tab1_list);
		mListView.setOnItemClickListener(myItemListener);
		mScrollListener = new NextPageScrollListener(mHandler, WHAT_NEXT_PAGE);
		mListView.setOnScrollListener(mScrollListener);
		mListView.addFooterView(footer);
		mListAdapter = new TabMineListAdapter();
		mListView.setAdapter(mListAdapter);
	}
	
	public Handler getHandler(){
		return mHandler;
	}
	
	public TabMineListAdapter getTabMineAdapter(){
		return mListAdapter;
	}

	OnItemClickListener myItemListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			MobclickAgent.onEvent(mContext,"event_click_activities_item");

			HeaderViewListAdapter tabMineListAdapter = (HeaderViewListAdapter) arg0.getAdapter();
			FavorablePoiDbModel poi = ((FavorablePoiDbModel) tabMineListAdapter.getItem(arg2));

			String url = "";
			if(NewFrameActivity.getInstance().getTab0().mMapShowBuildId.equals("860100010080300003")) {//如果是奥莱
				url = "http://10.100.56.229/";
			} else {
				url = XunluApplication.getApp().getRootUrl();
			}
			url = url + "template/push_details.php?id_poi="+poi.poiId+"&id_site="+poi.idSite+"&id_bridge="+poi.idBridge;
			// 后续将采用这种方式请求服务器页面
			// openWebView("http://open2.rtmap.net/H5/?poi_id=" + poiId,
			// "活动详情",(FavorablePoiDbModel)tabMineListAdapter.getItem(arg2));
			openWebView(url, "活动详情", (FavorablePoiDbModel) tabMineListAdapter.getItem(arg2));
		}
	};

	private void showFravorableList(String data) {
		if (!XunluUtil.isEmpty(data)) {

			FavorablePoiParseUtil parseUtil = new FavorablePoiParseUtil(data);

			if (parseUtil.mPois.size() == 0) {
				ToastUtil.shortToast("没有更多优惠信息");
			} else {
				mListAdapter.addData(parseUtil.mPois);
				saveCityFavorable(parseUtil.mPois, NewFrameActivity.getInstance().mCurrentGpsCity, mIsSaveFavorablePois);
			}

		} else {
			ToastUtil.shortToast("没有更多优惠信息");
		}
		mIsSaveFavorablePois = false;
		if (mListView.getFooterViewsCount() > 0) {
			mListView.removeFooterView(footer);
		}
	}

	private void showFravorableList(List<FavorablePoiDbModel> pois) {

		if (pois.size() == 0) {
			ToastUtil.shortToast("没有更多优惠信息");
		}
		mListAdapter.addData(pois);

		if (mListView.getFooterViewsCount() > 0) {
			mListView.removeFooterView(footer);
		}
	}

	/**
	 * @explain 下一页请求，在这里面可以写请求规则，listview最后一行显示出来，则请求下一页。
	 * */
	private void requestNextPage() {

		if (mIsPosting) {
			return;
		}

		if (mBuildIdList.size() > 0) {

		} else {
			if (mListView.getFooterViewsCount() == 0) {
				mListView.addFooterView(footer);
			}
			if (!NetWorkPost.detectInter(XunluApplication.mApp)) {//如果没有网络的情况，直接读取本地db
				if(mListView.getChildCount() == 0) {
					List<FavorablePoiDbModel> pois = DBOperation.getInstance().queryFavorablePoisAll();
					showFravorableList(pois);
				} else {
					if (mListView.getFooterViewsCount() != 0) {
						mListView.removeFooterView(footer);
					}
				}
			} else {
				if (NewFrameActivity.getInstance().getTab0().mCurrentLocation == null) {//判断室内定位是否成功
					List<String> dbFavCitys = DBOperation.getInstance().queryFavorableCitys();
					if (dbFavCitys.size() != 0 && NewFrameActivity.getInstance().mCurrentGpsCity.equals(dbFavCitys.get(0))) {//如果本地数据城市，与GPS城市相同，则读取本地
						if(mListView.getChildCount() == 0) {
							List<FavorablePoiDbModel> pois = DBOperation.getInstance().queryFavorablePoisAll();
							showFravorableList(pois);
						} else {
							if (mListView.getFooterViewsCount() != 0) {
								mListView.removeFooterView(footer);
							}
						}
					} else {//根据城市名，请求网络
						mIsSaveFavorablePois = true;
						if(!mPostFavorableCityName.equals(NewFrameActivity.getInstance().mCurrentGpsCity)) {
							mPostFavorableCityName = NewFrameActivity.getInstance().mCurrentGpsCity;
							PostData.postFavorableByCity(mHandler, ConstantsUtil.HANDLER_POST_FAVORABLE_BY_CITY, NewFrameActivity.getInstance().mCurrentGpsCity, 0, 0, 1);
							MobclickAgent.onEvent(mContext,"event_click_activities_loading");
						} else {
							if (mListView.getFooterViewsCount() != 0) {
								mListView.removeFooterView(footer);
							}
						}
					}
				} else {//室内，根据建筑ID请求网络
					String buildId = NewFrameActivity.getInstance().getTab0().mCurrentBuildId;
					if(!mPostFavorableId.equals(buildId)) {
						mPostFavorableId = buildId;
						PostData.postFavorableByBuildIdAndFloor(mHandler, ConstantsUtil.HANDLER_POST_FAVORABLE_BY_CITY, buildId, null);
						MobclickAgent.onEvent(mContext,"event_click_activities_loading");
					} else {
						if (mListView.getFooterViewsCount() != 0) {
							mListView.removeFooterView(footer);
						}
					}
				}
			}
		}
		mIsPosting = true;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case 0:
			break;
		}
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return true;
	}

	/**
	 * listview滚动监听类
	 * 
	 */
	public class NextPageScrollListener implements OnScrollListener {

		private Handler mNextPageHandler;
		private int mNextPageWhat;

		public NextPageScrollListener(Handler handler, int what) {
			mNextPageHandler = handler;
			mNextPageWhat = what;
		}

		/**
		 * 监听滚动状态改变：1-手指正在滑动 2-手指停止滑动 3-组件停止滚动
		 */
		public void onScrollStateChanged(AbsListView view, int scrollState) {
		}

		/**
		 * firstVisibleItem：第一个可见item visibleItemCount：可见item数量
		 * totalItemCount：总条目数量
		 */
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			/* 如果滚动到最后一条 */
			if (mListView.getLastVisiblePosition() + 1 == totalItemCount) {
				if (totalItemCount > 0) {
					mNextPageHandler.sendEmptyMessage(mNextPageWhat);
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
	private void openWebView(String url, String title, FavorablePoiDbModel poi) {
		TestWebDialogFragment webDialogFragment = new TestWebDialogFragment(url, title);
		webDialogFragment.setFavorablePoiDbModel(poi);
//		MyFragmentManager.showFragmentdialog(webDialogFragment, MyFragmentManager.PRCOCESS_DIALOGFRAGMENT_PROMOTION_DETAIL,
//				MyFragmentManager.DIALOGFRAGMENT_PROMOTION_DETAIL);
		MyFragmentManager.getInstance().replaceFragment(NewFrameActivity.ID_ALL, webDialogFragment,  MyFragmentManager.PRCOCESS_DIALOGFRAGMENT_PROMOTION_DETAIL, MyFragmentManager.DIALOGFRAGMENT_PROMOTION_DETAIL);
	}

	/**
	 * 将优惠信息存储到DB中,注意，db中，只保存一个城市的优惠信息
	 * */
	public void saveCityFavorable(List<FavorablePoiDbModel> pois, String cityName, boolean isSave) {
		if (pois.size() == 0 || !isSave) {
			Log.e("TabMinFragment", "save city favorable pois size 0 or needn't save");
			return;
		}
		for (FavorablePoiDbModel f : pois) {
			f.cityName = NewFrameActivity.getInstance().mCurrentGpsCity;
		}
		DBOperation.getInstance().clearFavorableTableData();
		DBOperation.getInstance().insertFavorablePois(pois);
	}

}
