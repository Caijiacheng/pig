package org.restexpress;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

import junit.framework.Assert;

import io.netty.handler.codec.http.HttpMethod;

import org.junit.Test;
import org.restexpress.TestRestExpress.ErrorMsgObserver;
import org.restexpress.annotations.RequestMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestRestExpressAnnotations {

	final static int SRV_PORT = 8081;
	final static String HTTP_LOCALHOST = "http://127.0.0.1:8081/";
	
	static Logger LOG = LoggerFactory.getLogger(TestRestExpressAnnotations.class);
	
	static class AnnotationControls
	{
		
		static AtomicBoolean M1_FLAG = new AtomicBoolean(false);
		static AtomicBoolean M2_FLAG = new AtomicBoolean(false);
		
		@RequestMethod(value="/index.html", method="GET")
		public void method1(Request r, Response p)
		{
			
			if (r.getHttpMethod().equals(HttpMethod.GET))
			{
				M1_FLAG.set(true);
			}
		}
		
		@RequestMethod(value="/index1.html", method="HEAD")
		public void method2(Request r, Response p)
		{
			if (r.getHttpMethod().equals(HttpMethod.HEAD))
			{
				M2_FLAG.set(true);
			}
		}
		
		boolean verify()
		{
			return M1_FLAG.get() && M2_FLAG.get();
		}
		
	}
	
	
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
	
	@Test
	public void testAnnotation()
	{
		RestExpress srv;
		srv = new RestExpress().setName("testErrorUrl")
				.setBaseUrl(HTTP_LOCALHOST)
				.addMessageObserver(new ErrorMsgObserver());
		
		AnnotationControls control = new AnnotationControls();
		
		srv.routeBuildWithAnnotations(control);

		srv.bind(SRV_PORT);
		
		new RestClient("index.html", "GET").run();
		new RestClient("index1.html", "HEAD").run();
		
		srv.shutdown();
		
		Assert.assertTrue(control.verify());
	}
}
