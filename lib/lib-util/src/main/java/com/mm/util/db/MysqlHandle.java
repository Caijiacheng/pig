package com.mm.util.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.google.common.base.Preconditions;

//FIXME: 不同的DB共享的连接是不一样的
public class MysqlHandle extends AbsDBHandle<Connection> {

	static
	{
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static class Conf
	{
		public String mysqServer;
		public String mysqlDb;
		public String mysqlUser;
		public String mysqlPwd;
	}
	
	private final String mysql_conn;
	private final String mysql_db;
	
	static private Conf default_config = null;
	
	static public Conf getDefaultConfig()
	{
		if (default_config == null)
		{
			default_config = new Conf();
		}
		return default_config;
	}
	
	
	public MysqlHandle()
	{
		this(getDefaultConfig().mysqlDb);
	}
	
	public MysqlHandle(String db)
	{
		
		Preconditions.checkNotNull(default_config);
		Preconditions.checkNotNull(db);
		Preconditions.checkNotNull(getDefaultConfig().mysqServer);
		Preconditions.checkNotNull(getDefaultConfig().mysqlUser);
		Preconditions.checkNotNull(getDefaultConfig().mysqlPwd);
		
		mysql_conn = String.format("jdbc:mysql://%s/%s?user=%s&password=%s", 
				getDefaultConfig().mysqServer,
				db,
				getDefaultConfig().mysqlUser,
				getDefaultConfig().mysqlPwd);
		mysql_db = db;
	}
	
	protected Connection doConnect()
	{
		Connection conn;
		try {
		    conn =
		       DriverManager.getConnection(mysql_conn);
		} catch (SQLException ex) {
		   throw new RuntimeException(ex);
		}
		
		if (s_conn_share_mode.get())
		{
			return new WrapperConn(conn);
		}
		
		return conn;
	}
	
	@Override
	protected boolean isSharedConn(AutoCloseable handle) {
		if (super.isSharedConn(handle))
		{
			Connection c = (Connection)handle;
			try {
				if (c.getCatalog().equals(mysql_db))
				{
					return true;
				}
			} catch (SQLException e1) {
			}
		}
		return false;
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
