/**
 * 
 */
package com.babeeta.hudee.test.rest.dev.account;

import org.apache.http.client.methods.HttpGet;

import com.babeeta.hudee.test.rest.RestfulClient;

/**
 * 
 */
public class DeviceAuthClient extends RestfulClient {

	private final String ip;
	private final int port;

	/**
	 * @param ip
	 *            service host ip
	 * @param port
	 *            service port
	 */
	public DeviceAuthClient(String ip, int port) {
		super();
		this.ip = ip;
		this.port = port;
	}

	@Override
	protected String getIp() {
		return ip;
	}

	@Override
	protected int getPort() {
		return port;
	}

	/**
	 * @param id
	 *            device id
	 * @param key
	 *            device key
	 */
	public void auth(String id, String key) {
		setHttpMethod(new HttpGet("/1/api/account/dev/auth/" + id + "/" + key));
		execute();
	}
}
