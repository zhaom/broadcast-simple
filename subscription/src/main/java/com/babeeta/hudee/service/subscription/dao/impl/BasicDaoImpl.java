package com.babeeta.hudee.service.subscription.dao.impl;

import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.hudee.service.subscription.entity.Subscription;
import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.MongoOptions;
import com.mongodb.ServerAddress;

/**
 * Basic DAO implements
 */
public class BasicDaoImpl {
	private final static Logger logger = LoggerFactory
	        .getLogger(BasicDaoImpl.class);

	protected Mongo mongo = null;
	protected Morphia morphia = null;
	protected Datastore datastore = null;
	protected DBCollection collection = null;

	/**
	 * Constructor
	 * 
	 * @param host
	 *            address of mongoDB
	 * @param port
	 *            port of mongoDB
	 * @param dbName
	 *            name of mongoDB
	 */
	public BasicDaoImpl(String host, int port, String dbName) {
		logger.info("[BasicDao] init with : {}:{}::{}",
		        new Object[] { host, port, dbName });

		MongoOptions mongoOptions = new MongoOptions();
		mongoOptions.threadsAllowedToBlockForConnectionMultiplier = 120;
		mongoOptions.connectionsPerHost = 50;
		mongoOptions.autoConnectRetry = true;
		mongoOptions.socketKeepAlive = true;

		try {
			mongo = new Mongo(new ServerAddress(host, port),
			        mongoOptions);
		} catch (UnknownHostException e) {
			logger.error(
			        "[BasicDao] Can not connect to {}:{}.", host, port);
			e.printStackTrace();
		} catch (MongoException e) {
			logger.error(
			        "[BasicDao] Caught exception when init mongoDB: {}. ",
			        e.getMessage());
			e.printStackTrace();
		} finally {
			if (mongo != null) {
				morphia = new Morphia();
				morphia.map(Subscription.class);
				datastore = morphia
				        .createDatastore(mongo, dbName);
				datastore.ensureIndexes();
				collection = mongo.getDB(dbName).getCollection("subscription");
				logger.debug("[BasicDao] init success.");
			} else {
				logger.info("[BasicDao] init failed.");
			}
		}
	}
}
