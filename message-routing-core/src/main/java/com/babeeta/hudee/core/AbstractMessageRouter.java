package com.babeeta.hudee.core;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.hudee.core.MessageRouting.Message;

/**
 *
 */
public abstract class AbstractMessageRouter implements MessageHandler {
	private static final Logger logger = LoggerFactory
	        .getLogger(AbstractMessageRouter.class);

	private final MessageSender messageSender;

	protected AtomicLong recvCount = new AtomicLong(0);
	protected AtomicLong transformErrorCount = new AtomicLong(0);
	protected AtomicLong dropCount = new AtomicLong(0);
	protected AtomicLong toSendCount = new AtomicLong(0);

	/**
	 * Constructor
	 * 
	 * @param messageSender
	 *            instance of message sender implements
	 */
	public AbstractMessageRouter(MessageSender messageSender) {
		super();
		this.messageSender = messageSender;
	}

	@Override
	public void onMessage(final Message message) {
		recvCount.getAndIncrement();
		Message transformed = null;
		try {
			transformed = transform(message);
		} catch (Throwable t) {
			transformErrorCount.getAndIncrement();
			logger.error("[{}] is dropped. {}", message.getUid(),
			        t.getMessage());
			t.printStackTrace();
			return;
		}

		if (transformed == null) {
			logger.debug("[{}] is dropped.", message.getUid());
			dropCount.getAndIncrement();
		} else {
			logger.debug("Sending [{}] to [{}]", message.getUid(),
			        transformed.getTo());
			toSendCount.getAndIncrement();
			messageSender.send(transformed).addListener(
			        new MessageFutureListener() {
				        @Override
				        public void operationComplete(MessageFuture future) {
					        if (future.isSuccess()) {
						        logger.debug("[{}] is delivered",
						                message.getUid());
					        } else {
						        logger.error("[{}] Cause:{}", message.getUid(),
						                future.getCause().getMessage());
					        }
				        }
			        });
		}
	}

	protected MessageSender getMessageSender() {
		return messageSender;
	}

	protected abstract Message transform(Message message);
}
