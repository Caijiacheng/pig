package com.mm.tinylove.imp;

import java.util.List;

import com.google.common.base.Verify;
import com.google.common.collect.Lists;
import com.mm.tinylove.IRangeList;

public class LongRangeList implements IRangeList<Long>, IStorage{

	String key;
	List<Long> new_ins = Lists.newArrayList();
	
	
	public LongRangeList(String key)
	{
		this.key = key;
	}
	
	
	//TODO: support -1
	@Override
	public List<Long> range(long begin, long end) {
		
		Verify.verify(end > begin, "end must > begin");
		
		long new_begin = begin - new_ins.size();
		long new_end = end - new_ins.size();
		
		List<Long> oflist = Lists.newArrayList();
		if (new_begin <= 0)
		{
			int begin_index = (int) begin;
			
			if (new_end <= 0)
			{
				int end_index = (int)end;
				return Lists.newArrayList(new_ins.subList(begin_index, end_index));
			}else
			{
				int end_index = new_ins.size();
				oflist.addAll(new_ins.subList(begin_index, end_index));
				oflist.addAll(Ins.getLongRangeService().loadRange(key, 0, new_end));
				return oflist;
			}
		}else
		{
			return Ins.getLongRangeService().loadRange(key, new_begin, new_end);
		}
	}

	@Override
	public long size() {
		return Ins.getLongRangeService().lsize(key) + new_ins.size();
	}

	@Override
	public void lpush(Long e) {
		new_ins.add(e);
	}

	@Override
	public List<Long> lpushCollection() {
		return Lists.newCopyOnWriteArrayList(new_ins);
	}

	@Override
	public String marshalKey() {
		return key;
	}

	@Override
	public List<Long> all() {
		return range(0, size());
	}

	@Override
	public void cleanlpush() {
		new_ins.clear();
	}

}
