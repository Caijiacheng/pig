package com.mm.account.data;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import com.mm.account.db.RedisDB;
import com.mm.account.error.NotExistException;
import com.mm.account.error.UnknowAccException;


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
	
	DefaultPhoto(String file) {
		in = Files.asByteSource(new File(file));
	}
	
	DefaultPhoto(File f) {
		in = Files.asByteSource(f);
	}
	
	public DefaultPhoto(byte[] bytes)
	{
		in = ByteSource.wrap(bytes);
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

	static public class StreamPhoto implements IPhoto
	{

		IUrl url;
		public StreamPhoto(IUrl url) {
			this.url = url;
		}
		
		@Override
		public InputStream data() {
			return new RedisStream(((Url)url).reskey);
		}
		
	}
	
	static public class RedisStream extends InputStream
	{
		
		Jedis handle;
		static final int maxsize = 16384;
		byte[] hold = null;
		int offset = 0;
		int next = 0; 
		byte[] key_stream;
		
		
		public RedisStream(byte[] key_stream) {
			handle = new RedisDB().getConn();
			this.key_stream = key_stream;
		}
		
		@Override
		public int read() throws IOException {
			if (hold == null || next >= hold.length )
			{
				if (hold != null && hold.length < maxsize)
				{
					return -1;//end
				}
				hold = handle.getrange(key_stream, offset, offset + maxsize);
				offset = offset + hold.length;
				next = 0;
			}
			return hold[next++];
		}
		@Override
		public void close() throws IOException {
			handle.close();
			super.close();
		}
		
		
	}
	
	static class Url implements IUrl
	{

		Url(byte[] key)
		{
			reskey = key;
		}
		byte[] reskey;
		@Override
		public String url() {
			return new String(reskey, Charsets.UTF_8);
		}
	}
	
	static public class Service implements IPhotoService
	{
		
		
		static Logger LOG = LoggerFactory.getLogger(Service.class);
		
		static String downloadDir = "./downlaod/";
		public Service() {
			File baseDir = new File(downloadDir);
			if (!baseDir.exists())
			{
				baseDir.mkdirs();
			}
			
		}
		
		String getIncrPhotoKey(long userid)
		{
			return String.format("DefaultPhoto.Service:%s:PhotoCnt", userid);
		}
		
		byte[] getPhotoIDKey(long userid, long photoid)
		{
			return String.format("DefaultPhoto.Service:%s:Photo:%s", userid, photoid).getBytes(Charsets.UTF_8);
		}
		
		@Override
		public IUrl upload(IPhoto photo) {
			
			DefaultPhoto ph = (DefaultPhoto)photo;
		
			
			RedisDB db = new RedisDB();
			try(Jedis handle = db.getConn())
			{
				Long photoid = handle.incr(getIncrPhotoKey(ph.userid));
				
				byte[] b = new byte[16384];
				InputStream in = ph.data();
				
				byte[] photokey = getPhotoIDKey(ph.userid, photoid);
				while(true){
					int num = in.read(b);
					if (num < 0 )
					{
						break;
					}
					byte[] bb = new byte[num];
					System.arraycopy(b, 0, bb, 0, num);
//					LOG.debug("append .. {}", num);
					handle.append(photokey, bb);
				}
				return new Url(photokey);
			} catch (IOException e) {
				throw new UnknowAccException(e);
			}
		}

		
		@Override
		public IPhoto download(IUrl url) {
			
			return new StreamPhoto(url);
//			Url u = (Url)url;
//			
//			File f = new File(downloadDir + u.url());
//			ByteSink sink = Files.asByteSink(f);
//			
//			RedisDB db = new RedisDB();
//			int bytes = 16384;
//			try(Jedis handle = db.getConn())
//			{
//				try(OutputStream ots = sink.openBufferedStream())
//				{
//					int begin = 0;
//					while(true)
//					{
//						byte[] bb = handle.getrange(u.reskey, begin, begin + bytes);
//						ots.write(bb);
//						begin += bb.length;
////						LOG.debug("getrange ..{}", bb.length);
//						if (bb.length < bytes)
//						{
//							return new DefaultPhoto(f);
//						}
//					}
//				}
//				
//			} catch (IOException e) {
//				throw new UnknowAccException(e);
//			}
		}
		
	}


	
}
