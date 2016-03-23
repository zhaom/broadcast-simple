package com.babeeta.hudee.service.account.monitor;

import com.babeeta.hudee.service.account.controller.AppAccountResource;

/**
 *
 */
public class AppAccountJMX implements AppAccountJMXMBean {

	@Override
	public long getRegisterRequestCount() {
		return AppAccountResource.getRegisterCount(false);
	}

	@Override
	public long getAuthRequestCount() {
		return AppAccountResource.getAuthCount(false);
	}

	@Override
	public long resetRegisterRequestCount() {
		return AppAccountResource.getRegisterCount(true);
	}

	@Override
	public long resetAuthRequestCount() {
		return AppAccountResource.getAuthCount(true);
	}

	@Override
	public long getRegisterSucceedCount() {
		return AppAccountResource.getRegisterSucceedCount(false);
	}

	@Override
	public long getAuthSucceedCount() {
		return AppAccountResource.getAuthSucceedCount(false);
	}

	@Override
	public long resetRegisterSucceedCount() {
		return AppAccountResource.getRegisterSucceedCount(true);
	}

	@Override
	public long resetAuthSucceedCount() {
		return AppAccountResource.getAuthSucceedCount(true);
	}
}
