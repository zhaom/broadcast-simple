package com.babeeta.hudee.service.message.dao;

/**
 *
 */
public interface MessageDao {
	/**
	 * Create a new record for a new message with given info
	 * 
	 * @param uid
	 *            message id
	 * @param sender
	 *            sender
	 * @param recipient
	 *            recipient
	 * @param device
	 *            message owner
	 * @param life
	 *            message life
	 * @param content
	 *            message content in byte array
	 * @return the result of create new message
	 */
	boolean save(String uid, String sender, String recipient, String device,
	        int life, byte[] content);

	/**
	 * Create a new record for a new message with given info
	 * 
	 * @param uid
	 *            message id
	 * @param sender
	 *            sender
	 * @param recipient
	 *            recipient
	 * @param life
	 *            message life
	 * @param content
	 *            message content in byte array
	 * @return the result of create new message
	 */
	boolean save(String uid, String sender, String recipient, int life,
	        byte[] content);

	/**
	 * Create a new record for a new message with given info
	 * 
	 * @param uid
	 *            message id
	 * @param sender
	 *            sender
	 * @param life
	 *            message life
	 * @param content
	 *            message content in byte array
	 * @return the result of create new message
	 */
	boolean save(String uid, String sender, int life, byte[] content);

	/**
	 * Create a new record for a new message with given info
	 * 
	 * @param uid
	 *            message id
	 * @param sender
	 *            sender
	 * @param recipient
	 *            recipient
	 * @param device
	 *            message owner
	 * @param life
	 *            message life
	 * @param parent
	 *            parent message id
	 * @return the result of create new message
	 */
	boolean save(String uid, String sender, String recipient, String device,
	        int life, String parent);

	/**
	 * Create a new record for a new message with given info
	 * 
	 * @param uid
	 *            message id
	 * @param device
	 *            message owner
	 * @param parent
	 *            parent message id
	 * @return the result of create new message
	 */
	boolean save(String uid, String device, String parent);

	/**
	 * Get the status of appointed message
	 * 
	 * @param uid
	 *            message id
	 * @return the status of appointed message
	 */
	String getStatus(String uid);

	/**
	 * Query message owned by given device id and stand in appointed status
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
	 * @return BSON encoding byte array
	 */
	byte[] query(String did, String status, int pageSize, int pageIndex,
	        String aid);

	/**
	 * Update appointed message's status
	 * 
	 * @param uid
	 *            message id
	 * @param newStatus
	 *            new status
	 */
	void updateStatus(String uid, String newStatus);

	/**
	 * Count message owned by given device id
	 * 
	 * @param did
	 *            device id
	 * @param status
	 *            status of message stand in
	 * @param aid
	 *            special aid
	 * @return the count of message owned by given device id
	 */
	long count(String did, String status, String aid);

	/**
	 * Count the confirm message about parent message
	 * 
	 * @param parentId
	 *            parent message id
	 * @return the count of confirm message about parent message
	 */
	long countConfirm(String parentId);

	/**
	 * Select one message by message id
	 * 
	 * @param uid
	 *            message id
	 * @return message content encode in byte
	 */
	byte[] selectMessage(String uid);

	/**
	 * Delete confirm message
	 * 
	 * @param parent
	 *            parent message id
	 */
	void deleteConfirms(String parent);

	/**
	 * Delete reference message
	 * 
	 * @param parent
	 *            parent message id
	 */
	void deleteReference(String parent);
}
