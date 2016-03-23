/**
 * 
 */
package com.babeeta.hudee.test.rest.dev.subscription.highlevel;

import org.apache.http.client.methods.HttpGet;

import com.babeeta.hudee.test.rest.RestfulClient;

/**
 * 
 */
public class CountResultClient extends RestfulClient {

	private final String ip;
	private final int port;

	/**
	 * @param ip
	 *            service host ip
	 * @param port
	 *            service port
	 */
	public CountResultClient(String ip, int port) {
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
	 * @param key
	 *            task key
	 */
	public void count(String key) {
		setHttpMethod(new HttpGet("/1/api/subscription/tags/count?key=" + key));
		execute();
	}
}
