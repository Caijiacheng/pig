package com.mm.account.data;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;


@Deprecated
@Ignore
public class TestDefaultPhoto {

	
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
	
	String test_file_name = "src/test/resources/aio1j.html";
	
	@Test
	public void testAssertFileIsExist()
	{
		String fname = test_file_name;
		File f = new File(fname);
		
		Assert.assertTrue(f.exists());
	}
	
	@Test
	public void testDefaultPhoto() throws IOException, NoSuchAlgorithmException
	{
		IPhotoService service = new DefaultPhoto.Service();
		
		
		DefaultPhoto raw_photo = new DefaultPhoto(test_file_name);
		
		IUrl url  = service.upload(raw_photo);
		
		IPhoto db_photo = service.download(url);

		String raw_md5;
		try(InputStream fis = raw_photo.data())
		{
			raw_md5 = getMd5(fis);
		}
		
		String db_md5;
		try(InputStream fis = db_photo.data())
		{
			db_md5 = getMd5(fis);
		}
		
		Assert.assertEquals(raw_md5, db_md5);
		
	}

}
