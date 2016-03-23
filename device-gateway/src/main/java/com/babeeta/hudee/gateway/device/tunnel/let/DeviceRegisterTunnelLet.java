package com.babeeta.hudee.gateway.device.tunnel.let;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.hudee.core.MessageRouting;
import com.babeeta.hudee.core.MessageRouting.Credential;
import com.babeeta.hudee.core.MessageRouting.DeviceRegister;
import com.babeeta.hudee.core.MessageRouting.WorkMode;
import com.babeeta.hudee.gateway.device.ServerContext;
import com.babeeta.hudee.gateway.device.service.DeviceAccountService;
import com.babeeta.hudee.gateway.device.tunnel.TunnelContext;
import com.babeeta.hudee.gateway.device.tunnel.TunnelData;
import com.babeeta.hudee.gateway.device.tunnel.TunnelLet;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.MessageLite;

/**
 * Handle "register" message from device.
 * 
 */
public class DeviceRegisterTunnelLet implements TunnelLet<DeviceRegister> {

	private static final Logger logger = LoggerFactory
	        .getLogger(DeviceRegisterTunnelLet.class);
	@SuppressWarnings("unused")
	private final ServerContext serverContext;

	/**
	 * Count the "device register" message from client for monitor
	 */
	private static AtomicLong registerMessageCounter = new AtomicLong(0);

	/**
	 * Count the times of "device register" execute successful for monitor
	 */
	private static AtomicLong registerSuccessCounter = new AtomicLong(0);

	/**
	 * Count the times of service unavailable for monitor
	 */
	private static AtomicLong serviceUnavailableCounter = new AtomicLong(0);

	private final DeviceAccountService deviceAccountService;

	/**
	 * Constructor
	 * 
	 * @param serverContext
	 *            server context, included initiative parameters
	 * @param deviceAccountService
	 *            instance of device account
	 */
	public DeviceRegisterTunnelLet(ServerContext serverContext,
	        DeviceAccountService deviceAccountService) {
		super();
		this.serverContext = serverContext;
		this.deviceAccountService = deviceAccountService;
	}

	@Override
	public void messageReceived(
	        final TunnelContext tunnelContext,
	        final TunnelData<DeviceRegister> data) {
		registerMessageCounter.getAndIncrement();
		onDeviceRegister(tunnelContext, data);
	}

	@Override
	public void shutdown() throws InterruptedException {
		logger.info("Shutting down...");
		deviceAccountService.destroy();
		logger.info("Shut down.");
	}

	private String convertToJson(DeviceRegister deviceRegister) {
		Map<FieldDescriptor, Object> map = deviceRegister.getAllFields();
		Map<String, Object> stringMap = new HashMap<String, Object>();
		Iterator<FieldDescriptor> it = map.keySet().iterator();
		while (it.hasNext()) {
			FieldDescriptor key = it.next();
			String name = key.getFullName().substring(
			        key.getFullName().lastIndexOf('.') + 1);
			stringMap.put(name, map.get(key));
		}
		return JSONObject.fromObject(stringMap).toString();
	}

	private void onDeviceRegister(final TunnelContext tunnelContext,
	        final TunnelData<DeviceRegister> data) {
		DeviceRegister deviceRegister = data.obj;

		String remoteResult = null;
		remoteResult = deviceAccountService
		        .doRegister(convertToJson(deviceRegister));
		if (remoteResult != null) {
			JSONObject obj = JSONObject.fromObject(remoteResult);
			// TODO: work mode should be configurable.
			WorkMode workMode = WorkMode.PULL;
			Credential credential = Credential.newBuilder()
			        .setId(obj.getString("id"))
			        .setSecureKey(obj.getString("key"))
			        .setWorkMode(workMode)
			        .build();
			tunnelContext.setDeviceId(credential.getId());
			tunnelContext.setPushMode(workMode == WorkMode.PUSH);
			registerSuccessCounter.getAndIncrement();
			tunnelContext.getChannel().write(new TunnelData<MessageLite>(
			        data.tag, MessageRouting.MessageCMD.CREDENTIAL.getNumber(),
			        credential));
			logger.info("[{}] Sent new device to client. [{}]",
			        tunnelContext.getChannel()
			                .getId(), obj.toString());
			devicePostLogin(tunnelContext);
		} else {
			logger.error("[{}] Device register failed!", tunnelContext
			        .getChannel().getId());
		}
	}

	private void devicePostLogin(TunnelContext tunnelContext) {

	}

	/**
	 * Get the value of "device register" message counter
	 * 
	 * @param reset
	 *            whether reset counter.
	 * @return the value of "device register" message counter.
	 */
	public synchronized static long getDeviceRegisterMessageCounter(
	        boolean reset) {
		if (reset) {
			return registerMessageCounter.getAndSet(0);
		} else {
			return registerMessageCounter.get();
		}
	}

	/**
	 * Get the value of register success counter
	 * 
	 * @param reset
	 *            whether reset counter.
	 * @return the value of register success counter.
	 */
	public synchronized static long getRegisterSuccessCounter(boolean reset) {
		if (reset) {
			return registerSuccessCounter.getAndSet(0);
		} else {
			return registerSuccessCounter.get();
		}
	}

	/**
	 * Get the value of service unavailable counter
	 * 
	 * @param reset
	 *            whether reset counter.
	 * @return the value of service unavailable counter.
	 */
	public synchronized static long getServiceUnavailableCounter(boolean reset) {
		if (reset) {
			return serviceUnavailableCounter.getAndSet(0);
		} else {
			return serviceUnavailableCounter.get();
		}
	}
}