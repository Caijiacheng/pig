package com.mm.photo.data;

import org.junit.Test;
import org.iq80.leveldb.*;

import com.mm.photo.proto.Storage.ImageKey;

import static org.fusesource.leveldbjni.JniDBFactory.*;
import java.io.*;

import junit.framework.Assert;

public class TestLevelDB {
	
	@Test
	public void testLevelJni() throws IOException
	{
		Options options = new Options();
		options.createIfMissing(true);
		try (DB db = factory.open(new File("example"), options)){
			String k = "Tampa";
			String v = "rocks";
			db.put(bytes(k), bytes(v));
			String value = asString(db.get(bytes(k)));
			Assert.assertEquals(v, value);
			db.delete(bytes("Tampa"));
		} 
	}
	
	@Test
	public void testParseStream() throws IOException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		out.write(ImageKey.newBuilder().setIsSplit(true).setUrl("aaa").build().toByteArray());
		System.err.println("size:" + out.size());
		out.write(ImageKey.newBuilder().setIsSplit(false).setUrl("bbb").build().toByteArray());
		System.err.println("size:" + out.size());
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		System.err.println("size:" + out.toByteArray().length);
		ImageKey key1 = ImageKey.parseFrom(in);
		System.err.println(key1.toString());
//		Assert.assertTrue(key1.getIsSplit());
		
		ImageKey key2 = ImageKey.parseFrom(in);
		System.err.println(key2.toString());
//		Assert.assertFalse(key2.getIsSplit());
		
		ImageKey key3 = ImageKey.parseFrom(in);
		
//		Assert.assertTrue(false);

	}
	
}
