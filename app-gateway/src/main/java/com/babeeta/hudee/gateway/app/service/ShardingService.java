package com.babeeta.hudee.gateway.app.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;

import com.babeeta.hudee.gateway.app.entity.ShardingMessageContext;

/**
 * implement the interface of authenticate service
 */
public class ShardingService extends AbstractHttpRPCService {
	private final String domain;
	private static String submitPattern = "/1/api/%s/push/group?id=%s&parent=%s&pageSize=%d&pageIndex=%d";
	private final String SUPER_USERNAME;
	private final String SUPER_PASSWORD;

	/**
	 * Constructor
	 * 
	 * @param maxThread
	 *            max executor threads
	 * @param domain
	 *            domain of app gateway
	 * 
	 * @param superUser
	 *            internal use super user name
	 * @param superPwd
	 *            internal use super user password
	 */
	public ShardingService(int maxThread, String domain, String superUser,
	        String superPwd) {
		super(maxThread);
		this.domain = domain;
		this.SUPER_USERNAME = superUser;
		this.SUPER_PASSWORD = superPwd;
	}

	private interface Callback {
		int doIt();
	}

	private int execute(Callback callback) {
		return callback.doIt();
	}

	/**
	 * Submit mutlicast sub task to app-gateway
	 * 
	 * @param sctx
	 *            sharding message context
	 * @param pageIndex
	 *            index
	 * @return response status code
	 */
	public int submit(final ShardingMessageContext sctx, final int pageIndex) {
		int ret = execute(new Callback() {
			@Override
			public int doIt() {
				return doSubmit(sctx, pageIndex);
			}
		});
		return ret;
	}

	private String encodeAuthInfo(String aid, String key) {
		if (aid != null && key != null) {
			byte[] token = null;
			token = (aid + ":" + key).getBytes();
			return "Basic " + new String(Base64.encodeBase64(token));
		}
		return null;
	}

	private int doSubmit(final ShardingMessageContext sctx,
	        final int pageIndex) {

		Map<String, String> header = new HashMap<String, String>();
		header.put(HttpHeaders.AUTHORIZATION,
		        encodeAuthInfo(SUPER_USERNAME, SUPER_PASSWORD));
		header.put(HttpHeaders.CONTENT_TYPE, "application/octet-stream");

		header.put("life", Integer.toString(sctx.getLife()));

		String uri = composeURI(
		        domain,
		        String.format(submitPattern, sctx.getAid(),
		                sctx.getTaskId(), sctx.getParentId(),
		                sctx.getPageSize(), pageIndex));
		HttpRPCResult result = invokePost(uri, header, sctx.getContent(),
		        HttpStatus.SC_OK);
		return result.getStatusCode();
	}
}
