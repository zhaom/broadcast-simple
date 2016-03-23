package com.babeeta.hudee.test.parallel;

import java.rmi.UnexpectedException;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.hudee.core.MessageRouting;
import com.babeeta.hudee.gateway.device.tunnel.TunnelData;

public class HeartbeatInitState extends AbstractTunnelState
  implements TunnelState
{
  static final Logger logger = LoggerFactory.getLogger(MessageRouting.HeartbeatInit.class);

  public TunnelState getNextState()
  {
    return this.nextState;
  }

  public TunnelState onBegin(Session session, MessageEvent e, ChannelHandlerContext ctx)
  {
	  if(session.channel.isConnected()){
		  logger.info("[{}]Sending heartbeat init with pullinfo.", session.channel.getId());
		  session.channel.write(new TunnelData(session.getTag(), 0, MessageRouting.HeartbeatInit.newBuilder().setCause("none.").setLastTimeout(0).setLastException(MessageRouting.HeartbeatInit.HeartbeatException.none).setPullInfo(session.appId+"."+session.did).build()));
	  }
	  return this.nextState;
  }

  public TunnelState onMessageReceived(Session session, MessageEvent e, ChannelHandlerContext ctx)
    throws UnexpectedException
  {
    throw new UnsupportedOperationException();
  }
}