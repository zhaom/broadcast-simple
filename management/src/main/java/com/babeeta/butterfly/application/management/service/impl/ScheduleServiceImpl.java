package com.babeeta.butterfly.application.management.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.butterfly.application.management.dao.AppDao;
import com.babeeta.butterfly.application.management.dao.MessageDao;
import com.babeeta.butterfly.application.management.dao.ScheduleDao;
import com.babeeta.butterfly.application.management.entity.App;
import com.babeeta.butterfly.application.management.entity.GroupMessage;
import com.babeeta.butterfly.application.management.entity.Schedule;
import com.babeeta.butterfly.application.management.service.ScheduleService;
import com.babeeta.butterfly.application.management.util.PushClient;

public class ScheduleServiceImpl implements ScheduleService {

	private final static Logger logger = LoggerFactory
			.getLogger(ScheduleServiceImpl.class);
	private static ScheduledExecutorService scheduledExecutorService = Executors
			.newScheduledThreadPool(1);

	private ScheduleDao<Schedule, String> scheduleDaoImpl;

	private MessageDao<GroupMessage, String> messageDaoImpl;

	private AppDao<App, String> appDaoImpl;

	@Override
	public List<Schedule> queryByDate(Date scheduleDate) {
		return this.scheduleDaoImpl.queryByDate(scheduleDate);
	}

	@Override
	public Schedule queryById(String scheduleId) {
		return this.scheduleDaoImpl.findById(scheduleId);
	}

	@Override
	public Schedule addSchedule(Schedule schedule) {
		this.scheduleDaoImpl.saveSchedule(schedule);
		return schedule;
	}

	/**
	 * 查询schedule列表
	 */
	public List<Schedule> querySchedules(List<String> status) {

		return this.scheduleDaoImpl.findSchedules(status);
	}

	@Override
	public Schedule updateSchedule(Schedule schedule) {
		this.scheduleDaoImpl.updateSchedule(schedule);
		return schedule;
	}

	@Override
	public void startScheduleForBroadcast() {
		scheduledExecutorService.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				logger.info("schedule begin at :" + (new Date()));
				try {
					List<String> lstatus = new ArrayList<String>();
					lstatus.add("checked");
					List<Schedule> ls = scheduleDaoImpl.findSchedules(lstatus);
					if (ls != null) {
						Iterator<Schedule> it = ls.iterator();
						while (it.hasNext()) {
							Schedule entry = it.next();
							GroupMessage message = messageDaoImpl
									.findById(entry.getMessageId());
							if (message != null && message.getCheck() == 1) {
								Date schDate = message.getScheduleDate();

								Long schHour = schDate.getTime() / 1000 / 3600;
								Long nowHour = System.currentTimeMillis() / 1000 / 3600;
								if ((schHour - nowHour) < 3) {
									logger.info(
											"schedule push to app,message id [{}]",
											entry.getMessageId());
									App app = appDaoImpl.findById(entry
											.getAppId());
									String appId = app.getAppId();
									String appKey = app.getAppKey();
									String messageContent = message
											.getMessageContent();
									int scheduledLife = (int) getMessageLife(
											entry.getScheduleDate(),
											entry.getBeginHour(),
											entry.getEndHour());

									PushClient push = new PushClient(appId,
											appKey);

									push.push(scheduledLife, messageContent);

									try {
										String messageId = EntityUtils
												.toString(push
														.getResponseEntity());
										int code = push.getResponseStatus();
										entry.setStatus("scheduled");
										scheduleDaoImpl.updateSchedule(entry);
										if (messageId != null
												&& !messageId.isEmpty()
												&& code == 200) {
											logger.info(
													"push sccess,messageId:[{}],schedule id: [{}]",
													messageId, entry.getId());
										}
									} catch (ParseException e) {
										e.printStackTrace();
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
							} else if (message != null
									&& message.getCheck() == 2) {
								entry.setStatus("rejected");
								scheduleDaoImpl.updateSchedule(entry);
							}
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				logger.info("schedule end at :" + (new Date()));
			}

			private long getMessageLife(Date scheduleDate, int beginHour,
					int endHour) {
				scheduleDate.setHours(endHour);
				long ss = scheduleDate.getTime();
				Date now = new Date();
				long ns = now.getTime();
				return (ss - ns) / 1000;
			}
		}, 2L, 5L, TimeUnit.MINUTES);
	}

	public ScheduleDao<Schedule, String> getScheduleDaoImpl() {
		return scheduleDaoImpl;
	}

	public void setScheduleDaoImpl(ScheduleDao<Schedule, String> scheduleDaoImpl) {
		this.scheduleDaoImpl = scheduleDaoImpl;
	}

	public MessageDao<GroupMessage, String> getMessageDaoImpl() {
		return messageDaoImpl;
	}

	public void setMessageDaoImpl(
			MessageDao<GroupMessage, String> messageDaoImpl) {
		this.messageDaoImpl = messageDaoImpl;
	}

	public AppDao<App, String> getAppDaoImpl() {
		return appDaoImpl;
	}

	public void setAppDaoImpl(AppDao<App, String> appDaoImpl) {
		this.appDaoImpl = appDaoImpl;
	}

}
