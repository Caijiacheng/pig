package com.mm.tinylove.imp;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.mm.tinylove.IPair;
import com.mm.tinylove.IUser;
import com.mm.tinylove.proto.Storage.Pair;

public class DefaultPair extends ProtoStorage<Pair.Builder> implements IPair{

	public DefaultPair(long id) {
		super(id, Pair.newBuilder());
	}

	@Override
	public String name() {
		return getProto().getName();
	}

	@Override
	public List<IUser> user() {
		
		return Lists.transform(getProto().getUsersList(), new Function<Long, IUser>() {
			public IUser apply(Long id)
			{
				return Ins.getIUser(id);
			}
		});
	}

	@Override
	public IUser creator() {
		return Ins.getIUser(id);
	}

}
