package com.babeeta.hudee.core.router.network;

import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.hudee.core.MessageHandler;
import com.babeeta.hudee.core.MessageRouting.Message;

/**
 *
 */
@Sharable
public class RequestHandler extends SimpleChannelUpstreamHandler {

	private static final Logger logger = LoggerFactory
	        .getLogger(RequestHandler.class);

	/**
	 * @return the count of channel in group
	 */
	public static int getChannelGroupSize() {
		return channelGroup.size();
	}

	private final MessageHandler messageHandler;

	private static ChannelGroup channelGroup;

	/**
	 * Constructor
	 * 
	 * @param messageHandler
	 *            message handler instance
	 */
	public RequestHandler(MessageHandler messageHandler) {
		super();
		this.messageHandler = messageHandler;
		channelGroup = new DefaultChannelGroup();
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
	        throws Exception {
		super.channelConnected(ctx, e);
		channelGroup.add(e.getChannel());
		logger.debug("[{}]Channel Connected.", e.getChannel().getId());
	}

	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
	        throws Exception {
		logger.debug("[{}]Channel Closed.", e.getChannel().getId());
		super.channelClosed(ctx, e);
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
	        throws Exception {
		if (!(e.getMessage() instanceof Message)) {
			logger.error("Unsupported message type: {}", e.getMessage()
			        .getClass());
		} else if ("heartbeat".equals(((Message) e.getMessage()).getUid())) {
			logger.info("[{}]Heartbeat from {}", e.getChannel().getId(), e
			        .getChannel().getRemoteAddress());
		} else {
			messageHandler.onMessage((Message) e.getMessage());
		}
	}

	@Override
	public void exceptionCaught(
	        ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		logger.info("[{}]exception Caught [{}]", e.getChannel().getId(),
		        e.getCause());
		e.getChannel().close();
	}

	/**
	 * @return the channel group
	 */
	public ChannelGroup getDefaultChannelGroup() {
		return channelGroup;
	}
}