package com.babeeta.hudee.gateway.device.monitor;

/**
 * Tunnel let monitor interface
 */
public interface DeviceGatewayMonitorJMXMBean {
	/**
	 * @return current tunnel connection count
	 */
	int getTunnelConnectionCount();

	/**
	 * @return heartbeat request times in last second
	 */
	long getHeartbeatCount();

	/**
	 * @return list message request times in last second
	 */
	long getListRequestCount();

	/**
	 * @return list message request successful times in last second
	 */
	long getListSuccessCount();

	/**
	 * @return list message service unavailable times in last second
	 */
	long getListServiceUnavailableCount();

	/**
	 * @return register request times in last second
	 */
	long getRegisterRequestCount();

	/**
	 * @return register request successful times in last second
	 */
	long getRegisterSuccessCount();

	/**
	 * @return register service unavailable times in last second
	 */
	long getRegisterServiceUnavailableCount();

	/**
	 * @return auth request times in last second
	 */
	long getAuthRequestCount();

	/**
	 * @return auth request successful times in last second
	 */
	long getAuthSuccessCount();

	/**
	 * @return auth service unavailable times in last second
	 */
	long getAuthServiceUnavailableCount();

	/**
	 * @return count request times in last second
	 */
	long getCountRequestCount();

	/**
	 * @return count request successful times in last second
	 */
	long getCountSuccessCount();

	/**
	 * @return Count service unavailable times in last second
	 */
	long getCountServiceUnavailableCount();

	/**
	 * @return update request times in last second
	 */
	long getUpdateRequestCount();

	/**
	 * @return update request successful times in last second
	 */
	long getUpdateSuccessCount();

	/**
	 * @return update service unavailable times in last second
	 */
	long getUpdateServiceUnavailableCount();

	/**
	 * @return bind request times in last second
	 */
	long getBindRequestCount();

	/**
	 * @return bind request successful times in last second
	 */
	long getBindSuccessCount();

	/**
	 * @return bind service unavailable times in last second
	 */
	long getBindServiceUnavailableCount();

	/**
	 * @return unbind request times in last second
	 */
	long getUnbindRequestCount();

	/**
	 * @return unbind request successful times in last second
	 */
	long getUnbindSuccessCount();

	/**
	 * @return unbind service unavailable times in last second
	 */
	long getUnbindServiceUnavailableCount();

	/**
	 * @return list message request times in last second
	 */
	long getAckListRequestCount();

	/**
	 * @return list message request successful times in last second
	 */
	long getAckListSuccessCount();

	/**
	 * @return list message service unavailable times in last second
	 */
	long getAckListServiceUnavailableCount();
}
