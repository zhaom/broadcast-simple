package com.babeeta.hudee.core.router.network.monitor;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.hudee.core.router.network.DataDumpHandler;
import com.babeeta.hudee.core.router.network.RequestHandler;

/**
 * Created by IntelliJ IDEA. User: XYuser Date: 10-12-23 Time: 下午7:15 To change
 * this template use File | Settings | File Templates.
 */
public class Network implements NetworkMBean {

	private static final Logger logger = LoggerFactory.getLogger(Network.class);

	private final DataDumpHandler dataDumpHandler;
	private static final AtomicInteger counter = new AtomicInteger(0);

	/**
	 * Constructor
	 * 
	 * @param dataDumpHandler
	 *            data dump handler
	 */
	public Network(DataDumpHandler dataDumpHandler) {
		super();
		this.dataDumpHandler = dataDumpHandler;
		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
		try {
			int index = counter.incrementAndGet();
			ObjectName objectName = new ObjectName(
			        "MessageRouting-Network:name=Network-" + (index));
			mBeanServer.registerMBean(this, objectName);
			logger.info("Network-" + index + " MBean Server is started.");
		} catch (Exception e) {
			logger.info("Network MBean Server bootstrap error:" + e);
		}
	}

	@Override
	public void beginDump(String file) throws IOException {
		dataDumpHandler.startDump(file);
	}

	@Override
	public int getChannelGroupSize() {
		return RequestHandler.getChannelGroupSize();
	}

	@Override
	public void stopDump() throws IOException {
		dataDumpHandler.stopDump();
	}
}