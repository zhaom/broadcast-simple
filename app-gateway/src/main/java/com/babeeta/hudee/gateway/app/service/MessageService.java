package com.babeeta.hudee.gateway.app.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * implement the interface of message service
 */
public class MessageService extends AbstractHttpRPCService {
	private static final Logger logger = LoggerFactory
	        .getLogger(MessageService.class);
	private final String domain;
	private static String queryPattern = "/1/api/message/status/query?id=%s";
	private static String createPattern = "/1/api/message/create/%s/%s/%s";
	private static String createParentPattern = "/1/api/message/create/parent/%s/%s";
	private static String createReferencePattern = "/1/api/message/create/reference/%s/%s/%s/%s";
	private static String contentType = "application/octet-stream";

	/**
	 * Constructor
	 * 
	 * @param maxConnection
	 *            max connection limit
	 * 
	 * @param domain
	 *            message service domain
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
	 * Query message status by message id
	 * 
	 * @param id
	 *            message id
	 * @return return a JSON object in string if message id exists, otherwise
	 *         return null.
	 */
	public String getMessageStatus(final String id) {
		String ret = execute(new Callback() {
			@Override
			public String doIt() {
				return doQueryStatus(id);
			}
		});

		return ret;
	}

	/**
	 * Create a normal message
	 * 
	 * @param from
	 *            application id
	 * @param to
	 *            client id
	 * @param device
	 *            device id
	 * @param life
	 *            message life
	 * @param content
	 *            message content
	 * @return message id if create succeed, otherwise return null;
	 */
	public String createNormalMessage(final String from, final String to,
	        final String device, final int life, final byte[] content) {
		String ret = execute(new Callback() {
			@Override
			public String doIt() {
				return doCreateNormalMessage(from, to, device, life, content);
			}
		});
		return ret;
	}

	/**
	 * Create a parent message
	 * 
	 * @param from
	 *            application id
	 * @param to
	 *            client id
	 * @param life
	 *            message life
	 * @param content
	 *            message content
	 * @return message id if create succeed, otherwise return null;
	 */
	public String createParentMessage(final String from, final String to,
	        final int life, final byte[] content) {
		String ret = execute(new Callback() {
			@Override
			public String doIt() {
				return doCreateParentMessage(from, to, life, content);
			}
		});
		return ret;
	}

	/**
	 * Create a normal message
	 * 
	 * @param from
	 *            application id
	 * @param to
	 *            client id
	 * @param device
	 *            device id
	 * @param parent
	 *            parent message id
	 * @param life
	 *            message life
	 * @return message id if create succeed, otherwise return null;
	 */
	public String createReferenceMessage(final String from, final String to,
	        final String device, final String parent, final int life) {
		String ret = execute(new Callback() {
			@Override
			public String doIt() {
				return doCreateReferenceMessage(from, to, device, parent, life);
			}
		});
		return ret;
	}

	private String doQueryStatus(final String messageId) {
		HttpRPCResult result = invokeGet(
		        composeURI(domain, String.format(queryPattern, messageId)),
		        HttpStatus.SC_OK);
		String retResult = null;
		if (result.getStatusCode() == HttpStatus.SC_OK) {
			String returnStr = new String(result.getPayload());
			logger.debug("[RPC-getMsgStatus] Success! {}",
			        returnStr);
			return returnStr;
		} else {
			logger.error(
			        "[RPC-getMsgStatus] failed! statusCode: {}; message: {}",
			        result.getStatusCode(), result.getMessage());
		}
		return retResult;
	}

	private String doCreateNormalMessage(final String from, final String to,
	        final String device, final int life, final byte[] content) {
		Map<String, String> header = new HashMap<String, String>();
		header.put(HttpHeaders.CONTENT_TYPE, contentType);
		header.put("life", Integer.toString(life));
		final String uri = composeURI(domain,
		        String.format(createPattern, from, to, device));
		HttpRPCResult result = invokePost(uri, header, content,
		        HttpStatus.SC_CREATED);
		String retResult = null;
		if (result.getStatusCode() == HttpStatus.SC_CREATED) {
			String returnStr = new String(result.getPayload());
			if (returnStr != null && returnStr.length() > 0
			        && returnStr.length() == 32) {
				retResult = returnStr;
			} else {
				logger.error(
				        "[RPC-CreateMsg] Fatal error! Remote return:{}",
				        returnStr);
			}
		} else {
			logger.error(
			        "[RPC-CreateMsg] failed! statusCode: {}; message: {}",
			        result.getStatusCode(), result.getMessage());
		}
		return retResult;
	}

	private String doCreateParentMessage(final String from, final String to,
	        final int life, final byte[] content) {
		Map<String, String> header = new HashMap<String, String>();
		header.put(HttpHeaders.CONTENT_TYPE, contentType);
		header.put("life", Integer.toString(life));
		final String uri = composeURI(domain,
		        String.format(createParentPattern, from, to));
		HttpRPCResult result = invokePost(uri, header, content,
		        HttpStatus.SC_CREATED);
		String retResult = null;
		if (result.getStatusCode() == HttpStatus.SC_CREATED) {
			String returnStr = new String(result.getPayload());
			if (returnStr != null && returnStr.length() > 0
			        && returnStr.length() == 32) {
				retResult = returnStr;
			} else {
				logger.error(
				        "[RPC-CreateMsg] Fatal error! Remote return:{}",
				        returnStr);
			}
		} else {
			logger.error(
			        "[RPC-CreateMsg] failed! statusCode: {}; message: {}",
			        result.getStatusCode(), result.getMessage());
		}
		return retResult;
	}

	private String doCreateReferenceMessage(final String from, final String to,
	        final String device, final String parent, final int life) {
		Map<String, String> header = new HashMap<String, String>();
		header.put("life", Integer.toString(life));
		final String uri = composeURI(domain,
		        String.format(createReferencePattern, from, to, device, parent));
		HttpRPCResult result = invokePost(uri, header, null,
		        HttpStatus.SC_CREATED);
		String retResult = null;
		if (result.getStatusCode() == HttpStatus.SC_CREATED) {
			String returnStr = new String(result.getPayload());
			if (returnStr != null && returnStr.length() > 0
			        && returnStr.length() == 32) {
				retResult = returnStr;
			} else {
				logger.error(
				        "[RPC-CreateMsg] Fatal error! Remote return:{}",
				        returnStr);
			}
		} else {
			logger.error(
			        "[RPC-CreateMsg] failed! statusCode: {}; message: {}",
			        result.getStatusCode(), result.getMessage());
		}
		return retResult;
	}
}
