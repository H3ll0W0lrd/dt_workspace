package com.rtm.frm.thread;

import java.util.List;

import android.os.Handler;
import android.util.Log;

import com.rtm.frm.database.DBOperation;
import com.rtm.frm.model.Build;
import com.rtm.frm.model.Floor;
import com.rtm.frm.utils.BuildsParseUtil;
import com.rtm.frm.utils.ConstantsUtil;

/**
 * @author liyan
 * 初始化建筑物,楼层数据线程
 */
public class InitBuildsThread extends Thread {

	private String mData;
	
	private BuildsParseUtil mBuildsParse;
	
	private DBOperation mBatchOperation;
	
	private Handler mHandler;
	
	private int mWhat;
	
	private String mBuildsTable;
	
	private String mFloorsTable;
	
	private boolean mIsPrivate;
	
	public boolean isRunning = false;
	
	/**
	 * @param data 待解析的xml字符串
	 * @param handler 处理完毕后，接收的handler
	 * @param what 处理完毕后，msg.what
	 * @param buildsTable 需要初始化建筑物表名
	 * @param floorsTable 需要初始化楼层表名
	 */
	public InitBuildsThread(String data,Handler handler,int what,String buildsTable,String floorsTable,boolean isPrivate) {
		mData = data;
		mHandler = handler;
		mWhat = what;
		mBuildsTable = buildsTable;
		mFloorsTable = floorsTable;
		mIsPrivate = isPrivate;
	}
	
	@Override
	public void run() {
		isRunning = true;
		super.run();
		try {
			Log.e("InitBuildsThread", "数据初始化开始");
			//1.将字符串数据解析成对象
			mBuildsParse = new BuildsParseUtil(mData,mIsPrivate);
			List<Build> builds =  mBuildsParse.mBuilds;
			List<Floor> floors = mBuildsParse.mFloors;
			
			DBOperation.getInstance().clearAllTableData(false);
			//2.批量插入数据库
			mBatchOperation = DBOperation.getInstance();
			mBatchOperation.insertBuildsBatch(builds,mBuildsTable);
			mBatchOperation.insertFloorsBatch(floors,mFloorsTable);
			Log.e("InitBuildsThread", "数据初始化结束");
		} catch(Exception e) {
			e.printStackTrace();
			mWhat = ConstantsUtil.HANDLER_THREAD_INIT_ERR;
		}
		//3.发送完成消息
		mHandler.sendEmptyMessage(mWhat);
		isRunning = false;
	}
}


