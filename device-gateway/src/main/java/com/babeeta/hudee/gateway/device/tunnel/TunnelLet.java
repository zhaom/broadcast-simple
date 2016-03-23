package com.babeeta.hudee.gateway.device.tunnel;

import com.google.protobuf.MessageLite;

/**
 * Interface declaration of tunnel let
 * 
 * @param <T>
 *            message object extends from message list of protocol buffer
 */
public interface TunnelLet<T extends MessageLite> {

	/**
	 * 处理Tunnel中收到的消息
	 * 
	 * @param tunnelContext
	 *            tunnel context
	 * 
	 * @param data
	 *            tunnel data
	 */
	void messageReceived(
	        TunnelContext tunnelContext,
	        TunnelData<T> data);

	/**
	 * 系统停止
	 * 
	 * @throws InterruptedException
	 */
	void shutdown() throws InterruptedException;
}
