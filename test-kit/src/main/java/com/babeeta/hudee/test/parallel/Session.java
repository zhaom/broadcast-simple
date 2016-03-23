package com.babeeta.hudee.test.parallel;

import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.netty.channel.Channel;

public class Session {
	  public String appId;
	  public String appKey;
	  public String did;
	  public String secureKey;
	  public String clientId;
	  private final AtomicInteger tagCounter = new AtomicInteger();
	  public Channel channel;
	  public long starttime = System.currentTimeMillis();
	  public long endtime = -1;
	  public int getTag()
	  {
	    return this.tagCounter.incrementAndGet();
	  }
}
