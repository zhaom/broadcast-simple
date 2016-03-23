package com.babeeta.hudee.gateway.device.tunnel.let;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

import org.bson.BSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.hudee.core.MessageRouting;
import com.babeeta.hudee.core.MessageRouting.Acknowledgement;
import com.babeeta.hudee.core.MessageRouting.Message;
import com.babeeta.hudee.core.MessageRouting.ReserveInterval;
import com.babeeta.hudee.gateway.device.ServerContext;
import com.babeeta.hudee.gateway.device.service.MessageService;
import com.babeeta.hudee.gateway.device.tunnel.TunnelContext;
import com.babeeta.hudee.gateway.device.tunnel.TunnelData;
import com.babeeta.hudee.gateway.device.tunnel.TunnelLet;
import com.google.protobuf.ByteString;
import com.google.protobuf.MessageLite;
import com.mongodb.Bytes;

/**
 * Handle "ack" message from device.
 * 
 */
public class AcknowledgementTunnelLet implements TunnelLet<Acknowledgement> {
	private static final Logger logger = LoggerFactory
	        .getLogger(AcknowledgementTunnelLet.class);
	private final ServerContext serverContext;
	/**
	 * Count the message update request for monitor
	 */
	private static AtomicLong updateRequestCounter = new AtomicLong(0);

	/**
	 * Count the times of "message update" execute successful from client for
	 * monitor
	 */
	private static AtomicLong updateSuccessCounter = new AtomicLong(0);

	/**
	 * Count the times of update service unavailable for monitor
	 */
	private static AtomicLong updateServiceUnavailableCounter = new AtomicLong(
	        0);

	/**
	 * Count the message list request for monitor
	 */
	private static AtomicLong ackListRequestCounter = new AtomicLong(0);

	/**
	 * Count the times of "message list" execute successful from client for
	 * monitor
	 */
	private static AtomicLong ackListSuccessCounter = new AtomicLong(0);

	/**
	 * Count the times of list service unavailable for monitor
	 */
	private static AtomicLong ackListServiceUnavailableCounter = new AtomicLong(
	        0);

	private final MessageService messageService;

	/**
	 * Constructor
	 * 
	 * @param serverContext
	 *            server context, included initiative parameters
	 * @param messageService
	 *            instance of message service
	 */
	public AcknowledgementTunnelLet(ServerContext serverContext,
	        MessageService messageService) {
		super();
		this.serverContext = serverContext;
		this.messageService = messageService;
	}

	@Override
	public void messageReceived(
	        final TunnelContext tunnelContext,
	        final TunnelData<Acknowledgement> data) {
		updateRequestCounter.getAndIncrement();
		logger.debug("[{}]{}", tunnelContext.getChannel().getId(),
		        data.obj.getUid());
		onMessageUpdate(tunnelContext, data);
	}

	@Override
	public void shutdown() throws InterruptedException {
		logger.info("Shuting down...");
		messageService.destroy();
		logger.info("Shut down.");
	}

	private void onMessageUpdate(final TunnelContext tunnelContext,
	        final TunnelData<Acknowledgement> data) {
		logger.debug("[{}]Check undelivered message of device:{}.",
		        tunnelContext.getChannel().getId(), tunnelContext.getDeviceId());
		String remoteResult;

		remoteResult = messageService.doMessageUpdate(data.obj.getUid());
		if (remoteResult != null) {
			updateSuccessCounter.getAndIncrement();
			tunnelContext.getChannel().write(
			        new TunnelData<MessageLite>(data.tag,
			                MessageRouting.MessageCMD.RESPONSE.getNumber(),
			                MessageRouting.Response.newBuilder()
			                        .setStatus("SUCCESS").build()));

			tunnelContext
			        .setUndeliverMessage(tunnelContext
			                .getUndeliverMessage() - 1);
			tunnelContext.setWaitAck(tunnelContext.getWaitAck() - 1);

			if (tunnelContext.getWaitAck() == 0
			        && tunnelContext.getUndeliverMessage() > 0) {
				listMessage(tunnelContext);
			} else if (tunnelContext.getWaitAck() == 0
			        && tunnelContext.getUndeliverMessage() == 0) {
				if (!tunnelContext.inPushMode()) {
					long interval = serverContext.getPullPolicy()
					        .getNextInterval(
					                tunnelContext.getServiceId(),
					                tunnelContext.getDeviceId(),
					                System.currentTimeMillis() / 1000L);
					logger.debug(
					        "[{}] Reserved time connection time and close channel",
					        tunnelContext.getChannel()
					                .getId(), interval);
					tunnelContext
					        .getChannel()
					        .write(new TunnelData<MessageLite>(
					                0,
					                MessageRouting.MessageCMD.RESERVE_INTERVAL
					                        .getNumber(),
					                ReserveInterval
					                        .newBuilder()
					                        .setAid(tunnelContext
					                                .getServiceId())
					                        .setNextInterval(interval)
					                        .build()));
				}
			}
		} else {
			logger.error("[{}] Error occured while request.",
			        tunnelContext.getChannel().getId());
			tunnelContext.getChannel().write(
			        new TunnelData<MessageLite>(data.tag,
			                MessageRouting.MessageCMD.RESPONSE.getNumber(),
			                MessageRouting.Response.newBuilder()
			                        .setStatus("SERVICE_UNAVAILABLE")
			                        .build()));
		}
	}

	private void listMessage(final TunnelContext tunnelContext) {
		ackListRequestCounter.getAndIncrement();
		onListMessage(tunnelContext);
	}

	private void onListMessage(final TunnelContext tunnelContext) {
		logger.debug("[{}](Ack)List undelivered message of device:{}.",
		        tunnelContext.getChannel().getId(), tunnelContext.getDeviceId());

		byte[] remoteResult = null;

		if (tunnelContext.inPushMode()) {
			remoteResult = messageService.doMessageList(tunnelContext
			        .getDeviceId(), null);
		} else {
			remoteResult = messageService.doMessageList(tunnelContext
			        .getDeviceId(), tunnelContext.getServiceId());
		}

		if (remoteResult != null) {
			BSONObject bsonMessageList = Bytes.decode(remoteResult);
			tunnelContext.setWaitAck(bsonMessageList.keySet().size());
			logger.debug("[{}](Ack) Found {} undelivered message(s).",
			        tunnelContext.getChannel().getId(),
			        tunnelContext.getWaitAck());
			ackListSuccessCounter.getAndIncrement();
			Iterator<String> it = bsonMessageList.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				BSONObject value = (BSONObject) bsonMessageList.get(key);

				Message message = composeMessage(value);
				logger.info(
				        "[{}](Ack)Send message [{}] to [{}].",
				        new Object[] {
				                tunnelContext.getChannel().getId(),
				                message.getUid(),
				                tunnelContext.getDeviceId() });
				tunnelContext.getChannel().write(
				        new TunnelData<MessageLite>(0,
				                MessageRouting.MessageCMD.MESSAGE.getNumber(),
				                message));
			}
		} else {
			logger.error("[{}](Ack) List message failed!", tunnelContext
			        .getChannel().getId());
		}
	}

	private Message composeMessage(BSONObject record) {
		String uid = record.get("_id").toString();
		String recipient = record.get("recipient").toString();
		String sender = record.get("sender").toString();
		byte[] messageBody = (byte[]) record.get("message");

		Message message = Message
		        .newBuilder()
		        .setUid(uid)
		        .setContent(ByteString.copyFrom(messageBody))
		        .setFrom("message@dev")
		        .setTo(new StringBuilder(recipient)
		                .append(".")
		                .append(sender)
		                .append("@dev").toString())
		        .setDate(System.currentTimeMillis()).build();
		return message;
	}

	/**
	 * Get the value of "message update" request counter
	 * 
	 * @param reset
	 *            whether reset counter.
	 * @return the value of "message update" request counter.
	 */
	public synchronized static long getUpdateRequestCounter(boolean reset) {
		if (reset) {
			return updateRequestCounter.getAndSet(0);
		} else {
			return updateRequestCounter.get();
		}
	}

	/**
	 * Get the value of "message update" success counter
	 * 
	 * @param reset
	 *            whether reset counter.
	 * @return the value of "message update" success counter.
	 */
	public synchronized static long getUpdateSuccessCounter(boolean reset) {
		if (reset) {
			return updateSuccessCounter.getAndSet(0);
		} else {
			return updateSuccessCounter.get();
		}
	}

	/**
	 * Get the value of update service unavailable counter
	 * 
	 * @param reset
	 *            whether reset counter.
	 * @return the value of update service unavailable counter.
	 */
	public synchronized static long getUpdateServiceUnavailableCounter(
	        boolean reset) {
		if (reset) {
			return updateServiceUnavailableCounter.getAndSet(0);
		} else {
			return updateServiceUnavailableCounter.get();
		}
	}

	/**
	 * Get the value of "message list" request counter
	 * 
	 * @param reset
	 *            whether reset counter.
	 * @return the value of "message list" request counter.
	 */
	public synchronized static long getListRequestCounter(boolean reset) {
		if (reset) {
			return ackListRequestCounter.getAndSet(0);
		} else {
			return ackListRequestCounter.get();
		}
	}

	/**
	 * Get the value of "message list" success counter
	 * 
	 * @param reset
	 *            whether reset counter.
	 * @return the value of "message list" success counter.
	 */
	public synchronized static long getListSuccessCounter(boolean reset) {
		if (reset) {
			return ackListSuccessCounter.getAndSet(0);
		} else {
			return ackListSuccessCounter.get();
		}
	}

	/**
	 * Get the value of list service unavailable counter
	 * 
	 * @param reset
	 *            whether reset counter.
	 * @return the value of list service unavailable counter.
	 */
	public synchronized static long getListServiceUnavailableCounter(
	        boolean reset) {
		if (reset) {
			return ackListServiceUnavailableCounter.getAndSet(0);
		} else {
			return ackListServiceUnavailableCounter.get();
		}
	}
}
