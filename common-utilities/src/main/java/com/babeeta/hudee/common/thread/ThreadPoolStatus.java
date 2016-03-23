package com.babeeta.hudee.common.thread;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 
 */
public class ThreadPoolStatus implements ThreadPoolStatusMBean {
	private final TrackingThreadPool pool;
	private final String poolName;

	/**
	 * Constructor
	 * 
	 * @param poolName
	 *            name of tracking thread pool
	 * 
	 * @param pool
	 *            an instance of tracking thread pool
	 */
	public ThreadPoolStatus(String poolName, TrackingThreadPool pool) {
		this.pool = pool;
		this.poolName = poolName;
	}

	@Override
	public String getPoolName() {
		return poolName;
	}

	@Override
	public int getActiveThreads() {
		return pool.getPoolSize();
	}

	@Override
	public int getActiveTasks() {
		return pool.getActiveCount();
	}

	@Override
	public int getTotalTasks() {
		return pool.getTotalTasks();
	}

	@Override
	public int getQueuedTasks() {
		return pool.getQueue().size();
	}

	@Override
	public double getAverageTaskTime() {
		return pool.getAverageTaskTime();
	}

	@Override
	public String[] getActiveTaskNames() {
		return toStringArray(pool.getInProgressTasks());
	}

	@Override
	public String[] getQueuedTaskNames() {
		return toStringArray(pool.getQueue());
	}

	private String[] toStringArray(Collection<Runnable> collection) {
		ArrayList<String> list = new ArrayList<String>();
		for (Runnable r : collection) {
			list.add(r.toString());
		}
		return list.toArray(new String[0]);
	}
}