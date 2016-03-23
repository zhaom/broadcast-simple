package com.babeeta.hudee.service.message.service.impl;

import com.babeeta.hudee.common.util.OptimizedIdFactory;
import com.babeeta.hudee.service.message.MessageStringDefs;
import com.babeeta.hudee.service.message.dao.MessageDao;
import com.babeeta.hudee.service.message.service.MessageService;

/**
 *
 */
public class MessageServiceImpl implements MessageService {
	private MessageDao messageDao;

	/**
	 * Set DAO of message, used by Spring framework.
	 * 
	 * @param messageDao
	 *            instance of message DAO
	 */
	public void setMessageDao(MessageDao messageDao) {
		this.messageDao = messageDao;
	}

	@Override
	public byte[] getMessagesList(String did, String status, int pageSize,
	        int pageIndex, String aid) {
		byte[] result = messageDao.query(did, status, pageSize,
		        pageIndex, aid);

		return result;
	}

	@Override
	public String getMessageStatus(String uid) {
		return messageDao.getStatus(uid);
	}

	@Override
	public boolean updateAck(String uid) {
		messageDao.updateStatus(uid, MessageStringDefs.STATUS_ACKED);
		return true;
	}

	@Override
	public String saveMessage(String sender, String recipient,
	        String device,
	        int life, byte[] content) {
		String strUUID = OptimizedIdFactory.getOptimizedId();

		messageDao.save(strUUID, sender, recipient, device, life, content);

		return strUUID;
	}

	@Override
	public long countMessage(String did, String status, String aid) {
		return messageDao.count(did, status, aid);
	}

	@Override
	public String saveParentMessage(String sender, String recipient, int life,
	        byte[] content) {
		String strUUID = OptimizedIdFactory.getOptimizedId();

		messageDao.save(strUUID, sender, recipient, life, content);

		return strUUID;
	}

	@Override
	public String saveReferenceMessage(String parent, String sender,
	        String recipient, int life, String device) {
		String strUUID = OptimizedIdFactory.getOptimizedId();

		messageDao.save(strUUID, sender, recipient, device, life, parent);

		return strUUID;
	}

	@Override
	public String saveEventMessage(String sender, int life, byte[] content) {
		String strUUID = OptimizedIdFactory.getOptimizedId();

		messageDao.save(strUUID, sender, life, content);

		return strUUID;
	}

	@Override
	public String saveConfirmMessage(String parent, String device) {
		String strUUID = OptimizedIdFactory.getOptimizedId();

		messageDao.save(strUUID, device, parent);

		return strUUID;
	}

	@Override
	public byte[] getMessages(String uid) {
		return messageDao.selectMessage(uid);
	}
}
