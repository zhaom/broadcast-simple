package com.babeeta.hudee.gateway.device.tunnel.let;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.hudee.core.MessageRouting.Response;
import com.babeeta.hudee.gateway.device.ServerContext;
import com.babeeta.hudee.gateway.device.tunnel.TunnelContext;
import com.babeeta.hudee.gateway.device.tunnel.TunnelData;
import com.babeeta.hudee.gateway.device.tunnel.TunnelLet;

/**
 * Handle "reserve confirm" message from device.
 * 
 */
public class ReserveConfirmTunnelLet implements TunnelLet<Response> {
	private static final Logger logger = LoggerFactory
	        .getLogger(ReserveConfirmTunnelLet.class);
	@SuppressWarnings("unused")
	private final ServerContext serverContext;

	/**
	 * Constructor
	 * 
	 * @param serverContext
	 *            server context, included initiative parameters
	 */
	public ReserveConfirmTunnelLet(ServerContext serverContext) {
		super();
		this.serverContext = serverContext;
	}

	@Override
	public void messageReceived(
	        final TunnelContext tunnelContext,
	        final TunnelData<Response> data) {
		logger.debug("[{}] Disconnect.", tunnelContext.getChannel().getId());
		tunnelContext.getChannel().close();
	}

	@Override
	public void shutdown() throws InterruptedException {
		logger.info("Shuting down...");
	}
}
