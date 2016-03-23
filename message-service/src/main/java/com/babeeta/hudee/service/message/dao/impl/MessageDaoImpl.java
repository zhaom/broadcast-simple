package com.babeeta.hudee.service.message.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bson.BSONObject;
import org.bson.BasicBSONObject;

import com.babeeta.hudee.service.message.MessageStringDefs;
import com.babeeta.hudee.service.message.dao.MessageDao;
import com.mongodb.BasicDBObject;
import com.mongodb.Bytes;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 *
 */
public class MessageDaoImpl extends BasicDaoImpl implements MessageDao {

	/**
	 * Constructor
	 * 
	 * @param host
	 *            host of mongoDB
	 * @param port
	 *            port of mongoDB
	 * @param dbName
	 *            database name
	 * @param collectionName
	 *            collection name
	 */
	public MessageDaoImpl(String host, int port, String dbName,
	        String collectionName) {
		super(host, port, dbName, collectionName);
		collection.ensureIndex(MessageStringDefs.DB_FIELD_DEVICE);
	}

	private boolean expired(Date createAt, int life) {
		int messageLifeInMillisecond = life * 1000;
		Date expireAt = new Date(createAt.getTime()
		        + messageLifeInMillisecond);

		return new Date().after(expireAt);
	}

	private String composeStatusReport(String status, Date modifiedAt) {
		StringBuilder reportBuilder = new StringBuilder("{\""
		        + MessageStringDefs.DB_FIELD_STATUS + "\":\"")
		        .append(status)
		        .append("\", \"" + MessageStringDefs.DB_FIELD_MODIFIEDAT
		                + "\":\"")
		        .append(modifiedAt.toString()).append("\"}");

		return reportBuilder.toString();
	}

	private String composeStatusReport(String status, Date modifiedAt,
	        long confirm) {
		StringBuilder reportBuilder = new StringBuilder("{\"")
		        .append(MessageStringDefs.DB_FIELD_STATUS)
		        .append("\":\"")
		        .append(status)
		        .append("\", \"")
		        .append(MessageStringDefs.DB_FIELD_MODIFIEDAT)
		        .append("\":\"")
		        .append(modifiedAt.toString())
		        .append("\", \"")
		        .append(MessageStringDefs.DB_FIELD_CONFIRM)
		        .append("\":\"")
		        .append(confirm).append("\"}");

		return reportBuilder.toString();
	}

	private String composeStatusReport(String status, Date modifiedAt,
	        long acked, long total) {
		StringBuilder reportBuilder = new StringBuilder("{\"")
		        .append(MessageStringDefs.DB_FIELD_STATUS)
		        .append("\":\"")
		        .append(status)
		        .append("\", \"")
		        .append(MessageStringDefs.DB_FIELD_MODIFIEDAT)
		        .append("\":\"")
		        .append(modifiedAt.toString())
		        .append("\", \"")
		        .append(MessageStringDefs.DB_FIELD_TOTAL)
		        .append("\":\"")
		        .append(total)
		        .append("\", \"")
		        .append(MessageStringDefs.DB_FIELD_ACKED)
		        .append("\":\"")
		        .append(acked)
		        .append("\"}");

		return reportBuilder.toString();
	}

	// return a JSON like :
	// {"status" : STATUS, "modifiedAt" : DATE}
	// {"status" : "EXPIRED", "modifiedAt" : DATE, "confirm" : confirm}
	// {"status" : "EXPIRED", "modifiedAt" : DATE, "acked" : acked, "total" :
	// total}
	@Override
	public String getStatus(String uid) {
		DBObject result = select(uid);

		if (result == null) {
			return null;
		} else {
			String status = result.get(MessageStringDefs.DB_FIELD_STATUS)
			        .toString();
			Date modifiedAt = null;

			if (result.containsField(MessageStringDefs.DB_FIELD_MODIFIEDAT)) {
				modifiedAt = (Date) result
				        .get(MessageStringDefs.DB_FIELD_MODIFIEDAT);
			} else {
				modifiedAt = (Date) result
				        .get(MessageStringDefs.DB_FIELD_CREATEAT);
			}
			if (result.get(MessageStringDefs.DB_FIELD_STATUS).equals(
			        MessageStringDefs.STATUS_EXPIRED)) {
				if (result.get(MessageStringDefs.DB_FIELD_CLASS).equals(
				        MessageStringDefs.CLASS_EVENT)) {
					long confirm = Long.parseLong(result.get(
					        MessageStringDefs.DB_FIELD_CONFIRM).toString());
					return composeStatusReport(status, modifiedAt, confirm);
				} else if (result.get(MessageStringDefs.DB_FIELD_CLASS).equals(
				        MessageStringDefs.CLASS_PARENT)) {
					long acked = Long.parseLong(result.get(
					        MessageStringDefs.DB_FIELD_ACKED).toString());
					long total = Long.parseLong(result.get(
					        MessageStringDefs.DB_FIELD_TOTAL).toString());
					return composeStatusReport(status, modifiedAt, acked, total);
				} else {
					// ignore
				}
			} else {
				// ignore
			}
			return composeStatusReport(status, modifiedAt);
		}
	}

	@Override
	public byte[] query(String did, String status, int pageSize, int pageIndex,
	        String aid) {
		List<BSONObject> list = new ArrayList<BSONObject>();
		List<DBObject> refList = new ArrayList<DBObject>();

		DBObject query = new BasicDBObject();
		query.put(MessageStringDefs.DB_FIELD_DEVICE, did);
		query.put(MessageStringDefs.DB_FIELD_STATUS, status);
		if (aid != null) {
			query.put(MessageStringDefs.DB_FIELD_SENDER, aid);
		}
		DBCursor messageCursor = null;

		if (pageSize > 0 && pageIndex >= 0) {
			messageCursor = collection.find(query)
			        .skip(pageSize * pageIndex).limit(pageSize);
		} else {
			messageCursor = collection.find(query);
		}

		while (messageCursor.hasNext()) {
			DBObject result = messageCursor.next();
			String uid = result.get(MessageStringDefs.DB_FIELD_ID).toString();
			if (expired((Date) result.get(MessageStringDefs.DB_FIELD_CREATEAT),
			        Integer.parseInt(result
			                .get(MessageStringDefs.DB_FIELD_LIFE)
			                .toString()))) {
				updateStatus(uid, MessageStringDefs.STATUS_EXPIRED);
			} else {
				if (result.containsField(MessageStringDefs.DB_FIELD_CLASS)) {
					if (result.get(MessageStringDefs.DB_FIELD_CLASS).equals(
					        MessageStringDefs.CLASS_REFERENCE)) {
						// need transform to normal message
						refList.add(result);
					} else if (result.get(MessageStringDefs.DB_FIELD_CLASS)
					        .equals(MessageStringDefs.CLASS_CONFIRM)) {
						// ignore
					} else if (result.get(MessageStringDefs.DB_FIELD_CLASS)
					        .equals(MessageStringDefs.CLASS_PARENT)) {
						// illegal for device
					} else if (result.get(MessageStringDefs.DB_FIELD_CLASS)
					        .equals(MessageStringDefs.CLASS_EVENT)) {
						// illegal for device
					} else {
						list.add(result);
					}
				} else {
					list.add(result);
				}
			}
		}
		// TODO: put parent message in a cache to avoid access database
		// transform to normal message
		for (DBObject ref : refList) {
			DBObject normal = transformReference(ref);

			if (!list.contains(normal)) {
				list.add(normal);
			}
		}

		if (list.size() > 0) {
			BSONObject result = new BasicBSONObject();
			for (BSONObject r : list) {
				result.put(r.get(MessageStringDefs.DB_FIELD_ID).toString(), r);
			}

			return Bytes.encode(result);
		}

		return null;
	}

	@Override
	public void updateStatus(String uid, String newStatus) {
		DBObject query = new BasicDBObject();
		query.put(MessageStringDefs.DB_FIELD_ID, uid);

		DBObject content = new BasicDBObject();

		content.put(MessageStringDefs.DB_FIELD_STATUS, newStatus);
		content.put(MessageStringDefs.DB_FIELD_MODIFIEDAT, new Date());

		DBObject update = new BasicDBObject();
		update.put("$set", content);

		collection.update(query, update);
	}

	@Override
	public long count(String did, String status, String aid) {
		DBObject query = new BasicDBObject();
		query.put(MessageStringDefs.DB_FIELD_DEVICE, did);
		query.put(MessageStringDefs.DB_FIELD_STATUS, status);
		if (aid != null) {
			query.put(MessageStringDefs.DB_FIELD_SENDER, aid);
		}
		return collection.count(query);
	}

	private boolean save(String uid, String classStr, String sender,
	        String recipient, String device, String life,
	        byte[] content, String parent, String status) {
		DBObject messageObj = new BasicDBObject();
		messageObj.put(MessageStringDefs.DB_FIELD_ID, uid);
		messageObj.put(MessageStringDefs.DB_FIELD_CLASS, classStr);
		messageObj.put(MessageStringDefs.DB_FIELD_CREATEAT, new Date());
		messageObj.put(MessageStringDefs.DB_FIELD_STATUS, status);

		if (sender != null) {
			messageObj.put(MessageStringDefs.DB_FIELD_SENDER, sender);
		}
		if (recipient != null) {
			messageObj.put(MessageStringDefs.DB_FIELD_RECIPIENT, recipient);
		}
		if (device != null) {
			messageObj.put(MessageStringDefs.DB_FIELD_DEVICE, device);
		}
		if (content != null) {
			messageObj.put(MessageStringDefs.DB_FIELD_MESSAGE, content);
		}
		if (life != null) {
			messageObj.put(MessageStringDefs.DB_FIELD_LIFE, life);
		}
		if (parent != null) {
			messageObj.put(MessageStringDefs.DB_FIELD_PARENT, parent);
		}
		collection.insert(messageObj);
		return true;
	}

	@Override
	public boolean save(String uid, String sender, String recipient,
	        String device, int life, byte[] content) {
		return save(uid, MessageStringDefs.CLASS_NORMAL, sender, recipient,
		        device, Integer.toString(life), content, null,
		        MessageStringDefs.STATUS_DELIVERING);
	}

	@Override
	public boolean save(String uid, String sender, String recipient, int life,
	        byte[] content) {
		return save(uid, MessageStringDefs.CLASS_PARENT, sender, recipient,
		        null, Integer.toString(life), content, null,
		        MessageStringDefs.STATUS_DELIVERING);
	}

	@Override
	public boolean save(String uid, String sender, int life, byte[] content) {
		return save(uid, MessageStringDefs.CLASS_EVENT, sender, null,
		        null, Integer.toString(life), content, null,
		        MessageStringDefs.STATUS_DELIVERING);
	}

	@Override
	public boolean save(String uid, String sender, String recipient,
	        String device, int life, String parent) {
		return save(uid, MessageStringDefs.CLASS_REFERENCE, sender, recipient,
		        device, Integer.toString(life), null, parent,
		        MessageStringDefs.STATUS_DELIVERING);
	}

	@Override
	public boolean save(String uid, String device, String parent) {
		return save(uid, MessageStringDefs.CLASS_CONFIRM, null, null, null,
		        null, null, parent, MessageStringDefs.STATUS_ACKED);
	}

	@Override
	public long countConfirm(String parentId) {
		DBObject query = new BasicDBObject();
		query.put(MessageStringDefs.DB_FIELD_PARENT, parentId);
		query.put(MessageStringDefs.DB_FIELD_CLASS,
		        MessageStringDefs.CLASS_CONFIRM);
		return collection.count(query);
	}

	@Override
	public byte[] selectMessage(String uid) {
		DBObject result = select(uid);
		if (result != null) {
			return Bytes.encode(result);
		} else {
			return null;
		}
	}

	@Override
	public void deleteConfirms(String parent) {
		DBObject query = new BasicDBObject();
		query.put(MessageStringDefs.DB_FIELD_PARENT, parent);
		query.put(MessageStringDefs.DB_FIELD_CLASS,
		        MessageStringDefs.CLASS_CONFIRM);

		collection.remove(query);
	}

	@Override
	public void deleteReference(String parent) {
		DBObject query = new BasicDBObject();
		query.put(MessageStringDefs.DB_FIELD_PARENT, parent);
		query.put(MessageStringDefs.DB_FIELD_CLASS,
		        MessageStringDefs.CLASS_REFERENCE);

		collection.remove(query);
	}

	private DBObject select(String uid) {
		DBObject messageObj = new BasicDBObject();
		messageObj.put(MessageStringDefs.DB_FIELD_ID, uid);
		DBObject result = collection.findOne(messageObj);

		if (expired((Date) result.get(MessageStringDefs.DB_FIELD_CREATEAT),
		        Integer.parseInt(result
		                .get(MessageStringDefs.DB_FIELD_LIFE)
		                .toString()))) {
			Map<String, Object> updateFields = new HashMap<String, Object>();

			updateFields.put(MessageStringDefs.DB_FIELD_STATUS,
			        MessageStringDefs.STATUS_EXPIRED);
			updateFields.put(MessageStringDefs.DB_FIELD_MODIFIEDAT, new Date());

			// update summary for parent/event message when it expired
			if (result.containsField(MessageStringDefs.DB_FIELD_CLASS)) {
				if (result.get(MessageStringDefs.DB_FIELD_CLASS).equals(
				        MessageStringDefs.CLASS_PARENT)) {
					long ack = countInternal(uid,
					        MessageStringDefs.CLASS_REFERENCE,
					        MessageStringDefs.STATUS_ACKED);
					long total = countInternal(uid,
					        MessageStringDefs.CLASS_REFERENCE,
					        null);
					updateFields.put(MessageStringDefs.DB_FIELD_ACKED, ack);
					updateFields.put(MessageStringDefs.DB_FIELD_TOTAL, total);

					deleteReference(uid);
				} else if (result.get(MessageStringDefs.DB_FIELD_CLASS).equals(
				        MessageStringDefs.CLASS_EVENT)) {
					long confirm = countInternal(uid,
					        MessageStringDefs.CLASS_CONFIRM,
					        null);
					updateFields.put(MessageStringDefs.DB_FIELD_CONFIRM,
					        confirm);
					deleteConfirms(uid);
				} else {
					// ignore
				}
			}
			update(uid, updateFields);
			return collection.findOne(messageObj);
		}

		return result;
	}

	private long countInternal(String parent, String classStr, String status) {
		DBObject query = new BasicDBObject();
		query.put(MessageStringDefs.DB_FIELD_PARENT, parent);
		query.put(MessageStringDefs.DB_FIELD_CLASS, classStr);
		if (status != null) {
			query.put(MessageStringDefs.DB_FIELD_STATUS, status);
		}
		return collection.count(query);
	}

	private void update(String uid, Map<String, Object> fields) {
		DBObject query = new BasicDBObject();
		query.put(MessageStringDefs.DB_FIELD_ID, uid);

		DBObject content = new BasicDBObject();
		Iterator<String> it = fields.keySet().iterator();

		while (it.hasNext()) {
			String key = it.next();
			Object value = fields.get(key);
			content.put(key, value);
		}

		DBObject update = new BasicDBObject();
		update.put("$set", content);

		collection.update(query, update);
	}

	private DBObject transformReference(DBObject ref) {
		String parent = ref.get(MessageStringDefs.DB_FIELD_PARENT).toString();

		DBObject queryObj = new BasicDBObject();
		queryObj.put(MessageStringDefs.DB_FIELD_ID, parent);
		DBObject parentMsg = collection.findOne(queryObj);

		ref.removeField(MessageStringDefs.DB_FIELD_CLASS);
		ref.removeField(MessageStringDefs.DB_FIELD_PARENT);

		ref.put(MessageStringDefs.DB_FIELD_CLASS,
		        MessageStringDefs.CLASS_NORMAL);
		ref.put(MessageStringDefs.DB_FIELD_MESSAGE,
		        parentMsg.get(MessageStringDefs.DB_FIELD_MESSAGE));
		return ref;
	}

}
