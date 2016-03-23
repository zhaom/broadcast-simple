package com.babeeta.hudee.core.router.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.hudee.common.mbean.MBeanManager;
import com.babeeta.hudee.common.thread.NamedThreadFactory;
import com.babeeta.hudee.common.thread.ThreadPoolStatus;
import com.babeeta.hudee.common.thread.TrackingThreadPool;
import com.babeeta.hudee.core.MessageFuture;
import com.babeeta.hudee.core.MessageRouting.Message;
import com.babeeta.hudee.core.router.network.dns.DNSClient;
import com.babeeta.hudee.core.router.network.dns.DNSClientDefaultImpl;
import com.babeeta.hudee.core.router.network.pool.ChannelFactory;
import com.babeeta.hudee.core.router.network.pool.PooledChannelFactory;

/**
 * 消息发送的网络实现
 * 
 * @author leon
 */
public class MessageSenderImpl implements com.babeeta.hudee.core.MessageSender,
        MessageSenderImplMBean {

	private static final Logger logger = LoggerFactory
	        .getLogger(MessageSenderImpl.class);
	private static final String MbeanNamePattern = "MessageRouting: type = network, name = MessageSender + ";

	private static AtomicLong counter = new AtomicLong(0);

	private final ChannelFactory channelFactory;
	private final DNSClient dnsClient;

	private TrackingThreadPool senderExecutorService;
	private TrackingThreadPool nioClientWorkerExecutorService;
	private TrackingThreadPool nioClientBossExecutorService;

	private AtomicLong sentMessageCounter = new AtomicLong(0);
	private AtomicLong sentSuccessCounter = new AtomicLong(0);
	private AtomicLong sentFailedCounter = new AtomicLong(0);
	private AtomicLong sentErrorDNSCounter = new AtomicLong(0);
	private AtomicLong sentErrorTimeoutCounter = new AtomicLong(0);
	private AtomicLong sentErrorIOCounter = new AtomicLong(0);

	/**
	 * Constructor
	 */
	public MessageSenderImpl() {
		this("NoName", new DNSClientDefaultImpl(), Runtime.getRuntime()
		        .availableProcessors() * 4);
	}

	/**
	 * Constructor
	 * 
	 * @param maxConnection
	 *            max of connection
	 */
	public MessageSenderImpl(int maxConnection) {
		this("NoName", new DNSClientDefaultImpl(), maxConnection);
	}

	/**
	 * Constructor
	 * 
	 * @param name
	 *            message sender name
	 * 
	 * @param maxConnection
	 *            max of connection
	 */
	public MessageSenderImpl(String name, int maxConnection) {
		this(name, new DNSClientDefaultImpl(), maxConnection);
	}

	MessageSenderImpl(String name, DNSClient dnsClient, int maxConnection) {
		super();
		this.dnsClient = dnsClient;

		senderExecutorService = new TrackingThreadPool(maxConnection,
		        maxConnection, 0L, TimeUnit.MILLISECONDS,
		        new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory(
		                "MessageSender", true));

		nioClientWorkerExecutorService =
		        new TrackingThreadPool(maxConnection,
		                maxConnection, 0L, TimeUnit.MILLISECONDS,
		                new LinkedBlockingQueue<Runnable>(),
		                new NamedThreadFactory("NettyClientWorker", true,
		                        Thread.MAX_PRIORITY - 1));

		nioClientBossExecutorService =
		        new TrackingThreadPool(0,
		                Integer.MAX_VALUE,
		                60L, TimeUnit.SECONDS,
		                new SynchronousQueue<Runnable>(),
		                new NamedThreadFactory("NettyClientBoss", true,
		                        Thread.MAX_PRIORITY));

		channelFactory = new PooledChannelFactory(
		        nioClientBossExecutorService,
		        nioClientWorkerExecutorService, maxConnection);

		String beanName = name + "-" + MbeanNamePattern
		        + counter.incrementAndGet();

		MBeanManager.registerMBean("MessageSender - " + counter.get()
		        + ":name=PooledChannelFactory",
		        channelFactory);
		MBeanManager.registerMBean(beanName, this);

		MBeanManager.registerMBean("MessageSender - " + counter.get()
		        + ":name=SenderExecutorService",
		        new ThreadPoolStatus("SenderExecutorService",
		                senderExecutorService));

		MBeanManager.registerMBean("MessageSender - " + counter.get()
		        + ":name=NioClientWorkerExecutorService",
		        new ThreadPoolStatus("NioClientWorkerExecutorService",
		                nioClientWorkerExecutorService));
	}

	@Override
	public MessageFuture send(final Message message) {
		sentMessageCounter.getAndIncrement();
		final MessageFutureImpl future = new MessageFutureImpl();
		senderExecutorService.execute(new Runnable() {
			@Override
			public void run() {
				String domain = message.getTo().substring(
				        message.getTo().indexOf("@") + 1,
				        message.getTo().length());
				Channel channel = null;
				InetSocketAddress address = null;
				try {
					address = new InetSocketAddress(domain,
					        dnsClient.resolve(domain).getPort());

					channel = channelFactory.getChannel(address);
				} catch (TimeoutException e) {
					sentErrorTimeoutCounter.getAndIncrement();
					logger.error("Get channel failed! TimeoutException:{}",
					        e.getMessage());
					e.printStackTrace();
				} catch (UnknownHostException e) {
					sentErrorDNSCounter.getAndIncrement();
					logger.error(
					        "[FATAL ERROR] Can not reslove domain {}. Please check DNS and Nginx. UnknownHostException: {}",
					        domain, e.getMessage());
					e.printStackTrace();
				} catch (IOException e) {
					sentErrorIOCounter.getAndIncrement();
					logger.error("Get channel failed! IOException:{}",
					        e.getMessage());
					e.printStackTrace();
				}
				logger.debug("Channel check {}", channel);
				if (channel != null) {
					ChannelFuture channelFuture = channel.write(message);
					future.setChannelFuture(channelFuture);
					channelFuture.addListener(new ChannelFutureListener() {
						@Override
						public void operationComplete(ChannelFuture future)
						        throws Exception {
							if (future.isSuccess()) {
								sentSuccessCounter.getAndIncrement();
								logger.debug("[{}] sent succeed.",
								        message.getUid());
							} else {
								sentFailedCounter.getAndIncrement();
								logger.debug("[{}] sent fail.",
								        message.getUid());
							}
						}
					});
					channelFactory.returnChannel(channel);
				}
			}
		});
		return future;
	}

	/**
	 * destroy message sender instance
	 * 
	 * @throws Exception
	 *             exception happened during destroy
	 */
	public void destroy() throws Exception {
		channelFactory.shutdown();
		senderExecutorService.shutdownNow();
	}

	@Override
	public long getSentMessageCount() {
		return sentMessageCounter.get();
	}

	@Override
	public long resetSentMessageCount() {
		return sentMessageCounter.getAndSet(0);
	}

	@Override
	public long getSentSuccessCount() {
		return sentSuccessCounter.get();
	}

	@Override
	public long resetSentSuccessCount() {
		return sentSuccessCounter.getAndSet(0);
	}

	@Override
	public long getSentErrorDNSCount() {
		return sentErrorDNSCounter.get();
	}

	@Override
	public long resetSentErrorDNSCount() {
		return sentErrorDNSCounter.getAndSet(0);
	}

	@Override
	public long getSentErrorTimeoutCount() {
		return sentErrorTimeoutCounter.get();
	}

	@Override
	public long resetSentErrorTimeoutCount() {
		return sentErrorTimeoutCounter.getAndSet(0);
	}

	@Override
	public long getSentFailedCount() {
		return sentFailedCounter.get();
	}

	@Override
	public long resetSentFailedCount() {
		return sentFailedCounter.getAndSet(0);
	}

	@Override
	public long getSentErrorIOCount() {
		return sentErrorIOCounter.get();
	}

	@Override
	public long resetSentErrorIOCount() {
		return sentErrorIOCounter.getAndSet(0);
	}
}