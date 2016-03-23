package com.babeeta.hudee.core.rpc;

/**
 *
 */
public interface MessageBasedRPCServiceMBean {

	/**
	 * 取累计的调用次数
	 * 
	 * @return the count of invoke
	 */
	public abstract long getCounter();

	/**
	 * 查询当前正在进行的调用数量
	 * 
	 * @return the count of pending invoke
	 */
	public abstract int getPendingInvokeCount();

}