package com.babeeta.hudee.service.account.service;

import java.util.Map;

import com.babeeta.hudee.service.account.entity.DeviceAccount;

/**
 * Interface of device account service
 * 
 */
public interface DeviceAccountService {
	/**
	 * do authentication
	 * 
	 * @param id
	 *            id of device account
	 * @param key
	 *            key of device account
	 * @return a result in JSON
	 */
	String auth(String id, String key);

	/**
	 * Get a device account by id
	 * 
	 * @param id
	 *            appointed id
	 * @return the device account if given id valid
	 */
	DeviceAccount getAccount(String id);

	/**
	 * Register a new device account
	 * 
	 * @param info
	 *            other info submit by client
	 * @return the new device account if register successful
	 */
	DeviceAccount register(Map<String, Object> info);
}
