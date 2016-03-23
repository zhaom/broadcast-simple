package com.babeeta.hudee.gateway.device.tunnel;

import java.net.InetSocketAddress;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.ChannelGroupFutureListener;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.execution.MemoryAwareThreadPoolExecutor;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.hudee.common.mbean.MBeanManager;
import com.babeeta.hudee.common.thread.NamedThreadFactory;
import com.babeeta.hudee.common.thread.TrackingThreadPool;
import com.babeeta.hudee.gateway.device.ServerContext;
import com.babeeta.hudee.gateway.device.monitor.DeviceGatewayMonitorJMX;

/**
 * Tunnel Service
 */
public class TunnelService {
	private static final Logger logger = LoggerFactory
	        .getLogger(TunnelService.class);

	private static final int DEFAULT_PORT_FOR_NET = 5757;
	private static final int DEFAULT_PORT_FOR_WAP = 443;
	private TrackingThreadPool executorService;

	private static void startMonitor() {
		DeviceGatewayMonitorJMX tunnel = new DeviceGatewayMonitorJMX();
		MBeanManager.registerMBean(
		        "TunnelService."
		                + DeviceGatewayMonitorJMX.class.getSimpleName()
		                + ":name=DeviceGatewayMonitorJMX", tunnel);
	}

	private final ServerContext serverContext;

	private final NioServerSocketChannelFactory nioServerSocketChannelFactory;
	private Channel serverChannel;
	private Channel serverChannel443;
	private static ChannelGroup channelGroup = new DefaultChannelGroup();

	/**
	 * @return the current size of channel pool
	 */
	public static int getTunnelCount() {
		return channelGroup.size();
	}

	private final TunnelLetFactory tunnelLetFactory;
	private final MemoryAwareThreadPoolExecutor executor;

	/**
	 * Constructor
	 * 
	 * @param serverContext
	 *            context included initiative parameters
	 */
	public TunnelService(ServerContext serverContext) {
		super();
		this.serverContext = serverContext;

		executorService =
		        new TrackingThreadPool(0,
		                Integer.MAX_VALUE,
		                60L, TimeUnit.SECONDS,
		                new SynchronousQueue<Runnable>(),
		                new NamedThreadFactory("TunnelListen", true,
		                        Thread.MAX_PRIORITY));

		nioServerSocketChannelFactory = new NioServerSocketChannelFactory(
		        executorService, executorService,
		        serverContext.getListenWorkerCount());
		this.tunnelLetFactory = new DefaultTunnelLetFactory(this.serverContext);
		executor = new OrderedMemoryAwareThreadPoolExecutor(
		        serverContext.getListenWorkerCount() * 4,
		        0, 0, 310, TimeUnit.SECONDS);
	}

	/**
	 * shutdown tunnel service
	 */
	public void shutdown() {
		serverChannel.close().awaitUninterruptibly();
		serverChannel443.close().awaitUninterruptibly();
		channelGroup.write(ChannelBuffers.EMPTY_BUFFER).addListener(
		        new ChannelGroupFutureListener() {
			        @Override
			        public void operationComplete(ChannelGroupFuture future)
			                throws Exception {
				        future.getGroup().close();
			        }
		        });
		nioServerSocketChannelFactory.releaseExternalResources();
		executorService.shutdownNow();
		executor.shutdownNow();
	}

	/**
	 * tunnel service startup
	 */
	public void start() {
		ServerBootstrap serverBootstrap = new ServerBootstrap(
		        nioServerSocketChannelFactory);
		// serverBootstrap.setOption("tcpNoDelay", true);
		serverBootstrap.setOption("child.tcpNoDelay", true);
		serverBootstrap.setOption("child.keepAlive", true);// TODO: verify!
		serverBootstrap.setPipelineFactory(
		        new TunnelPipelineFactory(channelGroup, tunnelLetFactory,
		                executor));

		serverChannel = serverBootstrap.bind(
		        new InetSocketAddress(DEFAULT_PORT_FOR_NET));
		serverChannel443 = serverBootstrap.bind(
		        new InetSocketAddress(DEFAULT_PORT_FOR_WAP));

		logger.info("Tunnel server is serving on {} and 443",
		        serverChannel.getLocalAddress());

		startMonitor();
	}
}