package org.czr.rest;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public class RestServerHandle extends SimpleChannelInboundHandler<FullHttpRequest> 
{

	static Logger LOG = LoggerFactory.getLogger(RestServerHandle.class);
	static ExecutorService DISPATCH = Executors.newCachedThreadPool();
	
	
	
	@Override
	protected void messageReceived(ChannelHandlerContext ctx,
			FullHttpRequest msg) throws Exception {
		LOG.debug("get http from: {}, url: {}, content: {}", 
				ctx.channel().remoteAddress().toString(), 
				msg.getUri(), msg.content().toString());
		//parse uri
		
		//mapping route
		
		//mapping action
		
		}
	
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    	LOG.warn("SessionException: {}", cause);
        ctx.close();
    }
    
}
