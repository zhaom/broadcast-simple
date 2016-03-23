package com.babeeta.hudee.service.account.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.hudee.common.util.OptimizedIdFactory;
import com.babeeta.hudee.service.account.AppAccountStringDefs;
import com.babeeta.hudee.service.account.dao.AppAccountDao;
import com.babeeta.hudee.service.account.entity.AppAccount;
import com.babeeta.hudee.service.account.service.AppAccountService;

/**
 * Implements of interface "AppAccountService"
 * 
 */
public class AppAccountServiceImpl implements AppAccountService {

	private final static Logger logger = LoggerFactory
	        .getLogger(AppAccountServiceImpl.class);

	private AppAccountDao appAccountDao;

	/**
	 * Set DAO of account, used by Spring framework.
	 * 
	 * @param appAccountDao
	 *            instance of account DAO
	 */
	public void setAppAccountDao(AppAccountDao appAccountDao) {
		this.appAccountDao = appAccountDao;
	}

	private String composeReturnJson(String status) {
		return new StringBuilder("{\"status\":\"").append(status).append("\"}")
		        .toString();
	}

	@Override
	public String auth(String id, String key) {
		AppAccount account = appAccountDao.selectById(id);

		if (account != null && (account.getKey() != null
		        && account.getKey().equals(key))) {
			if (AppAccountStringDefs.STATUS_NORMAL.equals(account.getStatus())) {
				logger.debug("Auth success, id:{},key:{}", account.getId(),
				        account.getKey());
				return composeReturnJson(AppAccountStringDefs.STATUS_OK);
			} else {
				logger.warn(
				        "Auth failed,reason for 'FREEZED' status, Id:{},key:{}",
				        account.getId(),
				        account.getKey());
				return composeReturnJson(AppAccountStringDefs.STATUS_FREEZED);
			}
		}
		logger.warn("Auth failed, Id:{},key:{}", id, key);
		return composeReturnJson(AppAccountStringDefs.STATUS_FAILED);
	}

	@Override
	public AppAccount getAccountById(String id) {
		return appAccountDao.selectById(id);
	}

	@Override
	public List<AppAccount> ListAccountByStatus(String status) {
		return appAccountDao.selectByStatus(status);
	}

	@Override
	public void updateAccountStatus(String id, String status) {
		appAccountDao.updateStatus(id, status);
	}

    @Override
    public int count() {
        return appAccountDao.count();
    }

    @Override
	public AppAccount register(Map<String, Object> info) {
		logger.debug("Register account with {}", info.toString());
		AppAccount account = new AppAccount();
		account.setStatus(AppAccountStringDefs.STATUS_NORMAL);
		account.setId(OptimizedIdFactory.getOptimizedId());
		account.setKey(UUID.randomUUID().toString().replaceAll("-", ""));
		account.setCreateDate(new Date());
		account.setExtra(info);

		account = appAccountDao.insertAccount(account);

		logger.debug("Register result:  id:{},key:{}", account.getId(),
		        account.getKey());
		return account;
	}
}
