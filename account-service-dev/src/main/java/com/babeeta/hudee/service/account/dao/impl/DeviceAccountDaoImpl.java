package com.babeeta.hudee.service.account.dao.impl;

import java.util.Date;

import com.babeeta.hudee.service.account.DeviceAccountStringDefs;
import com.babeeta.hudee.service.account.dao.DeviceAccountDao;
import com.babeeta.hudee.service.account.entity.DeviceAccount;
import com.google.code.morphia.query.Query;

/**
 * Implement of account DAO
 */
public class DeviceAccountDaoImpl extends DeviceBasicDaoImpl implements
        DeviceAccountDao {
	private final long WEEK_IN_MS = 7 * 24 * 60 * 1000;

	/**
	 * Constructor
	 * 
	 * @param host
	 *            host of mongoDB
	 * @param port
	 *            port of mongoDB
	 * @param dbName
	 *            database name
	 */
	public DeviceAccountDaoImpl(String host, int port, String dbName) {
		super(host, port, dbName);
	}

	@Override
	public void deleteById(String id) {
		DeviceAccount account = new DeviceAccount();
		account.setId(id);
		datastore.delete(account);
	}

	@Override
	public DeviceAccount insert(DeviceAccount account) {
		datastore.save(account);
		return account;
	}

	@Override
	public DeviceAccount queryById(String id) {
		DeviceAccount account = new DeviceAccount();
		account.setId(id);
		account = datastore.get(account);

		if (account != null) {
			Date activateAt = account.getActivateAt();

			if (activateAt == null) {
				account.setActivateAt(new Date());
				datastore.save(account);
			} else {
				if (activateAt.before(new Date(System.currentTimeMillis()
				        + WEEK_IN_MS))) {
					account.setActivateAt(new Date());
					datastore.save(account);
				} else {
					account.setActivateAt(new Date());
				}
			}
		}
		return account;
	}

	@Override
	public DeviceAccount queryByIdentifier(String identifier) {
		Query<DeviceAccount> query = datastore
		        .createQuery(DeviceAccount.class)
		        .filter(DeviceAccountStringDefs.DB_FIELD_IDENTIFIER, identifier);
		return query.get();
	}
}
