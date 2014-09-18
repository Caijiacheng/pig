package com.mm.photo.storage;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.fusesource.leveldbjni.JniDBFactory;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;

import com.google.common.collect.Maps;

public class LevelDB {
	static private LevelDB handle = new LevelDB();
	public static LevelDB ins()
	{
		return handle;
	}
	
	
	static Options defaultOptions = new Options();
	static
	{
		defaultOptions.createIfMissing(true)
		.blockSize(4 * 1024 * 1024).
		cacheSize(128 * 1024 * 1024);
	}
	

	Map<String, DB> nameDBMap = Maps.newTreeMap();
	
	private LevelDB(){};
	
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
	
	
}
