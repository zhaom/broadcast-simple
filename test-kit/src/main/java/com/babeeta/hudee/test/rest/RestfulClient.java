/**
 * 
 */
package com.babeeta.hudee.test.rest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * 
 */
public abstract class RestfulClient {
	private String appId;
	private String appKey;
	private HttpUriRequest httpUriRequest;
	private HttpEntity httpEntity;
	private HttpEntity responseEntity;
	private int responseStatus;

	/**
	 * @param appId
	 *            application id
	 * @param appKey
	 *            application key
	 */
	public RestfulClient(String appId, String appKey) {
		this.appId = appId;
		this.appKey = appKey;
	}

	/**
	 */
	public RestfulClient() {
		this.appId = null;
		this.appKey = null;
	}

	protected abstract String getIp();

	protected abstract int getPort();

	protected void execute() {
		HttpClient client = new DefaultHttpClient();
		try {
			setAuthHeader();

			if (httpUriRequest instanceof HttpEntityEnclosingRequestBase) {
				((HttpEntityEnclosingRequest) httpUriRequest)
						.setEntity(httpEntity);
			}

			System.out.println("http " + httpUriRequest.getMethod()
					+ " request url>>>");
			System.out.println(httpUriRequest.getURI());

			System.out.println("http request header>>>");
			for (Header header : httpUriRequest.getAllHeaders()) {
				System.out.println(header.getName() + ":" + header.getValue());
			}

			HttpResponse httpResponse = null;

			httpResponse = client.execute(new HttpHost(getIp(), getPort()),
					httpUriRequest);
			System.out.println("http response status>>>");
			System.out.println(httpResponse.getStatusLine());

			System.out.println("http response content>>>");
			responseEntity = httpResponse.getEntity();
			responseStatus = httpResponse.getStatusLine().getStatusCode();

		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return response status code
	 */
	public int getResponseStatus() {
		return responseStatus;
	}

	/**
	 * @return response entity
	 */
	public HttpEntity getResponseEntity() {
		return responseEntity;
	}

	/**
	 * @param httpEntity
	 *            HTTP request entity
	 */
	public void setHttpContent(HttpEntity httpEntity) {
		this.httpEntity = httpEntity;
	}

	/**
	 * @param contentType
	 *            content type
	 */
	public void setHttpContentType(String contentType) {
		httpUriRequest.addHeader("Content-type", contentType);
	}

	/**
	 * @param key
	 *            header name
	 * @param value
	 *            header content
	 */
	public void setHeader(String key, String value) {
		httpUriRequest.addHeader(key, value);
	}

	/**
	 * @param httpUriRequest
	 *            HTTP request object
	 */
	public void setHttpMethod(HttpUriRequest httpUriRequest) {
		this.httpUriRequest = httpUriRequest;
	}

	protected void setAppId(String appId) {
		this.appId = appId;
	}

	protected void setAppKey(String appKey) {
		this.appKey = appKey;
	}

	/**
	 * @return the appId
	 */
	protected String getAppId() {
		return appId;
	}

	/**
	 * @return the appKey
	 */
	protected String getAppKey() {
		return appKey;
	}

	private void setAuthHeader() throws UnsupportedEncodingException {
		if (appId != null && appKey != null) {
			byte[] token = null;
			String authorization = null;
			try {
				token = (appId + ":" + appKey).getBytes("utf-8");
				authorization = "Basic "
						+ new String(Base64.encodeBase64(token), "utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			httpUriRequest.addHeader("Authorization", authorization);
		}
	}
}