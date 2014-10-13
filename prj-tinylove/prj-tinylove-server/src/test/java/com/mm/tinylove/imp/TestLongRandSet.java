package com.mm.tinylove.imp;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

public class TestLongRandSet {

	String test_key = this.getClass().getCanonicalName();

	@Before
	public void setup() {
		Ins.s_storage_service = new RemoveStorageService();
		Ins.getStorageService().cleanStorage();
	}

	@Test
	public void testLongRandSet() {
		LongRandSet rand = new LongRandSet(test_key);

		Assert.assertEquals(rand.size(), 0);

		Long[] ls = new Long[] { 1L, 2L, 3L, 4L, 5L, 6L, 7L };

		for (Long i : ls) {
			rand.sadd(i);
			//dup add to set . not work
			rand.sadd(i);
		}
		Assert.assertEquals(rand.size(), ls.length);

		Assert.assertEquals(rand.randMember(ls.length * 3).size(), ls.length);
		
		rand.remove(ls[0]);
		Assert.assertEquals(rand.size(), ls.length-1);
	
		LongRandSet rand_1 = new LongRandSet(test_key);
		Assert.assertEquals(rand_1.size(), 0);
		
		Ins.getStorageService().save(rand);
		Assert.assertEquals(rand.saddCollection().size(), 0);
		Assert.assertEquals(rand_1.size(), rand.size());
		
		
		//test remove
		rand_1.remove(ls[1]);
		Assert.assertEquals(rand_1.size(), rand.size()-1);
		Ins.getStorageService().save(rand_1);
		Assert.assertEquals(rand.sremCollection().size(), 0);
		Assert.assertEquals(rand_1.size(), rand.size());

		Assert.assertEquals(rand.randMember(ls.length * 3).size(), rand.all().size());
		Assert.assertEquals(rand.randMember(1).size(), 1);
		
		Assert.assertTrue(rand_1.exist(ls[ls.length-1]));

	}
}
