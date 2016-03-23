package com.babeeta.hudee.common.thread;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Tracking thread pool
 * 
 * Can count the costed of thread in pool.
 */
public class TrackingThreadPool extends ThreadPoolExecutor {
	private final Map<Runnable, Boolean> inProgress = new ConcurrentHashMap<Runnable, Boolean>();
	private final ThreadLocal<Long> startTime = new ThreadLocal<Long>();
	private long totalTime;
	private int totalTasks;

	/**
	 * Constructor
	 * 
	 * @param corePoolSize
	 *            core thread size
	 * @param maximumPoolSize
	 *            maximum thread size
	 * @param keepAliveTime
	 *            idle thread keep time
	 * @param unit
	 *            unit of keep time
	 * @param workQueue
	 *            blocked task queue
	 */
	public TrackingThreadPool(int corePoolSize,
	        int maximumPoolSize,
	        long keepAliveTime,
	        TimeUnit unit,
	        BlockingQueue<Runnable> workQueue) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
	}

	/**
	 * Constructor
	 * 
	 * @param corePoolSize
	 *            core thread size
	 * @param maximumPoolSize
	 *            maximum thread size
	 * @param keepAliveTime
	 *            idle thread keep time
	 * @param unit
	 *            unit of keep time
	 * @param workQueue
	 *            blocked task queue
	 * @param threadFactory
	 *            thread factory instance
	 */
	public TrackingThreadPool(int corePoolSize,
	        int maximumPoolSize,
	        long keepAliveTime,
	        TimeUnit unit,
	        BlockingQueue<Runnable> workQueue,
	        ThreadFactory threadFactory) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
		        threadFactory);
	}

	/**
	 * Constructor
	 * 
	 * @param corePoolSize
	 *            core thread size
	 * @param maximumPoolSize
	 *            maximum thread size
	 * @param keepAliveTime
	 *            idle thread keep time
	 * @param unit
	 *            unit of keep time
	 * @param workQueue
	 *            blocked task queue
	 * @param threadFactory
	 *            thread factory instance
	 * @param handler
	 *            the handle to handle the exception thrown when thread pool
	 *            reject to execute thread
	 */
	public TrackingThreadPool(int corePoolSize,
	        int maximumPoolSize,
	        long keepAliveTime,
	        TimeUnit unit,
	        BlockingQueue<Runnable> workQueue,
	        ThreadFactory threadFactory,
	        RejectedExecutionHandler handler) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
		        threadFactory, handler);
	}

	protected void beforeExecute(Thread t, Runnable r) {
		super.beforeExecute(t, r);
		inProgress.put(r, Boolean.TRUE);
		startTime.set(new Long(System.currentTimeMillis()));
	}

	protected void afterExecute(Runnable r, Throwable t) {
		long time = System.currentTimeMillis() - startTime.get().longValue();
		synchronized (this) {
			totalTime += time;
			++totalTasks;
		}
		inProgress.remove(r);
		super.afterExecute(r, t);
	}

	/**
	 * Get the in progress tasks
	 * 
	 * @return a set about in progress tasks
	 */
	public Set<Runnable> getInProgressTasks() {
		return Collections.unmodifiableSet(inProgress.keySet());
	}

	/**
	 * Get the count of executed tasks
	 * 
	 * @return the count of executed tasks
	 */
	public synchronized int getTotalTasks() {
		return totalTasks;
	}

	/**
	 * Get the average task costed time
	 * 
	 * @return the average task costed time
	 */
	public synchronized double getAverageTaskTime() {
		return (totalTasks == 0) ? 0 : totalTime / totalTasks;
	}
}