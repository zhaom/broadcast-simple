package com.babeeta.hudee.core;

/**
 * 操作异步处理结果监听器
 * 
 * @author leon
 * 
 */
public interface MessageFutureListener {
	/**
	 * Method called when action executed completed
	 * 
	 * @param future
	 *            execute result
	 */
	void operationComplete(MessageFuture future);
}
