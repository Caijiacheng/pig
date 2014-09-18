package com.mm.photo.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import junit.framework.Assert;

import org.iq80.leveldb.DB;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mm.photo.proto.Storage.ImageKey;



public class TestLevelDBPhoto {

	
	  public static String bytesToString(byte[] data) {
          char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
                          'e', 'f'};
          char[] temp = new char[data.length * 2];
          for (int i = 0; i < data.length; i++) {
                  byte b = data[i];
                  temp[i * 2] = hexDigits[b >>> 4 & 0x0f];
                  temp[i * 2 + 1] = hexDigits[b & 0x0f];
          }
          return new String(temp);

	  }
	
	public String getMd5(InputStream fis) throws NoSuchAlgorithmException, IOException
	{
		   MessageDigest md = MessageDigest.getInstance("MD5");
           byte[] buffer = new byte[8192];
           int length = -1;
           while ((length = fis.read(buffer)) != -1) {
                   md.update(buffer, 0, length);
           }
           return bytesToString(md.digest());
	}
	
	
	static Logger LOG = LoggerFactory.getLogger(TestLevelDBPhoto.class);
	
	@Test
	public void testBuilder() throws IOException, NoSuchAlgorithmException
	{
		
		Long[] test_length = new Long[]{1024L, 4096L, 128 * 1024L, 1 * 1024 * 1024L, 10 * 1024 * 1024L, 30 * 1024 * 1024L};
		
		DB db = LevelDB.ins().getDB("temp");
		
		byte[] chunk = new byte[1024];
		for (int i = 0 ; i<chunk.length; i++)
		{
			chunk[i] = (byte) (i);
		}
		
		for (Long l : test_length)
		{
			
			LOG.error("test file : {}", l);
			
			File tmp_file = 
					Files.createTempFile("leveldb_tmp", null, new FileAttribute<?>[]{}).toFile();
			OutputStream file = new FileOutputStream(tmp_file);
			long begin = 0;
			int bytes = 0;
			
			while (begin < l)
			{
				bytes = (int) (l - begin > chunk.length ? chunk.length : l - begin);
				file.write(chunk, 0, bytes);
				begin = begin + bytes;
			}
			file.close();
			
			LevelDBFile.Builder builder = 
					new LevelDBFile.Builder(db, 
							ImageKey.newBuilder().setUrl(tmp_file.getName()));

			LevelDBFile photo;
			if (l < 16 * 1024L)
			{
				builder.writeFrom(new FileInputStream(tmp_file));
				photo = builder.build();
				Assert.assertFalse(photo.getKey().getIsSplit());
			}else
			{
				try(FileInputStream fis = new FileInputStream(tmp_file))
				{
					byte[] o_datas = new byte[16*1024];
					while(true)
					{
						int len = fis.read(o_datas);
						if (len == -1)
						{
							break;
						}
						try(OutputStream out = builder.openStream())
						{
							out.write(o_datas, 0, len);
						}
						if (len < o_datas.length)
						{
							break;
						}
					}
					photo = builder.build();
					Assert.assertTrue(photo.getKey().getIsSplit());
				}
			}
			Assert.assertEquals(getMd5(new FileInputStream(tmp_file)),
					getMd5(photo.openStream()));
			
			LevelDBFile dupOpen = 
					new LevelDBFile(db, photo.getKey());
			
			Assert.assertEquals(getMd5(dupOpen.openStream()),
					getMd5(photo.openStream()));
		}
	}
	
	@Test
	public void testOpenNotExist()
	{
		
	}
	
}
