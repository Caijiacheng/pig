package com.mm.tinylove.imp;

import com.mm.tinylove.IPair;
import com.mm.tinylove.IRandSet;
import com.mm.tinylove.IUser;
import com.mm.tinylove.proto.Storage.Pair;

public class DefaultPair extends FollowStorage<Pair> implements IPair {

	public DefaultPair(long id) {
		super(id);
	}

	// TODO:处理相同名字的pair.需要重新为名字做一个索引到pairid的映射
	static DefaultPair create(String name, IUser user) {
		DefaultPair pair = new DefaultPair(INVAID_KEY);
		Pair.Builder builder = pair.getKBuilder();
		builder.setName(name);
		builder.setCreator(user.id());
		pair.rebuildValueAndBrokenImmutable(builder);
		return pair;
	}

	@Override
	public String name() {
		return getProto().getName();
	}

	static final String USERS_TAG = ":users";

	IRandSet<Long> getPairsUserIDs() {
		return LongRandSet.getIns(getKey() + USERS_TAG);
	}

	@Override
	public IRandSet<IUser> user() {
		return new ObjectRandSet<IUser>(getPairsUserIDs()) {
			public IUser apply(Long id) {
				return Ins.getIUser(id);
			}
		};
	}

	@Override
	public IUser creator() {
		return Ins.getIUser(getProto().getCreator());
	}

}
