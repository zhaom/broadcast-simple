package com.babeeta.hudee.service.account.controller;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.hudee.service.account.entity.DeviceAccount;
import com.babeeta.hudee.service.account.service.DeviceAccountService;

/**
 * RESTful interface implements
 */
@Path("/")
public class DeviceAccountResource {
	private final static Logger logger = LoggerFactory
	        .getLogger(DeviceAccountResource.class);

	private DeviceAccountService deviceAccountService;

	private static AtomicLong totalAuthSucceedCount = new AtomicLong(0);
	private static AtomicLong totalRegisterSucceedCount = new AtomicLong(0);

	private static AtomicLong totalAuthCount = new AtomicLong(0);
	private static AtomicLong totalRegisterCount = new AtomicLong(0);

	/**
	 * Set service instance of account, used by Spring framework.
	 * 
	 * @param deviceAccountService
	 *            instance of account service
	 */
	public void setDeviceAccountService(
	        DeviceAccountService deviceAccountService) {
		this.deviceAccountService = deviceAccountService;
	}

	/**
	 * @param id
	 *            device id
	 * @param key
	 *            device key
	 * @return HTTP response
	 */
	@GET
	@Path("/auth/{id}/{key}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response auth(@PathParam("id") String id,
	        @PathParam("key") String key) {
		totalAuthCount.getAndIncrement();
		if (id == null || key == null) {
			logger.error("Auth request not contain valid id[{}] or key[{}].",
			        id, key);
			return Response.status(HttpStatus.SC_BAD_REQUEST).build();
		} else {
			try {
				// for performance monitor start
				totalAuthSucceedCount.getAndIncrement();
				// for performance monitor end

				String result = deviceAccountService.auth(id, key);

				logger.debug("[{}]:[{}] auth successful!", id, key);
				return Response.ok(result).build();
			} catch (Exception e) {
				logger.error("Auth account[{}]:[{}] failed: {}", new Object[] {
				        id, key, e.getMessage() });
				return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
				        .build();
			}
		}
	}

	/**
	 * Register a new device account
	 * 
	 * @param info
	 *            device info
	 * @return HTTP response
	 */
	@POST
	@Path("/register")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response register(Map<String, Object> info) {
		logger.debug("Register account with [{}]", info.toString());
		totalRegisterCount.getAndIncrement();
		try {
			// for performance monitor start
			totalRegisterSucceedCount.getAndIncrement();
			// for performance monitor end

			DeviceAccount result = deviceAccountService.register(info);

			return Response.ok(result).build();
		} catch (Exception e) {
			logger.error("Register account with [{}] failed: {}", new Object[] {
			        info.toString(), e.getMessage() });
			return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).build();
		}
	}

	/**
	 * @param reset
	 *            whether reset counter
	 * @return the value of counter (before reset)
	 */
	public synchronized static long getRegisterCount(boolean reset) {
		if (reset) {
			return totalRegisterCount.getAndSet(0);
		} else {
			return totalRegisterCount.get();
		}
	}

	/**
	 * @param reset
	 *            whether reset counter
	 * @return the value of counter (before reset)
	 */
	public synchronized static long getRegisterSucceedCount(boolean reset) {
		if (reset) {
			return totalRegisterSucceedCount.getAndSet(0);
		} else {
			return totalRegisterSucceedCount.get();
		}
	}

	/**
	 * @param reset
	 *            whether reset counter
	 * @return the value of counter (before reset)
	 */
	public synchronized static long getAuthCount(boolean reset) {
		if (reset) {
			return totalAuthCount.getAndSet(0);
		} else {
			return totalAuthCount.get();
		}
	}

	/**
	 * @param reset
	 *            whether reset counter
	 * @return the value of counter (before reset)
	 */
	public synchronized static long getAuthSucceedCount(boolean reset) {
		if (reset) {
			return totalAuthSucceedCount.getAndSet(0);
		} else {
			return totalAuthSucceedCount.get();
		}
	}
}
