package com.babeeta.hudee.common.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Xinyu
 * 
 */
public class NamedThreadFactory implements ThreadFactory {
	private final String prefix;
	private final boolean isDaemon;
	private final AtomicInteger threadCount = new AtomicInteger(1);
	private final int priority;

	/**
	 * Constructor
	 * 
	 * @param prefix
	 *            thread prefix name
	 * @param isDaemon
	 *            is a daemon thread
	 */
	public NamedThreadFactory(String prefix, boolean isDaemon) {
		this.prefix = prefix + "-thread-";
		this.isDaemon = isDaemon;
		priority = -1;
	}

	/**
	 * Constructor
	 * 
	 * @param prefix
	 *            thread prefix name
	 * @param isDaemon
	 *            is a daemon thread
	 * @param priority
	 *            priority
	 */
	public NamedThreadFactory(String prefix, boolean isDaemon, int priority) {
		this.prefix = prefix + "-thread-";
		this.isDaemon = isDaemon;
		if (priority >= Thread.MIN_PRIORITY && priority <= Thread.MAX_PRIORITY) {
			this.priority = priority;
		} else {
			this.priority = -1;
		}
	}

	@Override
	public Thread newThread(Runnable r) {
		String name = prefix + threadCount.incrementAndGet();
		Thread ret = new Thread(null, r, name, 0);
		ret.setDaemon(isDaemon);
		if (priority != -1) {
			ret.setPriority(priority);
		}
		return ret;
	}
}