package com.babeeta.hudee.gateway.device.tunnel.let;

import java.util.concurrent.atomic.AtomicLong;

import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.hudee.core.MessageRouting;
import com.babeeta.hudee.core.MessageRouting.Response;
import com.babeeta.hudee.core.MessageRouting.ServiceBind;
import com.babeeta.hudee.gateway.device.ServerContext;
import com.babeeta.hudee.gateway.device.service.SubscriptionService;
import com.babeeta.hudee.gateway.device.tunnel.TunnelContext;
import com.babeeta.hudee.gateway.device.tunnel.TunnelData;
import com.babeeta.hudee.gateway.device.tunnel.TunnelLet;
import com.google.protobuf.MessageLite;

/**
 * Handle "unbind" message from device.
 * 
 */
public class ServiceUnbindTunnelLet implements TunnelLet<ServiceBind> {
	private static final Logger logger = LoggerFactory
	        .getLogger(ServiceUnbindTunnelLet.class);
	/**
	 * Count the "device unbind" message from client for monitor
	 */
	private static AtomicLong unbindRequestCounter = new AtomicLong(0);

	/**
	 * Count the times of "device unbind" execute successful for monitor
	 */
	private static AtomicLong unbindSuccessCounter = new AtomicLong(0);

	/**
	 * Count the times of service unavailable for monitor
	 */
	private static AtomicLong serviceUnavailableCounter = new AtomicLong(0);

	private final SubscriptionService unbindService;

	/**
	 * Constructor
	 * 
	 * @param serverContext
	 *            server context, included initiative parameters
	 * @param unbindService
	 *            instance of subscription service
	 */
	public ServiceUnbindTunnelLet(ServerContext serverContext,
	        SubscriptionService unbindService) {
		super();
		this.unbindService = unbindService;
	}

	@Override
	public void messageReceived(final TunnelContext tunnelContext,
	        final TunnelData<ServiceBind> data) {
		unbindRequestCounter.getAndIncrement();
		unbind(tunnelContext, data);
	}

	@Override
	public void shutdown() throws InterruptedException {
		logger.info("Shuting down...");
		unbindService.destroy();
		logger.info("Shut down.");
	}

	private void unbind(final TunnelContext tunnelContext,
	        final TunnelData<ServiceBind> data) {
		logger.debug(
		        "[{}] Sent service unbind to [unbind@subscription.dev]. [{}] =/= [{}]",
		        new Object[] {
		                tunnelContext.getChannel().getId(),
		                data.obj.getApplicationId(),
		                data.obj.getClientId() });

		String remoteResult = null;

		remoteResult = unbindService.doUnbind(data.obj.getApplicationId(),
		        data.obj.getClientId(), tunnelContext.getDeviceId());
		if (remoteResult != null) {
			JSONObject obj = JSONObject.fromObject(remoteResult);
			logger.debug("[{}] [{}]", tunnelContext.getChannel().getId(),
			        obj.toString());
			if ("OK".equalsIgnoreCase(obj.getString("status"))) {
				ServiceBind serviceBind = ServiceBind.newBuilder()
				        .setApplicationId(data.obj.getApplicationId())
				        .build();
				tunnelContext.getChannel().write(
				        new TunnelData<MessageLite>(data.tag,
				                MessageRouting.MessageCMD.SERVICE_BIND_UPDATE
				                        .getNumber(),
				                serviceBind));
				unbindSuccessCounter.getAndIncrement();
				logger.debug(
				        "[{}] Service unbind success,sent to client. [{}:{}:{}]",
				        new Object[] { tunnelContext.getChannel().getId(),
				                serviceBind.getApplicationId(),
				                serviceBind.getClientId(),
				                tunnelContext.getDeviceId() }
				        );
			} else {
				tunnelContext.getChannel()
				        .write(new TunnelData<MessageLite>(data.tag,
				                MessageRouting.MessageCMD.RESPONSE.getNumber(),
				                Response
				                        .newBuilder().setStatus("ERROR")
				                        .build()));
			}
		} else {
			logger.error("[{}] Device unbind failed!", tunnelContext
			        .getChannel().getId());
			tunnelContext.getChannel()
			        .write(new TunnelData<MessageLite>(data.tag,
			                MessageRouting.MessageCMD.RESPONSE.getNumber(),
			                Response.newBuilder().setStatus("ERROR")
			                        .build()));
		}
	}

	/**
	 * Get the value of "device unbind" message counter
	 * 
	 * @param reset
	 *            whether reset counter.
	 * @return the value of "device unbind" message counter.
	 */
	public synchronized static long getUnbindRequestCounter(boolean reset) {
		if (reset) {
			return unbindRequestCounter.getAndSet(0);
		} else {
			return unbindRequestCounter.get();
		}
	}

	/**
	 * Get the value of unbind success counter
	 * 
	 * @param reset
	 *            whether reset counter.
	 * @return the value of unbind success counter.
	 */
	public synchronized static long getUnbindSuccessCounter(boolean reset) {
		if (reset) {
			return unbindSuccessCounter.getAndSet(0);
		} else {
			return unbindSuccessCounter.get();
		}
	}

	/**
	 * Get the value of service unavailable counter
	 * 
	 * @param reset
	 *            whether reset counter.
	 * @return the value of service unavailable counter.
	 */
	public synchronized static long getServiceUnavailableCounter(boolean reset) {
		if (reset) {
			return serviceUnavailableCounter.getAndSet(0);
		} else {
			return serviceUnavailableCounter.get();
		}
	}
}
