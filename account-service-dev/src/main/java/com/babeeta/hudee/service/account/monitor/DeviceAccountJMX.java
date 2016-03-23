package com.babeeta.hudee.service.account.monitor;

import com.babeeta.hudee.service.account.controller.DeviceAccountResource;

/**
 *
 */
public class DeviceAccountJMX implements DeviceAccountJMXMBean {

	@Override
	public long getRegisterRequestCount() {
		return DeviceAccountResource.getRegisterCount(false);
	}

	@Override
	public long getAuthRequestCount() {
		return DeviceAccountResource.getAuthCount(false);
	}

	@Override
	public long resetRegisterRequestCount() {
		return DeviceAccountResource.getRegisterCount(true);
	}

	@Override
	public long resetAuthRequestCount() {
		return DeviceAccountResource.getAuthCount(true);
	}

	@Override
	public long getRegisterSucceedCount() {
		return DeviceAccountResource.getRegisterSucceedCount(false);
	}

	@Override
	public long getAuthSucceedCount() {
		return DeviceAccountResource.getAuthSucceedCount(false);
	}

	@Override
	public long resetRegisterSucceedCount() {
		return DeviceAccountResource.getRegisterSucceedCount(true);
	}

	@Override
	public long resetAuthSucceedCount() {
		return DeviceAccountResource.getAuthSucceedCount(true);
	}
}
