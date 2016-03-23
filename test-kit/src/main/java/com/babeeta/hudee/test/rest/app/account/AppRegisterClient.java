/**
 * 
 */
package com.babeeta.hudee.test.rest.app.account;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;

import com.babeeta.hudee.test.rest.RestfulClient;

/**
 * 
 */
public class AppRegisterClient extends RestfulClient {
	private final String ip;
	private final int port;

	/**
	 * RESTful HTTP client for test register feature in application account
	 * service
	 * 
	 * @param ip
	 *            host of web service
	 * @param port
	 *            port of web service
	 */
	public AppRegisterClient(String ip, int port) {
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
	 * @param description
	 *            application description
	 * @param email
	 *            email
	 */
	public void register(String description, String email) {
		Map<String, Object> info = new HashMap<String, Object>();
		info.put("description", description);
		info.put("email", email);

		String contentStr = JSONObject.fromObject(info).toString();

		setHttpMethod(new HttpPost("/1/api/account/app/register"));
		setHttpContentType("application/json;charset=UTF-8");
		ByteArrayEntity byteArray = null;
		try {
			byteArray = new ByteArrayEntity(contentStr.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		setHttpContent(byteArray);

		execute();
	}
}
