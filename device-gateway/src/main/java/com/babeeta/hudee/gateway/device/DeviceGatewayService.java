package com.babeeta.hudee.gateway.device;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import org.apache.commons.lang.StringUtils;

import com.babeeta.hudee.core.MessageSelector;
import com.babeeta.hudee.core.router.network.Service;
import com.babeeta.hudee.gateway.device.tunnel.TunnelService;

/**
 * Device gateway service
 * 
 */
public class DeviceGatewayService implements Daemon {

	private ServerContext serverContext;
	private Service messageService;
	private TunnelService tunnelService;

	@Override
	public void destroy() {
		tunnelService.shutdown();
		messageService.shutdownGraceFully();
	}

	@Override
	public void init(DaemonContext ctx) throws DaemonInitException {
		System.out.println("initializing...");
		Options options = new Options();

		Option option = new Option("r", true, "网关编号");
		option.setRequired(true);
		option.setArgName("host");
		options.addOption(option);

		// I/O worker count
		option = new Option("s", true, "listen I/O worker count.");
		option.setRequired(false);
		option.setArgName("listenIoWorkerCount");
		options.addOption(option);

		option = new Option("c", true, "Receive I/O worker count.");
		option.setRequired(false);
		option.setArgName("receiveIoWorkerCount");
		options.addOption(option);

		try {
			CommandLine cl = new GnuParser().parse(options, ctx.getArguments());

			File DIR_LOG = new File(
			        StringUtils.isBlank(
			                System.getenv(("LOG_DIR"))) ?
			                "/var/log/dev-gateway-service"
			                : System.getenv(("LOG_DIR")));
			if (!DIR_LOG.exists()) {
				DIR_LOG.mkdirs();
			}
			System.setProperty("LOG_DIR", DIR_LOG.getCanonicalPath());

			serverContext = new ServerContext(cl);
			bootstrap(serverContext);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("java ....DeviceGatewayService", options);
		}
	}

	@Override
	public void start() throws Exception {

	}

	@Override
	public void stop() throws Exception {
	}

	private void bootstrap(final ServerContext serverContext)
	        throws Exception {

		messageService = new Service("DevGW",
		        createNewMessageSelector(),
		        serverContext.getSendWorkerCount());

		messageService.start(serverContext.getMessageServiceAddress());
		System.out.println("Message service started.");

		tunnelService = new TunnelService(serverContext);
		tunnelService.start();
		System.out.println("Tunnel service started.");
	}

	private MessageSelector createNewMessageSelector() {
		MessageSelector messageSelector = new MessageSelector();
		return messageSelector;
	}

}
