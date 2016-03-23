package com.babeeta.hudee.service.message.monitor;

import java.util.Timer;
import java.util.TimerTask;

import com.babeeta.hudee.service.message.controller.MessageResource;

public class MessageServiceJMX implements MessageServiceJMXMBean {

	@Override
	public long getListRequestCount() {
		return MessageResource.getTotalListCount(false);
	}

	@Override
	public long getCreateRequestCount() {
		return MessageResource.getTotalCreateCount(false);
	}

	@Override
	public long getUpdateRequestCount() {
		return MessageResource.getTotalUpdateCount(false);
	}

	@Override
	public long getQueryRequestCount() {
		return MessageResource.getTotalQueryCount(false);
	}

	@Override
	public long getCountRequestCount() {
		return MessageResource.getTotalCountCount(false);
	}
	@Override
	public long resetListRequestCount() {
		return MessageResource.getTotalListCount(true);
	}

	@Override
	public long resetCreateRequestCount() {
		return MessageResource.getTotalCreateCount(true);
	}

	@Override
	public long resetUpdateRequestCount() {
		return MessageResource.getTotalUpdateCount(true);
	}

	@Override
	public long resetQueryRequestCount() {
		return MessageResource.getTotalQueryCount(true);
	}

	@Override
	public long resetCountRequestCount() {
		return MessageResource.getTotalCountCount(true);
	}
}
