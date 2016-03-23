package com.babeeta.hudee.service.account.controller;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.hudee.service.account.entity.AppAccount;
import com.babeeta.hudee.service.account.service.AppAccountService;

/**
 * RESTful interface implements
 */
@Path("/")
public class AppAccountResource {

	private final static Logger logger = LoggerFactory
	        .getLogger(AppAccountResource.class);

	private AppAccountService appAccountService;
	private final Pattern idPattern = Pattern.compile("[a-f0-9A-F]{32}");

	private static AtomicLong totalRegisterCount = new AtomicLong(0);
	private static AtomicLong totalAuthCount = new AtomicLong(0);

	private static AtomicLong totalRegisterSucceedCount = new AtomicLong(0);
	private static AtomicLong totalAuthSucceedCount = new AtomicLong(0);

	/**
	 * Set service instance of account, used by Spring framework.
	 * 
	 * @param appAccountService
	 *            instance of account service
	 */
	public void setAppAccountService(AppAccountService appAccountService) {
		this.appAccountService = appAccountService;
	}

	/**
	 * @param aid
	 *            application id
	 * @param key
	 *            application key
	 * @return HTTP response
	 */
	@GET
	@Path("auth/{aid}/{key}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response auth(@PathParam("aid") String aid,
	        @PathParam("key") String key) {
		totalAuthCount.getAndIncrement();
		if (aid == null || key == null) {
			logger.error("Auth request not contain valid id[{}] or key[{}].",
			        aid, key);
			return Response.status(HttpStatus.SC_BAD_REQUEST).build();
		} else {
			try {
				final String result = appAccountService.auth(aid, key);
				totalAuthSucceedCount.getAndIncrement();
				return Response.ok(result).build();
			} catch (Exception e) {
				logger.error("Auth account[{}]:[{}] failed: {}", new Object[] {
				        aid, key, e.getMessage() });
				return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
				        .build();
			}
		}
	}

	/**
	 * Query application account by application id
	 * 
	 * @param id
	 *            application id
	 * @return HTTP response
	 */
	@GET
	@Path("query")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAccount(@QueryParam("id") String id) {
		if (id != null && idPattern.matcher(id).matches()) {
			try {
				return Response.ok(appAccountService.getAccountById(id))
				        .build();
			} catch (Exception e) {
				logger.error("[QueryAccount] Get account[{}] failed: {}", id,
				        e.getMessage());
				return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
				        .build();
			}
		} else {
			logger.error("[QueryAccount] Query with invalid id. {}", id);
			return Response.status(HttpStatus.SC_BAD_REQUEST).build();
		}
	}

	/**
	 * Register a new application account
	 * 
	 * @param info
	 *            application account info
	 * @return HTTP response
	 */
	@POST
	@Path("register")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response register(Map<String, Object> info) {
		final long stamp = System.nanoTime();
		logger.debug("[{}]Register account with [{}]", stamp, info.toString());
		totalRegisterCount.getAndIncrement();
		try {
			AppAccount result = appAccountService.register(info);
			totalRegisterSucceedCount.getAndIncrement();
			return Response.ok(result).build();
		} catch (Exception e) {
			logger.error("[{}]Register account with [{}] failed: {}",
			        new Object[] { stamp,
			                info.toString(), e.getMessage() });
			return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).build();
		}
	}

    @GET
    @Path("/count")
    @Produces("text/plain")
    public Response count(){
        try {
            return Response.ok().entity(appAccountService.count()).build();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return Response.status(500).entity("server internal error by hudee").build();
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
