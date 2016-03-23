package com.babeeta.hudee.gateway.app.service;

import java.util.Date;
import java.util.List;

import net.sf.json.JSONArray;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * implement the interface of authenticate service
 */
public class SubscriptionService extends AbstractHttpRPCService {
	private static final Logger logger = LoggerFactory
	        .getLogger(SubscriptionService.class);
	private final String domain;
	private static String queryPattern = "/1/api/subscription/query/%s/%s";
	private static String createGroupTaskPattern = "/1/api/subscription/tags/list?aid=%s&stamp=%s&life=0";
	private static String checkTaskPattern = "/1/api/subscription/tags/count?key=%s";
	private static String getResultPattern = "/1/api/subscription/tags/result?key=%s&pageSize=%d&pageIndex=%d";

	/**
	 * Constructor
	 * 
	 * @param maxConnection
	 *            max connection limit
	 * 
	 * @param domain
	 *            subscription service domain
	 */
	public SubscriptionService(int maxConnection, String domain) {
		super(maxConnection);
		this.domain = domain;
	}

	private interface Callback {
		String doIt();
	}

	private String execute(Callback callback) {
		return callback.doIt();
	}

	private interface CallbackLong {
		long doIt();
	}

	private long execute(CallbackLong callback) {
		return callback.doIt();
	}

	/**
	 * Query did by aid and cid
	 * 
	 * @param aid
	 *            application id
	 * @param cid
	 *            client id
	 * @return return device id if subscription of aid and cid exists, otherwise
	 *         return null
	 */
	public String getDeviceId(final String aid, final String cid) {
		String ret = execute(new Callback() {
			@Override
			public String doIt() {
				return queryDeviceId(aid, cid);
			}
		});
		return ret;
	}

	/**
	 * Create a query task for a group described by tag expression
	 * 
	 * @param aid
	 *            application id
	 * @param tagExpression
	 *            tag expression
	 * @return key of task or null
	 */
	public String createGroupTask(final String aid, final String tagExpression) {
		String ret = execute(new Callback() {
			@Override
			public String doIt() {
				return createGroup(aid, tagExpression, new Date().getTime());
			}
		});
		return ret;
	}

	/**
	 * @param key
	 *            task key
	 * @return count of group if task finished
	 */
	public long checkGroupTask(final String key) {
		long ret = execute(new CallbackLong() {
			@Override
			public long doIt() {
				return checkTask(key);
			}
		});
		return ret;
	}

	/**
	 * Get query task result
	 * 
	 * @param key
	 *            task key
	 * @param pageSize
	 *            page size
	 * @param pageIndex
	 *            page index
	 * @return a list of device key or null
	 */
	public List<String> getTaskResult(final String key, final int pageSize,
	        final int pageIndex) {
		String ret = execute(new Callback() {
			@Override
			public String doIt() {
				return queryTaskResult(key, pageIndex, pageSize);
			}
		});

		if (ret != null) {
			JSONArray obj = JSONArray.fromObject(ret);
			@SuppressWarnings("unchecked")
			List<String> list = (List<String>) JSONArray.toCollection(obj,
			        String.class);
			return list;
		}
		return null;
	}

	private String queryDeviceId(final String aid, final String cid) {
		HttpRPCResult result = invokeGet(
		        composeURI(domain,
		                String.format(queryPattern, aid, cid)),
		        HttpStatus.SC_OK);
		String retResult = null;
		if (result.getStatusCode() == HttpStatus.SC_OK) {
			String returnStr = new String(result.getPayload());

			if (returnStr != null && returnStr.length() > 0
			        && returnStr.length() == 32) {
				retResult = returnStr;
			} else {
				logger.error("[RPC-queryDid] Fatal error! Remote return null.");
			}
		} else {
			logger.error(
			        "[RPC-queryDid] failed! statusCode: {}; message: {}",
			        result.getStatusCode(), result.getMessage());
		}
		return retResult;
	}

	private String createGroup(final String aid, final String tagExpression,
	        final long stamp) {
		HttpRPCResult result = invokePost(composeURI(domain,
		        String.format(createGroupTaskPattern, aid, stamp)), null,
		        tagExpression.getBytes(), HttpStatus.SC_OK);
		String retResult = null;
		if (result.getStatusCode() == HttpStatus.SC_OK) {
			String returnStr = new String(result.getPayload());

			if (returnStr != null && returnStr.length() > 0
			        && returnStr.length() == 32) {
				retResult = returnStr;
			} else {
				logger.error("[RPC-createGroup] Fatal error! Remote return null.");
			}
		} else {
			logger.error(
			        "[RPC-createGroup] failed! statusCode: {}; message: {}",
			        result.getStatusCode(), result.getMessage());
		}
		return retResult;
	}

	private long checkTask(final String key) {
		HttpRPCResult result = invokeGet(composeURI(domain,
		        String.format(checkTaskPattern, key)), HttpStatus.SC_OK);
		if (result.getStatusCode() == HttpStatus.SC_OK) {
			String returnStr = new String(result.getPayload());
			return Long.parseLong(returnStr);
		} else if (result.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
			return Long.MIN_VALUE;
		} else {
			logger.error(
			        "[RPC-checkTask] failed! statusCode: {}; message: {}",
			        result.getStatusCode(), result.getMessage());
		}
		return -1;
	}

	private String queryTaskResult(final String key, final int pageIndex,
	        final int pageSize) {
		HttpRPCResult result = invokeGet(
		        composeURI(domain,
		                String.format(getResultPattern, key, pageSize,
		                        pageIndex)),
		        HttpStatus.SC_OK);
		String retResult = null;
		if (result.getStatusCode() == HttpStatus.SC_OK) {
			retResult = new String(result.getPayload());
		} else {
			logger.error(
			        "[RPC-queryDid] failed! statusCode: {}; message: {}",
			        result.getStatusCode(), result.getMessage());
		}
		return retResult;
	}
}
