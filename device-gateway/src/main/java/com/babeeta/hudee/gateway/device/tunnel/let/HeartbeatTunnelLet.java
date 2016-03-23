package com.babeeta.hudee.gateway.device.tunnel.let;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.hudee.core.MessageRouting;
import com.babeeta.hudee.core.MessageRouting.Heartbeat;
import com.babeeta.hudee.core.MessageRouting.HeartbeatResponse;
import com.babeeta.hudee.gateway.device.ServerContext;
import com.babeeta.hudee.gateway.device.tunnel.TunnelContext;
import com.babeeta.hudee.gateway.device.tunnel.TunnelData;
import com.babeeta.hudee.gateway.device.tunnel.TunnelLet;
import com.google.protobuf.MessageLite;

/**
 * Handle "heartbeat" message from device.
 * 
 */
public class HeartbeatTunnelLet implements TunnelLet<Heartbeat> {
	private final static Logger logger = LoggerFactory
	        .getLogger(HeartbeatTunnelLet.class);
	@SuppressWarnings("unused")
	private final ServerContext serverContext;
	/**
	 * Count the "heartbeat" message from client for monitor
	 */
	private static AtomicLong heartbeatCount = new AtomicLong(0);

	/**
	 * Constructor
	 * 
	 * @param serverContext
	 *            server context, included initiative parameters
	 */
	public HeartbeatTunnelLet(ServerContext serverContext) {
		super();
		this.serverContext = serverContext;
	}

	@Override
	public void messageReceived(
	        TunnelContext tunnelContext,
	        TunnelData<Heartbeat> data) {
		heartbeatCount.getAndIncrement();
		tunnelContext.setCurrentHeartbeatPolicy(tunnelContext
		        .getCurrentHeartbeatPolicy().getNextIntervalPolicy());
		int delay =
		        tunnelContext.getCurrentHeartbeatPolicy().getNextInterval();

		logger.debug("[{}] new heartbeat = {}", tunnelContext.getChannel()
		        .getId(), delay);
		tunnelContext.getChannel().write(
		        new TunnelData<MessageLite>(data.tag,
		                MessageRouting.MessageCMD.HEARTBEAT_RESPONSE
		                        .getNumber(), HeartbeatResponse
		                        .newBuilder()
		                        .setDelay(delay)
		                        .build()));
	}

	@Override
	public void shutdown() throws InterruptedException {
		logger.info("Shuting down...");
		logger.info("Shut down.");
	}

	/**
	 * Get the value of "heartbeat" message counter
	 * 
	 * @param reset
	 *            whether reset counter.
	 * @return the value of "heartbeat" message counter.
	 */
	public synchronized static long getHeartbeatCounter(boolean reset) {
		if (reset) {
			return heartbeatCount.getAndSet(0);
		} else {
			return heartbeatCount.get();
		}
	}
}
