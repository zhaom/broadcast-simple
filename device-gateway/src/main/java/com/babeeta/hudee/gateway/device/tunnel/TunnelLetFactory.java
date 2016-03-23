package com.babeeta.hudee.gateway.device.tunnel;

import com.google.protobuf.MessageLite;

/**
 * Interface declaration of tunnel let factory
 * 
 */
public interface TunnelLetFactory {

	/**
	 * Get tunnel let by enumeration value of message object
	 * 
	 * @param cmd
	 *            The enumeration value of message object
	 * 
	 * @return tunnel let for message object extends from message list of
	 *         protocol buffer
	 */
	TunnelLet<? extends MessageLite> getTunnelLet(int cmd);

	/**
	 * shutdown
	 */
	void shutdown();
}
