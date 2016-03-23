/**
 * 
 */
package com.babeeta.hudee.test.rest.dev.message;

import org.apache.http.client.methods.HttpGet;

import com.babeeta.hudee.test.rest.RestfulClient;

/**
 * 
 */
public class MessageQueryClient extends RestfulClient {

	private final String ip;
	private final int port;

	/**
	 * @param ip
	 *            service host ip
	 * @param port
	 *            service port
	 */
	public MessageQueryClient(String ip, int port) {
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
	 * @param msgId
	 *            message id
	 */
	public void query(String msgId) {
		setHttpMethod(new HttpGet("/1/api/message/status/query?id=" + msgId));
		execute();
	}
}
