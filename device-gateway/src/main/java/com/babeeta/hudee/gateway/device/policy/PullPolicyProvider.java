package com.babeeta.hudee.gateway.device.policy;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Xinyu
 * 
 */
public class PullPolicyProvider implements
        PullPolicy {
	private static final Logger logger = LoggerFactory
	        .getLogger(PullPolicyProvider.class);

	private Map<String, Policy> policy = new HashMap<String, Policy>();

	public PullPolicyProvider() {
		// TODO: only for test, remove later
		createPullPolicy("4fe83ea858b0e4b0387cb36eae54066b", 1, 0, 24);
        createPullPolicy("4ff268da48f9256854fbf0b5e565e91a", 1, 0, 24);
	}

	@Override
	public long getCurrentInterval(String identifier, String clientKey,
	        long timeInSec) {
		if (policy.containsKey(identifier)) {
			Policy pp = policy.get(identifier);
			if (pp.isInServiceDuration(timeInSec)) {
				return pp.getCurrentInterval(clientKey,
				        timeInSec);
			}
			return 3600L;
		}
		logger.warn("Invalid identifier : " + identifier);
		return 86400L;
	}

	@Override
	public long getNextInterval(String identifier, String clientKey,
	        long timeInSec) {
		if (policy.containsKey(identifier)) {
			Policy pp = policy.get(identifier);
			if (!pp.isInServiceDuration(timeInSec)) {
				return pp.getReservedInterval(clientKey, timeInSec);
			}
			return 3600L;
		}
		logger.warn("Invalid identifier : " + identifier);
		return 86400L;
	}

	@Override
	public boolean isInService(String identifier, long timeInSec) {
		if (policy.containsKey(identifier)) {
			Policy pp = policy.get(identifier);
			return pp.isInServiceDuration(timeInSec);
		}
		logger.warn("Invalid identifier : " + identifier);
		return false;
	}

	/**
	 * @param identifier
	 * @param interval
	 * @param startupAt
	 * @param shutdownAt
	 */
	public void createPullPolicy(String identifier, int interval,
	        int startupAt, int shutdownAt) {
		if (!policy.containsKey(identifier)) {
			policy.put(identifier, new Policy(interval, startupAt, shutdownAt));
		} else {
			policy.remove(identifier);
			policy.put(identifier, new Policy(interval, startupAt, shutdownAt));
		}
	}

	/**
	 * @param key
	 *            key
	 * @return hash of key
	 */
	private static Long computeKeyHash(String key) {
		byte[] bKey = computeMD5(key);
		return ((long) (bKey[3] & 0xFF) << 24)
		        | ((long) (bKey[2] & 0xFF) << 16)
		        | ((long) (bKey[1] & 0xFF) << 8)
		        | (bKey[0] & 0xFF);
	}

	/**
	 * @param value
	 *            input value
	 * @return result of MD5
	 */
	private static byte[] computeMD5(String value) {
		MessageDigest md5;
		try {
			md5 = MessageDigest.getInstance("md5");
			md5.reset();
			return md5.digest(value.getBytes("utf-8"));

		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("MD5 not supported.", e);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("UTF-8 not supported.", e);
		}
	}

	/**
	 * @param digest
	 *            MD5 result
	 * @param h
	 *            ?
	 * @return key of node
	 */
	private static Long computeNodeKey(byte[] digest, int h) {
		return ((long) (digest[3 + h * 4] & 0xFF) << 24)
		        | ((long) (digest[2 + h * 4] & 0xFF) << 16)
		        | ((long) (digest[1 + h * 4] & 0xFF) << 8)
		        | (digest[h * 4] & 0xFF);
	}

	class Policy {
		// date of policy become effective in econds
		private long startAt;
		// interval of policy effective in day. min = 1, max = 7. in days
		private int interval;
		// policy startup time. min = 0, max = 23. in hours
		private int startupAt;
		// policy shutdown time. min = 1, max = 24. in hours
		private int shutdownAt;
		private TreeMap<Long, Long> resourceMap = new TreeMap<Long, Long>();

		public Policy(int interval, int startupAt,
		        int shutdownAt) {
			startAt = System.currentTimeMillis() / 1000L;
			startAt = startAt - (startAt % 86400L);
			this.interval = interval;
			this.startupAt = startupAt;
			this.shutdownAt = shutdownAt;

			int duration = (shutdownAt - startupAt) * 3600;
			int totalZones = duration / (300);
			createKetamaHashRing(totalZones);
		}

		private boolean isInServiceDay(long timeInSec) {
			long dayInSec = (timeInSec - startAt) % (interval * 86400L);
			if (dayInSec < 86400L) {
				return true;
			}
			return false;
		}

		public boolean isInServiceDuration(long timeInSec) {
			if (isInServiceDay(timeInSec)) {
				long dayInSec = (timeInSec - startAt) % (interval * 86400L);
				long hour = dayInSec / 3600L;

				if (hour >= startupAt && hour < shutdownAt) {
					return true;
				}
			}
			return false;
		}

		private boolean isBeforeServiceDuration(long timeInSec) {
			if (isInServiceDay(timeInSec)) {
				long dayInSec = (timeInSec - startAt) % (interval * 86400L);
				long hour = dayInSec / 3600L;

				if (hour < startupAt) {
					return true;
				}
			}
			return false;
		}

		public long getCurrentInterval(String key, long timeInSec) {
			if (isInServiceDuration(timeInSec)) {
				long sdis = (timeInSec - startAt) % (interval * 86400L);
				long oi = timeInSec - sdis - (startAt * 3600L);
				long standard = 0;

				final TreeMap<Long, Long> resourceMap = this.resourceMap;
				Long k = computeKeyHash(key);
				if (resourceMap.containsKey(k)) {
					standard = resourceMap.get(k);
				} else {
					k = resourceMap.ceilingKey(k);
					if (k == null) {
						k = resourceMap.firstKey();
					}
					standard = resourceMap.get(k);
				}

				if (standard <= oi) {
					return timeInSec + 300000L;
				}
				return standard;
			}
			return getReservedInterval(key, timeInSec);
		}

		public long getReservedInterval(String key, long timeInSec) {
			if (!isInServiceDuration(timeInSec)) {
				if (isInServiceDay(timeInSec)
				        && isBeforeServiceDuration(timeInSec)) {
					long nowTime = (timeInSec - startAt) % 86400L;

					long interval = (startupAt * 3600L) - nowTime;

					final TreeMap<Long, Long> resourceMap = this.resourceMap;
					Long k = computeKeyHash(key);
					if (resourceMap.containsKey(k)) {
						return interval + resourceMap.get(k);
					} else {
						k = resourceMap.ceilingKey(k);
						if (k == null) {
							k = resourceMap.firstKey();
						}
						return interval + resourceMap.get(k);
					}
				} else {
					long today = (timeInSec - startAt) / 86400L;
					long toNextServiceDayInSec = (interval - (today % interval)) * 86400L;

					toNextServiceDayInSec += (startupAt * 3600L);

					final TreeMap<Long, Long> resourceMap = this.resourceMap;
					Long k = computeKeyHash(key);
					if (resourceMap.containsKey(k)) {
						return toNextServiceDayInSec + resourceMap.get(k);
					} else {
						k = resourceMap.ceilingKey(k);
						if (k == null) {
							k = resourceMap.firstKey();
						}
						return toNextServiceDayInSec + resourceMap.get(k);
					}
				}
			}
			return getCurrentInterval(key, timeInSec);
		}

		private void createKetamaHashRing(int totalZones) {
			for (int j = 0; j < totalZones; j++) {
				long key = j * 300;
				byte[] digest = computeMD5(Long.toString(key));
				for (int h = 0; h < 4; h++) {
					resourceMap.put(computeNodeKey(digest, h), key);
				}
			}
		}
	}
}
