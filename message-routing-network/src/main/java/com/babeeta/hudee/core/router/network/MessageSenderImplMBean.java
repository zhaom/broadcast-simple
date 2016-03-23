package com.babeeta.hudee.core.router.network;

@SuppressWarnings("javadoc")
public interface MessageSenderImplMBean {

	long getSentMessageCount();

	long resetSentMessageCount();

	long getSentSuccessCount();

	long resetSentSuccessCount();

	long getSentFailedCount();

	long resetSentFailedCount();

	long getSentErrorDNSCount();

	long resetSentErrorDNSCount();

	long getSentErrorTimeoutCount();

	long resetSentErrorTimeoutCount();

	long getSentErrorIOCount();

	long resetSentErrorIOCount();
}
