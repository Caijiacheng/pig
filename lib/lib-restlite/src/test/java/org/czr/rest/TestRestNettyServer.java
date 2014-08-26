package org.czr.rest;

import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponse;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestRestNettyServer {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	private static AtomicInteger verifyCnt = new AtomicInteger(0);
	private RestNettyServer srv;

	@Before
	public void setUp() throws Exception {
		verifyCnt.set(0);
	}

	@After
	public void tearDown() throws Exception {
		if (srv != null) {
			srv.shutdown();
			srv = null;
		}
	}

	@Sharable
	static public class VerifySrvHandle extends RestServerHandle {

		@Override
		protected void messageReceived(ChannelHandlerContext ctx,
				FullHttpRequest msg) throws Exception {

			LOG.debug("get http from: {}, url: {}, content: {}", ctx.channel()
					.remoteAddress().toString(), msg.getUri(), msg.content()
					.toString());

			LOG.debug("ctx: {}, handle: {}", ctx, this);
			verifyCnt.addAndGet(1);

			HttpResponse response = new DefaultHttpResponse(HTTP_1_1, FORBIDDEN);
			ctx.writeAndFlush(response);
		}
	}

	static Logger LOG = LoggerFactory.getLogger(TestRestNettyServer.class);

	final static int SRV_PORT = 8081;
	final static String HTTP_LOCALHOST = "http://127.0.0.1:8081/";

	// final static String HTTP_LOCALHOST = "http://www.baidu.com";

	@Test
	public void testHttpClient() throws Exception {
		srv = new RestNettyServer(SRV_PORT, VerifySrvHandle.class);
		srv.run();

		URL url = new URL(HTTP_LOCALHOST + "index.html");

		// Thread.sleep(100_000);

		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setDoOutput(true);
		conn.setReadTimeout(2000);
		conn.connect();

		LOG.info("get the Response from: {}, ResCode:{}, ResCode:{}",
				url.toString(), conn.getResponseCode(),
				conn.getResponseMessage());

		if (conn.getResponseCode() == 200) {
			try (BufferedReader rd = new BufferedReader(new InputStreamReader(
					conn.getInputStream()))) {
				String line;
				while ((line = rd.readLine()) != null) {
					System.err.println(line);
				}
			}
		}

		Assert.assertEquals(verifyCnt.get(), 1);

	}

	@Test
	public void testHttpClientMoreInstance() throws Exception {
		srv = new RestNettyServer(SRV_PORT, VerifySrvHandle.class);
		srv.run();

		int cnt = 10;
		for (int i = 0; i < cnt; i++) {
			URL url = new URL(HTTP_LOCALHOST + "index.html");

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setDoOutput(true);
			conn.setReadTimeout(2000);
			conn.connect();

			LOG.info("get the Response from: {}, ResCode:{}, ResCode:{}",
					url.toString(), conn.getResponseCode(),
					conn.getResponseMessage());

			if (conn.getResponseCode() == 200) {
				try (BufferedReader rd = new BufferedReader(
						new InputStreamReader(conn.getInputStream()))) {
					String line;
					while ((line = rd.readLine()) != null) {
						System.err.println(line);
					}
				}
			}
		}

		Assert.assertEquals(verifyCnt.get(), cnt);

	}

}
