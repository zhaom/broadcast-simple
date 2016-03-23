package com.babeeta.hudee.service.account.service;

import java.util.List;
import java.util.Map;

import com.babeeta.hudee.service.account.entity.AppAccount;

/**
 * Interface of account service
 * 
 * @author Xinyu
 * 
 */
public interface AppAccountService {

	/**
	 * Do authentication
	 * 
	 * @param id
	 *            application id
	 * @param key
	 *            application key
	 * @return result in JSON
	 */
	String auth(String id, String key);

	/**
	 * Get account entity by id
	 * 
	 * @param id
	 *            application id
	 * @return account entity or null
	 */
	AppAccount getAccountById(String id);

	/**
	 * List application account by status
	 * 
	 * @param status
	 *            appointed status
	 * @return a list of account entity with appointed status
	 */
	List<AppAccount> ListAccountByStatus(String status);

	/**
	 * Register a new application account
	 * 
	 * @param info
	 *            a map about account info
	 * @return entity of register result
	 */
	AppAccount register(Map<String, Object> info);

	/**
	 * Change status of account
	 * 
	 * @param id
	 *            appointed account id
	 * @param status
	 *            new status
	 */
	void updateAccountStatus(String id, String status);

    /**
     *
     * @return
     */
    public int count();
}
