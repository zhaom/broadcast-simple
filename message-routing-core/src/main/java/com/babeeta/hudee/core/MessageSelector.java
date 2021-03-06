package com.babeeta.hudee.core;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.hudee.core.MessageRouting.Message;

/**
 * 根据收件人分拣的实现
 * 
 * @author leon
 * 
 */
public class MessageSelector implements MessageHandler {

	private final static Logger logger = LoggerFactory
	        .getLogger(MessageSelector.class);

	private final ConcurrentMap<String, MessageHandler> recipientMap = new ConcurrentHashMap<String, MessageHandler>();

	private MessageHandler defaultMessageHandler = null;

	/**
	 * Constructor
	 */
	public MessageSelector() {
		// 默认的实现，把消息都丢弃掉
		this.defaultMessageHandler = new MessageHandler() {
			@Override
			public void onMessage(Message message) {
				logger.info("[{}] Message selector default is dropped.",
				        message.getUid());
			}
		};
	}

	@Override
	public void onMessage(Message message) {
		if (message.getTo().indexOf("@") == -1) {
			logger.error("[{}]Illegal recipient address. {}", message.getUid(),
			        message.getTo());
		} else {
			MessageHandler handler = recipientMap.get(message.getTo()
			        .substring(0, message.getTo().indexOf("@")));
			if (handler == null) {
				handler = this.defaultMessageHandler;
			}
			handler.onMessage(message);
		}
	}

	/**
	 * 注册新的收件人
	 * 
	 * @param recipient
	 *            收件人名字
	 * @param messageHandler
	 *            message handler
	 * @throws RecipientAlreadyRegisteredException
	 *             当recipient已经被注册
	 */
	public void register(String recipient, MessageHandler messageHandler) throws
	        RecipientAlreadyRegisteredException {
		if (recipientMap.putIfAbsent(recipient, messageHandler) != null) {
			throw new RecipientAlreadyRegisteredException();
		}
	}

	/**
	 * Set (override) the default message handler
	 * 
	 * @param messageHandler
	 *            message handler
	 */
	public void setDefaultMessageHandler(MessageHandler messageHandler) {
		this.defaultMessageHandler = messageHandler;
	}
}