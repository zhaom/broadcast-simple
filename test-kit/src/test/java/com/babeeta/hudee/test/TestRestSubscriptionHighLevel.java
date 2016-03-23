package com.babeeta.hudee.test;

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;
import net.sf.json.JSONArray;

import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import com.babeeta.hudee.test.rest.dev.subscription.highlevel.CountResultClient;
import com.babeeta.hudee.test.rest.dev.subscription.highlevel.CountTagClient;
import com.babeeta.hudee.test.rest.dev.subscription.highlevel.CreateTaskClient;
import com.babeeta.hudee.test.rest.dev.subscription.highlevel.DeleteTaskClient;
import com.babeeta.hudee.test.rest.dev.subscription.highlevel.ListResultClient;

@SuppressWarnings("javadoc")
public class TestRestSubscriptionHighLevel {
	private static final String IP = "192.168.20.83";
	private static final String aid = "4fe83ea858b0e4b0387cb36eae54066b";

	@Test
	public final void testCountTag() throws NumberFormatException,
	        ParseException, IOException {
		CountTagClient counter = new CountTagClient(IP, 8083);

		counter.countTag(aid, "_ALL");
		Assert.assertEquals(counter.getResponseStatus(), 200);
		long countResult = Long.parseLong(EntityUtils.toString(counter
		        .getResponseEntity()));

		System.out.println("Count result = " + countResult);
	}

	@Test
	public final void testCreateTask() throws ParseException, IOException {
		CreateTaskClient task = new CreateTaskClient(IP, 8083);

		task.createTask(aid);
		Assert.assertEquals(task.getResponseStatus(), 200);
		String key = EntityUtils.toString(task.getResponseEntity());

		System.out.println("Key = " + key);

		long result = countResult(key);
		System.out.println("Count result = " + result);
		byte[] readContent = ListResultClient(key, 0);

		String listStr = new String(readContent);
		JSONArray obj = JSONArray.fromObject(listStr);

		@SuppressWarnings("unchecked")
		List<String> list = (List<String>) JSONArray.toCollection(obj,
		        String.class);

		for (String p : list) {
			System.out.println(p);
		}

		deleteTask(key);
	}

	private static void deleteTask(String key) {
		DeleteTaskClient deleter = new DeleteTaskClient(IP, 8083);
		deleter.deleteTask(key);
		Assert.assertEquals(deleter.getResponseStatus(), 200);
	}

	private static long countResult(String key) throws NumberFormatException,
	        ParseException, IOException {
		CountResultClient counter = new CountResultClient(IP, 8083);
		counter.count(key);
		Assert.assertEquals(counter.getResponseStatus(), 200);
		long result = Long.parseLong(EntityUtils.toString(counter
		        .getResponseEntity()));

		return result;
	}

	private static byte[] ListResultClient(String key, int index)
	        throws IOException {
		ListResultClient lister = new ListResultClient(
		        IP, 8083);

		lister.list(key, index, 20);

		Assert.assertEquals(lister.getResponseStatus(), 200);

		return EntityUtils.toByteArray(lister.getResponseEntity());
	}
}
