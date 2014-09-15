/*
 * Copyright 2010, eCollege, Inc.  All rights reserved.
 */
package org.restexpress.pipeline;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.EventExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

/**
 * Provides a tiny DSL to define the pipeline features.
 * 
 * @author toddf
 * @since Aug 27, 2010
 */
public class PipelineBuilder extends ChannelInitializer<SocketChannel>
{
	// SECTION: CONSTANTS

	private static final int DEFAULT_MAX_CONTENT_LENGTH = 20480;


	// SECTION: INSTANCE VARIABLES

	private List<ChannelHandler> requestHandlers = new ArrayList<ChannelHandler>();
	private int maxContentLength = DEFAULT_MAX_CONTENT_LENGTH;
	private SSLContext sslContext = null;

	
	// SECTION: CONSTRUCTORS

	public PipelineBuilder()
	{
		super();
	}

	
	// SECTION: BUILDER METHODS
	
//	public PipelineBuilder setExecutionHandler(ExecutionHandler handler)
//	{
//		this.executionHandler = handler;
//		return this;
//	}
	
	
	public PipelineBuilder addRequestHandler(ChannelHandler handler)
	{
		if (!requestHandlers.contains(handler))
		{
			requestHandlers.add(handler);
		}

		return this;
	}
	
	/**
	 * Set the maximum length of the aggregated (chunked) content. If the length of the
	 * aggregated content exceeds this value, a TooLongFrameException will be raised during
	 * the request, which can be mapped in the RestExpress server to return a
	 * BadRequestException, if desired.
	 * 
	 * @param value
	 * @return this PipelineBuilder for method chaining.
	 */
	public PipelineBuilder setMaxContentLength(int value)
	{
		this.maxContentLength = value;
		return this;
	}

	public PipelineBuilder setSSLContext(SSLContext sslContext)
	{
		this.sslContext = sslContext;
		return this;
	}
	
	public SSLContext getSSLContext()
	{
		return sslContext;
	}

	private EventExecutor eventExecutor = new DefaultEventExecutor(Executors.newCachedThreadPool());

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();

		if (null != sslContext)
		{
			SSLEngine sslEngine = sslContext.createSSLEngine();
			sslEngine.setUseClientMode(false);
			SslHandler sslHandler = new SslHandler(sslEngine);
			pipeline.addLast("ssl", sslHandler);
		}
		
		pipeline.addLast("decoder", new HttpRequestDecoder());
		pipeline.addLast("inflater", new HttpContentDecompressor());
		pipeline.addLast("aggregator", new HttpObjectAggregator(maxContentLength));

		pipeline.addLast("logger", 
				new LoggingHandler(LogLevel.WARN));
		
		pipeline.addLast("encoder", new HttpResponseEncoder());
		pipeline.addLast("deflater", new HttpContentCompressor());
		pipeline.addLast("chunkWriter", new ChunkedWriteHandler());
		

//		pipeline.addLast("keepalive", new IdleStateHandler(0, 0, 1));
		for (ChannelHandler handler : requestHandlers)
		{
			//pipeline.addLast(handler.getClass().getSimpleName(), handler);
			pipeline.addLast(eventExecutor, handler);
		}

	}

	// SECTION: CHANNEL PIPELINE FACTORY

//	@Override
//	public ChannelPipeline getPipeline()
//	throws Exception
//	{
//		ChannelPipeline pipeline = Channels.pipeline();
//
//		if (null != sslContext)
//		{
//			SSLEngine sslEngine = sslContext.createSSLEngine();
//			sslEngine.setUseClientMode(false);
//			SslHandler sslHandler = new SslHandler(sslEngine);
//			pipeline.addLast("ssl", sslHandler);
//		}
//		
//		pipeline.addLast("decoder", new HttpRequestDecoder());
//		pipeline.addLast("aggregator", new HttpChunkAggregator(maxContentLength));
//		pipeline.addLast("encoder", new HttpResponseEncoder());
//		pipeline.addLast("chunkWriter", new ChunkedWriteHandler());
//		pipeline.addLast("inflater", new HttpContentDecompressor());
//		pipeline.addLast("deflater", new HttpContentCompressor());
//
//		if (executionHandler != null)
//		{
//			pipeline.addLast("executionHandler", executionHandler);
//		}
//
//		for (ChannelHandler handler : requestHandlers)
//		{
//			pipeline.addLast(handler.getClass().getSimpleName(), handler);
//		}
//
//		return pipeline;
//	}
}
