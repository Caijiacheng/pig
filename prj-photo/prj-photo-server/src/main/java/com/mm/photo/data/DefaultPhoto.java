package com.mm.photo.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.iq80.leveldb.DB;

import com.mm.photo.proto.Storage.ImageKey;
import com.mm.photo.storage.LevelDB;
import com.mm.photo.storage.LevelDBFile;

public class DefaultPhoto implements IPhoto{

	static DB db;
	static
	{
		db = LevelDB.ins().getDB();
	}
	
	String uniqname;
	
	LevelDBFile file;
	
	public DefaultPhoto(String uniqname) {
		this.uniqname = uniqname;
		file = new LevelDBFile(
				db, ImageKey.newBuilder().setUrl(uniqname).build());
	}
	
	
	static public class Service implements IPhotoService
	{
		@Override
		public boolean isExist(IUrl url) {
			return new DefaultPhoto(url.url()).isExist();
		}

		@Override
		public IPhoto get(IUrl url)
		{
			return new DefaultPhoto(url.url());
		}
		
		@Override
		public OutputStream getPhotoOutput(IUrl url) throws IOException {
			return new DefaultPhoto(url.url()).file.newBuilder().openStream();
		}

		@Override
		public InputStream getPhotoInput(IUrl url) throws IOException {
			return new DefaultPhoto(url.url()).data();
		}
	}


	@Override
	public InputStream data() throws IOException 
	{
			return file.openStream();
	}


	@Override
	public String uniqname() {
		return uniqname;
	}


	@Override
	public boolean isExist() {
		return file.isExist();
	}


	@Override
	public long length() {
		return file.length();
	}
	

	

}
