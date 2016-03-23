package com.babeeta.hudee.gateway.device.tunnel.let;

import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.hudee.core.MessageRouting;
import com.babeeta.hudee.core.MessageRouting.Message;
import com.babeeta.hudee.core.MessageRouting.Response;
import com.babeeta.hudee.core.misc.Address;
import com.babeeta.hudee.gateway.device.tunnel.TunnelContext;
import com.babeeta.hudee.gateway.device.tunnel.TunnelData;
import com.babeeta.hudee.gateway.device.tunnel.TunnelLet;
import com.google.protobuf.MessageLite;

/**
 * Handle the upload message from client
 * 
 * @author Xinyu
 * 
 */
public class MessageTunnelLet implements TunnelLet<Message> {
	private static final Logger logger = LoggerFactory
	        .getLogger(MessageTunnelLet.class);
	private final String toDomain = "app";
	private final String fromDomain = "dev";
	private static final Pattern idPattern = Pattern.compile("[a-f0-9A-F]{32}");
	/**
	 * Count the upload message from client for monitor
	 */
	private static AtomicLong uploadMessageCounter = new AtomicLong(0);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.babeeta.butterfly.device.gateway.tunnel.TunnelLet#messageReceived
	 * (com.babeeta.butterfly.device.gateway.tunnel.TunnelContext,
	 * com.babeeta.butterfly.device.gateway.tunnel.TunnelData)
	 */
	@Override
	public void messageReceived(
	        TunnelContext tunnelContext, TunnelData<Message> data) {
		uploadMessageCounter.getAndIncrement();
		logger.debug("[{}]", tunnelContext.getChannel().getId());

		// Get message
		Message msg = data.obj;
		// First, check "From" and "To" of message.
		if (checkAddress(msg.getTo(), msg.getFrom())) {
			// IMPLEMENT: Persistence

			// IMPLEMENT: send to gateway.app

			// send response to client
			tunnelContext
			        .getChannel()
			        .write(new TunnelData<MessageLite>(data.tag,
			                MessageRouting.MessageCMD.RESPONSE.getNumber(),
			                Response.newBuilder().setStatus("SUCCESS").build()))
			        .addListener(new ChannelFutureListener() {
				        @Override
				        public void operationComplete(ChannelFuture future)
				                throws Exception {
					        logger.debug("MessageTunnelLet success response has been sent.");
				        }
			        });
		} else {
			// return error to client
			tunnelContext
			        .getChannel()
			        .write(new TunnelData<MessageLite>(data.tag,
			                MessageRouting.MessageCMD.RESPONSE.getNumber(),
			                Response.newBuilder().setStatus("BadMessage")
			                        .build()))
			        .addListener(new ChannelFutureListener() {
				        @Override
				        public void operationComplete(ChannelFuture future)
				                throws Exception {
					        logger.debug("MessageTunnelLet error response has been sent.");
				        }
			        });
		}
	}

	@Override
	public void shutdown() throws InterruptedException {
		logger.info("Shut down.");
	}

	/**
	 * Get the value of upload message counter and reset it with given new
	 * value.
	 * 
	 * @param newValue
	 *            reset value after get action.
	 * @return the value of upload message counter.
	 */
	public static long getUploadMessageCounter(long newValue) {
		return uploadMessageCounter.getAndSet(newValue);
	}

	private boolean checkAddress(String toStr, String fromStr) {
		do {
			if (toStr == null || toStr.length() <= 0) {
				logger.error("Illegal 'To'. [{}]", toStr);
				break;
			}

			if (fromStr == null || fromStr.length() <= 0) {
				logger.error("Illegal 'From'. [{}]", fromStr);
				break;
			}
			Address to = new Address(toStr);
			Address from = new Address(fromStr);
			if (!idPattern.matcher(to.getApplicationId()).matches() ||
			        !idPattern.matcher(from.getApplicationId()).matches() ||
			        !idPattern.matcher(from.getClientId()).matches()) {
				// application id or client id invalid.
				logger.error(
				        "Invalid appId of cid. to.aid:from.aid:from.cid = {}:{}:{}",
				        new Object[] { to.getApplicationId(),
				                from.getApplicationId(),
				                from.getClientId() });
				break;
			}

			if (!to.getApplicationId()
			        .equalsIgnoreCase(from.getApplicationId())) {
				// appId not match!
				logger.error(
				        "AppId not match! to.aid:from.aid = [{}]:[{}]",
				        to.getApplicationId(), from.getApplicationId());
				break;
			}

			if (!to.getDomain().equalsIgnoreCase(toDomain)) {
				// Invalid domain in "To"
				logger.error("Invalid domain of 'To'. {} <> {}",
				        to.getDomain(),
				        toDomain);
				break;
			}

			if (!from.getDomain().equalsIgnoreCase(fromDomain)) {
				// Invalid domain in "From"
				logger.error("Invalid domain of 'From'. {} <> {}",
				        from.getDomain(),
				        fromDomain);
				break;
			}
			return true;
		} while (false);
		return false;
	}
}
