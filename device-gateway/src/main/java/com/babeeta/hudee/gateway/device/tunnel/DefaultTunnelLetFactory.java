package com.babeeta.hudee.gateway.device.tunnel;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.hudee.gateway.device.ServerContext;
import com.babeeta.hudee.gateway.device.service.DeviceAccountService;
import com.babeeta.hudee.gateway.device.service.MessageService;
import com.babeeta.hudee.gateway.device.service.SubscriptionService;
import com.babeeta.hudee.gateway.device.tunnel.let.AcknowledgementTunnelLet;
import com.babeeta.hudee.gateway.device.tunnel.let.AuthTunnelLet;
import com.babeeta.hudee.gateway.device.tunnel.let.DeviceRegisterTunnelLet;
import com.babeeta.hudee.gateway.device.tunnel.let.HeartbeatInitTunnelLet;
import com.babeeta.hudee.gateway.device.tunnel.let.HeartbeatTunnelLet;
import com.babeeta.hudee.gateway.device.tunnel.let.ReserveConfirmTunnelLet;
import com.babeeta.hudee.gateway.device.tunnel.let.ServiceBindTunnelLet;
import com.babeeta.hudee.gateway.device.tunnel.let.ServiceUnbindTunnelLet;
import com.google.protobuf.MessageLite;

/**
 * tunnel let factory
 * 
 */
public class DefaultTunnelLetFactory implements TunnelLetFactory {

	private static final Logger logger = LoggerFactory
	        .getLogger(DefaultTunnelLetFactory.class);

	private final ConcurrentHashMap<Integer, TunnelLet<? extends MessageLite>> tunnelLetMap = new ConcurrentHashMap<Integer, TunnelLet<? extends MessageLite>>();
	private final SubscriptionService subscriptionService;
	private final MessageService messageService;
	private final DeviceAccountService deviceAccountService;

	/**
	 * Constructor
	 * 
	 * @param serverContext
	 *            server context, included initiative parameters
	 */
	public DefaultTunnelLetFactory(ServerContext serverContext) {
		// init service
		subscriptionService = new SubscriptionService(
		        serverContext.getSubscriptionWorker(),
		        serverContext.getSubscriptionServiceHost());
		messageService = new MessageService(serverContext.getMessageWorker(),
		        serverContext.getMessageServiceHost());
		deviceAccountService = new DeviceAccountService(
		        serverContext.getDeviceAccountWorker(),
		        serverContext.getDeviceAccountServiceHost());

		tunnelLetMap.put(0, new HeartbeatInitTunnelLet(serverContext));
		tunnelLetMap.put(1, new HeartbeatTunnelLet(serverContext));
		tunnelLetMap.put(131, new DeviceRegisterTunnelLet(serverContext,
		        deviceAccountService));
		// tunnelLetMap.put(129, new MessageTunnelLet());
		tunnelLetMap.put(133, new ServiceBindTunnelLet(serverContext,
		        subscriptionService));
		tunnelLetMap.put(134, new ServiceUnbindTunnelLet(serverContext,
		        subscriptionService));
		tunnelLetMap.put(132, new AuthTunnelLet(serverContext,
		        deviceAccountService, messageService));
		tunnelLetMap.put(130, new AcknowledgementTunnelLet(serverContext,
		        messageService));
		tunnelLetMap.put(135, new ReserveConfirmTunnelLet(serverContext));
	}

	@Override
	public TunnelLet<? extends MessageLite> getTunnelLet(int cmd) {
		return tunnelLetMap.get(cmd);
	}

	@Override
	public void shutdown() {
		for (TunnelLet<?> tunnelLet : tunnelLetMap.values()) {
			try {
				tunnelLet.shutdown();
			} catch (InterruptedException e) {
				logger.warn(e.toString());
			}
		}
		subscriptionService.destroy();
		messageService.destroy();
		deviceAccountService.destroy();
	}

}
