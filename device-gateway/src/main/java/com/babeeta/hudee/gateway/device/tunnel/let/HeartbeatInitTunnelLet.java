package com.babeeta.hudee.gateway.device.tunnel.let;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.hudee.core.MessageRouting;
import com.babeeta.hudee.core.MessageRouting.HeartbeatInit;
import com.babeeta.hudee.core.MessageRouting.HeartbeatResponse;
import com.babeeta.hudee.gateway.device.ServerContext;
import com.babeeta.hudee.gateway.device.tunnel.TunnelContext;
import com.babeeta.hudee.gateway.device.tunnel.TunnelData;
import com.babeeta.hudee.gateway.device.tunnel.TunnelLet;
import com.babeeta.hudee.gateway.device.tunnel.heartbeat.DefaultConfigurationProvider;
import com.babeeta.hudee.gateway.device.tunnel.heartbeat.HeartbeatPolicyFactory;
import com.google.protobuf.MessageLite;

/**
 * Handle "heartbeat init" message from device.
 * 
 */
public class HeartbeatInitTunnelLet implements TunnelLet<HeartbeatInit> {
	private final static Logger logger = LoggerFactory
	        .getLogger(HeartbeatInitTunnelLet.class);
	@SuppressWarnings("unused")
	private final ServerContext serverContext;
	private final HeartbeatPolicyFactory heartbeatPolicyFactory;
	/**
	 * Count the "heartbeat init" message from client for monitor
	 */
	private static AtomicLong heartbeatInitCount = new AtomicLong(0);

	/**
	 * Constructor
	 * 
	 * @param serverContext
	 *            server context, included initiative parameters
	 */
	public HeartbeatInitTunnelLet(ServerContext serverContext) {
		super();
		this.serverContext = serverContext;
		heartbeatPolicyFactory = new HeartbeatPolicyFactory();
		heartbeatPolicyFactory
		        .setConfigurationProvider(new DefaultConfigurationProvider());
	}

	@Override
	public void messageReceived(
	        final TunnelContext tunnelContext,
	        TunnelData<HeartbeatInit> data) {
		heartbeatInitCount.getAndIncrement();
		logger.debug("[{}]HeartbeatInit: e:{}, delay:{}, cause:{}",
		        new Object[] {
		                tunnelContext.getChannel().getId(),
		                data.obj.getLastException(),
		                data.obj.getLastTimeout(),
		                data.obj.getCause() });

		tunnelContext.setCurrentHeartbeatPolicy(
		        heartbeatPolicyFactory.getInstance(
		                data.obj.getLastException().toString(),
		                data.obj.getLastTimeout()));
		int initial =
		        tunnelContext.getCurrentHeartbeatPolicy().getNextInterval();
		logger.debug("[{}]Initial heartbeat:{}", tunnelContext.getChannel()
		        .getId(), initial);

		tunnelContext.getChannel().write(
		        new TunnelData<MessageLite>(data.tag,
		                MessageRouting.MessageCMD.HEARTBEAT_RESPONSE
		                        .getNumber(), HeartbeatResponse
		                        .newBuilder()
		                        .setDelay(initial)
		                        .build()));
	}

	@Override
	public void shutdown() {
		logger.info("Shutdown.");
	}

	/**
	 * Get the value of "heartbeat init" message counter
	 * 
	 * @param reset
	 *            whether reset counter.
	 * @return the value of "heartbeat init" message counter.
	 */
	public synchronized static long getHeartbeatInitCounter(boolean reset) {
		if (reset) {
			return heartbeatInitCount.getAndSet(0);
		} else {
			return heartbeatInitCount.get();
		}
	}
}