package com.mm.tinylove;

import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class TestLists {

	
	static Logger LOG = LoggerFactory.getLogger(TestLists.class);
	
	@Test
	public void testTranform()
	{
		ImmutableList<String> a = ImmutableList.of("1","2", "3");
		
		List<Long> b = Lists.transform(a,
				new Function<String, Long>() {
					public Long apply(String input) {
						return null;
					}
				});
		
		LOG.error("{}", b);
		
	}
}
