package com.mm.account.server;

import java.io.IOException;
import java.util.Properties;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hello world!
 *
 */
public class App 
{
	static Logger LOG = LoggerFactory.getLogger("aaa");
    public static void main( String[] args ) throws IOException
    {
        System.out.println( "Hello World!" );
		Properties prop = new Properties();
		prop.load(App.class.getClassLoader().getResourceAsStream("release.propertis"));
		
		for (Entry<Object, Object> obj : prop.entrySet())
		{
			LOG.error("key:{}, value:{}", obj.getKey(), obj.getValue());
		}
    }
}
