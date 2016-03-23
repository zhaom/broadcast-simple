/**
 * 
 */
package com.babeeta.hudee.test.rest.dev.subscription;

import org.apache.http.client.methods.HttpGet;

import com.babeeta.hudee.test.rest.RestfulClient;

/**
 * 
 */
public class QueryClient extends RestfulClient {

	private final String ip;
	private final int port;

	/**
	 * @param ip
	 *            service host ip
	 * @param port
	 *            service port
	 */
	public QueryClient(String ip, int port) {
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
	 * @param aid
	 *            application id
	 * @param cid
	 *            client id
	 */
	public void query(String aid, String cid) {
		setHttpMethod(new HttpGet("/1/api/subscription/query/" + aid + "/"
		        + cid));
		execute();
	}
}
