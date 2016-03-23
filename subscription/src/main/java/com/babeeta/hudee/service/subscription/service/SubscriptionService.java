package com.babeeta.hudee.service.subscription.service;

import java.util.List;

/**
 *
 */
public interface SubscriptionService {
	/**
	 * Subscribe
	 * 
	 * @param aid
	 *            application id
	 * @param cid
	 *            client id
	 * @param did
	 *            device id
	 * @return new client id
	 */
	String subscribe(String aid, String cid, String did);

	/**
	 * Unsubscribe
	 * 
	 * @param aid
	 *            application id
	 * @param cid
	 *            client id
	 * @param did
	 *            device id
	 * @return return true if succeed, otherwise return false.
	 */
	boolean unsubscribe(String aid, String cid, String did);

	/**
	 * Query device id
	 * 
	 * @param aid
	 *            application id
	 * @param cid
	 *            client id
	 * @return device id or null
	 */
	String queryDevice(String aid, String cid);

	/**
	 * List all subscriptions about device id
	 * 
	 * @param did
	 *            device id
	 * @return a list about pairs of application id and client id
	 */
	List<String> listSubscription(String did);

	/**
	 * Delete all subscription about given device id
	 * 
	 * @param did
	 *            device id
	 * @return count of delete effect items
	 */
	long deleteByDevice(String did);

	/**
	 * Add tags
	 * 
	 * @param aid
	 *            application id
	 * @param cid
	 *            client id
	 * @param tNameList
	 *            name of tags
	 * @return a list about created tags
	 */
	List<String> addTags(String aid, String cid, List<String> tNameList);

	/**
	 * Remove tags
	 * 
	 * @param aid
	 *            application id
	 * @param cid
	 *            client id
	 * @param tNameList
	 *            name of tags
	 * @return a list about deleted tags
	 */
	List<String> removeTags(String aid, String cid, List<String> tNameList);

	/**
	 * List tags of user (aid + cid)
	 * 
	 * @param aid
	 *            application id
	 * @param cid
	 *            client id
	 * @return a list of tags user owned
	 */
	List<String> listTags(String aid, String cid);

	/**
	 * Count the device which has tag
	 * 
	 * @param aid
	 *            application id
	 * @param tName
	 *            tag name
	 * @return count of device has tag
	 */
	long countDeviceByTags(String aid, String tName);

	/**
	 * Count the device which has subscription with aid
	 * 
	 * @param aid
	 *            application
	 * @return count result
	 */
	long countDeviceByOwner(String aid);

	/**
	 * List device has tag
	 * 
	 * @param aid
	 *            application id
	 * @param tName
	 *            tag name
	 * @return a list about device has tag
	 */
	List<String> listDeviceByTags(String aid, String tName);
}
