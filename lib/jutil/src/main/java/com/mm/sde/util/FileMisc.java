package com.moshi.sde.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;

public class FileMisc
{
	
	
	public static String getMD5(File file) {
		FileInputStream fis = null;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			fis = new FileInputStream(file);
			byte[] buffer = new byte[8192];
			int length = -1;
			while ((length = fis.read(buffer)) != -1) {
				md.update(buffer, 0, length);
			}
			return bytesToString(md.digest());
		} catch (Exception ex) {
			throw new Error(ex);
		} finally {
			try {
				if (fis != null)
				{
					fis.close();
				}
			} catch (IOException ex) 
			{
			}
		}

	}
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

    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     * @param dir 将要删除的文件目录
     * @return boolean Returns "true" if all deletions were successful.
     *                 If a deletion fails, the method stops attempting to
     *                 delete and returns "false".
     */
    static public boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }
    
    public static void copyFile(File sourceFile, File targetFile)
    {
        BufferedInputStream inBuff = null;
        BufferedOutputStream outBuff = null;
        try {
            inBuff = new BufferedInputStream(new FileInputStream(sourceFile));
            outBuff = new BufferedOutputStream(new FileOutputStream(targetFile));
            byte[] b = new byte[1024 * 8];
            int len;
            while ((len = inBuff.read(b)) != -1) {
                outBuff.write(b, 0, len);
            }
            // 刷新此缓冲的输出流
            
        }catch(Exception e){
        	throw new Error(e);
        }finally {
            // 关闭流
            
				try {
					if (inBuff != null)
						inBuff.close();
					if (outBuff != null)
		                outBuff.close();
				} catch (IOException e) {
				}
            
        }
    }
    
    public static Collection<String> getFileContent(File sourceFile, Charset charset) throws IOException
    {
    	BufferedReader r = null;
    	try
    	{
    		r = new BufferedReader(
        			new InputStreamReader(
        					new FileInputStream(sourceFile), charset));
        	
        	ArrayList<String> lines = new ArrayList<String>();
        	String line;
        	while ((line = r.readLine()) != null)
        	{
        		lines.add(new String(line.getBytes(charset), charset));
        	}
        	return lines;
    	}finally
    	{
    		if (r != null)
    		{
    			r.close();
    		}
    	}
    }
    
    public static void dumpToFile(File dstFile, Charset charset, String[] lines) throws IOException
    {
    	BufferedWriter w = null;
    	try
    	{
    		w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dstFile), charset));
    		
    		for (String line : lines)
    		{
    			String l = new String((line + "\n").getBytes(charset), charset);
    			w.write(l);
    		}
    	}
    	finally
    	{
    		if (w != null)
    		{
    			w.close();
    		}
    	}
    }
    
}
