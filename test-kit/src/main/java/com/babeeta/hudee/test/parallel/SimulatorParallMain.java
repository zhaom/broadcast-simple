package com.babeeta.hudee.test.parallel;

import java.util.ResourceBundle;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class SimulatorParallMain {

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

		option = new Option("u", true, "virtual user");
		option.setRequired(false);
		option.setArgName("vUser");
		options.addOption(option);

		option = new Option("t", true, "total count");
		option.setRequired(false);
		option.setArgName("total");
		options.addOption(option);
		
		option = new Option("m", true, "keep minutes");
		option.setRequired(false);
		option.setArgName("minutes");
		options.addOption(option);

		try {
			CommandLine cl = new GnuParser().parse(options, args);
			ParrallContext context = new ParrallContext(cl);
			bootstrap(context);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("java ....SimulatorParallMain", options);
		}
	}

	private static void bootstrap(ParrallContext context) throws Exception {
		LoadRunner.getInstance().startup(context);
	}
}

class ParrallContext {
	 String host;
	 int port;
	 String appId;
	 String appKey;
	 long vUser;
	 long total;
	 long keepInMinutes;

	public ParrallContext(CommandLine cl) {
		
		ResourceBundle rb = ResourceBundle.getBundle("service");
		if(rb != null){
			appId = rb.getString("appId");
			appKey = rb.getString("appKey");
			vUser = Long.valueOf(rb.getString("vUser"));
			total = Long.valueOf(rb.getString("total"));
			keepInMinutes = Long.valueOf(rb.getString("keepInMinutes"));
			host = rb.getString("hostIp");
			port = Integer.valueOf(rb.getString("port"));
		}
		
		if (cl.getOptionValues("d") != null
				&& cl.getOptionValues("d").length == 2) {
			host = cl.getOptionValues("d")[0];
			port = Integer.parseInt(cl.getOptionValues("m")[1]);
		}

		if (cl.getOptionValue("a") != null
				&& cl.getOptionValue("a").trim().length() > 0) {
			appId = cl.getOptionValue("a");
		} 

		if (cl.getOptionValue("k") != null
				&& cl.getOptionValue("k").trim().length() > 0) {
			appKey = cl.getOptionValue("k");
		} 

		if (cl.getOptionValue("u") != null
				&& cl.getOptionValue("u").trim().length() > 0) {
			vUser = Long.parseLong(cl.getOptionValue("u"));
		} 

		if (cl.getOptionValue("t") != null
				&& cl.getOptionValue("t").trim().length() > 0) {
			total = Long.parseLong(cl.getOptionValue("t"));
		}
	}
}
