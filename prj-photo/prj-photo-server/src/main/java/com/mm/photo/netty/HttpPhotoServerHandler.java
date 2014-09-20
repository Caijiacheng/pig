package com.mm.photo.netty;

import static io.netty.handler.codec.http.HttpHeaders.is100ContinueExpected;
import static io.netty.handler.codec.http.HttpHeaders.setContentLength;
import static io.netty.handler.codec.http.HttpHeaders.Names.CACHE_CONTROL;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Names.DATE;
import static io.netty.handler.codec.http.HttpHeaders.Names.EXPIRES;
import static io.netty.handler.codec.http.HttpHeaders.Names.IF_MODIFIED_SINCE;
import static io.netty.handler.codec.http.HttpHeaders.Names.LAST_MODIFIED;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_MODIFIED;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.stream.ChunkedStream;
import io.netty.util.CharsetUtil;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import javax.activation.MimetypesFileTypeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
import com.mm.photo.data.DefaultPhoto;
import com.mm.photo.data.DefaultUrl;
import com.mm.photo.data.IPhoto;
import com.mm.photo.data.IPhotoService;
import com.mm.photo.data.IUrl;


//FIXME: 是否处理并发上传相同文件的问题? 需要加个全局读写锁?

public class HttpPhotoServerHandler extends
		SimpleChannelInboundHandler<HttpObject> {

	private static final Logger LOG = LoggerFactory
			.getLogger(HttpPhotoServerHandler.class);

	private HttpRequest request;

	public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
	public static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
	public static final int HTTP_CACHE_SECONDS = 3600;

	private OutputStream os_photo;

	IPhotoService photo_service = new DefaultPhoto.Service();

	private static void sendNotModified(ChannelHandlerContext ctx) {
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
				NOT_MODIFIED);
		setDateHeader(response);

		// Close the connection as soon as the error message is sent.
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}

	private static void setDateHeader(FullHttpResponse response) {
		SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT,
				Locale.US);
		dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

		Calendar time = new GregorianCalendar();
		response.headers().set(DATE, dateFormatter.format(time.getTime()));
	}

	private static void setDateAndCacheHeaders(HttpResponse response) {
		SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT,
				Locale.US);
		dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

		// Date header
		Calendar time = new GregorianCalendar();
		response.headers().set(DATE, dateFormatter.format(time.getTime()));

		// Add cache headers
		time.add(Calendar.SECOND, HTTP_CACHE_SECONDS);
		response.headers().set(EXPIRES, dateFormatter.format(time.getTime()));
		response.headers().set(CACHE_CONTROL,
				"private, max-age=" + HTTP_CACHE_SECONDS);
		response.headers().set(LAST_MODIFIED, dateFormatter.format(new Date()));
	}

	/**
	 * Sets the content type header for the HTTP Response
	 * 
	 * @param response
	 *            HTTP response
	 * @param file
	 *            file to extract content type
	 */
	private static void setContentTypeHeader(HttpResponse response, String name) {
		MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
		response.headers().set(CONTENT_TYPE, mimeTypesMap.getContentType(name));
	}

	@Override
	protected void messageReceived(ChannelHandlerContext ctx, HttpObject msg)
			throws Exception {

		
		LOG.info("messageReceived: msg:{}, ctx:{}", msg, ctx.channel().remoteAddress());
		
		if (msg instanceof HttpRequest) {
			
			request = (HttpRequest) msg;

			IUrl url = new DefaultUrl(request.getUri());

			if (request.getMethod().equals(HttpMethod.GET)) {

				final IPhoto photo = photo_service.get(url);
				if (!photo.isExist()) {
					HttpResponse response = new DefaultFullHttpResponse(
							HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
					ctx.channel().writeAndFlush(response)
							.addListener(ChannelFutureListener.CLOSE);
					return;
				}

				String ifModifiedSince = request.headers().get(
						IF_MODIFIED_SINCE);
				if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
					sendNotModified(ctx);
					return;
				}

				HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
				setContentLength(response, photo.length());
				setContentTypeHeader(response, photo.uniqname());
				setDateAndCacheHeaders(response);
				ctx.write(response);

				ChannelFuture sendFileFuture = ctx.write(new ChunkedStream(
						photo.data(), 16 * 1024));

				sendFileFuture
						.addListener(new ChannelFutureListener() {
							@Override
							public void operationComplete(ChannelFuture future)
									throws Exception {
								LOG.info("[{}]Transfer complete.",
										photo.uniqname());								
							}
						});

				// Write the end marker
				ChannelFuture lastContentFuture = ctx
						.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
				lastContentFuture.addListener(ChannelFutureListener.CLOSE);
				return;
			} else if (request.getMethod().equals(HttpMethod.POST)) {
				if (photo_service.isExist(url)) {
					HttpResponse response = new DefaultFullHttpResponse(
							HttpVersion.HTTP_1_1,
							HttpResponseStatus.NOT_ACCEPTABLE);
					ctx.channel().writeAndFlush(response)
							.addListener(ChannelFutureListener.CLOSE);
					
					LOG.info("HttpResponseStatue Back:{}", response);
					return;
				}
				
	            if (is100ContinueExpected(request)) {
	                send100Continue(ctx);
	            }

				os_photo = photo_service.getPhotoOutput(url);
				return;

			} else {
				// writeResponse(ctx.channel());
				HttpResponse response = new DefaultFullHttpResponse(
						HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
				ctx.channel().writeAndFlush(response)
						.addListener(ChannelFutureListener.CLOSE);
				return;
			}

		}

		if (os_photo != null && msg instanceof HttpContent && 
				request.getMethod().equals(HttpMethod.POST)  )
		{
			HttpContent chunk = (HttpContent)msg;
			
			ByteStreams.copy(new ByteBufInputStream(chunk.content()), 
					Preconditions.checkNotNull(os_photo));
			if (chunk instanceof LastHttpContent)
			{
				os_photo.close();
				ctx.channel().writeAndFlush(new DefaultFullHttpResponse(HTTP_1_1, OK)).
				addListener(ChannelFutureListener.CLOSE);
			}
			return;
		}
	}


    private static void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, CONTINUE);
        ctx.writeAndFlush(response);
    }
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		cause.printStackTrace();
		if (ctx.channel().isActive()) {
			sendError(ctx, INTERNAL_SERVER_ERROR);
		}
	}

	private static void sendError(ChannelHandlerContext ctx,
			HttpResponseStatus status) {
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
				status, Unpooled.copiedBuffer("Failure: " + status.toString()
						+ "\r\n", CharsetUtil.UTF_8));
		response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");

		// Close the connection as soon as the error message is sent.
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}


}
