package com.babeeta.butterfly.application.management.dao;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.babeeta.butterfly.application.management.entity.Schedule;

public interface ScheduleDao<T, K extends Serializable> extends IDao<T, K> {
	/**
	 * 根据状态查询schedule列表
	 * @param status
	 * @return
	 */
	public List<Schedule> findSchedules(List<String> status);

	public void saveSchedule(Schedule schedule);

	public List<Schedule> queryByDate(Date scheduleDate);

	public Schedule updateSchedule(Schedule schedule);
}
