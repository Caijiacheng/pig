package com.mm.account.db;

import java.sql.Connection;
import java.sql.SQLException;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import redis.clients.jedis.Jedis;

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
		try(Connection conn = db.getConn())
		{
			Assert.assertNotNull(conn);
		}
		
		Connection share_conn = db.getSharedConn();
		Connection share_conn_1 = db.getSharedConn();
		
		Assert.assertEquals(share_conn, share_conn_1);
	}
	
	@Test
	public void testRedisDB() 
	{
		RedisDB db = new RedisDB();
		Jedis handle = db.getConn();
		
		Assert.assertNotNull(handle);
		
		Jedis share_conn = db.getSharedConn();
		Jedis share_conn_1 = db.getSharedConn();
		Assert.assertEquals(share_conn, share_conn_1);		
		
	}

}
