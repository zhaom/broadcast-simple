package com.babeeta.hudee.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;
import net.sf.json.JSONArray;

import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.babeeta.hudee.test.rest.dev.subscription.DeleteClient;
import com.babeeta.hudee.test.rest.dev.subscription.ListClient;
import com.babeeta.hudee.test.rest.dev.subscription.NewBindClient;
import com.babeeta.hudee.test.rest.dev.subscription.RebindClient;
import com.babeeta.hudee.test.rest.dev.subscription.UnbindClient;

@SuppressWarnings("javadoc")
public class TestRestSubscription {
	private static final String IP = "192.168.20.83";
	private String did = "50acb6f623f665ebf7675f738f0da4ef";
	private String aidList[] = { "449ff42e4d1143f0ad2a9f42b4bd96d2",
	        "f535579fe314455e94d635d353cdfc91",
	        "0c5e1fa25eec4eaa8be010ea983145d1",
	        "4274dfb51e684aad899ed6bfb101ab91" };

	@Before
	public void setUp() throws Exception {

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testDevSubscription() throws ParseException, IOException {
		Map<String, String> pair = new HashMap<String, String>();
		/*
		 * Test new bind
		 */
		NewBindClient newBinder = new NewBindClient(IP, 8083);

		for (String aid : aidList) {
			newBinder.newBind(aid, did);
			Assert.assertEquals(newBinder.getResponseStatus(), 200);
			String cid = EntityUtils.toString(newBinder.getResponseEntity());

			Assert.assertNotSame(cid.length(), 0);
			if (!pair.containsKey(aid)) {
				pair.put(aid, cid);
				System.out.println("Pair: " + aid + "." + cid);
			}
		}
		System.out.println("Test new bind okay!");
		/*
		 * Test rebind
		 */
		RebindClient rebinder = new RebindClient(IP, 8083);
		for (String aid : aidList) {
			rebinder.rebind(aid, pair.get(aid), did);
			Assert.assertEquals(rebinder.getResponseStatus(), 200);
			String cid = EntityUtils.toString(rebinder.getResponseEntity());
			Assert.assertEquals(cid, pair.get(aid));
		}
		System.out.println("Test rebind okay!");
		/*
		 * Test list
		 */
		ListClient lister = new ListClient(IP, 8083);

		lister.list(did);
		Assert.assertEquals(lister.getResponseStatus(), 200);
		String listStr = EntityUtils.toString(lister.getResponseEntity());
		JSONArray obj = JSONArray.fromObject(listStr);

		List<String> list = (List<String>) JSONArray.toCollection(obj,
		        String.class);

		for (String p : list) {
			String[] item = p.split(":");
			Assert.assertEquals(pair.containsKey(item[0]), true);
			Assert.assertEquals(pair.get(item[0]), item[1]);
		}

		System.out.println("Test list okay!");
		/*
		 * Test unbind
		 */
		UnbindClient unbindClient = new UnbindClient(IP, 8083);
		unbindClient.unbind(aidList[0], pair.get(aidList[0]), did);
		Assert.assertEquals(unbindClient.getResponseStatus(), 200);

		lister.list(did);
		Assert.assertEquals(lister.getResponseStatus(), 200);
		JSONArray obj2 = JSONArray.fromObject(EntityUtils.toString(lister
		        .getResponseEntity()));
		List<String> list2 = (List<String>) JSONArray.toCollection(obj2,
		        String.class);

		Assert.assertEquals(
		        list2.contains(aidList[0] + ":" + pair.get(aidList[0])), false);
		System.out.println("Test unbind okay!");

		/*
		 * Test delete
		 */
		DeleteClient deleter = new DeleteClient(IP, 8083);
		deleter.deleteByDeviceId(did);
		Assert.assertEquals(deleter.getResponseStatus(), 200);
		long deleted = Long.parseLong(EntityUtils.toString(deleter
		        .getResponseEntity()));
		Assert.assertEquals(deleted, aidList.length - 1);
		System.out.println("Test delete okay!");
	}

	@After
	public void tearDown() throws Exception {
	}

}