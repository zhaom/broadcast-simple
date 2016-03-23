package com.babeeta.hudee.service.subscription.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.babeeta.hudee.service.subscription.SubscriptionStringDefs;
import com.babeeta.hudee.service.subscription.dao.SubscriptionDao;
import com.babeeta.hudee.service.subscription.entity.Subscription;
import com.google.code.morphia.query.Query;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 *
 */
public class SubscriptionDaoImpl extends BasicDaoImpl implements
        SubscriptionDao {

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
	public SubscriptionDaoImpl(String host, int port, String dbName) {
		super(host, port, dbName);
	}

	@Override
	public Subscription insert(Subscription sub) {
		datastore.save(sub);
		return sub;
	}

	@Override
	public void deleteById(String id) {
		datastore.delete(Subscription.class, id);
	}

	@Override
	public void deleteByKey(String key) {
		Query<Subscription> query = datastore.createQuery(Subscription.class)
		        .filter(SubscriptionStringDefs.DB_FIELD_KEY, key);
		datastore.delete(query);
	}

	@Override
	public void deleteByOwner(String owner) {
		Query<Subscription> query = datastore.createQuery(Subscription.class)
		        .filter(SubscriptionStringDefs.DB_FIELD_OWNER, owner);
		datastore.delete(query);
	}

	@Override
	public void deleteByDid(String did) {
		Query<Subscription> query = datastore.createQuery(Subscription.class)
		        .filter(SubscriptionStringDefs.DB_FIELD_DID, did);
		datastore.delete(query);
	}

	@Override
	public void delete(String key, String did) {
		Query<Subscription> query = datastore.createQuery(Subscription.class)
		        .filter(SubscriptionStringDefs.DB_FIELD_KEY, key)
		        .filter(SubscriptionStringDefs.DB_FIELD_DID, did);
		datastore.delete(query);
	}

	@Override
	public Subscription selectByKey(String key) {
		Query<Subscription> query = datastore.createQuery(Subscription.class)
		        .filter(SubscriptionStringDefs.DB_FIELD_KEY, key);
		return query.get();
	}

	@Override
	public List<Subscription> listByDid(String did) {
		Query<Subscription> query = datastore.createQuery(Subscription.class)
		        .filter(SubscriptionStringDefs.DB_FIELD_DID, did);
		return query.asList();
	}

	@Override
	public List<Subscription> listByOwner(String owner) {
		Query<Subscription> query = datastore.createQuery(Subscription.class)
		        .filter(SubscriptionStringDefs.DB_FIELD_OWNER, owner);
		return query.asList();
	}

	@Override
	public List<Subscription> listByKey(String key) {
		Query<Subscription> query = datastore.createQuery(Subscription.class)
		        .filter(SubscriptionStringDefs.DB_FIELD_KEY, key);
		return query.asList();
	}

	@Override
	public long countByKey(String key, long stamp) {
		Query<Subscription> query = datastore.createQuery(Subscription.class)
		        .filter(SubscriptionStringDefs.DB_FIELD_KEY, key)
		        .filter(SubscriptionStringDefs.DB_FIELD_CREATEDAT + " <=",
		                new Date(stamp));
		return datastore.getCount(query);
	}

	@Override
	public long countByOwner(String owner, long stamp) {
		Query<Subscription> query = datastore.createQuery(Subscription.class)
		        .filter(SubscriptionStringDefs.DB_FIELD_OWNER, owner)
		        .filter(SubscriptionStringDefs.DB_FIELD_CREATEDAT + " <=",
		                new Date(stamp));
		return datastore.getCount(query);
	}

	@Override
	public long countByDid(String did, long stamp) {
		Query<Subscription> query = datastore.createQuery(Subscription.class)
		        .filter(SubscriptionStringDefs.DB_FIELD_DID, did)
		        .filter(SubscriptionStringDefs.DB_FIELD_CREATEDAT + " <=",
		                new Date(stamp));
		return datastore.getCount(query);
	}

	@Override
	public List<String> listDeviceByKey(String key, long stamp) {
		List<String> result = new ArrayList<String>();
		Query<Subscription> query = datastore
		        .createQuery(Subscription.class)
		        .filter(SubscriptionStringDefs.DB_FIELD_KEY, key)
		        .filter(SubscriptionStringDefs.DB_FIELD_CREATEDAT + " <=",
		                new Date(stamp))
		        .retrievedFields(true, SubscriptionStringDefs.DB_FIELD_DID,
		                SubscriptionStringDefs.DB_FIELD_OWNER);
		List<Subscription> queryResult = query.asList();
		for (Subscription sub : queryResult) {
			String deviceKey = sub.getDid() + "."
			        + sub.getOwner().substring(32);
			if (!result.contains(deviceKey)) {
				result.add(deviceKey);
			}
		}
		return result;
	}

	@Override
	public List<String> listDeviceByOwner(String owner, long stamp) {
		List<String> result = new ArrayList<String>();
		Query<Subscription> query = datastore
		        .createQuery(Subscription.class)
		        .filter(SubscriptionStringDefs.DB_FIELD_OWNER, owner)
		        .filter(SubscriptionStringDefs.DB_FIELD_CREATEDAT + " <=",
		                new Date(stamp))
		        .retrievedFields(true, SubscriptionStringDefs.DB_FIELD_DID,
		                SubscriptionStringDefs.DB_FIELD_KEY);
		List<Subscription> queryResult = query.asList();
		for (Subscription sub : queryResult) {
			String deviceKey = sub.getDid() + "."
			        + sub.getKey().substring(32);
			if (!result.contains(deviceKey)) {
				result.add(deviceKey);
			}
		}
		return result;
	}

	// TODO: remove from next version begin
	@Override
	public void oldDelete(String aid, String cid) {
		BasicDBObject doc = new BasicDBObject();
		BasicDBObject key = new BasicDBObject();
		key.put("aid", aid);
		key.put("cid", cid);
		doc.put("_id", key);
		BasicDBObject noExists = new BasicDBObject();
		noExists.put("$exists", false);
		doc.put("tName", noExists);
		collection.remove(doc);
	}

	@Override
	public String oldSelectByKey(String aid, String cid) {
		String did = null;
		BasicDBObject doc = new BasicDBObject();
		BasicDBObject key = new BasicDBObject();
		key.put("aid", aid);
		key.put("cid", cid);

		doc.put("_id", key);
		BasicDBObject noExists = new BasicDBObject();
		noExists.put("$exists", false);
		doc.put("tName", noExists);

		DBObject result = collection.findOne(doc);
		if (result != null) {
			did = result.get("did").toString();
		}

		return did;
	}

	@Override
	public void oldDeleteUseless(String aid, String did) {
		BasicDBObject doc = new BasicDBObject();
		doc.put("_id.aid", aid);
		doc.put("did", did);
		BasicDBObject noExists = new BasicDBObject();
		noExists.put("$exists", false);
		doc.put("tName", noExists);

		collection.remove(doc);
	}

	// TODO: remove from next version end

	@Override
	public List<String> listDeviceByKey(String key, long stamp, int amount,
	        int offset) {
		List<String> result = new ArrayList<String>();
		Query<Subscription> query = datastore
		        .createQuery(Subscription.class)
		        .filter(SubscriptionStringDefs.DB_FIELD_KEY, key)
		        .filter(SubscriptionStringDefs.DB_FIELD_CREATEDAT + " <=",
		                new Date(stamp))
		        .retrievedFields(true, SubscriptionStringDefs.DB_FIELD_DID,
		                SubscriptionStringDefs.DB_FIELD_OWNER)
		        .limit(amount).offset(offset);
		List<Subscription> queryResult = query.asList();
		for (Subscription sub : queryResult) {
			String deviceKey = sub.getDid() + "."
			        + sub.getOwner().substring(32);
			if (!result.contains(deviceKey)) {
				result.add(deviceKey);
			}
		}
		return result;
	}

	@Override
	public List<String> listDeviceByOwner(String owner, long stamp, int amount,
	        int offset) {
		List<String> result = new ArrayList<String>();
		Query<Subscription> query = datastore
		        .createQuery(Subscription.class)
		        .filter(SubscriptionStringDefs.DB_FIELD_OWNER, owner)
		        .filter(SubscriptionStringDefs.DB_FIELD_CREATEDAT + " <=",
		                new Date(stamp))
		        .retrievedFields(true, SubscriptionStringDefs.DB_FIELD_DID,
		                SubscriptionStringDefs.DB_FIELD_KEY)
		        .limit(amount).offset(offset);
		List<Subscription> queryResult = query.asList();
		for (Subscription sub : queryResult) {
			String deviceKey = sub.getDid() + "."
			        + sub.getKey().substring(32);
			if (!result.contains(deviceKey)) {
				result.add(deviceKey);
			}
		}
		return result;
	}
}
