/**
 * 
 */
package com.babeeta.hudee.test.rest.dev.message;

import org.apache.http.client.methods.HttpPut;

import com.babeeta.hudee.test.rest.RestfulClient;

/**
 * 
 */
public class MessageUpdateClient extends RestfulClient {

	private final String ip;
	private final int port;

	/**
	 * @param ip
	 *            service host ip
	 * @param port
	 *            service port
	 */
	public MessageUpdateClient(String ip, int port) {
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
	 * @param status
	 *            message status
	 */
	public void update(String msgId, String status) {
		setHttpMethod(new HttpPut("/1/api/message/" + msgId
		        + "/status/update?status=" + status));
		execute();
	}
}
