package com.babeeta.hudee.service.message.dao.impl;

import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	protected DBCollection collection = null;
	protected Mongo mongo = null;

	/**
	 * Constructor
	 * 
	 * @param host
	 *            address of mongoDB
	 * @param port
	 *            port of mongoDB
	 * @param dbName
	 *            name of mongoDB
	 * @param collectionName
	 *            name of collection
	 */
	public BasicDaoImpl(String host, int port, String dbName,
	        String collectionName) {
		logger.info("[BasicDao] init with : {}:{}::{}:{}",
		        new Object[] { host, port, dbName, collectionName });

		MongoOptions mongoOptions = new MongoOptions();
		mongoOptions.threadsAllowedToBlockForConnectionMultiplier = 120;
		mongoOptions.connectionsPerHost = 50;
		mongoOptions.autoConnectRetry = true;
		mongoOptions.socketKeepAlive = true;

		try {
			mongo = new Mongo(new ServerAddress(host, port),
			        mongoOptions);
			collection = mongo.getDB(dbName).getCollection(collectionName);
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
			if (mongo != null && collection != null) {
				logger.debug("[BasicDao] init success.");
			} else {
				logger.info("[BasicDao] init failed.");
			}
		}
	}
}
