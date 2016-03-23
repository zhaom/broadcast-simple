package com.babeeta.hudee.gateway.device.tunnel.heartbeat;

import com.babeeta.hudee.core.MessageRouting.HeartbeatInit.HeartbeatException;

/**
 * Abstract class for heart beat policy
 * 
 */
public abstract class HeartbeatPolicy {
	private int initialInterval;
	private HeartbeatException lastException;
	private int nextInterval;
	private int lastInterval;

	private ConfigurationProvider configurationProvider = null;

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
	public HeartbeatPolicy(HeartbeatException lastException, int lastInterval,
	        ConfigurationProvider configurationProvider) {
		this.lastException = lastException;
		nextInterval = lastInterval;
		initialInterval = lastInterval;
		this.lastInterval = lastInterval;
		this.configurationProvider = configurationProvider;
	}

	HeartbeatPolicy(HeartbeatPolicy heartbeatPolicy) {
		initialInterval = heartbeatPolicy.initialInterval;
		lastException = heartbeatPolicy.lastException;
		nextInterval = heartbeatPolicy.lastInterval;
		lastInterval = heartbeatPolicy.lastInterval;
		configurationProvider = heartbeatPolicy.configurationProvider;
	}

	/**
	 * @return heart beat configuration
	 */
	public ConfigurationProvider getConfigurationProvider() {
		return configurationProvider;
	}

	/**
	 * @return last heart beat value
	 */
	public final int getLastInterval() {
		return lastInterval;
	}

	/**
	 * @return last exception
	 */
	public HeartbeatException getLastException() {
		return lastException;
	}

	/**
	 * @return new heart beat policy
	 */
	public abstract HeartbeatPolicy getNextIntervalPolicy();

	/**
	 * @return initial interval of policy trigger
	 */
	public int getInitialInterval() {
		return initialInterval;
	}

	/**
	 * @return next heart beat value
	 */
	public final int getNextInterval() {
		return nextInterval;
	}

	protected final void setNextInterval(int nextInterval) {
		this.nextInterval = nextInterval;
		lastInterval = nextInterval;
	}

	/**
	 * ignore next communication caused by new push message
	 */
	public void onMessage() {

	}
}
