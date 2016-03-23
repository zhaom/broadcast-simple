/**
 * 
 */
package com.babeeta.hudee.test.rest.app.account;

import org.apache.http.client.methods.HttpGet;

import com.babeeta.hudee.test.rest.RestfulClient;

/**
 * 
 */
public class AppAuthClient extends RestfulClient {

	private final String ip;
	private final int port;

	/**
	 * RESTful HTTP client for test auth feature in application account service
	 * 
	 * @param ip
	 *            host of web service
	 * @param port
	 *            port of web service
	 */
	public AppAuthClient(String ip, int port) {
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
	 * Do auth
	 * 
	 * @param appId
	 *            application id
	 * @param appKey
	 *            application key
	 */
	public void auth(String appId, String appKey) {
		setHttpMethod(new HttpGet("/1/api/account/app/auth/" + appId + "/"
		        + appKey));
		execute();
	}

}
