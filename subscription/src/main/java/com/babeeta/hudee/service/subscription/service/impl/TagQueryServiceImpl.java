package com.babeeta.hudee.service.subscription.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.hudee.common.mbean.MBeanManager;
import com.babeeta.hudee.common.thread.NamedThreadFactory;
import com.babeeta.hudee.common.thread.ThreadPoolStatus;
import com.babeeta.hudee.common.thread.TrackingThreadPool;
import com.babeeta.hudee.service.subscription.dao.SubscriptionDao;
import com.babeeta.hudee.service.subscription.service.TagQueryService;

/**
 *
 */
public class TagQueryServiceImpl implements TagQueryService {
	private final static Logger logger = LoggerFactory
	        .getLogger(TagQueryServiceImpl.class);
	private final int sliceCount;
	private final int threadCount;
	private final long cleanInterval;
	private Timer cleanTimer = new Timer(true);
	private TimerTask cleanTask = new TimerTask() {
		@Override
		public void run() {
			synchronized (taskPool) {
				Iterator<QueryTask> it = taskPool.values().iterator();
				while (it.hasNext()) {
					QueryTask task = it.next();
					if (task.isExpired()) {
						taskPool.remove(task.getKey());
					}
				}
			}
		}
	};
	private TrackingThreadPool querySetExecutor;

	private final ConcurrentHashMap<String, QueryTask> taskPool = new ConcurrentHashMap<String, QueryTask>();

	private SubscriptionDao subscriptionDao;

	/**
	 * Set DAO of account, used by Spring framework.
	 * 
	 * @param subscriptionDao
	 *            instance of subscription DAO
	 */
	public void setSubscriptionDao(SubscriptionDao subscriptionDao) {
		this.subscriptionDao = subscriptionDao;
	}

	/**
	 * Constructor
	 * 
	 * @param queryThreadCount
	 *            size of query thread pool
	 * @param autoCleanInterval
	 *            interval of auto clean
	 * @param sliceCount
	 *            size of slice in task
	 * @param threadCount
	 *            max thread limit of task
	 */
	public TagQueryServiceImpl(int queryThreadCount, long autoCleanInterval,
	        int sliceCount, int threadCount) {
		this.cleanInterval = autoCleanInterval;
		this.sliceCount = sliceCount;
		this.threadCount = threadCount;
		cleanTimer.scheduleAtFixedRate(cleanTask, cleanInterval, cleanInterval);
		querySetExecutor =
		        new TrackingThreadPool(0, queryThreadCount, 60,
		                TimeUnit.SECONDS,
		                new LinkedBlockingQueue<Runnable>(),
		                new NamedThreadFactory("QuerySetExecutor-", true));

		MBeanManager.registerMBean(
		        "Subscription." + TagQueryServiceImpl.class.getSimpleName()
		                + ":name=TagQueryService",
		        new ThreadPoolStatus("TagQueryService-", querySetExecutor));
	}

	@Override
	public String createTask(long stamp, long life, String owner) {
		QueryTask task = new QueryTask(owner, life, stamp, sliceCount,
		        threadCount);

		taskPool.put(task.getKey(), task);
		logger.debug("[{}] start at [{}].", task.getKey(),
		        new Date().toString());
		querySetExecutor.execute(task);

		return task.getKey();
	}

	@Override
	public String queryTaskStage(String key) {
		if (taskPool.containsKey(key)) {
			QueryTask task = taskPool.get(key);
			return task.getStage();
		}
		return null;
	}

	@Override
	public List<String> readResult(String key, int fromIndex, int toIndex) {
		if (taskPool.containsKey(key)) {
			QueryTask task = taskPool.get(key);
			return task.readResult(fromIndex, toIndex);
		}
		return null;
	}

	@Override
	public int countResult(String key) {
		if (taskPool.containsKey(key)) {
			QueryTask task = taskPool.get(key);
			return task.getResultCount();
		}
		return -1;
	}

	@Override
	public void deleteTask(String key) {
		if (taskPool.containsKey(key)) {
			QueryTask task = taskPool.get(key);
			if (task.getStage().equals(QueryTaskStage.Ready)
			        || task.getStage().equals(QueryTaskStage.Expired)) {
				// remove from task pool
				taskPool.remove(key);
			} else {
				// running or in queue, drop it by itself.
				task.dropTask();
			}
		}
	}

	private enum QueryTaskStage {
		Droped("droped"),
		Pending("pending"),
		Building("building"),
		Ready("ready"),
		Expired("expired");

		private final String tag;

		private QueryTaskStage(String tag) {
			this.tag = tag;
		}

		@Override
		public String toString() {
			return tag;
		}
	}

	private class QueryTask implements Runnable {
		private final long stamp;
		private final String owner;
		private final String key;
		private final long life;

		private int totalSubTask;
		private long total;
		private long startAt;
		private long readyAt;
		private QueryTaskStage stage;
		private List<String> result = null;
		private volatile boolean drop = false;

		private TrackingThreadPool executor;
		private CompletionService<List<String>> completionService;
		private final int querySlice;

		public QueryTask(String owner, long life, long stamp, int slice,
		        int threadCount) {
			this.owner = owner;
			this.life = life;
			this.stamp = stamp;
			this.querySlice = slice;
			key = java.util.UUID.randomUUID().toString().replaceAll("-", "");
			stage = QueryTaskStage.Pending;

			executor = new TrackingThreadPool(threadCount,
			        threadCount, 60,
			        TimeUnit.SECONDS,
			        new LinkedBlockingQueue<Runnable>(),
			        new NamedThreadFactory("QueryTaskExecutor-", true));
			completionService = new ExecutorCompletionService<List<String>>(
			        executor);
		}

		public String getKey() {
			return key;
		}

		public String getStage() {
			return stage.toString();
		}

		public void dropTask() {
			drop = true;
		}

		public int getResultCount() {
			if ((stage.equals(QueryTaskStage.Building) || stage
			        .equals(QueryTaskStage.Ready)) && result != null) {
				return result.size();
			}
			return -1;
		}

		public List<String> readResult(int fromIndex, int toIndex) {
			if ((stage.equals(QueryTaskStage.Building) || stage
			        .equals(QueryTaskStage.Ready)) && result != null) {
				return result.subList(fromIndex, toIndex);
			}
			return null;
		}

		public boolean isExpired() {
			if (stage.equals(QueryTaskStage.Ready)) {
				return (readyAt + life < System.currentTimeMillis());
			} else if (stage.equals(QueryTaskStage.Expired)) {
				return true;
			} else if (stage.equals(QueryTaskStage.Droped)) {
				return true;
			}
			return false;
		}

		private List<String> runQuery(String owner, String tName,
		        long stamp, int offset, int amount) {
			return subscriptionDao.listDeviceByOwner(
			        owner, stamp, amount, offset);
		}

		@Override
		public void run() {
			startAt = System.currentTimeMillis();
			stage = QueryTaskStage.Building;
			// count the result
			total = subscriptionDao.countByOwner(owner, stamp);

			// break up task
			totalSubTask = (int) (total / querySlice);
			if (total % querySlice > 0) {
				totalSubTask++;
			}

			for (int i = 0; i < totalSubTask; i++) {
				if (!executor.isShutdown()) {
					completionService.submit(new SubTask(owner, "_ALL", stamp,
					        i * querySlice, querySlice));
				}
			}

			result = getResult();

			if (drop) {
				stage = QueryTaskStage.Droped;
			} else {
				stage = QueryTaskStage.Ready;
				readyAt = System.currentTimeMillis();
			}
		}

		private List<String> getResult() {
			List<String> result = new ArrayList<String>();
			for (int i = 0; i < totalSubTask; i++) {
				try {
					List<String> subList = completionService.take().get();
					result.addAll(subList);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
			logger.debug("[{}] Collection done. {}", key,
			        System.currentTimeMillis() - startAt);
			return result;
		}

		class SubTask implements Callable<List<String>> {
			private final String owner;
			private final String tName;
			private final long stamp;
			private final int offset;
			private final int amount;

			public SubTask(String owner, String tName, long stamp, int offset,
			        int amount) {
				this.owner = owner;
				this.tName = tName;
				this.stamp = stamp;
				this.offset = offset;
				this.amount = amount;
			}

			@Override
			public List<String> call() throws Exception {
				long startAt = System.currentTimeMillis();
				List<String> subList = runQuery(owner, tName, stamp,
				        offset, amount);
				logger.debug("{} slice done. costed {}", offset
				        / querySlice, System.currentTimeMillis() - startAt);
				return subList;
			}
		}
	}
}
