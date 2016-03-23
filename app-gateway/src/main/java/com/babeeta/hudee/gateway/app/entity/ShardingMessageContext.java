package com.babeeta.hudee.gateway.app.entity;

/**
 * Message context for sharding
 * 
 */
public class ShardingMessageContext extends BaseMessageContext {
	private String taskId;
	private String parentId;
	private int pageSize;
	private int pageIndex;
	private int totalShard;

	/**
	 * @return the taskId
	 */
	public String getTaskId() {
		return taskId;
	}

	/**
	 * @param taskId
	 *            the taskId to set
	 */
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	/**
	 * @return the parentId
	 */
	public String getParentId() {
		return parentId;
	}

	/**
	 * @param parentId
	 *            the parentId to set
	 */
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	/**
	 * @return the pageSize
	 */
	public int getPageSize() {
		return pageSize;
	}

	/**
	 * @param pageSize
	 *            the pageSize to set
	 */
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	/**
	 * @return the pageIndex
	 */
	public int getPageIndex() {
		return pageIndex;
	}

	/**
	 * @param pageIndex
	 *            the pageIndex to set
	 */
	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}

	/**
	 * @return the totalShard
	 */
	public int getTotalShard() {
		return totalShard;
	}

	/**
	 * @param totalShard
	 *            the totalShard to set
	 */
	public void setTotalShard(int totalShard) {
		this.totalShard = totalShard;
	}
}
