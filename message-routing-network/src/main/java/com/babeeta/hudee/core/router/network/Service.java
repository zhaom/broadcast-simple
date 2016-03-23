package com.babeeta.hudee.core.router.network;

import java.net.Inet6Address;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.DefaultChannelPipeline;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.protobuf.ProtobufDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.hudee.common.mbean.MBeanManager;
import com.babeeta.hudee.common.thread.NamedThreadFactory;
import com.babeeta.hudee.common.thread.ThreadPoolStatus;
import com.babeeta.hudee.common.thread.TrackingThreadPool;
import com.babeeta.hudee.core.MessageHandler;
import com.babeeta.hudee.core.MessageRouting;
import com.babeeta.hudee.core.router.network.dns.DNSClient;
import com.babeeta.hudee.core.router.network.dns.DNSClientDefaultImpl;
import com.babeeta.hudee.core.router.network.monitor.Network;

/**
 * <p>
 * 路由器的网络通讯层Server实现
 * </p>
 * <p>
 * 默认使用SRV记录来启动服务
 * </p>
 * 
 * @author leon
 */
public class Service {
	private static final Logger logger = LoggerFactory.getLogger(Service.class);
	/**
	 * for IPv4
	 */
	private static final String DEFAULT_BIND_IPV4_ADDRESS = "0.0.0.0";
	/**
	 * for IPv6
	 */
	private static final String DEFAULT_BIND_IPV6_ADDRESS = "0:0:0:0:0:0:0:0";

	private static final AtomicInteger counter = new AtomicInteger(0);

	private final DNSClient dnsClient;

	private NioServerSocketChannelFactory nioServerSocketChannelFactory;
	private Channel bossChannel;

	private final RequestHandler requestHandler;
	private final DataDumpHandler dataDumpHandler;

	private TrackingThreadPool nioServerWorkerExecutorService;
	private TrackingThreadPool nioServerBossExecutorService;

	/**
	 * @param messageHandler
	 *            message handler
	 */
	public Service(MessageHandler messageHandler) {
		this("NoName", new DNSClientDefaultImpl(), messageHandler, 32);
	}

	/**
	 * @param messageHandler
	 *            message handler executor service
	 * @param ioWorkerCount
	 *            io worker count
	 */
	public Service(MessageHandler messageHandler, int ioWorkerCount) {
		this("NoName", new DNSClientDefaultImpl(), messageHandler,
		        ioWorkerCount);
	}

	/**
	 * @param serviceName
	 *            Service name
	 * @param messageHandler
	 *            message handler executor service
	 * @param ioWorkerCount
	 *            io worker count
	 */
	public Service(String serviceName, MessageHandler messageHandler,
	        int ioWorkerCount) {
		this(serviceName, new DNSClientDefaultImpl(), messageHandler,
		        ioWorkerCount);
	}

	Service(String serviceName, DNSClient dnsClient,
	        MessageHandler messageHandler,
	        int ioWorkerCount) {
		this.dnsClient = dnsClient;
		this.dataDumpHandler = new DataDumpHandler();
		this.requestHandler = new RequestHandler(messageHandler);

		nioServerBossExecutorService =
		        new TrackingThreadPool(0,
		                Integer.MAX_VALUE,
		                60L, TimeUnit.SECONDS,
		                new SynchronousQueue<Runnable>(),
		                new NamedThreadFactory("NettyServerBoss", true,
		                        Thread.MAX_PRIORITY));
		nioServerWorkerExecutorService =
		        new TrackingThreadPool(ioWorkerCount,
		                ioWorkerCount, 0L, TimeUnit.MILLISECONDS,
		                new LinkedBlockingQueue<Runnable>(),
		                new NamedThreadFactory("NettyServerWorker", true,
		                        Thread.MAX_PRIORITY - 1));

		nioServerSocketChannelFactory = new NioServerSocketChannelFactory(
		        nioServerBossExecutorService,
		        nioServerWorkerExecutorService, ioWorkerCount);

		MBeanManager.registerMBean(serviceName + ":Server - " + counter.get()
		        + ":name=NioServerWorkerExecutorService",
		        new ThreadPoolStatus("NioServerWorkerExecutorService",
		                nioServerWorkerExecutorService));
	}

	/**
	 * 关闭Server
	 */
	public void shutdownGraceFully() {
		logger.info("Shutting down...");
		bossChannel.close().awaitUninterruptibly();
		requestHandler.getDefaultChannelGroup().close()
		        .awaitUninterruptibly();
		nioServerSocketChannelFactory.releaseExternalResources();
		logger.info("See you next time.");
	}

	/**
	 * 使用参数启动Server
	 * 
	 * @param serviceName
	 *            service domain
	 * 
	 * @throws Exception
	 *             unknown host exception
	 */
	public void start(String serviceName) throws Exception {
		logger.info("bootstrapping...");

		ServerBootstrap serverBootstrap = new ServerBootstrap(
		        nioServerSocketChannelFactory);
		serverBootstrap.setOption("keepAlive", true);
		serverBootstrap.setOption("tcpNoDelay", true);
		serverBootstrap.setPipelineFactory(new NetworkServicePipelineFactory(
		        requestHandler, dataDumpHandler));

		try {
			logger.info("Resoving service name.");
			InetSocketAddress result = dnsClient.resolve(serviceName);
			logger.info("Service: {}:{}", result.getAddress().toString(),
			        result.getPort());
			if (result.getAddress() instanceof Inet6Address) {
				bossChannel = serverBootstrap.bind(new InetSocketAddress(
				        DEFAULT_BIND_IPV6_ADDRESS, result.getPort()));
			} else {
				bossChannel = serverBootstrap.bind(new InetSocketAddress(
				        DEFAULT_BIND_IPV4_ADDRESS, result.getPort()));
			}
		} catch (UnknownHostException e) {
			logger.error("Failed to bind. {}", serviceName);
			throw e;
		}

		logger.info("Router is serving on {}", bossChannel.getLocalAddress());

		new Network(dataDumpHandler);
	}
}

class NetworkServicePipelineFactory implements ChannelPipelineFactory {

	private final RequestHandler requestHandler;
	private final DataDumpHandler dataDumpHandler;
	private final ProtobufDecoder protobufDecoder = new ProtobufDecoder(
	        MessageRouting.Message.getDefaultInstance());

	public NetworkServicePipelineFactory(RequestHandler requestHandler,
	        DataDumpHandler dataDumpHandler) {
		super();
		this.requestHandler = requestHandler;
		this.dataDumpHandler = dataDumpHandler;
	}

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = new DefaultChannelPipeline();
		pipeline.addLast("dump", dataDumpHandler);

		pipeline.addLast("frameDecoder", new ProtobufVarint32FrameDecoder());
		pipeline.addLast("protobuf", protobufDecoder);

		pipeline.addLast("request handler", requestHandler);
		return pipeline;
	}
}