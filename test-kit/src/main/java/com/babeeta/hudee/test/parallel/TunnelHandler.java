package com.babeeta.hudee.test.parallel;

import java.nio.channels.ClosedChannelException;
import java.util.concurrent.atomic.AtomicLong;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.hudee.gateway.device.tunnel.TunnelData;


public class TunnelHandler extends SimpleChannelHandler
{
  Logger logger = LoggerFactory.getLogger(TunnelHandler.class.getName());
  private final ChannelGroup channelGroup;
  private final Session session;
  private TunnelState currentState = null;
  private final AtomicLong ERROR_COUNTER;
  
  private final AtomicLong TIME_ERROR_SUM;
  private final AtomicLong TIME_SUM;

  public Session getSession(){
	  return session;
  }
  public TunnelHandler(ChannelGroup defaultChannelGroup, TunnelState initialState, AtomicLong TUNNEL_ERROR_COUNTER, AtomicLong TIME_SUM, AtomicLong TIME_ERROR_SUM)
  {
    this.channelGroup = defaultChannelGroup;
    this.currentState = initialState;
    this.session = MemFileSessionFactory.getNewSession();
    this.TIME_ERROR_SUM = TIME_ERROR_SUM;
    this.TIME_SUM = TIME_SUM;
    this.ERROR_COUNTER = TUNNEL_ERROR_COUNTER;
  }

  public TunnelHandler(ChannelGroup defaultChannelGroup, TunnelState initialState, AtomicLong TUNNEL_ERROR_COUNTER, AtomicLong TIME_SUM, AtomicLong TIME_ERROR_SUM, Session session)
  {
    this.channelGroup = defaultChannelGroup;
    this.currentState = initialState;
    this.session = session;
    this.TIME_ERROR_SUM = TIME_ERROR_SUM;
    this.TIME_SUM = TIME_SUM;
    this.ERROR_COUNTER = TUNNEL_ERROR_COUNTER;
  }

  public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
    throws Exception
  {
    this.logger.info("[{}] [{}] channel closed", e.getChannel().getId());
    super.channelClosed(ctx, e);
    this.channelGroup.remove(e.getChannel());
    HeartbeatService.cancelPushSchedule(e.getChannel().getId());
    if(this.session.endtime<0){
    	this.session.endtime = System.currentTimeMillis();
    }
    this.TIME_SUM.addAndGet(this.session.endtime-this.session.starttime);
  }

  public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
    throws Exception
  {
    this.logger.info("[{}] channel connected", e.getChannel().getId());
    this.session.channel = e.getChannel();
    this.channelGroup.add(e.getChannel());
    TunnelState nextState = this.currentState.onBegin(this.session, null, ctx);

    onBegin(ctx, null, nextState);
  }


  public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e)
    throws Exception
  {
    this.logger.info("[{}] channel disconnected", e.getChannel().getId());
    this.channelGroup.remove(e.getChannel());
    HeartbeatService.cancelPushSchedule(e.getChannel().getId());
  }

  public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
    throws Exception
  {

    this.ERROR_COUNTER.incrementAndGet();
    this.session.endtime = System.currentTimeMillis();
    this.TIME_ERROR_SUM.addAndGet(System.currentTimeMillis() - this.session.starttime);
    try{
    e.getChannel().close();
    }catch(Exception ex){
    	
    }
  }

  @SuppressWarnings("rawtypes")
public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
    throws Exception
  {
    if (2 == ((TunnelData)e.getMessage()).cmd) {
    	this.logger.info("[{}] heartbeat received", e.getChannel().getId());
    	if(this.session.getTag()>10){
    		e.getChannel().close();
    	}else{
      //HeartbeatService.getDefaultInstance().onHeartbeat(e, this.session);
    	}
    } else if(3 ==((TunnelData)e.getMessage()).cmd){
    	this.logger.info("[{}] reserveinterval received", e.getChannel().getId());
    	currentState = new ListenState();
    	if(e.getChannel()!=null){
    		try{
    			e.getChannel().close();
    		}catch(Exception ex){
    			this.logger.info("Exception when close channel.exception:"+ex.getMessage());
    		}
    	}
    }else {
    	currentState = this.currentState.onMessageReceived(this.session, e, ctx);
    }
    onBegin(ctx, e, currentState);
  }

  private void onBegin(ChannelHandlerContext ctx, MessageEvent e, TunnelState nextState)
  {
    if (this.currentState != nextState) {
      TunnelState newState = null;
      while (nextState != (newState = nextState.onBegin(this.session, e, ctx)))
      {
        nextState = newState;
      }
      this.currentState = nextState;
    }
  }
}