package com.mm.account.instance;

import io.netty.handler.codec.UnsupportedMessageTypeException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.BaseEncoding;
import com.mm.account.db.MysqlDB;
import com.mm.account.error.DBException;
import com.mm.account.error.DupRegException;
import com.mm.account.error.NotExistException;
import com.mm.account.proto.Account.UserRelate;

/**
 * account base info : store to mysql account detail info : protobuf to redis
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
		this._pwd = acc.passwd().orNull();
		this._weiboid = acc.weiboid().orNull();
		this._qqid = acc.qqid().orNull();
		this._validate = acc.validate();
		this._request_token = acc.requesttoken();
	}

	@Override
	public void load() {

		String sql = String.format("select * from %s where id='%s'",
				TABLE_NAME, id());

		MysqlDB db = new MysqlDB(DB_NAME);
		try (Connection conn = db.getConn()) {
			try (Statement stmt = conn.createStatement()) {
				try (ResultSet rs = stmt.executeQuery(sql)) {
					if (!rs.next()) {
						throw new NotExistException(sql);
					}
					initAccWithResultSet(this, rs);
				}
			}
		} catch (SQLException e) {
			throw new DBException(sql, e);
		}

	}

	static DefaultAccount initAccWithResultSet(DefaultAccount acc, ResultSet rs)
			throws SQLException {
		acc._name = rs.getString("name");
		acc._phoneid = rs.getString("phone_id");
		acc._infover = rs.getInt("info_version");
		acc._qqid = rs.getString("qq_id");
		acc._weiboid = rs.getString("weibo_id");
		acc._pwd = rs.getString("passwd");
		acc._validate = rs.getBoolean("friend_validate");
		acc._request_token = rs.getString("request_token");
		return acc;
	}

	static public class Service implements IAccountService {

		static final Logger LOG = LoggerFactory.getLogger(Service.class);

		private IAccount transform(ResultSet rs) throws SQLException {
			DefaultAccount acc = new DefaultAccount(rs.getLong("id"));
			return initAccWithResultSet(acc, rs);
		}

		String newLoginToken(long userid) {

			String st = String.format("login_token_%s_%s", userid,
					System.currentTimeMillis());

			MessageDigest md;
			try {
				md = MessageDigest.getInstance("md5");
				md.update(st.getBytes());

			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			}

			return BaseEncoding.base32Hex().encode(md.digest());
		}

		@Override
		public IAccount register(String phoneid, String pwdmd5) {

			String sql = String
					.format("insert into `%s` (phone_id, passwd) select * from (select %s,%s) AS tmp where not exists (select phone_id from `%s` where phone_id=%s) limit 1",
							TABLE_NAME, phoneid, pwdmd5, TABLE_NAME, phoneid);

			MysqlDB db = new MysqlDB(DB_NAME);

			try (Connection conn = db.getConn()) {
				try (Statement stmt = conn.createStatement()) {
					if (stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS) != 1) {
						throw new DupRegException("dup register? " + phoneid);
					}
					try (ResultSet rs = stmt.getGeneratedKeys()) {
						if (!rs.next()) {
							throw new NotExistException(sql);
						}

						DefaultAccount acc = new DefaultAccount(rs.getLong(1));
						acc._phoneid = phoneid;
						acc._infover = 0;
						acc._pwd = pwdmd5;
						// acc._request_token = newLoginToken(acc.id());
						return rebuildToken(acc);
					}

				}
			} catch (SQLException e) {
				throw new DBException(sql, e);
			}
		}

		@Override
		public Optional<IAccount> get(long id) {

			if (!exist(id)) {
				return Optional.absent();
			}
			DefaultAccount account = new DefaultAccount(id);
			account.load();
			return Optional.of((IAccount) account);
		}

		@Override
		public boolean exist(long id) {
			String sql = String.format("select id from %s where id=%s",
					TABLE_NAME, id);

			MysqlDB db = new MysqlDB(DB_NAME);

			try (Connection conn = db.getConn()) {
				try (Statement stmt = conn.createStatement()) {
					try (ResultSet rs = stmt.executeQuery(sql)) {
						return rs.next();
					}
				}
			} catch (SQLException e) {
				throw new DBException(sql, e);
			}
		}

		//
		@Override
		public void unregister(long id) {

			String query = String.format("delete from %s where id=%s",
					DefaultAccount.TABLE_NAME, id);

			String relate = String.format(
					"delete from %s where user_id_a=%s or user_id_b=%s",
					TABLE_RELATE_NAME, id, id);

			MysqlDB db = new MysqlDB(DefaultAccount.DB_NAME);

			try (Connection conn = db.getConn()) {
				try (Statement stmt = conn.createStatement()) {
					stmt.execute(query);
					stmt.execute(relate);
				}
			} catch (SQLException e) {
				throw new DBException(e);
			}
		}

		@Override
		public Optional<IAccount> getByPhoneId(String phoneid) {

			String sql = String.format("select * from %s where phone_id=%s ",
					TABLE_NAME, phoneid);

			MysqlDB db = new MysqlDB(DB_NAME);
			try (Connection conn = db.getConn()) {
				try (Statement stmt = conn.createStatement()) {
					try (ResultSet rs = stmt.executeQuery(sql)) {
						if (!rs.next()) {
							return Optional.absent();
						}

						return Optional.of(transform(rs));
					}
				}
			} catch (SQLException e) {
				throw new DBException(sql, e);
			}

		}

		//
		// @Override
		// public Optional<IAccount> getByWeiboId(String weiboid) {
		// String sql =
		// String.format("select * where weibo_id='%s' from '%s'", weiboid,
		// TABLE_NAME);
		//
		// MysqlDB db = new MysqlDB(DB_NAME);
		// try(Connection conn = db.getConn())
		// {
		// try(Statement stmt = conn.createStatement()) {
		// try(ResultSet rs = stmt.executeQuery(sql))
		// {
		// if (!rs.next())
		// {
		// return Optional.absent();
		// }
		//
		// return Optional.of(transform(rs));
		// }
		// }
		// }catch (SQLException e) {
		// throw new DBException(e);
		// }
		// }
		//
		// @Override
		// public Optional<IAccount> getByQQId(String qqid) {
		// String sql =
		// String.format("select * where qq_id='%s' from '%s'", qqid,
		// TABLE_NAME);
		//
		// MysqlDB db = new MysqlDB(DB_NAME);
		// try(Connection conn = db.getConn())
		// {
		// try(Statement stmt = conn.createStatement()) {
		// try(ResultSet rs = stmt.executeQuery(sql))
		// {
		// if (!rs.next())
		// {
		// return Optional.absent();
		// }
		//
		// return Optional.of(transform(rs));
		// }
		// }
		// }catch (SQLException e) {
		// throw new DBException(e);
		// }
		// }

		@Override
		public void modifyPasswd(long userid, String pwdmd5) {
			String sql = String.format("update %s set passwd='%s' where id=%s",
					TABLE_NAME, pwdmd5, userid);

			MysqlDB db = new MysqlDB(DB_NAME);
			try (Connection conn = db.getConn()) {
				try (Statement stmt = conn.createStatement()) {
					if (stmt.executeUpdate(sql) != 1) {
						throw new NotExistException("userid:" + userid);
					}
				}
			} catch (SQLException e) {
				throw new DBException(sql, e);
			}
		}

		@Override
		public boolean ping() {
			MysqlDB db = new MysqlDB(DB_NAME);

			try (Connection conn = db.getConn()) {
				try (Statement stmt = conn.createStatement()) {
					stmt.execute(String.format("select * from %s limit 1",
							TABLE_NAME));
				}
				return true;
			} catch (SQLException e) {
				return false;
			}
		}

		@Override
		public IAccount incrVersion(IAccount acc) {

			MysqlDB db = new MysqlDB(DB_NAME);

			String[] sqls = new String[] {
					String.format(
							"update %s set info_version = LAST_INSERT_ID(info_version + 1) where id=%s",
							TABLE_NAME, acc.id()),
					String.format("select LAST_INSERT_ID()"), };

			try (Connection conn = db.getConn()) {
				try (Statement stmt = conn.createStatement()) {
					if (stmt.executeUpdate(sqls[0]) != 1) {
						throw new DBException(sqls[0]);
					}

					if (!stmt.execute(sqls[1])) {
						throw new DBException(sqls[1]);
					}

					ResultSet rs = stmt.getResultSet();
					if (!rs.next()) {
						throw new DBException("not resultSet return");
					}
					int new_ver = rs.getInt(1);
					// LOG.error("ver:{}", new_ver);
					DefaultAccount dac = new DefaultAccount(acc);
					dac._infover = new_ver;
					return dac;
				}

			} catch (SQLException e) {
				throw new DBException(e);
			}
		}

		public static final String TABLE_RELATE_NAME = "user_relate";

		@Override
		public boolean makePair(IAccount acc_from, IAccount acc_to) {
			return makePair(acc_from, acc_to, "");
		}

		@Override
		public boolean makePair(IAccount acc_from, IAccount acc_to,
				String ask_msg) {

			Preconditions.checkArgument(acc_from.id() != acc_to.id());

			IAccount user_a = acc_from.id() < acc_to.id() ? acc_from : acc_to;
			IAccount user_b = acc_from.id() > acc_to.id() ? acc_from : acc_to;
			String a_ask_b = null;
			String b_ask_a = null;
			if (acc_from == user_a) {
				a_ask_b = ask_msg;
			} else {
				b_ask_a = ask_msg;
			}

			String sql_relate = String
					.format("select * from %s where user_id_a=%s and user_id_b=%s for update",
							TABLE_RELATE_NAME, user_a.id(), user_b.id());

			MysqlDB db = new MysqlDB(DB_NAME);
			try (Connection conn = db.getConn()) {
				try (Statement stmt = conn.createStatement()) {
					try (ResultSet rs = stmt.executeQuery(sql_relate)) {

						UserRelate replace_data = null;
						String sql_replace = null;
						if (!rs.next()) {
							// throw new NotExistException(sql);
							// build to db
							UserRelate.Builder builder = UserRelate
									.newBuilder().setUseridA(user_a.id())
									.setUseridB(user_b.id()).setABPair(false);
							if (a_ask_b != null) {
								builder.setAAskB(a_ask_b);
							}
							if (b_ask_a != null) {
								builder.setBAskA(b_ask_a);
							}
							replace_data = builder.build();
							sql_replace = String
									.format("insert into `%s` (user_id_a, user_id_b, a_ask_b, b_ask_a, a_b_pair) values (%s, %s, '%s', '%s', %s)",
											TABLE_RELATE_NAME,
											replace_data.getUseridA(),
											replace_data.getUseridB(),
											replace_data.getAAskB() == null ? "[NULL]"
													: replace_data.getAAskB(),
											replace_data.getBAskA() == null ? "[NULL]"
													: replace_data.getBAskA(),
											replace_data.getABPair() ? "1"
													: "0");

							if (stmt.executeUpdate(sql_replace) != 1) {
								throw new DBException(sql_replace);
							}
							return false;

						} else {
							//
							replace_data = UserRelate
									.newBuilder()
									.setUseridA(rs.getLong("user_id_a"))
									.setUseridB(rs.getLong("user_id_b"))
									.setAAskB(
											a_ask_b != null ? a_ask_b : rs
													.getString("a_ask_b"))
									.setBAskA(
											b_ask_a != null ? b_ask_a : rs
													.getString("b_ask_a"))
									.setABPair(rs.getBoolean("a_b_pair"))
									.build();

							if (replace_data.getABPair()) {
								LOG.error("dup make paire?");
								return true;
							}

							boolean is_pair = replace_data.getABPair();

							if (replace_data.getAAskB() != null
									&& replace_data.getBAskA() != null) {
								is_pair = true;
							}

							sql_replace = String
									.format("update `%s` set a_ask_b='%s', b_ask_a='%s', a_b_pair=%s where id=%s",
											TABLE_RELATE_NAME, !replace_data
													.hasAAskB() ? "[NULL]"
													: replace_data.getAAskB(),
											!replace_data.hasBAskA() ? "[NULL]"
													: replace_data.getBAskA(),
											is_pair ? "1" : "0", rs
													.getLong("id"));

							if (stmt.executeUpdate(sql_replace) != 1) {
								throw new DBException(sql_replace);
							}
							return is_pair;
						}

					}
				}

			} catch (SQLException e) {
				throw new DBException(sql_relate, e);
			}

		}

		@Override
		public boolean isPair(IAccount acc_aa, IAccount acc_bb) {

			IAccount user_a = acc_aa.id() < acc_bb.id() ? acc_aa : acc_bb;
			IAccount user_b = acc_aa.id() > acc_bb.id() ? acc_aa : acc_bb;

			String sql_relate = String
					.format("select a_b_pair from %s where user_id_a=%s and user_id_b=%s ",
							TABLE_RELATE_NAME, user_a.id(), user_b.id());

			MysqlDB db = new MysqlDB(DB_NAME);
			try (Connection conn = db.getConn()) {
				try (Statement stmt = conn.createStatement()) {
					try (ResultSet rs = stmt.executeQuery(sql_relate)) {
						if (rs.next()) {
							return rs.getBoolean("a_b_pair");
						}
					}
				}
			} catch (SQLException e) {
				throw new DBException(sql_relate, e);
			}

			return false;
		}

		@Override
		public void unPair(IAccount acc_aa, IAccount acc_bb) {
			IAccount user_a = acc_aa.id() < acc_bb.id() ? acc_aa : acc_bb;
			IAccount user_b = acc_aa.id() > acc_bb.id() ? acc_aa : acc_bb;

			String sql_relate = String.format(
					"delete from %s where user_id_a=%s and user_id_b=%s ",
					TABLE_RELATE_NAME, user_a.id(), user_b.id());

			MysqlDB db = new MysqlDB(DB_NAME);
			try (Connection conn = db.getConn()) {
				try (Statement stmt = conn.createStatement()) {
					stmt.executeUpdate(sql_relate);
				}
			} catch (SQLException e) {
				throw new DBException(sql_relate, e);
			}
		}

		@Override
		public Optional<String> getPairAskMsg(IAccount acc_from, IAccount acc_to) {
			IAccount user_a = acc_from.id() < acc_to.id() ? acc_from : acc_to;
			IAccount user_b = acc_from.id() > acc_to.id() ? acc_from : acc_to;

			String tap = "a_ask_b";

			if (user_a == acc_to) {
				tap = "b_ask_a";
			}

			String sql_relate = String.format(
					"select %s from %s where user_id_a=%s and user_id_b=%s ",
					tap, TABLE_RELATE_NAME, user_a.id(), user_b.id());

			MysqlDB db = new MysqlDB(DB_NAME);
			try (Connection conn = db.getConn()) {
				try (Statement stmt = conn.createStatement()) {
					try (ResultSet rs = stmt.executeQuery(sql_relate)) {
						if (rs.next()) {
							return Optional.fromNullable(rs.getString(tap));
						}
					}
				}
			} catch (SQLException e) {
				throw new DBException(sql_relate, e);
			}

			return Optional.absent();
		}

		@Override
		public Collection<IAccount> getPairsList(IAccount acc) {

			Set<Long> ids = Sets.newTreeSet();
			List<IAccount> accs = Lists.newArrayList();
			String sql_user = String
					.format("select user_id_a, user_id_b, a_b_pair from %s where user_id_a=%s or user_id_b=%s",
							TABLE_RELATE_NAME, acc.id(), acc.id());
			MysqlDB db = new MysqlDB(DB_NAME);
			try (Connection conn = db.getConn()) {
				try (Statement stmt = conn.createStatement()) {
					try (ResultSet rs = stmt.executeQuery(sql_user)) {
						while (rs.next()) {
							if (rs.getBoolean("a_b_pair")) {
								Long nid = rs.getLong("user_id_a");
								if (nid.longValue() != acc.id()) {
									ids.add(nid);
								}
								nid = rs.getLong("user_id_b");
								if (nid.longValue() != acc.id()) {
									ids.add(nid);
								}
							}
						}
					}

					if (ids.size() == 0) {
						return accs;
					}

					String id_join = Joiner.on(",").join(ids);
					String sql_acc = String.format(
							"select * from %s where id in (%s)", TABLE_NAME,
							id_join);
					// LOG.error("sql_acc:{}", sql_acc);
					try (ResultSet rs = stmt.executeQuery(sql_acc)) {
						while (rs.next()) {
							accs.add(initAccWithResultSet(
									new DefaultAccount(rs.getLong("id")), rs));
						}
					}
				}
			} catch (SQLException e) {
				throw new DBException(e);
			}

			return accs;
		}

		@Override
		public Collection<IAccount> getByPhoneId(Collection<String> phones) {

			String phone_join = Joiner.on(",").join(phones);

			String sql = String.format(
					"select * from %s where phone_id in (%s) ", TABLE_NAME,
					phone_join);

			List<IAccount> accs = Lists.newArrayList();

			MysqlDB db = new MysqlDB(DB_NAME);
			try (Connection conn = db.getConn()) {
				try (Statement stmt = conn.createStatement()) {
					try (ResultSet rs = stmt.executeQuery(sql)) {
						while (rs.next()) {
							accs.add(transform(rs));
						}
					}
				}
			} catch (SQLException e) {
				throw new DBException(sql, e);
			}

			return accs;
		}

		static UserRelate transform(UserRelate.Builder builder, ResultSet rs)
				throws SQLException {

			String a_ask_b = rs.getString("a_ask_b");
			String b_ask_a = rs.getString("b_ask_a");

			if (a_ask_b != null) {
				builder.setAAskB(a_ask_b);
			}
			if (b_ask_a != null) {
				builder.setBAskA(b_ask_a);
			}

			return builder.setUseridA(rs.getLong("user_id_a"))
					.setUseridB(rs.getLong("user_id_b"))
					.setABPair(rs.getBoolean("a_b_pair")).build();
		}

		@Override
		public Collection<UserRelate> getPairRelate(IAccount acc) {

			List<UserRelate> relates = Lists.newArrayList();

			String sql_user = String.format(
					"select * from %s where user_id_a=%s or user_id_b=%s",
					TABLE_RELATE_NAME, acc.id(), acc.id());

			MysqlDB db = new MysqlDB(DB_NAME);
			try (Connection conn = db.getConn()) {
				try (Statement stmt = conn.createStatement()) {
					try (ResultSet rs = stmt.executeQuery(sql_user)) {
						while (rs.next()) {
							relates.add(transform(UserRelate.newBuilder(), rs));
						}
					}
				}
			} catch (SQLException e) {
				throw new DBException(sql_user, e);
			}

			return relates;
		}

		@Override
		public IAccount rebuildToken(IAccount acc) {

			String newToken = newLoginToken(acc.id());

			String sql = String.format(
					"update %s set request_token='%s' where id=%s", TABLE_NAME,
					newToken, acc.id());

			MysqlDB db = new MysqlDB(DB_NAME);
			try (Connection conn = db.getConn()) {
				try (Statement stmt = conn.createStatement()) {
					if (stmt.executeUpdate(sql) != 1) {
						throw new NotExistException("userid:" + acc.id());
					}
				}
			} catch (SQLException e) {
				throw new DBException(sql, e);
			}

			DefaultAccount acc0 = (DefaultAccount) acc;
			acc0._request_token = newToken;

			return acc0;
		}

		@Override
		public IAccount registerWith3RD(String otherid, String accessToken,
				Platform3RD ptype) {

			String dbkey = ptype.dbKey();

			String sql = String
					.format("insert into `%s` (%s, request_token) select * from (select %s, %s) AS tmp where not exists (select %s from `%s` where %s=%s) limit 1",
							TABLE_NAME, dbkey, otherid, accessToken,
							dbkey, TABLE_NAME, dbkey, otherid);

			MysqlDB db = new MysqlDB(DB_NAME);

			try (Connection conn = db.getConn()) {
				try (Statement stmt = conn.createStatement()) {
					if (stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS) != 1) {
						throw new DupRegException("dup register? " + otherid
								+ " Platform3RD:" + ptype);
					}
					try (ResultSet rs = stmt.getGeneratedKeys()) {
						if (!rs.next()) {
							throw new NotExistException(sql);
						}

						DefaultAccount acc = new DefaultAccount(rs.getLong(1));

						switch (ptype) {
						case WEIBO:
							acc._weiboid = otherid;
							break;
						case QQ:
							acc._qqid = otherid;
							break;
						case WEIXIN:
							acc._weixinid = otherid;
							break;
						default:
							throw new UnsupportedMessageTypeException("ptype: " + ptype);
						}
						acc._infover = 0;
						acc._request_token = accessToken;
						return acc;
					}

				}
			} catch (SQLException e) {
				throw new DBException(sql, e);
			}
		}

		@Override
		public Optional<IAccount> getByPlatform3RD(String phoneid,
				Platform3RD ptype) {
			String sql = String.format("select * from %s where %s=%s ",
					TABLE_NAME, ptype.dbKey(), phoneid);

			MysqlDB db = new MysqlDB(DB_NAME);
			try (Connection conn = db.getConn()) {
				try (Statement stmt = conn.createStatement()) {
					try (ResultSet rs = stmt.executeQuery(sql)) {
						if (!rs.next()) {
							return Optional.absent();
						}

						return Optional.of(transform(rs));
					}
				}
			} catch (SQLException e) {
				throw new DBException(sql, e);
			}

		}

	}
}
