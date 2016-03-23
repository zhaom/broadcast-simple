package com.babeeta.hudee.common.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For force shutdown executor
 * 
 */
public class ExecutorServiceUtils {
	private final static Logger logger = LoggerFactory
	        .getLogger(ExecutorServiceUtils.class);

	/**
	 * Force shutdown executor
	 * 
	 * @param executorService
	 *            executor will be shutdown
	 * @throws InterruptedException
	 *             thread interrupted exception
	 */
	public static void shutdown(ExecutorService executorService)
	        throws InterruptedException {
		executorService.shutdown();
		if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
			logger.warn(
			        "Force shutdown. {} tasks has been dropped.",
			        executorService.shutdownNow().size());
		}
	}

}
