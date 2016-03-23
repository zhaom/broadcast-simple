package com.babeeta.hudee.gateway.device.tunnel.heartbeat;

/**
 * interface declaration of heartbeat configuration
 * 
 * @author Xinyu
 * 
 */
public interface ConfigurationProvider {

	/**
	 * @return heartbeat step
	 */
	int getStep();

	/**
	 * @return proxy policy trigger value
	 */
	int getProxyInterval();

	/**
	 * @return default policies trigger value
	 */
	int getDefaultInterval();

	/**
	 * @return max heartbeat interval
	 */
	int getMaxInterval();

	/**
	 * @return min heartbeat interval
	 */
	int getMinInterval();

	/**
	 * @return increase condition in normal policy
	 */
	int getHoldTimes();

	/**
	 * @return increase condition in proxy policy
	 */
	int getHoldInterval();
}