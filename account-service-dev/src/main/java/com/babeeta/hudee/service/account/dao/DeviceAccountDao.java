package com.babeeta.hudee.service.account.dao;

import com.babeeta.hudee.service.account.entity.DeviceAccount;

/**
 * Interface of DAO
 * 
 */
public interface DeviceAccountDao {
	/**
	 * Delete account by id
	 * 
	 * @param id
	 *            device account id
	 */
	void deleteById(String id);

	/**
	 * Insert new device account
	 * 
	 * @param account
	 *            new device account
	 * @return the account inserted
	 */
	DeviceAccount insert(DeviceAccount account);

	/**
	 * Query device account by id
	 * 
	 * @param id
	 *            device account id
	 * @return device account if given id valid
	 */
	DeviceAccount queryById(String id);

	/**
	 * Query device account by identifier
	 * 
	 * @param identifier
	 *            device account identifier
	 * @return device account if given id valid
	 */
	DeviceAccount queryByIdentifier(String identifier);
}
