package com.babeeta.hudee.test.parallel;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

import com.babeeta.hudee.gateway.device.tunnel.TunnelData;
import com.google.protobuf.MessageLite;

/**
 * Encode object extends from message lite and add header for identify and
 * synchronous
 */
public class TunnelDataEncoder extends OneToOneEncoder {

	private static final int HEADER_SIZE = 4 * 4 * 4;

	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel channel,
	        Object msg) throws Exception {
		if (!(msg instanceof TunnelData)) {
			return msg;
		}

		@SuppressWarnings("unchecked")
		TunnelData<MessageLite> tunnelData = (TunnelData<MessageLite>) msg;

		if (tunnelData.cmd < 0) {
			throw new IllegalArgumentException("Unkown command: "
			        + tunnelData.obj.getClass().getName());
		}

		int len = tunnelData.obj.getSerializedSize();

		ChannelBuffer buf = ChannelBuffers.directBuffer(HEADER_SIZE + len);

		buf.writeInt(tunnelData.tag);
		buf.writeInt(tunnelData.cmd);
		buf.writeInt(len);
		ChannelBufferOutputStream out = new ChannelBufferOutputStream(buf);
		tunnelData.obj.writeTo(out);
		out.close();
		return buf;
	}

}
