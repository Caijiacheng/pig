package com.mm.account.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;

import com.mm.util.db.AbsDBHandle;

public class TestDB {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testMysqlDB() throws SQLException {
		MysqlDB db = new MysqlDB("test");
		try (Connection conn = db.getConn()) {
			Assert.assertNotNull(conn);
			Assert.assertTrue(!conn.isClosed());
		}

		Connection conn1 = db.getConn();
		conn1.close();
		Assert.assertTrue(conn1.isClosed());

	}

	@Test(expected = Throwable.class)
	public void testMysqlDBNotInSharedConnMode() throws SQLException {
		MysqlDB db = new MysqlDB("test");
		try (Connection conn = db.getSharedConn()) {
			Assert.assertTrue(false);
		}
	}

	static Connection tmp_conn;

	@Test
	public void testMysqlDBInShareMode() throws SQLException {

		Runnable r = new Runnable() {
			@Override
			public void run() {
				MysqlDB db = new MysqlDB("test");
				Connection conn = new MysqlDB("test").getSharedConn();
				Connection conn1 = new MysqlDB("test").getConn();
				Assert.assertEquals(conn, conn1);
				Connection conn11 = new MysqlDB("information_schema").getConn();
				Assert.assertNotSame(conn, conn11);
				try (Connection conn2 = db.getConn()) {
					conn2.close();
					Assert.assertTrue(!conn2.isClosed());
				} catch (SQLException e1) {
				}
				tmp_conn = conn1;
				try {
					Assert.assertTrue(!conn.isClosed());
					Assert.assertTrue(!conn1.isClosed());
				} catch (SQLException e) {
				}
			}
		};

		AbsDBHandle.decorateShareConnModeRunnable(r).run();

		Assert.assertTrue(tmp_conn.isClosed());
	}

	@Test
	public void testRedisDB() {

		RedisDB db = new RedisDB();

		try (Jedis conn = db.getConn()) {
			Assert.assertNotNull(conn);
			Assert.assertTrue(conn.isConnected());
		}

		Jedis conn1 = db.getConn();
		conn1.close();
		Assert.assertTrue(!conn1.isConnected());

	}

	@Test(expected = Throwable.class)
	public void testRedisDBInShareMode() throws SQLException {
		RedisDB db = new RedisDB();
		try (Jedis conn = db.getSharedConn()) {
			Assert.assertTrue(false);
		}
	}

	static Jedis tmp_jedis;;

	@Test
	public void testRedisDBInShareModeRunnable() throws SQLException {

		Runnable r = new Runnable() {
			@Override
			public void run() {
				RedisDB db = new RedisDB();
				Jedis conn = new RedisDB().getSharedConn();
				Jedis conn1 = new RedisDB().getConn();
				Assert.assertEquals(conn, conn1);

				conn.close();
				conn1.close();
				Assert.assertTrue(conn.isConnected());
				Assert.assertTrue(conn1.isConnected());

				try (Jedis conn2 = db.getConn()) {
					conn2.close();
					Assert.assertTrue(conn2.isConnected());
				}
				tmp_jedis = conn1;
			}
		};

		AbsDBHandle.decorateShareConnModeRunnable(r).run();
		Assert.assertTrue(!tmp_jedis.isConnected());
	}

	static Logger LOG = LoggerFactory.getLogger(TestDB.class);

	static class TestThreadPoolExecutor extends ThreadPoolExecutor {

		public TestThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
				long keepAliveTime, TimeUnit unit,
				BlockingQueue<Runnable> workQueue) {

			super(corePoolSize, maximumPoolSize, keepAliveTime, unit,
					workQueue,
					new AbsDBHandle.DecorateShareConnModeThreadFactory());
		}

	}

	
	static AtomicInteger done_cnt = new AtomicInteger(0);
	static Jedis thread_share_conn = null;;
	@Test
	public void testThreadMemLeak() throws InterruptedException {
		ExecutorService service = new TestThreadPoolExecutor(0, 1, 2,
				TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
		// new SynchronousQueue<Runnable>());

		int cnt = 10;
		
		for (int i = 0; i < cnt; i++) {
			service.execute(new Runnable() {

				@Override
				public void run() {
					Jedis share_conn = new RedisDB().getConn();
					if (thread_share_conn == null)
					{
						thread_share_conn = share_conn;
					}else
					{
						if (!thread_share_conn.equals(share_conn))
						{
							LOG.error("error here");
						}
					}
					
					share_conn.ping();
					LOG.error("ping share_conn: {}", share_conn);
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
					}
					done_cnt.incrementAndGet();
				}
			});
		}

		for (int i = 0; i<cnt; i++)
		{
			if (done_cnt.get() != cnt)
			{
				Thread.sleep(30);
			}
		}
		Assert.assertEquals(done_cnt.get(), cnt);
		Assert.assertTrue(thread_share_conn.isConnected());
		service.awaitTermination(3, TimeUnit.SECONDS);
		
		Assert.assertTrue(!thread_share_conn.isConnected());


	}

}
