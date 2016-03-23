package com.babeeta.hudee.gateway.device.monitor;

import com.babeeta.hudee.gateway.device.tunnel.TunnelService;
import com.babeeta.hudee.gateway.device.tunnel.let.AcknowledgementTunnelLet;
import com.babeeta.hudee.gateway.device.tunnel.let.AuthTunnelLet;
import com.babeeta.hudee.gateway.device.tunnel.let.DeviceRegisterTunnelLet;
import com.babeeta.hudee.gateway.device.tunnel.let.HeartbeatTunnelLet;
import com.babeeta.hudee.gateway.device.tunnel.let.ServiceBindTunnelLet;
import com.babeeta.hudee.gateway.device.tunnel.let.ServiceUnbindTunnelLet;

/**
 * implement tunnel let monitor interface
 * 
 */
public class DeviceGatewayMonitorJMX implements DeviceGatewayMonitorJMXMBean {

	@Override
	public int getTunnelConnectionCount() {
		return TunnelService.getTunnelCount();
	}

	@Override
	public long getHeartbeatCount() {
		return HeartbeatTunnelLet.getHeartbeatCounter(false);
	}

	@Override
	public long getListRequestCount() {
		return AuthTunnelLet.getListRequestCounter(false);
	}

	@Override
	public long getListSuccessCount() {
		return AuthTunnelLet.getListSuccessCounter(false);
	}

	@Override
	public long getListServiceUnavailableCount() {
		return AuthTunnelLet.getListServiceUnavailableCounter(false);
	}

	@Override
	public long getRegisterRequestCount() {
		return DeviceRegisterTunnelLet.getDeviceRegisterMessageCounter(false);
	}

	@Override
	public long getRegisterSuccessCount() {
		return DeviceRegisterTunnelLet.getRegisterSuccessCounter(false);
	}

	@Override
	public long getRegisterServiceUnavailableCount() {
		return DeviceRegisterTunnelLet.getServiceUnavailableCounter(false);
	}

	@Override
	public long getAuthRequestCount() {
		return AuthTunnelLet.getDeviceAuthMessageCounter(false);
	}

	@Override
	public long getAuthSuccessCount() {
		return AuthTunnelLet.getAuthSuccessCounter(false);
	}

	@Override
	public long getAuthServiceUnavailableCount() {
		return AuthTunnelLet.getAuthServiceUnavailableCounter(false);
	}

	@Override
	public long getCountRequestCount() {
		return AuthTunnelLet.getCountRequestCounter(false);
	}

	@Override
	public long getCountSuccessCount() {
		return AuthTunnelLet.getCountSuccessCounter(false);
	}

	@Override
	public long getCountServiceUnavailableCount() {
		return AuthTunnelLet.getCountServiceUnavailableCounter(false);
	}

	@Override
	public long getUpdateRequestCount() {
		return AcknowledgementTunnelLet.getUpdateRequestCounter(false);
	}

	@Override
	public long getUpdateSuccessCount() {
		return AcknowledgementTunnelLet.getUpdateSuccessCounter(false);
	}

	@Override
	public long getUpdateServiceUnavailableCount() {
		return AcknowledgementTunnelLet
		        .getUpdateServiceUnavailableCounter(false);
	}

	@Override
	public long getBindRequestCount() {
		return ServiceBindTunnelLet.getBindRequestCounter(false);
	}

	@Override
	public long getBindSuccessCount() {
		return ServiceBindTunnelLet.getBindSuccessCounter(false);
	}

	@Override
	public long getBindServiceUnavailableCount() {
		return ServiceBindTunnelLet.getServiceUnavailableCounter(false);
	}

	@Override
	public long getUnbindRequestCount() {
		return ServiceUnbindTunnelLet.getUnbindRequestCounter(false);
	}

	@Override
	public long getUnbindSuccessCount() {
		return ServiceUnbindTunnelLet.getUnbindSuccessCounter(false);
	}

	@Override
	public long getUnbindServiceUnavailableCount() {
		return ServiceUnbindTunnelLet.getServiceUnavailableCounter(false);
	}

	@Override
	public long getAckListRequestCount() {
		return AcknowledgementTunnelLet.getListRequestCounter(false);
	}

	@Override
	public long getAckListSuccessCount() {
		return AcknowledgementTunnelLet.getListSuccessCounter(false);
	}

	@Override
	public long getAckListServiceUnavailableCount() {
		return AcknowledgementTunnelLet.getListSuccessCounter(false);
	}
}
