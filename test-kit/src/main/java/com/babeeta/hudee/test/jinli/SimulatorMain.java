package com.babeeta.hudee.test.jinli;

import com.babeeta.hudee.test.jinli.ClientSimulator;
import org.apache.commons.cli.*;

import java.util.Random;
import java.util.ResourceBundle;

@SuppressWarnings("javadoc")
public class SimulatorMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Options options = new Options();

		Option option = new Option("d", true, "device gateway host:port");
		option.setRequired(false);
		option.setArgName("host:port");
		option.setArgs(2);
		option.setValueSeparator(':');
		options.addOption(option);

		option = new Option("a", true, "app id");
		option.setRequired(false);
		option.setArgName("app id");
		options.addOption(option);

		option = new Option("k", true, "app key");
		option.setRequired(false);
		option.setArgName("app key");
		options.addOption(option);

		option = new Option("i", true, "imei");
		option.setRequired(false);
		option.setArgName("imei");
		options.addOption(option);

		try {
			CommandLine cl = new GnuParser().parse(options, args);
			Context context = new Context(cl);
			bootstrap(context);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("java ....SimulatorMain", options);
		}
	}

	private static void bootstrap(Context context) throws Exception {
		new ClientSimulator(context.host, context.port, context.appId,
		        context.appKey,
		        context.imei).start();
	}
}

class Context {
	final String host;
	final int port;
	final String appId;
	final String appKey;
	final String imei;

	public Context(CommandLine cl) {
        ResourceBundle rb = ResourceBundle.getBundle("config");
		if (cl.getOptionValues("d") != null
		        && cl.getOptionValues("d").length == 2) {
			host = cl.getOptionValues("d")[0];
			port = Integer.parseInt(cl.getOptionValues("m")[1]);
		} else {
            String hostIp = rb.getString("gateway.device.ip");
            String strPort = rb.getString("gateway.device.port");
			host = hostIp;
			port = Integer.parseInt(strPort);
		}

		if (cl.getOptionValue("a") != null
		        && cl.getOptionValue("a").trim().length() > 0) {
			appId = cl.getOptionValue("a");
		} else {
			appId = rb.getString("simulator.app.id");
		}

		if (cl.getOptionValue("k") != null
		        && cl.getOptionValue("k").trim().length() > 0) {
			appKey = cl.getOptionValue("k");
		} else {
			appKey = rb.getString("simulator.app.key");
		}

		if (cl.getOptionValue("i") != null
		        && cl.getOptionValue("i").trim().length() > 0) {
			imei = cl.getOptionValue("i");
		} else {
			imei = generateImei();
		}
	}

	private String generateImei() {
		long randomNumber = 0;

		do {
			randomNumber = Math.abs(new Random().nextLong());
		} while (Long.toString(randomNumber).length() < 15);

		return (Long.toString(randomNumber).substring(0, 15));
	}
}