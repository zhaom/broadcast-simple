package com.babeeta.hudee.gateway.device.tunnel.heartbeat;

import com.babeeta.hudee.core.MessageRouting.HeartbeatInit.HeartbeatException;

/**
 * @author Xinyu
 * 
 */
public class HeartbeatProxyPolicy extends HeartbeatPolicy {
	private int successCounter = 0;
	private long keepDuration = 0;

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
	public HeartbeatProxyPolicy(HeartbeatException lastException,
	        int lastInterval, ConfigurationProvider configurationProvider) {
		super(lastException, lastInterval, configurationProvider);

		int newTimeout = lastInterval - configurationProvider.getStep();

		if (newTimeout < configurationProvider.getMinInterval()) {
			newTimeout = configurationProvider.getMinInterval();
		}
		setNextInterval(newTimeout);
	}

	@Override
	public HeartbeatPolicy getNextIntervalPolicy() {
		if (successCounter > 0) {
			keepDuration += getLastInterval();
		}
		successCounter++;

		if (keepDuration >= getConfigurationProvider().getHoldInterval()) {
			// reached hold interval, check last interval whether reached normal
			// heartbeat interval.
			if (getLastInterval() < getConfigurationProvider()
			        .getProxyInterval()) {
				// not reach normal heartbeat interval,
				int nextInterval = getLastInterval()
				        + getConfigurationProvider().getStep();
				if (nextInterval > getConfigurationProvider()
				        .getProxyInterval()) {
					nextInterval = getConfigurationProvider()
					        .getProxyInterval();
				}
				setNextInterval(nextInterval);
				successCounter = 0;
			} else {
				// keep current interval
				return new HeartbeatProbePolicy(this);
			}
		} else {
			// keep current interval
			setNextInterval(getLastInterval());
		}
		return this;
	}
}
