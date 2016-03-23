package com.babeeta.hudee.service.subscription.dao;

import java.util.List;

import com.babeeta.hudee.service.subscription.entity.Subscription;

/**
 * 
 */
public interface SubscriptionDao {

	/**
	 * Insert a new subscription record
	 * 
	 * @param sub
	 *            the subscription entity to be inserted
	 * @return inserted subscription entity
	 */
	Subscription insert(Subscription sub);

	/**
	 * Delete a record by id
	 * 
	 * @param id
	 *            record id
	 */
	void deleteById(String id);

	/**
	 * Delete record by key
	 * 
	 * @param key
	 *            64 bits string composed by aid-cid or aid-tid
	 */
	void deleteByKey(String key);

	/**
	 * Delete record by owner
	 * 
	 * @param owner
	 *            owner
	 */
	void deleteByOwner(String owner);

	/**
	 * Delete record by device id
	 * 
	 * @param did
	 *            device id
	 */
	void deleteByDid(String did);

	/**
	 * Delete record by key and device id
	 * 
	 * @param key
	 *            64 bits string composed by aid-cid or aid-tid
	 * @param did
	 *            device id
	 */
	void delete(String key, String did);

	/**
	 * Select record by key
	 * 
	 * @param key
	 *            64 bits string composed by aid-cid
	 * @return subscription record
	 */
	Subscription selectByKey(String key);

	/**
	 * List record by device id
	 * 
	 * @param did
	 *            device id
	 * @return a list about subscription
	 */
	List<Subscription> listByDid(String did);

	/**
	 * List record by owner
	 * 
	 * @param owner
	 *            owner
	 * @return a list about subscription
	 */
	List<Subscription> listByOwner(String owner);

	/**
	 * List record by key
	 * 
	 * @param key
	 *            64 bits string composed by aid-tid
	 * @return a list about subscription
	 */
	List<Subscription> listByKey(String key);

	/**
	 * Get list about device id from record whose create time should before
	 * given stamp
	 * 
	 * @param key
	 *            64 bits string composed by aid-tid
	 * @param stamp
	 *            time in long
	 * @return a list about device key (did.cid)
	 */
	List<String> listDeviceByKey(String key, long stamp);

	/**
	 * Get list about device id from record whose create time should before
	 * given stamp
	 * 
	 * @param key
	 *            64 bits string composed by aid-tid
	 * @param stamp
	 *            time in long
	 * @param amount
	 *            amount of list result
	 * @param offset
	 *            offset of the start element in result of query
	 * @return a list about device key (did.cid)
	 */
	List<String> listDeviceByKey(String key, long stamp, int amount, int offset);

	/**
	 * Get list about device id from record whose create time should before
	 * given stamp
	 * 
	 * @param owner
	 *            application id
	 * @param stamp
	 *            time in long
	 * @return a list about device key (did.cid)
	 */
	List<String> listDeviceByOwner(String owner, long stamp);

	/**
	 * Get list about device id from record whose create time should before
	 * given stamp
	 * 
	 * @param owner
	 *            application id
	 * @param stamp
	 *            time in long
	 * @param amount
	 *            amount of list result
	 * @param offset
	 *            offset of the start element in result of query
	 * @return a list about device key (did.cid)
	 */
	List<String> listDeviceByOwner(String owner, long stamp, int amount,
	        int offset);

	/**
	 * Count record by key
	 * 
	 * @param key
	 *            64 bits string composed by aid-cid or aid-tid
	 * @param stamp
	 *            time in long
	 * @return value of count
	 */
	long countByKey(String key, long stamp);

	/**
	 * Count record by record
	 * 
	 * @param owner
	 *            owner
	 * @param stamp
	 *            time in long
	 * @return value of count
	 */
	long countByOwner(String owner, long stamp);

	/**
	 * Count record by device id
	 * 
	 * @param did
	 *            device id
	 * @param stamp
	 *            time in long
	 * @return value of count
	 */
	long countByDid(String did, long stamp);

	// TODO: remove from next version begin
	/**
	 * Select record by aid and cid
	 * 
	 * @param aid
	 *            application id
	 * @param cid
	 *            client id
	 * @return device id
	 */
	String oldSelectByKey(String aid, String cid);

	/**
	 * Delete record by aid and cid
	 * 
	 * @param aid
	 *            application id
	 * @param cid
	 *            client id
	 */
	void oldDelete(String aid, String cid);

	/**
	 * Try to delete all relationship about application id and device id
	 * 
	 * @param aid
	 *            application id
	 * @param did
	 *            device id
	 */
	void oldDeleteUseless(String aid, String did);
	// TODO: remove from next version end
}
