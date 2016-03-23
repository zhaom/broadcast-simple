package com.babeeta.hudee.test.parallel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;


public class DeviceClientFactory {
	 private final ExecutorService bossExecutorService = Executors.newCachedThreadPool();

	  private final ExecutorService workerExecutorService = Executors.newCachedThreadPool();

	  private final NioClientSocketChannelFactory nioClientSocketChannelFactory = new NioClientSocketChannelFactory(this.bossExecutorService, this.workerExecutorService);
	  private final ChannelGroup defaultChannelGroup;
	  private final AtomicLong TUNNEL_ERROR_COUNTER;
	  private final AtomicLong TUNNEL_ERROR_TIME_SUM;
	  private final AtomicLong TUNNEL_TIME_SUM;

/*	  public DeviceClientFactory(ChannelGroup defaultChannelGroup, AtomicLong TUNNEL_ERROR_COUNTER)
	  {
	    this.defaultChannelGroup = defaultChannelGroup;
	    this.TUNNEL_ERROR_COUNTER = TUNNEL_ERROR_COUNTER;
	  }*/
	  
	  public DeviceClientFactory(ChannelGroup defaultChannelGroup, AtomicLong TUNNEL_ERROR_COUNTER, AtomicLong TUNNEL_TIME_SUM, AtomicLong TUNNEL_ERROR_TIME_SUM )
	  {
	    this.defaultChannelGroup = defaultChannelGroup;
	    this.TUNNEL_ERROR_COUNTER = TUNNEL_ERROR_COUNTER;
	    this.TUNNEL_ERROR_TIME_SUM = TUNNEL_ERROR_TIME_SUM;
	    this.TUNNEL_TIME_SUM = TUNNEL_TIME_SUM;
	  }

	  public ClientBootstrap newClient() {
	    HeartbeatInitState hbs = new HeartbeatInitState();
	    AuthState aus = new AuthState();
	    ListenState ls = new ListenState();

	    hbs.setNextState(aus).setNextState(ls).setNextState(ls);

	    ClientBootstrap bootstrap = new ClientBootstrap(this.nioClientSocketChannelFactory);
	    
	    TunnelHandler handle = new TunnelHandler(defaultChannelGroup, (TunnelState)hbs, TUNNEL_ERROR_COUNTER, TUNNEL_TIME_SUM, TUNNEL_ERROR_TIME_SUM);

	    bootstrap.setPipelineFactory(new PipelineFactory(handle));
	    bootstrap.setOption("tcpNoDelay", Boolean.valueOf(true));
	    bootstrap.setOption("keepAlive", Boolean.valueOf(true));
	    bootstrap.setOption("connectTimeoutMillis", Integer.valueOf(30000));

	    return bootstrap;
	  }

}
