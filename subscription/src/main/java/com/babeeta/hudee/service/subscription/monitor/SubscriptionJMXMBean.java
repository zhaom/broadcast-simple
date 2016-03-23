package com.babeeta.hudee.service.subscription.monitor;

@SuppressWarnings("javadoc")
public interface SubscriptionJMXMBean {
	long getNewBindRequestCount();

	long getRebindRequestCount();

	long getUnbindRequestCount();

	long getQueryRequestCount();

	long getListRequestCount();

	long getDeleteRequestCount();

	long resetNewBindRequestCount();

	long resetRebindRequestCount();

	long resetUnbindRequestCount();

	long resetQueryRequestCount();

	long resetListRequestCount();

	long resetDeleteRequestCount();

	long getNewBindSucceedCount();

	long getRebindSucceedCount();

	long getUnbindSucceedCount();

	long getQuerySucceedCount();

	long getListSucceedCount();

	long getDeleteSucceedCount();

	long resetNewBindSucceedCount();

	long resetRebindSucceedCount();

	long resetUnbindSucceedCount();

	long resetQuerySucceedCount();

	long resetListSucceedCount();

	long resetDeleteSucceedCount();

	long getTagCountDeviceCount();

	long getTagCreateSetCount();

	long getTagCountSetCount();

	long getTagGetSetCount();

	long resetTagCountDeviceCount();

	long resetTagCreateSetCount();

	long resetTagCountSetCount();

	long resetTagGetSetCount();

	long getTagCountDeviceSucceedCount();

	long getTagCreateSetSucceedCount();

	long getTagCountSetSucceedCount();

	long getTagGetSetSucceedCount();

	long resetTagCountDeviceSucceedCount();

	long resetTagCreateSetSucceedCount();

	long resetTagCountSetSucceedCount();

	long resetTagGetSetSucceedCount();

	long getTagDeleteSetCount();

	long resetTagDeleteSetCount();

	long getTagDeleteSetSucceedCount();

	long resetTagDeleteSetSucceedCount();
}
