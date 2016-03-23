package com.babeeta.hudee.service.account.monitor;

@SuppressWarnings("javadoc")
public interface DeviceAccountJMXMBean {
	long getRegisterRequestCount();

	long getAuthRequestCount();

	long resetRegisterRequestCount();

	long resetAuthRequestCount();

	long getRegisterSucceedCount();

	long getAuthSucceedCount();

	long resetRegisterSucceedCount();

	long resetAuthSucceedCount();
}
