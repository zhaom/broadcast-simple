package com.babeeta.hudee.core.rpc;

import com.babeeta.hudee.core.MessageHandler;
import com.babeeta.hudee.core.MessageRouting.Message;

/**
 * 远程调用返回处理接口
 * 
 * @author leon
 * 
 */
public interface RPCHandler extends MessageHandler {
	/**
	 * @param message
	 *            message of handler do with
	 * @param t
	 *            exception
	 */
	void exceptionCaught(Message message, Throwable t);
}
