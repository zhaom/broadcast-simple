package com.babeeta.hudee.gateway.device.tunnel;

import java.util.concurrent.Executor;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.DefaultChannelPipeline;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;

/**
 * Tunnel pipeline factory which do encoding and decoding.
 * 
 */
public class TunnelPipelineFactory implements ChannelPipelineFactory {

	private final ChannelGroup channelGroup;
	private final TunnelLetFactory tunnelLetFactory;
	private final Timer timer;
	private ExecutionHandler executionHandler;

	/**
	 * Constructor
	 * 
	 * @param channelGroup
	 *            channel pool
	 * @param tunnelLetFactory
	 *            tunnel let factory implements
	 * @param pipelineExecutor
	 *            executor of pipeline
	 */
	public TunnelPipelineFactory(ChannelGroup channelGroup,
	        TunnelLetFactory tunnelLetFactory, Executor pipelineExecutor) {
		super();
		this.channelGroup = channelGroup;
		this.tunnelLetFactory = tunnelLetFactory;
		this.timer = new HashedWheelTimer();

		executionHandler = new ExecutionHandler(pipelineExecutor);
	}

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = new DefaultChannelPipeline();
		pipeline.addLast("frameDecoder",
		        new LengthFieldBasedFrameDecoder(10240, 8, 4, 0, 0));
		// TODO : adjust idle connection limit here
		int readerIdleTimeSeconds = 310;
		// writer idle timer used in PULL mode.
		int writerIdleTimeSeconds = 60;
		pipeline.addLast("idlestate", new IdleStateHandler(timer,
		        readerIdleTimeSeconds, writerIdleTimeSeconds, 0));
		pipeline.addLast("executor", executionHandler);
		pipeline.addLast("requestDecoder", new TunnelDataEncoder());
		pipeline.addLast("requestEncoder", new TunnelDataDecoder());
		pipeline.addLast("requstHandler",
		        new TunnelHandler(channelGroup, tunnelLetFactory));
		return pipeline;
	}
}
