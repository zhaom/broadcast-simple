/**
 * 
 */
package com.babeeta.hudee.test.rest.dev.message;

import org.apache.http.client.methods.HttpGet;

import com.babeeta.hudee.test.rest.RestfulClient;

/**
 * 
 */
public class MessageListClient extends RestfulClient {

	private final String ip;
	private final int port;

	/**
	 * @param ip
	 *            service host ip
	 * @param port
	 *            service port
	 */
	public MessageListClient(String ip, int port) {
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
	 * @param did
	 *            device id
	 * @param status
	 *            message status
	 */
	public void list(String did, String status) {
		setHttpMethod(new HttpGet("/1/api/message/list?device=" + did
		        + "&status=" + status));
		execute();
	}

	/**
	 * @param did
	 *            device id
	 * @param status
	 *            message status
	 * @param pageSize
	 *            page size
	 * @param pageIndex
	 *            page index
	 */
	public void list(String did, String status, int pageSize, int pageIndex) {
		setHttpMethod(new HttpGet("/1/api/message/list?device=" + did
		        + "&status=" + status + "&pageSize=" + pageSize + "&pageIndex="
		        + pageIndex));
		execute();
	}
}
