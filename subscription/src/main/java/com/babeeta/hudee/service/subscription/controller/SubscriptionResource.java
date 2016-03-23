package com.babeeta.hudee.service.subscription.controller;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.httpclient.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.hudee.service.subscription.service.SubscriptionService;
import com.babeeta.hudee.service.subscription.service.TagQueryService;

/**
 * RESTful interface implements
 */
@Path("/")
public class SubscriptionResource {

	private final static Logger logger = LoggerFactory
	        .getLogger(SubscriptionResource.class);

	private final Pattern idPattern = Pattern.compile("[a-f0-9A-F]{32}");
	private final Pattern tagPattern = Pattern.compile("[a-zA-Z0-9_]{3,32}");

	private final long maxSetKeepLimit;

	private SubscriptionService subscriptionService;
	private TagQueryService tagQueryService;

	private static AtomicLong totalNewBindCount = new AtomicLong(0);
	private static AtomicLong totalRebindCount = new AtomicLong(0);
	private static AtomicLong totalUnbindCount = new AtomicLong(0);
	private static AtomicLong totalQueryCount = new AtomicLong(0);
	private static AtomicLong totalListCount = new AtomicLong(0);
	private static AtomicLong totalDeleteCount = new AtomicLong(0);

	private static AtomicLong totalTagCountDeviceCount = new AtomicLong(0);
	private static AtomicLong totalTagCreateSetCount = new AtomicLong(0);
	private static AtomicLong totalTagCountSetCount = new AtomicLong(0);
	private static AtomicLong totalTagGetSetCount = new AtomicLong(0);
	private static AtomicLong totalTagDeleteSetCount = new AtomicLong(0);

	private static AtomicLong totalNewBindSucceedCount = new AtomicLong(0);
	private static AtomicLong totalRebindSucceedCount = new AtomicLong(0);
	private static AtomicLong totalUnbindSucceedCount = new AtomicLong(0);
	private static AtomicLong totalQuerySucceedCount = new AtomicLong(0);
	private static AtomicLong totalListSucceedCount = new AtomicLong(0);
	private static AtomicLong totalDeleteSucceedCount = new AtomicLong(0);

	private static AtomicLong totalTagCountDeviceSucceedCount = new AtomicLong(
	        0);
	private static AtomicLong totalTagCreateSetSucceedCount = new AtomicLong(0);
	private static AtomicLong totalTagCountSetSucceedCount = new AtomicLong(0);
	private static AtomicLong totalTagGetSetSucceedCount = new AtomicLong(0);
	private static AtomicLong totalTagDeleteSetSucceedCount = new AtomicLong(0);

	/**
	 * Constructor
	 * 
	 * @param maxSetKeepLimit
	 *            max group result set keep time
	 */
	public SubscriptionResource(long maxSetKeepLimit) {
		this.maxSetKeepLimit = maxSetKeepLimit;
	}

	/**
	 * Set service instance of subscription, used by Spring framework.
	 * 
	 * @param tagQueryService
	 *            instance of tag Query Service
	 */
	public void setTagQueryService(TagQueryService tagQueryService) {
		this.tagQueryService = tagQueryService;
	}

	/**
	 * Set service instance of subscription, used by Spring framework.
	 * 
	 * @param subscriptionService
	 *            instance of subscription service
	 */
	public void setSubscriptionService(SubscriptionService subscriptionService) {
		this.subscriptionService = subscriptionService;
	}

	/**
	 * Create subscription
	 * 
	 * @param aid
	 *            application id
	 * @param did
	 *            device id
	 * @return HTTP response
	 */
	@PUT
	@Path("/newBind/{did}/{aid}")
	public Response newBind(@PathParam("aid") String aid,
	        @PathParam("did") String did) {
		logger.debug("New bind about [{},{}].", new Object[] { aid, did });

		totalNewBindCount.getAndIncrement();

		if (aid == null || !idPattern.matcher(aid).matches()) {
			logger.error("New bind request not contain valid aid[{}].", aid);
			return Response
			        .status(HttpStatus.SC_BAD_REQUEST)
			        .entity("New bind request not contain valid aid: " + aid)
			        .build();
		}

		if (did == null || !idPattern.matcher(did).matches()) {
			logger.error("New bind request not contain valid did[{}].", did);
			return Response
			        .status(HttpStatus.SC_BAD_REQUEST)
			        .entity("New bind request not contain valid did: " + did)
			        .build();
		}

		try {
			String result = subscriptionService.subscribe(aid, null, did);
			totalNewBindSucceedCount.getAndIncrement();
			return Response.ok(result).build();
		} catch (Exception e) {
			logger.error(e.getMessage());
			return Response.serverError().build();
		}
	}

	/**
	 * Create subscription
	 * 
	 * @param aid
	 *            application id
	 * @param did
	 *            device id
	 * @param cid
	 *            client id
	 * @return HTTP response
	 */
	@PUT
	@Path("/rebind/{did}/{aid}/{cid}")
	public Response rebind(@PathParam("aid") String aid,
	        @PathParam("did") String did,
	        @PathParam("cid") String cid) {
		logger.debug("Rebind about [{},{}] ? {}.", new Object[] { aid, did,
		        cid });

		totalRebindCount.getAndIncrement();

		if (cid != null && cid.equalsIgnoreCase("null")) {
			cid = null;
		}

		if (aid == null || !idPattern.matcher(aid).matches()) {
			logger.error("Rebind request not contain valid aid[{}].", aid);
			return Response
			        .status(HttpStatus.SC_BAD_REQUEST)
			        .entity("Rebind request not contain valid aid: " + aid)
			        .build();
		}

		if (did == null || !idPattern.matcher(did).matches()) {
			logger.error("Rebind request not contain valid did[{}].", did);
			return Response
			        .status(HttpStatus.SC_BAD_REQUEST)
			        .entity("Rebind request not contain valid did: " + did)
			        .build();
		}

		if (cid != null && !idPattern.matcher(cid).matches()) {
			logger.error("Rebind request not contain valid cid[{}].", cid);
			return Response
			        .status(HttpStatus.SC_BAD_REQUEST)
			        .entity("Rebind request not contain valid cid: " + cid)
			        .build();
		}

		try {
			String result = subscriptionService.subscribe(aid, cid, did);
			totalRebindSucceedCount.getAndIncrement();
			return Response.ok(result).build();
		} catch (Exception e) {
			logger.error(e.getMessage());
			return Response.serverError().build();
		}
	}

	/**
	 * Delete subscription
	 * 
	 * @param aid
	 *            application id
	 * @param did
	 *            device id
	 * @param cid
	 *            client id
	 * @return HTTP response
	 */
	@DELETE
	@Path("/unbind/{did}/{aid}/{cid}")
	public Response unbind(@PathParam("aid") String aid,
	        @PathParam("did") String did,
	        @PathParam("cid") String cid) {
		logger.debug("[{},{},{}] on unbinding.", new Object[] { aid, did, cid });
		totalUnbindCount.getAndIncrement();

		if (aid == null || !idPattern.matcher(aid).matches()) {
			logger.error("Unbind request not contain valid aid[{}].", aid);
			return Response
			        .status(HttpStatus.SC_BAD_REQUEST)
			        .entity("Unbind request not contain valid aid: " + aid)
			        .build();
		}

		if (did == null || !idPattern.matcher(did).matches()) {
			logger.error("Unbind request not contain valid did[{}].", did);
			return Response
			        .status(HttpStatus.SC_BAD_REQUEST)
			        .entity("Unbind request not contain valid did: " + did)
			        .build();
		}

		if (cid == null || !idPattern.matcher(cid).matches()) {
			logger.error("Unbind request not contain valid cid[{}].", cid);
			return Response
			        .status(HttpStatus.SC_BAD_REQUEST)
			        .entity("Unbind request not contain valid cid: " + cid)
			        .build();
		}

		try {
			subscriptionService.unsubscribe(aid, cid, did);
			totalUnbindSucceedCount.getAndIncrement();
			return Response.ok().build();
		} catch (Exception e) {
			logger.error(e.getMessage());
			return Response.serverError().build();
		}
	}

	/**
	 * Query device id
	 * 
	 * @param aid
	 *            application id
	 * @param cid
	 *            client id
	 * @return HTTP response
	 */
	@GET
	@Path("/query/{aid}/{cid}")
	public Response queryDevice(@PathParam("aid") String aid,
	        @PathParam("cid") String cid) {
		logger.debug("[{},{}] on query device.", aid, cid);
		totalQueryCount.getAndIncrement();

		if (aid == null || !idPattern.matcher(aid).matches()) {
			logger.error("Query device id request not contain valid aid[{}].",
			        aid);
			return Response
			        .status(HttpStatus.SC_BAD_REQUEST)
			        .entity("Query device id request not contain valid aid: "
			                + aid)
			        .build();
		}

		if (cid == null || !idPattern.matcher(cid).matches()) {
			logger.error("Query device id request not contain valid cid[{}].",
			        cid);
			return Response
			        .status(HttpStatus.SC_BAD_REQUEST)
			        .entity("Query device id request not contain valid cid: "
			                + cid)
			        .build();
		}

		try {
			String result = subscriptionService.queryDevice(aid, cid);
			if (result != null) {
				totalQuerySucceedCount.getAndIncrement();
				return Response.ok(result).build();
			} else {
				return Response.status(HttpStatus.SC_NOT_FOUND).build();
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			return Response.serverError().build();
		}
	}

	/**
	 * List all subscription about given device id
	 * 
	 * @param did
	 *            device id
	 * @return HTTP response
	 */
	@GET
	@Path("/list/{did}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response list(@PathParam("did") String did) {
		logger.debug("List subscription about [{}].", did);
		totalListCount.getAndIncrement();

		if (did == null || !idPattern.matcher(did).matches()) {
			logger.error(
			        "List subscription request not contain valid did[{}].", did);
			return Response
			        .status(HttpStatus.SC_BAD_REQUEST)
			        .entity("List subscription request not contain valid did: "
			                + did)
			        .build();
		}

		try {
			List<String> list = subscriptionService.listSubscription(did);
			if (list.size() == 0) {
				return Response.status(HttpStatus.SC_NOT_FOUND).build();
			} else {
				totalListSucceedCount.getAndIncrement();
				return Response.ok(list).build();
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			return Response.serverError().build();
		}
	}

	/**
	 * Delete all subscription about given device id
	 * 
	 * @param did
	 *            device id
	 * @return HTTP response
	 */
	@DELETE
	@Path("/deleteByDevice")
	public Response deleteByDevice(@QueryParam("did") String did) {
		logger.debug("Delete all subscription about [{}].", did);
		totalDeleteCount.getAndIncrement();

		if (did == null || !idPattern.matcher(did).matches()) {
			logger.error(
			        "Delete all subscription request not contain valid did[{}].",
			        did);
			return Response
			        .status(HttpStatus.SC_BAD_REQUEST)
			        .entity("List subscription request not contain valid did: "
			                + did)
			        .build();
		}

		try {
			long count = subscriptionService.deleteByDevice(did);
			if (count == 0) {
				return Response.status(HttpStatus.SC_NOT_FOUND).build();
			} else {
				totalDeleteSucceedCount.getAndIncrement();
				return Response.ok(count).build();
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			return Response.serverError().build();
		}
	}

	/**
	 * Count device has tag
	 * 
	 * @param aid
	 *            application id
	 * @param tName
	 *            tag name
	 * @return HTTP response
	 */
	@GET
	@Path("/tags/{aid}/count")
	public Response countDeviceByTags(
	        @PathParam("aid") String aid,
	        @QueryParam("tag") String tName) {
		logger.debug("Count device by tag [{}:{}].",
		        new Object[] { aid, tName });
		totalTagCountDeviceCount.getAndIncrement();

		if (aid == null || !idPattern.matcher(aid).matches()) {
			logger.error(
			        "Count device by tag request not contain valid aid[{}].",
			        aid);
			return Response
			        .status(HttpStatus.SC_BAD_REQUEST)
			        .entity("List tags request not contain valid aid: "
			                + aid)
			        .build();
		}

		if (tName == null || !tagPattern.matcher(tName).matches()) {
			logger.error(
			        "Count device by tag request contains invalid tag name[{}].",
			        tName);
			return Response
			        .status(HttpStatus.SC_BAD_REQUEST)
			        .entity("Count device by tag request contains invalid tag name: "
			                + tName)
			        .build();
		}

		try {
			long count = 0;
			if ("_ALL".equals(tName)) {
				count = subscriptionService.countDeviceByOwner(aid);
			} else {
				count = subscriptionService.countDeviceByTags(aid, tName);
			}

			totalTagCountDeviceSucceedCount.getAndIncrement();
			return Response.status(HttpStatus.SC_OK).entity(count).build();
		} catch (Exception e) {
			logger.error("Count device by tag request [{}]:[{}] failed: {}",
			        new Object[] {
			                aid, tName, e.getMessage() });
			return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
			        .build();
		}
	}

	/**
	 * Create a query set for a tags expression
	 * 
	 * @param aid
	 *            application id
	 * @param stamp
	 *            time limit of tag
	 * @param life
	 *            set keep alive life
	 * @return HTTP response
	 */
	@POST
	@Path("/tags/list")
	public Response createTask(
	        @QueryParam("aid") String aid,
	        @QueryParam("stamp") @DefaultValue("0") long stamp,
	        @QueryParam("life") @DefaultValue("600000") long life) {
		logger.debug("Create query set about: [{}] with [{}, {}].",
		        new Object[] { aid, stamp, life });
		totalTagCreateSetCount.getAndIncrement();
		long finalLife = 0;
		if (aid == null || !idPattern.matcher(aid).matches()) {
			logger.error(
			        "Create query set request not contain valid aid[{}].",
			        aid);
			return Response
			        .status(HttpStatus.SC_BAD_REQUEST)
			        .entity("Create query set request not contain valid aid: "
			                + aid)
			        .build();
		}

		if (stamp > System.currentTimeMillis()) {
			logger.error(
			        "Create query set request not contain valid stamp[{}].",
			        aid);
			return Response
			        .status(HttpStatus.SC_BAD_REQUEST)
			        .entity("Create query set request not contain valid stamp: "
			                + stamp)
			        .build();
		}

		if (life <= 0 || life > maxSetKeepLimit) {
			finalLife = maxSetKeepLimit;
		} else {
			finalLife = life;
		}

		try {
			// execute
			String key = tagQueryService.createTask(stamp, finalLife, aid);

			totalTagCreateSetSucceedCount.getAndIncrement();
			return Response.status(HttpStatus.SC_OK).entity(key).build();
		} catch (Exception e) {
			logger.error("Create query set request [{}] failed: {}",
			        new Object[] {
			                aid, e.getMessage() });
			return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
			        .build();
		}
	}

	/**
	 * Count result by key
	 * 
	 * @param key
	 *            key of task
	 * @return HTTP response
	 */
	@GET
	@Path("/tags/count")
	public Response countResult(@QueryParam("key") String key) {
		logger.debug("Count result by key [{}].",
		        new Object[] { key });
		totalTagCountSetCount.getAndIncrement();

		if (key == null || !idPattern.matcher(key).matches()) {
			logger.error(
			        "Count result by key request not contain valid key[{}].",
			        key);
			return Response
			        .status(HttpStatus.SC_BAD_REQUEST)
			        .entity("Count result by key request not contain valid key: "
			                + key)
			        .build();
		}

		try {
			String stage = tagQueryService.queryTaskStage(key);
			if (stage == null) {
				return Response.status(HttpStatus.SC_NOT_FOUND).build();
			} else if (stage.equalsIgnoreCase("ready")) {
				long count = tagQueryService.countResult(key);
				totalTagCountSetSucceedCount.getAndIncrement();
				return Response.status(HttpStatus.SC_OK).entity(count).build();
			} else {
				String info = "This task(" + key + ") is in stage : " + stage;
				logger.warn(info);
				return Response.status(HttpStatus.SC_FORBIDDEN).entity(info)
				        .build();
			}
		} catch (Exception e) {
			logger.error("Count result by key request [{}] failed: {}",
			        new Object[] {
			                key, e.getMessage() });
			return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
			        .build();
		}
	}

	/**
	 * Get result
	 * 
	 * @param key
	 *            key of task
	 * @param pageSize
	 *            page size
	 * @param pageIndex
	 *            page index
	 * @return HTTP response
	 */
	@GET
	@Path("/tags/result")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getResult(
	        @QueryParam("key") String key,
	        @QueryParam("pageSize") @DefaultValue("-1") int pageSize,
	        @QueryParam("pageIndex") @DefaultValue("-1") int pageIndex) {
		logger.debug("Get result [{}:{}:{}].",
		        new Object[] { key, pageSize, pageIndex });
		totalTagGetSetCount.getAndIncrement();

		if (key == null || !idPattern.matcher(key).matches()) {
			logger.error(
			        "Get result request not contain valid key[{}].",
			        key);
			return Response
			        .status(HttpStatus.SC_BAD_REQUEST)
			        .entity("Get result request not contain valid key: "
			                + key)
			        .build();
		}

		if (pageSize == -1 || pageIndex == -1) {
			String info = "Get result request not contain valid page info[{"
			        + pageSize + "}:{" + pageIndex + "}].";
			logger.error(info);
			return Response
			        .status(HttpStatus.SC_BAD_REQUEST)
			        .entity(info)
			        .build();
		}

		try {
			String stage = tagQueryService.queryTaskStage(key);
			if (stage == null) {
				return Response.status(HttpStatus.SC_NOT_FOUND).build();
			} else if (stage.equalsIgnoreCase("ready")) {
				int fromIndex = pageSize * pageIndex;
				int toIndex = fromIndex + pageSize;
				int total = tagQueryService.countResult(key);
				if (toIndex > total) {
					toIndex = total;
				}
				List<String> result = tagQueryService.readResult(key,
				        fromIndex, toIndex);
				totalTagGetSetSucceedCount.getAndIncrement();
				return Response.status(HttpStatus.SC_OK).entity(result).build();
			} else {
				String info = "This task(" + key + ") is in stage : " + stage;
				return Response.status(HttpStatus.SC_FORBIDDEN).entity(info)
				        .build();
			}
		} catch (Exception e) {
			logger.error("Get result request [{}] failed: {}",
			        new Object[] {
			                key, e.getMessage() });
			return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
			        .build();
		}
	}

	/**
	 * Delete result
	 * 
	 * @param key
	 *            key of task
	 * @return HTTP response
	 */
	@DELETE
	@Path("/tags/result")
	public Response deleteResult(@QueryParam("key") String key) {
		logger.debug("Delete result [{}].",
		        new Object[] { key });
		totalTagDeleteSetCount.getAndIncrement();

		if (key == null || !idPattern.matcher(key).matches()) {
			logger.error(
			        "Delete result request not contain valid key[{}].",
			        key);
			return Response
			        .status(HttpStatus.SC_BAD_REQUEST)
			        .entity("Delete result request not contain valid key: "
			                + key)
			        .build();
		}

		try {
			String stage = tagQueryService.queryTaskStage(key);
			if (stage == null) {
				return Response.status(HttpStatus.SC_NOT_FOUND).build();
			} else {
				tagQueryService.deleteTask(key);
				totalTagDeleteSetSucceedCount.getAndIncrement();
				return Response.status(HttpStatus.SC_OK).build();
			}
		} catch (Exception e) {
			logger.error("Delete result request [{}] failed: {}",
			        new Object[] {
			                key, e.getMessage() });
			return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
			        .build();
		}
	}

	/**
	 * @param reset
	 *            whether reset counter
	 * @return the value of counter (before reset)
	 */
	public synchronized static long getNewBindCount(boolean reset) {
		if (reset) {
			return totalNewBindCount.getAndSet(0);
		} else {
			return totalNewBindCount.get();
		}
	}

	/**
	 * @param reset
	 *            whether reset counter
	 * @return the value of counter (before reset)
	 */
	public synchronized static long getRebindCount(boolean reset) {
		if (reset) {
			return totalRebindCount.getAndSet(0);
		} else {
			return totalRebindCount.get();
		}
	}

	/**
	 * @param reset
	 *            whether reset counter
	 * @return the value of counter (before reset)
	 */
	public synchronized static long getUnbindCount(boolean reset) {
		if (reset) {
			return totalUnbindCount.getAndSet(0);
		} else {
			return totalUnbindCount.get();
		}
	}

	/**
	 * @param reset
	 *            whether reset counter
	 * @return the value of counter (before reset)
	 */
	public synchronized static long getQueryCount(boolean reset) {
		if (reset) {
			return totalQueryCount.getAndSet(0);
		} else {
			return totalQueryCount.get();
		}
	}

	/**
	 * @param reset
	 *            whether reset counter
	 * @return the value of counter (before reset)
	 */
	public synchronized static long getListCount(boolean reset) {
		if (reset) {
			return totalListCount.getAndSet(0);
		} else {
			return totalListCount.get();
		}
	}

	/**
	 * @param reset
	 *            whether reset counter
	 * @return the value of counter (before reset)
	 */
	public synchronized static long getDeleteCount(boolean reset) {
		if (reset) {
			return totalDeleteCount.getAndSet(0);
		} else {
			return totalDeleteCount.get();
		}
	}

	/**
	 * @param reset
	 *            whether reset counter
	 * @return the value of counter (before reset)
	 */
	public synchronized static long getNewBindSucceedCount(boolean reset) {
		if (reset) {
			return totalNewBindSucceedCount.getAndSet(0);
		} else {
			return totalNewBindSucceedCount.get();
		}
	}

	/**
	 * @param reset
	 *            whether reset counter
	 * @return the value of counter (before reset)
	 */
	public synchronized static long getRebindSucceedCount(boolean reset) {
		if (reset) {
			return totalRebindSucceedCount.getAndSet(0);
		} else {
			return totalRebindSucceedCount.get();
		}
	}

	/**
	 * @param reset
	 *            whether reset counter
	 * @return the value of counter (before reset)
	 */
	public synchronized static long getUnbindSucceedCount(boolean reset) {
		if (reset) {
			return totalUnbindSucceedCount.getAndSet(0);
		} else {
			return totalUnbindSucceedCount.get();
		}
	}

	/**
	 * @param reset
	 *            whether reset counter
	 * @return the value of counter (before reset)
	 */
	public synchronized static long getQuerySucceedCount(boolean reset) {
		if (reset) {
			return totalQuerySucceedCount.getAndSet(0);
		} else {
			return totalQuerySucceedCount.get();
		}
	}

	/**
	 * @param reset
	 *            whether reset counter
	 * @return the value of counter (before reset)
	 */
	public synchronized static long getListSucceedCount(boolean reset) {
		if (reset) {
			return totalListSucceedCount.getAndSet(0);
		} else {
			return totalListSucceedCount.get();
		}
	}

	/**
	 * @param reset
	 *            whether reset counter
	 * @return the value of counter (before reset)
	 */
	public synchronized static long getDeleteSucceedCount(boolean reset) {
		if (reset) {
			return totalDeleteSucceedCount.getAndSet(0);
		} else {
			return totalDeleteSucceedCount.get();
		}
	}

	/**
	 * @param reset
	 *            whether reset counter
	 * @return the value of counter (before reset)
	 */
	public synchronized static long getTagCountDeviceCount(boolean reset) {
		if (reset) {
			return totalTagCountDeviceCount.getAndSet(0);
		} else {
			return totalTagCountDeviceCount.get();
		}
	}

	/**
	 * @param reset
	 *            whether reset counter
	 * @return the value of counter (before reset)
	 */
	public synchronized static long getTagCreateSetCount(boolean reset) {
		if (reset) {
			return totalTagCreateSetCount.getAndSet(0);
		} else {
			return totalTagCreateSetCount.get();
		}
	}

	/**
	 * @param reset
	 *            whether reset counter
	 * @return the value of counter (before reset)
	 */
	public synchronized static long getTagCountSetCount(boolean reset) {
		if (reset) {
			return totalTagCountSetCount.getAndSet(0);
		} else {
			return totalTagCountSetCount.get();
		}
	}

	/**
	 * @param reset
	 *            whether reset counter
	 * @return the value of counter (before reset)
	 */
	public synchronized static long getTagGetSetCount(boolean reset) {
		if (reset) {
			return totalTagGetSetCount.getAndSet(0);
		} else {
			return totalTagGetSetCount.get();
		}
	}

	/**
	 * @param reset
	 *            whether reset counter
	 * @return the value of counter (before reset)
	 */
	public synchronized static long getTagCountDeviceSucceedCount(boolean reset) {
		if (reset) {
			return totalTagCountDeviceSucceedCount.getAndSet(0);
		} else {
			return totalTagCountDeviceSucceedCount.get();
		}
	}

	/**
	 * @param reset
	 *            whether reset counter
	 * @return the value of counter (before reset)
	 */
	public synchronized static long getTagCreateSetSucceedCount(boolean reset) {
		if (reset) {
			return totalTagCreateSetSucceedCount.getAndSet(0);
		} else {
			return totalTagCreateSetSucceedCount.get();
		}
	}

	/**
	 * @param reset
	 *            whether reset counter
	 * @return the value of counter (before reset)
	 */
	public synchronized static long getTagCountSetSucceedCount(boolean reset) {
		if (reset) {
			return totalTagCountSetSucceedCount.getAndSet(0);
		} else {
			return totalTagCountSetSucceedCount.get();
		}
	}

	/**
	 * @param reset
	 *            whether reset counter
	 * @return the value of counter (before reset)
	 */
	public synchronized static long getTagGetSetSucceedCount(boolean reset) {
		if (reset) {
			return totalTagGetSetSucceedCount.getAndSet(0);
		} else {
			return totalTagGetSetSucceedCount.get();
		}
	}

	/**
	 * @param reset
	 *            whether reset counter
	 * @return the value of counter (before reset)
	 */
	public synchronized static long getTagDeleteSetCount(boolean reset) {
		if (reset) {
			return totalTagDeleteSetCount.getAndSet(0);
		} else {
			return totalTagDeleteSetCount.get();
		}
	}

	/**
	 * @param reset
	 *            whether reset counter
	 * @return the value of counter (before reset)
	 */
	public synchronized static long getTagDeleteSetSucceedCount(boolean reset) {
		if (reset) {
			return totalTagDeleteSetSucceedCount.getAndSet(0);
		} else {
			return totalTagDeleteSetSucceedCount.get();
		}
	}
}
