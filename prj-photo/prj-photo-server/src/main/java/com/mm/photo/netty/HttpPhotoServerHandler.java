package com.mm.photo.netty;

import static io.netty.handler.codec.http.HttpHeaders.setContentLength;
import static io.netty.handler.codec.http.HttpHeaders.Names.CACHE_CONTROL;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Names.DATE;
import static io.netty.handler.codec.http.HttpHeaders.Names.EXPIRES;
import static io.netty.handler.codec.http.HttpHeaders.Names.IF_MODIFIED_SINCE;
import static io.netty.handler.codec.http.HttpHeaders.Names.LAST_MODIFIED;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_MODIFIED;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelProgressiveFuture;
import io.netty.channel.ChannelProgressiveFutureListener;
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
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.stream.ChunkedStream;
import io.netty.util.CharsetUtil;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
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

	// private boolean readingChunks;

	private final StringBuilder responseContent = new StringBuilder();

	// private static final HttpDataFactory factory = new
	// DefaultHttpDataFactory(
	// DefaultHttpDataFactory.MINSIZE); // Disk
	// // if
	// private HttpPostRequestDecoder decoder;
	// static {
	// DiskFileUpload.deleteOnExitTemporaryFile = true; // should delete file
	// // on exit (in
	// // normal
	// // exit)
	// DiskFileUpload.baseDirectory = null; // system temp directory
	// DiskAttribute.deleteOnExitTemporaryFile = true; // should delete file on
	// // exit (in normal exit)
	// DiskAttribute.baseDirectory = null; // system temp directory
	// }

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

		if (msg instanceof HttpRequest) {
			request = (HttpRequest) msg;
//			responseContent.setLength(0);
//			responseContent.append("WELCOME TO THE WILD WILD WEB SERVER\r\n");
//			responseContent.append("===================================\r\n");
//
//			responseContent.append("VERSION: "
//					+ request.getProtocolVersion().text() + "\r\n");
//
//			responseContent.append("REQUEST_URI: " + request.getUri()
//					+ "\r\n\r\n");
//			responseContent.append("\r\n\r\n");
//
//			// new getMethod
//			for (Entry<String, String> entry : request.headers()) {
//				responseContent.append("HEADER: " + entry.getKey() + '='
//						+ entry.getValue() + "\r\n");
//			}
//			responseContent.append("\r\n\r\n");

			// new getMethod
//			Set<Cookie> cookies;
//			String value = request.headers().get(COOKIE);
//			if (value == null) {
//				cookies = Collections.emptySet();
//			} else {
//				cookies = CookieDecoder.decode(value);
//			}
//			for (Cookie cookie : cookies) {
//				responseContent.append("COOKIE: " + cookie.toString() + "\r\n");
//			}
//			responseContent.append("\r\n\r\n");

			QueryStringDecoder decoderQuery = new QueryStringDecoder(
					request.getUri());
			Map<String, List<String>> uriAttributes = decoderQuery.parameters();
			for (Entry<String, List<String>> attr : uriAttributes.entrySet()) {
				for (String attrVal : attr.getValue()) {
					responseContent.append("URI: " + attr.getKey() + '='
							+ attrVal + "\r\n");
				}
			}
			responseContent.append("\r\n\r\n");

			IUrl url = new DefaultUrl(request.getUri());

			if (request.getMethod().equals(HttpMethod.GET)) {
				// So stop here
				responseContent.append("\r\n\r\nEND OF GET CONTENT\r\n");

				final IPhoto photo = photo_service.get(url);
				if (!photo.isExist()) {
					HttpResponse response = new DefaultHttpResponse(
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
						.addListener(new ChannelProgressiveFutureListener() {
							@Override
							public void operationProgressed(
									ChannelProgressiveFuture future,
									long progress, long total) {
								if (total < 0) { // total unknown
									LOG.info("[{}]Transfer progress: {}",
											photo.uniqname(), progress);
								} else {
									LOG.info("[{}]Transfer progress: {}/{}",
											photo.uniqname(), progress, total);
								}
							}

							@Override
							public void operationComplete(
									ChannelProgressiveFuture future)
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
					HttpResponse response = new DefaultHttpResponse(
							HttpVersion.HTTP_1_1,
							HttpResponseStatus.NOT_ACCEPTABLE);
					ctx.channel().writeAndFlush(response)
							.addListener(ChannelFutureListener.CLOSE);
					return;
				}

				os_photo = photo_service.getPhotoOutput(url);
				return;
				

			} else {
				// writeResponse(ctx.channel());
				HttpResponse response = new DefaultHttpResponse(
						HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
				ctx.channel().writeAndFlush(response)
						.addListener(ChannelFutureListener.CLOSE);
				return;
			}

		}

		if (msg instanceof HttpContent && request.getMethod().equals(HttpMethod.POST))
		{
			HttpContent chunk = (HttpContent)msg;
			
			ByteStreams.copy(new ByteBufInputStream(chunk.content()), 
					Preconditions.checkNotNull(os_photo));
			if (chunk instanceof LastHttpContent)
			{
				os_photo.close();
				ctx.channel().writeAndFlush(new DefaultHttpResponse(HTTP_1_1, OK)).
				addListener(ChannelFutureListener.CLOSE);
			}
			return;
		}
		
		if (msg instanceof HttpContent && request.getMethod().equals(HttpMethod.GET))
		{
			ctx.channel().close();
			return;
		}
		
		// check if the decoder was constructed before
		// if not it handles the form get
		// if (decoder != null) {
		// if (msg instanceof HttpContent) {
		// // New chunk is received
		// HttpContent chunk = (HttpContent) msg;
		// try {
		// decoder.offer(chunk);
		// } catch (ErrorDataDecoderException e1) {
		// e1.printStackTrace();
		// responseContent.append(e1.getMessage());
		// writeResponse(ctx.channel());
		// ctx.channel().close();
		// return;
		// }
		// responseContent.append('o');
		// // example of reading chunk by chunk (minimize memory usage due
		// // to
		// // Factory)
		// readHttpDataChunkByChunk();
		// // example of reading only if at the end
		// if (chunk instanceof LastHttpContent) {
		// writeResponse(ctx.channel());
		// readingChunks = false;
		//
		// reset();
		// }
		// }
		// }
	}

	// private void readHttpDataChunkByChunk() {
	// try {
	// while (decoder.hasNext()) {
	// InterfaceHttpData data = decoder.next();
	// if (data != null) {
	// try {
	// // new value
	// writeHttpData(data);
	// } finally {
	// data.release();
	// }
	// }
	// }
	// } catch (EndOfDataDecoderException e1) {
	// // end
	// responseContent
	// .append("\r\n\r\nEND OF CONTENT CHUNK BY CHUNK\r\n\r\n");
	// }
	// }

	// private void writeHttpData(InterfaceHttpData data) {
	// if (data.getHttpDataType() == HttpDataType.Attribute) {
	// Attribute attribute = (Attribute) data;
	// String value;
	// try {
	// value = attribute.getValue();
	// } catch (IOException e1) {
	// // Error while reading data from File, only print name and error
	// e1.printStackTrace();
	// responseContent.append("\r\nBODY Attribute: "
	// + attribute.getHttpDataType().name() + ": "
	// + attribute.getName() + " Error while reading value: "
	// + e1.getMessage() + "\r\n");
	// return;
	// }
	// if (value.length() > 100) {
	// responseContent.append("\r\nBODY Attribute: "
	// + attribute.getHttpDataType().name() + ": "
	// + attribute.getName() + " data too long\r\n");
	// } else {
	// responseContent.append("\r\nBODY Attribute: "
	// + attribute.getHttpDataType().name() + ": "
	// + attribute.toString() + "\r\n");
	// }
	// } else {
	// responseContent.append("\r\nBODY FileUpload: "
	// + data.getHttpDataType().name() + ": " + data.toString()
	// + "\r\n");
	// if (data.getHttpDataType() == HttpDataType.FileUpload) {
	// FileUpload fileUpload = (FileUpload) data;
	// if (fileUpload.isCompleted()) {
	// if (fileUpload.length() < 10000) {
	// responseContent.append("\tContent of file\r\n");
	// try {
	// responseContent.append(fileUpload
	// .getString(fileUpload.getCharset()));
	// } catch (IOException e1) {
	// // do nothing for the example
	// e1.printStackTrace();
	// }
	// responseContent.append("\r\n");
	// } else {
	// responseContent
	// .append("\tFile too long to be printed out:"
	// + fileUpload.length() + "\r\n");
	// }
	// // fileUpload.isInMemory();// tells if the file is in Memory
	// // or on File
	// // fileUpload.renameTo(dest); // enable to move into another
	// // File dest
	// // decoder.removeFileUploadFromClean(fileUpload); //remove
	// // the File of to delete file
	// } else {
	// responseContent
	// .append("\tFile to be continued but should not!\r\n");
	// }
	// }
	// }
	// }

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

//	private void writeResponse(Channel channel) {
//		// Convert the response content to a ChannelBuffer.
//		ByteBuf buf = copiedBuffer(responseContent.toString(),
//				CharsetUtil.UTF_8);
//		responseContent.setLength(0);
//
//		// Decide whether to close the connection or not.
//		boolean close = request.headers().contains(CONNECTION,
//				HttpHeaders.Values.CLOSE, true)
//				|| request.getProtocolVersion().equals(HttpVersion.HTTP_1_0)
//				&& !request.headers().contains(CONNECTION,
//						HttpHeaders.Values.KEEP_ALIVE, true);
//
//		// Build the response object.
//		FullHttpResponse response = new DefaultFullHttpResponse(
//				HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);
//		response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
//
//		if (!close) {
//			// There's no need to add 'Content-Length' header
//			// if this is the last response.
//			response.headers().set(CONTENT_LENGTH, buf.readableBytes());
//		}
//
//		Set<Cookie> cookies;
//		String value = request.headers().get(COOKIE);
//		if (value == null) {
//			cookies = Collections.emptySet();
//		} else {
//			cookies = CookieDecoder.decode(value);
//		}
//		if (!cookies.isEmpty()) {
//			// Reset the cookies if necessary.
//			for (Cookie cookie : cookies) {
//				response.headers().add(SET_COOKIE,
//						ServerCookieEncoder.encode(cookie));
//			}
//		}
//		// Write the response.
//		ChannelFuture future = channel.writeAndFlush(response);
//		// Close the connection after the write operation is done if necessary.
//		if (close) {
//			future.addListener(ChannelFutureListener.CLOSE);
//		}
//	}

}
