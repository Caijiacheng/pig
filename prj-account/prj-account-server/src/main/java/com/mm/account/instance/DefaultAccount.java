package com.mm.account.instance;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.mm.account.db.MysqlDB;
import com.mm.account.error.DBException;
import com.mm.account.error.DupRegException;
import com.mm.account.error.NotExistException;


/**
 * 	account base info : store to mysql
 *  account detail info : protobuf to redis
 * 
 * @author caijiacheng
 *
 */

public class DefaultAccount extends PojoAccount {

	public static final String DB_NAME = "account";
	public static final String TABLE_NAME = "user";
	
	static final Logger LOG = LoggerFactory.getLogger(DefaultAccount.class);
	
	DefaultAccount(long id) {
		this._id = id;
	}
	
	
	public DefaultAccount(IAccount acc) {
		this._id = acc.id();
		this._infover = acc.version();
		this._name = acc.name().orNull();
		this._phoneid = acc.phoneid().orNull();
		this._pwd = acc.passwd();
		this._weiboid = acc.weiboid().orNull();
		this._qqid = acc.qqid().orNull();
	}
	
	@Override
	public void load() {
		
		String sql = 
				String.format("select * from %s where id='%s'", TABLE_NAME, id());
		
		MysqlDB db = new MysqlDB(DB_NAME);
		try(Connection conn = db.getConn())
		{
			try(Statement stmt = conn.createStatement()) {
				try(ResultSet rs = stmt.executeQuery(sql))
				{
					if ( !rs.next() )
					{
						throw new NotExistException(sql);
					}
					initAccWithResultSet(this, rs);
				}
			} 
		}catch (SQLException e) {
			throw new DBException(sql, e);
		}
		
	}

	
	static DefaultAccount initAccWithResultSet(DefaultAccount acc, ResultSet rs) throws SQLException
	{
		acc._name = rs.getString("name");
		acc._phoneid = rs.getString("phone_id");
		acc._infover = rs.getInt("info_version");
		acc._qqid = rs.getString("qq_id");
		acc._weiboid = rs.getString("weibo_id");
		acc._pwd = rs.getString("passwd");
		return acc;
	}
	
	static public class Service implements IAccountService
	{

		static final Logger LOG = LoggerFactory.getLogger(Service.class);
		
		private IAccount transform(ResultSet rs) throws SQLException
		{
			DefaultAccount acc = new DefaultAccount(rs.getLong("id"));
			return initAccWithResultSet(acc, rs);
		}
		
		@Override
		public IAccount register(String phoneid, String pwdmd5) {
			
			String sql = String.format("insert into `%s` (phone_id, passwd) select * from (select %s,%s) AS tmp where not exists (select phone_id from `%s` where phone_id=%s) limit 1",
					TABLE_NAME, phoneid, pwdmd5, TABLE_NAME, phoneid);
			
			MysqlDB db = new MysqlDB(DB_NAME);
			
			try(Connection conn = db.getConn())
			{
				try(Statement stmt = conn.createStatement()) {
					if ( stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS) != 1)
					{
						throw new DupRegException("dup register? " + phoneid);
					}
					try(ResultSet rs = stmt.getGeneratedKeys())
					{
						if ( !rs.next() )
						{
							throw new NotExistException(sql);
						}
						
						DefaultAccount acc = new DefaultAccount(rs.getLong(1));
						acc._phoneid = phoneid;
						acc._infover = 0;
						acc._pwd = pwdmd5;
						return acc;
					}
					
				} 
			}catch (SQLException e) {
				throw new DBException(sql, e);
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
			String sql = String.format("select id from %s where id=%s", TABLE_NAME, id);
			
			MysqlDB db = new MysqlDB(DB_NAME);
			
			try(Connection conn = db.getConn())
			{
				try(Statement stmt = conn.createStatement()) {
					try(ResultSet rs = stmt.executeQuery(sql))
					{
						return rs.next();
					}
				} 
			}catch (SQLException e) {
				throw new DBException(sql, e);
			}
		}

		@Override
		public void unregister(long id) {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public Optional<IAccount> getByPhoneId(String phoneid) {
			
			String sql = 
					String.format("select * from %s where phone_id=%s ", TABLE_NAME, phoneid);
			
			MysqlDB db = new MysqlDB(DB_NAME);
			try(Connection conn = db.getConn())
			{
				try(Statement stmt = conn.createStatement()) {
					try(ResultSet rs = stmt.executeQuery(sql))
					{
						if (!rs.next())
						{
							return Optional.absent();
						}
						
						return Optional.of(transform(rs));
					}
				} 
			}catch (SQLException e) {
				throw new DBException(sql, e);
			}
			
		}

		@Override
		public Optional<IAccount> getByWeiboId(String weiboid) {
			String sql = 
					String.format("select * where weibo_id='%s' from '%s'", weiboid, TABLE_NAME);
			
			MysqlDB db = new MysqlDB(DB_NAME);
			try(Connection conn = db.getConn())
			{
				try(Statement stmt = conn.createStatement()) {
					try(ResultSet rs = stmt.executeQuery(sql))
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
			String sql = 
					String.format("select * where qq_id='%s' from '%s'", qqid, TABLE_NAME);
			
			MysqlDB db = new MysqlDB(DB_NAME);
			try(Connection conn = db.getConn())
			{
				try(Statement stmt = conn.createStatement()) {
					try(ResultSet rs = stmt.executeQuery(sql))
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
		public void modifyPasswd(long userid, String pwdmd5) {
			String sql = 
					String.format("update %s set passwd=%s where id='%s'", TABLE_NAME, pwdmd5, userid);
			
			MysqlDB db = new MysqlDB(DB_NAME);
			try(Connection conn = db.getConn())
			{
				try(Statement stmt = conn.createStatement()) {
					if ( stmt.executeUpdate(sql) != 1)
					{
						throw new NotExistException("userid:"+userid);
					}
				} 
			}catch (SQLException e) {
				throw new DBException(e);
			}
		}

		@Override
		public boolean ping() {
			MysqlDB db = new MysqlDB(DB_NAME);
			
			try(Connection conn = db.getConn())
			{
				try(Statement stmt = conn.createStatement())
				{
					stmt.execute(String.format("select * from %s limit 1", TABLE_NAME));
				}
				return true;
			} catch (SQLException e) {
				return false;
			}
		}

		@Override
		public IAccount incrVersion(IAccount acc) {
			
			MysqlDB db = new MysqlDB(DB_NAME);
			
			String[] sqls = new String[]{
					String.format("update %s set info_version = LAST_INSERT_ID(info_version + 1) where id=%s", TABLE_NAME, acc.id()),
					String.format("select LAST_INSERT_ID()"),
			};
			
			try(Connection conn = db.getConn())
			{
				try(Statement stmt = conn.createStatement())
				{
					if (stmt.executeUpdate(sqls[0]) != 1)
					{
						throw new DBException(sqls[0]);
					}
					
					if (!stmt.execute(sqls[1]))
					{
						throw new DBException(sqls[1]);
					}
					
					ResultSet rs = stmt.getResultSet();
					if (!rs.next())
					{
						throw new DBException("not resultSet return");
					}
					int new_ver = rs.getInt(1);
					LOG.error("ver:{}", new_ver);
					DefaultAccount dac =  new DefaultAccount(acc);
					dac._infover = new_ver;
					return dac;
				}
				
			} catch (SQLException e) {
				throw new DBException(e);
			}
		}
	}
}
