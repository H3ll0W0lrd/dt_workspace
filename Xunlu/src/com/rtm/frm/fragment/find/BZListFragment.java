/**
 * @author liyan
 * @date 2014.08.18 21:15
 */
package com.rtm.frm.fragment.find;

import java.util.ArrayList;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.rtm.frm.R;
import com.rtm.frm.adapter.BZListAdapter;
import com.rtm.frm.fragment.BaseFragment;
import com.rtm.frm.fragment.controller.MyFragmentManager;
import com.rtm.frm.model.BaoZangSuccData;
import com.rtm.frm.model.BaoZangSuccListParser;
import com.rtm.frm.net.PostData;
import com.rtm.frm.newframe.NewFrameActivity;
import com.rtm.frm.utils.ConstantsUtil;


public class BZListFragment extends BaseFragment implements OnItemClickListener ,View.OnClickListener ,OnTouchListener{
	//View 
	ListView mBZListView;
	BZListAdapter mBzAdapter;
	ImageView mNoBzBg;
	ImageView mImgTop;
	LinearLayout mLinBack;
	
	//Data
	ArrayList<BaoZangSuccData> dataArray = new ArrayList<BaoZangSuccData>();
	BZListAdapter bzListAdapter = new BZListAdapter();
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	    Bundle savedInstanceState) {
		View contentView = inflater.inflate(R.layout.fragment_bz_list,
			container, false);
		return contentView;
    }
    
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    	super.onViewCreated(view, savedInstanceState);
    	initView(view);
    	postBZSuccList();
    }

    private void postBZSuccList() {
    	String buildId = NewFrameActivity.getInstance().getTab0().mMapShowBuildId;
    	PostData.postSuccBZList(mHandler, ConstantsUtil.HANDLER_POST_BZ_LIST,buildId);
	}

	private void initView(View v) {
    	mBZListView = (ListView)v.findViewById(R.id.bz_list);
		mNoBzBg = (ImageView)v.findViewById(R.id.img_bg_no_bz);
		mImgTop = (ImageView)v.findViewById(R.id.img_bz);
		mLinBack = (LinearLayout)v.findViewById(R.id.lin_back);
		mLinBack.setOnClickListener(this);
    }

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
	}
    
	Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case ConstantsUtil.HANDLER_POST_BZ_LIST:
				String result = (String)msg.obj;
				BaoZangSuccListParser bzList = new BaoZangSuccListParser(result);
				dataArray = bzList.getBZList();
				
				setListView();
				break;

			default:
				break;
			}
		}
	};
	

	private void setListView() {
		if(dataArray.size() == 0){
			mImgTop.setVisibility(View.GONE);
			mNoBzBg.setVisibility(View.VISIBLE);
		}else{
			mImgTop.setVisibility(View.VISIBLE);
			mBZListView.setVisibility(View.VISIBLE);
			
			bzListAdapter.setData(dataArray);
			mBZListView.setAdapter(bzListAdapter);
		}
	};
	
	Bundle b = new Bundle();
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.fail_keep:
			b.putBoolean("isKeepSucc", true);
			MyFragmentManager.getInstance().backFragment();
			break;
		case R.id.fail_quit:
			b.putBoolean("isCloseAll", true);
			MyFragmentManager.getInstance().backFragment();
			break;
		case R.id.lin_back:
			MyFragmentManager.getInstance().backFragment();
			break;
		default:
			break;
		}
	}

	@Override
	public void onDestroy() {
		b.putBoolean("isKeepSucc", true);
		callOnFinish(b);
		
		super.onDestroy();
	}

	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		return true;
	}
}