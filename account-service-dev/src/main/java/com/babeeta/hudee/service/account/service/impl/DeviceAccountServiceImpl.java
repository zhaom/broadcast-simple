package com.babeeta.hudee.service.account.service.impl;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.hudee.common.util.OptimizedIdFactory;
import com.babeeta.hudee.service.account.DeviceAccountStringDefs;
import com.babeeta.hudee.service.account.dao.DeviceAccountDao;
import com.babeeta.hudee.service.account.entity.DeviceAccount;
import com.babeeta.hudee.service.account.service.DeviceAccountService;

/**
 *
 */
public class DeviceAccountServiceImpl implements DeviceAccountService {

	private final static Logger logger = LoggerFactory
	        .getLogger(DeviceAccountServiceImpl.class);

	private DeviceAccountDao deviceAccountDao;

	/**
	 * Set DAO of account, used by Spring framework.
	 * 
	 * @param deviceAccountDao
	 *            instance of account DAO
	 */
	public void setDeviceAccountDao(DeviceAccountDao deviceAccountDao) {
		this.deviceAccountDao = deviceAccountDao;
	}

	private String composeReturnJson(String status, boolean mode) {
		// Xinyu modified: add work mode in reture JSON object
		return new StringBuilder("{\"status\":\"").append(status)
		        .append("\", \"push_mode\":\"").append(mode).append("\"}")
		        .toString();
	}

	@Override
	public String auth(String id, String key) {
		DeviceAccount account = deviceAccountDao.queryById(id);

		if (account != null && (account.getKey() != null
		        && account.getKey().equals(key))) {
			logger.debug("Auth success, id:{},key:{}", account.getId(),
			        account.getKey());
			// Xinyu modified: add work mode in reture JSON object
			return composeReturnJson(DeviceAccountStringDefs.STATUS_OK,
			        account.isPushMode());
		}
		logger.warn("Auth failed, Id:{},key:{}", id, key);
		// Xinyu modified: set work mode to pull as default
		return composeReturnJson(DeviceAccountStringDefs.STATUS_FAIL, false);
	}

	@Override
	public DeviceAccount getAccount(String id) {
		DeviceAccount result = deviceAccountDao.queryById(id);
		return result;
	}

	@Override
	public DeviceAccount register(Map<String, Object> info) {
		String identifier = generateIdentifier(info);

		DeviceAccount old = deviceAccountDao.queryByIdentifier(identifier);

		if (old != null) {
			logger.debug("There is a record about identifier <{}>: id {}",
			        identifier, old.getId());
			return old;
		} else {
			DeviceAccount account = new DeviceAccount();

			account.setId(OptimizedIdFactory.getOptimizedId());
			account.setIdentifier(identifier);
			account.setKey(UUID.randomUUID().toString()
			        .replaceAll("-", ""));
			account.setActivateAt(new Date());
			account.setExtra(info);
			// TODO: should set push mode flag by device submit information
			account.setPushMode(false);

			account = deviceAccountDao.insert(account);

			logger.debug("Register result: id={}, key={}", account.getId(),
			        account.getKey());
			return account;
		}
	}

	private String generateIdentifier(Map<String, Object> info) {
		String keyword = null;
		String imei = null;
		String wifiMac = null;

		if (info.containsKey("imei")) {
			imei = info.get("imei").toString();
		}

		if (info.containsKey("wifi")) {
			wifiMac = info.get("wifi").toString();
		}

		if (imei == null && wifiMac == null) {
			keyword = UUID.randomUUID().toString().replaceAll("-", "");
		} else if (wifiMac != null && imei == null) {
			// Pad without phone feature
			keyword = DigestUtils.md5Hex(wifiMac);
		} else {
			// phone
			keyword = DigestUtils.md5Hex(imei);
		}

		return keyword;
	}
}
