/**
 * 
 */
package com.babeeta.hudee.test.rest.dev.subscription;

import org.apache.http.client.methods.HttpPut;

import com.babeeta.hudee.test.rest.RestfulClient;

/**
 * 
 */
public class NewBindClient extends RestfulClient {

	private final String ip;
	private final int port;

	/**
	 * @param ip
	 *            service host ip
	 * @param port
	 *            service port
	 */
	public NewBindClient(String ip, int port) {
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
	 * @param did
	 *            device id
	 */
	public void newBind(String aid, String did) {
		StringBuilder url = new StringBuilder(
		        "/1/api/subscription/newBind/").append(did)
		        .append("/").append(aid);

		setHttpMethod(new HttpPut(url.toString()));
		execute();
	}
}
