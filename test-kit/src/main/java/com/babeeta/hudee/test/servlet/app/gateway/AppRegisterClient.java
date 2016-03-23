/**
 * 
 */
package com.babeeta.hudee.test.servlet.app.gateway;

import java.io.UnsupportedEncodingException;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;

import com.babeeta.hudee.test.rest.RestfulClient;

/**
 * 
 */
@SuppressWarnings("javadoc")
public class AppRegisterClient extends RestfulClient {

	private final String ip;
	private final int port;

	/**
	 */
	public AppRegisterClient(String ip, int port, String superUsername,
	        String superPassword) {
		super(superUsername, superPassword);
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

	public void register(String description, String email) {
		StringBuilder content = new StringBuilder();

		// content.append("{\"extra\":{")
		// .append("\"description\":\"").append(description)
		// .append("\",\"email\":\"").append(email).append("\"}}");

		content
		        .append("{\"description\":\"").append(description)
		        .append("\",\"email\":\"").append(email).append("\"}");

		setHttpMethod(new HttpPost("/internal/AppInfo/generate"));
		setHttpContentType("application/json;charset=UTF-8");

		ByteArrayEntity byteArray = null;
		try {
			String contentStr = content.toString();
			byteArray = new ByteArrayEntity(contentStr.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		setHttpContent(byteArray);

		execute();
	}

	public void register(String JSON) {
		setHttpMethod(new HttpPost("/internal/AppInfo/generate"));
		setHttpContentType("application/json;charset=UTF-8");

		ByteArrayEntity byteArray = null;
		try {
			// String contentStr = content.toString();
			byteArray = new ByteArrayEntity(JSON.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		setHttpContent(byteArray);

		execute();
	}
}
