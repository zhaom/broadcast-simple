/**
 * 
 */
package com.babeeta.hudee.test.servlet.app.gateway;

import org.apache.http.client.methods.HttpGet;

import com.babeeta.hudee.test.rest.RestfulClient;

/**
 * 
 */
@SuppressWarnings("javadoc")
public class QueryClient extends RestfulClient {

	private final String ip;
	private final int port;
	private final String appId;

	/**
	 */
	public QueryClient(String ip, int port, String appId, String appKey) {
		super(appId, appKey);
		this.appId = appId;
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

	public void query(String msgId) {
		setHttpMethod(new HttpGet("/1/api/" + appId + "/query?id="
		        + msgId));
		execute();
	}
}
