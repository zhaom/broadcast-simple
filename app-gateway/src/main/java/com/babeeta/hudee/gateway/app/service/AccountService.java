package com.babeeta.hudee.gateway.app.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.hudee.common.memcache.MemoryCache;

/**
 * implement the interface of authenticate service
 */
public class AccountService extends AbstractHttpRPCService {
	private static final Logger logger = LoggerFactory
	        .getLogger(AccountService.class);

	private final String SUPER_USERNAME;
	private final String SUPER_PASSWORD;

	private final String domain;
	private final MemoryCache cache;
	private static String Succeed = "SUCCESS";
	private static String Failed = "FAIL";
	private static String uriAuthPattern = "/1/api/account/app/auth/%s/%s";
	private static String uriRegisterPattern = "/1/api/account/app/register";
	private static String authResultFormat = "{\"status\":\"%s\", \"Message\":\"%s\"}";
	private static String registerFormat = "{\"email\":\"%s\", \"description\":\"%s\", \"others\":\"%s\"}";

	/**
	 * Constructor
	 * 
	 * @param maxConnection
	 *            max connection limit
	 * 
	 * @param domain
	 *            account service domain
	 * @param superUser
	 *            internal use super user name
	 * @param superPwd
	 *            internal use super user password
	 */
	public AccountService(int maxConnection, String domain, String superUser,
	        String superPwd) {
		super(maxConnection);
		this.domain = domain;
		this.SUPER_USERNAME = superUser;
		this.SUPER_PASSWORD = superPwd;
		cache = new MemoryCache("AppAuth", 100, 60 * 1000);
	}

	private interface Callback {
		String doIt();
	}

	private String execute(Callback callback) {
		return callback.doIt();
	}

	private String composeAuthResult(boolean isSuccess, String message) {
		String result = null;
		if (isSuccess) {
			result = Succeed;
		} else {
			result = Failed;
		}

		if (message == null) {
			message = "null";
		}

		return String.format(authResultFormat, result, message);
	}

	private static String keywordStatus = "status";
	private static String keywordStatusOk = "OK";
	private static String keywordStatusFreezed = "FREEZED";

	/**
	 * do authenticate
	 * 
	 * @param appId
	 *            application id
	 * @param appKey
	 *            application key
	 * @return report for authenticate
	 */
	public String authenticate(final String appId, final String appKey) {
		if (appId.equals(SUPER_USERNAME) && appKey.equals(SUPER_PASSWORD)) {
			return composeAuthResult(true, "Used super user");
		} else {
			// cache code
			{
				Object result = cache.resolve(appId);

				if (result != null
				        && result.toString().equalsIgnoreCase(appKey)) {
					logger.debug("[RPC-authenticate] {}.{} in cache.",
					        appId, appKey);
					return composeAuthResult(true, null);
				}
			}
			String ret = execute(new Callback() {
				@Override
				public String doIt() {
					return doAuth(appId, appKey);
				}
			});
			return ret;
		}
	}

	/**
	 * Register a new application account
	 * 
	 * @param email
	 *            user info
	 * @param description
	 *            user info
	 * @param others
	 *            user info
	 * @return result of register
	 */
	public String register(final String email, final String description,
	        final String others) {
		final byte[] payload = String.format(registerFormat, email,
		        description, others).getBytes();

		String ret = execute(new Callback() {
			@Override
			public String doIt() {
				return doRegister(payload);
			}
		});

		return ret;
	}

	private String doRegister(final byte[] payload) {
		Map<String, String> header = new HashMap<String, String>();
		header.put(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON);
		HttpRPCResult result = invokePost(
		        composeURI(domain, uriRegisterPattern),
		        header, payload, HttpStatus.SC_OK);
		String retResult = null;
		if (result.getStatusCode() == HttpStatus.SC_OK) {
			String returnStr = new String(result.getPayload());
			logger.debug("[RPC-register] Success! {}",
			        returnStr);
			retResult = returnStr;
		} else {
			logger.error(
			        "[RPC-register] failed! statusCode: {}; message: {}",
			        result.getStatusCode(), result.getMessage());
		}

		return retResult;
	}

	private String doAuth(final String appId, final String appKey) {
		HttpRPCResult result = invokeGet(
		        composeURI(domain, String.format(uriAuthPattern,
		                appId, appKey)),
		        HttpStatus.SC_OK);
		String retResult = null;
		if (result.getStatusCode() == HttpStatus.SC_OK) {
			String returnStr = new String(result.getPayload());

			if (returnStr.contains(keywordStatus)) {
				if (returnStr.contains(keywordStatusOk)) {
					retResult = composeAuthResult(true, null);
					cache.register(appId, appKey);
				} else if (returnStr.contains(keywordStatusFreezed)) {
					logger.error(
					        "[RPC-authenticate] failed cause by account status in FREEZED.");
					retResult = composeAuthResult(false,
					        "Acocunt in status \"FREEZED\".");
				} else {
					logger.error(
					        "[RPC-authenticate] failed cause by account status in {}.",
					        returnStr);
					retResult = composeAuthResult(false,
					        "Acocunt in unknown status.");
				}
			} else {
				logger.error(
				        "[RPC-authenticate] Invalid return entity {}.",
				        returnStr);
				retResult = composeAuthResult(false,
				        "Invalid return entity.");
			}
		} else {
			logger.error(
			        "[RPC-authenticate] failed! statusCode: {}; message: {}",
			        result.getStatusCode(), result.getMessage());
			retResult = composeAuthResult(
			        false,
			        "Unexpectant response status:"
			                + result.getStatusCode());
		}

		return retResult;
	}
}
