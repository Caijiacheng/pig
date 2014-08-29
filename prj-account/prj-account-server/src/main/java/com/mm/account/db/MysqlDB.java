package com.mm.account.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import com.mm.account.server.Config;


public class MysqlDB extends AbsDBHandle<Connection> {

	static
	{
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	private final String mysql_conn;
	
	public MysqlDB(String db)
	{
		Properties prop = Config.ins().getProperties();
		mysql_conn = String.format("jdbc:mysql://%s/%s?user=%s&password=%s", 
				prop.get(Config.PROP_MYSQL_SERVER), 
				db,
				prop.get(Config.PROP_MYSQL_USER), 
				prop.get(Config.PROP_MYSQL_PWD));
	}
	
	Connection doConnect()
	{
		Connection conn;
		try {
		    conn =
		       DriverManager.getConnection(mysql_conn);
		} catch (SQLException ex) {
		   throw new RuntimeException(ex);
		}
		return conn;
	}

	@Override
	public
	void close(Connection h) {
		try {
			h.close();
		} catch (SQLException e) {
		}
	}

}
