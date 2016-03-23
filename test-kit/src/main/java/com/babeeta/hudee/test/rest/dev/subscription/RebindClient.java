/**
 * 
 */
package com.babeeta.hudee.test.rest.dev.subscription;

import org.apache.http.client.methods.HttpPut;

import com.babeeta.hudee.test.rest.RestfulClient;

/**
 * 
 */
public class RebindClient extends RestfulClient {

	private final String ip;
	private final int port;

	/**
	 * @param ip
	 *            service host ip
	 * @param port
	 *            service port
	 */
	public RebindClient(String ip, int port) {
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
	 *            old client id
	 * @param did
	 *            device id
	 */
	public void rebind(String aid, String cid, String did) {
		StringBuilder url = new StringBuilder(
		        "/1/api/subscription/rebind/").append(did)
		        .append("/").append(aid).append("/").append(cid);

		setHttpMethod(new HttpPut(url.toString()));
		execute();
	}
}
