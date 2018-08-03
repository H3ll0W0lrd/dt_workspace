package com.rtmap.wifipicker.widget;

import com.rtmap.wifipicker.R;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 用于创建对话框
 * 
 * @author MJ
 */
public class DialogBulder {
   Dialog dialog;
   Context context;

   public int dialogId;

   public DialogBulder(Context context, int dialogId) {
      Dialog dialog = new Dialog(context, R.style.Dialog);
      dialog.setContentView(R.layout.dilog);// 先设置要显示的内容
      //dialog.getWindow().getAttributes().width = 280;// 调节对话框的宽度
      this.dialog = dialog;
      this.context = context;
      this.dialogId = dialogId;
   }

   public DialogBulder(Context context) {
      this(context, 0);
   }

   /**
    * 设置标题
    * 
    * @param title
    *           标题内容(字符串 或者 资源id)
    */
   public DialogBulder setTitle(Object title) {
      // setTitle("提示")
      // setTitle(R.string.tip)
      TextView titleView = getView(R.id.title);
      titleView.setText(parseParam(title));

      return this;
   }

   /**
    * 设置中间的内容
    */
   public DialogBulder setMessage(Object message) {
      TextView messageView = getView(R.id.message);
      messageView.setText(parseParam(message));

      return this;
   }

   Button left;

   public DialogBulder setButtons(Object leftBtn, Object rightBtn, final OnDialogButtonClickListener listener) {
      // 设置左边按钮的文字
      Button left = getView(R.id.left);
      left.setText(parseParam(leftBtn));
      // 给按钮绑定监听器
      left.setOnClickListener(new View.OnClickListener() {
         public void onClick(View v) {
            dialog.dismiss();

            if (listener != null) { // 有监听器
               listener.onDialogButtonClick(context, DialogBulder.this, dialog, dialogId,
                     OnDialogButtonClickListener.BUTTON_LEFT);
            }
         }
      });
      this.left = left;

      // 设置右边按钮的文字
      Button right = getView(R.id.right);
      right.setText(parseParam(rightBtn));
      // 给按钮绑定监听器
      right.setOnClickListener(new View.OnClickListener() {
         public void onClick(View v) {
            dialog.dismiss();

            if (listener != null) { // 有监听器
               listener.onDialogButtonClick(context, DialogBulder.this, dialog, dialogId,
                     OnDialogButtonClickListener.BUTTON_RIGHT);
            }
         }
      });
      return this;
   }

   /**
    * 通过id找到对话框中对应的子控件
    * 
    * @param id
    *           子控件的id
    * @return 子控件
    */
   @SuppressWarnings("unchecked")
   public <T extends View> T getView(int id) {
      return (T) dialog.findViewById(id);
   }

   /**
    * 解析参数
    * 
    * @param param
    *           字符串 或者 资源id
    * @return 统一返回字符串
    */
   private String parseParam(Object param) {
      if (param instanceof Integer) {
         return context.getString((Integer) param);
      } else if (param instanceof String) {
         return param.toString();
      }
      return null;
   }

   /**
    * 说明已经确定对话框的界面参数
    * 
    * @return
    */
   public Dialog create() {
      if (left == null) { // 说明不需要按钮
         // 得到按钮所在的布局
         ViewGroup btnsLayout = getView(R.id.btns_layout);
         // 得到根节点
         ViewGroup root = getView(R.id.root);
         // 移除按钮所在的布局
         // ViewGroup root = (ViewGroup) btnsLayout.getParent();
         root.removeView(btnsLayout);
      }
      return dialog;
   }

   public interface OnDialogButtonClickListener {
      public static final int BUTTON_LEFT = 0;
      public static final int BUTTON_RIGHT = 1;

      public void onDialogButtonClick(Context context, DialogBulder builder, Dialog dialog, int dialogId, int which);
   }

   /**
    * 将view放进对话框中
    */
   public DialogBulder setView(View view) {
      // 1.获得message所在的布局
      ViewGroup messageLayout = getView(R.id.message_layout);
      // 2.移除TextView
      messageLayout.removeAllViews();
      // 3.添加新的View
      messageLayout.addView(view);
      return this;
   }

   /**
    * 将布局文件对应的View放进对话框中
    * 
    * @param layout
    * @return
    */
   public DialogBulder setView(int layout) {
      ViewGroup parent = getView(R.id.message_layout);
      View view = LayoutInflater.from(context).inflate(layout, parent, false);
      return setView(view);
   }

   public DialogBulder setItems(String[] items, final OnDialogItemClickListener listener) {
      // 1.把ListView放进对话框中
      setView(R.layout.list_view);

      // 2.设置ListView的数据
      ListView listView = getView(android.R.id.list);
      // item_text_view的根节点必须是TextView
      ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.item_text_view, items);
      // new ArrayAdapter<T>(context, R.layout.item_text_view, R.id.text_view,
      // items)
      // listView.setAdapter(new SImple)
      listView.setAdapter(adapter);

      // 3.给ListView绑定监听器
      listView.setOnItemClickListener(new OnItemClickListener() {
         public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            dialog.dismiss();

            if (listener != null) {
               listener.onDialogItemClick(context, DialogBulder.this, dialog, position);
            }
         }
      });
      return this;
   }

   public DialogBulder setItems(int arrayId, OnDialogItemClickListener listener) {
      String[] items = context.getResources().getStringArray(arrayId);
      return setItems(items, listener);
   }

   public interface OnDialogItemClickListener {
      public void onDialogItemClick(Context context, DialogBulder builder, Dialog dialog, int position);
   }
}
