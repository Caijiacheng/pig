package com.mm.photo.storage;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.fusesource.leveldbjni.JniDBFactory;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

public class LevelDB {
	static private LevelDB handle = new LevelDB();
	public static LevelDB ins()
	{
		return handle;
	}
	
	static Logger LOG = LoggerFactory.getLogger(LevelDB.class);
	static Options defaultOptions = new Options();
	static String defaultDbName = "temp";
	static
	{
		Properties prop = new Properties();
		try {
			prop.load(ClassLoader.getSystemResourceAsStream("release.propertis"));
		} catch (IOException e) {
			LOG.error("Config Load Failed!");
		}
		defaultOptions.createIfMissing(true)
		.blockSize(Integer.parseInt(
				prop.getProperty("leveldb.block.size", String.valueOf(16384)))).
		cacheSize(Integer.parseInt(
				prop.getProperty("leveldb.cache.size", String.valueOf(128 * 1024 * 1024))));
		defaultDbName = prop.getProperty("leveldb.default.db.name", defaultDbName);
	}
	

	Map<String, DB> nameDBMap = Maps.newTreeMap();
	
	private LevelDB(){};
	
	
	synchronized public DB getDB()
	{
		return getDB(defaultDbName, defaultOptions);
	}
	
	synchronized public DB getDB(String name)
	{
		return getDB(name, defaultOptions);
	}
	
	synchronized public DB getDB(String name, Options option)
	{
		DB db = nameDBMap.get(name);
		if (db == null)
		{
			try {
				db = JniDBFactory.factory.open(new File(name), option);
			} catch (IOException e) {
				throw new RuntimeException("can't open DB:" + name);
			};
			nameDBMap.put(name, db);
		}
		return db;
	}
	
	synchronized void close(DB db)
	{
		for (Entry<String, DB> ent : nameDBMap.entrySet())
		{
			try {
				if (ent.getValue() == db)
				{
					db.close();
					nameDBMap.remove(ent.getKey());
					break;
				}
				
			} catch (IOException e) {
			}
		}
	}
	
	synchronized void close()
	{
		for (DB db : nameDBMap.values())
		{
			try {
				db.close();
			} catch (IOException e) {
			}
		}
	}
	
	public void cleanDB(DB db)
	{
		DBIterator iter = db.iterator();
		iter.seekToFirst();
		while(iter.hasNext())
		{
			Map.Entry<byte[], byte[]> entry = iter.next();
			db.delete(entry.getKey());
		}
	}
	
	
}
