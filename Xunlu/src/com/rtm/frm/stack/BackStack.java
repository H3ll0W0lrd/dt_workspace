package com.rtm.frm.stack;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment.InstantiationException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 页面的状态，包含了页面的名称以及与该页面关联的数据，储存在堆栈里， activity异常关闭时，需要进行保存，以便恢复.
 * @author hukunge
 * @version 2014-08-18 下午19:58
 */
final class BackStackState implements Parcelable {
    final String mName;
    final String mClassName;
    final Bundle mArguments;
    final boolean mIsAdded;

    public BackStackState(BackStack b) {
	mName = b.mName;
	mClassName = b.getClass().getName();
	mArguments = b.mArgs;
	mIsAdded = b.mIsAdded;
    }

    public BackStackState(Parcel in) {
	mName = in.readString();
	mClassName = in.readString();
	mArguments = in.readBundle();
	mIsAdded = in.readInt() != 0;
    }

    public BackStack instantiate(Activity activity) {
	if (mArguments != null) {
	    mArguments.setClassLoader(activity.getClassLoader());
	}

	BackStack instance = BackStack.instantiate(activity, mClassName,
		mArguments);

	instance.mName = mName;

	return instance;
    }

    @Override
    public int describeContents() {
	return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
	dest.writeString(mName);
	dest.writeString(mClassName);
	dest.writeBundle(mArguments);
	dest.writeInt(mIsAdded ? 1 : 0);
    }
}

/**
 * 页面的基类，定义了生命周期以及相关的回调，包含了页面的数据.
 * 
 * @author hua.wu
 * 
 */
public abstract class BackStack {
    private static final HashMap<String, Class<?>> sClassMap = new HashMap<String, Class<?>>();

    /**
     * 创建的初始状态.
     */
    public static final int INITIALIZING = 0;

    /**
     * 创建页面对应的UI.
     */
    public static final int CREATED = 1;

    /**
     * 刷新UI，并且显示.
     */
    public static final int STARTED = 2;

    /**
     * 显示动画以及其余数据.
     */
    public static final int RESUMED = 3;

    int mState = INITIALIZING;

    public boolean mFromPopBack = false;

    protected BackStackActivity mActivity;

    protected ArrayList<View> mAnimationViews;

    String mName;
    Bundle mArgs;
    int mContainerId;
    boolean mIsAdded = false;

    View mView;
    ViewGroup mContainer;

    protected int mRequestCode = -1;

    private int mResultCode = Activity.RESULT_CANCELED;
    private Intent mResultData = null;

    public BackStack() {
    }

    /**
     * 
     * @param activity
     *            包含页面的activity.
     * @param name
     *            页面的名称.
     * @param container
     *            页面view的容器，可为null.
     */
    public BackStack(BackStackActivity activity, String name, int containerId) {
	mActivity = activity;
	mName = name;
	mContainerId = containerId;
    }

    /**
     * 
     * @param activity
     *            包含页面的activity.
     * @param name
     *            页面的名称.
     * @param container
     *            页面view的容器，可为null.
     * @param requestCode
     *            请求的code.
     */
    public BackStack(BackStackActivity activity, String name, int containerId,
	    int requestCode) {
	this(activity, name, containerId);
	mRequestCode = requestCode;
    }

    /**
     * 通过给定的名称，创建一个BackStack实例.
     * 
     * @param context
     * @param fname
     * @param args
     * @return
     */
    public static BackStack instantiate(Context context, String fname,
	    Bundle args) {
	try {
	    Class<?> clazz = sClassMap.get(fname);
	    if (clazz == null) {
		clazz = context.getClassLoader().loadClass(fname);
		sClassMap.put(fname, clazz);
	    }
	    BackStack bs = (BackStack) clazz.newInstance();
	    if (args != null) {
		args.setClassLoader(bs.getClass().getClassLoader());
		bs.mArgs = args;
	    }
	    return bs;
	} catch (ClassNotFoundException e) {
	    throw new InstantiationException("Unable to instantiate backstack "
		    + fname
		    + ": make sure class name exists, is public, and has an"
		    + " empty constructor that is public", e);
	} catch (java.lang.InstantiationException e) {
	    throw new InstantiationException("Unable to instantiate backstack "
		    + fname
		    + ": make sure class name exists, is public, and has an"
		    + " empty constructor that is public", e);
	} catch (IllegalAccessException e) {
	    throw new InstantiationException("Unable to instantiate backstack "
		    + fname
		    + ": make sure class name exists, is public, and has an"
		    + " empty constructor that is public", e);
	}
    }

    /**
     * 将数据传入页面.
     * 
     * @param args
     */
    public void setArguments(Bundle args) {
	mArgs = args;
    }

    /**
     * 创建与页面关联的view.
     * 
     * @return
     */
    protected abstract View onCreateView(LayoutInflater inflater,
	    ViewGroup container);

    protected abstract void onViewCreated(Bundle args);

    /**
     * 页面被用户可见.
     */
    protected abstract void onStart();

    /**
     * 页面对关联的数据进行显示.
     */
    protected abstract void onResume();

    /**
     * 页面对用户不可见.
     */
    protected abstract void onStopped();

    /**
     * 页面不可见后的回调.
     */
    protected abstract void onDismissed();

    /**
     * 页面需要被销毁.
     */
    protected abstract void onDestroyed();

    @Override
    public String toString() {
	return mName;
    }

    public LayoutInflater getLayoutInflater() {
	return mActivity.getLayoutInflater();
    }

    public void onBackStackResult(int requestCode, int resultCode, Intent data) {
    }

    protected void setResult(int resultCode, Intent data) {
	mResultCode = resultCode;
	mResultData = data;
    }

    public int getResultCode() {
	return mResultCode;
    }

    public Intent getResultData() {
	return mResultData;
    }

    void performStart() {
	onStart();
    }

    void performStop() {
	onStopped();
    }

    final public boolean isVisible() {
	return mView != null && mView.getWindowToken() != null
		&& mView.getVisibility() == View.VISIBLE;
    }

    public String getName() {
	return mName;
    }

    public int getState() {
	return mState;
    }
}
