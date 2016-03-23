package com.babeeta.hudee.test.parallel;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.DefaultChannelPipeline;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;



public class PipelineFactory implements ChannelPipelineFactory {
	private com.babeeta.hudee.test.parallel.TunnelHandler handle;
	
	
	public PipelineFactory(com.babeeta.hudee.test.parallel.TunnelHandler handle2) {
		this.handle = handle2;
	}

	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = new DefaultChannelPipeline();
		pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(
				10240, 8, 4, 0, 0));
		pipeline.addLast("requestEncoder", new TunnelDataDecoder());
		pipeline.addLast("requestDecoder", new TunnelDataEncoder());

		

		pipeline.addLast("handler", handle);
		return pipeline;
	}

}