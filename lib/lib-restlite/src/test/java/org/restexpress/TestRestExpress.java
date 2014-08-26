package org.restexpress;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.restexpress.pipeline.MessageObserver;
import org.restexpress.pipeline.SimpleConsoleLogMessageObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jzlib.GZIPInputStream;
import com.jcraft.jzlib.GZIPOutputStream;

public class TestRestExpress {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	static Logger LOG = LoggerFactory.getLogger(TestRestExpress.class);

	static public class ExampleController {

		public String create(Request request, Response response) {
			LOG.debug("ExampleController: create");
			return "create";
		}

		public Object read(Request request, Response response) {
			LOG.debug("ExampleController: read");
			return "read";
		}

		public List<Object> readAll(Request request, Response response) {
			LOG.debug("ExampleController: readAll");
			return new ArrayList<>();
		}

		public void update(Request request, Response response) {
			LOG.debug("ExampleController: update");
			return;
		}

		public void delete(Request request, Response response) {
			LOG.debug("ExampleController: delete");
			return;
		}

		public void headMethod(Request request, Response response) {
			LOG.debug("ExampleController: headMethod");
			return;
		}

		public void showParam(Request request) {
		}

	}

	static class RestServer implements Runnable {
		RestExpress server;

		@Override
		public void run() {

			server = new RestExpress().setName("Sample Blogging")
					.setBaseUrl(HTTP_LOCALHOST)
					.addMessageObserver(new MsgObserver());

			server.uri("/example/{id}/{format}", new ExampleController())
					.action("readAll", HttpMethod.GET).method(HttpMethod.GET)
					.name("ROUTE_EXAMPLE");

			server.uri("/index.html", new ExampleController())
					.method(HttpMethod.GET).name("DEFAULT_HTML");

			server.bind(SRV_PORT);
		}

		public void awaitShutdown() {
			server.awaitShutdown();
		}

		public void shutdown() {
			server.shutdown();
		}

	}

	final static int SRV_PORT = 8081;
	final static String HTTP_LOCALHOST = "http://127.0.0.1:8081/";

	static class RestClient implements Runnable {
		String uri;
		String method = "GET";

		public RestClient(String uri) {
			this.uri = uri;
		}

		public RestClient(String url, String method) {
			this(url);
			this.method = method;
		}

		@Override
		public void run() {
			URL url;
			try {
				url = new URL(HTTP_LOCALHOST + uri);

				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.setRequestMethod(method);
				conn.setDoOutput(true);
				conn.setReadTimeout(0);
				conn.connect();

				if (conn.getResponseCode() == 200) {
					try (BufferedReader rd = new BufferedReader(
							new InputStreamReader(conn.getInputStream()))) {
						String line;
						while ((line = rd.readLine()) != null) {
							// System.err.println("ttt:" + line);
							LOG.error("get Response Content: {}", line);
						}
					}
				}

				LOG.info("get the Response from: {}, ResCode:{}, RetMsg:{}",
						url.toString(), conn.getResponseCode(),
						conn.getResponseMessage());

			} catch (Exception e) {
				LOG.error("", e);
			}

		}
	}

	static class MsgObserver extends MessageObserver {

		@Override
		protected void onReceived(Request request, Response response) {
			LOG.error("Get Request: {}", request);
		}

		@Override
		protected void onException(Throwable exception, Request request,
				Response response) {
			LOG.error("Request Error: {} exception:{}", request.getUrl(),
					exception);
		}
	}

	private RestExpress srv;
	private static AtomicInteger verifyCnt = new AtomicInteger(0);

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

	@Ignore
	public void test() throws InterruptedException {
		RestServer server = new RestServer();
		server.run();

		RestClient client = new RestClient("index.html");
		client.run();

		server.shutdown();
	}

	static AtomicBoolean flagRecv = new AtomicBoolean(false);
	static AtomicBoolean flagException = new AtomicBoolean(false);

	static class ErrorMsgObserver extends MessageObserver {

		@Override
		protected void onReceived(Request request, Response response) {
			flagRecv.set(true);
			// LOG.debug("flag Recv");
		}

		@Override
		protected void onException(Throwable exception, Request request,
				Response response) {
			flagException.set(true);
			// LOG.debug("flag Exception");
		}

	}

	@Ignore
	public void testErrorUrl() throws InterruptedException {
		srv = new RestExpress().setName("testErrorUrl")
				.setBaseUrl(HTTP_LOCALHOST)
				.addMessageObserver(new ErrorMsgObserver());
		srv.uri("/index.html", new ExampleController()).method(HttpMethod.GET)
				.name("DEFAULT_HTML");

		srv.bind(SRV_PORT);

		RestClient client = new RestClient("index1.html");
		client.run();

		Assert.assertTrue(flagRecv.get());
		Assert.assertTrue(flagException.get());
	}

	static volatile String Param_ID = "";
	static volatile String Param_FORMAT = "";

	static class ParamControl extends ExampleController {
		@Override
		public Object read(Request request, Response response) {

			Param_ID = request.getHeader("id");
			Param_FORMAT = request.getHeader("ttt");
			LOG.error("URL={}, ParamID={}, Param_FORMAT={}, headers={}",
					request.getUrl(), Param_ID, Param_FORMAT,
					request.getHeaderNames());

			return super.read(request, response);
		}
	}

	@Ignore
	public void testParamUrl() throws InterruptedException {
		srv = new RestExpress().setName("testParamUrl")
				.setBaseUrl(HTTP_LOCALHOST)
				.addMessageObserver(new ErrorMsgObserver());
		srv.uri("/index.html/{id}/{ttt}", new ParamControl())
				.method(HttpMethod.GET).name("DEFAULT_HTML");

		srv.bind(SRV_PORT);

		String id = "111";
		String format = "fedf";

		RestClient client = new RestClient("index.html/" + id + "/" + format);
		client.run();

		Assert.assertEquals(Param_ID, id);
		Assert.assertEquals(Param_FORMAT, format);
	}

	static Map<String, String> QutaParams;

	static class QutaControl extends ExampleController {
		@Override
		public Object read(Request request, Response response) {

			Param_ID = request.getHeader("id");
			Param_FORMAT = request.getHeader("ttt");

			// comment: getHeader(key1), getHeader(key2) is the same. NOTE the
			// comment of the request.getHeader()
			QutaParams = request.getQueryStringMap();

			LOG.info("QutaParams={}", QutaParams);

			return super.read(request, response);
		}
	}

	@Ignore
	public void testQuta() throws InterruptedException, UnsupportedEncodingException {
		srv = new RestExpress().setName("testQuta").setBaseUrl(HTTP_LOCALHOST)
				.addMessageObserver(new ErrorMsgObserver());

		srv.uri("/index.html/{id}/{ttt}", new QutaControl())
				.method(HttpMethod.GET).name("DEFAULT_HTML");

		srv.bind(SRV_PORT);

		String id = "222";
		String format = "fdcfsdf";
		String key1 = "key1";
		String key2 = "key2";
		String key3 = "key3";
		String value1 = "value1";
		String value2 = "value2";
		String value3 = "value3";

		String uri = String.format("index.html/%s/%s?%s=%s&%s=%s;%s=%s", id,
				format, key1, URLEncoder.encode(value1,"utf-8"), key2, URLEncoder.encode(value2, "utf-8"), key3, URLEncoder.encode(value3, "utf-8"));

		RestClient client = new RestClient(uri);
		client.run();

		Assert.assertEquals(Param_ID, id);
		Assert.assertEquals(Param_FORMAT, format);

		Assert.assertEquals(QutaParams.get(key1), value1);
		Assert.assertEquals(QutaParams.get(key2), value2);
		Assert.assertEquals(QutaParams.get(key3), value3);
	}

	static AtomicBoolean FlagHeadMethod = new AtomicBoolean(false);

	static class ActionController extends ExampleController {
		@Override
		public void headMethod(Request request, Response response) {
			FlagHeadMethod.set(true);
			LOG.error("HEAD METHOD CALL");
		}
	}

	@Ignore
	public void testActionMapping() throws InterruptedException {
		srv = new RestExpress().setName("testActionMapping").setBaseUrl(
				HTTP_LOCALHOST);
		srv.uri("/index.html", new ActionController()).method(HttpMethod.GET)
				.action("headMethod", HttpMethod.HEAD).name("DEFAULT_HTML");

		srv.bind(SRV_PORT);

		RestClient client = new RestClient("index.html", "HEAD");
		client.run();

		Assert.assertTrue(FlagHeadMethod.get());
	}

	public static final String CONTENT = "{a:[1,2,3,4], b:[1,2,3,4]}";

	static class RestPostCompress implements Runnable {
		String uri;
		String method = "POST";
		
		public volatile boolean isEchoOK = false;

		public RestPostCompress(String uri) {
			this.uri = uri;
		}

		@Override
		public void run() {
			URL url;
			try {
				url = new URL(HTTP_LOCALHOST + uri);

				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.setRequestMethod(method);
				conn.setDoOutput(true);
				conn.setReadTimeout(0);
				conn.setRequestProperty("Content-Encoding", "gzip");
				conn.setRequestProperty("Content-Type", "text/html; charset=utf-8");
				conn.setRequestProperty(HttpHeaders.Names.ACCEPT_ENCODING.toString(), "gzip");
				conn.connect();

				ByteArrayOutputStream originalContent = new ByteArrayOutputStream();
				originalContent
						.write(CONTENT.getBytes(Charset.forName("UTF-8")));

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				GZIPOutputStream gzipOut = new GZIPOutputStream(baos);
				originalContent.writeTo(gzipOut);
				gzipOut.finish();

				DataOutputStream out = new DataOutputStream(
						conn.getOutputStream());

				out.write(baos.toByteArray());
				//out.writeBytes(CONTENT);
				out.flush();
				out.close();

				
				if (conn.getResponseCode() == 200) {
					
					
					try(GZIPInputStream gzipinput = new GZIPInputStream(conn.getInputStream()))
					{
						StringBuilder sb = new StringBuilder();
						byte[] bs = new byte[256];
						int length = 0;
						while( (length=gzipinput.read(bs)) != -1)
						{
							sb.append(new String(bs, 0, length));
						}
						
						LOG.error("get Response Content: {}", sb.toString());
						sb.trimToSize();
						String sbb = sb.toString();
						String wrapperContent = "\"" + CONTENT + "\"";
						
						LOG.debug("Equals? {}, len: sbb={}, content={}", sbb.equals(wrapperContent), sbb.length(), wrapperContent.length());
						
						isEchoOK = Objects.equals(sbb, wrapperContent );
					}
					
				}

				LOG.info("get the Response from: {}, ResCode:{}, RetMsg:{}",
						url.toString(), conn.getResponseCode(),
						conn.getResponseMessage());
//				Thread.sleep(60000)	;

			} catch (Exception e) {
				LOG.error("", e);
			}

		}
	}

	static class CompressController extends ExampleController {
		@Override
		public String create(Request request, Response response) {

			LOG.info("POST METHOD");
			response.addHeader(HttpHeaders.Names.CONTENT_ENCODING.toString(), HttpHeaders.Values.IDENTITY.toString());
			
			FlagPOSTMethod.set(true);
			return request.getBody().toString(Charset.forName("UTF-8"));
		}
	}

	static AtomicBoolean FlagPOSTMethod = new AtomicBoolean(false);

	
	
	@Ignore
	public void testCompress() throws InterruptedException {
		srv = new RestExpress().setName("testActionMapping").setBaseUrl(
				HTTP_LOCALHOST);
		srv.uri("/index.html", new CompressController()).method(HttpMethod.POST)
				.name("DEFAULT_HTML");

		srv.bind(SRV_PORT);

		RestPostCompress client = new RestPostCompress("index.html");
		client.run();

		Assert.assertTrue(FlagPOSTMethod.get());
		Assert.assertTrue(client.isEchoOK);

	}
	
	static class RestKeepAliveGet implements Runnable {
		String uri;
		String method = "GET";
		
		public volatile boolean isEchoOK = false;

		public RestKeepAliveGet(String uri) {
			this.uri = uri;
		}

		@Override
		public void run() {
			URL url;
			try {
				url = new URL(HTTP_LOCALHOST + uri);

				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.setRequestMethod(method);
				conn.setDoOutput(true);
				conn.setReadTimeout(0);
				conn.setRequestProperty(HttpHeaders.Names.CONNECTION.toString(), HttpHeaders.Values.KEEP_ALIVE.toString());
				conn.connect();

				//out.writeBytes(CONTENT);
//				out.close();
				
				if (conn.getResponseCode() == 200) {
					
					try (BufferedReader rd = new BufferedReader(
							new InputStreamReader(conn.getInputStream()))) {
						String line;
						while ((line = rd.readLine()) != null) {
							// System.err.println("ttt:" + line);
							LOG.error("get Response Content: {}", line);
						}
					}
					
				}
				LOG.info("get the Response from: {}, ResCode:{}, RetMsg:{}",
						url.toString(), conn.getResponseCode(),
						conn.getResponseMessage());
				
//				conn.disconnect();
				
//				if (conn.getResponseCode() == 200) {
//					
//					try (BufferedReader rd = new BufferedReader(
//							new InputStreamReader(conn.getInputStream()))) {
//						String line;
//						while ((line = rd.readLine()) != null) {
//							// System.err.println("ttt:" + line);
//							LOG.error("get Response Content: {}", line);
//						}
//					}
//					
//				}
//				
//				LOG.info("get the Response from: {}, ResCode:{}, RetMsg:{}",
//						url.toString(), conn.getResponseCode(),
//						conn.getResponseMessage());
				

			} catch (Exception e) {
				LOG.error("", e);
			}

		}
	}
	
	
	@Test
	public void testKeepAlive() throws InterruptedException
	{
		srv = new RestExpress().setName("testKeepAlive").setBaseUrl(
				HTTP_LOCALHOST).addMessageObserver(new SimpleConsoleLogMessageObserver());
		srv.uri("/index.html", new ExampleController()).method(HttpMethod.GET)
				.name("DEFAULT_HTML");

		srv.bind(SRV_PORT);

		RestKeepAliveGet client = new RestKeepAliveGet("index.html");
		client.run();

		Assert.assertTrue(client.isEchoOK);
	}
	
//	static class RestEchoPostJSON
//	{
//		
//	}
//	
//	@Ignore
//	public void testJSON() throws InterruptedException
//	{
//		srv = new RestExpress().setName("testActionMapping").setBaseUrl(
//				HTTP_LOCALHOST);
//		srv.uri("/index.html", new CompressController()).method(HttpMethod.POST)
//				.name("DEFAULT_HTML");
//
//		srv.bind(SRV_PORT);
//
//		RestPostCompress client = new RestPostCompress("index.html");
//		client.run();
//
//		Assert.assertTrue(FlagPOSTMethod.get());
//		Assert.assertTrue(client.isEchoOK);
//
//	}

}
