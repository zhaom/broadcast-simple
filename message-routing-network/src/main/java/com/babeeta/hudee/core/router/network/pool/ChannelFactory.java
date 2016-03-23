package com.babeeta.hudee.core.router.network.pool;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeoutException;

import org.jboss.netty.channel.Channel;

/**
 * Channel的Factory。负责创建到目的地的网络链接
 * 
 * @author leon
 * 
 */
public interface ChannelFactory {
	/**
	 * @param inetSocketAddress
	 * @return a channel borrow from pool
	 * @throws TimeoutException
	 * @throws IOException
	 */
	Channel getChannel(InetSocketAddress inetSocketAddress)
	        throws TimeoutException, IOException;

	/**
	 * @param channel
	 *            the channel will be return to pool
	 */
	void returnChannel(Channel channel);

	/**
	 * @throws Exception
	 *             exception happen during shutdown
	 */
	void shutdown() throws Exception;
}
