package com.babeeta.hudee.service.subscription.service;

import java.util.List;

/**
 *
 */
public interface TagQueryService {
	/**
	 * Create a query task
	 * 
	 * @param stamp
	 *            time stamp
	 * @param life
	 *            task life
	 * @param owner
	 *            task owner
	 * @return key of task
	 */
	String createTask(long stamp, long life, String owner);

	/**
	 * Query task stage
	 * 
	 * @param key
	 *            key of task
	 * @return return task stage if task exists, otherwise return null.
	 */
	String queryTaskStage(String key);

	/**
	 * Get the result
	 * 
	 * @param key
	 *            key of task
	 * @param fromIndex
	 *            from index
	 * @param toIndex
	 *            to index
	 * @return a sub list of result
	 */
	List<String> readResult(String key, int fromIndex, int toIndex);

	/**
	 * Count the list of result
	 * 
	 * @param key
	 *            key of task
	 * @return the count value
	 */
	int countResult(String key);

	/**
	 * Delete a task by key
	 * 
	 * @param key
	 *            key of task
	 */
	void deleteTask(String key);
}
