package com.rtmap.wifipicker.wifi;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/**
 * ui通信工具类 com.rtm.location.util
 * 
 * @author lixinxin <br/>
 *         create at 2012-8-1 下午21:11:57
 */
public class UIEvent {
   @SuppressWarnings("unused")
   private static final String TAG = "UIEvent";

   /** handler列表，所有需要接收其消息的窗口需要注册到该列表中 **/
   private List<Handler> needToReflashList = null;
   /** UIEvent的静态对象，用于该类的单例实现 **/
   private static UIEvent uiEvent = null;

   /**
    * 得到UIEvent的一个单例
    * 
    * @return UIEvent 单例对象
    */
   public static UIEvent getInstance() {
      if (uiEvent == null) {
         uiEvent = new UIEvent();
      }
      return uiEvent;
   }

   private UIEvent() {
      needToReflashList = new ArrayList<Handler>();
   }

   /**
    * 注册handler对象
    * 
    * @param item
    *           handler对象
    */
   public void register(Handler item) {
      needToReflashList.add(item);
   }
   
   /**
    * 注册handler对象
    * 
    * @param item
    *           handler对象
    */
   public boolean contains(Handler item) {
      return needToReflashList.contains(item);
   }

   /**
    * 注销handler对象
    * 
    * @param item
    *           handler对象
    */
   public void remove(Handler item) {
      needToReflashList.remove(item);
   }

   public void notifications(int flag) {
      notifications(flag, 0, 0, null, null);
   }

   public void notifications(int flag, int arg1) {
      notifications(flag, arg1, 0, null, null);
   }

   public void notifications(int flag, int arg1, int arg2) {
      notifications(flag, arg1, arg2, null, null);
   }

   public void notifications(Message msg) {
      if (needToReflashList == null || needToReflashList.isEmpty()) {
         return;
      } else {
         for (Handler item : needToReflashList) {
            item.sendMessage(msg);
         }
      }
   }

   /**
    * 通知所有注册到UIEvent的ui窗口，通过handler的方式
    * 
    * @param flag
    *           对应msg.what
    * @param arg1
    *           对应msg.arg1
    * @param arg2
    *           对应msg.arg2
    * @param obj
    *           对应msg.obj
    * @param data
    *           对应msg.bundle
    */
   public void notifications(int flag, int arg1, int arg2, Object obj, Bundle data) {
      if (needToReflashList == null || needToReflashList.isEmpty()) {
         return;
      } else {
         for (Handler item : needToReflashList) {
            Message msg = Message.obtain();
            msg.what = flag;
            msg.arg1 = arg1;
            msg.arg2 = arg2;
            msg.obj = obj;
            msg.setData(data);
            item.sendMessage(msg);
         }
      }
   }

   public void notifications(int flag, Bundle data) {
      notifications(flag, 0, 0, null, data);
   }

   public void notifications(int flag, String obj) {
      notifications(flag, 0, 0, obj, null);
   }

   public void notifications(int flag, int arg1, int arg2, Object obj) {
      notifications(flag, arg1, arg2, obj, null);
   }
}