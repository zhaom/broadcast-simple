/**
 * 
 */
package com.babeeta.hudee.test.rest.dev.subscription.highlevel;

import org.apache.http.client.methods.HttpGet;

import com.babeeta.hudee.test.rest.RestfulClient;

/**
 * 
 */
public class ListResultClient extends RestfulClient {

	private final String ip;
	private final int port;

	/**
	 * @param ip
	 *            service host ip
	 * @param port
	 *            service port
	 */
	public ListResultClient(String ip, int port) {
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
	 * @param index
	 *            index
	 * @param pageSize
	 *            page size
	 */
	public void list(String key, int index, int pageSize) {
		setHttpMethod(new HttpGet("/1/api/subscription/tags/result?key=" + key
		        + "&pageSize=" + pageSize + "&pageIndex=" + index));
		execute();
	}
}
