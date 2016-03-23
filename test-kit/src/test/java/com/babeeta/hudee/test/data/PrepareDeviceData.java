package com.babeeta.hudee.test.data;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import junit.framework.Assert;
import net.sf.json.JSONObject;

import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import com.babeeta.hudee.test.rest.dev.account.DeviceRegisterClient;
import com.babeeta.hudee.test.rest.dev.subscription.NewBindClient;

/*
 appId = 4fe83ea858b0e4b0387cb36eae54066b; appKey = 30393169c3de4585a772b0060ff7c71b
 */
@SuppressWarnings("javadoc")
public class PrepareDeviceData {
	private static final String IP = "192.168.20.83";
	private static final String aid = "4fe83ea858b0e4b0387cb36eae54066b";
	private static final int limit = 60;

	public static String generateImei() {
		long randomNumber = 0;

		do {
			randomNumber = Math.abs(new Random().nextLong());
		} while (Long.toString(randomNumber).length() < 15);

		return (Long.toString(randomNumber).substring(0, 15));
	}

	@Ignore
	@SuppressWarnings("static-access")
	@Test
	public final void test() throws ParseException, IOException,
	        InterruptedException {
		// prepare clients
		DeviceRegisterClient registerClient = new DeviceRegisterClient(IP, 8082);
		NewBindClient newBinder = new NewBindClient(IP, 8083);
		// prepare file
		PrintWriter file = new PrintWriter("d:\\user_info.txt");
		for (int i = 0; i < limit; i++) {
			User user = new User();

			// Step 1: register device
			String imei = generateImei();
			registerClient.register(imei);
			Assert.assertEquals(registerClient.getResponseStatus(), 200);

			String result = null;
			result = EntityUtils.toString(registerClient.getResponseEntity());
			JSONObject account = JSONObject.fromObject(result);

			String did = account.getString("id");
			String key = account.getString("key");

			System.out.println("id = " + did + "; key = " + key);
			System.out.println(account.toString());

			user.did = did;
			user.key = key;
			// Step 2: bind to application
			newBinder.newBind(aid, did);
			Assert.assertEquals(newBinder.getResponseStatus(), 200);
			String cid = EntityUtils.toString(newBinder.getResponseEntity());
			Assert.assertNotSame(cid.length(), 0);

			System.out.println("Test new bind okay!");

			user.aid = aid;
			user.cid = cid;

			// Step 3: output to file
			file.printf("%s;%s;%s;%s\n", user.did, user.key, user.aid, user.cid);

			Thread.currentThread().sleep(1000L);
		}
		file.flush();
		file.close();
	}

	private class User {
		public String did;
		public String key;
		public String aid;
		public String cid;
	}
}
