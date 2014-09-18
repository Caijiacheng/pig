package com.mm.photo.netty;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Names.COOKIE;
import static io.netty.handler.codec.http.HttpHeaders.Names.SET_COOKIE;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.CookieDecoder;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.ServerCookieEncoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.DiskAttribute;
import io.netty.handler.codec.http.multipart.DiskFileUpload;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.EndOfDataDecoderException;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.ErrorDataDecoderException;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;
import io.netty.util.CharsetUtil;

public class HttpPhotoServerHandler extends
		SimpleChannelInboundHandler<HttpObject> {

	private static final Logger LOG = LoggerFactory
			.getLogger(HttpPhotoServerHandler.class);

	private HttpRequest request;

	private boolean readingChunks;

	private final StringBuilder responseContent = new StringBuilder();

	private static final HttpDataFactory factory = new DefaultHttpDataFactory(
			DefaultHttpDataFactory.MINSIZE); // Disk
												// if
	private HttpPostRequestDecoder decoder;
	static {
		DiskFileUpload.deleteOnExitTemporaryFile = true; // should delete file
															// on exit (in
															// normal
															// exit)
		DiskFileUpload.baseDirectory = null; // system temp directory
		DiskAttribute.deleteOnExitTemporaryFile = true; // should delete file on
														// exit (in normal exit)
		DiskAttribute.baseDirectory = null; // system temp directory
	}

	@Override
	protected void messageReceived(ChannelHandlerContext ctx, HttpObject msg)
			throws Exception {

		if (msg instanceof HttpRequest) {
			HttpRequest request = this.request = (HttpRequest) msg;
			URI uri = new URI(request.getUri());
			if (!uri.getPath().startsWith("/form")) {
				// Write Menu
				// writeMenu(ctx);
				return;
			}
			responseContent.setLength(0);
			responseContent.append("WELCOME TO THE WILD WILD WEB SERVER\r\n");
			responseContent.append("===================================\r\n");

			responseContent.append("VERSION: "
					+ request.getProtocolVersion().text() + "\r\n");

			responseContent.append("REQUEST_URI: " + request.getUri()
					+ "\r\n\r\n");
			responseContent.append("\r\n\r\n");

			// new getMethod
			for (Entry<String, String> entry : request.headers()) {
				responseContent.append("HEADER: " + entry.getKey() + '='
						+ entry.getValue() + "\r\n");
			}
			responseContent.append("\r\n\r\n");

			// new getMethod
			Set<Cookie> cookies;
			String value = request.headers().get(COOKIE);
			if (value == null) {
				cookies = Collections.emptySet();
			} else {
				cookies = CookieDecoder.decode(value);
			}
			for (Cookie cookie : cookies) {
				responseContent.append("COOKIE: " + cookie.toString() + "\r\n");
			}
			responseContent.append("\r\n\r\n");

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

			// if GET Method: should not try to create a HttpPostRequestDecoder
			if (request.getMethod().equals(HttpMethod.GET)) {
				// GET Method: should not try to create a HttpPostRequestDecoder
				// So stop here
				responseContent.append("\r\n\r\nEND OF GET CONTENT\r\n");
				writeResponse(ctx.channel());
				return;
			}
			try {
				decoder = new HttpPostRequestDecoder(factory, request);
			} catch (ErrorDataDecoderException e1) {
				e1.printStackTrace();
				responseContent.append(e1.getMessage());
				writeResponse(ctx.channel());
				ctx.channel().close();
				return;
			}

			readingChunks = HttpHeaders.isTransferEncodingChunked(request);
			responseContent.append("Is Chunked: " + readingChunks + "\r\n");
			responseContent.append("IsMultipart: " + decoder.isMultipart()
					+ "\r\n");
			if (readingChunks) {
				// Chunk version
				responseContent.append("Chunks: ");
				readingChunks = true;
			}
		}

		// check if the decoder was constructed before
		// if not it handles the form get
		if (decoder != null) {
			if (msg instanceof HttpContent) {
				// New chunk is received
				HttpContent chunk = (HttpContent) msg;
				try {
					decoder.offer(chunk);
				} catch (ErrorDataDecoderException e1) {
					e1.printStackTrace();
					responseContent.append(e1.getMessage());
					writeResponse(ctx.channel());
					ctx.channel().close();
					return;
				}
				responseContent.append('o');
				// example of reading chunk by chunk (minimize memory usage due
				// to
				// Factory)
				readHttpDataChunkByChunk();
				// example of reading only if at the end
				if (chunk instanceof LastHttpContent) {
					writeResponse(ctx.channel());
					readingChunks = false;

					reset();
				}
			}
		}
	}

	
    private void reset() {
        request = null;

        // destroy the decoder to release all resources
        decoder.destroy();
        decoder = null;
    }
	
	private void readHttpDataChunkByChunk() {
		try {
			while (decoder.hasNext()) {
				InterfaceHttpData data = decoder.next();
				if (data != null) {
					try {
						// new value
						writeHttpData(data);
					} finally {
						data.release();
					}
				}
			}
		} catch (EndOfDataDecoderException e1) {
			// end
			responseContent
					.append("\r\n\r\nEND OF CONTENT CHUNK BY CHUNK\r\n\r\n");
		}
	}

	private void writeHttpData(InterfaceHttpData data) {
		if (data.getHttpDataType() == HttpDataType.Attribute) {
			Attribute attribute = (Attribute) data;
			String value;
			try {
				value = attribute.getValue();
			} catch (IOException e1) {
				// Error while reading data from File, only print name and error
				e1.printStackTrace();
				responseContent.append("\r\nBODY Attribute: "
						+ attribute.getHttpDataType().name() + ": "
						+ attribute.getName() + " Error while reading value: "
						+ e1.getMessage() + "\r\n");
				return;
			}
			if (value.length() > 100) {
				responseContent.append("\r\nBODY Attribute: "
						+ attribute.getHttpDataType().name() + ": "
						+ attribute.getName() + " data too long\r\n");
			} else {
				responseContent.append("\r\nBODY Attribute: "
						+ attribute.getHttpDataType().name() + ": "
						+ attribute.toString() + "\r\n");
			}
		} else {
			responseContent.append("\r\nBODY FileUpload: "
					+ data.getHttpDataType().name() + ": " + data.toString()
					+ "\r\n");
			if (data.getHttpDataType() == HttpDataType.FileUpload) {
				FileUpload fileUpload = (FileUpload) data;
				if (fileUpload.isCompleted()) {
					if (fileUpload.length() < 10000) {
						responseContent.append("\tContent of file\r\n");
						try {
							responseContent.append(fileUpload
									.getString(fileUpload.getCharset()));
						} catch (IOException e1) {
							// do nothing for the example
							e1.printStackTrace();
						}
						responseContent.append("\r\n");
					} else {
						responseContent
								.append("\tFile too long to be printed out:"
										+ fileUpload.length() + "\r\n");
					}
					// fileUpload.isInMemory();// tells if the file is in Memory
					// or on File
					// fileUpload.renameTo(dest); // enable to move into another
					// File dest
					// decoder.removeFileUploadFromClean(fileUpload); //remove
					// the File of to delete file
				} else {
					responseContent
							.append("\tFile to be continued but should not!\r\n");
				}
			}
		}
	}

	private void writeResponse(Channel channel) {
		// Convert the response content to a ChannelBuffer.
		ByteBuf buf = copiedBuffer(responseContent.toString(),
				CharsetUtil.UTF_8);
		responseContent.setLength(0);

		// Decide whether to close the connection or not.
		boolean close = request.headers().contains(CONNECTION,
				HttpHeaders.Values.CLOSE, true)
				|| request.getProtocolVersion().equals(HttpVersion.HTTP_1_0)
				&& !request.headers().contains(CONNECTION,
						HttpHeaders.Values.KEEP_ALIVE, true);

		// Build the response object.
		FullHttpResponse response = new DefaultFullHttpResponse(
				HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);
		response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");

		if (!close) {
			// There's no need to add 'Content-Length' header
			// if this is the last response.
			response.headers().set(CONTENT_LENGTH, buf.readableBytes());
		}

		Set<Cookie> cookies;
		String value = request.headers().get(COOKIE);
		if (value == null) {
			cookies = Collections.emptySet();
		} else {
			cookies = CookieDecoder.decode(value);
		}
		if (!cookies.isEmpty()) {
			// Reset the cookies if necessary.
			for (Cookie cookie : cookies) {
				response.headers().add(SET_COOKIE,
						ServerCookieEncoder.encode(cookie));
			}
		}
		// Write the response.
		ChannelFuture future = channel.writeAndFlush(response);
		// Close the connection after the write operation is done if necessary.
		if (close) {
			future.addListener(ChannelFutureListener.CLOSE);
		}
	}

}
