package com.babeeta.hudee.gateway.device.policy;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.hudee.gateway.device.service.ScheduleService;

public class SchedulePolicyProvider implements PullPolicy {
	
	private static final  Logger logger = LoggerFactory
	        .getLogger(SchedulePolicyProvider.class);
	
	private Hashtable<String,List<Schedule>> schedules = new Hashtable<String,List<Schedule>>();
	
	private Idle currentIdle;
	
	private final ScheduleService scheduleService;
	
	private static ScheduledExecutorService scheduledExecutorService = Executors
			.newScheduledThreadPool(1);
	
	/**
	 * just for test use,remove when publish
	 */
	@SuppressWarnings("deprecation")
	public SchedulePolicyProvider(){
		this.scheduleService = new ScheduleService(1,"");
		Date nowDate = new Date();
		addSchedule("4fe83ea858b0e4b0387cb36eae54066b", nowDate, 8, 22);
		for (int i = 0; i < 20; i++) {
			nowDate.setDate(nowDate.getDate() + 1);
			addSchedule("4fe83ea858b0e4b0387cb36eae54066b",
					nowDate, 8, 22);
		}
		refreshScheduleAndIdle();
	}
	
	public SchedulePolicyProvider(ScheduleService ss){
		this.scheduleService = ss;
        refreshScheduleAndIdle();
		scheduledExecutorService.scheduleAtFixedRate(new Runnable(){
			@Override
			public void run() {
				logger.info("schedule begin at :" + (new Date()));
				try{
					String remoteResult = scheduleService.querySchedules();
					JSONObject obj = JSONObject.fromObject(remoteResult);
					JSONArray ja = obj.getJSONArray("schedules");
					if(ja!=null){
						int jaSize = ja.size();
						for(int i=0; i<jaSize; i++){
							JSONObject entry = ja.getJSONObject(i);
							String aId = entry.getString("aId");
							String d = entry.getString("d");
							String bh = entry.getString("bh");
							String eh = entry.getString("eh");
							DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
							Date scheduleDate = df.parse(d);
							addSchedule(aId,scheduleDate,Integer.parseInt(bh),Integer.parseInt(eh));
						}
					}
					refreshScheduleAndIdle();
				}catch(Exception ex){
					logger.error("error in schedule"+ex.getMessage());
					ex.printStackTrace();
				}
				logger.info("schedule end at :" + (new Date()));
			}}, 1L, 5L, TimeUnit.MINUTES);
	}
	
	
	public void refreshScheduleAndIdle() {
		ResourceBundle rb = ResourceBundle.getBundle("service");
		String beginStr = rb.getString("schedule.idle.beginhour");
		String endStr = rb.getString("schedule.idle.endhour");

		int beginHour = Integer.parseInt(beginStr);
		int endHour = Integer.parseInt(endStr);
		long nowInSec = System.currentTimeMillis()/1000L;
		if (currentIdle == null) {
			currentIdle = createIdleFromContext(beginHour,endHour);
		}else{
			if(!currentIdle.isBeforeIdleDuration(nowInSec)){
				currentIdle = currentIdle.getNextIdle();
			}
		}
		if(schedules!=null){
			Collection<List<Schedule>> cls = schedules.values();
			if(cls!=null){
				Iterator<List<Schedule>> itc = cls.iterator();
				while(itc.hasNext()){
					List<Schedule> ls = itc.next();
					if(ls!=null){
						Iterator<Schedule> it = ls.iterator();
						while(it.hasNext()){
							Schedule schedule = it.next();
							if(schedule.isAfterServiceDuration(nowInSec)){
								ls.remove(schedule);
							}
						}
					}
				}
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	private Idle createIdleFromContext(int beginHour,int endHour){
		Date begin = new Date();
		Date end = new Date();
		int currenthour = begin.getHours();
		if (currenthour < beginHour) {
			begin.setHours(beginHour);
			begin.setMinutes(0);
			begin.setSeconds(0);
			
			end.setDate(end.getDate()+1);
			end.setHours(endHour);
			end.setMinutes(0);
			end.setSeconds(0);
		}else{
			begin.setDate(begin.getDate()+1);
			begin.setHours(beginHour);
			begin.setMinutes(0);
			begin.setSeconds(0);
			
			end.setDate(end.getDate()+2);
			end.setHours(endHour);
			end.setMinutes(0);
			end.setSeconds(0);
		}
		return new Idle(begin,end);
	}
	
	@SuppressWarnings("deprecation")
	public void addSchedule(String identifier,Date scheduleDate, int beginHour, int endHour){
		if(endHour < beginHour){
			logger.error("schedule's begin hour [{}] is greater than end hour [{}],omit it!",new int[]{beginHour,endHour});
			return;
		}
		Schedule schedule = new Schedule(scheduleDate,beginHour,endHour);
		if(schedules.containsKey(identifier)){
			List<Schedule> ls = schedules.get(identifier);
			if(ls!=null && ls.size()>0){
				Iterator<Schedule> it = ls.iterator();
				while(it.hasNext()){
					Schedule sch = it.next();
					if(sch.isOverlap(schedule)){
						logger.info("The schedule [{}],[{}],[{}] existed,omit it", new String[]{scheduleDate.toLocaleString(), ""+beginHour,""+endHour});
						return;
					}else if(schedule.isBeforeSchedule(sch)){
						ls.add(ls.indexOf(sch), schedule);
						return;
					}
				}
			}
			if(ls==null|| ls.size()<1){
				ls = new ArrayList<Schedule>();
			}
			ls.add(schedule);
		}else{
			List<Schedule> ls = new ArrayList<Schedule>();
			ls.add(schedule);
			schedules.put(identifier, ls);
		}
	}

	@Override
	public long getNextInterval(String identifier, String clientKey,
			long timeInSec) {
		if(schedules.containsKey(identifier)){
			List<Schedule> ls = schedules.get(identifier);
			if(ls!=null){
				Iterator<Schedule> it = ls.iterator();
				while(it.hasNext()){
					Schedule schedule = it.next();
					if(schedule.isBeforeServiceDuration(timeInSec)){
						return schedule.getReservedInterval(clientKey, timeInSec);
					}
				}
			}
		}
		return currentIdle.getNextIdleInterval(clientKey, timeInSec);
	}

	@Override
	public long getCurrentInterval(String identifier, String clientKey,
			long timeInSec) {
		if(schedules.containsKey(identifier)){
			List<Schedule> ls = schedules.get(identifier);
			if(ls!=null){
				Iterator<Schedule> it = ls.iterator();
				while(it.hasNext()){
					Schedule entry = it.next();
					if(entry.isInServiceDuration(timeInSec)){
						return entry.getCurrentInterval(clientKey, timeInSec);
					}
				}
			}
		}
		return getNextInterval(identifier,clientKey,timeInSec);
	}

	@Override
	public boolean isInService(String identifier, long timeInSec) {
		if(schedules.containsKey(identifier)){
			List<Schedule> ls = schedules.get(identifier);
			if(ls!=null){
				Iterator<Schedule> it = ls.iterator();
				while(it.hasNext()){
					Schedule entry = it.next();
					if(entry.isInServiceDuration(timeInSec)){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * @param value
	 *            input value
	 * @return result of MD5
	 */
	private static byte[] computeMD5(String value) {
		MessageDigest md5;
		try {
			md5 = MessageDigest.getInstance("md5");
			md5.reset();
			return md5.digest(value.getBytes("utf-8"));

		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("MD5 not supported.", e);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("UTF-8 not supported.", e);
		}
	}

	/**
	 * @param digest
	 *            MD5 result
	 * @param h
	 *            ?
	 * @return key of node
	 */
	private static Long computeNodeKey(byte[] digest, int h) {
		return ((long) (digest[3 + h * 4] & 0xFF) << 24)
		        | ((long) (digest[2 + h * 4] & 0xFF) << 16)
		        | ((long) (digest[1 + h * 4] & 0xFF) << 8)
		        | (digest[h * 4] & 0xFF);
	}
	
	/**
	 * @param key
	 *            key
	 * @return hash of key
	 */
	private static Long computeKeyHash(String key) {
		byte[] bKey = computeMD5(key);
		return ((long) (bKey[3] & 0xFF) << 24)
		        | ((long) (bKey[2] & 0xFF) << 16)
		        | ((long) (bKey[1] & 0xFF) << 8)
		        | (bKey[0] & 0xFF);
	}
	
	public ScheduleService getScheduleService() {
		return scheduleService;
	}


	class Schedule {
		
		private long beginInSecond;
		
		private long endInSecond;
		
		private TreeMap<Long, Long> resourceMap = new TreeMap<Long, Long>();
		
		@SuppressWarnings("deprecation")
		public Schedule(Date scheduleDate, int beginHour, int endHour){
			scheduleDate.setHours(beginHour);
			scheduleDate.setMinutes(0);
			scheduleDate.setSeconds(0);
			this.beginInSecond = scheduleDate.getTime()/1000L;
			scheduleDate.setHours(endHour);
			this.endInSecond = scheduleDate.getTime()/1000L;
			
			int duration = (endHour - beginHour) * 3600;
			int totalZones = duration / (300);
			createKetamaHashRing(totalZones);
		}
		
		public boolean isBeforeSchedule(Schedule sch) {
			if(this.endInSecond<sch.beginInSecond){
				return true;
			}
			return false;
		}

		public boolean isInServiceDuration(long timeInSec) {
			if (timeInSec>=this.beginInSecond && timeInSec<this.endInSecond) {
				return true;
			}
			return false;
		}
		
		public boolean isAfterServiceDuration(long timeInSec){
			if(timeInSec >=this.endInSecond){
				return true;
			}
			return false;
		}
		
		public boolean isBeforeServiceDuration(long timeInSec){
			if(timeInSec < this.beginInSecond){
				return true;
			}
			return false;
		}
		
		public boolean isOverlap(Schedule sch){
			if(this.beginInSecond>sch.endInSecond && this.endInSecond<sch.beginInSecond)
			{
				return false;
			}
			return true;
		}
		
		public long getReservedInterval(String key, long timeInSec) {
			if(isBeforeServiceDuration(timeInSec)){
				long sdis = this.beginInSecond - timeInSec;
				long standard = 0;
				final TreeMap<Long, Long> resourceMap = this.resourceMap;
				Long k = computeKeyHash(key);
				if (resourceMap.containsKey(k)) {
					standard = resourceMap.get(k);
				} else {
					k = resourceMap.ceilingKey(k);
					if (k == null) {
						k = resourceMap.firstKey();
					}
					standard = resourceMap.get(k);
				}
				return standard + sdis;
			}
			//should never go here
			return getCurrentInterval(key,timeInSec);
		}
		
		public long getCurrentInterval(String key, long timeInSec) {
			if (isInServiceDuration(timeInSec)) {
				long sdis = (timeInSec - this.beginInSecond);
				long standard = 0;
				final TreeMap<Long, Long> resourceMap = this.resourceMap;
				Long k = computeKeyHash(key);
				if (resourceMap.containsKey(k)) {
					standard = resourceMap.get(k);
				} else {
					k = resourceMap.ceilingKey(k);
					if (k == null) {
						k = resourceMap.firstKey();
					}
					standard = resourceMap.get(k);
				}

				if (standard>=sdis) {
					return standard - sdis;
				}
				return standard;
			}
			//should never go here
			return getReservedInterval(key,timeInSec);
		}
		
		private void createKetamaHashRing(int totalZones) {
			for (int j = 0; j < totalZones; j++) {
				long key = j * 300;
				byte[] digest = computeMD5(Long.toString(key));
				for (int h = 0; h < 4; h++) {
					resourceMap.put(computeNodeKey(digest, h), key);
				}
			}
		}
	}
	
	class Idle{
		
		private long beginInSec;
		
		private long endInSec;
		
		private TreeMap<Long, Long> resourceMap = new TreeMap<Long, Long>();
		
		private Idle nextIdle;
		
		public Idle(Date bDate,Date eDate){
			this.beginInSec = bDate.getTime()/1000L;
			this.endInSec = eDate.getTime()/1000L;
			long duration = this.endInSec - this.beginInSec;
			int totalZones = (int)duration / (300);
			createKetamaHashRing(totalZones);
		}
		
		private Idle createNextIdle() {
			long b = this.beginInSec + 86400L;
			long e = this.endInSec + 86400L;
			Date bDate = new Date();
			bDate.setTime(b * 1000L);
			Date eDate = new Date();
			eDate.setTime(e * 1000L);
			return new Idle(bDate, eDate);
		}
		
		public Idle getNextIdle(){
			return createNextIdle();
		}

		public boolean isInIdleDuration(long timeInSec) {
			if (timeInSec>=this.beginInSec && timeInSec<this.endInSec) {
				return true;
			}
			return false;
		}
		
		
		public boolean isBeforeIdleDuration(long timeInSec) {
			if (timeInSec<this.beginInSec) {
				return true;
			}
			return false;
		}
		
		public long getNextIdleInterval(String key, long timeInSec) {
			if (isBeforeIdleDuration(timeInSec)) {
				long sdis = this.beginInSec - timeInSec;
				long standard = 0;
				final TreeMap<Long, Long> resourceMap = this.resourceMap;
				Long k = computeKeyHash(key);
				if (resourceMap.containsKey(k)) {
					standard = resourceMap.get(k);
				} else {
					k = resourceMap.ceilingKey(k);
					if (k == null) {
						k = resourceMap.firstKey();
					}
					standard = resourceMap.get(k);
				}
				return standard +sdis;
			}
			if(nextIdle ==null){
				nextIdle = createNextIdle();
			}
			return nextIdle.getNextIdleInterval(key, timeInSec);
		}
		
		private void createKetamaHashRing(int totalZones) {
			for (int j = 0; j < totalZones; j++) {
				long key = j * 300;
				byte[] digest = computeMD5(Long.toString(key));
				for (int h = 0; h < 4; h++) {
					resourceMap.put(computeNodeKey(digest, h), key);
				}
			}
		}
		
	}
}
