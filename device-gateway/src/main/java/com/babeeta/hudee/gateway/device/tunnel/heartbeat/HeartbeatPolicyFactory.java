package com.babeeta.hudee.gateway.device.tunnel.heartbeat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.hudee.core.MessageRouting.HeartbeatInit.HeartbeatException;

/**
 * factory of heart beat policy
 * 
 */
public class HeartbeatPolicyFactory {

	private ConfigurationProvider configurationProvider = null;

	private static final Logger log = LoggerFactory
	        .getLogger(HeartbeatPolicyFactory.class);

	/**
	 * @return heart beat configuration
	 */
	public ConfigurationProvider getConfigurationProvider() {
		return configurationProvider;
	}

	/**
	 * Get heart beat policy instance by last exception and delay
	 * 
	 * @param lastException
	 *            last exception caused disconnection
	 * @param timeout
	 *            last heart beat value
	 * @return heart beat policy
	 */
	public HeartbeatPolicy getInstance(String lastException, int timeout) {
		HeartbeatException e = convertToEnum(lastException);
		if (e != null) {
			return createNewPolicy(e, timeout);
		} else {
			return null;
		}
	}

	/**
	 * @param configurationProvider
	 *            heart beat configuration
	 */
	public void setConfigurationProvider(
	        ConfigurationProvider configurationProvider) {
		this.configurationProvider = configurationProvider;
	}

	private HeartbeatException convertToEnum(String paramE) {
		try {
			return HeartbeatException.valueOf(paramE);
		} catch (Exception ex) {
			log.error(ex.getMessage());
			return null;
		}
	}

	private HeartbeatPolicy createNewPolicy(HeartbeatException e, int timeout) {
		if (HeartbeatException.choke.equals(e)) {
			if (timeout <= configurationProvider.getProxyInterval()) {
				// Apply the proxy policy when the timeout given by client less
				// the sum of min initial interval and max step interval.

				int newTimeout = timeout - configurationProvider.getStep();

				if (newTimeout < configurationProvider.getMinInterval()) {
					newTimeout = configurationProvider.getMinInterval();
				}
				return new HeartbeatProxyPolicy(e,
				        newTimeout,
				        configurationProvider);
			} else {
				// Apply decrease policy to handle normal choke.
				return new HeartbeatChokePolicy(e, timeout,
				        configurationProvider);
			}
		} else if (HeartbeatException.none.equals(e)) {
			return new HeartbeatProbePolicy(e, timeout,
			        configurationProvider);
		} else if (HeartbeatException.exception.equals(e)) {
			return new HeartbeatProbePolicy(e,
			        configurationProvider.getMinInterval(),
			        configurationProvider);
		} else {
			return new HeartbeatPreservePolicy(e, timeout,
			        configurationProvider);
		}
	}
}