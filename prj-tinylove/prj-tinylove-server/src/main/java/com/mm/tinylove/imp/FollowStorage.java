package com.mm.tinylove.imp;

import com.google.protobuf.Message;
import com.mm.tinylove.IFollowObject;
import com.mm.tinylove.IRandSet;
import com.mm.tinylove.IUser;

public class FollowStorage<T extends Message> extends ProtoStorage<T>
		implements IFollowObject {

	public FollowStorage(long id) {
		super(id);
	}

	static String TAG_OBJ_FOLLOWERS = ":follwers";

	IRandSet<Long> getObjectsFollowers() {
		return LongRandSet.getIns(getKey() + TAG_OBJ_FOLLOWERS);
	}

	@Override
	public IRandSet<IUser> followers() {
		return new ObjectRandSet<IUser>(getObjectsFollowers()) {
			public IUser apply(Long id) {
				return Ins.getIUser(id);
			}
		};
	}

}
