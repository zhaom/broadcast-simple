package com.babeeta.butterfly.application.management.dao.impl;

import java.util.Date;
import java.util.List;

import com.babeeta.butterfly.application.management.dao.MorphiaDataStore;
import com.babeeta.butterfly.application.management.dao.ScheduleDao;
import com.babeeta.butterfly.application.management.entity.GroupMessage;
import com.babeeta.butterfly.application.management.entity.Schedule;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;

public class ScheduleDaoImpl extends BaseDao<Schedule, String> implements ScheduleDao<Schedule, String> {
	
	public ScheduleDaoImpl(MorphiaDataStore ds) {
		super(Schedule.class, ds);
	}

	@Override
	public List<Schedule> findSchedules(List<String> status) {
		
		Query<Schedule> query=this.ds.createQuery(Schedule.class).order("scheduleDate");
		if(status!=null && !status.isEmpty())
		{
			query.field("status").in(status);
		}
		List<Schedule> list=query.asList();
		if(list!=null&&list.size()>0)
		{
			return list;
		}
		return null;
	}

	@Override
	public void saveSchedule(Schedule schedule) {
		this.ds.save(schedule);
	}

	@Override
	public List<Schedule> queryByDate(Date scheduleDate) {
		Query<Schedule> query=this.ds.createQuery(Schedule.class).order("scheduleDate");
		if(scheduleDate != null){
			query.filter("scheduleDate", scheduleDate);
		}
		query.filter("status !=", "rejected");
		List<Schedule> list=query.asList();
		if(list != null && list.size()>0){
			return list;
		}
		return null;
	}

	@Override
	public Schedule updateSchedule(Schedule schedule) {
		Query<Schedule> query = this.createQuery().filter("_id",
				schedule.getId());
		UpdateOperations<Schedule> ops = this.createUpdateOperations();
		if (schedule.getScheduleDate() != null) {
			ops.set("scheduleDate", schedule.getScheduleDate());
		}
		if (schedule.getBeginHour() != 0) {
			ops.set("beginHour", schedule.getBeginHour());
		}
		if (schedule.getEndHour() != 0) {
			ops.set("endHour", schedule.getEndHour());
		}
		if (schedule.getStatus() != null && !schedule.getStatus().isEmpty()) {
			ops.set("status", schedule.getStatus());
		}
		this.ds.update(query, ops);
		return schedule;
	}
}
