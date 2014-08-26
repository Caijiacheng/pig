/*
 * Copyright 2010, eCollege, Inc.  All rights reserved.
 */
package org.restexpress.response;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;

import org.restexpress.ContentType;
import org.restexpress.Request;
import org.restexpress.Response;
import org.restexpress.util.HttpSpecification;

/**
 * @author toddf
 * @since Aug 26, 2010
 */
public class DefaultHttpResponseWriter
implements HttpResponseWriter
{
	@Override
	public void write(ChannelHandlerContext ctx, Request request, Response response)
	{
		FullHttpResponse httpResponse = null;//new DefaultFullHttpResponse(request.getHttpVersion(), response.getResponseStatus());

		if (response.hasBody() && HttpSpecification.isContentAllowed(response))
		{
			// If the response body already contains a ChannelBuffer, use it.
			if (ByteBuf.class.isAssignableFrom(response.getBody().getClass()))
			{
				//httpResponse.setContent(response.getBody());
				httpResponse = new DefaultFullHttpResponse(request.getHttpVersion(), response.getResponseStatus(), (ByteBuf)response.getBody());
			}
			else // response body is assumed to be a string (e.g. JSON or XML).
			{
				ByteBuf bb = Unpooled.copiedBuffer(response.getBody().toString(), ContentType.CHARSET);
				httpResponse = new DefaultFullHttpResponse(request.getHttpVersion(), response.getResponseStatus(), bb);
			}
		}else
		{
			httpResponse = new DefaultFullHttpResponse(request.getHttpVersion(), response.getResponseStatus());
		}
		
		addHeaders(response, httpResponse);		
		
		if (request.isKeepAlive())
	  	{
	  		// Add 'Content-Length' header only for a keep-alive connection.
			if (HttpSpecification.isContentLengthAllowed(response))
	  		{
				httpResponse.headers().add(HttpHeaders.Names.CONTENT_LENGTH.toString(), String.valueOf(httpResponse.content().readableBytes()));
	  		}

			// Support "Connection: Keep-Alive" for HTTP 1.0 requests.
			if (request.isHttpVersion1_0()) 
			{
				httpResponse.headers().add(HttpHeaders.Names.CONNECTION.toString(), "Keep-Alive");
			}

	  		ctx.channel().write(httpResponse).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
	  	}
		else
		{
			httpResponse.headers().add(HttpHeaders.Names.CONNECTION.toString(), "close");

			// Close the connection as soon as the message is sent.
			ctx.channel().write(httpResponse).addListener(ChannelFutureListener.CLOSE);
		}
	}

	/**
     * @param response
     * @param httpResponse
     */
    private void addHeaders(Response response, HttpResponse httpResponse)
    {
    	for (String name : response.getHeaderNames())
    	{
    		for (String value : response.getHeaders(name))
    		{
    			httpResponse.headers().add(name, value);
    		}
    	}
    }
}
