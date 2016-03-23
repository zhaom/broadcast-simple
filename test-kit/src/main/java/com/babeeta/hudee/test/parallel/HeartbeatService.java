package com.babeeta.hudee.test.parallel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.babeeta.hudee.core.MessageRouting;
import com.babeeta.hudee.gateway.device.tunnel.TunnelData;

public class HeartbeatService
{
  private static final Logger logger = LoggerFactory.getLogger(HeartbeatService.class);

  private static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() + 1);

  private static final HeartbeatService DEFAULT_HEARTBEAT_SERVICE = new HeartbeatService();

  private static final Map<Integer, ScheduledFuture> futureMap = new ConcurrentHashMap();

  public static void cancelPushSchedule(Integer channelId) {
    if (futureMap.containsKey(channelId))
      ((ScheduledFuture)futureMap.get(channelId)).cancel(false);
  }

  public static HeartbeatService getDefaultInstance()
  {
    return DEFAULT_HEARTBEAT_SERVICE;
  }

  public static void shutdown() {
    scheduledExecutorService.shutdownNow();
  }

  public void addHeartBeat(final Session session, final int delay)
  {
    futureMap.put(session.channel.getId(), scheduledExecutorService.schedule(new Runnable()
    {
      public void run()
      {
        if (!session.channel.isConnected()) {
          logger.error("channel not connected.");
          return;
        }
        session.channel.write(new TunnelData(session.getTag(), 1, MessageRouting.Heartbeat.newBuilder().setLastDelay(delay).build())).addListener(new ChannelFutureListener()
        {
          public void operationComplete(ChannelFuture future)
            throws Exception
          {
            logger.debug("[{}]", session.channel.getId());
          }
        });
      }
    }
    , delay, TimeUnit.SECONDS));
  }

  public void onHeartbeat(MessageEvent e, Session session)
  {
    TunnelData data = (TunnelData)e.getMessage();

    logger.debug("[{}]Heartbeat:[{}]", e.getChannel().getId(), Integer.valueOf(((MessageRouting.HeartbeatResponse)data.obj).getDelay()));

    addHeartBeat(session, ((MessageRouting.HeartbeatResponse)data.obj).getDelay() > 280 ? 280 : ((MessageRouting.HeartbeatResponse)data.obj).getDelay());
  }
}