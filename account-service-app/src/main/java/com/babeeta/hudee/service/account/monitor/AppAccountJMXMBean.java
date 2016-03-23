package com.babeeta.hudee.service.account.monitor;

/**
 * monitor interface definition
 */
@SuppressWarnings("javadoc")
public interface AppAccountJMXMBean {

	long getRegisterRequestCount();

	long getAuthRequestCount();

	long resetRegisterRequestCount();

	long resetAuthRequestCount();

	long getRegisterSucceedCount();

	long getAuthSucceedCount();

	long resetRegisterSucceedCount();

	long resetAuthSucceedCount();
}
