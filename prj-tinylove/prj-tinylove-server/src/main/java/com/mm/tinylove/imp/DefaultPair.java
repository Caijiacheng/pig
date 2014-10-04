package com.mm.tinylove.imp;

import com.mm.tinylove.IPair;
import com.mm.tinylove.IRangeList;
import com.mm.tinylove.IUser;
import com.mm.tinylove.proto.Storage.Pair;

public class DefaultPair extends ProtoStorage<Pair.Builder> implements IPair{

	public DefaultPair(long id) {
		super(id, Pair.newBuilder());
	}
	
	static DefaultPair create(String name, long creatorid)
	{
		DefaultPair pair =  new DefaultPair(INVAID_KEY);
		pair.getProto().setName(name);
		pair.getProto().setCreator(creatorid);
		return pair;
	}
	
	@Override
	public String name() {
		return getProto().getName();
	}
	
	static final String USERS_TAG = ":users";
	
	IRangeList<Long> getPairsUserIDs()
	{
		return new LongRangeList(getKey()+USERS_TAG);
	}
	
	@Override
	public IRangeList<IUser> user() {
		return new ImmutableObjectRangeList<IUser>(getPairsUserIDs()) 
				{
				public IUser apply(Long id)
				{
					return Ins.getIUser(id);
				}
		};
	}

	@Override
	public IUser creator() {
		return Ins.getIUser(id);
	}

}
