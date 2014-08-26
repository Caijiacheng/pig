package com.moshi.sde.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.locks.Lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceMisc
{
	static private LockMap<String> _resLockMap = 
			new LockMap<String>();

	static Logger s_logger = 
			LoggerFactory.getLogger(ResourceMisc.class);

	static Logger getLogger()
	{
		return s_logger;
	}
	
	
	public static File getResExec(String name)
	{
		Lock lock = _resLockMap.getLock(name);
		lock.lock();
		try
		{
			File res = new File(name);
			if (res.exists() && res.isFile())
			{
				return res;
			}
			InputStream io = ResourceMisc.class.getClassLoader()
					.getResourceAsStream(name);
			if (io == null)
			{
				throw new RuntimeException(name + " is not in the Resource");
			}
			FileOutputStream os = null;
			try {
				os = new FileOutputStream(res);
				int num = 8194;
				byte[] bytes = new byte[num];
				while (true)
				{
					int n = io.read(bytes);
					if (n > 0)
					{
						os.write(bytes, 0, n);
					}
					if (n == -1 )
					{
						break;
					}
				}
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}finally
			{
				try {
					os.flush();//???
					os.close();
				} catch (Exception e) {
					return null;
				}
				try
				{
					io.close();
				} catch (Exception e) {
					return null;
				}
			}
			return res;
		}finally
		{
			lock.unlock();
		}

	}
}
