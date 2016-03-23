/**
 * 
 */
package com.babeeta.hudee.test.rest.dev.subscription.highlevel;

import java.util.Date;

import org.apache.http.client.methods.HttpPost;

import com.babeeta.hudee.test.rest.RestfulClient;

/**
 * 
 */
public class CreateTaskClient extends RestfulClient {

	private final String ip;
	private final int port;

	/**
	 * @param ip
	 *            service host ip
	 * @param port
	 *            service port
	 */
	public CreateTaskClient(String ip, int port) {
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
	 */
	public void createTask(String aid) {
		setHttpMethod(new HttpPost("/1/api/subscription/tags/list?aid=" + aid
		        + "&stamp=" + new Date().getTime()));
		execute();
	}
}
