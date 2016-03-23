package com.babeeta.hudee.test.capacity;

import java.net.InetSocketAddress;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.DefaultChannelPipeline;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.protobuf.ProtobufEncoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.hudee.common.thread.NamedThreadFactory;
import com.babeeta.hudee.common.thread.TrackingThreadPool;

@SuppressWarnings("javadoc")
public class DeviceGatewayCapacityTest {
	static class ClientPipelineFactory implements ChannelPipelineFactory {
		private static final DummyHandler DUMMY_HANDLER = new DummyHandler();
		private static final ProtobufEncoder PROTOBUF_ENCODER = new ProtobufEncoder();
		private static final ProtobufVarint32LengthFieldPrepender LENGTH_FIELD_PREPENDER = new ProtobufVarint32LengthFieldPrepender();
		final ChannelPipeline pipeLine = new DefaultChannelPipeline();

		private ClientPipelineFactory() {
		}

		@Override
		public ChannelPipeline getPipeline() throws Exception {
			ChannelPipeline pipeLine = new DefaultChannelPipeline();
			pipeLine.addLast("frameEncoder", LENGTH_FIELD_PREPENDER);
			pipeLine.addLast("protobufEncoder", PROTOBUF_ENCODER);
			pipeLine.addLast("dummy", DUMMY_HANDLER);
			return pipeLine;
		}

	}

	@Sharable
	static class DummyHandler implements ChannelUpstreamHandler {
		@Override
		public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e)
		        throws Exception {
			if (e instanceof ExceptionEvent) {
				logger.error("Error occured:", ((ExceptionEvent) e).getCause());
				e.getChannel().close();
			}
		}

	}

	private static final Logger logger = LoggerFactory
	        .getLogger(DeviceGatewayCapacityTest.class);

	private final TrackingThreadPool bossExecutorService;
	private final TrackingThreadPool workerExecutorService;
	private final NioClientSocketChannelFactory nioClientSocketChannelFactory;
	private final ClientPipelineFactory clientPipelineFactory = new ClientPipelineFactory();
	private final ChannelGroup channelGroup = new DefaultChannelGroup();

	public DeviceGatewayCapacityTest(int maxConnection) {
		super();
		workerExecutorService =
		        new TrackingThreadPool(maxConnection,
		                maxConnection, 0L, TimeUnit.MILLISECONDS,
		                new LinkedBlockingQueue<Runnable>(),
		                new NamedThreadFactory("NettyClientWorker", true,
		                        Thread.MAX_PRIORITY - 1));

		bossExecutorService =
		        new TrackingThreadPool(0,
		                Integer.MAX_VALUE,
		                60L, TimeUnit.SECONDS,
		                new SynchronousQueue<Runnable>(),
		                new NamedThreadFactory("NettyClientBoss", true,
		                        Thread.MAX_PRIORITY));
		this.nioClientSocketChannelFactory = new NioClientSocketChannelFactory(
		        this.bossExecutorService, this.workerExecutorService,
		        maxConnection);
	}

	public synchronized long getConnectionCount() {
		return channelGroup.size();
	}

	public long startup(String ip, int port, long connectionCount)
	        throws InterruptedException {
		long start = System.currentTimeMillis();

		for (long i = 0; i < connectionCount; i++) {
			ChannelFuture channelFuture = getClientBootstrap().connect(
			        new InetSocketAddress(ip, port)).await();

			if (channelFuture.isSuccess()) {
				channelGroup.add(channelFuture.getChannel());
				if (getConnectionCount() % 1000 == 0) {
					System.out.println(System.currentTimeMillis() - start
					        + " Connected : " + getConnectionCount());
				}
				Channel ch = channelFuture.getChannel();
				startupClient(ch);
			} else if (channelFuture.getCause() != null) {
				logger.error("Ops, something wrong...",
				        channelFuture.getCause());
				break;
			} else {
				logger.error("Ops, something wrong...");
				break;
			}
		}

		return System.currentTimeMillis() - start;
	}

	public void shutdown() {
		nioClientSocketChannelFactory.releaseExternalResources();
	}

	private ClientBootstrap getClientBootstrap() {
		ClientBootstrap clientBootstrap = new ClientBootstrap();
		clientBootstrap.setFactory(nioClientSocketChannelFactory);
		clientBootstrap.setPipelineFactory(clientPipelineFactory);
		clientBootstrap.setOption("keepAlive", true);
		clientBootstrap.setOption("tcpNoDelay", true);
		return clientBootstrap;
	}

	private void startupClient(Channel ch) {

	}
}
