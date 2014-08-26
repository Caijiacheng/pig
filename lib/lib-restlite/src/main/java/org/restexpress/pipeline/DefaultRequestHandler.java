/*
 * Copyright 2009, Strategic Gains, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.restexpress.pipeline;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.restexpress.ContentType;
import org.restexpress.Request;
import org.restexpress.Response;
import org.restexpress.exception.DefaultExceptionMapper;
import org.restexpress.exception.ExceptionMapping;
import org.restexpress.exception.ExceptionUtils;
import org.restexpress.exception.ServiceException;
import org.restexpress.response.HttpResponseWriter;
import org.restexpress.route.Action;
import org.restexpress.route.RouteResolver;
import org.restexpress.serialization.SerializationProvider;
import org.restexpress.serialization.SerializationSettings;
import org.restexpress.util.HttpSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author toddf
 * @since Nov 13, 2009
 */

@Sharable
public class DefaultRequestHandler
extends SimpleChannelInboundHandler<FullHttpRequest>
//extends SimpleChannelUpstreamHandler
{
	// SECTION: INSTANCE VARIABLES

	private RouteResolver routeResolver;
	private SerializationProvider serializationProvider;
	private HttpResponseWriter responseWriter;
	private List<Preprocessor> preprocessors = new ArrayList<Preprocessor>();
	private List<Postprocessor> postprocessors = new ArrayList<Postprocessor>();
	private List<Postprocessor> finallyProcessors = new ArrayList<Postprocessor>();
	private ExceptionMapping exceptionMap = new DefaultExceptionMapper();
	private List<MessageObserver> messageObservers = new ArrayList<MessageObserver>();
	private boolean shouldEnforceHttpSpec = true;


	// SECTION: CONSTRUCTORS

	public DefaultRequestHandler(RouteResolver routeResolver, SerializationProvider serializationProvider,
		HttpResponseWriter responseWriter, boolean enforceHttpSpec)
	{
		super();
		this.routeResolver = routeResolver;
		this.serializationProvider = serializationProvider;
		setResponseWriter(responseWriter);
		this.shouldEnforceHttpSpec = enforceHttpSpec;
	}


	// SECTION: MUTATORS
	
	public void addMessageObserver(MessageObserver... observers)
	{
		for (MessageObserver observer : observers)
		{
			if (!messageObservers.contains(observer))
			{
				messageObservers.add(observer);
			}
		}
	}

	public <T extends Throwable, U extends ServiceException> DefaultRequestHandler mapException(Class<T> from, Class<U> to)
	{
		exceptionMap.map(from, to);
		return this;
	}
	
	public DefaultRequestHandler setExceptionMap(ExceptionMapping map)
	{
		this.exceptionMap = map;
		return this;
	}

	public HttpResponseWriter getResponseWriter()
	{
		return this.responseWriter;
	}

	public void setResponseWriter(HttpResponseWriter writer)
	{
		this.responseWriter = writer;
	}


	// SECTION: SIMPLE-CHANNEL-UPSTREAM-HANDLER
	
//	static private ExecutorService s_workerExecService = Executors.newCachedThreadPool(new ThreadFactory() {
//		
//		AtomicInteger _workThreadID = new AtomicInteger(0);
//		private String _prefix = "Work-Request";
//		
//		class WorkerThread extends Thread
//		{
//			public WorkerThread(Runnable r)
//			{
//				super(r, _prefix + "-" + String.valueOf(_workThreadID.incrementAndGet()));
//			}
//		}
//		
//		@Override
//		public Thread newThread(Runnable r) {
//			return new WorkerThread(r);
//		}
//	});
	

	@Override
	public void messageReceived(ChannelHandlerContext ctx, FullHttpRequest request)
	throws Exception
	{
		
		MessageContext context = createInitialContext(ctx, request);

		try
		{
			notifyReceived(context);
			resolveRoute(context);
			resolveResponseProcessor(context);
			invokePreprocessors(preprocessors, context.getRequest());
			Object result = context.getAction().invoke(context.getRequest(), context.getResponse());

			if (result != null)
			{
				context.getResponse().setBody(result);
			}
	
			invokePostprocessors(postprocessors, context.getRequest(), context.getResponse());
			serializeResponse(context, false);
			enforceHttpSpecification(context);
			invokeFinallyProcessors(finallyProcessors, context.getRequest(), context.getResponse());
			writeResponse(ctx, context);
			notifySuccess(context);
		}
		catch(Throwable t)
		{
			
//			LOG.error("MessageReceive Exception: {}", t);
			handleRestExpressException(ctx, t);
		}
		finally
		{
			notifyComplete(context);
		}
	}

	static Logger LOG = LoggerFactory.getLogger(DefaultRequestHandler.class);
	
	private void resolveResponseProcessor(MessageContext context)
    {
		SerializationSettings s = serializationProvider.resolveResponse(context.getRequest(), context.getResponse(), false);
		context.setSerializationSettings(s);
    }

	/**
     * @param context
     */
    private void enforceHttpSpecification(MessageContext context)
    {
    	if (shouldEnforceHttpSpec)
    	{
    		HttpSpecification.enforce(context.getResponse());
    	}
    }

	private void handleRestExpressException(ChannelHandlerContext ctx, Throwable cause)
	throws Exception
	{
		MessageContext context =  ctx.attr(MSG_CTX).get();
		Throwable rootCause = mapServiceException(cause);
		
		if (rootCause != null) // was/is a ServiceException
		{
			
			context.setHttpStatus(((ServiceException) rootCause).getHttpStatus());
			
			if (ServiceException.class.isAssignableFrom(rootCause.getClass()))
			{
				((ServiceException) rootCause).augmentResponse(context.getResponse());
			}
		}
		else
		{
			
			rootCause = ExceptionUtils.findRootCause(cause);
			context.setHttpStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
			
//			context.setHttpStatus(HttpResponseStatus.NOT_FOUND);
		}

		context.setException(rootCause);
		notifyException(context);
		serializeResponse(context, true);
		invokeFinallyProcessors(finallyProcessors, context.getRequest(), context.getResponse());
		writeResponse(ctx, context);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable event)
	throws Exception
	{
		
		//LOG.error("exceptionCaught: handler: exceptionCaught: ", event);
		try
		{
			MessageContext messageContext = ctx.attr(MSG_CTX).get();
			
			if (messageContext != null)
			{
				messageContext.setException(event.getCause());
				notifyException(messageContext);
			}
		}
		catch(Throwable t)
		{
//			System.err.print("DefaultRequestHandler.exceptionCaught() threw an exception.");
//			t.printStackTrace();
			LOG.error("exceptionCaught: ", t);
		}
		finally
		{
//			event.getChannel().close();
//			LOG.error("exceptionCaught: flush");
//			LOG.error("exceptionCaught: close?");
			ctx.close();
		}
	}

	static final AttributeKey<MessageContext> MSG_CTX = AttributeKey.valueOf("MESSAGE_CONTEXT"); 
	
	private MessageContext createInitialContext(ChannelHandlerContext ctx, FullHttpRequest r)
	{
		Request request = createRequest(r, ctx);
		Response response = createResponse();
		MessageContext context = new MessageContext(request, response);
		ctx.attr(MSG_CTX).set(context);
		return context;
	}

	private void resolveRoute(MessageContext context)
    {
	    Action action = routeResolver.resolve(context.getRequest());
		context.setAction(action);
    }


    /**
     * @param request
     * @param response
     */
    private void notifyReceived(MessageContext context)
    {
    	for (MessageObserver observer : messageObservers)
    	{
    		observer.onReceived(context.getRequest(), context.getResponse());
    	}
    }

	/**
     * @param request
     * @param response
     */
    private void notifyComplete(MessageContext context)
    {
    	for (MessageObserver observer : messageObservers)
    	{
    		observer.onComplete(context.getRequest(), context.getResponse());
    	}
    }

	// SECTION: UTILITY -- PRIVATE

	/**
     * @param exception
     * @param request
     * @param response
     */
    private void notifyException(MessageContext context)
    {
    	Throwable exception = context.getException();

    	for (MessageObserver observer : messageObservers)
    	{
    		observer.onException(exception, context.getRequest(), context.getResponse());
    	}
    }

	/**
     * @param request
     * @param response
     */
    private void notifySuccess(MessageContext context)
    {
    	for (MessageObserver observer : messageObservers)
    	{
    		observer.onSuccess(context.getRequest(), context.getResponse());
    	}
    }
	
	public void addPreprocessor(Preprocessor handler)
	{
		if (!preprocessors.contains(handler))
		{
			preprocessors.add(handler);
		}
	}

	public void addPostprocessor(Postprocessor handler)
	{
		if (!postprocessors.contains(handler))
		{
			postprocessors.add(handler);
		}
	}

	public void addFinallyProcessor(Postprocessor handler)
	{
		if (!finallyProcessors.contains(handler))
		{
			finallyProcessors.add(handler);
		}
	}

    private void invokePreprocessors(List<Preprocessor> processors, Request request)
    {
		for (Preprocessor handler : processors)
		{
			handler.process(request);
		}

		request.getBody().resetReaderIndex();
    }

    private void invokePostprocessors(List<Postprocessor> processors, Request request, Response response)
    {
		for (Postprocessor handler : processors)
		{
			handler.process(request, response);
		}
    }

    private void invokeFinallyProcessors(List<Postprocessor> processors, Request request, Response response)
    {
		for (Postprocessor handler : processors)
		{
			try
			{
				handler.process(request, response);
			}
			catch(Throwable t)
			{
				t.printStackTrace(System.err);
			}
		}
    }

	/**
	 * Uses the exceptionMap to map a Throwable to a ServiceException, if possible.
	 * 
	 * @param cause
	 * @return Either a ServiceException or the root cause of the exception.
	 */
	private Throwable mapServiceException(Throwable cause)
    {
		if (ServiceException.isAssignableFrom(cause))
		{
			return cause;
		}
			
		return exceptionMap.getExceptionFor(cause);
    }

	/**
     * @param request
     * @return
     */
    private Request createRequest(FullHttpRequest request, ChannelHandlerContext context)
    {
    	return new Request(request, routeResolver, serializationProvider);
    }

	/**
     * @param request
     * @return
     */
    private Response createResponse()
    {
    	return new Response();
    }

    /**
     * @param message
     * @return
     */
    private void writeResponse(ChannelHandlerContext ctx, MessageContext context)
    {
    	getResponseWriter().write(ctx, context.getRequest(), context.getResponse());
    }

	private void serializeResponse(MessageContext context, boolean force)
	{
		Response response = context.getResponse();

		if (HttpSpecification.isContentTypeAllowed(response))
		{
			SerializationSettings settings = null;

			if (response.hasSerializationSettings())
			{
				settings = response.getSerializationSettings();
			}
			else if (force)
			{
				settings = serializationProvider.resolveResponse(context.getRequest(), response, force);
			}

			if (settings != null)
			{
				if (response.isSerialized())
				{
					String serialized = settings.serialize(response);
					
					if (serialized != null)
					{
						response.setBody(serialized);
	
						if (!response.hasHeader(HttpHeaders.Names.CONTENT_TYPE.toString()))
						{
							response.setContentType(settings.getMediaType());
						}
					}
				}
			}

			if (!response.hasHeader(HttpHeaders.Names.CONTENT_TYPE.toString()))
			{
				response.setContentType(ContentType.TEXT_PLAIN);
			}
		}
	}
	
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
    		throws Exception {
    	
//    	LOG.error("UserEventTriggered: {}", evt);
    	
    	if (evt instanceof IdleStateEvent)
    	{
    		IdleStateEvent e = (IdleStateEvent)evt;
    		if (Objects.equals(e.state(), IdleState.ALL_IDLE))
    		{
    			LOG.info("ChannelHandlerContext Timeout to Close: {}", ctx.channel().remoteAddress());
    			ctx.flush();
    			ctx.close();
    		}
    	}else
    	{
    		super.userEventTriggered(ctx, evt);
    	}
    	
    }
	
}
