package com.babeeta.hudee.service.subscription.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;

import com.babeeta.hudee.common.util.OptimizedIdFactory;
import com.babeeta.hudee.service.subscription.SubscriptionStringDefs;
import com.babeeta.hudee.service.subscription.dao.SubscriptionDao;
import com.babeeta.hudee.service.subscription.entity.Subscription;
import com.babeeta.hudee.service.subscription.service.SubscriptionService;

/**
 *
 */
public class SubscriptionServiceImpl implements SubscriptionService {
	private SubscriptionDao subscriptionDao;

	/**
	 * Set DAO of account, used by Spring framework.
	 * 
	 * @param subscriptionDao
	 *            instance of subscription DAO
	 */
	public void setSubscriptionDao(SubscriptionDao subscriptionDao) {
		this.subscriptionDao = subscriptionDao;
	}

	private String makeTagKey(String owner, String tName) {
		return owner + DigestUtils.md5Hex(tName);
	}

	private void doClean(String aid, String did) {
		// clear all record about this aid and did.
		List<Subscription> r = subscriptionDao.listByDid(did);

		for (Subscription s : r) {
			if (s.getOwner().equals(s.getKey().substring(0, 32))) {
				// it is a subscription record
				if (s.getKey().substring(0, aid.length()).equals(aid)) {
					// it is about given aid
					// remove all tags by owner
					subscriptionDao.deleteByOwner(s.getId());
					// remove this record
					subscriptionDao.deleteById(s.getId());
				} else {
					// this is no business about this action.
					continue;
				}
			} else {
				// this is a tag record.
				continue;
			}
		}
	}

	@Override
	public String subscribe(String aid, String cid, String did) {
		// TODO: remove from next version begin
		// try to delete in old format data
		subscriptionDao.oldDeleteUseless(aid, did);
		// TODO: remove from next version end

		if (cid != null) {
			// try to find old record
			String key = aid + cid;
			Subscription sub = subscriptionDao.selectByKey(key);

			if (sub != null) {
				return sub.getKey().substring(aid.length());
			} else {
				// not found old record.
			}
		}

		doClean(aid, did);

		// create new
		Subscription sub = new Subscription();
		String newCid = OptimizedIdFactory.getOptimizedId();
		sub.setId(newCid);
		sub.setOwner(aid);
		sub.setDid(did);
		sub.setKey(aid + newCid);
		sub.settName(SubscriptionStringDefs.DEFAULT_TAG_NAME);
		sub.setCreatedAt(new Date());

		subscriptionDao.insert(sub);

		return newCid;
	}

	@Override
	public boolean unsubscribe(String aid, String cid, String did) {

		// TODO: remove from next version begin
		// try to delete in old format data
		subscriptionDao.oldDeleteUseless(aid, did);
		// TODO: remove from next version end

		// try to delete in new format data
		String key = aid + cid;
		Subscription sub = subscriptionDao.selectByKey(key);

		if (sub != null) {
			// remove all tags by owner
			subscriptionDao.deleteByOwner(sub.getId());
			// remove this record
			subscriptionDao.deleteById(sub.getId());
			return true;
		} else {
			// not found record.
			return false;
		}
	}

	@Override
	public String queryDevice(String aid, String cid) {
		// TODO: remove from next version begin
		// try to find in old format data
		String did = subscriptionDao.oldSelectByKey(aid, cid);
		if (did != null) {
			subscriptionDao.oldDeleteUseless(aid, did);
			Subscription sub = new Subscription();
			sub.setId(cid);
			sub.setOwner(aid);
			sub.setDid(did);
			sub.setKey(aid + cid);
			sub.settName(SubscriptionStringDefs.DEFAULT_TAG_NAME);
			sub.setCreatedAt(new Date());
			subscriptionDao.insert(sub);

			return did;
		} else
		// TODO: remove from next version end
		{
			String key = aid + cid;
			Subscription sub = subscriptionDao.selectByKey(key);
			if (sub != null) {
				return sub.getDid();
			} else {
				return null;
			}
		}
	}

	@Override
	public List<String> listSubscription(String did) {
		List<String> result = new ArrayList<String>();
		List<Subscription> allList = subscriptionDao.listByDid(did);

		for (Subscription sub : allList) {
			if (sub.getOwner().equals(sub.getKey().substring(0, 32))) {
				String aid = sub.getKey().substring(0, 32);
				String cid = sub.getKey().substring(32);
				String record = aid + ":" + cid;
				if (!result.contains(record)) {
					result.add(record);
				}
			}
		}

		return result;
	}

	@Override
	public long deleteByDevice(String did) {
		long ret = subscriptionDao.countByDid(did, new Date().getTime());
		subscriptionDao.deleteByDid(did);
		return ret;
	}

	@Override
	public List<String> addTags(String aid, String cid, List<String> tNameList) {
		List<String> result = new ArrayList<String>();
		Subscription owner = subscriptionDao.selectByKey(aid + cid);
		if (owner != null) {
			for (String tName : tNameList) {
				String key = makeTagKey(aid, tName);
				Subscription tag = subscriptionDao.selectByKey(key);
				if (tag == null) {
					tag = new Subscription();
					tag.setId(OptimizedIdFactory.getOptimizedId());
					tag.setOwner(owner.getId());
					tag.setDid(owner.getDid());
					tag.settName(tName);
					tag.setCreatedAt(new Date());
					tag.setKey(key);

					subscriptionDao.insert(tag);

					if (!result.contains(tName)) {
						result.add(tName);
					}
				}
			}
		}

		return result;
	}

	@Override
	public List<String> removeTags(String aid, String cid,
	        List<String> tNameList) {
		List<String> result = new ArrayList<String>();
		Subscription owner = subscriptionDao.selectByKey(aid + cid);
		if (owner != null) {
			for (String tName : tNameList) {
				String key = makeTagKey(aid, tName);
				Subscription tag = subscriptionDao.selectByKey(key);
				if (tag != null) {
					subscriptionDao.deleteByKey(key);
					if (!result.contains(tName)) {
						result.add(tName);
					}
				}
			}
		}
		return result;
	}

	@Override
	public List<String> listTags(String aid, String cid) {
		List<String> result = new ArrayList<String>();
		Subscription owner = subscriptionDao.selectByKey(aid + cid);
		if (owner != null) {
			List<Subscription> tagList = subscriptionDao.listByOwner(owner
			        .getId());

			for (Subscription item : tagList) {
				if (item.getOwner().equals(item.getKey().substring(0, 32))) {
					continue;
				}

				if (!result.contains(item.gettName())) {
					result.add(item.gettName());
				}
			}
		}
		return result;
	}

	@Override
	public long countDeviceByTags(String aid, String tName) {
		String key = makeTagKey(aid, tName);

		return subscriptionDao.countByKey(key, new Date().getTime());
	}

	@Override
	public long countDeviceByOwner(String aid) {
		return subscriptionDao.countByOwner(aid, new Date().getTime());
	}

	@Override
	public List<String> listDeviceByTags(String aid, String tName) {
		List<String> result = new ArrayList<String>();
		String key = makeTagKey(aid, tName);
		List<Subscription> tagList = subscriptionDao.listByKey(key);

		for (Subscription item : tagList) {
			if (!result.contains(item.getDid())) {
				result.add(item.getDid());
			}
		}
		return result;
	}
}
