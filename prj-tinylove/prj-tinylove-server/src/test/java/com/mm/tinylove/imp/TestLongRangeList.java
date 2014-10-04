package com.mm.tinylove.imp;

import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class TestLongRangeList {

	String test_key = "test:111";
	
	@Before
	public void setup()
	{
		Ins.s_storage_service = new RemoveStorageService();
		Ins.getStorageService().remove(test_key);
	}
	
	
	static Logger LOG = LoggerFactory.getLogger(TestLongRangeList.class);
	
	@Test
	public void testLongRangeList()
	{
		LongRangeList range = new LongRangeList(test_key);
		Assert.assertEquals(range.size(), 0);
		range.lpush(new Long(1L));
		range.lpush(new Long(2L));
		Assert.assertEquals(range.lpushCollection().size(), 2);
		Assert.assertEquals(range.size(), 2);
		Assert.assertEquals(range.range(0, range.size()).size(), 2);
		
		
		LongRangeList range1 = new LongRangeList(test_key);
		Assert.assertEquals(range1.size(), 0);

		List<IStorage> ins_to_save = Lists.newArrayList();
		ins_to_save.add(range);
		
		Ins.getStorageService().saveInTransaction(ins_to_save);
		Assert.assertEquals(range1.size(), range.size());
		List<Long> a = range1.range(0, 2);
		Assert.assertEquals(a.get(0), new Long(2L));
		Assert.assertEquals(a.get(1), new Long(1L));
		
	}
	
	@Test
	public void testLongRangeOperRange()
	{
		LongRangeList range = new LongRangeList(test_key);
		Assert.assertEquals(range.size(), 0);
		
		Long[] ls = new Long[]{1L, 2L, 3L, 4L, 5L, 6L, 7L};
		
		for (Long l : ls)
		{
			range.lpush(l);
		}
		
		List<IStorage> ins_to_save = Lists.newArrayList();
		ins_to_save.add(range);
		Ins.getStorageService().saveInTransaction(ins_to_save);
		
		
		Assert.assertEquals(range.size(), ls.length);
		Assert.assertEquals(range.all().size(), ls.length);
		Assert.assertEquals(range.new_ins.size(), 0);

		LOG.error("rangeAll: {}", range.all());

	
		for (Long l : ls)
		{
			LOG.error("range: {}", l);

			range.lpush(l);
		}
		
		Ins.getStorageService().saveInTransaction(ins_to_save);
		LOG.error("rangeAll: {}", range.all());

		Assert.assertEquals(range.size(), ls.length * 2);
		Assert.assertEquals(range.all().size(), ls.length * 2);
		
		// now , we have 7 objs in storage and 7 objs in mem
		Assert.assertEquals(range.range(0,  ls.length*2).size(), ls.length * 2);
		Assert.assertEquals(range.range(0,  ls.length*10).size(), ls.length * 2);
		LOG.error("rangeAll: {}", range.all());
		
		Assert.assertEquals(range.range(0, 2).get(0), new Long(7L));
		Assert.assertEquals(range.range(0, 2).get(1), new Long(6L));
		Assert.assertEquals(range.range(ls.length, ls.length + 2).get(1), new Long(6L));
		Assert.assertEquals(range.range(ls.length-1, ls.length + 2).get(0), new Long(1L));
		Assert.assertEquals(range.range(ls.length-1, ls.length + 2).get(1), new Long(7L));
		Assert.assertEquals(range.range(2*ls.length-1, ls.length).get(0), new Long(1L));
	}
	
}
