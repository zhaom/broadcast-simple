package com.babeeta.hudee.gateway.device.service;

import org.apache.http.HttpStatus;
import org.apache.http.util.CharArrayBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the HTTP RPC of message count
 * 
 * @author Xinyu
 * 
 */
public class MessageService extends AbstractHttpRPCService {
	private final String domain;
	private static final Logger logger = LoggerFactory
	        .getLogger(MessageService.class);

	/**
	 * Constructor
	 * 
	 * @param maxConnection
	 *            allow max quantity of connection
	 * @param domain
	 *            domain of device account service
	 */
	public MessageService(int maxConnection, String domain) {
		super(maxConnection);
		this.domain = domain;
	}

	private interface Callback {
		String doIt();
	}

	private String execute(Callback callback) {
		return callback.doIt();
	}

	/**
	 * Do message count
	 * 
	 * @param deviceId
	 *            device id
	 * @param specialAid
	 *            special aid, for counting all message if null.
	 * @return result in JSON
	 */
	public String doMessageCount(final String deviceId, final String specialAid) {
		logger.debug("[RPC-MessageCount] deviceId = {}, special aid = {}",
		        deviceId, specialAid);
		String ret = execute(new Callback() {
			@Override
			public String doIt() {
				return count(deviceId, specialAid);
			}
		});

		return ret;
	}

	/**
	 * Do message update
	 * 
	 * @param messageId
	 *            message id
	 * @return result in JSON
	 */
	public String doMessageUpdate(final String messageId) {
		logger.debug("[RPC-MessageUpdate] messageId = {}", messageId);
		String ret = execute(new Callback() {
			@Override
			public String doIt() {
				return update(messageId);
			}
		});
		return ret;
	}

	private interface listCallback {
		byte[] doIt();
	}

	private byte[] execute(listCallback callback) {
		return callback.doIt();
	}

	/**
	 * Do message list
	 * 
	 * @param deviceId
	 *            device id
	 * @param aid
	 *            special aid
	 * @return result in JSON
	 */
	public byte[] doMessageList(final String deviceId, final String aid) {
		logger.debug("[RPC-MessageList] deviceId = {}", deviceId);
		byte[] ret = execute(new listCallback() {
			@Override
			public byte[] doIt() {
				return list(deviceId, aid);
			}
		});

		return ret;
	}

	private String count(final String did, final String aid) {
		CharArrayBuffer buffer = new CharArrayBuffer(150);
		buffer.append("/1/api/message/count?device=");
		buffer.append(did);
		buffer.append("&status=DELIVERING");
		if (aid != null) {
			buffer.append("&aid=");
			buffer.append(aid);
		}
		HttpRPCResult result = invokeGet(
		        composeURI(domain, buffer.toString()), HttpStatus.SC_OK);

		if (result.getStatusCode() == HttpStatus.SC_OK) {
			return new String(result.getPayload());
		} else if (result.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
			return "0";
		}
		logger.error(
		        "[RPC-MessageCount] failed! statusCode: {}; message: {}",
		        result.getStatusCode(), result.getMessage());
		return null;
	}

	private String update(final String uid) {
		CharArrayBuffer buffer = new CharArrayBuffer(100);
		buffer.append("/1/api/message/");
		buffer.append(uid);
		buffer.append("/status/update?status=ACKED");

		HttpRPCResult result = invokePut(
		        composeURI(domain, buffer.toString()), null, null,
		        HttpStatus.SC_OK);

		if (result.getStatusCode() == HttpStatus.SC_OK) {
			return new String(result.getPayload());
		}
		logger.error(
		        "[RPC-MessageUpdate] failed! statusCode: {}; message: {}",
		        result.getStatusCode(), result.getMessage());
		return null;
	}

	private byte[] list(final String did, final String aid) {
		CharArrayBuffer buffer = new CharArrayBuffer(100);
		buffer.append("/1/api/message/list?device=");
		buffer.append(did);
		buffer.append("&status=DELIVERING&pageSize=20&pageIndex=0");
		if (aid != null) {
			buffer.append("&aid=");
			buffer.append(aid);
		}
		HttpRPCResult result = invokeGet(
		        composeURI(domain, buffer.toString()), HttpStatus.SC_OK);

		if (result.getStatusCode() == HttpStatus.SC_OK) {
			if (result.getPayload().length > 10 * 1024) {
				logger.warn("[RPC-MessageList] Payload size = {}",
				        result.getPayload().length);
			}
			return result.getPayload();
		}
		logger.error(
		        "[RPC-MessageList] failed! statusCode: {}; message: {}",
		        result.getStatusCode(), result.getMessage());
		return null;
	}
}
