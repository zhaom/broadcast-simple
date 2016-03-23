package com.babeeta.hudee.gateway.device.tunnel;

import org.jboss.netty.channel.Channel;

import com.babeeta.hudee.gateway.device.tunnel.heartbeat.HeartbeatPolicy;

/**
 * tunnel context class
 */
public class TunnelContext {
	private HeartbeatPolicy currentHeartbeatPolicy = null;
	private Channel channel = null;
	private String deviceId = null;
	private int undeliverMessage = -1;
	private int waitAck = -1;
	private boolean push = true;
	private String serviceId;

	/**
	 * @return the currentHeartbeatPolicy
	 */
	public HeartbeatPolicy getCurrentHeartbeatPolicy() {
		return currentHeartbeatPolicy;
	}

	/**
	 * @param currentHeartbeatPolicy
	 *            the currentHeartbeatPolicy to set
	 */
	public void setCurrentHeartbeatPolicy(HeartbeatPolicy currentHeartbeatPolicy) {
		this.currentHeartbeatPolicy = currentHeartbeatPolicy;
	}

	/**
	 * @return the channel
	 */
	public Channel getChannel() {
		return channel;
	}

	/**
	 * @param channel
	 *            the channel to set
	 */
	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	/**
	 * @return the deviceId
	 */
	public String getDeviceId() {
		return deviceId;
	}

	/**
	 * @param deviceId
	 *            the deviceId to set
	 */
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	/**
	 * @return the undeliverMessage
	 */
	public int getUndeliverMessage() {
		return undeliverMessage;
	}

	/**
	 * @param undeliverMessage
	 *            the undeliverMessage to set
	 */
	public void setUndeliverMessage(int undeliverMessage) {
		this.undeliverMessage = undeliverMessage;
	}

	/**
	 * @return the waitAck
	 */
	public int getWaitAck() {
		return waitAck;
	}

	/**
	 * @param waitAck
	 *            the waitAck to set
	 */
	public void setWaitAck(int waitAck) {
		this.waitAck = waitAck;
	}

	/**
	 * @return the push flag
	 */
	public boolean inPushMode() {
		return push;
	}

	/**
	 * Set push mode
	 * 
	 * @param inPushMode
	 *            push flag
	 */
	public void setPushMode(boolean inPushMode) {
		push = inPushMode;
	}

	/**
     * @return the serviceId
     */
    public String getServiceId() {
	    return serviceId;
    }

	/**
     * @param serviceId the serviceId to set
     */
    public void setServiceId(String serviceId) {
	    this.serviceId = serviceId;
    }
}
