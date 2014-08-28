package com.mm.account.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;


final public class Config {

	static Config conf = new Config();
	static final String DEFAULT_PROPERTIES = "release.propertis";
	public static final Charset DEFAULT_CHARSET = Charset.forName("utf-8"); 
	
	public final static String PROP_REDIS_SERVER = "redis.server";
	public final static String PROP_REDIS_PASSWORD = "redis.password";
	public final static String PROP_REDIS_DBNUM = "redis.dbnum";

	public final static String PROP_MYSQL_SERVER = "mysql.server";
	public final static String PROP_MYSQL_USER = "mysql.user";
	public final static String PROP_MYSQL_PWD = "mysql.password";
	public final static String PROP_RESTFUL_BIND_PORT = "restful.bind.port";
	
	static
	{
		conf.init();
	}
	
	private Config(){};
	
	static public Config ins()
	{
		return conf;
	}
	
	Properties prop = new Properties();
	
	public Properties getProperties()
	{
		return prop;
	}
	
	private void init()
	{
		Path property_file = FileSystems.getDefault().getPath("", DEFAULT_PROPERTIES);
		
		try(BufferedReader reader = Files.newBufferedReader(property_file, DEFAULT_CHARSET))
		{
			prop.load(reader);
			
		} catch (IOException e) {
			throw new RuntimeException("Init Conf failed", e);
		}
	}
}
