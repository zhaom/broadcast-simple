/**
 * 
 */
package com.babeeta.hudee.test.rest.dev.subscription;

import org.apache.http.client.methods.HttpGet;

import com.babeeta.hudee.test.rest.RestfulClient;

/**
 * 
 */
public class ListClient extends RestfulClient {

	private final String ip;
	private final int port;

	/**
	 * @param ip
	 *            service host ip
	 * @param port
	 *            service port
	 */
	public ListClient(String ip, int port) {
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
	 * @param did
	 *            device id
	 */
	public void list(String did) {
		setHttpMethod(new HttpGet("/1/api/subscription/list/" + did));
		execute();
	}
}
