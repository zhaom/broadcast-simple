package com.babeeta.butterfly.application.management.service;

import java.util.Date;
import java.util.List;

import com.babeeta.butterfly.application.management.entity.Schedule;

public interface ScheduleService {

	public List<Schedule> queryByDate(Date scheduleDate);

	public Schedule queryById(String scheduleId);

	/**
	 * 取排班信息
	 * @param status
	 * @return
	 */
	public List<Schedule> querySchedules(List<String> status);
	
	/**
	 * 添加schedule
	 * @param schedule
	 * @return
	 */
	public Schedule addSchedule(Schedule schedule);

	public void startScheduleForBroadcast();

	public Schedule updateSchedule(Schedule schedule);
}
