/**
 * 
 */
package com.babeeta.hudee.test.rest.dev.subscription.highlevel;

import org.apache.http.client.methods.HttpDelete;

import com.babeeta.hudee.test.rest.RestfulClient;

/**
 * 
 */
public class DeleteTaskClient extends RestfulClient {

	private final String ip;
	private final int port;

	/**
	 * @param ip
	 *            service host ip
	 * @param port
	 *            service port
	 */
	public DeleteTaskClient(String ip, int port) {
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
	public void deleteTask(String key) {
		setHttpMethod(new HttpDelete("/1/api/subscription/tags/result?key="
		        + key));
		execute();
	}
}
