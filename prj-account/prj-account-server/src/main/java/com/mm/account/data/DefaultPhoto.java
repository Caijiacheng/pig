package com.mm.account.data;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.restexpress.exception.NotFoundException;

import redis.clients.jedis.Jedis;

import com.google.common.base.Preconditions;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteSource;
import com.mm.account.db.RedisDB;
import com.mm.account.error.NotExistException;


/**
 *  
 *  简单store到redis中去, only for test.
 *  mac上安装fastdfs比较麻烦,为了保证这个服务的可用性,先暂时存到redis中去
 *  
 * @author apple
 *
 */

public class DefaultPhoto implements IPhoto {
	
	ByteSource in;

	long userid;
	
	public DefaultPhoto() {
	}
	
	@Override
	public InputStream data() {
		try {
			Preconditions.checkNotNull(in);
			return in.openStream();
		} catch (IOException e) {
			throw new NotExistException(in.toString());
		}
	}

	
	static class Url implements IUrl
	{

		@Override
		public String url() {
			return null;
		}
	}
	
	static public class Service implements IPhotoService
	{

		String getIncrPhotoKey(long userid)
		{
			return String.format("DefaultPhoto.Service:%s:PhotoCnt", userid);
		}
		
		String getPhotoIDKey(long userid, long photoid)
		{
			return String.format("DefaultPhoto.Service:%s:Photo:%s", userid, photoid);
		}
		
		@Override
		public IUrl upload(IPhoto photo) {
			
			DefaultPhoto ph = (DefaultPhoto)photo;
			
			RedisDB db = new RedisDB();
			try(Jedis handle = db.getConn())
			{
				Long photoid = handle.incr(getIncrPhotoKey(ph.userid));
				
			}
			
			return null;
		}

		@Override
		public IPhoto download(IUrl url) {
			
			return null;
		}
		
	}


	
}
