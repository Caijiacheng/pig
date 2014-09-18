package com.mm.photo.storage;

import java.io.IOException;
import java.io.InputStream;

import org.iq80.leveldb.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteSource;
import static org.fusesource.leveldbjni.JniDBFactory.bytes;

public class LevelDBData extends ByteSource implements IData{
	
	
	static Logger LOG = LoggerFactory.getLogger(LevelDBData.class);
	
	DB db;
	byte[] key;
	ByteSource wrapValue;
	public LevelDBData(DB db, byte[] key) {
		this.db = db;
		this.key = key;
	}
	
	public LevelDBData(DB db, String key)
	{
		this(db, bytes(key));
	}
	
	@Override
	public InputStream openStream() throws IOException {
		
		if (wrapValue == null)
		{
//			LOG.error("db.get:{}", key);
			wrapValue = wrap(db.get(key));
		}
		return wrapValue.openStream();
	}
}
