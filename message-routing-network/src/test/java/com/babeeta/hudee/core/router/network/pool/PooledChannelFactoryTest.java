package com.babeeta.hudee.core.router.network.pool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelState;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.babeeta.hudee.common.thread.NamedThreadFactory;
import com.babeeta.hudee.common.thread.TrackingThreadPool;

@SuppressWarnings("javadoc")
public class PooledChannelFactoryTest {

	@Sharable
	private final class DummyHandler implements ChannelUpstreamHandler {
		@Override
		public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e)
		        throws Exception {
			if (e instanceof ChannelStateEvent
			        && ChannelState.CONNECTED == ((ChannelStateEvent) e)
			                .getState()) {
				channelGroup.add(e.getChannel());
			}
		}
	}

	private static final int MAX_CONNECTION = Runtime.getRuntime()
	        .availableProcessors() * 4;

	private final TrackingThreadPool executor = new TrackingThreadPool(0,
	        Integer.MAX_VALUE,
	        60L, TimeUnit.SECONDS,
	        new SynchronousQueue<Runnable>(),
	        new NamedThreadFactory("PooledChannelTestExecutor", true));
	private final TrackingThreadPool mockExecutors = new TrackingThreadPool(0,
	        Integer.MAX_VALUE,
	        60L, TimeUnit.SECONDS,
	        new SynchronousQueue<Runnable>(),
	        new NamedThreadFactory("PooledChannelTestMockExecutor", true));
	private final PooledChannelFactory pooledChannelFactory = new PooledChannelFactory(
	        executor, executor);
	private final ChannelGroup channelGroup = new DefaultChannelGroup();

	private final NioServerSocketChannelFactory nioServerSocketChannelFactory = new NioServerSocketChannelFactory(
	        mockExecutors, mockExecutors, (Runtime.getRuntime()
	                .availableProcessors() < 8 ? 8 : Runtime.getRuntime()
	                .availableProcessors()));

	@Before
	public void setUp() throws Exception {
		ServerBootstrap bootstrap = new ServerBootstrap(
		        nioServerSocketChannelFactory);
		bootstrap.getPipeline().addLast("handler", new DummyHandler());

		channelGroup.add(bootstrap.bind(new InetSocketAddress("localhost",
		        15757)));

	}

	@After
	public void tearDown() throws Exception {
		channelGroup.close().awaitUninterruptibly();
		nioServerSocketChannelFactory.releaseExternalResources();
		pooledChannelFactory.shutdown();
	}

	@Test
	public void testGetChannel() throws TimeoutException, IOException,
	        InterruptedException {
		assertEquals(MAX_CONNECTION, pooledChannelFactory.getMaxTotal());
		assertEquals(MAX_CONNECTION, pooledChannelFactory.getMaxActive());

		final ChannelBuffer buf = ChannelBuffers.wrappedBuffer("Hello, there"
		        .getBytes());
		assertEquals(0, pooledChannelFactory.getNumActive());

		List<Channel> channelList = new ArrayList<Channel>();

		for (int i = 0; i < MAX_CONNECTION; i++) {
			channelList.add(pooledChannelFactory
			        .getChannel(new InetSocketAddress("localhost", 15757)));
		}

		assertEquals(MAX_CONNECTION, pooledChannelFactory.getNumActive());

		for (Channel channel : channelList) {
			channel.write(buf);
		}
		assertEquals(MAX_CONNECTION, pooledChannelFactory.getNumActive());

		for (Channel channel : channelList) {
			pooledChannelFactory.returnChannel(channel);
		}

		Thread.sleep(100L);

		assertEquals(0, pooledChannelFactory.getNumActive());
	}

	@Test
	public void testOnBorrow() throws Exception {
		final Channel channel = (Channel) pooledChannelFactory
		        .borrowObject(new InetSocketAddress("localhost", 15757));
		pooledChannelFactory.returnChannel(channel);

		assertEquals(true, pooledChannelFactory.getTestOnBorrow());
		assertEquals(false, pooledChannelFactory.getTestOnReturn());
		assertEquals(1, pooledChannelFactory.getNumIdle());

		Channel obj = (Channel) pooledChannelFactory
		        .borrowObject(new InetSocketAddress("localhost", 15757));

		assertSame(channel, obj);

		pooledChannelFactory.returnChannel(obj);

	}
}