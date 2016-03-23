package com.babeeta.hudee.gateway.device.service;

import org.apache.http.HttpStatus;
import org.apache.http.util.CharArrayBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the HTTP RPC of device auth
 * 
 * @author Xinyu
 * 
 */
public class DeviceAccountService extends AbstractHttpRPCService {
	private final String domain;
	private static final Logger logger = LoggerFactory
	        .getLogger(DeviceAccountService.class);

	/**
	 * Constructor
	 * 
	 * @param maxConnection
	 *            allow max quantity of connection
	 * @param domain
	 *            domain of device account service
	 */
	public DeviceAccountService(int maxConnection, String domain) {
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
	 * Do device auth
	 * 
	 * @param deviceId
	 *            device id
	 * @param deviceKey
	 *            device key
	 * 
	 * @return result in JSON
	 */
	public String doAuth(final String deviceId, final String deviceKey) {
		logger.debug("[RPC-DeviceAuth] deviceId = {}, deviceKey = {}",
		        new Object[] { deviceId, deviceKey });
		String ret = execute(new Callback() {
			@Override
			public String doIt() {
				return auth(deviceId, deviceKey);
			}
		});
		return ret;
	}

	/**
	 * Do device register
	 * 
	 * @param deviceInfo
	 *            device info submitted by client
	 * @return result in JSON
	 */
	public String doRegister(final String deviceInfo) {
		logger.debug("[RPC-DeviceRegister] device info = {}", deviceInfo);
		final byte[] payload = deviceInfo.getBytes();

		String ret = execute(new Callback() {
			@Override
			public String doIt() {
				return register(payload);
			}
		});

		return ret;
	}

	private String auth(final String did, final String key) {
		CharArrayBuffer buffer = new CharArrayBuffer(100);
		buffer.append("/1/api/account/dev/auth/");
		buffer.append(did);
		buffer.append("/");
		buffer.append(key);
		HttpRPCResult result = invokeGet(
		        composeURI(domain, buffer.toString()), HttpStatus.SC_OK);

		if (result.getStatusCode() == HttpStatus.SC_OK) {
			return new String(result.getPayload());
		}
		logger.error(
		        "[RPC-DeviceAuth] failed! statusCode: {}; message: {}",
		        result.getStatusCode(), result.getMessage());
		return null;
	}

	private String register(final byte[] payload) {
		HttpRPCResult result = invokePost(
		        composeURI(domain, "/1/api/account/dev/register"),
		        APPLICATION_JSON, payload, HttpStatus.SC_OK);

		if (result.getStatusCode() == HttpStatus.SC_OK) {
			return new String(result.getPayload());
		}
		logger.error(
		        "[RPC-DeviceRegister] failed! statusCode: {}; message: {}",
		        result.getStatusCode(), result.getMessage());
		return null;
	}
}
