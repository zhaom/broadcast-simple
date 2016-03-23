package com.babeeta.hudee.service.subscription.monitor;

import com.babeeta.hudee.service.subscription.controller.SubscriptionResource;

/**
 *
 */
public class SubscriptionJMX implements SubscriptionJMXMBean {

	@Override
	public long getNewBindRequestCount() {
		return SubscriptionResource.getNewBindCount(false);
	}

	@Override
	public long getUnbindRequestCount() {
		return SubscriptionResource.getUnbindCount(false);
	}

	@Override
	public long getQueryRequestCount() {
		return SubscriptionResource.getQueryCount(false);
	}

	@Override
	public long resetNewBindRequestCount() {
		return SubscriptionResource.getNewBindCount(true);
	}

	@Override
	public long resetUnbindRequestCount() {
		return SubscriptionResource.getUnbindCount(true);
	}

	@Override
	public long resetQueryRequestCount() {
		return SubscriptionResource.getQueryCount(true);
	}

	@Override
	public long getNewBindSucceedCount() {
		return SubscriptionResource.getNewBindSucceedCount(false);
	}

	@Override
	public long getUnbindSucceedCount() {
		return SubscriptionResource.getUnbindSucceedCount(false);
	}

	@Override
	public long getQuerySucceedCount() {
		return SubscriptionResource.getQuerySucceedCount(false);
	}

	@Override
	public long resetNewBindSucceedCount() {
		return SubscriptionResource.getNewBindSucceedCount(true);
	}

	@Override
	public long resetUnbindSucceedCount() {
		return SubscriptionResource.getUnbindSucceedCount(true);
	}

	@Override
	public long resetQuerySucceedCount() {
		return SubscriptionResource.getQuerySucceedCount(true);
	}

	@Override
	public long getRebindRequestCount() {
		return SubscriptionResource.getRebindCount(false);
	}

	@Override
	public long getListRequestCount() {
		return SubscriptionResource.getListCount(false);
	}

	@Override
	public long getDeleteRequestCount() {
		return SubscriptionResource.getDeleteCount(false);
	}

	@Override
	public long resetRebindRequestCount() {
		return SubscriptionResource.getRebindCount(true);
	}

	@Override
	public long resetListRequestCount() {
		return SubscriptionResource.getListCount(true);
	}

	@Override
	public long resetDeleteRequestCount() {
		return SubscriptionResource.getDeleteCount(true);
	}

	@Override
	public long getRebindSucceedCount() {
		return SubscriptionResource.getRebindSucceedCount(false);
	}

	@Override
	public long getListSucceedCount() {
		return SubscriptionResource.getListSucceedCount(false);
	}

	@Override
	public long getDeleteSucceedCount() {
		return SubscriptionResource.getDeleteSucceedCount(false);
	}

	@Override
	public long resetRebindSucceedCount() {
		return SubscriptionResource.getRebindSucceedCount(true);
	}

	@Override
	public long resetListSucceedCount() {
		return SubscriptionResource.getListSucceedCount(true);
	}

	@Override
	public long resetDeleteSucceedCount() {
		return SubscriptionResource.getDeleteSucceedCount(true);
	}

	@Override
	public long getTagCountDeviceCount() {
		return SubscriptionResource.getTagCountDeviceCount(false);
	}

	@Override
	public long getTagCreateSetCount() {
		return SubscriptionResource.getTagCreateSetCount(false);
	}

	@Override
	public long getTagCountSetCount() {
		return SubscriptionResource.getTagCountSetCount(false);
	}

	@Override
	public long getTagGetSetCount() {
		return SubscriptionResource.getTagGetSetCount(false);
	}

	@Override
	public long resetTagCountDeviceCount() {
		return SubscriptionResource.getTagCountDeviceCount(true);
	}

	@Override
	public long resetTagCreateSetCount() {
		return SubscriptionResource.getTagCreateSetCount(true);
	}

	@Override
	public long resetTagCountSetCount() {
		return SubscriptionResource.getTagCountSetCount(true);
	}

	@Override
	public long resetTagGetSetCount() {
		return SubscriptionResource.getTagGetSetCount(true);
	}

	@Override
	public long getTagCountDeviceSucceedCount() {
		return SubscriptionResource.getTagCountDeviceSucceedCount(false);
	}

	@Override
	public long getTagCreateSetSucceedCount() {
		return SubscriptionResource.getTagCreateSetSucceedCount(false);
	}

	@Override
	public long getTagCountSetSucceedCount() {
		return SubscriptionResource.getTagCountSetSucceedCount(false);
	}

	@Override
	public long getTagGetSetSucceedCount() {
		return SubscriptionResource.getTagGetSetSucceedCount(false);
	}

	@Override
	public long resetTagCountDeviceSucceedCount() {
		return SubscriptionResource.getTagCountDeviceSucceedCount(true);
	}

	@Override
	public long resetTagCreateSetSucceedCount() {
		return SubscriptionResource.getTagCreateSetSucceedCount(true);
	}

	@Override
	public long resetTagCountSetSucceedCount() {
		return SubscriptionResource.getTagCountSetSucceedCount(true);
	}

	@Override
	public long resetTagGetSetSucceedCount() {
		return SubscriptionResource.getTagGetSetSucceedCount(true);
	}

	@Override
	public long getTagDeleteSetCount() {
		return SubscriptionResource.getTagDeleteSetCount(false);
	}

	@Override
	public long resetTagDeleteSetCount() {
		return SubscriptionResource.getTagDeleteSetCount(true);
	}

	@Override
	public long getTagDeleteSetSucceedCount() {
		return SubscriptionResource.getTagDeleteSetSucceedCount(false);
	}

	@Override
	public long resetTagDeleteSetSucceedCount() {
		return SubscriptionResource.getTagDeleteSetSucceedCount(true);
	}
}
