package com.babeeta.hudee.core;

/**
 * 消息监听接口
 * 
 * @author leon
 * 
 */
public interface MessageHandler {
	/**
	 * @param message
	 *            message need handled
	 */
	void onMessage(MessageRouting.Message message);
}
