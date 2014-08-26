package org.czr.rest;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

@Deprecated
public final class RestNettyServer {

	private int port = 8081;
	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
	private ChannelFuture cf;

	private Class<? extends ChannelHandlerAdapter> clsChannelHandle;
	
	private ChannelHandlerAdapter shareHandlerAdapter;
	
	public RestNettyServer(int port) {
		this.port = port;
		clsChannelHandle = RestServerHandle.class;
	}

	RestNettyServer(int port, Class<? extends ChannelHandlerAdapter> clsChannel) {
		this.port = port;
		clsChannelHandle = clsChannel;
	}

	public void shutdown() {
		try {
			if (cf != null) {
				cf.channel().close().sync();
			}
		} catch (InterruptedException e) {
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
			bossGroup = null;
			workerGroup = null;
			cf = null;
		}
	}

	public void run() throws Exception {


		bossGroup = new NioEventLoopGroup();
		workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.option(ChannelOption.SO_BACKLOG, 100)
					.handler(new LoggingHandler(LogLevel.INFO))
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch)
								throws Exception {
							
							if (shareHandlerAdapter == null)
							{
								shareHandlerAdapter = clsChannelHandle.newInstance();
							}
							
							ch.pipeline().addLast("decoder", new HttpRequestDecoder());
							ch.pipeline().addLast("aggregator", new HttpObjectAggregator(65536));
							ch.pipeline().addLast("logger", 
									new LoggingHandler(LogLevel.DEBUG));
							ch.pipeline().addLast("encoder", new HttpResponseEncoder());
							ch.pipeline().addLast("chunkedWriter", new ChunkedWriteHandler());
							
							ch.pipeline().addLast(
									"handler",
									shareHandlerAdapter);
							
							if (!shareHandlerAdapter.isSharable())
							{
								shareHandlerAdapter = null;
							}
							
						}
					});

			// Start the server.
			cf = b.bind(port).sync();

		} catch (Exception e) {
			// Shut down all event loops to terminate all threads.
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
			bossGroup = null;
			workerGroup = null;
			cf = null;
		}
	}

}
