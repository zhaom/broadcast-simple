package com.babeeta.hudee.service.account.dao.impl;

import java.util.List;

import com.babeeta.hudee.service.account.AppAccountStringDefs;
import com.babeeta.hudee.service.account.dao.AppAccountDao;
import com.babeeta.hudee.service.account.entity.AppAccount;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;

/**
 * Implement of account DAO
 */
public class AppAccountDaoImpl extends AppBasicDaoImpl implements AppAccountDao {
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
	public AppAccountDaoImpl(String host, int port, String dbName) {
		super(host, port, dbName);
	}

	@Override
	public void deleteById(String id) {
		AppAccount account = new AppAccount();
		account.setId(id);
		datastore.delete(account);
	}

	@Override
	public AppAccount insertAccount(AppAccount account) {
		datastore.save(account);
		return account;
	}

	@Override
	public AppAccount selectById(String id) {
		AppAccount account = new AppAccount();
		account.setId(id);
		account = datastore.get(account);
		return account;
	}

	@Override
	public List<AppAccount> selectByStatus(String status) {
		Query<AppAccount> query = datastore.createQuery(AppAccount.class)
		        .filter(AppAccountStringDefs.DB_FIELD_STATUS, status);

		return query.asList();
	}

	@Override
	public void updateStatus(String id, String status) {
		Query<AppAccount> query = datastore.createQuery(AppAccount.class)
		        .filter(AppAccountStringDefs.DB_FIELD_ID, id);

		UpdateOperations<AppAccount> ops = datastore.createUpdateOperations(
		        AppAccount.class).set(AppAccountStringDefs.DB_FIELD_STATUS,
		        status);
		datastore.findAndModify(query, ops, true, false);
	}

    @Override
    public int count() {
        long count=datastore.createQuery(AppAccount.class).countAll();
        return Integer.parseInt(String.valueOf(count));
    }
}
