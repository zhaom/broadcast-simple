package com.babeeta.hudee.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;
import net.sf.json.JSONObject;

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.bson.BSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.babeeta.hudee.test.rest.dev.message.MessageCountClient;
import com.babeeta.hudee.test.rest.dev.message.MessageCreateClient;
import com.babeeta.hudee.test.rest.dev.message.MessageListClient;
import com.babeeta.hudee.test.rest.dev.message.MessageQueryClient;
import com.babeeta.hudee.test.rest.dev.message.MessageUpdateClient;
import com.mongodb.Bytes;

@SuppressWarnings("javadoc")
public class TestRestMessage {
	private static final String IP = "192.168.20.83";
	private final String recipient = "02f31dc2e78349d785c82017be661e20";
	private final String sender = "4274dfb51e684aad899ed6bfb101ab91";
	private final String device = "b29762c7283b40b7865efa3ae22fb1cb";
	private final String content = "test-content";

	@Before
	public void setUp() throws Exception {

	}

	@Test
	public void testDevMessage() throws ParseException, IOException {
		MessageCreateClient createClient = new MessageCreateClient(IP, 8084);
		createClient.create(sender, recipient, device, content, 3600);
		Assert.assertEquals(createClient.getResponseStatus(), 201);

		String messageId = null;
		messageId = EntityUtils.toString(createClient.getResponseEntity());
		System.out.println(messageId);

		MessageUpdateClient update = new MessageUpdateClient(IP, 8084);
		update.update(messageId, "ACKED");
		Assert.assertEquals(update.getResponseStatus(), 200);

		MessageQueryClient query = new MessageQueryClient(IP, 8084);
		query.query(messageId);
		Assert.assertEquals(query.getResponseStatus(), 200);

		Assert.assertEquals(
		        "ACKED",
		        JSONObject.fromObject(
		                EntityUtils.toString(query.getResponseEntity()))
		                .getString("status"));

		MessageListClient list = new MessageListClient(IP, 8084);
		list.list(device, "ACKED");
		Assert.assertEquals(list.getResponseStatus(), 200);

		HttpEntity entity = list.getResponseEntity();
		byte[] result = EntityUtils.toByteArray(entity);

		BSONObject bsonList = Bytes.decode(result);
		long msgCount = 0;
		Iterator<String> it = bsonList.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			BSONObject value = (BSONObject) bsonList.get(key);
			Assert.assertEquals(value.get("_id").toString(), key);
			System.out.println("id =[" + key + "] content = ["
			        + value.toString() + "]");
			msgCount++;
		}

		MessageCountClient count = new MessageCountClient(IP, 8084);
		count.list(device, "ACKED");
		Assert.assertEquals(count.getResponseStatus(), 200);

		long messageCount = Long.parseLong(EntityUtils.toString(count
		        .getResponseEntity()));

		Assert.assertEquals(msgCount, messageCount);
	}

	@Test
	public void testListMessage() throws ParseException, IOException {
		List<String> messageIdList = new ArrayList<String>();
		MessageCreateClient createClient = new MessageCreateClient(IP, 8084);

		for (int i = 0; i < 100; i++) {
			createClient.create(sender, recipient, device, content, 3600);
			Assert.assertEquals(createClient.getResponseStatus(), 201);
			String messageId = EntityUtils.toString(createClient
			        .getResponseEntity());
			System.out.println(messageId);
			if (!messageIdList.contains(messageId)) {
				messageIdList.add(messageId);
			}
		}

		MessageCountClient count = new MessageCountClient(IP, 8084);
		count.list(device, "DELIVERING");
		Assert.assertEquals(count.getResponseStatus(), 200);

		long messageCount = Long.parseLong(EntityUtils.toString(count
		        .getResponseEntity()));

		System.out.println("Count message result = " + messageCount);

		MessageListClient list = new MessageListClient(IP, 8084);
		list.list(device, "DELIVERING", 20, 0);
		Assert.assertEquals(list.getResponseStatus(), 200);

		HttpEntity entity = list.getResponseEntity();
		byte[] result = EntityUtils.toByteArray(entity);

		BSONObject bsonList = Bytes.decode(result);
		Iterator<String> it = bsonList.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			BSONObject value = (BSONObject) bsonList.get(key);
			Assert.assertEquals(value.get("_id").toString(), key);
			System.out.println("id =[" + key + "] content = ["
			        + value.toString() + "]");
		}
	}

	@After
	public void tearDown() throws Exception {
	}

}