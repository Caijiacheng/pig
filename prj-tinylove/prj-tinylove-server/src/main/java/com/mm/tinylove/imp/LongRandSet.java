package com.mm.tinylove.imp;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mm.tinylove.IRandSet;

public class LongRandSet implements IRandSet<Long>, IStorage{

	String key;
	Set<Long> new_ins = Sets.newTreeSet();
	
	public LongRandSet(String key)
	{
		this.key = key;
	}
	
	@Override
	public Set<Long> srandMember(int count) {
		return Ins.getLongRangeService().srandmem(key, count);
	}

	@Override
	public long size() {
		return Ins.getLongRangeService().scard(key) + new_ins.size();
	}

	@Override
	public Set<Long> sall() {
		Set<Long> s = Ins.getLongRangeService().sall(key);
		s.addAll(new_ins);
		return s;
	}

	@Override
	public void remove(Long e) {
		if (!new_ins.remove(e))
			Ins.getLongRangeService().srem(key, e);
		
	}

	@Override
	public void sadd(Long e) {
		new_ins.add(e);
	}
	
	@Override
	public Set<Long> saddCollection() {
		return new_ins;
	}

	@Override
	public byte[] marshalKey() {
		return this.key.getBytes(StandardCharsets.UTF_8);
	}

	@Override
	public void cleanAdd() {
		new_ins.clear();
	}

	@Override
	public List<Long> randMember(int count) {
		return Lists.newArrayList(srandMember(count));
	}

	@Override
	public List<Long> all() {
		return Lists.newArrayList(sall());
	}


}
