package com.mm.account.server;

import java.io.IOException;
import java.util.Properties;

import org.restexpress.RestExpress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mm.account.control.LoginController;

/**
 *
 */
@Deprecated
public class App 
{
	static Logger LOG = LoggerFactory.getLogger(App.class);
    public static void main( String[] args ) throws IOException
    {
//        System.out.println( "Hello World!" );
		Properties prop = new Properties();
		prop.load(App.class.getClassLoader().getResourceAsStream("release.propertis"));
		
//		LOG.error("prop:{}", prop);
		RestExpress srv;
		srv = new RestExpress()
		.setName("account-server")
		.setBaseUrl("0.0.0.0");
		
		LoginController login = new LoginController();
		srv.routeBuildWithAnnotations(login);
		srv.bind(8080);
		
		srv.awaitShutdown();
    }
}
