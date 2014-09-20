package com.mm.photo.netty;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class HttpPhotoServer {
	static Logger LOG = LoggerFactory.getLogger(HttpPhotoServer.class);
	static final int DEFAULT_PORT = 8081;
	static final int DEFAULT_WORK_THREAD_NUM = 10;
	static final int DEFAULT_BOSS_THREAD_NUM = 0;
	static Properties s_prop = new Properties();
	static
	{
		try {
			s_prop.load(ClassLoader
					.getSystemResourceAsStream("release.propertis"));
		} catch (IOException e) {
			LOG.error("Config Load Failed!");
		}
	}
	
	
	
	private final int port;

	public HttpPhotoServer(int port) {
		this.port = port;
	}


	public HttpPhotoServer()
	{
		this(Integer.parseInt(s_prop.getProperty("leveldb.block.size",
				String.valueOf(DEFAULT_PORT))));
	}
	
	EventLoopGroup bossGroup = null;
	EventLoopGroup workerGroup = null;
	ChannelFuture bootStrap = null;
	
	public HttpPhotoServer run()  {
		bossGroup = new NioEventLoopGroup(
				);
		workerGroup = new NioEventLoopGroup(
				Integer.parseInt(s_prop.getProperty("photo.http.worker.thread.num",
						String.valueOf(DEFAULT_WORK_THREAD_NUM))));
		
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.childHandler(new HttpPhotoServerInitializer());

			bootStrap = b.bind(port).sync();//.channel().closeFuture().sync();
		} catch(Throwable e) {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
			throw new RuntimeException(e);
		}
		return this;
	}
	
	public void awaitShutdown()
	{
		try {
			bootStrap.channel().closeFuture().sync();
		} catch (InterruptedException e) {
		}finally{
			shutdown();
		}
	}
	
	public void shutdown()
	{
		if (bootStrap != null)
		{
			try {
				bootStrap.channel().close().sync();
			} catch (InterruptedException e) {
			}finally
			{
				bootStrap = null;
				if (bossGroup != null)
				{
					bossGroup.shutdownGracefully();
					bossGroup = null;
				}
				if (workerGroup != null)
				{
					workerGroup.shutdownGracefully();
					workerGroup = null;
				}
			}
			
			
		}
	}

	

	public static void main(String[] args) throws Exception {
		new HttpPhotoServer().run().awaitShutdown();
	}
}
