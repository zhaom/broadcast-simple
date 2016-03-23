package com.babeeta.hudee.gateway.device.policy;

/**
 * Interface for policy configuration of client in pull mode
 * 
 * @author Xinyu
 * 
 */
public interface PullPolicy {
	/**
	 * @param identifier
	 *            client identifier which can help to assign client to specially
	 *            policy
	 * @param clientKey
	 *            key of client
	 * @param timeInSec
	 *            current time in seconds
	 * @return next interval of connect to server and pull data
	 */
	long getNextInterval(String identifier, String clientKey, long timeInSec);

	/**
	 * @param identifier
	 *            client identifier which can help to assign client to specially
	 *            policy
	 * @param clientKey
	 *            key of client
	 * @param timeInSec
	 *            current time in seconds
	 * @return next interval of connect to server and pull data
	 */
	long getCurrentInterval(String identifier, String clientKey, long timeInSec);

	/**
	 * @param identifier
	 *            client identifier which can help to assign client to specially
	 *            policy
	 * @param timeInSec
	 *            current time in seconds
	 * @return return true if identifier's service in serving.
	 */
	boolean isInService(String identifier, long timeInSec);
}
