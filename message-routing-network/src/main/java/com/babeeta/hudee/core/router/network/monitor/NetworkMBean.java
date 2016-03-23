package com.babeeta.hudee.core.router.network.monitor;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA. User: XYuser Date: 10-12-23 Time: 下午7:15 To change
 * this template use File | Settings | File Templates.
 */
public interface NetworkMBean {
	/**
	 * @param file
	 * @throws IOException
	 */
	void beginDump(String file) throws IOException;

	/**
	 * @return size of channel group
	 */
	int getChannelGroupSize();

	/**
	 * @throws IOException
	 */
	void stopDump() throws IOException;
}
