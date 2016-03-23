package com.babeeta.hudee.core.router.network;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.easymock.IMocksControl;
import org.jboss.netty.channel.ChannelFuture;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.babeeta.hudee.core.MessageFuture;
import com.babeeta.hudee.core.MessageRouting.Message;
import com.google.protobuf.ByteString;

@SuppressWarnings("javadoc")
public class MessageSenderImplTest {

	private MessageSenderImpl impl = null;
	private IMocksControl mocksControl = null;

	@Before
	public void setUp() throws Exception {
		mocksControl = createControl();
		impl = new MessageSenderImpl();
	}

	@Test
	public void testSend() throws TimeoutException, IOException {
		ChannelFuture channelFuture = mocksControl
		        .createMock(ChannelFuture.class);
		expect(channelFuture.isDone()).andReturn(false).once();
		expect(channelFuture.isDone()).andReturn(true).once();
		mocksControl.replay();
		Message msg = Message.newBuilder()
		        .setUid("UID")
		        .setTo("xyz@domain")
		        .setFrom("FROM")
		        .setDate(System.currentTimeMillis())
		        .setContent(ByteString.copyFrom(new byte[0]))
		        .build();

		MessageFuture messageFuture = impl.send(msg);
		assertNull(ReflectionTestUtils.getField(messageFuture, "channelFuture"));
	}

	@Test
	public void testSend连接建立失败() throws Exception {

		Message msg = Message.newBuilder()
		        .setUid("UID")
		        .setTo("xyz@domain")
		        .setFrom("FROM")
		        .setDate(System.currentTimeMillis())
		        .setContent(ByteString.copyFrom(new byte[0]))
		        .build();

		MessageFuture messageFuture = impl.send(msg);
		assertNull(messageFuture.getCause());
		assertNull(ReflectionTestUtils.getField(messageFuture, "channelFuture"));
	}
}
