package com.babeeta.hudee.gateway.device.service;

import org.apache.http.HttpStatus;
import org.apache.http.util.CharArrayBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScheduleService extends AbstractHttpRPCService {

	private static final Logger logger = LoggerFactory
	        .getLogger(ScheduleService.class);
	
	private final String domain;
	
	
	
	public ScheduleService(int maxConnection,String domain) {
		super(maxConnection);
		this.domain = domain;
	}
	
	
	private interface Callback {
		String doIt();
	}
	
	private String execute(Callback callback) {
		return callback.doIt();
	}
	
	public String querySchedules() {
		logger.debug("[RPC-querySchedules] query schedules");
		String ret = execute(new Callback() {
			@Override
			public String doIt() {
				return query();
			}
		});
		return ret;
	}

	private String query() {
		CharArrayBuffer buffer = new CharArrayBuffer(100);
		buffer.append("/1/api/schedule");
		HttpRPCResult result = invokeGet(
		        composeURI(domain, buffer.toString()), HttpStatus.SC_OK);

		if (result.getStatusCode() == HttpStatus.SC_OK) {
			return new String(result.getPayload());
		}
		logger.error(
		        "[RPC-querySchedules] failed! statusCode: {}; message: {}",
		        result.getStatusCode(), result.getMessage());
		return null;
	}
}
