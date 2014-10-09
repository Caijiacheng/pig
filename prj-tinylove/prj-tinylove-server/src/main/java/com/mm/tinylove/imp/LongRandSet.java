package com.mm.tinylove.imp;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.Sets;
import com.mm.tinylove.IRandSet;

public class LongRandSet implements IRandSet<Long> {

	String key;
	Set<Long> new_ins = Sets.newTreeSet();
	
	public LongRandSet(String key)
	{
		this.key = key;
	}
	
	@Override
	public Set<Long> randMember(int count) {
		
		
		
		return null;
	}

	@Override
	public long size() {
		return 0;
	}

	@Override
	public Set<Long> all() {
		return null;
	}

	@Override
	public void remove(Long e) {
		
	}

}
