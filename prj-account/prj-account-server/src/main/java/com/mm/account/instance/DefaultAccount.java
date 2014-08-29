package com.mm.account.instance;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.mm.account.db.MysqlDB;
import com.mm.account.error.DBException;
import com.mm.account.error.NotExistException;


/**
 * 	account base info : store to mysql
 *  account detail info : protobuf to redis
 * 
 * @author caijiacheng
 *
 */

public class DefaultAccount extends AbsAccount {

	static final String DB_NAME = "account";
	static final String TABLE_NAME = "user";
	
	static final Logger LOG = LoggerFactory.getLogger(DefaultAccount.class);
	
	DefaultAccount(long id) {
		this._id = id;
	}
	
	
	@Override
	public void load() {
		
		String query = 
				String.format("select * from %s where id='%s'", TABLE_NAME, id());
		
		MysqlDB db = new MysqlDB(DB_NAME);
		try(Connection conn = db.getConn())
		{
			try(Statement stmt = conn.createStatement()) {
				try(ResultSet rs = stmt.executeQuery(query))
				{
					if ( !rs.next() )
					{
						throw new NotExistException(query);
					}
					_name = rs.getString("name");
					_phoneid = rs.getString("phone_id");
					_qqid = rs.getString("qq_id");
					_weiboid = rs.getString("weibo_id");
					_infover = rs.getInt("info_version");
				}
			} 
		}catch (SQLException e) {
			throw new DBException(query, e);
		}
		
	}

	
	static public class Service implements IAccountService
	{

		static final Logger LOG = LoggerFactory.getLogger(Service.class);
		
		private IAccount transform(ResultSet rs) throws SQLException
		{
			DefaultAccount acc = new DefaultAccount(rs.getLong("id"));
			acc._name = rs.getString("name");
			acc._phoneid = rs.getString("phone_id");
			acc._infover = rs.getInt("info_version");
			acc._qqid = rs.getString("qq_id");
			acc._weiboid = rs.getString("weibo_id");
			return acc;
		}
		
		@Override
		public IAccount register(String phoneid) {
			
			String query = String.format("insert into `%s` (phone_id) select * from (select %s) AS tmp where not exists (select phone_id from `%s` where phone_id=%s) limit 1",
					TABLE_NAME, phoneid, TABLE_NAME, phoneid);
			
			MysqlDB db = new MysqlDB(DB_NAME);
			
			try(Connection conn = db.getConn())
			{
				try(Statement stmt = conn.createStatement()) {
					if ( stmt.executeUpdate(query, Statement.RETURN_GENERATED_KEYS) != 1)
					{
						throw new DBException("dup register? " + phoneid);
					}
					try(ResultSet rs = stmt.getGeneratedKeys())
					{
						if ( !rs.next() )
						{
							throw new NotExistException(query);
						}
						
						DefaultAccount acc = new DefaultAccount(rs.getLong(1));
						acc._phoneid = phoneid;
						acc._infover = 0;
						return acc;
					}
					
				} 
			}catch (SQLException e) {
				throw new DBException(query, e);
			}
		}

		@Override
		public Optional<IAccount> get(long id) {
			
			if (!exist(id))
			{
				return Optional.absent();
			}
			DefaultAccount account = new DefaultAccount(id);
			account.load();
			return Optional.of((IAccount)account);
		}

		@Override
		public boolean exist(long id) {
			String query = String.format("select id from %s where id=%s", TABLE_NAME, id);
			
			MysqlDB db = new MysqlDB(DB_NAME);
			
			try(Connection conn = db.getConn())
			{
				try(Statement stmt = conn.createStatement()) {
					try(ResultSet rs = stmt.executeQuery(query))
					{
						return rs.next();
					}
				} 
			}catch (SQLException e) {
				throw new DBException(query, e);
			}
		}

		@Override
		public void unregister(long id) {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public Optional<IAccount> getByPhoneId(String phoneid) {
			
			String query = 
					String.format("select * from %s where phone_id=%s ", TABLE_NAME, phoneid);
			
			MysqlDB db = new MysqlDB(DB_NAME);
			try(Connection conn = db.getConn())
			{
				try(Statement stmt = conn.createStatement()) {
					try(ResultSet rs = stmt.executeQuery(query))
					{
						if (!rs.next())
						{
							return Optional.absent();
						}
						
						return Optional.of(transform(rs));
					}
				} 
			}catch (SQLException e) {
				throw new DBException(query, e);
			}
			
		}

		@Override
		public Optional<IAccount> getByWeiboId(String weiboid) {
			String query = 
					String.format("select * where weibo_id='%s' from '%s'", weiboid, TABLE_NAME);
			
			MysqlDB db = new MysqlDB(DB_NAME);
			try(Connection conn = db.getConn())
			{
				try(Statement stmt = conn.createStatement()) {
					try(ResultSet rs = stmt.executeQuery(query))
					{
						if (!rs.next())
						{
							return Optional.absent();
						}
						
						return Optional.of(transform(rs));
					}
				} 
			}catch (SQLException e) {
				throw new DBException(e);
			}
		}

		@Override
		public Optional<IAccount> getByQQId(String qqid) {
			String query = 
					String.format("select * where qq_id='%s' from '%s'", qqid, TABLE_NAME);
			
			MysqlDB db = new MysqlDB(DB_NAME);
			try(Connection conn = db.getConn())
			{
				try(Statement stmt = conn.createStatement()) {
					try(ResultSet rs = stmt.executeQuery(query))
					{
						if (!rs.next())
						{
							return Optional.absent();
						}
						
						return Optional.of(transform(rs));
					}
				} 
			}catch (SQLException e) {
				throw new DBException(e);
			}
		}

		
	}


	
	
	
}
