package com.babeeta.hudee.service.message.monitor;

public interface MessageServiceJMXMBean {
    // transaction
	long getListRequestCount();
	long getCreateRequestCount();
	long getUpdateRequestCount();
	long getQueryRequestCount();
	long getCountRequestCount();
	
	long resetListRequestCount();
	long resetCreateRequestCount();
	long resetUpdateRequestCount();
	long resetQueryRequestCount();
	long resetCountRequestCount();
}
