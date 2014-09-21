package com.mm.photo.netty;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.Args;
import org.iq80.leveldb.DB;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.mm.photo.storage.LevelDB;

@RunWith(Parameterized.class)
public class TestPhotoServer {

	@Parameters
	public static List<Object[]> data() {
		return Arrays.asList(new Object[][] { { 100L, 20 }, { 2 * 1024L, 10 },
				{ 16 * 1024L, 8 }, { 64 * 1024L, 7 }, { 1 * 1024 * 1024L, 3 },
				{ 50 * 1024 * 1024L, 2 }, { 100 * 1024 * 1024L, 2 } });
	}

	private long flen;
	private int nConcurrent;

	public TestPhotoServer(long len, int nthread) {
		flen = len;
		nConcurrent = nthread;
	}

	static Logger LOG = LoggerFactory.getLogger(TestPhotoServer.class);

	static HttpPhotoServer server;

	public static String bytesToString(byte[] data) {
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f' };
		char[] temp = new char[data.length * 2];
		for (int i = 0; i < data.length; i++) {
			byte b = data[i];
			temp[i * 2] = hexDigits[b >>> 4 & 0x0f];
			temp[i * 2 + 1] = hexDigits[b & 0x0f];
		}
		return new String(temp);

	}

	static public String getMd5(InputStream fis) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
			byte[] buffer = new byte[8192];
			int length = -1;
			while ((length = fis.read(buffer)) != -1) {
				md.update(buffer, 0, length);
			}
			return bytesToString(md.digest());
		} catch (NoSuchAlgorithmException | IOException e) {
			throw new RuntimeException(e);
		}

	}

	@BeforeClass
	static public void init() {
		DB db = LevelDB.ins().getDB();
		LevelDB.ins().cleanDB(db);
		server = new HttpPhotoServer(SRV_PORT).run();
	}

	@AfterClass
	static public void uninit() {
		server.shutdown();
	}

	@Before
	public void setup() {
		LOG.info("-----------------------");
	}

	@After
	public void teardown() {
	}

	final static int SRV_PORT = 10001;
	final static String HTTP_LOCALHOST = "http://127.0.0.1:" + SRV_PORT;

	
	static class SlowFileEntity extends FileEntity
	{

		public SlowFileEntity(File file, ContentType contentType) {
			super(file, contentType);
		}
		

		@Override
		public void writeTo(OutputStream outstream) throws IOException {
			
			 Args.notNull(outstream, "Output stream");
		        final InputStream instream = new FileInputStream(this.file);
		        try {
		            final byte[] tmp = new byte[OUTPUT_BUFFER_SIZE*4];
		            int l;
		            while ((l = instream.read(tmp)) != -1) {
		            	try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
						}
//		            	LOG.error("slowFileEntity to send bytes");
		                outstream.write(tmp, 0, l);
		            }
		            outstream.flush();
		        } finally {
		            instream.close();
		        }
			
		}
	}
	
	static class HttpClientPostFile implements Runnable {
		String uri;
		String method = "POST";
		HttpURLConnection conn;
		FileEntity myfe;
		
		public HttpClientPostFile(String uri, File f) {
			this.uri = uri;
			if (myfe == null)
			{
				myfe = new FileEntity(f,
						ContentType.APPLICATION_OCTET_STREAM);
			}
			
		}

		public HttpClientPostFile(String uri, FileEntity myfe)
		{
			this.myfe = myfe;
			this.uri = uri;
		}
		
		int rescode = -1;

		public int getResCode() {
			return rescode;
		}

		@Override
		public void run() {
			CloseableHttpClient httpclient = HttpClients.createDefault();

			try {
				HttpPost httppost = new HttpPost(this.uri);

				httppost.setEntity(myfe);

				LOG.info("Executing request: " + httppost.getRequestLine());
				CloseableHttpResponse response = httpclient.execute(httppost);
				try {
					LOG.info("----------------------------------------");
					LOG.info("{}", response.getStatusLine());
					rescode = response.getStatusLine().getStatusCode();
					// EntityUtils.consume(response.getEntity());
				} finally {
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

		public File getDownloadFile() {
			Preconditions.checkNotNull(download_file);
			return download_file;
		}

		int rescode = -1;

		int getResponseCode() {
			return rescode;
		}

		@Override
		public void run() {
			URL url;
			try {
				url = new URL(uri);

				conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod(method);
				conn.setDoOutput(true);
				conn.setReadTimeout(0);
				conn.connect();

				if (conn.getResponseCode() == 200) {
					download_file = Files.createTempFile("http_tmp", null,
							new FileAttribute<?>[] {}).toFile();
					try (FileOutputStream out = new FileOutputStream(
							download_file)) {
						ByteStreams.copy(conn.getInputStream(), out);
					}
				}
				
				rescode = conn.getResponseCode();
				
				LOG.info("[{}]get the Response from: {}, ResCode:{}, RetMsg:{}. rescode:{}",
						this, url.toString(), conn.getResponseCode(),
						conn.getResponseMessage(), getResponseCode());
				
			} catch (Exception e) {
				LOG.error("", e);
			}

		}
	}

	@Test
	public void testNull() {
		Assert.assertTrue(true);
	}

	@Test
	public void testOneUploadAndDownload() throws IOException,
			NoSuchAlgorithmException {
		File tmp = getTmpFile(flen);

		String url = HTTP_LOCALHOST + "/test/" + tmp.getName();

		HttpClientPostFile post_ok = new HttpClientPostFile(url, tmp);
		post_ok.run();
		Assert.assertEquals(post_ok.getResCode(), HttpStatus.SC_OK);

		// #issue :
		// http://stackoverflow.com/questions/9161591/apache-httpclient-4-x-behaving-strange-when-uploading-larger-files

		if (flen < 16 * 1024) {
			LOG.error("begin to dup post to check NO_ACCEPTABLE");
			HttpClientPostFile post_failed = new HttpClientPostFile(url, tmp);

			post_failed.run();
			Assert.assertEquals(post_failed.getResCode(),
					HttpStatus.SC_NOT_ACCEPTABLE);
		}

		HttpClientGetFile downGet = new HttpClientGetFile(url);

		downGet.run();
		Assert.assertEquals(downGet.getResponseCode(), HttpStatus.SC_OK);

		LOG.info("tmp_file:{}, download_file:{}", tmp,
				downGet.getDownloadFile());

		Assert.assertEquals(getMd5(new FileInputStream(tmp)),
				getMd5(new FileInputStream(downGet.getDownloadFile())));

		HttpClientGetFile downGet_failed = new HttpClientGetFile(url + "212323");
		downGet_failed.run();
		Assert.assertEquals(downGet_failed.getResponseCode(),
				HttpStatus.SC_NOT_FOUND);

	}

	static File getTmpFile(long size) throws IOException {

		byte[] chunk = new byte[1024];
		for (int i = 0; i < chunk.length; i++) {
			chunk[i] = (byte) (i);
		}

		File tmp_file = Files.createTempFile("http_tmp", null,
				new FileAttribute<?>[] {}).toFile();
		OutputStream file = new FileOutputStream(tmp_file);
		long begin = 0;
		int bytes = 0;

		while (begin < size) {
			bytes = (int) (size - begin > chunk.length ? chunk.length : size
					- begin);
			file.write(chunk, 0, bytes);
			begin = begin + bytes;
		}
		file.close();

		Assert.assertEquals(tmp_file.length(), size);
		return tmp_file;
	}

	class UploadAndDownRunnable implements Runnable {

		boolean isOK = false;

		@Override
		public void run() {
			File tmp;
			try {
				tmp = getTmpFile(flen);

				String url = HTTP_LOCALHOST + "/test/" + tmp.getName();

				HttpClientPostFile post_ok = new HttpClientPostFile(url, tmp);
				post_ok.run();
				Assert.assertEquals(post_ok.getResCode(), HttpStatus.SC_OK);

				HttpClientGetFile downGet = new HttpClientGetFile(url);

				downGet.run();
				Assert.assertEquals(downGet.getResponseCode(), HttpStatus.SC_OK);

				LOG.info("tmp_file:{}, download_file:{}", tmp,
						downGet.getDownloadFile());

				Assert.assertEquals(getMd5(new FileInputStream(tmp)),
						getMd5(new FileInputStream(downGet.getDownloadFile())));

			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			isOK = true;
		}

		public boolean isOK() {
			return isOK;
		}

	}

	
	@Test
	public void testPhotoUploadAndDownloadMutext() throws InterruptedException, ExecutionException {

		if (flen > 30 * 1024 * 1024) {
			ScheduledExecutorService service = Executors.newScheduledThreadPool(3);

			List<Check_Runnalbe> runnables = Lists.newArrayList();
			List<Future<?>> futures = Lists.newArrayList();
			
			Upload_Post_OK r0 = new Upload_Post_OK();
			Upload_Get_Process r1 = new Upload_Get_Process(r0.url);
			Upload_Post_Process r2 = new Upload_Post_Process(r0.url);
			
			runnables.add(r0);
			runnables.add(r1);
			runnables.add(r2);
			
			futures.add(service.submit(r0));
			futures.add(service.schedule(r1, 500, TimeUnit.MILLISECONDS));
			futures.add(service.schedule(r2, 500, TimeUnit.MILLISECONDS));
			
			for (Future<?> f : futures) {
				f.get();
			}

			for (Check_Runnalbe r : runnables) {
				Assert.assertTrue(r.isOK);
			}
		}

	}
	
	@Test
	public void testPhotoUploadAndDownloadMultiThread() throws IOException,
			InterruptedException, ExecutionException {
		if (nConcurrent > 1) {
			ExecutorService service = Executors.newCachedThreadPool();

			List<UploadAndDownRunnable> runnables = Lists.newArrayList();
			List<Future<?>> futures = Lists.newArrayList();
			for (int i = 0; i < nConcurrent; i++) {
				UploadAndDownRunnable r = new UploadAndDownRunnable();
				runnables.add(r);
				futures.add(service.submit(r));
			}
			for (Future<?> f : futures) {
				f.get();
			}

			for (UploadAndDownRunnable r : runnables) {
				Assert.assertTrue(r.isOK);
			}

		}
	}

	abstract class Check_Runnalbe implements Runnable {
		boolean isOK = false;

		public boolean isOK() {
			return isOK;
		}
	}

	class Upload_Post_OK extends Check_Runnalbe {

		String url;
		File tmp;

		public Upload_Post_OK() {
			try {
				tmp = getTmpFile(flen);
			} catch (IOException e) {
			}
			url = HTTP_LOCALHOST + "/test/" + tmp.getName();
		}

		@Override
		public void run() {
			try {
				HttpClientPostFile post_ok = new HttpClientPostFile(url, 
						new SlowFileEntity(tmp, ContentType.APPLICATION_OCTET_STREAM));
				post_ok.run();
				Assert.assertEquals(post_ok.getResCode(), HttpStatus.SC_OK);
			} catch (Exception e) {
				LOG.error("post_ok. exception:{}", e);
				throw new RuntimeException(e);
			}
			isOK = true;
		}

		String url() {
			return url;
		}
	}

	class Upload_Post_Process extends Check_Runnalbe {
		private String url;

		public Upload_Post_Process(String url) {
			this.url = url;
		}

		@Override
		public void run() {
			File tmp;
			try {
				tmp = getTmpFile(1 * 1024);
				HttpClientPostFile post_ok = new HttpClientPostFile(url, tmp);
				post_ok.run();
				Assert.assertEquals(post_ok.getResCode(), HttpStatus.SC_LOCKED);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			isOK = true;
		}

	}

	class Upload_Get_Process extends Check_Runnalbe {
		private String url;

		public Upload_Get_Process(String url) {
			this.url = url;
		}

		@Override
		public void run() {
			try {
				HttpClientGetFile downGet = new HttpClientGetFile(url);

				downGet.run();
				Assert.assertEquals(downGet.getResponseCode(),
						HttpStatus.SC_LOCKED);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			isOK = true;
		}

	}

	

}
