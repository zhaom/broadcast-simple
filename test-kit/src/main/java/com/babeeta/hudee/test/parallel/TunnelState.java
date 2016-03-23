package com.babeeta.hudee.test.parallel;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public interface TunnelState {
	  public abstract TunnelState onBegin(Session paramSession, MessageEvent paramMessageEvent, ChannelHandlerContext paramChannelHandlerContext);

	  public abstract TunnelState onMessageReceived(Session paramSession, MessageEvent paramMessageEvent, ChannelHandlerContext paramChannelHandlerContext)
	    throws Exception;
}
