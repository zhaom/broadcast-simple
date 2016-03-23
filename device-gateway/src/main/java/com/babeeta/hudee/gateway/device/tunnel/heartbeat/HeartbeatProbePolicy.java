package com.babeeta.hudee.gateway.device.tunnel.heartbeat;

import com.babeeta.hudee.core.MessageRouting.HeartbeatInit.HeartbeatException;

/**
 * @author Xinyu
 * 
 */
public class HeartbeatProbePolicy extends HeartbeatPolicy {
	private boolean ignore = false;

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
	public HeartbeatProbePolicy(HeartbeatException lastException,
	        int lastInterval, ConfigurationProvider configurationProvider) {
		super(lastException, lastInterval, configurationProvider);
		// TODO Auto-generated constructor stub
	}

	HeartbeatProbePolicy(HeartbeatPolicy heartbeatPolicy) {
		super(heartbeatPolicy);
	}

	@Override
	public HeartbeatPolicy getNextIntervalPolicy() {
		if (ignore) {
			ignore = false;
			return this;
		}
		if (getLastInterval() < getConfigurationProvider().getMaxInterval()) {
			// not reach max heartbeat interval,
			int nextInterval = getLastInterval()
			        + getConfigurationProvider().getStep();
			if (nextInterval > getConfigurationProvider().getMaxInterval()) {
				nextInterval = getConfigurationProvider().getMaxInterval();
			}
			setNextInterval(nextInterval);
			return this;
		} else {
			// keep current interval
			return new HeartbeatPreservePolicy(this);
		}
	}

	@Override
	public void onMessage() {
		ignore = true;
	}
}
