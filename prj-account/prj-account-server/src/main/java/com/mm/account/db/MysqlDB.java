package com.mm.account.db;

import java.util.Properties;

import com.mm.account.server.Config;
import com.mm.util.db.MysqlHandle;


public class MysqlDB extends MysqlHandle {

	static
	{
		Properties prop = Config.ins().getProperties();
		MysqlHandle.getDefaultConfig().mysqlDb = "account";
		MysqlHandle.getDefaultConfig().mysqlUser = 
				prop.getProperty(Config.PROP_MYSQL_USER);
		MysqlHandle.getDefaultConfig().mysqlPwd = 
				prop.getProperty(Config.PROP_MYSQL_PWD);
		MysqlHandle.getDefaultConfig().mysqServer = 
				prop.getProperty(Config.PROP_MYSQL_SERVER);
		
	}
	
	public MysqlDB(String db)
	{
		super(db);
	}
	

}
