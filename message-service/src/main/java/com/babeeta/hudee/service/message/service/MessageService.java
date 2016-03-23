package com.babeeta.hudee.service.message.service;

/**
 *
 */
public interface MessageService {

	/**
	 * List all message in appointed status owned by appointed device
	 * 
	 * @param did
	 *            device id
	 * @param status
	 *            message status
	 * @param pageSize
	 *            page size
	 * @param pageIndex
	 *            page index
	 * @param aid
	 *            special aid
	 * @return the byte array of message encoding by BSON
	 */
	byte[] getMessagesList(String did, String status, int pageSize,
	        int pageIndex, String aid);

	/**
	 * Get the status of appointed message
	 * 
	 * @param uid
	 *            message id
	 * @return status report in JSON
	 */
	String getMessageStatus(String uid);

	/**
	 * Create a new message
	 * 
	 * @param sender
	 *            sender
	 * @param recipient
	 *            recipient
	 * @param device
	 *            device id
	 * @param life
	 *            life
	 * @param content
	 *            content
	 * @return message id
	 */
	String saveMessage(String sender, String recipient,
	        String device, int life,
	        byte[] content);

	/**
	 * Update message status to ACK
	 * 
	 * @param uid
	 * @return the operation result
	 */
	boolean updateAck(String uid);

	/**
	 * Count message
	 * 
	 * @param did
	 *            message owner, device id
	 * @param status
	 *            message status
	 * @param aid
	 *            special aid
	 * @return the count of message owned by device id and standing in appointed
	 *         status
	 */
	long countMessage(String did, String status, String aid);

	/**
	 * Create a new parent message
	 * 
	 * @param sender
	 *            sender
	 * @param recipient
	 *            recipient
	 * @param life
	 *            life
	 * @param content
	 *            content
	 * @return message id
	 */
	String saveParentMessage(String sender, String recipient, int life,
	        byte[] content);

	/**
	 * Create a new parent message
	 * 
	 * @param sender
	 *            sender
	 * @param life
	 *            life
	 * @param content
	 *            content
	 * @return message id
	 */
	String saveEventMessage(String sender, int life, byte[] content);

	/**
	 * Create a new reference message
	 * 
	 * @param sender
	 *            sender
	 * @param recipient
	 *            recipient
	 * @param parent
	 *            parent message id
	 * @param device
	 *            device id
	 * @param life
	 *            life
	 * @return message id
	 */
	String saveReferenceMessage(String parent, String sender,
	        String recipient, int life, String device);

	/**
	 * Create a new confirm message
	 * 
	 * @param parent
	 *            parent message id
	 * @param device
	 *            device id
	 * @return message id
	 */
	String saveConfirmMessage(String parent, String device);

	/**
	 * Get a message
	 * 
	 * @param uid
	 *            message id
	 * @return message encoded in BSON
	 */
	byte[] getMessages(String uid);
}
