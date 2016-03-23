package com.babeeta.hudee.common.thread;

/**
 * MBean interface for status monitor of thread pool
 * 
 */
public interface ThreadPoolStatusMBean {
	/**
	 * @return the current size of thread pool
	 */
	public int getActiveThreads();

	/**
	 * @return the count of active tasks
	 */
	public int getActiveTasks();

	/**
	 * @return the count of total tasks
	 */
	public int getTotalTasks();

	/**
	 * @return the count of tasks in blocking queue
	 */
	public int getQueuedTasks();

	/**
	 * @return the average task costed time
	 */
	public double getAverageTaskTime();

	/**
	 * @return a string array about name of active tasks
	 */
	public String[] getActiveTaskNames();

	/**
	 * @return a string array about name of blocking tasks
	 */
	public String[] getQueuedTaskNames();

	/**
	 * @return the name of pool
	 */
	public String getPoolName();
}