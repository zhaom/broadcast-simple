package com.babeeta.hudee.gateway.device.service;

import org.apache.http.HttpStatus;
import org.apache.http.util.CharArrayBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the HTTP RPC of device bind
 * 
 * @author Xinyu
 * 
 */
public class SubscriptionService extends AbstractHttpRPCService {
	private final String domain;
	private static final Logger logger = LoggerFactory
	        .getLogger(SubscriptionService.class);

	/**
	 * Constructor
	 * 
	 * @param maxConnection
	 *            allow max quantity of connection
	 * @param domain
	 *            domain of device account service
	 */
	public SubscriptionService(int maxConnection, String domain) {
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
	 * Do device bind
	 * 
	 * @param appId
	 *            application id
	 * @param clientId
	 *            client id
	 * @param deviceId
	 *            device id
	 * 
	 * @return result in JSON
	 */
	public String doBind(final String appId, final String clientId,
	        final String deviceId) {
		logger.debug(
		        "[RPC-DeviceBind] appId = {}, deviceId = {}, clientId = {}",
		        new Object[] { appId, deviceId, clientId });
		String ret = execute(new Callback() {
			@Override
			public String doIt() {
				if (clientId == null || clientId != null
				        && clientId.length() == 0) {
					return newBind(appId, deviceId);
				} else {
					return rebind(appId, clientId, deviceId);
				}
			}
		});

		return ret;
	}

	/**
	 * Do device unbind
	 * 
	 * @param appId
	 *            application id
	 * @param clientId
	 *            client id
	 * @param deviceId
	 *            device id
	 * 
	 * @return result in JSON
	 */
	public String doUnbind(final String appId, final String clientId,
	        final String deviceId) {
		logger.debug(
		        "[RPC-DeviceUnbind] appId = {}, deviceId = {}, clientId = {}",
		        new Object[] { appId, deviceId, clientId });

		String ret = execute(new Callback() {
			@Override
			public String doIt() {
				return unbind(appId, clientId, deviceId);
			}
		});

		return ret;
	}

	private String newBind(final String aid, final String did) {
		CharArrayBuffer buffer = new CharArrayBuffer(128);
		buffer.append("/1/api/subscription/newBind/");
		buffer.append(did);
		buffer.append("/");
		buffer.append(aid);

		HttpRPCResult result = invokePut(
		        composeURI(domain, buffer.toString()),
		        null, null, HttpStatus.SC_OK);

		if (result.getStatusCode() == HttpStatus.SC_OK) {
			return new String(result.getPayload());
		}
		logger.error(
		        "[RPC-DeviceBind] failed! statusCode: {}; message: {}",
		        result.getStatusCode(), result.getMessage());
		return null;

	}

	private String rebind(final String aid, final String cid, final String did) {
		CharArrayBuffer buffer = new CharArrayBuffer(128);
		buffer.append("/1/api/subscription/rebind/");
		buffer.append(did);
		buffer.append("/");
		buffer.append(aid);
		buffer.append("/");
		buffer.append(cid);

		HttpRPCResult result = invokePut(
		        composeURI(domain, buffer.toString()),
		        null, null, HttpStatus.SC_OK);

		if (result.getStatusCode() == HttpStatus.SC_OK) {
			return new String(result.getPayload());
		}
		logger.error(
		        "[RPC-DeviceBind] failed! statusCode: {}; message: {}",
		        result.getStatusCode(), result.getMessage());
		return null;
	}

	private String unbind(final String aid, final String cid, final String did) {
		CharArrayBuffer buffer = new CharArrayBuffer(128);
		buffer.append("/1/api/subscription/unbind/");
		buffer.append(did);
		buffer.append("/");
		buffer.append(aid);
		buffer.append("/");
		buffer.append(cid);

		HttpRPCResult result = invokeDelete(composeURI(domain,
		        buffer.toString()));

		if (result.getStatusCode() == HttpStatus.SC_OK) {
			return "{\"status\":\"OK\"}";
		}
		logger.error(
		        "[RPC-DeviceUnbind] failed! statusCode: {}; message: {}",
		        result.getStatusCode(), result.getMessage());
		return null;
	}
}
