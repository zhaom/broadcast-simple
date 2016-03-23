package com.babeeta.hudee.gateway.device;

import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonController;
import org.apache.commons.daemon.DaemonInitException;

/**
 * Main entry of device gateway service
 * 
 */
public class Server {

	/**
	 * @param args
	 *            parameters
	 * @throws Exception
	 *             other exception thrown by start function
	 * @throws DaemonInitException
	 *             daemon init exception
	 */

	public static void main(final String[] args) throws DaemonInitException,
	        Exception {
		DeviceGatewayService dgs = new DeviceGatewayService();
		dgs.init(new DaemonContext() {
			@Override
			public String[] getArguments() {
				return args;
			}

			@Override
			public DaemonController getController() {
				return null;
			}
		});

		dgs.start();
	}
}