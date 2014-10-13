package com.mm.tinylove.imp;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mm.tinylove.IRandSet;

public class LongRandSet implements IRandSet<Long>, IStorage {

	String key;
	Set<Long> new_ins = Sets.newTreeSet();
	Set<Long> rem_ins = Sets.newTreeSet();

	public LongRandSet(String key) {
		this.key = key;
	}

	@Override
	public Set<Long> srandMember(int count) {

		Set<Long> ret = Ins.getLongRangeService().srandmem(key, count);
		if (ret.size() < count && new_ins.size() > 0) {
			int rcnt = count - ret.size();
			for (Long l : new_ins) {
				ret.add(l);
				rcnt = rcnt - 1;
				if (rcnt == 0) {
					break;
				}
			}
		}
		ret.removeAll(rem_ins);
		return ret;
	}

	@Override
	public long size() {
		return Ins.getLongRangeService().scard(key) + new_ins.size()
				- rem_ins.size();
	}

	@Override
	public Set<Long> sall() {
		Set<Long> s = Ins.getLongRangeService().sall(key);
		s.addAll(new_ins);
		s.removeAll(rem_ins);
		return s;
	}

	@Override
	public void remove(Long e) {
		if (!new_ins.remove(e))
			rem_ins.add(e);
	}

	@Override
	public void sadd(Long e) {
		new_ins.add(e);
		rem_ins.remove(e);
	}

	@Override
	public Set<Long> saddCollection() {
		Set<Long> ret = new_ins;
		new_ins = Sets.newTreeSet();
		return ret;
	}

	@Override
	public byte[] marshalKey() {
		return this.key.getBytes(StandardCharsets.UTF_8);
	}

	@Override
	public List<Long> randMember(int count) {
		List<Long> ret = Lists.newArrayList(srandMember(count));
		return ret;
	}

	@Override
	public List<Long> all() {
		List<Long> ret = Lists.newArrayList(sall());
		ret.removeAll(rem_ins);
		return ret;
	}

	@Override
	public Set<Long> sremCollection() {
		Set<Long> ret = rem_ins;
		rem_ins = Sets.newTreeSet();
		return ret;
	}

}
