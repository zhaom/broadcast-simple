/**
 * 
 */
package com.babeeta.hudee.gateway.device.service;

import java.io.IOException;
import java.net.SocketException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.CharArrayBuffer;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * abstract class of RPC service
 * 
 */
public abstract class AbstractHttpRPCService {
	protected static final Logger logger = LoggerFactory
	        .getLogger(AbstractHttpRPCService.class);

	protected static final String APPLICATION_JSON = "application/json;charset=UTF-8";
	private long keepAliveDuration = 300000;
	private DefaultHttpClient client;
	private ThreadSafeClientConnManager httpClientConnectionManager;
	private final int MAX_CONNECTION;
	private final static int CONNECT_TIMEOUT = 10000;
	private final static int READ_TIMEOUT = 10000;

	private final ThreadLocal<DefaultHttpClient> localHttpClient = new ThreadLocal<DefaultHttpClient>() {
		@Override
		public DefaultHttpClient initialValue() {
			SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register(
			        new Scheme("http", 80, PlainSocketFactory
			                .getSocketFactory()));
			httpClientConnectionManager = new ThreadSafeClientConnManager(
			        schemeRegistry);

			httpClientConnectionManager.setMaxTotal(MAX_CONNECTION);
			httpClientConnectionManager.setDefaultMaxPerRoute(MAX_CONNECTION);

			HttpParams params = new BasicHttpParams();
			params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION,
			        HttpVersion.HTTP_1_1);
			params.setParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE,
			        false);
			params.setParameter(CoreProtocolPNames.WAIT_FOR_CONTINUE, 0);
			params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,
			        CONNECT_TIMEOUT);
			params.setParameter(CoreConnectionPNames.SO_TIMEOUT, READ_TIMEOUT);

			client = new DefaultHttpClient(
			        httpClientConnectionManager, params);
			client.setHttpRequestRetryHandler(httpRequestRetryHandler);
			client.setKeepAliveStrategy(connectionKeepAliveStrategy);
			localHttpClient.set(client);
			return client;
		}
	};

	private ConnectionKeepAliveStrategy connectionKeepAliveStrategy =
	        new DefaultConnectionKeepAliveStrategy() {
		        @Override
		        public long getKeepAliveDuration(
		                HttpResponse response,
		                HttpContext context) {
			        long keepAlive = super.getKeepAliveDuration(response,
			                context);
			        if (keepAlive == -1) {
				        keepAlive = keepAliveDuration;
			        }
			        return keepAlive;
		        }
	        };
	private HttpRequestRetryHandler httpRequestRetryHandler =
	        new HttpRequestRetryHandler() {
		        public boolean retryRequest(
		                IOException exception,
		                int executionCount,
		                HttpContext context) {
			        HttpRequest httpRequest = (HttpRequest) context
			                .getAttribute(ExecutionContext.HTTP_REQUEST);
			        String uri = httpRequest.getRequestLine().getUri();
			        String methodName = httpRequest.getRequestLine()
			                .getMethod();

			        if (executionCount >= 5) {
				        // Do not retry if over max retry count
				        logger.error(
				                "[{}] [{}] [{}] [{}] have been retry 5 times,stop retry.",
				                new Object[] { uri, methodName,
				                        exception.getClass().getName(),
				                        exception.getMessage() });
				        return false;
			        }
			        if (exception instanceof NoHttpResponseException) {
				        // Retry if the server dropped connection on us
				        logger.error(
				                "[{}] [{}] [{}] [{}] http retry",
				                new Object[] { uri, methodName,
				                        exception.getClass().getName(),
				                        exception.getMessage() });
				        return true;
			        }
			        if ((exception instanceof SocketException)
			                && exception.getMessage()
			                        .startsWith("Connection reset")) {
				        logger.error(
				                "[{}] [{}] [{}] [{}] Connection reset retry",
				                new Object[] { uri, methodName,
				                        exception.getClass().getName(),
				                        exception.getMessage() });
				        return true;
			        }

			        boolean idempotent = !(httpRequest instanceof HttpEntityEnclosingRequest);
			        if (idempotent) {
				        // Retry if the request is considered idempotent
				        logger.error("[{}] [{}] idempotent retry",
				                new Object[] {
				                        uri, methodName });
				        return true;
			        }
			        return false;
		        }
	        };

	private DefaultHttpClient getHttpClient() {
		DefaultHttpClient client = localHttpClient.get();
		if (client == null) {
			SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register(
			        new Scheme("http", 80, PlainSocketFactory
			                .getSocketFactory()));
			httpClientConnectionManager = new ThreadSafeClientConnManager(
			        schemeRegistry);

			httpClientConnectionManager.setMaxTotal(MAX_CONNECTION);
			httpClientConnectionManager.setDefaultMaxPerRoute(MAX_CONNECTION);

			HttpParams params = new BasicHttpParams();
			params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION,
			        HttpVersion.HTTP_1_1);
			params.setParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE,
			        false);
			params.setParameter(CoreProtocolPNames.WAIT_FOR_CONTINUE, 0);
			params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,
			        CONNECT_TIMEOUT);
			params.setParameter(CoreConnectionPNames.SO_TIMEOUT, READ_TIMEOUT);

			client = new DefaultHttpClient(
			        httpClientConnectionManager, params);
			client.setHttpRequestRetryHandler(httpRequestRetryHandler);
			client.setKeepAliveStrategy(connectionKeepAliveStrategy);
			localHttpClient.set(client);
		}
		return client;
	}

	/**
	 * Constructor
	 * 
	 * @param maxConnection
	 *            allow max quantity of connection
	 */
	protected AbstractHttpRPCService(int maxConnection) {
		MAX_CONNECTION = maxConnection;
	}

	/**
	 * @param httpUriRequest
	 *            HTTP request
	 * @return HTTP response
	 * @throws ClientProtocolException
	 *             client version exception
	 * @throws IOException
	 *             socket I/O exception
	 */
	private HttpResponse invoke(HttpUriRequest httpUriRequest)
	        throws ClientProtocolException, IOException {
		return getHttpClient().execute(httpUriRequest);
	}

	/**
	 * 
	 */
	public void destroy() {
		if (getHttpClient() != null) {
			getHttpClient().getConnectionManager().shutdown();
		}
	}

	private byte[] convertEntity(final HttpEntity entity) throws IOException {
		byte[] result = null;
		if (entity != null) {
			result = EntityUtils.toByteArray(entity);
		}

		return result;
	}

	private HttpRPCResult invokeRPC(String requestUrl, String method,
	        String payloadType, byte[] requestContent, int expectantStatus) {
		HttpRPCResult result = null;
		HttpEntityEnclosingRequestBase request = null;
		if (method.equalsIgnoreCase("Post") || method.equalsIgnoreCase("Put")) {
			if (method.equalsIgnoreCase("Post")) {
				request = new HttpPost(requestUrl);
			} else {
				request = new HttpPut(requestUrl);
			}
			if (payloadType != null && payloadType.length() > 0) {
				request.setHeader(HttpHeaders.CONTENT_TYPE, payloadType);
			}
			if (requestContent != null) {
				request.setEntity(new ByteArrayEntity(requestContent));
			}
			HttpResponse httpResponse = null;
			try {
				httpResponse = invoke(request);
				int status = httpResponse.getStatusLine().getStatusCode();
				HttpEntity entity = httpResponse.getEntity();
				if (status == expectantStatus) {
					byte[] payload = convertEntity(entity);
					result = new HttpRPCResult(status, "Okay", payload);
				} else {
					result = new HttpRPCResult(status,
					        "Unexpectant response status", null);
				}
			} catch (IOException e) {
				result = new HttpRPCResult(-1,
				        "Error occured while executing HTTP RPC: "
				                + e.getMessage(), null);
				e.printStackTrace();
			} finally {
				try {
					if (httpResponse != null
					        && httpResponse.getEntity() != null) {
						EntityUtils.consume(httpResponse.getEntity());
					}
				} catch (IOException e) {
					result = new HttpRPCResult(-1, "Consume entity failed: "
					        + e.getMessage(), null);
					e.printStackTrace();
				}
				request.abort();
			}
		} else {
			result = new HttpRPCResult(-1, "Illegal HTTP method", null);
		}

		return result;
	}

	private HttpRPCResult invokeRPC(String requestUrl, String method,
	        int expectantStatus) {
		HttpRPCResult result = null;
		HttpRequestBase request = null;
		if (method.equalsIgnoreCase("Get") || method.equalsIgnoreCase("Delete")) {
			if (method.equalsIgnoreCase("Delete")) {
				request = new HttpDelete(requestUrl);
			} else {
				request = new HttpGet(requestUrl);
			}

			HttpResponse httpResponse = null;
			try {
				httpResponse = invoke(request);
				int status = httpResponse.getStatusLine().getStatusCode();
				HttpEntity entity = httpResponse.getEntity();
				if (status == expectantStatus) {
					if (method.equalsIgnoreCase("Get")) {
						byte[] payload = convertEntity(entity);
						result = new HttpRPCResult(status, "Okay", payload);
					} else {
						result = new HttpRPCResult(status, "Okay", null);
					}
				} else {
					result = new HttpRPCResult(status,
					        "Unexpectant response status", null);
				}
			} catch (IOException e) {
				result = new HttpRPCResult(-1,
				        "Error occured while executing HTTP RPC: "
				                + e.getMessage(),
				        null);
				e.printStackTrace();
			} finally {
				try {
					if (httpResponse != null
					        && httpResponse.getEntity() != null) {
						EntityUtils.consume(httpResponse.getEntity());
					}
				} catch (IOException e) {
					result = new HttpRPCResult(-1, "Consume entity failed: "
					        + e.getMessage(), null);
					e.printStackTrace();
				}
				request.abort();
			}
		} else {
			result = new HttpRPCResult(-1, "Illegal HTTP method", null);
		}

		return result;
	}

	/**
	 * Execute HTTP RPC with "Post" method
	 * 
	 * @param URI
	 *            full URI included host and request path
	 * @param payloadType
	 *            entity type of request
	 * @param requestContent
	 *            entity of request
	 * @param expectantStatus
	 *            expectant response status code
	 * @return HTTP RPC result object
	 */
	protected HttpRPCResult invokePost(String URI, String payloadType,
	        byte[] requestContent, int expectantStatus) {
		return invokeRPC(URI, "POST", payloadType, requestContent,
		        expectantStatus);
	}

	/**
	 * Execute HTTP RPC with "Put" method
	 * 
	 * @param URI
	 *            full URI included host and request path
	 * @param payloadType
	 *            entity type of request
	 * @param requestContent
	 *            entity of request
	 * @param expectantStatus
	 *            expectant response status code
	 * @return HTTP RPC result object
	 */
	protected HttpRPCResult invokePut(String URI, String payloadType,
	        byte[] requestContent, int expectantStatus) {
		return invokeRPC(URI, "PUT", payloadType, requestContent,
		        expectantStatus);
	}

	/**
	 * Execute HTTP RPC with "Delete" method
	 * 
	 * @param URI
	 *            full URI included host and request path
	 * @return HTTP RPC result object
	 */
	protected HttpRPCResult invokeDelete(String URI) {
		return invokeRPC(URI, "DELETE", 200);
	}

	/**
	 * Execute HTTP RPC with "Get" method
	 * 
	 * @param URI
	 *            full URI included host and request path
	 * @param expectantStatus
	 *            expectant response status code
	 * @return HTTP RPC result object
	 */
	protected HttpRPCResult invokeGet(String URI, int expectantStatus) {
		return invokeRPC(URI, "GET", expectantStatus);
	}

	private String composeURI(final String scheme, final String hostname,
	        final int port, final String path) {
		CharArrayBuffer buffer = new CharArrayBuffer(128);
		buffer.append(scheme);
		buffer.append("://");
		buffer.append(hostname);
		if (port != -1) {
			buffer.append(':');
			buffer.append(Integer.toString(port));
		}
		buffer.append(path);
		return buffer.toString();
	}

	protected String composeURI(String hostname, String path) {
		return composeURI("http", hostname, -1, path);
	}

	protected String composeURI(String hostname, int port, String path) {
		return composeURI("http", hostname, port, path);
	}

	/**
	 * Result of HTTP RPC service
	 * 
	 * @author Xinyu
	 * 
	 */
	protected class HttpRPCResult {
		private int statusCode;
		private byte[] payload;
		private String message;

		/**
		 * Constructor
		 * 
		 * @param statusCode
		 *            HTTP status code
		 * @param message
		 *            message of exception or others
		 * @param payload
		 *            entity of response
		 */
		public HttpRPCResult(int statusCode, String message, byte[] payload) {
			this.statusCode = statusCode;
			this.message = message;
			this.payload = payload;
		}

		/**
		 * @return the statusCode
		 */
		public int getStatusCode() {
			return statusCode;
		}

		/**
		 * @param statusCode
		 *            the statusCode to set
		 */
		public void setStatusCode(int statusCode) {
			this.statusCode = statusCode;
		}

		/**
		 * @return the payload
		 */
		public byte[] getPayload() {
			return payload;
		}

		/**
		 * @param payload
		 *            the payload to set
		 */
		public void setPayload(byte[] payload) {
			this.payload = payload;
		}

		/**
		 * @return the message
		 */
		public String getMessage() {
			return message;
		}

		/**
		 * @param message
		 *            the message to set
		 */
		public void setMessage(String message) {
			this.message = message;
		}
	}

	/**
	 * @return
	 */
	protected int getConnectionInPool() {
		return httpClientConnectionManager.getConnectionsInPool();
	}
}