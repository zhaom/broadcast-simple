package com.babeeta.hudee.test.parallel;

import java.net.InetSocketAddress;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.ChannelGroupFutureListener;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadRunner {
	private static final Logger logger = LoggerFactory
			.getLogger(LoadRunner.class.getName());
	private static LoadRunner instance;
	private ExecutorService loadRunnerExecutorService;
	private InetSocketAddress address;

	public static final ChannelGroup CHANNEL_GROUP = new DefaultChannelGroup();
	private AtomicLong CLIENT_COUNTER = new AtomicLong(0L);
	private AtomicLong CREATE_CLIENT_SUCCESS_COUNTER = new AtomicLong(0L);
	private AtomicLong CREATE_CLIENT_FAILED_COUNTER = new AtomicLong(0L);
	private AtomicLong TUNNEL_ERROR_COUNTER = new AtomicLong(0L);
	private AtomicLong AUTH_FAILED_COUNTER = new AtomicLong(0L);

	private AtomicLong TUNNEL_ERROR_TIME_SUM = new AtomicLong(0L);
	private AtomicLong TUNNEL_TIME_SUM = new AtomicLong(0L);

	private DeviceClientFactory deviceClientFactory = new DeviceClientFactory(
			CHANNEL_GROUP, this.TUNNEL_ERROR_COUNTER, this.TUNNEL_TIME_SUM,
			this.TUNNEL_ERROR_TIME_SUM);

	private Timer keepTimer = new Timer(true);
	private long vUser;
	private long total;
	private long keepInMinutes;

	public static LoadRunner getInstance() {
		if (instance == null) {
			instance = new LoadRunner();
		}
		return instance;
	}

	private void printClientInfo() {
		logger.info("{} client created, failed {}.",
				Long.valueOf(this.CLIENT_COUNTER.get()),
				Long.valueOf(this.CREATE_CLIENT_FAILED_COUNTER.get()));
	}

	private void printCreateClientFailed(String reason) {
		logger.error("Connect failed caused : " + reason + " [{}]",
				Long.valueOf(this.CREATE_CLIENT_FAILED_COUNTER.get()));
	}

	private void printReport() {
		logger.error("test finish : created client : "
				+ this.CREATE_CLIENT_SUCCESS_COUNTER.get()
				+ " | create failed: "
				+ this.CREATE_CLIENT_FAILED_COUNTER.get()
				+ " | failed after connected: "
				+ this.TUNNEL_ERROR_COUNTER.get() + " | auth failed: "
				+ this.AUTH_FAILED_COUNTER.get() + " | message received: "
				+ ListenState.MESSAGE_RECEIVED_COUNTER.get()
				+ " | tunnel sum time: " + this.TUNNEL_TIME_SUM.get()
				+ " | exception ended tunnel total time: "
				+ this.TUNNEL_ERROR_TIME_SUM.get());
	}

	public void authFailedHandler(String reason) {
		this.AUTH_FAILED_COUNTER.getAndIncrement();
		logger.error("auth failed : " + reason + " [{}]",
				Long.valueOf(this.AUTH_FAILED_COUNTER.get()));
	}

	public void startup(ParrallContext loadContext) {
		logger.info("request to {}:{} started", loadContext.host,
				loadContext.port);
		logger.info("Virtual User {}, ",
				new Object[] { Long.valueOf(loadContext.vUser) });

		this.vUser = loadContext.vUser;
		this.total = loadContext.total;
		this.keepInMinutes = loadContext.keepInMinutes;
		this.loadRunnerExecutorService = Executors
				.newFixedThreadPool((int) loadContext.vUser);
		this.address = new InetSocketAddress(loadContext.host, loadContext.port);
		for (int i = 0; i < this.total; i++) {
			try {
				if (CHANNEL_GROUP.size() < this.vUser) {
					createClient();
				} else {
					--i;
					
				}
				Thread.sleep(10L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	private void createClient() {
		this.loadRunnerExecutorService.execute(new Runnable() {
			public void run() {
				deviceClientFactory.newClient().connect(address)
						.addListener(new ChannelFutureListener() {
							public void operationComplete(ChannelFuture future)
									throws Exception {
								createClientResultHandler(future);
							}
						});
			}
		});

	}

	private void createClientResultHandler(ChannelFuture future) {
		if (future.isDone()) {
			this.CLIENT_COUNTER.getAndIncrement();
			if (!future.isSuccess()) {
				this.CREATE_CLIENT_FAILED_COUNTER.getAndIncrement();
				printCreateClientFailed(future.getCause().toString());
			} else {
				this.CREATE_CLIENT_SUCCESS_COUNTER.getAndIncrement();
			}
			if (this.CLIENT_COUNTER.get() % 100L == 0L) {
				printClientInfo();
			}
			if (this.CLIENT_COUNTER.get() == this.total) {
				logger.info("all virtual user created. start keep stage.");

				this.keepTimer.schedule(new TimerTask() {
					public void run() {
						LoadRunner.logger.info("shutdown......");
						LoadRunner.CHANNEL_GROUP.close().awaitUninterruptibly()
								.addListener(new ChannelGroupFutureListener() {
									public void operationComplete(
											ChannelGroupFuture future)
											throws Exception {
										printReport();
										System.exit(0);
									}
								});
					}
				}, this.keepInMinutes * 60 * 1000);
			}
		}
	}
}