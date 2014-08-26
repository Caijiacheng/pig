package org.czr.rest;

import io.netty.handler.codec.http.HttpMethod;

import org.restexpress.Request;
import org.restexpress.Response;
import org.restexpress.RestExpress;
import org.restexpress.pipeline.MessageObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestMainServer {

	static Logger LOG = LoggerFactory.getLogger(RestMainServer.class);
	
	static class ErrorMsgObserver extends MessageObserver {

		@Override
		protected void onReceived(Request request, Response response) {
			// LOG.debug("flag Recv");
		}

		@Override
		protected void onException(Throwable exception, Request request,
				Response response) {
			// LOG.debug("flag Exception");
			LOG.error("", exception);
		}

	}
	
	static public class EchoController {

		public Object read(Request request, Response response) {
//			LOG.debug("ExampleController: read");
			return "done";
		}

		

	}
	
	final static int SRV_PORT = 10000;
	final static String HTTP_LOCALHOST = "http://0.0.0.0:10000/";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		RestExpress srv = new RestExpress().setName("MAINSERVER")
				.setBaseUrl(HTTP_LOCALHOST)
				.addMessageObserver(new ErrorMsgObserver());
		srv.uri("/index.html", new EchoController()).method(HttpMethod.GET)
				.name("DEFAULT_HTML");

		srv.bind(SRV_PORT);
		srv.awaitShutdown();
	}

}
