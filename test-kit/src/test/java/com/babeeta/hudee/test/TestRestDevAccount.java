package com.babeeta.hudee.test;

import java.io.IOException;
import java.util.Random;

import junit.framework.Assert;
import net.sf.json.JSONObject;

import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.babeeta.hudee.test.rest.dev.account.DeviceAuthClient;
import com.babeeta.hudee.test.rest.dev.account.DeviceRegisterClient;

@SuppressWarnings("javadoc")
public class TestRestDevAccount {
	private static final String IP = "192.168.20.83";

	@Before
	public void setUp() throws Exception {

	}

	public static String generateImei() {
		long randomNumber = 0;

		do {
			randomNumber = Math.abs(new Random().nextLong());
		} while (Long.toString(randomNumber).length() < 15);

		return (Long.toString(randomNumber).substring(0, 15));
	}

	@Test
	public void testDevAccount() {
		DeviceRegisterClient registerClient = new DeviceRegisterClient(IP, 8082);
		String imei = generateImei();
		registerClient.register(imei);
		Assert.assertEquals(registerClient.getResponseStatus(), 200);

		String result = null;
		try {
			result = EntityUtils
			        .toString(registerClient.getResponseEntity());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JSONObject account = JSONObject.fromObject(result);

		String did = account.getString("id");
		String key = account.getString("key");

		System.out.println("id = " + did + "; key = " + key);
		System.out.println(account.toString());

		DeviceAuthClient authClient = new DeviceAuthClient(IP, 8082);
		authClient.auth(did, key);
		Assert.assertEquals(authClient.getResponseStatus(), 200);
	}

	@After
	public void tearDown() throws Exception {
	}

}