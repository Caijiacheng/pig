package com.mm.photo.netty;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import junit.framework.Assert;

import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;

public class TestPhotoServer {

	
	static Logger LOG = LoggerFactory.getLogger(TestPhotoServer.class);
	
	HttpPhotoServer server;
	
	
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
	
	@Before
	public void setup() 
	{
		server = new HttpPhotoServer(SRV_PORT).run();
	}
	
	@After
	public void teardown()
	{
		server.shutdown();
	}
	
	
	final static int SRV_PORT = 10001;
	final static String HTTP_LOCALHOST = "http://127.0.0.1:" + SRV_PORT;

	
	
	static class HttpClientPostFile implements Runnable
	{
		String uri;
		String method = "POST";
		HttpURLConnection conn;
		File f;

		public HttpClientPostFile(String uri, File f) {
			this.uri = uri;
			this.f = f;
		}

		
		int rescode = -1;
		public int getResCode()
		{
			return rescode;
		}
		@Override
		public void run()  {
			  CloseableHttpClient httpclient = HttpClients.createDefault();
		        try {
		            HttpPost httppost = new HttpPost(this.uri);

		            File file = f;

		            FileEntity entity = new FileEntity(file, ContentType.APPLICATION_OCTET_STREAM);

		            httppost.setEntity(entity);

		            LOG.info("Executing request: " + httppost.getRequestLine());
		            CloseableHttpResponse response = httpclient.execute(httppost);
		            try {
		            	LOG.info("----------------------------------------");
		            	LOG.info("{}",response.getStatusLine());
		            	rescode = response.getStatusLine().getStatusCode();
//		                EntityUtils.consume(response.getEntity());
		            }  finally {
		                try {
							response.close();
						} catch (IOException e) {
						}
		            }
		        } catch (IOException e1) {
		        	throw new RuntimeException(e1);
				} finally {
		            try {
						httpclient.close();
					} catch (IOException e) {
					}
		        }
			
		}

	}
	
	static class HttpClientGetFile implements Runnable {
		String uri;
		String method = "GET";
		HttpURLConnection conn;

		File download_file;
		
		public HttpClientGetFile(String uri) {
			this.uri = uri;
		}

		public HttpClientGetFile(String url, String method) {
			this(url);
			this.method = method;
		}

		
		public File getDownloadFile()
		{
			Preconditions.checkNotNull(download_file);
			return download_file;
		}
		
		int rescode = -1;
		int getResponseCode()
		{
			return rescode;
		}
		@Override
		public void run() {
			URL url;
			try {
				url = new URL(uri);

				conn = (HttpURLConnection) url
						.openConnection();
				conn.setRequestMethod(method);
				conn.setDoOutput(true);
				conn.setReadTimeout(0);
				conn.connect();

				if (conn.getResponseCode() == 200) {
					download_file = 
							Files.createTempFile("http_tmp", null, new FileAttribute<?>[]{}).toFile();
					
					ByteStreams.copy(conn.getInputStream(), new FileOutputStream(download_file));
				}
				rescode = conn.getResponseCode();
				LOG.info("get the Response from: {}, ResCode:{}, RetMsg:{}",
						url.toString(), conn.getResponseCode(),
						conn.getResponseMessage());

			} catch (Exception e) {
				LOG.error("", e);
			}

		}
	}
	
	
	@Test
	public void testNull()
	{
		Assert.assertTrue(true);
	}
	@Test
	public void testOneUploadAndDownload() throws IOException, NoSuchAlgorithmException
	{
		File tmp = getTmpFile(100L);
		
		String url = HTTP_LOCALHOST + "/test/" + tmp.getName();
		
		
		HttpClientPostFile post_ok = new HttpClientPostFile(url, tmp);
		post_ok.run();
		Assert.assertEquals(post_ok.getResCode(), HttpStatus.SC_OK);

		
		HttpClientPostFile post_failed = new HttpClientPostFile(url, tmp);
		
		post_failed.run();
		
		Assert.assertEquals(post_failed.getResCode(), HttpStatus.SC_NOT_ACCEPTABLE);
		
		HttpClientGetFile downGet = new HttpClientGetFile(url);
		
		downGet.run();
		Assert.assertEquals(downGet.getResponseCode(), HttpStatus.SC_OK);
		Assert.assertEquals(getMd5(new FileInputStream(tmp)), getMd5(new FileInputStream(downGet.getDownloadFile())));
		
		HttpClientGetFile downGet_failed = new HttpClientGetFile(url + "212323");
		downGet_failed.run();
		Assert.assertEquals(downGet_failed.getResponseCode(), HttpStatus.SC_NOT_FOUND);

	}
	
	File getTmpFile(long size) throws IOException
	{
		
		byte[] chunk = new byte[1024];
		for (int i = 0 ; i<chunk.length; i++)
		{
			chunk[i] = (byte) (i);
		}
		
		File tmp_file = 
				Files.createTempFile("http_tmp", null, new FileAttribute<?>[]{}).toFile();
		OutputStream file = new FileOutputStream(tmp_file);
		long begin = 0;
		int bytes = 0;
		
		while (begin < size)
		{
			bytes = (int) (size - begin > chunk.length ? chunk.length : size - begin);
			file.write(chunk, 0, bytes);
			begin = begin + bytes;
		}
		file.close();
		return tmp_file;
	}
	
	
	@Ignore
	public void testPhotoUploadAndDownload() throws IOException
	{
		Long[] test_length = new Long[]{1024L, 4096L, 128 * 1024L, 1 * 1024 * 1024L, 10 * 1024 * 1024L, 30 * 1024 * 1024L};
		
		byte[] chunk = new byte[1024];
		for (int i = 0 ; i<chunk.length; i++)
		{
			chunk[i] = (byte) (i);
		}
		
		for (Long l : test_length)
		{
			
			LOG.error("test file : {}", l);
			
			
			
			
		}

	}
}
