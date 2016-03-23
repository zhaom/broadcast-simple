package com.babeeta.hudee.service.account.dao.impl;

import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.hudee.service.account.entity.AppAccount;
import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.MongoOptions;
import com.mongodb.ServerAddress;

/**
 * Basic DAO implements with morphia supports
 */
public class AppBasicDaoImpl {
	private final static Logger logger = LoggerFactory
	        .getLogger(AppBasicDaoImpl.class);

	protected Morphia morphia = null;
	protected Mongo mongo = null;
	protected Datastore datastore = null;

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
	public AppBasicDaoImpl(String host, int port, String dbName) {
		logger.info("[AppBasicDao] init with : {}:{}::{}",
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
			        "[AppBasicDao] Can not connect to {}:{}.", host, port);
			e.printStackTrace();
		} catch (MongoException e) {
			logger.error(
			        "[AppBasicDao] Caught exception when init mongoDB: {}. ",
			        e.getMessage());
			e.printStackTrace();
		} finally {
			if (mongo != null) {
				morphia = new Morphia();
				morphia.map(AppAccount.class);
				datastore = morphia.createDatastore(mongo, dbName);
				datastore.ensureIndexes();
				logger.debug("[AppBasicDao] init success.");
			} else {
				logger.info("[AppBasicDao] init failed.");
			}
		}
	}
}
