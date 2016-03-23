package com.babeeta.hudee.test.parallel;

import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.hudee.core.MessageRouting;
import com.babeeta.hudee.gateway.device.tunnel.TunnelData;

public class ListenState extends AbstractTunnelState
{
  private static final Logger logger = LoggerFactory.getLogger(ListenState.class);

  public static final AtomicInteger MESSAGE_RECEIVED_COUNTER = new AtomicInteger(0);
  private final boolean push;

  public ListenState()
  {
    this.push = true;
  }

  public ListenState(boolean push)
  {
    this.push = push;
  }

  public TunnelState onBegin(Session session, MessageEvent e, ChannelHandlerContext ctx)
  {
    if (this.push);
    return this;
  }

  public TunnelState onMessageReceived(Session session, MessageEvent e, ChannelHandlerContext ctx)
    throws Exception
  {
    TunnelData tunnelData = (TunnelData)e.getMessage();

    if (135 == tunnelData.cmd) {
      String status = ((MessageRouting.Response)tunnelData.obj).getStatus();
      if ("success".equalsIgnoreCase(status))
        logger.info("Ack response:{} - {}", Integer.valueOf(tunnelData.tag), status);
      else {
        logger.error("Ack response:{} - {}", Integer.valueOf(tunnelData.tag), status);
      }
    }
    else
    {
      TunnelData msg = (TunnelData)e.getMessage();
      MESSAGE_RECEIVED_COUNTER.incrementAndGet();
      logger.info("[{}] message received", new Object[] { ((MessageRouting.Message)msg.obj).getUid() });
      if(session.channel.isConnected()){
      session.channel.write(new TunnelData(session.getTag(), 130, MessageRouting.Acknowledgement.newBuilder().setUid(((MessageRouting.Message)msg.obj).getUid()).build()));
      }
    }

    return this;
  }
}
