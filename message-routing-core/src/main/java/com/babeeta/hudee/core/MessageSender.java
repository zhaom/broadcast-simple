package com.babeeta.hudee.core;

/**
 * 消息发送接口
 * 
 * @author leon
 * 
 */
public interface MessageSender {
	/**
	 * @param message
	 *            message would be sent.
	 * @return message future instance
	 */
	MessageFuture send(MessageRouting.Message message);
}
