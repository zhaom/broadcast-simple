package com.babeeta.hudee.service.account.dao;

import java.util.List;

import com.babeeta.hudee.service.account.entity.AppAccount;

/**
 * Interface of account DAO
 * 
 * @author Xinyu
 * 
 */
public interface AppAccountDao {
	/**
	 * Delete record by id
	 * 
	 * @param id
	 *            appointed record id
	 */
	void deleteById(String id);

	/**
	 * insert a new record
	 * 
	 * @param account
	 *            record entity
	 * @return inserted record entity
	 */
	AppAccount insertAccount(AppAccount account);

	/**
	 * Get a record by id
	 * 
	 * @param id
	 *            appointed record id
	 * @return record entity or null
	 */
	AppAccount selectById(String id);

	/**
	 * Query record by 'status'
	 * 
	 * @param status
	 *            appointed status
	 * @return a list about record with appointed status
	 */
	List<AppAccount> selectByStatus(String status);

	/**
	 * Change the 'status' of appointed record
	 * 
	 * @param id
	 *            appointed record id
	 * @param status
	 *            new status
	 */
	void updateStatus(String id, String status);

    public int count();
}
