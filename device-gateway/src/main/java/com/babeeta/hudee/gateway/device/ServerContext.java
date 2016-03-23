package com.babeeta.hudee.gateway.device;

import java.io.IOException;
import java.util.ResourceBundle;

import org.apache.commons.cli.CommandLine;

import com.babeeta.hudee.gateway.device.policy.PullPolicy;
import com.babeeta.hudee.gateway.device.policy.PullPolicyProvider;
import com.babeeta.hudee.gateway.device.policy.SchedulePolicyProvider;
import com.babeeta.hudee.gateway.device.service.DeviceAccountService;
import com.babeeta.hudee.gateway.device.service.ScheduleService;

/**
 * device gateway service context
 * 
 */
public class ServerContext {
	private int listenWorkerCount = -1;
	private int sendWorkerCount = -1;
	private int messageWorker;
	private int subscriptionWorker;
	private int deviceAccountWorker;
	private int manageWorker;

	private String messageServiceAddress;

	private String messageServiceHost;
	private String subscriptionServiceHost;
	private String deviceAccountServiceHost;
	private String manageServiceHost;
	
	private final PullPolicy pullPolicy;

	ServerContext(CommandLine cl) throws IOException {
		messageServiceAddress = cl.getOptionValue("r") + ".gateway.dev";

		ResourceBundle rb = ResourceBundle.getBundle("service");
		if (rb != null) {
			messageServiceHost = rb.getString("gateway.service.message.host");
			subscriptionServiceHost = rb
			        .getString("gateway.service.subscription.host");
			deviceAccountServiceHost = rb
			        .getString("gateway.service.account.host");
			manageServiceHost = rb
			        .getString("gateway.service.manage.host");

			messageWorker = Integer.parseInt(rb
			        .getString("gateway.service.message.worker"));
			subscriptionWorker = Integer.parseInt(rb
			        .getString("gateway.service.subscription.worker"));
			deviceAccountWorker = Integer.parseInt(rb
			        .getString("gateway.service.account.worker"));
			manageWorker = Integer.parseInt(rb
			        .getString("gateway.service.manage.worker"));

			String strListenWorkerCount = ((cl.getOptionValue("s") != null && cl
			        .getOptionValue("s").length() > 0) ? cl.getOptionValue("s")
			        : rb.getString("max-listen-worker"));
			if (strListenWorkerCount != null) {
				listenWorkerCount = Integer.parseInt(strListenWorkerCount);
			} else {
				listenWorkerCount = Runtime.getRuntime()
				        .availableProcessors() * 4;
			}

			String strSendWorkerCount = ((cl.getOptionValue("c") != null && cl
			        .getOptionValue("c").length() > 0) ? cl.getOptionValue("c")
			        : rb.getString("max-send-worker"));
			if (strSendWorkerCount != null) {
				sendWorkerCount = Integer
				        .parseInt(strSendWorkerCount);
			} else {
				sendWorkerCount = Runtime.getRuntime()
				        .availableProcessors() * 4;
			}
		} else {
			messageServiceHost = "message.dev";
			deviceAccountServiceHost = "accounts.dev";
			subscriptionServiceHost = "subscription.dev";
			manageServiceHost = "manage.dev";
			messageWorker = 512;
			subscriptionWorker = 256;
			deviceAccountWorker = 256;
			manageWorker = 1;
			listenWorkerCount = Runtime.getRuntime().availableProcessors() * 4;
			sendWorkerCount = Runtime.getRuntime().availableProcessors() * 4;
		}

		//pullPolicy = new PullPolicyProvider();
		pullPolicy = new SchedulePolicyProvider(new ScheduleService(this.getManageWorker(),this.getManageServiceHost()));
		
	}

	/**
	 * @return the messageWorker
	 */
	public int getMessageWorker() {
		return messageWorker;
	}

	/**
	 * @return the subscriptionWorker
	 */
	public int getSubscriptionWorker() {
		return subscriptionWorker;
	}

	/**
	 * @return the deviceAccountWorker
	 */
	public int getDeviceAccountWorker() {
		return deviceAccountWorker;
	}

	/**
	 * @return the messageServiceAddress
	 */
	public String getMessageServiceAddress() {
		return messageServiceAddress;
	}

	/**
	 * @return the messageServiceHost
	 */
	public String getMessageServiceHost() {
		return messageServiceHost;
	}

	/**
	 * @return the subscriptionServiceHost
	 */
	public String getSubscriptionServiceHost() {
		return subscriptionServiceHost;
	}

	/**
	 * @return the deviceAccountServiceHost
	 */
	public String getDeviceAccountServiceHost() {
		return deviceAccountServiceHost;
	}

	/**
	 * @return the listenWorkerCount
	 */
	public int getListenWorkerCount() {
		return listenWorkerCount;
	}

	/**
	 * @return the sendWorkerCount
	 */
	public int getSendWorkerCount() {
		return sendWorkerCount;
	}

	/**
	 * @return pull policy instance
	 */
	public PullPolicy getPullPolicy() {
		return pullPolicy;
	}

	public int getManageWorker() {
		return manageWorker;
	}

	public void setManageWorker(int manageWorker) {
		this.manageWorker = manageWorker;
	}

	public String getManageServiceHost() {
		return manageServiceHost;
	}

	public void setManageServiceHost(String manageServiceHost) {
		this.manageServiceHost = manageServiceHost;
	}
}