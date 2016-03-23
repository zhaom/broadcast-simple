package com.babeeta.hudee.gateway.device.tunnel.let;

import java.util.concurrent.atomic.AtomicLong;

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
 * Handle "bind" message from device.
 * 
 */
public class ServiceBindTunnelLet implements TunnelLet<ServiceBind> {

	private static final Logger logger = LoggerFactory
	        .getLogger(ServiceBindTunnelLet.class);
	private final ServerContext serverContext;
	/**
	 * Count the "device bind" message from client for monitor
	 */
	private static AtomicLong bindMessageCounter = new AtomicLong(0);

	/**
	 * Count the times of "device bind" execute successful for monitor
	 */
	private static AtomicLong bindSuccessCounter = new AtomicLong(0);

	/**
	 * Count the times of service unavailable for monitor
	 */
	private static AtomicLong serviceUnavailableCounter = new AtomicLong(0);

	private final SubscriptionService bindService;

	/**
	 * Constructor
	 * 
	 * @param serverContext
	 *            server context, included initiative parameters
	 * @param bindService
	 *            instance of subscription service
	 */
	public ServiceBindTunnelLet(ServerContext serverContext,
	        SubscriptionService bindService) {
		super();
		this.serverContext = serverContext;
		this.bindService = bindService;
	}

	@Override
	public void messageReceived(final TunnelContext tunnelContext,
	        final TunnelData<ServiceBind> data) {
		bindMessageCounter.getAndIncrement();

		if (tunnelContext.getDeviceId() == null) {
			tunnelContext.getChannel().write(
			        new TunnelData<MessageLite>(data.tag,
			                MessageRouting.MessageCMD.RESPONSE.getNumber(),
			                Response.newBuilder().setStatus("ERROR").build()));
			logger.debug(
			        "[{}] TunnelContext getDeviceId is null. [{}]",
			        tunnelContext.getChannel().getId(),
			        data.obj.getApplicationId()
			                + ":"
			                + (data.obj.getClientId() == null ? "null"
			                        : data.obj.getClientId())
			        );
			return;
		}
		bind(tunnelContext, data);
	}

	@Override
	public void shutdown() throws InterruptedException {
		logger.info("Shuting down...");
		bindService.destroy();
		logger.info("Shut down.");
	}

	private void bind(final TunnelContext tunnelContext,
	        final TunnelData<ServiceBind> data) {
		logger.debug(
		        "[{}] Sent service bind to [bind@subscription.dev]. [{}]",
		        new Object[] {
		                tunnelContext.getChannel().getId(),
		                data.obj.getApplicationId()
		                        + ":"
		                        + (data.obj.getClientId() == null ? ""
		                                : data.obj.getClientId()) + ":"
		                        + tunnelContext.getDeviceId()
		        }
		        );

		String remoteResult = null;
		remoteResult = bindService.doBind(data.obj.getApplicationId(),
		        data.obj.getClientId(),
		        tunnelContext.getDeviceId());
		if (remoteResult != null) {
			ServiceBind serviceBind;
			if (tunnelContext.inPushMode()) {
				serviceBind = ServiceBind.newBuilder()
				        .setApplicationId(data.obj.getApplicationId())
				        .setClientId(remoteResult)
				        .build();
			} else {
				long nextInterval = serverContext.getPullPolicy()
				        .getNextInterval(
				                data.obj.getApplicationId(),
				                tunnelContext.getDeviceId(),
				                System.currentTimeMillis() / 1000L);
				// TODO: need fix test code
				if (nextInterval < 0) {
					nextInterval = 987654321;
					/*
					 * if (nextInterval == -2) {
					 * logger.error("[{}] has not a schedule!",
					 * data.obj.getApplicationId()); } else { logger.error(
					 * "Get reserve interval about [{}] failed! {}",
					 * data.obj.getApplicationId(), nextInterval); } serviceBind
					 * = ServiceBind.newBuilder()
					 * .setApplicationId(data.obj.getApplicationId())
					 * .setClientId(remoteResult) .build();
					 */
				} // else {
				serviceBind = ServiceBind.newBuilder()
				        .setApplicationId(data.obj.getApplicationId())
				        .setClientId(remoteResult)
				        .setNextInterval(nextInterval)
				        .build();
				// }
			}

			tunnelContext.getChannel()
			        .write(new TunnelData<MessageLite>(data.tag,
			                MessageRouting.MessageCMD.SERVICE_BIND_UPDATE
			                        .getNumber(),
			                serviceBind));
			bindSuccessCounter.getAndIncrement();
			logger.debug(
			        "[{}] Service bind success,sent to client. [{}:{}:{}]",
			        new Object[] { tunnelContext.getChannel().getId(),
			                serviceBind.getApplicationId(),
			                serviceBind.getClientId(),
			                tunnelContext.getDeviceId() }
			        );
			devicePostBind(tunnelContext);
		} else {
			logger.error("[{}] Device bind failed!", tunnelContext
			        .getChannel().getId());
			tunnelContext.getChannel()
			        .write(new TunnelData<MessageLite>(data.tag,
			                MessageRouting.MessageCMD.RESPONSE.getNumber(),
			                Response.newBuilder().setStatus("ERROR")
			                        .build()));
		}
	}

	private void devicePostBind(TunnelContext tunnelContext) {

	}

	/**
	 * Get the value of "device bind" message counter
	 * 
	 * @param reset
	 *            whether reset counter.
	 * @return the value of "device bind" message counter.
	 */
	public synchronized static long getBindRequestCounter(boolean reset) {
		if (reset) {
			return bindMessageCounter.getAndSet(0);
		} else {
			return bindMessageCounter.get();
		}
	}

	/**
	 * Get the value of bind success counter
	 * 
	 * @param reset
	 *            whether reset counter.
	 * @return the value of bind success counter.
	 */
	public synchronized static long getBindSuccessCounter(boolean reset) {
		if (reset) {
			return bindSuccessCounter.getAndSet(0);
		} else {
			return bindSuccessCounter.get();
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