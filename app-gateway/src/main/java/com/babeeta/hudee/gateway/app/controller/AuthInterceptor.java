package com.babeeta.hudee.gateway.app.controller;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpStatus;
import org.jboss.resteasy.annotations.interception.SecurityPrecedence;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.ResourceMethod;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.interception.PreProcessInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.hudee.gateway.app.service.AccountService;

/**
 * 
 */
@Provider
@ServerInterceptor
@SecurityPrecedence
public class AuthInterceptor implements PreProcessInterceptor {
	private final static Logger logger = LoggerFactory
	        .getLogger(AuthInterceptor.class);
	private AccountService accountService;
	private static String headerAuthorization = "Authorization";
	private static String keywordStatus = "status";
	private static String keywordSucceed = "SUCCESS";

	/**
	 * @param accountService
	 *            account service instance
	 */
	public void setAccountService(AccountService accountService) {
		this.accountService = accountService;
	}

	private String[] getAuthContent(final String authorization) {
		try {
			String base64Content = authorization.split(" ")[1];
			String authContent = new String(Base64.decodeBase64(base64Content),
			        "UTF-8");
			return authContent.split(":");
		} catch (Exception e) {
			logger.error("[authorization header] {}", e.getMessage());
			return null;
		}
	}

	private boolean isAuthContentEmpty(final String[] authContent) {
		if (authContent == null || authContent.length != 2
		        || authContent[0].trim().length() == 0
		        || authContent[1].trim().length() == 0) {
			return true;
		}
		return false;
	}

	@Override
	public ServerResponse preProcess(final HttpRequest request,
	        final ResourceMethod method) {
		logger.debug("[AuthInterceptor] start");
		final String[] authContent = getAuthContent(request.getHttpHeaders()
		        .getRequestHeaders().getFirst(headerAuthorization));

		if (!isAuthContentEmpty(authContent)) {
			String authResult = accountService.authenticate(authContent[0],
			        authContent[1]);
			if (authResult != null && authResult.contains(keywordStatus)) {
				if (!authResult.contains(keywordSucceed)) {
					logger.debug("[AuthInterceptor] end, auth failed!");
					return ServerResponse
					        .copyIfNotServerResponse(Response.status(
					                HttpStatus.SC_UNAUTHORIZED).build());
				}
			} else {
				// should never run to this case.
				// please check:
				// 1. account service in module
				// 2. account service application in project
				logger.error(
				        "[Auth filter] Fatal error! Invalid result: {}",
				        authResult);
				return ServerResponse
				        .copyIfNotServerResponse(Response.status(
				                HttpStatus.SC_INTERNAL_SERVER_ERROR).build());
			}
		} else {
			logger.debug("[AuthInterceptor] end, no auth content!");
			return ServerResponse
			        .copyIfNotServerResponse(Response.status(
			                HttpStatus.SC_NON_AUTHORITATIVE_INFORMATION)
			                .build());
		}
		logger.debug("[AuthInterceptor] end, successful!");
		return null;
	}
}
