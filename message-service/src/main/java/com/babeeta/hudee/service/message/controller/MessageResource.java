package com.babeeta.hudee.service.message.controller;

import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.hudee.service.message.MessageStringDefs;
import com.babeeta.hudee.service.message.service.MessageService;

/**
 * RESTful interface implements
 */
@Path("/")
public class MessageResource {

	private final static Logger logger = LoggerFactory
	        .getLogger(MessageResource.class);
	private static final Pattern idPattern = Pattern.compile("[a-f0-9A-F]{32}");
	private final Pattern tagPattern = Pattern.compile("[a-zA-Z0-9_]{3,32}");

	private MessageService messageService;

	private static AtomicLong totalCreateCount = new AtomicLong(0);
	private static AtomicLong totalCreateParentCount = new AtomicLong(0);
	private static AtomicLong totalCreateRefCount = new AtomicLong(0);
	private static AtomicLong totalCreateEventCount = new AtomicLong(0);
	private static AtomicLong totalCreateConfirmCount = new AtomicLong(0);
	private static AtomicLong totalUpdateCount = new AtomicLong(0);
	private static AtomicLong totalQueryCount = new AtomicLong(0);
	private static AtomicLong totalListCount = new AtomicLong(0);
	private static AtomicLong totalCountCount = new AtomicLong(0);

	private static final String MessageDefaultLife = "86400"; // one day in
	                                                          // second

	/**
	 * Set service instance of message, used by Spring framework.
	 * 
	 * @param messageService
	 *            instance of message service
	 */
	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}

	private boolean checkTagExpressionStr(String expression) {
		String[] tNameArray = expression.split("\\+");
		if (tNameArray == null) {
			logger.error("Bad reuqest without invalid tag expression : {}.",
			        expression);
			return false;
		} else {
			for (String tName : tNameArray) {
				if (!tagPattern.matcher(tName).matches()) {
					logger.error(
					        "Bad reuqest without illegal tag name : {}.",
					        tName);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * @param sender
	 *            application id
	 * @param recipient
	 *            client id
	 * @param device
	 *            device id
	 * @param life
	 *            message life
	 * @param content
	 *            message content in byte array
	 * @return HTTP response
	 */
	@POST
	@Path("/create/{sender}/{recipient}/{device}")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	public Response saveMessage(@PathParam("sender") String sender,
	        @PathParam("recipient") String recipient,
	        @PathParam("device") String device,
	        @HeaderParam("life") @DefaultValue(MessageDefaultLife) int life,
	        byte[] content) {
		logger.debug(
		        "Create Message: sender = {}, recipient = {}, device = {}.",
		        new Object[] { sender, recipient, device });

		if (sender == null || recipient == null || device == null
		        || content == null) {
			logger.error(
			        "Bad request! sender {} or recipient {} or device {} or content {} is null.",
			        new Object[] { sender == null, recipient == null,
			                device == null, content == null });
			return Response.status(HttpStatus.SC_BAD_REQUEST).build();
		}

		if (!idPattern.matcher(recipient).matches()) {
			logger.error("Bad request! Invalid recipient!");
			return Response.status(HttpStatus.SC_BAD_REQUEST).build();
		}

		if (!idPattern.matcher(sender).matches()) {
			logger.error("Bad request! Invalid sender!");
			return Response.status(HttpStatus.SC_BAD_REQUEST).build();
		}

		if (!idPattern.matcher(device).matches()) {
			logger.error("Bad request! Invalid device!");
			return Response.status(HttpStatus.SC_BAD_REQUEST).build();
		}

		try {
			String result = messageService.saveMessage(sender,
			        recipient, device, life, content);

			if (result != null) {
				// for performance monitor start
				totalCreateCount.getAndIncrement();
				// for performance monitor end

				return Response.status(HttpStatus.SC_CREATED).entity(result)
				        .build();
			} else {
				logger.error("Save message failed");
				return Response.serverError().build();
			}
		} catch (Exception e) {
			logger.error("Caught exception : " + e.getMessage());
			return Response.serverError().build();
		}
	}

	/**
	 * Create a new parent message for broadcast or multicast
	 * 
	 * @param sender
	 *            application id
	 * @param recipient
	 *            client id
	 * @param life
	 *            message life
	 * @param content
	 *            message content in byte array
	 * @return HTTP response
	 */
	@POST
	@Path("/create/parent/{sender}/{recipient}")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	public Response saveParentMessage(@PathParam("sender") String sender,
	        @PathParam("recipient") String recipient,
	        @HeaderParam("life") @DefaultValue(MessageDefaultLife) int life,
	        byte[] content) {

		logger.debug(
		        "Create Parent Message: sender = {}, recipient = {}..",
		        new Object[] { sender, recipient });

		if (sender == null || recipient == null || content == null) {
			logger.error(
			        "Bad request! sender {} or recipient {} or content {} is null.",
			        new Object[] { sender == null, recipient == null,
			                content == null });
			return Response.status(HttpStatus.SC_BAD_REQUEST).build();
		}

		if (!idPattern.matcher(recipient).matches()
		        && !checkTagExpressionStr(recipient)) {
			logger.error("Bad request! Invalid recipient! {}", recipient);
			return Response.status(HttpStatus.SC_BAD_REQUEST).build();
		}

		if (!idPattern.matcher(sender).matches()) {
			logger.error("Bad request! Invalid sender!");
			return Response.status(HttpStatus.SC_BAD_REQUEST).build();
		}

		try {
			String result = messageService.saveParentMessage(sender, recipient,
			        life, content);

			if (result != null) {
				// for performance monitor start
				totalCreateParentCount.getAndIncrement();
				// for performance monitor end
				return Response.status(HttpStatus.SC_CREATED).entity(result)
				        .build();
			} else {
				logger.error("Save parent message failed");
				return Response.serverError().build();
			}
		} catch (Exception e) {
			logger.error("Caught exception : " + e.getMessage());
			return Response.serverError().build();
		}
	}

	/**
	 * Create a new reference message
	 * 
	 * @param sender
	 *            application id
	 * @param recipient
	 *            client id
	 * @param device
	 *            device id
	 * @param parent
	 *            parent message id
	 * @param life
	 *            message life
	 * @return HTTP response
	 */
	@POST
	@Path("/create/reference/{sender}/{recipient}/{device}/{parent}")
	public Response saveReferenceMessage(@PathParam("sender") String sender,
	        @PathParam("recipient") String recipient,
	        @PathParam("device") String device,
	        @PathParam("parent") String parent,
	        @HeaderParam("life") @DefaultValue(MessageDefaultLife) int life) {

		logger.debug(
		        "Create Reference Message: sender = {}, recipient = {}, device = {}, parent = {}.",
		        new Object[] { sender, recipient, device, parent });

		if (sender == null || recipient == null || device == null
		        || parent == null) {
			logger.error(
			        "Bad request! sender {} or recipient {} or device {} or parent {} is null.",
			        new Object[] { sender == null, recipient == null,
			                device == null, parent == null });
			return Response.status(HttpStatus.SC_BAD_REQUEST).build();
		}

		if (!idPattern.matcher(recipient).matches()) {
			logger.error("Bad request! Invalid recipient! {}", recipient);
			return Response.status(HttpStatus.SC_BAD_REQUEST).build();
		}

		if (!idPattern.matcher(sender).matches()) {
			logger.error("Bad request! Invalid sender!");
			return Response.status(HttpStatus.SC_BAD_REQUEST).build();
		}

		if (!idPattern.matcher(device).matches()) {
			logger.error("Bad request! Invalid device!");
			return Response.status(HttpStatus.SC_BAD_REQUEST).build();
		}

		if (!idPattern.matcher(parent).matches()) {
			logger.error("Bad request! Invalid parent!");
			return Response.status(HttpStatus.SC_BAD_REQUEST).build();
		}

		try {
			// TODO : think whether need check the status of parent message
			String parentStatus = messageService.getMessageStatus(parent);

			JSONObject parentStatusObj = new JSONObject(parentStatus);
			String status = parentStatusObj
			        .getString(MessageStringDefs.DB_FIELD_STATUS);
			if (status.equalsIgnoreCase(MessageStringDefs.STATUS_DELIVERING)) {
				String result = messageService.saveReferenceMessage(parent,
				        sender,
				        recipient, life, device);

				if (result != null) {
					// for performance monitor start
					totalCreateRefCount.getAndIncrement();
					// for performance monitor end
					return Response.status(HttpStatus.SC_CREATED)
					        .entity(result)
					        .build();
				} else {
					logger.error("Save reference message failed");
					return Response.serverError().build();
				}
			} else {
				logger.error(
				        "Bad request! Invalid parent! Parent message in status {}.",
				        status);
				return Response.status(HttpStatus.SC_BAD_REQUEST).build();
			}
		} catch (Exception e) {
			logger.error("Caught exception : " + e.getMessage());
			return Response.serverError().build();
		}
	}

	/**
	 * Create a new event message
	 * 
	 * @param sender
	 *            application id
	 * @param life
	 *            message life
	 * @param content
	 *            message content in byte array
	 * @return HTTP response
	 */
	@POST
	@Path("/create/event/{sender}")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	public Response saveEventMessage(@PathParam("sender") String sender,
	        @HeaderParam("life") @DefaultValue(MessageDefaultLife) int life,
	        byte[] content) {

		logger.debug(
		        "Create event Message: sender = {}.",
		        new Object[] { sender });

		if (sender == null || content == null) {
			logger.error(
			        "Bad request! sender {} or content {} is null.",
			        new Object[] { sender == null, content == null });
			return Response.status(HttpStatus.SC_BAD_REQUEST).build();
		}
		if (!idPattern.matcher(sender).matches()) {
			logger.error("Bad request! Invalid sender!");
			return Response.status(HttpStatus.SC_BAD_REQUEST).build();
		}

		try {
			String result = messageService.saveEventMessage(sender, life,
			        content);

			if (result != null) {
				// for performance monitor start
				totalCreateEventCount.getAndIncrement();
				// for performance monitor end
				return Response.status(HttpStatus.SC_CREATED).entity(result)
				        .build();
			} else {
				logger.error("Save event message failed");
				return Response.serverError().build();
			}
		} catch (Exception e) {
			logger.error("Caught exception : " + e.getMessage());
			return Response.serverError().build();
		}
	}

	/**
	 * Create a confirm message
	 * 
	 * @param parent
	 *            parent message id
	 * @param device
	 *            device id
	 * @return HTTP response
	 */
	@POST
	@Path("/create/confirm/{device}/{parent}")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	public Response saveConfirmMessage(@PathParam("parent") String parent,
	        @PathParam("device") String device) {

		logger.debug("Create confirm Message: parent = {}, device = {}.",
		        new Object[] { parent, device });

		if (parent == null || device == null) {
			logger.error(
			        "Bad request! parent {} or device {} is null.",
			        new Object[] { parent == null, device == null });
			return Response.status(HttpStatus.SC_BAD_REQUEST).build();
		}

		if (!idPattern.matcher(parent).matches()) {
			logger.error("Bad request! Invalid parent! {}", parent);
			return Response.status(HttpStatus.SC_BAD_REQUEST).build();
		}
		if (!idPattern.matcher(device).matches()) {
			logger.error("Bad request! Invalid parent! {}", device);
			return Response.status(HttpStatus.SC_BAD_REQUEST).build();
		}
		try {
			String result = messageService.saveConfirmMessage(parent, device);

			if (result != null) {
				// for performance monitor start
				totalCreateConfirmCount.getAndIncrement();
				// for performance monitor end
				return Response.status(HttpStatus.SC_CREATED).entity(result)
				        .build();
			} else {
				logger.error("Save confirm message failed");
				return Response.serverError().build();
			}
		} catch (Exception e) {
			logger.error("Caught exception : " + e.getMessage());
			return Response.serverError().build();
		}
	}

	/**
	 * @param id
	 *            message id
	 * @param newStatus
	 *            new message status
	 * @return HTTP response
	 */
	@PUT
	@Path("/{msgId}/status/update")
	public Response updateStatus(@PathParam("msgId") String id,
	        @QueryParam("status") @DefaultValue("ACKED") String newStatus) {
		logger.debug("Update Message {} to status = {}.", id,
		        newStatus);
		if (id == null || newStatus == null) {
			logger.error(
			        "Bad request! messageId {} or newStatus {} is null.",
			        id == null, newStatus == null);
			return Response.status(HttpStatus.SC_BAD_REQUEST).build();
		}

		if (!idPattern.matcher(id).matches()) {
			logger.error("Bad request! Invalid message id!");
			return Response.status(HttpStatus.SC_BAD_REQUEST).build();
		}

		if (newStatus.equalsIgnoreCase("ACKED")) {
			// for performance monitor start
			totalUpdateCount.getAndIncrement();
			// for performance monitor end

			boolean result = messageService.updateAck(id);

			if (result) {
				return Response.ok().build();
			}
			return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).build();
		} else {
			logger.error(
			        "Bad request! newStatus [{}] is invalid.", newStatus);
			return Response.status(HttpStatus.SC_FORBIDDEN).build();
		}
	}

	/**
	 * @param id
	 *            message id
	 * @return HTTP response
	 */
	@GET
	@Path("/status/query")
	public Response queryStatus(@QueryParam("id") String id) {
		logger.debug("Query status of Message [{}].", id);
		if (id == null) {
			logger.error(
			        "Bad request! messageId is null.");
			return Response.status(HttpStatus.SC_BAD_REQUEST).build();
		}

		if (!idPattern.matcher(id).matches()) {
			logger.error("Bad request! Invalid message id!");
			return Response.status(HttpStatus.SC_BAD_REQUEST).build();
		}

		// for performance monitor start
		totalQueryCount.getAndIncrement();
		// for performance monitor end

		String result = messageService.getMessageStatus(id);

		if (result != null) {
			return Response.ok(result).build();
		}
		return Response.status(HttpStatus.SC_NOT_FOUND).build();
	}

	/**
	 * @param device
	 *            device id
	 * @param aid
	 *            special aid
	 * @param status
	 *            message status
	 * @param pageSize
	 *            page size
	 * @param pageIndex
	 *            page index
	 * @return HTTP response
	 */
	@GET
	@Path("/list")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response queryMessage(
	        @QueryParam("device") String device,
	        @QueryParam("aid") String aid,
	        @QueryParam("status") @DefaultValue("DELIVERING") String status,
	        @QueryParam("pageSize") @DefaultValue("-1") int pageSize,
	        @QueryParam("pageIndex") @DefaultValue("-1") int pageIndex) {
		logger.debug("List Message: device = {}, status = {}.", device,
		        status);
		if (device == null || status == null) {
			logger.error(
			        "Bad request! deviceId {} or status {} is null.",
			        device == null, status == null);
			return Response.status(HttpStatus.SC_BAD_REQUEST).build();
		}

		if (!idPattern.matcher(device).matches()) {
			logger.error("Bad request! Invalid device id!");
			return Response.status(HttpStatus.SC_BAD_REQUEST).build();
		}

		try {
			if (pageSize <= 0 || pageIndex < 0) {
				pageSize = -1;
				pageIndex = -1;
			}

			// for performance monitor start
			totalListCount.getAndIncrement();
			// for performance monitor end
			byte[] result = null;
			if (aid != null && !"null".equalsIgnoreCase(aid)) {
				result = messageService.getMessagesList(device, status,
				        pageSize, pageIndex, aid);
			} else {
				result = messageService.getMessagesList(device, status,
				        pageSize, pageIndex, null);
			}

			if (result != null) {
				logger.debug("response entity size:{}", result.length);
				return Response.ok(result).build();
			}
			return Response.status(HttpStatus.SC_NOT_FOUND).build();
		} catch (Exception e) {
			logger.error(
			        "Caught exception in list message(in status {}) about {}.",
			        status, device);
			logger.error(e.getMessage());
			return Response.serverError().build();
		}
	}

	/**
	 * @param device
	 *            device id
	 * @param aid
	 *            special aid
	 * @param status
	 *            message status
	 * @return HTTP response
	 */
	@GET
	@Path("/count")
	public Response countMessage(
	        @QueryParam("device") String device,
	        @QueryParam("aid") String aid,
	        @QueryParam("status") @DefaultValue("DELIVERING") String status) {
		logger.debug("Count Message: device = {}, status = {}.", device,
		        status);
		if (device == null || status == null) {
			logger.error(
			        "Bad request! deviceId {} or status {} is null.",
			        device == null, status == null);
			return Response.status(HttpStatus.SC_BAD_REQUEST).build();
		}

		if (!idPattern.matcher(device).matches()) {
			logger.error("Bad request! Invalid device id!");
			return Response.status(HttpStatus.SC_BAD_REQUEST).build();
		}

		try {
			// for performance monitor start
			totalCountCount.getAndIncrement();
			// for performance monitor end
			long result = 0;
			if (aid != null && !"null".equalsIgnoreCase(aid)) {
				result = messageService.countMessage(device, status, aid);
			} else {
				result = messageService.countMessage(device, status, null);
			}

			if (result > 0) {
				logger.debug("[{}:{}]Count result of :", new Object[] { device,
				        status, result });
				return Response.ok(result).build();
			}
			return Response.status(HttpStatus.SC_NOT_FOUND).build();
		} catch (Exception e) {
			logger.error(
			        "Caught exception in count message(in status {}) about {}.",
			        status, device);
			logger.error(e.getMessage());
			return Response.serverError().build();
		}
	}

	/**
	 * @param messageId
	 *            message id
	 * @return HTTP response
	 */
	@GET
	@Path("/get")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getMessage(
	        @QueryParam("id") String messageId) {

		if (messageId == null) {
			logger.error("Bad request! messageId is null.");
			return Response.status(HttpStatus.SC_BAD_REQUEST).build();
		}

		if (!idPattern.matcher(messageId).matches()) {
			logger.error("Bad request! Invalid message id!");
			return Response.status(HttpStatus.SC_BAD_REQUEST).build();
		}

		try {
			byte[] result = messageService.getMessages(messageId);

			if (result != null) {
				return Response.ok(result).build();
			}
			return Response.status(HttpStatus.SC_NOT_FOUND).build();
		} catch (Exception e) {
			logger.error("Caught exception in get message {}.", messageId);
			logger.error(e.getMessage());
			return Response.serverError().build();
		}
	}

	/**
	 * @param reset
	 *            whether reset counter
	 * @return the value of counter (before reset)
	 */
	public synchronized static long getTotalCreateCount(boolean reset) {
		if (reset) {
			return totalCreateCount.getAndSet(0);
		} else {
			return totalCreateCount.get();
		}
	}

	/**
	 * @param reset
	 *            whether reset counter
	 * @return the value of counter (before reset)
	 */
	public synchronized static long getTotalUpdateCount(boolean reset) {
		if (reset) {
			return totalUpdateCount.getAndSet(0);
		} else {
			return totalUpdateCount.get();
		}
	}

	/**
	 * @param reset
	 *            whether reset counter
	 * @return the value of counter (before reset)
	 */
	public synchronized static long getTotalQueryCount(boolean reset) {
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
	public synchronized static long getTotalListCount(boolean reset) {
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
	public synchronized static long getTotalCountCount(boolean reset) {
		if (reset) {
			return totalCountCount.getAndSet(0);
		} else {
			return totalCountCount.get();
		}
	}
}
