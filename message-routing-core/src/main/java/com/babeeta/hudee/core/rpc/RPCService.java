package com.babeeta.hudee.core.rpc;

import com.babeeta.hudee.core.MessageRouting.Message;

/**
 * 远程异步调用服务
 * 
 * @author leon
 * 
 */
public interface RPCService {
	/**
	 * @param message
	 *            message need handled
	 * @param responseHandler
	 *            the response handler to handle the result of invoke
	 */
	void invoke(Message message, RPCHandler responseHandler);
}
