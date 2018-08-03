package com.rtm.location.entity;

/**
 * 接收结果回调模式：SERVER_OUTPUT 定位结果采用定位服务器时时输出的结果，回掉模式将不存在规定的时间间隔，结果给出之后发送给回调接口；
 * DATA_RECYCLE 定位结果采用时间间隔回调，也就是每隔x秒(默认1秒)给一次回调
 * 
 * @author dingtao
 *
 */
public enum ReceiveLocationMode {
	SERVER_OUTPUT, DATA_RECYCLE
}
