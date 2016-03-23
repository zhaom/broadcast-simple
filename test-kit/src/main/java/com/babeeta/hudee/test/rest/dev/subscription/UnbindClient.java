/**
 * 
 */
package com.babeeta.hudee.test.rest.dev.subscription;

import org.apache.http.client.methods.HttpDelete;

import com.babeeta.hudee.test.rest.RestfulClient;

/**
 * 
 */
public class UnbindClient extends RestfulClient {

	private final String ip;
	private final int port;

	/**
	 * @param ip
	 *            service host ip
	 * @param port
	 *            service port
	 */
	public UnbindClient(String ip, int port) {
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
	 * @param did
	 *            device id
	 */
	public void unbind(String aid, String cid, String did) {
		setHttpMethod(new HttpDelete("/1/api/subscription/unbind/" + did + "/"
		        + aid + "/" + cid));
		execute();
	}
}
