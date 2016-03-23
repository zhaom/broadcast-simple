package com.babeeta.hudee.gateway.device.tunnel.heartbeat;

import com.babeeta.hudee.core.MessageRouting.HeartbeatInit.HeartbeatException;

/**
 * @author Xinyu
 * 
 */
public class HeartbeatPreservePolicy extends HeartbeatPolicy {
	private int successCounter = 0;

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
	public HeartbeatPreservePolicy(HeartbeatException lastException,
	        int lastInterval, ConfigurationProvider configurationProvider) {
		super(lastException, lastInterval, configurationProvider);
		// TODO Auto-generated constructor stub
	}

	HeartbeatPreservePolicy(HeartbeatPolicy heartbeatPolicy) {
		super(heartbeatPolicy);
	}

	@Override
	public HeartbeatPolicy getNextIntervalPolicy() {
		successCounter++;
		if (successCounter > getConfigurationProvider().getHoldTimes()
		        && getLastInterval() < (getConfigurationProvider()
		                .getMaxInterval() - getConfigurationProvider()
		                .getStep())) {
			// try to probe
			return new HeartbeatProbePolicy(this);
		} else if (getLastInterval() > getConfigurationProvider()
		        .getMaxInterval()) {
			// replace with max limit interval
			setNextInterval(getConfigurationProvider().getMaxInterval());
			return this;
		} else {
			// keep current interval
			return this;
		}
	}
}
