package com.rtm.frm.thread;

import java.util.List;

import android.os.Handler;
import android.util.Log;

import com.rtm.frm.database.DBOperation;
import com.rtm.frm.model.FavorablePoiDbModel;
import com.rtm.frm.utils.ConstantsUtil;
import com.rtm.frm.utils.FavorablePoiParseUtil;

/**
 * @author liyan
 * 初始化建筑物,楼层数据线程
 */
public class InitFavorablesThread extends Thread {

	private String mData;
	
	private FavorablePoiParseUtil mFavPoiParse;
	
	private DBOperation mBatchOperation;
	
	private Handler mHandler;
	
	private int mWhat;
	
	public boolean isRunning = false;
	
	/**
	 * @param data 待解析的xml字符串
	 * @param handler 处理完毕后，接收的handler
	 * @param what 处理完毕后，msg.what
	 * @param buildsTable 需要初始化建筑物表名
	 * @param floorsTable 需要初始化楼层表名
	 */
	public InitFavorablesThread(String data,Handler handler,int what) {
		mData = data;
		mHandler = handler;
		mWhat = what;
	}
	
	@Override
	public void run() {
		isRunning = true;
		super.run();
		try {
			
			Log.e("InitFavorablesThread", "优惠数据初始化开始");
			//1.将字符串数据解析成对象
			mFavPoiParse =  new FavorablePoiParseUtil(mData);
			List<FavorablePoiDbModel> pois =  mFavPoiParse.mPois;
			
			DBOperation.getInstance().clearFavorableTableData();
			
			//2.批量插入数据库
			mBatchOperation = DBOperation.getInstance();
			mBatchOperation.insertFavorablePois(pois);
			Log.e("InitFavorablesThread", "优惠数据初始化结束");
		} catch(Exception e) {
			e.printStackTrace();
			mWhat = ConstantsUtil.HANDLER_THREAD_INIT_FAVORABLE_ERR;
		}
		//3.发送完成消息
		mHandler.sendEmptyMessage(mWhat);
		isRunning = false;
	}
}


