/**
 * 
 */
package com.babeeta.hudee.test.servlet.app.gateway;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;

import com.babeeta.hudee.test.rest.RestfulClient;

/**
 * 
 */
@SuppressWarnings("javadoc")
public class PushClient extends RestfulClient {

	private final String ip;
	private final int port;
	private final String appId;

	/**
	 */
	public PushClient(String ip, int port, String appId, String appKey) {
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

	private static String pattern = "{\"life\" : \"%d\", \"payload\" : \"%s\"}";

	public void push(int life, String content) {

		String request = String.format(pattern, life, content);

		setHttpMethod(new HttpPost("/1/api/" + appId + "/push/broadcast"));
		setHttpContentType("application/json");

		ByteArrayEntity byteArray = null;
		byteArray = new ByteArrayEntity(request.getBytes());
		setHttpContent(byteArray);

		execute();
	}
}
