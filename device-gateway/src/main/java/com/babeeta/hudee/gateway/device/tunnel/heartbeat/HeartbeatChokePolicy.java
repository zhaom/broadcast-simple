package com.babeeta.hudee.gateway.device.tunnel.heartbeat;

import com.babeeta.hudee.core.MessageRouting.HeartbeatInit.HeartbeatException;

/**
 * @author Xinyu
 * 
 */
public class HeartbeatChokePolicy extends HeartbeatPolicy {
	/**
	 * Constructor
	 * 
	 * @param lastException
	 *            last exception caused disconnection
	 * @param lastInterval
	 *            last heart beat value
	 * @param configurationProvider
	 *            heart beat configuration
	 */
	public HeartbeatChokePolicy(HeartbeatException lastException,
	        int lastInterval, ConfigurationProvider configurationProvider) {
		super(lastException, lastInterval, configurationProvider);
		setNextInterval(getLastInterval()
		        - getConfigurationProvider().getStep());
	}

	@Override
	public HeartbeatPolicy getNextIntervalPolicy() {
		return new HeartbeatPreservePolicy(this);
	}
}
