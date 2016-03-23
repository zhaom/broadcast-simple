package com.babeeta.hudee.core.router.network.pool;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.rmi.ConnectException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.hudee.common.thread.TrackingThreadPool;

/**
 * <p>
 * ChannelFactory的链接池实现
 * </p>
 * <p>
 * 所有借出的Channel都被加了代理以遍将close操作拦截为入池操作
 * </p>
 * 
 * @author leon
 */
public class PooledChannelFactory extends GenericKeyedObjectPool implements
        ChannelFactory, PooledChannelFactoryMBean {

	private static final Logger logger = LoggerFactory
	        .getLogger(PooledChannelFactory.class);

	private static final int DEFAULT_MAX_CONNECTION = Runtime.getRuntime()
	        .availableProcessors() * 4;
	private final KeyedPoolableChannelFactory factory;
	/**
	 * listener for handle event of channel close, replace close with return
	 * channel to pool
	 */
	public final ChannelFutureListener CHANNEL_CLOSE_LISTENER = new ChannelFutureListener() {
		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			Channel channel = future.getChannel();
			logger.warn("[{}]Channel closed outside pool.", channel.getId());
			PooledChannelFactory.this.invalidateObject(
			        channel.getRemoteAddress(), channel);
			channel.getCloseFuture().removeListener(this);
		}
	};

	/**
	 * Constructor
	 * 
	 * @param bossExecutorService
	 *            executor
	 * @param workerExecutorService
	 *            executor
	 */
	public PooledChannelFactory(TrackingThreadPool bossExecutorService,
	        TrackingThreadPool workerExecutorService) {
		this(bossExecutorService, workerExecutorService,
		        DEFAULT_MAX_CONNECTION);
	}

	private int getMaxIdle(int maxConnection) {
		if (maxConnection > DEFAULT_MAX_CONNECTION) {
			return maxConnection / 4;
		}
		return DEFAULT_MAX_CONNECTION;
	}

	/**
	 * Constructor
	 * 
	 * @param bossExecutorService
	 *            executor
	 * @param workerExecutorService
	 *            executor
	 * @param maxConnection
	 *            max connection limit
	 */
	public PooledChannelFactory(TrackingThreadPool bossExecutorService,
	        TrackingThreadPool workerExecutorService, int maxConnection) {
		super();
		factory = new KeyedPoolableChannelFactory(
		        bossExecutorService,
		        workerExecutorService,
		        maxConnection);
		this.setFactory(factory);
		this.setTestOnBorrow(true);
		this.setTestOnReturn(false);
		this.setMaxTotal(maxConnection);
		this.setMaxIdle(getMaxIdle(maxConnection));
		this.setMaxActive(maxConnection);
		this.setTimeBetweenEvictionRunsMillis(30000L);
		this.setTestWhileIdle(true);
	}

	@Override
	public Object borrowObject(final Object key) throws Exception {
		logger.debug("Get channel by [{}].", key.toString());
		final Object result = super.borrowObject(key);
		Channel channel = (Channel) result;
		logger.debug("[{}]BorrowObject", channel.getId());
		if (result == null) {
			return null;
		} else {
			channel.getCloseFuture().addListener(
			        CHANNEL_CLOSE_LISTENER);
			return result;
		}
	}

	@Override
	public Channel getChannel(InetSocketAddress inetSocketAddress)
	        throws TimeoutException, IOException {
		try {
			return (Channel) this.borrowObject(inetSocketAddress);
		} catch (Exception e) {
			throw new ConnectException("Failed to obtain channel to "
			        + inetSocketAddress.toString(), e);
		}
	}

	@Override
	public int getNumActive(String host) {
		return this.getNumActive(new InetSocketAddress(host, 5757));
	}

	@Override
	public void returnChannel(Channel channel) {
		logger.debug("[{}]Returning channel back to pool.", channel.getId());
		try {
			channel.getCloseFuture().removeListener(CHANNEL_CLOSE_LISTENER);
			this.returnObject(channel.getRemoteAddress(), channel);
		} catch (Exception e) {
			logger.error("Error while returning channel back to pool.", e);
			if (channel != null && channel.isConnected()) {
				channel.close();
			}
		}
	}

	@Override
	public void shutdown() throws Exception {
		factory.shutdown();
		close();
	}
}
