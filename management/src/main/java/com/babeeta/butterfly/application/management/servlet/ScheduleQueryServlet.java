package com.babeeta.butterfly.application.management.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.babeeta.butterfly.application.management.entity.Schedule;
import com.babeeta.butterfly.application.management.service.ScheduleService;

public class ScheduleQueryServlet extends HttpServlet {

	private static final Logger logger = LoggerFactory
			.getLogger(ScheduleQueryServlet.class);

	private ScheduleService scheduleServiceImpl;

	@Override
	public void init(ServletConfig config) {
		scheduleServiceImpl = (ScheduleService) WebApplicationContextUtils
				.getWebApplicationContext(config.getServletContext()).getBean(
						"scheduleServiceImpl");
		scheduleServiceImpl.startScheduleForBroadcast();
		logger.info("init ScheduleQueryServlet finished.");
	}
	
	/**
	 * write:{"schedules":
	 * 				[{"d":"2012-07-07","bh":"10","eh":"20","aId":"4fe83ea858b0e4b0387cb36eae54066b"},
	 * 				 {"d":"2012-07-08","bh":"8","eh":"22","aId":"4fe83ea858b0e4b0387cb36eae54066c"}]
	 * 		}
	 */
	
	@Override
	public void doGet(HttpServletRequest request,HttpServletResponse response)throws IOException{
		StringBuffer sb = new StringBuffer();
		List<String> ls = new ArrayList<String>();
		ls.add("scheduled");
		ls.add("checked");
		List<Schedule> list = this.scheduleServiceImpl.querySchedules(ls);
		if (list != null) {
			sb.append("{\"schedules\":[");
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			Iterator<Schedule> it = list.iterator();
			while (it.hasNext()) {
				Schedule entry = it.next();
				String sd = df.format(entry.getScheduleDate());
				sb.append("{\"d\":\"").append(sd).append("\",\"bh\":\"")
						.append(entry.getBeginHour()).append("\",\"eh\":\"")
						.append(entry.getEndHour()).append("\",\"aId\":\"")
						.append(entry.getAppId()).append("\"},");
			}
			if (sb.length() > 20) {
				sb.deleteCharAt(sb.lastIndexOf(","));
			}
			sb.append("]}");
		}
		response.getWriter().write(sb.toString());
	}

}
