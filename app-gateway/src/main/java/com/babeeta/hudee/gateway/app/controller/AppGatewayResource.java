package com.babeeta.hudee.gateway.app.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.hudee.common.mbean.MBeanManager;
import com.babeeta.hudee.common.thread.NamedThreadFactory;
import com.babeeta.hudee.common.thread.ThreadPoolStatus;
import com.babeeta.hudee.common.thread.TrackingThreadPool;
import com.babeeta.hudee.gateway.app.entity.BaseMessageContext;
import com.babeeta.hudee.gateway.app.entity.ShardingMessageContext;
import com.babeeta.hudee.gateway.app.service.MessageService;
import com.babeeta.hudee.gateway.app.service.ShardingService;
import com.babeeta.hudee.gateway.app.service.SubscriptionService;

/**
 * New application gateway
 */
@Path("/")
public class AppGatewayResource {
	private final static Logger logger = LoggerFactory
	        .getLogger(AppGatewayResource.class);
	private static final Pattern idPattern = Pattern.compile("[a-f0-9A-F]{32}");
	private final Pattern digitalPattern = Pattern.compile("\\d+");
	private final int MSG_CONTENT_MAX_LENGTH;
	private final int MAX_EXPIRE_TIME;
	private final int PAGE_SIZE;

	private final TrackingThreadPool executor;

	private SubscriptionService subscriptionService;
	private MessageService messageService;
	private ShardingService shardingService;

	/**
	 * Constructor
	 * 
	 * @param maxContentSize
	 *            the limit of content size
	 * @param maxExpiredTime
	 *            the limit of message life
	 * @param pageSize
	 *            page size
	 * 
	 */
	public AppGatewayResource(int maxContentSize, int maxExpiredTime,
	        int pageSize) {
		this.MSG_CONTENT_MAX_LENGTH = maxContentSize;
		this.MAX_EXPIRE_TIME = maxExpiredTime;
		this.PAGE_SIZE = pageSize;

		// create an executor to run sharding task for mutlicast and broadcast.
		executor = new TrackingThreadPool(
		        512,
		        512,
		        0, TimeUnit.SECONDS,
		        new LinkedBlockingQueue<Runnable>(),
		        new NamedThreadFactory("AppGW-Push-", true));
		MBeanManager.registerMBean(
		        "AppGateway." + AppGatewayResource.class.getSimpleName()
		                + ":name=AppGatewayResource",
		        new ThreadPoolStatus("AppGW-Push-", executor));
	}

	/**
	 * Set service of message, used by Spring framework.
	 * 
	 * @param messageService
	 *            instance of message service
	 */
	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}

	/**
	 * Set service of subscription, used by Spring framework.
	 * 
	 * @param subscriptionService
	 *            instance of subscription service
	 */
	public void setSubscriptionService(SubscriptionService subscriptionService) {
		this.subscriptionService = subscriptionService;
	}

	/**
	 * Set service of Sharding Service, used by Spring framework.
	 * 
	 * @param shardingService
	 *            instance of Sharding Service
	 */
	public void setShardingService(ShardingService shardingService) {
		this.shardingService = shardingService;
	}

	/**
	 * Broadcast push
	 * 
	 * @param aid
	 *            application id
	 * @param authInfo
	 *            authorization info
	 * @param request
	 *            request JSON object
	 * @return HTTP request
	 */
	@POST
	@Path("/{aid}/push/broadcast")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response broadcast(@PathParam("aid") String aid,
	        @HeaderParam("Authorization") String authInfo,
	        Map<String, Object> request) {
		// check parameters
		Response rsp = checkAppId(aid, authInfo);
		if (rsp != null) {
			return rsp;
		}
		rsp = checkBroadcastRequestBody(request);
		if (rsp != null) {
			return rsp;
		}
		// pick up payload and life
		String payload = request.get("payload").toString();
		int life = 0;
		if (request.containsKey("life")) {
			String lifeStr = request.get("life").toString();
			if (digitalPattern.matcher(lifeStr).matches()) {
				life = Integer.parseInt(lifeStr);
				if (life > MAX_EXPIRE_TIME || life <= 0) {
					life = MAX_EXPIRE_TIME;
				}
			} else {
				life = MAX_EXPIRE_TIME;
			}
		} else {
			life = MAX_EXPIRE_TIME;
		}

		BaseMessageContext messageContext = new BaseMessageContext();
		messageContext.setAid(aid);
		messageContext.setContent(payload.getBytes());
		messageContext.setLife(life);

		return doMutlicast("_ALL", messageContext);
	}

	/**
	 * internal use group push
	 * 
	 * @param aid
	 *            application id
	 * @param life
	 *            message life
	 * @param taskId
	 *            task id
	 * @param parentId
	 *            parent message id
	 * @param pageSize
	 *            page size
	 * @param pageIndex
	 *            page index
	 * @param content
	 *            message content
	 * @return HTTP response
	 */
	@SuppressWarnings("static-access")
	@POST
	@Path("/{aid}/push/group")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	public Response internalGroupPush(@PathParam("aid") String aid,
	        @HeaderParam("life") @DefaultValue("0") int life,
	        @QueryParam("id") String taskId,
	        @QueryParam("parent") String parentId,
	        @QueryParam("pageSize") int pageSize,
	        @QueryParam("pageIndex") int pageIndex,
	        byte[] content) {
		long startAt = System.currentTimeMillis();
		int succeedCounter = 0;
		List<String> deviceKeyList = subscriptionService.getTaskResult(taskId,
		        pageSize, pageIndex);
		List<String> retryList = new ArrayList<String>();
		for (String deviceKey : deviceKeyList) {
			// TODO: save reference message for each device
			// key = did.cid
			String[] key = deviceKey.split("\\.");
			String mid = messageService.createReferenceMessage(aid, key[1],
			        key[0], parentId, life);
			if (mid != null) {
				succeedCounter++;
			} else {
				if (!retryList.contains(deviceKey)) {
					retryList.add(deviceKey);
				}
			}
		}
		logger.debug("[{}] Part: {} Pass 1 done. {}/{}", new Object[] { taskId,
		        pageIndex, succeedCounter, deviceKeyList.size() });
		// TODO: add retry counter and limit to avoid endless loop? or just
		// retry one pass?
		// while (succeedCounter < deviceKeyList.size()) {
		if (succeedCounter < deviceKeyList.size()) {
			try {
				Thread.currentThread().sleep(3000L);
			} catch (InterruptedException e) {
				logger.error(
				        "[{}]Try to sleep thread for next retry failed! {}",
				        taskId, e.getMessage());
			} finally {
				for (int j = 0; j < retryList.size(); j++) {
					String[] key = retryList.get(j).split(".");
					String mid = messageService.createReferenceMessage(aid,
					        key[1], key[0], parentId, life);
					if (mid != null) {
						succeedCounter++;
						retryList.remove(j);
					}
				}
			}
		}
		logger.debug("[{}] Part: {} finally done. {}/{}. costed: {} ms",
		        new Object[] {
		                taskId, pageIndex, succeedCounter,
		                deviceKeyList.size(),
		                ((System.currentTimeMillis() - startAt)) });
		return Response.status(HttpStatus.SC_OK).entity(succeedCounter).build();
	}

	/**
	 * Query message status by message id
	 * 
	 * @param messageId
	 *            message id
	 * @return HTTP response
	 */
	@GET
	@Path("/{aid}/query")
	@Produces(MediaType.APPLICATION_JSON)
	public Response queryMessageStatus(@QueryParam("id") String messageId) {

		if (messageId == null || "null".equalsIgnoreCase(messageId)) {
			// not found messageId
			return Response.status(HttpStatus.SC_BAD_REQUEST)
			        .entity("Bad request. No message id.")
			        .build();
		}

		if (!idPattern.matcher(messageId).matches()) {
			// invalid messageId
			return Response.status(HttpStatus.SC_BAD_REQUEST)
			        .entity("Bad request. Invalid message id. " + messageId)
			        .build();
		}

		try {
			String result = messageService.getMessageStatus(messageId);
			if (result != null) {
				return Response.status(HttpStatus.SC_OK).entity(result).build();
			} else {
				return Response.status(HttpStatus.SC_NOT_FOUND).build();
			}
		} catch (Exception e) {
			return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).build();
		}
	}

	private Response doMutlicast(final String target,
	        final BaseMessageContext messageContext) {
		// TODO : transform tags from "(A&B)^(C|D)" to JSON which required by
		// create message
		final String parentId = messageService.createParentMessage(
		        messageContext.getAid(),
		        target,
		        messageContext.getLife(), messageContext.getContent());

		if (parentId != null) {
			// send message to big router
			final String key = subscriptionService.createGroupTask(
			        messageContext.getAid(), target);

			if (key != null) {
				// startup a thread to check the query group task status. and
				// dispatch sub-tasks when it finished.
				executor.execute(new DispatchTask(key, parentId,
				        messageContext, 256, PAGE_SIZE));
				return Response.status(HttpStatus.SC_OK).entity(parentId)
				        .build();
			} else {
				// create query set failed.
				return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
				        .build();
			}
		} else {
			// create message failed.
			return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).build();
		}
	}

	private final String[] getAuthContent(final String authorization) {
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

	private Response checkAppId(String aid, String authInfo) {
		// check parameters
		if (!idPattern.matcher(aid).matches()) {
			// invalid aid
			return Response.status(HttpStatus.SC_BAD_REQUEST)
			        .entity("Bad request. Aid in URI is invalid. " + aid)
			        .build();
		}
		String aidAuth = getAuthContent(authInfo)[0];
		if (!aidAuth.equalsIgnoreCase(aid)) {
			// aid is not same as aid in authorization info
			return Response
			        .status(HttpStatus.SC_FORBIDDEN)
			        .entity("Aid in URi is not same as aid in authorization info. "
			                + aid + " / " + aidAuth).build();
		}
		return null;
	}

	private Response checkBroadcastRequestBody(Map<String, Object> request) {
		if (!request.containsKey("payload")) {
			// not found content
			return Response.status(HttpStatus.SC_BAD_REQUEST)
			        .entity("Bad request. Not found \"payload\" in request.")
			        .build();
		}

		String payload = request.get("payload").toString();
		if (payload == null || "null".equalsIgnoreCase(payload)) {
			// not found payload
			return Response.status(HttpStatus.SC_BAD_REQUEST)
			        .entity("Bad request. No message content.")
			        .build();
		}
		if (payload.length() > MSG_CONTENT_MAX_LENGTH) {
			// too big entity
			return Response.status(HttpStatus.SC_REQUEST_TOO_LONG)
			        .entity("Payload too large. " + payload.length())
			        .build();
		}
		return null;
	}

	private class DispatchTask implements Runnable {
		private long resultCount;
		private final String key;
		private final String parentId;
		private final BaseMessageContext messageContext;
		private final int pageSize;

		private final TrackingThreadPool dispatchExecutor;
		private final CompletionService<List<Integer>> dispatchCompletionService;

		private int totalSubTask;

		public DispatchTask(String key, String parentId,
		        BaseMessageContext messageContext, int thread, int pageSize) {
			this.key = key;
			this.parentId = parentId;
			this.messageContext = messageContext;
			this.pageSize = pageSize;

			dispatchExecutor = new TrackingThreadPool(thread,
			        thread, 60,
			        TimeUnit.SECONDS,
			        new LinkedBlockingQueue<Runnable>(),
			        new NamedThreadFactory("DispatchExecutor-", true));
			dispatchCompletionService = new ExecutorCompletionService<List<Integer>>(
			        dispatchExecutor);
		}

		/*
		 * Use http client to submit sub task to internal group push interface
		 * of app gateway.
		 * 
		 * @param key key of group task in subscription service
		 * 
		 * @param parentId parent message id
		 * 
		 * @param ctx base message context included aid, life and content.
		 * 
		 * @param pageIndex index for read device id
		 * 
		 * @param pageSize page size
		 */
		private boolean subTask(ShardingMessageContext sctx, int pageIndex) {
			// long startAt = System.currentTimeMillis();
			int statusCode = shardingService.submit(sctx, pageIndex);
			// logger.debug("[{}] Submit {} costed : {} ms",
			// new Object[] { sctx.getTaskId(), pageIndex,
			// System.currentTimeMillis() - startAt });
			// TODO: Is there something fatal error to interrupt whole mutlicast
			// task?
			if (statusCode == HttpStatus.SC_OK) {
				return true;
			} else {
				return false;
			}
		}

		/*
		 * Send internal group push request to app gateway. Use the overage
		 * performance to push message to each device.
		 */
		private void startupSubTask(String key, String parentId, long count,
		        BaseMessageContext ctx) {
			long startAt = System.currentTimeMillis();
			// calculate the total count of sub-tasks by default page size.
			long totalShard = count / pageSize;
			if (count % pageSize > 0) {
				totalShard++;
			}

			ShardingMessageContext sctx = new ShardingMessageContext();
			sctx.setAid(ctx.getAid());
			sctx.setLife(ctx.getLife());
			sctx.setContent(ctx.getContent());
			sctx.setTaskId(key);
			sctx.setParentId(parentId);
			sctx.setPageSize(pageSize);
			sctx.setTotalShard((int) totalShard);
			// start to submit sub task to app gateway.
			Date startTime = new Date();

			logger.debug("Task: [{}], total sub task = {},  execute at: {}",
			        new Object[] { key, totalShard, startTime.toString() });

			totalSubTask = (int) (totalShard / 1000);
			if (totalShard % 1000 > 0) {
				totalSubTask++;
			}

			for (int i = 0; i < totalSubTask; i++) {
				int startIndex = i * 1000;
				int endIndex = 1000 * (i + 1);
				if (endIndex > totalShard) {
					endIndex = (int) totalShard;
				}
				if (!dispatchExecutor.isShutdown()) {
					dispatchCompletionService.submit(new SubTask(startIndex,
					        endIndex, sctx));
				}
			}

			List<Integer> retryList = getResult();

			logger.debug("Task: [{}] try to do retry.");
			do {
				for (int i = 0; i < retryList.size(); i++) {
					int index = retryList.get(i);
					if (subTask(sctx, index)) {
						// remove succeed sub task from retry list
						retryList.remove(i);
					}
				}
			} while (retryList.size() > 0);

			logger.debug(
			        "Task: [{}] has be done. finished at : {}; total cost : {} millisecond(s).",
			        new Object[] {
			                sctx.getTaskId(),
			                new Date().toString(),
			                (new Date().getTime() - startAt) });
		}

		@Override
		public void run() {
			do {
				resultCount = subscriptionService.checkGroupTask(key);
				try {
					Thread.sleep(30000L);
				} catch (InterruptedException e) {
					logger.error(
					        "[{}] doMutlicast - sleep failed!", key);
					e.printStackTrace();
				}
			} while (resultCount == -1);

			if (resultCount == Long.MIN_VALUE) {
				// Fatal error. not found key.
			} else if (resultCount == 0) {
				// no target need to push
			} else if (resultCount < 0) {
				// something wrong happened
			} else {
				// startup sub-tasks.
				logger.debug("[{}] Dispatch to {} devices", key, resultCount);
				startupSubTask(key, parentId, resultCount, messageContext);
			}
		}

		private List<Integer> getResult() {
			List<Integer> result = new ArrayList<Integer>();
			for (int i = 0; i < totalSubTask; i++) {
				try {
					List<Integer> subList = dispatchCompletionService.take()
					        .get();
					result.addAll(subList);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}

			return result;
		}

		class SubTask implements Callable<List<Integer>> {
			private final ShardingMessageContext smctx;
			private final int startIndex;
			private final int endIndex;

			public SubTask(int startIndex,
			        int endIndex,
			        ShardingMessageContext smctx) {
				this.startIndex = startIndex;
				this.endIndex = endIndex;
				this.smctx = smctx;
			}

			@Override
			public List<Integer> call() throws Exception {
				logger.debug(
				        "Task: [{}], sub task[{}-{}] start.",
				        new Object[] { smctx.getTaskId(), startIndex,
				                endIndex });
				long startAt = System.currentTimeMillis();
				List<Integer> failedShard = new ArrayList<Integer>();
				for (int i = startIndex; i < endIndex; i++) {
					if (!subTask(smctx, i)) {
						// append index of failed sub task to retry list
						if (!failedShard.contains(i)) {
							failedShard.add(i);
						}
					}
				}

				logger.debug(
				        "Task: [{}], sub task[{}-{}] done. costed : {} ms",
				        new Object[] { smctx.getTaskId(), startIndex,
				                endIndex,
				                (System.currentTimeMillis() - startAt) });
				return failedShard;
			}
		}
	}

}
