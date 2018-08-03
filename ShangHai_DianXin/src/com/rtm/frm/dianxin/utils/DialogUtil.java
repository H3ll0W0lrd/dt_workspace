package com.rtm.frm.dianxin.utils;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.WindowManager;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.rtm.frm.dianxin.R;
import com.rtm.frm.dianxin.manager.AppContext;

public class DialogUtil {
    // public DialogUtil instance = null;
    //
    // public DialogUtil() {
    // super();
    // }
    //
    // public DialogUtil getInstance() {
    // if (instance == null) {
    // instance = new DialogUtil();
    // }
    // return instance;
    // }
    private static DisplayImageOptions options;

    /**
     * 提示语弹出框 按钮默认标题为“确定”，点击后dialog自动消失
     *
     * @param context
     * @param title   标题
     * @param msg     提示信息
     * @return
     */
    public static Dialog showDialog(Context context, String title, String msg) {

        Builder builder = new Builder(context);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setPositiveButton("确定", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("取消", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        // builder.create().show();

        return builder.create();
    }

    /**
     * 提示语弹出框
     *
     * @param context
     * @param title                 标题
     * @param msg                   提示信息
     * @param okText((BaseActivity) getActivity()).pushActivity(RtmActivity.class, b);
     *                              左边按钮文字
     * @param okExcecutable         左边按钮执行方法
     * @param cancelText            右边按钮文字
     * @param cancelExcecutable     右边按钮执行方法
     * @return
     */
    public static Dialog showDialog(Context context, String title, String msg, String okText, final Executable okExcecutable,
                                    String cancelText, final Executable cancelExcecutable) {

        Builder builder = new Builder(context);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setPositiveButton(okText, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                if (okExcecutable != null) {
                    okExcecutable.execute();
                }
            }
        });
        builder.setNegativeButton(cancelText, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                if (cancelExcecutable != null) {
                    cancelExcecutable.execute();
                }
            }
        });
        // builder.create().show();

        return builder.create();
    }

    public static interface Executable {
        public void execute();
    }

    /**
     * 网络访问时的进度条
     *
     * @param context
     * @param cancelable 进度条是否可以在用户点击“back”按钮时消失
     * @param rCancel    进度条消失时执行自动执行的方法
     */
    public static Dialog getLoadingDialog(Context context, boolean cancelable, Runnable rCancel) {
        Dialog dialog = null;
        if (context == null) {
            dialog = new Dialog(AppContext.instance(), R.style.loading_small);
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        } else {
            dialog = new Dialog(context, R.style.loading_small);
        }
        dialog.setContentView(R.layout.fragment_loading);
//		dialog.setCancelable(false);
        // dialog.show();

        return dialog;
    }

//	public static Dialog getPoiDetailDialog(Context context, String title, String msg, String okText,
//			final Executable okExcecutable, String cancelText, final Executable cancelExcecutable) {
//		Dialog dialog = new Dialog(context, R.style.PoiDetailDialogStyle);
//
//		initImageLoaderOptions();
//
//		View v = View.inflate(context, R.layout.poi_detail, null);
//		LayoutParams p = new LayoutParams(LayoutParams.WRAP_CONTENT,
//				LayoutParams.WRAP_CONTENT);
//		dialog.setContentView(v,p);
//		ImageView img = (ImageView) v.findViewById(R.id.imageview_poi);
//		ImageLoader.getInstance().displayImage(
//				"http://g.hiphotos.baidu.com/image/pic/item/3c6d55fbb2fb43160b62835a25a4462309f7d395.jpg", img, options, null);
//
//		return dialog;
//	}
//
//	private static void initImageLoaderOptions() {
//		options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.loading)
//				.showImageForEmptyUri(R.drawable.load_fail).showImageOnFail(R.drawable.load_fail).cacheInMemory(true)
//				.cacheOnDisk(true).considerExifParams(true).displayer(new RoundedBitmapDisplayer(10)).build();
//	}

}
