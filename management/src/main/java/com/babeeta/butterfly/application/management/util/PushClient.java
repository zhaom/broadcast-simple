/**
 * 
 */
package com.babeeta.butterfly.application.management.util;

import java.util.ResourceBundle;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;


/**
 * 
 */
@SuppressWarnings("javadoc")
public class PushClient extends RestfulClient {

	private static final String ip;
	private static final int port;
	private final String appId;

	static{
		ResourceBundle rb = ResourceBundle.getBundle("appmaster");
		ip = rb.getString("pushHost");
		port = Integer.parseInt(rb.getString("pushPort"));
	}
	
	public PushClient(String appId, String appKey){
		super(appId, appKey);
		this.appId = appId;
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
