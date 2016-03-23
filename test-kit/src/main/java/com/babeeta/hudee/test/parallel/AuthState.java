package com.babeeta.hudee.test.parallel;

import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.hudee.core.MessageRouting;
import com.babeeta.hudee.gateway.device.tunnel.TunnelData;

public class AuthState extends AbstractTunnelState
{
  public static final AtomicInteger ERROR_COUNTER = new AtomicInteger();
  private static final Logger logger = LoggerFactory.getLogger(AuthState.class);
  private final CredentialProvider credentialProvider;

  public AuthState()
  {
    this(new SessionCredentialProvider());
  }

  public AuthState(CredentialProvider credentialProvider) {
    this.credentialProvider = credentialProvider;
  }

  public TunnelState onBegin(Session session, MessageEvent e, ChannelHandlerContext ctx)
  {
	  if(session.channel.isConnected()){
		  logger.info("[{}]Sending auth.", session.channel.getId());
		  session.channel.write(new TunnelData(session.getTag(), 132, MessageRouting.Credential.newBuilder().setId(this.credentialProvider.getId(session)).setSecureKey(this.credentialProvider.getKey(session)).build()));
	  }
    return this;
  }

  @SuppressWarnings("rawtypes")
public TunnelState onMessageReceived(Session session, MessageEvent e, ChannelHandlerContext ctx)
    throws Exception
  {
    TunnelData result = (TunnelData)e.getMessage();

    if (!((MessageRouting.Response)result.obj).getStatus().equalsIgnoreCase("success")) {
      LoadRunner.getInstance().authFailedHandler(((MessageRouting.Response)result.obj).getStatus());
    }else{
    	logger.info("[{}] auth seccess.", session.channel.getId());
    }

    return this.nextState;
  }
}