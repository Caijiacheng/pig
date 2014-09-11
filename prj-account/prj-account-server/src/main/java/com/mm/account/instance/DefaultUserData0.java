package com.mm.account.instance;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.mm.account.db.MysqlDB;
import com.mm.account.error.DBException;
import com.mm.account.proto.Account.Gender;
import com.mm.account.proto.Account.UserData;


//store to mysql

abstract public class DefaultUserData0 implements IVersion, ILoad, ISave {

	static final String DB_NAME = "account";
	static final String USER_TABLE = "user";
	static final String USER_INFO_TABLE = "user_info";
	
	protected UserData data;
	protected IAccount acc;
	IAccountService acc_service = new DefaultAccount.Service();
	
	int load_version = -1;
	
	public DefaultUserData0(long userid) {
		acc = acc_service.get(userid).get();
	}
	
	public DefaultUserData0(IAccount acc)
	{
		this.acc = acc;
	}
	
	
	final static Logger LOG = LoggerFactory.getLogger(DefaultUserData0.class);
	
	abstract public UserData.Builder transform(UserData.Builder builder);
	
	public UserData data()
	{
		return data;
	}
	@Override
	public void load() {
		
		if (!needToSync())
		{
			return;
		}
		
		String sql = 
				String.format("select * from %s where userid='%s' and version='%s'", USER_INFO_TABLE, acc.id(), acc.version());
		
		MysqlDB db = new MysqlDB(DB_NAME);
		try(Connection conn = db.getConn())
		{
			try(Statement stmt = conn.createStatement()) {
				try(ResultSet rs = stmt.executeQuery(sql))
				{
					if ( !rs.next() )
					{
						//throw new NotExistException(sql);
						if (acc.version() != 0)
						{
							LOG.error("DB didnot store the UserData correct? {}", acc );
						}
						
						data = UserData.newBuilder().setUid(acc.id()).setVersion(acc.version()).build();
					}else
					{
						data = UserData.newBuilder().setUid(acc.id())
								.setVersion(rs.getInt(acc.version()))
								.setFirstName(rs.getString("first_name"))
								.setLastName(rs.getString("last_name"))
								.setGender(Gender.valueOf(rs.getInt("gender")))
								.setHeadUrl(rs.getString("head_url"))
								.build();
					}
				}
			} 
		} catch (SQLException e) {
			throw new DBException(sql, e);
		}
		
		load_version = acc.version();
	}

	boolean needToSync()
	{
		if (load_version == acc.version())
		{
			return false;
		}
		return true;
	}
	
	/**
	 * XXX:这里可以不需要事务
	 * 
	 */
	
	@Override
	public void save() {
		
		
		data = transform(UserData.newBuilder()).setUid(acc.id()).setVersion(acc.version() + 1).build();
		Preconditions.checkNotNull(data);
		
		String sql_incre = String.format("update %s set info_version = LAST_INSERT_ID(info_version + 1) where id=%s", 
				USER_TABLE, acc.id());
		
		MysqlDB db = new MysqlDB(DB_NAME);
		try(Connection conn = db.getConn())
		{
			try(Statement increStat = conn.createStatement())
			{
				if (increStat.executeUpdate(sql_incre) != 1)
				{
					throw new DBException(sql_incre);
				}
				increStat.execute("select LAST_INSERT_ID()");
				ResultSet rs = increStat.getResultSet();
				if (!rs.next())
				{
					throw new DBException("not resultSet return");
				}
				int new_ver = rs.getInt(1);
				String sql_replace = 
						String.format("insert into %s (userid, version, gender, first_name, last_name, head_url) " +
								"values ('%s', %s, %s, '%s', '%s', '%s')",
								USER_INFO_TABLE, acc.id(), new_ver, data.getGender().getNumber(), 
								data.getFirstName(), data.getLastName(), data.getHeadUrl());
				if (increStat.executeUpdate(sql_replace) != 1)
				{
					throw new DBException(sql_replace);
				}
				
				DefaultAccount dac =  new DefaultAccount(acc);
				dac._infover = new_ver;
				acc = dac;
				
			}
		} catch (SQLException e) {
			throw new DBException(e);
		}
		
		/* -- 事务的实现
		String sql_incre = String.format("update %s set info_version = LAST_INSERT_ID(info_version + 1) where id=%s", 
				USER_TABLE, acc.id());
		String sql_replace = 
				String.format("insert into %s (userid, version, gender, first_name, last_name, head_url) " +
						"values ('%s', ?, %s, '%s', '%s', '%s')",
						USER_INFO_TABLE, acc.id(), data.getGender().getNumber(), 
						data.getFirstName(), data.getLastName(), data.getHeadUrl());
		

		MysqlDB db = new MysqlDB(DB_NAME);
		Connection conn = null;
		boolean isAutoCommit = false;
		try
		{
			conn = db.getConn();
			isAutoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);
			try(PreparedStatement increStat = conn.prepareStatement(sql_incre))
			{
				try(PreparedStatement replaceStat = conn.prepareStatement(sql_replace))
				{
					increStat.executeUpdate();
					increStat.execute("select LAST_INSERT_ID()");
					ResultSet rs = increStat.getResultSet();
					if (!rs.next())
					{
						throw new DBException("not resultSet return");
					}
					int new_ver = rs.getInt(1);
					replaceStat.setInt(1, new_ver);
					replaceStat.executeUpdate();
					conn.commit();
					
					DefaultAccount dac =  new DefaultAccount(acc);
					dac._infover = new_ver;
					acc = dac;
				}
			}
			
			
		} catch (SQLException e) {
			if (conn != null)
			{
				try {
					LOG.error("conn rollback");
					conn.rollback();
				} catch (SQLException e1) {
					throw new DBException(e1);
				}
			}
			throw new DBException(e);
		}finally
		{
			if (conn != null)
			{
				try {
					conn.setAutoCommit(isAutoCommit);
				} catch (SQLException e) {
				}
			}
		}
		*/
		load_version = acc.version();
		
	}

	@Override
	public int current() {
		return load_version;
	}

}
