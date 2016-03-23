package com.babeeta.hudee.test.capacity;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

@SuppressWarnings("javadoc")
public class CapacityTester {
	public static void main(String[] args) {
		Options options = new Options();

		Option option = new Option("d", true, "device gateway host:port");
		option.setRequired(false);
		option.setArgName("host:port");
		option.setArgs(2);
		option.setValueSeparator(':');
		options.addOption(option);

		option = new Option("t", true, "total");
		option.setRequired(false);
		option.setArgName("app id");
		options.addOption(option);

		try {
			CommandLine cl = new GnuParser().parse(options, args);
			TestContext context = new TestContext(cl);
			bootstrap(context);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("java ....SimulatorMain", options);
		}
	}

	private static void bootstrap(TestContext context) throws Exception {
		System.out.println("Startup......");
		new DeviceGatewayCapacityTest(1000).startup(context.host, context.port,
		        context.total);
	}
}

class TestContext {
	final String host;
	final int port;
	final long total;

	public TestContext(CommandLine cl) {
		if (cl.getOptionValues("d") != null
		        && cl.getOptionValues("d").length == 2) {
			host = cl.getOptionValues("d")[0];
			port = Integer.parseInt(cl.getOptionValues("m")[1]);
		} else {
			host = "192.168.20.83";
			port = 5757;
		}

		if (cl.getOptionValue("t") != null
		        && cl.getOptionValue("t").trim().length() > 0) {
			total = Long.parseLong(cl.getOptionValue("t"));
		} else {
			total = 300000;
		}
	}
}
