package com.babeeta.hudee.test.rest.dev.account;

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
public class DeviceRegisterClient extends RestfulClient {

	private final String ip;
	private final int port;

	/**
	 * @param ip
	 *            service host ip
	 * @param port
	 *            service port
	 */
	public DeviceRegisterClient(String ip, int port) {
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
	 * @param imei
	 *            imei
	 */
	public void register(String imei) {
		Map<String, Object> info = new HashMap<String, Object>();
		info.put("imei", imei);
		String contentStr = JSONObject.fromObject(info).toString();

		setHttpMethod(new HttpPost("/1/api/account/dev/register"));
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
