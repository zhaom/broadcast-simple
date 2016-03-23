package com.babeeta.hudee.gateway.device.tunnel.let;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

import net.sf.json.JSONObject;

import org.bson.BSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.hudee.core.MessageRouting;
import com.babeeta.hudee.core.MessageRouting.Credential;
import com.babeeta.hudee.core.MessageRouting.Message;
import com.babeeta.hudee.core.MessageRouting.ReserveInterval;
import com.babeeta.hudee.core.MessageRouting.Response;
import com.babeeta.hudee.core.MessageRouting.WorkMode;
import com.babeeta.hudee.gateway.device.ServerContext;
import com.babeeta.hudee.gateway.device.service.DeviceAccountService;
import com.babeeta.hudee.gateway.device.service.MessageService;
import com.babeeta.hudee.gateway.device.tunnel.TunnelContext;
import com.babeeta.hudee.gateway.device.tunnel.TunnelData;
import com.babeeta.hudee.gateway.device.tunnel.TunnelLet;
import com.babeeta.hudee.gateway.device.tunnel.TunnelService;
import com.google.protobuf.ByteString;
import com.google.protobuf.MessageLite;
import com.mongodb.Bytes;

/**
 * Handle "auth" message from device.
 * 
 */
public class AuthTunnelLet implements TunnelLet<Credential> {
	private static final Logger logger = LoggerFactory
	        .getLogger(AuthTunnelLet.class);
	private final ServerContext serverContext;

	/**
	 * Count the "device auth" message from client for monitor
	 */
	private static AtomicLong authMessageCounter = new AtomicLong(0);

	/**
	 * Count the times of "device auth" execute successful from client for
	 * monitor
	 */
	private static AtomicLong authSuccessCounter = new AtomicLong(0);

	/**
	 * Count the times of auth service unavailable for monitor
	 */
	private static AtomicLong authServiceUnavailableCounter = new AtomicLong(0);

	/**
	 * Count the message count request for monitor
	 */
	private static AtomicLong countRequestCounter = new AtomicLong(0);

	/**
	 * Count the times of "message count" execute successful from client for
	 * monitor
	 */
	private static AtomicLong countSuccessCounter = new AtomicLong(0);

	/**
	 * Count the times of count service unavailable for monitor
	 */
	private static AtomicLong countServiceUnavailableCounter = new AtomicLong(0);

	/**
	 * Count the message list request for monitor
	 */
	private static AtomicLong listRequestCounter = new AtomicLong(0);

	/**
	 * Count the times of "message list" execute successful from client for
	 * monitor
	 */
	private static AtomicLong listSuccessCounter = new AtomicLong(0);

	/**
	 * Count the times of list service unavailable for monitor
	 */
	private static AtomicLong listServiceUnavailableCounter = new AtomicLong(0);

	private final DeviceAccountService authService;
	private final MessageService messageService;

	/**
	 * Constructor
	 * 
	 * @param serverContext
	 *            server context, included initiative parameters
	 * @param authService
	 *            instance of device account service
	 * @param messageService
	 *            instance of message service
	 */
	public AuthTunnelLet(ServerContext serverContext,
	        DeviceAccountService authService, MessageService messageService) {
		super();
		this.serverContext = serverContext;
		this.authService = authService;
		this.messageService = messageService;
	}

	@Override
	public void messageReceived(
	        final TunnelContext tunnelContext, final TunnelData<Credential> data) {
		authMessageCounter.incrementAndGet();
		onAuth(tunnelContext, data);
	}

	@Override
	public void shutdown() throws InterruptedException {
		logger.info("Shuting down...");
		authService.destroy();
		messageService.destroy();
		logger.info("Shut down.");
	}

	private void onAuth(final TunnelContext tunnelContext,
	        final TunnelData<Credential> data) {
		logger.info("[{}] Auth from [{}]", data.obj.getId(),
		        tunnelContext
		                .getChannel().getRemoteAddress());
		Credential credential = data.obj;
		String remoteResult = null;

		// check schedule by aid in pull info
		if (credential.hasPullInfo()) {
			String pullInfo = data.obj.getPullInfo();
			tunnelContext.setPushMode(false);

			String serviceId = pullInfo.substring(0, pullInfo.indexOf("."));
			String deviceId = pullInfo.substring(pullInfo.indexOf(".") + 1);

			tunnelContext.setServiceId(serviceId);

			long nowTimeInSec = System.currentTimeMillis() / 1000L;
			if (!serverContext.getPullPolicy().isInService(serviceId,
			        nowTimeInSec)) {
				long interval = serverContext.getPullPolicy().getNextInterval(
				        serviceId, deviceId,
				        nowTimeInSec);
				logger.debug(
				        "[{}] out of service, reserved time connection time and close channel",
				        tunnelContext.getChannel()
				                .getId(), interval);
				// Xinyu modified: add "aid" to reserve interval object
				tunnelContext.getChannel().write(
				        new TunnelData<MessageLite>(data.tag,
				                MessageRouting.MessageCMD.RESERVE_INTERVAL
				                        .getNumber(),
				                ReserveInterval.newBuilder()
				                        .setAid(serviceId)
				                        .setNextInterval(interval)
				                        .build()));

				return;
			} else {
				// size of online waiting client larger than x
				// TODO: make x configurable
				if (TunnelService.getTunnelCount() > 300000) {
					long interval = serverContext.getPullPolicy()
					        .getCurrentInterval(serviceId, deviceId,
					                nowTimeInSec);
					logger.debug(
					        "[{}] too many connections, reserved time connection time and close channel",
					        tunnelContext.getChannel()
					                .getId(), interval);
					tunnelContext
					        .getChannel()
					        .write(new TunnelData<MessageLite>(
					                data.tag,
					                MessageRouting.MessageCMD.RESERVE_INTERVAL
					                        .getNumber(),
					                ReserveInterval.newBuilder()
					                        .setAid(serviceId)
					                        .setNextInterval(interval)
					                        .build()));
					return;
				} else {
					logger.debug("[{}] working in pull mode.", tunnelContext
					        .getChannel().getId());
				}
			}
		} else {
			tunnelContext.setPushMode(true);
		}

		remoteResult = authService.doAuth(credential.getId(),
		        credential.getSecureKey());
		if (remoteResult != null) {
			JSONObject obj = JSONObject.fromObject(remoteResult);
			if ("OK".equalsIgnoreCase(obj.get("status").toString())) {
				// TODO: work mode should be configurable.
				WorkMode workMode;
				if (obj.containsKey("push_mode")) {
					if (obj.getBoolean("push_mode")) {
						workMode = WorkMode.PUSH;
					} else {
						workMode = WorkMode.PULL;
					}
				} else {
					// default work mode is PUSH.
					workMode = WorkMode.PUSH;
				}

				// TODO: remove later! just for test!
				workMode = WorkMode.PULL;

				tunnelContext.setPushMode(workMode == WorkMode.PUSH);

				authSuccessCounter.incrementAndGet();
				tunnelContext.setDeviceId(data.obj.getId());

				tunnelContext.getChannel()
				        .write(new TunnelData<MessageLite>(data.tag,
				                MessageRouting.MessageCMD.RESPONSE
				                        .getNumber(),
				                Response
				                        .newBuilder()
				                        .setStatus("SUCCESS")
				                        .setWorkMode(workMode)
				                        .build()));
				logger.debug("[{}] Sent success to client. [{}]",
				        tunnelContext.getChannel().getId(), obj.toString());
				devicePostLogin(tunnelContext);
			} else {
				logger.debug(
				        "[{}] Device Auth result = fail. [{}:{}]",
				        new Object[] { tunnelContext.getChannel().getId(),
				                credential.getId(),
				                credential.getSecureKey() });
				sendError(tunnelContext, data);
			}
		} else {
			logger.error("[{}] Device auth failed!", tunnelContext
			        .getChannel().getId());
			sendError(tunnelContext, data);
		}
	}

	private void devicePostLogin(TunnelContext tunnelContext) {
		if (!tunnelContext.inPushMode() && tunnelContext.getServiceId() == null) {
			// do nothing if in PULL mode and service id is null.
			return;
		}
		countMessage(tunnelContext);
	}

	private void onCountMessage(final TunnelContext tunnelContext) {
		logger.debug("[{}]Check undelivered message of device:{}.",
		        tunnelContext.getChannel().getId(), tunnelContext.getDeviceId());

		String remoteResult = null;
		if (tunnelContext.inPushMode()) {
			remoteResult = messageService.doMessageCount(tunnelContext
			        .getDeviceId(), null);
		} else {
			remoteResult = messageService.doMessageCount(tunnelContext
			        .getDeviceId(), tunnelContext.getServiceId());
		}
		if (remoteResult != null) {
			int count = (int) Long.parseLong(remoteResult);
			logger.debug("[{}] Device [{}] has {} undeliver message.",
			        new Object[] {
			                tunnelContext.getChannel().getId(),
			                tunnelContext.getDeviceId(), count });
			countSuccessCounter.getAndIncrement();
			tunnelContext.setUndeliverMessage(count);

			if (count > 0) {
				listMessage(tunnelContext);
			} else {
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
					tunnelContext.getChannel().write(
					        new TunnelData<MessageLite>(
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
			logger.error("[{}] Count message failed!", tunnelContext
			        .getChannel().getId());
		}

	}

	private void countMessage(final TunnelContext tunnelContext) {
		countRequestCounter.getAndIncrement();
		onCountMessage(tunnelContext);
	}

	private void sendError(final TunnelContext tunnelContext,
	        final TunnelData<Credential> data) {
		tunnelContext.getChannel().write(
		        new TunnelData<MessageLite>(data.tag,
		                MessageRouting.MessageCMD.RESPONSE.getNumber(),
		                Response
		                        .newBuilder().setStatus("ERROR").build()));
	}

	private void listMessage(final TunnelContext tunnelContext) {
		listRequestCounter.getAndIncrement();
		onListMessage(tunnelContext);
	}

	private void onListMessage(final TunnelContext tunnelContext) {
		logger.debug("[{}]List undelivered message of device:{}.",
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
			logger.debug("[{}] Found {} undelivered message(s).",
			        tunnelContext.getChannel().getId(),
			        tunnelContext.getWaitAck());
			listSuccessCounter.getAndIncrement();
			Iterator<String> it = bsonMessageList.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				BSONObject value = (BSONObject) bsonMessageList.get(key);

				Message message = composeMessage(value);
				logger.info(
				        "[{}]Send message [{}] to [{}].",
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
			logger.error("[{}] List message failed!", tunnelContext
			        .getChannel().getId());
		}
	}

	private Message composeMessage(BSONObject record) {
		String uid = record.get("_id").toString();
		String recipient = record.get("to").toString();
		String sender = record.get("from").toString();
		byte[] messageBody = (byte[]) record.get("msg");

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
	 * Get the value of "device auth" message counter
	 * 
	 * @param reset
	 *            whether reset counter.
	 * @return the value of "device auth" message counter.
	 */
	public synchronized static long getDeviceAuthMessageCounter(boolean reset) {
		if (reset) {
			return authMessageCounter.getAndSet(0);
		} else {
			return authMessageCounter.get();
		}
	}

	/**
	 * Get the value of auth success counter
	 * 
	 * @param reset
	 *            whether reset counter.
	 * @return the value of auth success counter.
	 */
	public synchronized static long getAuthSuccessCounter(boolean reset) {
		if (reset) {
			return authSuccessCounter.getAndSet(0);
		} else {
			return authSuccessCounter.get();
		}
	}

	/**
	 * Get the value of auth service unavailable counter
	 * 
	 * @param reset
	 *            whether reset counter.
	 * @return the value of auth service unavailable counter.
	 */
	public synchronized static long getAuthServiceUnavailableCounter(
	        boolean reset) {
		if (reset) {
			return authServiceUnavailableCounter.getAndSet(0);
		} else {
			return authServiceUnavailableCounter.get();
		}
	}

	/**
	 * Get the value of "message count" request counter
	 * 
	 * @param reset
	 *            whether reset counter.
	 * @return the value of "message count" request counter.
	 */
	public synchronized static long getCountRequestCounter(boolean reset) {
		if (reset) {
			return countRequestCounter.getAndSet(0);
		} else {
			return countRequestCounter.get();
		}
	}

	/**
	 * Get the value of "message count" success counter
	 * 
	 * @param reset
	 *            whether reset counter.
	 * @return the value of "message count" success counter.
	 */
	public synchronized static long getCountSuccessCounter(boolean reset) {
		if (reset) {
			return countSuccessCounter.getAndSet(0);
		} else {
			return countSuccessCounter.get();
		}
	}

	/**
	 * Get the value of count service unavailable counter
	 * 
	 * @param reset
	 *            whether reset counter.
	 * @return the value of count service unavailable counter.
	 */
	public synchronized static long getCountServiceUnavailableCounter(
	        boolean reset) {
		if (reset) {
			return countServiceUnavailableCounter.getAndSet(0);
		} else {
			return countServiceUnavailableCounter.get();
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
			return listRequestCounter.getAndSet(0);
		} else {
			return listRequestCounter.get();
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
			return listSuccessCounter.getAndSet(0);
		} else {
			return listSuccessCounter.get();
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
			return listServiceUnavailableCounter.getAndSet(0);
		} else {
			return listServiceUnavailableCounter.get();
		}
	}
}
