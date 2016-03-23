package com.babeeta.hudee.test;

import java.io.IOException;

import junit.framework.Assert;

import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.Test;

import com.babeeta.hudee.test.servlet.app.gateway.PushClient;
import com.babeeta.hudee.test.servlet.app.gateway.QueryClient;

@SuppressWarnings("javadoc")
public class TestServletInterface {
	private static final String IP = "192.168.20.83";

	private static final String appId = "4fe83ea858b0e4b0387cb36eae54066b";
	private static final String appKey = "30393169c3de4585a772b0060ff7c71b";

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testNewApi() throws ParseException, IOException {
		System.out.println("\n\n>>>>>>>  Test new message interface >>>>>>");
		PushClient push = new PushClient(IP, 8080, appId, appKey);

		push.push(86400, "test~test! new api");

		Assert.assertEquals(push.getResponseStatus(), 200);

		String messageId = EntityUtils.toString(push.getResponseEntity());

		System.out.println("message id = " + messageId);

		QueryClient queryClient = new QueryClient(IP, 8080, appId, appKey);
		queryClient.query(messageId);
		Assert.assertEquals(queryClient.getResponseStatus(), 200);
		String messageStatus = EntityUtils.toString(queryClient
		        .getResponseEntity());

		System.out.println("message[" + messageId + "] is in " + messageStatus);

		System.out.println(">>>>>>> New message interface is okay!>>>>>");
	}
}
