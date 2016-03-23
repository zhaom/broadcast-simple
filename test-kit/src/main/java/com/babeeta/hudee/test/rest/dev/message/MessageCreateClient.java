/**
 * 
 */
package com.babeeta.hudee.test.rest.dev.message;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;

import com.babeeta.hudee.test.rest.RestfulClient;

/**
 * 
 */
public class MessageCreateClient extends RestfulClient {

	private final String ip;
	private final int port;

	/**
	 * @param ip
	 *            service host ip
	 * @param port
	 *            service port
	 */
	public MessageCreateClient(String ip, int port) {
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
	 * @param sender
	 *            aid
	 * @param recipient
	 *            cid
	 * @param deviceId
	 *            did
	 * @param content
	 *            content
	 * @param life
	 *            life in seconds
	 */
	public void create(String sender, String recipient, String deviceId,
	        String content, int life) {
		setHttpMethod(new HttpPost("/1/api/message/create/" + sender + "/"
		        + recipient + "/" + deviceId));
		setHttpContentType("application/octet-stream");
		setHeader("life", Integer.toString(life));

		ByteArrayEntity byteArray = null;
		byteArray = new ByteArrayEntity(content.getBytes());
		setHttpContent(byteArray);

		execute();
	}
}
