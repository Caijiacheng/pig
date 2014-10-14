package com.mm.tinylove.imp;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import com.google.common.base.Verify;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mm.tinylove.IRangeList;

public class LongRangeList implements IRangeList<Long>, IStorage {

	String key;
	List<Long> new_ins = Lists.newArrayList();

	LongRangeList(String key) {
		this.key = key;
	}

	static ThreadLocal<Map<String, LongRangeList>> TL_LONG_RANGE_LIST = 
			new ThreadLocal<Map<String, LongRangeList>>() {
		protected Map<String,LongRangeList> initialValue() {
			return Maps.newHashMap();
		};

	};

	static public LongRangeList getIns(String key) {
		LongRangeList ins = TL_LONG_RANGE_LIST.get().get(key);
		if (ins == null)
		{
			ins = new LongRangeList(key);
			TL_LONG_RANGE_LIST.get().put(key, ins);
		}
		return ins;
	}

	// TODO: support end = -1
	@Override
	public List<Long> range(long begin, long end) {

		Verify.verify(end >= begin, "end must > begin");

		if (begin == end) {
			return Lists.newArrayList();
		}

		long new_begin = begin - new_ins.size();
		long new_end = end - new_ins.size();

		List<Long> oflist = Lists.newArrayList();
		if (new_begin <= 0) {
			int begin_index = (int) begin;

			if (new_end <= 0) {
				int end_index = (int) end;
				return Lists.newArrayList(new_ins.subList(begin_index,
						end_index));
			} else {
				int end_index = new_ins.size();
				oflist.addAll(new_ins.subList(begin_index, end_index));
				oflist.addAll(Ins.getLongRangeService().lloadRange(key, 0,
						new_end));
				return oflist;
			}
		} else {
			return Ins.getLongRangeService()
					.lloadRange(key, new_begin, new_end);
		}
	}

	@Override
	public long size() {
		return Ins.getLongRangeService().lsize(key) + new_ins.size();
	}

	@Override
	public void lpush(Long e) {
		new_ins.add(e);
		StorageSaveRunnable.add2Save(this);
	}

	@Override
	public List<Long> savelpushCollection() {
		List<Long> ret = new_ins;
		new_ins = Lists.newArrayList();
		return ret;
	}

	@Override
	public byte[] marshalKey() {
		return key.getBytes(StandardCharsets.UTF_8);
	}

	@Override
	public List<Long> all() {
		return range(0, size());
	}

	@Override
	public boolean exist(Long ins) {

		if (new_ins.contains(ins)) {
			return true;
		}

		if (all().contains(ins)) {
			return true;
		}

		return false;
	}
}
