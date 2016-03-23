package com.babeeta.hudee.test.capacity;

import java.util.Random;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

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
		        "4fed491858b0e4b03da483d77fb433c7",
		        "f1aa548df1244de2a38cd817c69cf0ec").start();
	}
}

class Context {
	final String host;
	final int port;
	final String appId;
	final String appKey;
	final String imei;

	public Context(CommandLine cl) {
		if (cl.getOptionValues("d") != null
		        && cl.getOptionValues("d").length == 2) {
			host = cl.getOptionValues("d")[0];
			port = Integer.parseInt(cl.getOptionValues("m")[1]);
		} else {
			host = "192.168.20.83";
			port = 5757;
		}

		if (cl.getOptionValue("a") != null
		        && cl.getOptionValue("a").trim().length() > 0) {
			appId = cl.getOptionValue("a");
		} else {
			appId = "4fe83ea858b0e4b0387cb36eae54066b";
		}

		if (cl.getOptionValue("k") != null
		        && cl.getOptionValue("k").trim().length() > 0) {
			appKey = cl.getOptionValue("k");
		} else {
			appKey = "30393169c3de4585a772b0060ff7c71b";
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