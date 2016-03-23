package com.babeeta.hudee.test;

import java.io.IOException;

import junit.framework.Assert;
import net.sf.json.JSONObject;

import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.babeeta.hudee.test.rest.app.account.AppAuthClient;
import com.babeeta.hudee.test.rest.app.account.AppRegisterClient;

@SuppressWarnings("javadoc")
public class TestRestAppAccount {
	private static final String IP = "192.168.20.83";

	@Before
	public void setUp() throws Exception {

	}

	@Test
	public void testAppAccount() {
		AppRegisterClient registerClient = new AppRegisterClient(IP, 8081);
		registerClient.register("Xinyu-test", "xinyu.liu@babeeta.com");
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

		String appId = account.getString("id");
		String appKey = account.getString("key");

		System.out.println("appId = " + appId + "; appKey = " + appKey);
		System.out.println(account.toString());

		AppAuthClient authClient = new AppAuthClient(IP, 8081);
		authClient.auth(appId, appKey);
		Assert.assertEquals(authClient.getResponseStatus(), 200);
	}

	@After
	public void tearDown() throws Exception {
	}

}